package com.pt2vhf.aprs.service

import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.ServerSocket
import java.util.concurrent.CountDownLatch
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AprsIsConnectionTest {
    @Test fun validTransmitDoesNotDropVerifiedSession() {
        val received = LinkedBlockingQueue<String>()
        val releaseServer = CountDownLatch(1)
        val loginVerified = CountDownLatch(1)
        val stillVerified = AtomicBoolean(false)
        ServerSocket(0).use { server ->
            val serverThread = thread(name = "test-aprs-server", isDaemon = true) {
                runCatching {
                    server.accept().use { socket ->
                        socket.soTimeout = 6000
                        val reader = BufferedReader(InputStreamReader(socket.getInputStream(), Charsets.ISO_8859_1))
                        val login = reader.readLine()
                        if (login.startsWith("user ")) {
                            socket.getOutputStream().write("# logresp N0CALL verified, server TEST\r\n".toByteArray())
                            socket.getOutputStream().flush()
                            received.offer(reader.readLine())
                            releaseServer.await(4, TimeUnit.SECONDS)
                        }
                    }
                }
            }
            val client = AprsIsClient(onLine = {}, onState = { connected, verified, _ ->
                stillVerified.set(connected && verified)
                if (connected && verified) loginVerified.countDown()
            })
            try {
                client.start("127.0.0.1", server.localPort, "N0CALL", 12345, "")
                assertTrue("test server did not verify session", loginVerified.await(4, TimeUnit.SECONDS))
                val beacon = "N0CALL>APZVHF,TCPIP*:>Test beacon"
                assertTrue("socket write was rejected", client.send(beacon))
                assertEquals(beacon, received.poll(4, TimeUnit.SECONDS))
                assertTrue("sending should not close a verified APRS-IS socket", stillVerified.get())
            } finally {
                releaseServer.countDown()
                client.stop()
                serverThread.join(1000)
            }
        }
    }
}
