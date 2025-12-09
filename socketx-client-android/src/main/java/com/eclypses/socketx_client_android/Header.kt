package com.eclypses.socketx_client_android

import android.util.Log

/**
 * Action byte values used internally for SocketXClient messages.
 * (6th byte in the 7-byte header)
 */
enum class ActionByte(val value: Byte) {
    REQUEST(0),
    RESPONSE(1),
    PAIR_REQUEST(2),
    PAIR_RESPONSE(3),
    UPSTREAM_CONNECTION_REQUEST(4),
    UPSTREAM_CONNECTION_RESPONSE(5),
    PROXY_DATA(6),
    UNKNOWN(255.toByte()); // In Kotlin, Byte is signed, so 255 becomes -1

    companion object {
        fun fromByte(value: Byte): ActionByte = ActionByte.entries.find { it.value == value } ?: UNKNOWN
    }
}

/**
 * Internal object for handling the fixed 7-byte SocketXClient header.
 */
object Header {
    private val PROTOCOL_HEADER: ByteArray = byteArrayOf(77, 84, 69, 2) // "MTE" + 2
    private const val VERSION: Byte = 1

    // Data class to hold the result of unwrapping a raw message
    data class UnwrappedHeader(val action: ActionByte, val messageType: Byte, val payload: ByteArray)

    /**
     * Builds a full message with a 7-byte header + payload.
     */
    fun wrap(
        action: ActionByte,
        messageType: Byte = -2, // -2 corresponds to 254 (UInt8)
        payload: ByteArray = byteArrayOf()
    ): ByteArray {
        return PROTOCOL_HEADER + byteArrayOf(VERSION, action.value, messageType) + payload
    }

    /**
     * Unwraps a raw message into its constituent parts.
     * Returns null if the packet is malformed.
     */
    fun unwrap(data: ByteArray): UnwrappedHeader? {
        val headerSize = PROTOCOL_HEADER.size + 3
        if (data.size < headerSize) {
            Log.e("Header", "Malformed packet: too small. Size: ${data.size}")
            return null
        }

        val receivedProto = data.sliceArray(0 until PROTOCOL_HEADER.size)
        if (!receivedProto.contentEquals(PROTOCOL_HEADER)) {
            Log.e("Header", "Malformed packet: protocol header mismatch.")
            return null
        }

        val receivedVersion = data[PROTOCOL_HEADER.size]
        if (receivedVersion != VERSION) {
            Log.e("Header", "Version mismatch: got $receivedVersion, expected $VERSION")
            // The Manager is responsible for reporting this as a specific SocketXError
            return null
        }

        val actionByte = data[PROTOCOL_HEADER.size + 1]
        val messageType = data[PROTOCOL_HEADER.size + 2]
        val payload = data.sliceArray(headerSize until data.size)

        return UnwrappedHeader(ActionByte.fromByte(actionByte), messageType, payload)
    }
}
