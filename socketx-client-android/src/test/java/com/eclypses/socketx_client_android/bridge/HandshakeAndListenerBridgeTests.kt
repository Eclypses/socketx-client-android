package com.eclypses.socketx_client_android.bridge

import com.eclypses.socketx_client_android.ActionByte
import com.eclypses.socketx_client_android.HandshakeManager
import com.eclypses.socketx_client_android.Header
import com.eclypses.socketx_client_android.MteInternalWebSocketListener
import com.eclypses.socketx_client_android.MteSecureWebSocket
import com.eclypses.socketx_client_android.SocketXError
import com.eclypses.socketx_client_android.fixtures.TestFixtures
import com.eclypses.socketx_client_android.infrastructure.FakeCodecEngine
import com.eclypses.socketx_client_android.infrastructure.RecordingWebSocket
import com.eclypses.socketx_client_android.infrastructure.testResponse
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class HandshakeAndListenerBridgeTests {

    @Test
    fun `onOpen starts handshake by emitting REQUEST action`() {
        val sent = mutableListOf<Triple<ActionByte, Byte, ByteArray>>()
        val fakeCodec = FakeCodecEngine()
        val userListener = RecordingListener()

        lateinit var secure: MteSecureWebSocket
        val manager = HandshakeManager(
            urlString = TestFixtures.Endpoints.URL_GENERAL,
            reportError = { err -> userListener.onFailure(secure, err, null) },
            sendData = { action, type, payload -> sent += Triple(action, type, payload) },
            codecEngine = fakeCodec
        )

        val realSocket = RecordingWebSocket()
        secure = MteSecureWebSocket(realSocket, manager)

        val listener = MteInternalWebSocketListener(userListener, manager)
        listener.secureWebSocket = secure

        listener.onOpen(realSocket, testResponse())

        assertEquals(1, sent.size)
        assertEquals(ActionByte.REQUEST, sent[0].first)
        assertEquals((-2).toByte(), sent[0].second)
        assertArrayEquals(byteArrayOf(), sent[0].third)
    }

    @Test
    fun `listener forwards closing closed and failure to user listener with secure websocket`() {
        val fakeCodec = FakeCodecEngine()
        val userListener = RecordingListener()
        val manager = HandshakeManager(
            urlString = TestFixtures.Endpoints.URL_GENERAL,
            reportError = {},
            sendData = { _, _, _ -> },
            codecEngine = fakeCodec
        )
        val realSocket = RecordingWebSocket()
        val secure = MteSecureWebSocket(realSocket, manager)
        val listener = MteInternalWebSocketListener(userListener, manager)
        listener.secureWebSocket = secure

        listener.onClosing(realSocket, 1000, "normal")
        listener.onClosed(realSocket, 1000, "normal")
        listener.onFailure(realSocket, TestFixtures.Errors.network, null)

        assertTrue(userListener.lastClosingSocket === secure)
        assertTrue(userListener.lastClosedSocket === secure)
        assertTrue(userListener.lastFailureSocket === secure)
        assertTrue(userListener.lastFailureThrowable is SocketXError.NetworkError)
    }

    @Test
    fun `process incoming binary proxy data reaches message callback after pairing`() {
        val sent = mutableListOf<Triple<ActionByte, Byte, ByteArray>>()
        val fakeCodec = FakeCodecEngine().apply {
            encodeTransform = { input -> "enc:${String(input)}".toByteArray() }
            decodeTransform = { input -> String(input).removePrefix("enc:").toByteArray() }
        }

        val manager = HandshakeManager(
            urlString = TestFixtures.Endpoints.URL_GENERAL,
            reportError = {},
            sendData = { action, type, payload -> sent += Triple(action, type, payload) },
            codecEngine = fakeCodec
        )

        var receivedText: String? = null
        manager.onMessageReceived = { receivedText = it }

        manager.startHandshake()
        manager.processIncomingData(Header.wrap(ActionByte.RESPONSE, -2, byteArrayOf()))
        manager.processIncomingData(Header.wrap(ActionByte.PAIR_RESPONSE, -2, "pair-ok".toByteArray()))
        manager.processIncomingData(Header.wrap(ActionByte.UPSTREAM_CONNECTION_RESPONSE, -2, byteArrayOf()))

        manager.processIncomingData(
            Header.wrap(
                action = ActionByte.PROXY_DATA,
                messageType = 0,
                payload = "enc:${TestFixtures.TextPayloads.UNICODE}".toByteArray()
            )
        )

        assertEquals(TestFixtures.TextPayloads.UNICODE, receivedText)
    }

    private class RecordingListener : WebSocketListener() {
        var lastClosingSocket: WebSocket? = null
        var lastClosedSocket: WebSocket? = null
        var lastFailureSocket: WebSocket? = null
        var lastFailureThrowable: Throwable? = null

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            lastClosingSocket = webSocket
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            lastClosedSocket = webSocket
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            lastFailureSocket = webSocket
            lastFailureThrowable = t
        }
    }
}
