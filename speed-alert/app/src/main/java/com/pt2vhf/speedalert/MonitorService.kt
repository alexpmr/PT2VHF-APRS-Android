package com.pt2vhf.speedalert

import android.Manifest
import android.app.*
import android.content.*
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.location.*
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.*
import java.util.Locale

class MonitorService:Service() {
  data class State(val speed:Double,val limit:Double,val status:String,val source:String,val alarm:Boolean)
  companion object {
    const val START="com.pt2vhf.speedalert.START"
    const val STOP="com.pt2vhf.speedalert.STOP"
    const val UPDATE="com.pt2vhf.speedalert.UPDATE"
    @Volatile var latest:State?=null
  }
  private val handler=Handler(Looper.getMainLooper())
  private val prefs by lazy { getSharedPreferences("config",MODE_PRIVATE) }
  private lateinit var locationManager:LocationManager
  private lateinit var roads:Roads
  private var tone:ToneGenerator?=null
  private var playing=false
  private var running=false
  private var lastFix=0L
  private val beep=object:Runnable {
    override fun run() {
      if(!playing) return
      try { tone?.startTone(ToneGenerator.TONE_PROP_BEEP,250) } catch(_:Exception) {}
      handler.postDelayed(this,600L)
    }
  }
  private val checkStale=object:Runnable {
    override fun run() {
      if(running && SystemClock.elapsedRealtime()-lastFix>5000L)
        publish(State(-1.0,-1.0,"GPS INDISPONÍVEL","--",false))
      if(running) handler.postDelayed(this,1500L)
    }
  }
  private val listener=object:LocationListener {
    override fun onLocationChanged(loc:Location) { onFix(loc) }
    override fun onProviderDisabled(provider:String) {
      publish(State(-1.0,-1.0,"GPS DESLIGADO","--",false))
    }
  }
  override fun onCreate() {
    super.onCreate()
    locationManager=getSystemService(LOCATION_SERVICE) as LocationManager
    roads=Roads(this)
    tone=ToneGenerator(AudioManager.STREAM_MUSIC,80)
    (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(
      NotificationChannel("speed","PT2VHF velocidade",NotificationManager.IMPORTANCE_LOW))
  }
  override fun onStartCommand(intent:Intent?,flags:Int,startId:Int):Int {
    if(intent?.action==STOP) {
      running=false;handler.removeCallbacks(checkStale);stopBeep()
      try { locationManager.removeUpdates(listener) } catch(_:Exception){}
      stopForeground(STOP_FOREGROUND_REMOVE);stopSelf()
      latest=null
      return START_NOT_STICKY
    }
    if(intent?.action!=START || running) return START_NOT_STICKY
    if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED) {
      stopSelf();return START_NOT_STICKY
    }
    if(Build.VERSION.SDK_INT>=29)
      startForeground(101,notification("Aguardando GPS"),ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION)
    else startForeground(101,notification("Aguardando GPS"))
    running=true;lastFix=SystemClock.elapsedRealtime()
    publish(State(-1.0,-1.0,"AGUARDANDO GPS","--",false))
    try { locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000L,0f,listener,Looper.getMainLooper()) }
    catch(_:Exception) { publish(State(-1.0,-1.0,"FALHA GPS","--",false)) }
    handler.postDelayed(checkStale,1500L)
    return START_NOT_STICKY
  }
  private fun onFix(loc:Location) {
    val age=(SystemClock.elapsedRealtimeNanos()-loc.elapsedRealtimeNanos)/1000000L
    if(age<0 || age>4500 || !loc.hasSpeed() || !loc.hasAccuracy() || loc.accuracy>50f ||
      (Build.VERSION.SDK_INT>=26 && loc.hasSpeedAccuracy() && loc.speedAccuracyMetersPerSecond>4f)) {
      publish(State(-1.0,-1.0,"GPS IMPRECISO","--",false));return
    }
    lastFix=SystemClock.elapsedRealtime()
    val speed=loc.speed.toDouble()*3.6
    val selected=if(prefs.getBoolean("manual",false))
      Roads.Result(prefs.getInt("manualLimit",60).toDouble(),"MANUAL")
    else roads.resolve(loc)
    val max=selected.limit
    val excess=max!=null && speed>max
    publish(State(speed,max?:-1.0,
      if(max==null) "LIMITE DESCONHECIDO" else if(excess) "VELOCIDADE EXCEDIDA" else "DENTRO DO LIMITE",
      selected.source,excess))
  }
  private fun publish(s:State) {
    latest=s
    if(s.alarm) startBeep() else stopBeep()
    sendBroadcast(Intent(UPDATE).setPackage(packageName).apply {
      putExtra("speed",s.speed);putExtra("limit",s.limit)
      putExtra("status",s.status);putExtra("source",s.source);putExtra("alarm",s.alarm)
    })
    val speed=if(s.speed>=0) String.format(Locale.US,"%.1f km/h",s.speed) else "GPS indisponível"
    (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).notify(
      101,notification(speed+" — "+s.status))
  }
  private fun startBeep() {
    if(playing) return
    playing=true;handler.post(beep)
  }
  private fun stopBeep() {
    if(!playing) return
    playing=false;handler.removeCallbacks(beep)
    try { tone?.stopTone() } catch(_:Exception){}
  }
  private fun notification(s:String):Notification {
    val open=PendingIntent.getActivity(this,1,Intent(this,MainActivity::class.java),
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    val stop=PendingIntent.getService(this,2,Intent(this,MonitorService::class.java).setAction(STOP),
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    return Notification.Builder(this,"speed").setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
      .setContentTitle("PT2VHF Speed Alert").setContentText(s).setContentIntent(open)
      .setOngoing(true).addAction(Notification.Action.Builder(null,"Parar",stop).build()).build()
  }
  override fun onDestroy() {
    running=false;handler.removeCallbacks(checkStale);stopBeep()
    try { locationManager.removeUpdates(listener) } catch(_:Exception){}
    tone?.release();latest=null
    super.onDestroy()
  }
  override fun onBind(intent:Intent?):IBinder?=null
}
