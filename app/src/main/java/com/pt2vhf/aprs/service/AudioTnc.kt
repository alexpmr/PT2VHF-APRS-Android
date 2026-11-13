package com.pt2vhf.aprs.service

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.AudioDeviceInfo
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.os.Build
import androidx.core.content.ContextCompat
import com.pt2vhf.aprs.core.AfskDemodulator
import com.pt2vhf.aprs.core.AfskModulator
import com.pt2vhf.aprs.core.Ax25
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

/** Configuração do modem de áudio (#44). */
data class AudioConfig(
    val input: String = "default",
    val output: String = "default",
    val volume: Int = 70,
    val txDelayMs: Int = 300,
    val voxLeadMs: Int = 0,
    val highQuality: Boolean = true,
    // #93 PTT: vox | rts | dtr | cat (o acionamento em si vem de fora, por [AudioTnc.ptt])
    val pttMode: String = "vox",
    val pttBaud: Int = 9600,
    val catProtocol: String = "kenwood",
    val civAddress: Int = 0x94,
)

/**
 * O celular como TNC (#44): ouve AFSK 1200 na entrada de áudio e transmite pela saída, com PTT por VOX.
 * RX e TX usam o mesmo fluxo de pacotes RF dos TNCs KISS. Enquanto transmite, a recepção é ignorada
 * (half-duplex) para não decodificar o próprio pacote.
 */
class AudioTnc(
    context: Context,
    private val onFrame: (String) -> Unit,
    private val onState: (connected: Boolean, name: String, error: String) -> Unit,
    private val onLevel: (level: Double, transmitting: Boolean) -> Unit,
) {
    private val app = context.applicationContext
    private val audio = app.getSystemService(AudioManager::class.java)
    @Volatile private var running = false
    @Volatile private var listening = false
    private val transmitting = AtomicBoolean(false)
    private val epoch = AtomicLong(0L)
    @Volatile private var demod: AfskDemodulator? = null
    @Volatile private var config = AudioConfig()
    @Volatile private var scoStarted = false
    private val rate = 48_000

    /**
     * #93: PTT por hardware (RTS/DTR/CAT). null = VOX. Recebe true para entrar em TX e false para sair; devolve
     * false se não conseguiu (aí o pacote não é transmitido).
     */
    @Volatile var ptt: ((Boolean) -> Boolean)? = null

    val connected: Boolean get() = running && listening

    fun start(cfg: AudioConfig) {
        stop()
        config = cfg
        if (ContextCompat.checkSelfPermission(app, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            onState(false, NAME, ERR_PERMISSION)
            return
        }
        running = true
        val session = epoch.incrementAndGet()
        Thread({ rxLoop(session) }, "afsk-rx").apply { isDaemon = true; start() }
    }

    fun stop() {
        running = false
        epoch.incrementAndGet()
        listening = false
        stopSco()
    }

    private fun inputDevice(kind: String): AudioDeviceInfo? {
        val types = when (kind) {
            "wired" -> setOf(AudioDeviceInfo.TYPE_WIRED_HEADSET)
            "usb" -> setOf(AudioDeviceInfo.TYPE_USB_DEVICE, AudioDeviceInfo.TYPE_USB_HEADSET)
            "bt" -> setOf(AudioDeviceInfo.TYPE_BLUETOOTH_SCO)
            else -> return null
        }
        return audio?.getDevices(AudioManager.GET_DEVICES_INPUTS)?.firstOrNull { it.type in types }
    }

    private fun outputDevice(kind: String): AudioDeviceInfo? {
        val types = when (kind) {
            "wired" -> setOf(AudioDeviceInfo.TYPE_WIRED_HEADSET, AudioDeviceInfo.TYPE_WIRED_HEADPHONES)
            "usb" -> setOf(AudioDeviceInfo.TYPE_USB_DEVICE, AudioDeviceInfo.TYPE_USB_HEADSET)
            "bt" -> setOf(AudioDeviceInfo.TYPE_BLUETOOTH_SCO)
            else -> return null
        }
        return audio?.getDevices(AudioManager.GET_DEVICES_OUTPUTS)?.firstOrNull { it.type in types }
    }

    @Suppress("DEPRECATION")
    private fun startSco() {
        if (scoStarted || audio == null) return
        runCatching {
            if (Build.VERSION.SDK_INT >= 31) {
                val dev = audio.availableCommunicationDevices.firstOrNull { it.type == AudioDeviceInfo.TYPE_BLUETOOTH_SCO }
                if (dev != null) audio.setCommunicationDevice(dev)
            } else {
                audio.startBluetoothSco()
                audio.isBluetoothScoOn = true
            }
            scoStarted = true
        }
    }

    @Suppress("DEPRECATION")
    private fun stopSco() {
        if (!scoStarted || audio == null) return
        runCatching {
            if (Build.VERSION.SDK_INT >= 31) audio.clearCommunicationDevice()
            else { audio.isBluetoothScoOn = false; audio.stopBluetoothSco() }
        }
        scoStarted = false
    }

    @SuppressLint("MissingPermission")
    private fun rxLoop(session: Long) {
        val cfg = config
        if (cfg.input == "bt" || cfg.output == "bt") { startSco(); Thread.sleep(800) }
        val source = when (cfg.input) {
            "unprocessed" -> if (Build.VERSION.SDK_INT >= 24) MediaRecorder.AudioSource.UNPROCESSED else MediaRecorder.AudioSource.VOICE_RECOGNITION
            "bt" -> MediaRecorder.AudioSource.VOICE_COMMUNICATION
            else -> MediaRecorder.AudioSource.VOICE_RECOGNITION // sem AGC/supressão de ruído agressivos na maioria dos aparelhos
        }
        val minBuf = AudioRecord.getMinBufferSize(rate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
        if (minBuf <= 0) { onState(false, NAME, ERR_AUDIO); running = false; return }
        val record = try {
            AudioRecord(source, rate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, maxOf(minBuf * 4, rate / 2))
        } catch (e: Exception) {
            onState(false, NAME, e.message ?: ERR_AUDIO); running = false; return
        }
        if (record.state != AudioRecord.STATE_INITIALIZED) {
            record.release(); onState(false, NAME, ERR_AUDIO); running = false; return
        }
        inputDevice(cfg.input)?.let { runCatching { record.setPreferredDevice(it) } }
        val d = AfskDemodulator(rate, cfg.highQuality)
        demod = d
        try {
            record.startRecording()
            if (record.recordingState != AudioRecord.RECORDSTATE_RECORDING) throw IllegalStateException(ERR_AUDIO)
            listening = true
            onState(true, NAME, "")
            val buf = ShortArray(2048)
            var lastLevel = 0L
            while (running && epoch.get() == session) {
                val n = record.read(buf, 0, buf.size)
                if (n < 0) throw IllegalStateException("AudioRecord $n")
                if (n == 0) continue
                if (transmitting.get()) continue // half-duplex
                for (frame in d.feed(buf, n)) runCatching { onFrame(Ax25.toTnc2(frame)) }
                val now = System.currentTimeMillis()
                if (now - lastLevel >= 250) { lastLevel = now; onLevel(d.level, false) }
            }
            if (epoch.get() == session) onState(false, NAME, "")
        } catch (e: Exception) {
            if (epoch.get() == session) onState(false, NAME, e.message ?: ERR_AUDIO)
        } finally {
            listening = false
            runCatching { record.stop() }
            record.release()
            if (demod === d) demod = null
        }
    }

    /** Transmite um pacote TNC2 (chamar só da thread de TX). true após o áudio ser tocado por inteiro. */
    fun send(tnc2: String): Boolean {
        if (!connected) return false
        val frame = runCatching { Ax25.fromTnc2(tnc2) }.getOrNull() ?: return false
        // Espera curta enquanto outra estação transmite (detecção de portadora pelo HDLC).
        val waitUntil = System.currentTimeMillis() + 3_000
        while (demod?.busy == true && System.currentTimeMillis() < waitUntil) Thread.sleep(50)
        val cfg = config
        val samples = AfskModulator(rate).modulate(frame, cfg.txDelayMs, cfg.voxLeadMs, 40, cfg.volume / 100.0)
        return play(samples)
    }

    /** Tom de teste de 3 s para ajustar nível/VOX (#44); não depende de pacote. */
    fun testTone(): Boolean = play(AfskModulator(rate).testTone(3.0, config.volume / 100.0))

    private fun play(samples: ShortArray): Boolean {
        if (!transmitting.compareAndSet(false, true)) return false
        val cfg = config
        onLevel(0.0, true)
        val usage = if (cfg.output == "bt") AudioAttributes.USAGE_VOICE_COMMUNICATION else AudioAttributes.USAGE_MEDIA
        var track: AudioTrack? = null
        val keyer = ptt
        return try {
            if (keyer != null) {
                if (!keyer(true)) return false
                Thread.sleep(PTT_LEAD_MS)
            }
            val t = AudioTrack.Builder()
                .setAudioAttributes(AudioAttributes.Builder().setUsage(usage)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build())
                .setAudioFormat(AudioFormat.Builder().setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(rate).setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build())
                .setTransferMode(AudioTrack.MODE_STATIC)
                .setBufferSizeInBytes(samples.size * 2)
                .build()
            track = t
            outputDevice(cfg.output)?.let { runCatching { t.setPreferredDevice(it) } }
            val written = t.write(samples, 0, samples.size)
            if (written != samples.size) return false
            t.play()
            val durationMs = samples.size * 1000L / rate
            val deadline = System.currentTimeMillis() + durationMs + 1_000
            while (t.playbackHeadPosition < samples.size && System.currentTimeMillis() < deadline) Thread.sleep(20)
            Thread.sleep(60) // cauda para o VOX não cortar o último flag
            true
        } catch (e: Exception) {
            false
        } finally {
            runCatching { track?.stop() }
            track?.release()
            if (keyer != null) runCatching { keyer(false) } // solta o PTT sempre, mesmo em erro
            transmitting.set(false)
            onLevel(0.0, false)
        }
    }

    companion object {
        const val NAME = "AFSK"
        /** Espera entre acionar o PTT e começar o áudio (o TX delay do AX.25 vem depois, em flags). */
        const val PTT_LEAD_MS = 40L
        const val ERR_PERMISSION = "Permissão de microfone negada"
        const val ERR_AUDIO = "Falha ao abrir o áudio"
    }
}
