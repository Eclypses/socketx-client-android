package com.eclypses.socketx_client_android

import okhttp3.Request
import okhttp3.WebSocket
import okio.ByteString

// This class is a decorator that wraps the real WebSocket.
// It implements the WebSocket interface so it can be used just like a normal WebSocket.
internal class MteSecureWebSocket(
    // The real underlying WebSocket from OkHttp.
    private val realWebSocket: WebSocket,
    // The shared state machine for this connection.
    private val handshakeManager: HandshakeManager
) : WebSocket {

    /**
     * Intercepts an attempt to send a text message.
     * The data is passed to the HandshakeManager for encryption before being sent as binary.
     */
    override fun send(text: String): Boolean {
        handshakeManager.encryptAndSendData(text.toByteArray(Charsets.UTF_8), isBinary = false)
        // We return true, assuming the message will be sent. Any failures will be reported
        // asynchronously through the listener's onFailure callback.
        return true
    }

    /**
     * Intercepts an attempt to send a binary message.
     * The data is passed to the HandshakeManager for encryption before being sent.
     */
    override fun send(bytes: ByteString): Boolean {
        handshakeManager.encryptAndSendData(bytes.toByteArray(), isBinary = true)
        return true
    }

    // --- The following methods are simple pass-through calls to the real WebSocket. ---

    override fun close(code: Int, reason: String?): Boolean {
        return realWebSocket.close(code, reason)
    }

    override fun cancel() {
        realWebSocket.cancel()
    }

    override fun queueSize(): Long {
        return realWebSocket.queueSize()
    }

    override fun request(): Request {
        return realWebSocket.request()
    }
}
