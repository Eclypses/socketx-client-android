package com.eclypses.socketx_client_android

import com.eclypses.mte.MteBase
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString.Companion.toByteString

/**
 * A factory for creating secure WebSockets that use the SocketX protocol.
 *
 * This class is the main entry point for the SocketX client library. It wraps the
 * standard OkHttp `OkHttpClient` and provides a method to create a `WebSocket`
 * that automatically handles the MTE handshake and data encryption/decryption.
 *
 * @property okHttpClient The underlying OkHttp client that will be used for the WebSocket connection.
 *                        This client should be configured with any necessary authenticators, interceptors,
 *                        and especially a DNS resolver if connecting to internal hosts.
 */
class SocketXClient(
    private val okHttpClient: OkHttpClient
) {

    /**
     * An optional callback to handle global errors that are not specific to a single
     * WebSocket connection, such as MTE license initialization failures.
     */
    var onGlobalError: ((SocketXError) -> Unit)? = null

    init {
        if (!MteBase.initLicense(Settings.licCompanyName, Settings.licCompanyKey)) {
            val error = "MTE License Check failed. Please provide a valid license."
            InternalLog.e("SocketXClient", error)
            val socketXError = SocketXError.InternalError(error)
            onGlobalError?.invoke(socketXError)
            throw socketXError
        }
        InternalLog.d("SocketXClient", "Using SocketXClient Version ${Settings.socketXClientVersion} and MTE Version ${MteBase.getVersion()}")
    }

    /**
     * Creates a new secure WebSocket connection.
     *
     * This method follows the OkHttp `newWebSocket` pattern. It takes a standard `Request`
     * and `WebSocketListener` and returns a `WebSocket` instance.
     *
     * The returned `WebSocket` is a secure wrapper. Messages sent through it will be
     * automatically encrypted, and messages received by the listener will be automatically
     * decrypted.
     *
     * @param request The OkHttp `Request` object specifying the server URL and any headers.
     * @param listener The application's `WebSocketListener` that will receive events for
     *                 the secure connection. The `onOpen` method will not be called until
     *                 the MTE handshake is complete.
     * @return A `WebSocket` instance that transparently handles encryption and decryption.
     */
    fun newWebSocket(request: Request, listener: WebSocketListener): WebSocket {
        lateinit var realWebSocket: WebSocket
        lateinit var secureWebSocket: MteSecureWebSocket
        lateinit var savedResponse: Response

        val sendData: (action: ActionByte, messageType: Byte, payload: ByteArray) -> Unit = { action, messageType, payload ->
            val message = Header.wrap(action, messageType, payload)
            realWebSocket.send(message.toByteString())
        }

        val handshakeManager = HandshakeManager(
            urlString = request.url.toString(),
            reportError = { error -> listener.onFailure(secureWebSocket, error, null) },
            sendData = sendData
        )

        handshakeManager.onPaired = {
            listener.onOpen(secureWebSocket, savedResponse)
        }
        handshakeManager.onMessageReceived = { decryptedText ->
            listener.onMessage(secureWebSocket, decryptedText)
        }
        handshakeManager.onBinaryReceived = { decryptedBytes ->
            listener.onMessage(secureWebSocket, decryptedBytes.toByteString())
        }

        val mteInternalListener = object : MteInternalWebSocketListener(listener, handshakeManager) {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                savedResponse = response
                super.onOpen(webSocket, response)
            }
        }

        realWebSocket = okHttpClient.newWebSocket(request, mteInternalListener)
        secureWebSocket = MteSecureWebSocket(realWebSocket, handshakeManager)
        mteInternalListener.secureWebSocket = secureWebSocket

        return secureWebSocket
    }
}
