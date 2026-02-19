package com.eclypses.socketx_client_android

import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString

internal open class MteInternalWebSocketListener(
    private val userListener: WebSocketListener,
    private val handshakeManager: HandshakeManager
) : WebSocketListener() {

    lateinit var secureWebSocket: MteSecureWebSocket

    override fun onOpen(webSocket: WebSocket, response: Response) {
        InternalLog.d("MteListener", "onOpen received, starting handshake.")
        handshakeManager.startHandshake()
    }

    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
        InternalLog.d("MteListener", "onMessage (binary) received, passing to HandshakeManager.")
        handshakeManager.processIncomingData(bytes.toByteArray())
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        InternalLog.w("MteListener", "onMessage (text) received unexpectedly. Passing to HandshakeManager.")
        handshakeManager.processUnexpectedTextMessage()
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        userListener.onClosing(secureWebSocket, code, reason)
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        userListener.onClosed(secureWebSocket, code, reason)
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        userListener.onFailure(secureWebSocket, t, response)
    }
}
