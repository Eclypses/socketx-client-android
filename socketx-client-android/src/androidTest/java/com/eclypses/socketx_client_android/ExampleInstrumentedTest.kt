package com.eclypses.socketx_client_android

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.eclypses.mte.MteBase
import okhttp3.Dns
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.net.InetAddress
import java.net.Proxy
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Instrumented test, which will execute on an Android device.
 * This test performs a full, end-to-end connection to the test server.
 */
@RunWith(AndroidJUnit4::class)
class SocketXIntegrationTest {

    private lateinit var okHttpClient: OkHttpClient
    private lateinit var socketXClient: SocketXClient

    @Before
    fun setup() {
        // 1. Initialize MTE License.
        val licensed = MteBase.initLicense(Settings.licCompanyName, Settings.licCompanyKey)
        assertTrue("MTE License check must pass", licensed)

        // 2. Create an OkHttpClient with the necessary DNS override for the test server.
        val customDns = object : Dns {
            override fun lookup(hostname: String): List<InetAddress> {
                if (hostname.equals("dev-socketx-server.eclypses.com", ignoreCase = true)) {
                    return listOf(InetAddress.getByAddress(hostname, InetAddress.getByName("10.102.160.80").address))
                }
                return Dns.SYSTEM.lookup(hostname)
            }
        }
        okHttpClient = OkHttpClient.Builder()
            .readTimeout(0, TimeUnit.MILLISECONDS)
            .proxy(Proxy.NO_PROXY)
            .dns(customDns)
            .build()

        // 3. Create the SocketXClient factory.
        socketXClient = SocketXClient(okHttpClient)
    }

    @Test
    fun connectAndEcho_succeeds() {
        val latch = CountDownLatch(1)
        val testMessage = "Hello from Instrumented Test!"
        val expectedEcho = "[dogs]: $testMessage"
        var receivedMessage = ""

        val listener = object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                webSocket.send(testMessage)
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                // The test might receive multiple messages (e.g., a welcome banner).
                // We must specifically wait for the one we expect.
                if (text == expectedEcho) {
                    receivedMessage = text
                    latch.countDown()
                    webSocket.close(1000, "Test complete")
                } else {
                    // This is an unexpected message, like the welcome banner. Ignore it.
                    println("Test ignoring message: $text")
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                fail("WebSocket connection failed: ${t.message}")
                latch.countDown()
            }
        }

        val request = Request.Builder().url("wss://dev-socketx-server.eclypses.com/ws/dogs").build()
        socketXClient.newWebSocket(request, listener)

        val success = latch.await(10, TimeUnit.SECONDS)

        assertTrue("Test did not complete in time.", success)
        assertEquals(expectedEcho, receivedMessage)
    }
}
