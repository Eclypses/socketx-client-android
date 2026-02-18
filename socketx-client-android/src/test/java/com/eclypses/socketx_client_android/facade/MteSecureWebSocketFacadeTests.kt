package com.eclypses.socketx_client_android.facade

import com.eclypses.socketx_client_android.ActionByte
import com.eclypses.socketx_client_android.HandshakeManager
import com.eclypses.socketx_client_android.MteSecureWebSocket
import com.eclypses.socketx_client_android.SocketXError
import com.eclypses.socketx_client_android.fixtures.TestFixtures
import com.eclypses.socketx_client_android.infrastructure.FakeCodecEngine
import com.eclypses.socketx_client_android.infrastructure.RecordingWebSocket
import okio.ByteString.Companion.toByteString
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class MteSecureWebSocketFacadeTests {

    @Test
    fun `delegates close cancel queueSize and request to underlying websocket`() {
        val fakeCodec = FakeCodecEngine()
        val errors = mutableListOf<SocketXError>()
        val manager = HandshakeManager(
            urlString = TestFixtures.Endpoints.URL_GENERAL,
            reportError = { errors += it },
            sendData = { _, _, _ -> },
            codecEngine = fakeCodec
        )
        val realSocket = RecordingWebSocket().apply { queueSizeValue = 42L }
        val secureSocket = MteSecureWebSocket(realSocket, manager)

        val closeResult = secureSocket.close(1000, "done")
        secureSocket.cancel()

        assertTrue(closeResult)
        assertEquals(1, realSocket.closeCalls)
        assertEquals(1, realSocket.cancelCalls)
        assertEquals(42L, secureSocket.queueSize())
        assertEquals(realSocket.request(), secureSocket.request())
    }

    @Test
    fun `send text and binary route to handshake manager and report not paired errors`() {
        val sent = mutableListOf<Triple<ActionByte, Byte, ByteArray>>()
        val errors = mutableListOf<SocketXError>()
        val manager = HandshakeManager(
            urlString = TestFixtures.Endpoints.URL_GENERAL,
            reportError = { errors += it },
            sendData = { action, type, payload -> sent += Triple(action, type, payload) },
            codecEngine = FakeCodecEngine()
        )

        val secureSocket = MteSecureWebSocket(RecordingWebSocket(), manager)

        secureSocket.send(TestFixtures.TextPayloads.SIMPLE)
        secureSocket.send(TestFixtures.BinaryPayloads.SMALL.toByteString())

        assertEquals(0, sent.size)
        assertEquals(2, errors.size)
        assertTrue(errors.all { it is SocketXError.NetworkError })
    }

    @Test
    fun `rapid mixed payload sends before pairing produce deterministic network errors`() {
        val sent = mutableListOf<Triple<ActionByte, Byte, ByteArray>>()
        val errors = mutableListOf<SocketXError>()
        val manager = HandshakeManager(
            urlString = TestFixtures.Endpoints.URL_GENERAL,
            reportError = { errors += it },
            sendData = { action, type, payload -> sent += Triple(action, type, payload) },
            codecEngine = FakeCodecEngine()
        )
        val secureSocket = MteSecureWebSocket(RecordingWebSocket(), manager)

        repeat(20) {
            secureSocket.send("msg-$it")
            secureSocket.send(TestFixtures.BinaryPayloads.SMALL.toByteString())
        }

        assertEquals(0, sent.count { it.first == ActionByte.PROXY_DATA })
        assertEquals(40, errors.size)
        assertTrue(errors.all { it is SocketXError.NetworkError })
    }
}
