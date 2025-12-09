package com.eclypses.socketx_client_android

import android.util.Log
import org.json.JSONObject

internal class HandshakeManager(
    private val urlString: String,
    private val reportError: (SocketXError) -> Unit,
    private val sendData: (action: ActionByte, messageType: Byte, payload: ByteArray) -> Unit
) {

    // The captureRoomPath function is now in a companion object, making it a static-like method.
    companion object {
        fun captureRoomPath(urlString: String): Pair<String, String> {
            val basePath = "/ws"
            val wsRange = urlString.indexOf(basePath)
            if (wsRange == -1) {
                return Pair(urlString, "")
            }
            val afterWsIndex = wsRange + basePath.length
            val roomPath = if (afterWsIndex < urlString.length) urlString.substring(afterWsIndex) else ""
            return Pair(urlString.substring(0, afterWsIndex), roomPath)
        }
    }

    private enum class PairingState {
        UNPAIRED,
        SENT_HELLO,
        AWAITING_PAIR_RESPONSE,
        SENT_ENCRYPTED_PATH,
        PAIRED
    }

    private enum class MessageType(val value: Byte) {
        TEXT(0),
        BINARY(1);
    }

    var onPaired: (() -> Unit)? = null
    var onMessageReceived: ((String) -> Unit)? = null
    var onBinaryReceived: ((ByteArray) -> Unit)? = null

    private val codec: MteCodec
    private val roomPath: String
    private var pairingState = PairingState.UNPAIRED

    init {
        this.codec = MteCodec()
        val (_, path) = captureRoomPath(urlString)
        this.roomPath = path
    }

    fun startHandshake() {
        Log.d("HandshakeManager", "Starting handshake -> Sending client hello (Action: REQUEST)")
        sendData(ActionByte.REQUEST, -2, byteArrayOf())
        pairingState = PairingState.SENT_HELLO
    }

    fun processIncomingData(data: ByteArray) {
        val unwrapped = Header.unwrap(data)
        if (unwrapped == null) {
            reportError(SocketXError.TransportError("Received malformed binary message."))
            return
        }
        dispatchMessage(unwrapped.action, unwrapped.messageType, unwrapped.payload)
    }

    fun processUnexpectedTextMessage() {
        reportError(SocketXError.TransportError("Received unexpected text message."))
    }

    fun encryptAndSendData(data: ByteArray, isBinary: Boolean) {
        if (pairingState != PairingState.PAIRED) {
            reportError(SocketXError.NetworkError("Cannot send data, client is not paired."))
            return
        }
        val encoded = codec.encode(data)
        val type = if (isBinary) MessageType.BINARY.value else MessageType.TEXT.value
        sendData(ActionByte.PROXY_DATA, type, encoded)
    }

    private fun dispatchMessage(action: ActionByte, messageType: Byte, payload: ByteArray) {
        when (action) {
            ActionByte.PROXY_DATA -> handleProxyData(payload, messageType)
            else -> handleHandshake(action, payload)
        }
    }

    private fun handleProxyData(payload: ByteArray, messageType: Byte) {
        val decoded = codec.decode(payload)
        when (messageType) {
            MessageType.TEXT.value -> onMessageReceived?.invoke(decoded.toString(Charsets.UTF_8))
            MessageType.BINARY.value -> onBinaryReceived?.invoke(decoded)
            else -> reportError(SocketXError.NetworkError("Unknown message type: $messageType"))
        }
    }

    private fun handleHandshake(action: ActionByte, payload: ByteArray) {
        Log.d("HandshakeManager", "Handling handshake message. Current state: $pairingState, Received action: $action")
        when (Pair(pairingState, action)) {
            Pair(PairingState.SENT_HELLO, ActionByte.RESPONSE) -> initiateKyberPairing()
            Pair(PairingState.AWAITING_PAIR_RESPONSE, ActionByte.PAIR_RESPONSE) -> handlePairResponsePayload(payload)
            Pair(PairingState.SENT_ENCRYPTED_PATH, ActionByte.UPSTREAM_CONNECTION_RESPONSE) -> {
                pairingState = PairingState.PAIRED
                Log.d("HandshakeManager", "Handshake complete. Client is paired.")
                onPaired?.invoke()
            }
            else -> reportError(SocketXError.HandshakeError("Unexpected handshake message: state=$pairingState, action=$action"))
        }
    }

    private fun initiateKyberPairing() {
        Log.d("HandshakeManager", "Initiating new Kyber pairing -> Sending pair request (Action: PAIR_REQUEST)")
        val payload = codec.getPairingKeys()
        sendData(ActionByte.PAIR_REQUEST, -2, payload)
        pairingState = PairingState.AWAITING_PAIR_RESPONSE
    }

    private fun handlePairResponsePayload(payload: ByteArray) {
        Log.d("HandshakeManager", "Handling pair response payload.")
        try {
            codec.completePairing(payload)
            sendEncryptedRoomPath()
        } catch (e: Exception) {
            reportError(SocketXError.HandshakeError("Failed to process pairResponse: ${e.message}"))
        }
    }

    private fun sendEncryptedRoomPath() {
        Log.d("HandshakeManager", "Sending encrypted room path -> (Action: UPSTREAM_CONNECTION_REQUEST)")
        try {
            val json = JSONObject().put("pathname", roomPath)
            val pathnameData = json.toString().toByteArray(Charsets.UTF_8)
            val payload = codec.encode(pathnameData)
            sendData(ActionByte.UPSTREAM_CONNECTION_REQUEST, -2, payload)
            pairingState = PairingState.SENT_ENCRYPTED_PATH
        } catch (e: Exception) {
            reportError(SocketXError.InternalError("Failed to serialize or encrypt room path: ${e.message}"))
        }
    }
}
