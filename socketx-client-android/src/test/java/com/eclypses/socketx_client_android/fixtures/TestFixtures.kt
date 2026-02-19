package com.eclypses.socketx_client_android.fixtures

import com.eclypses.socketx_client_android.SocketXError

object TestFixtures {
    object Endpoints {
        const val BASE = "wss://socketx.example.com/ws"
        const val ROOM_GENERAL = "/chat/general"
        const val ROOM_PRIVATE = "/chat/private/user-123"
        const val URL_GENERAL = "$BASE$ROOM_GENERAL"
        const val URL_PRIVATE = "$BASE$ROOM_PRIVATE"
    }

    object Headers {
        val auth = mapOf(
            "Authorization" to "Bearer test-token",
            "X-API-Key" to "test-api-key"
        )
    }

    object TextPayloads {
        const val EMPTY = ""
        const val SIMPLE = "hello"
        const val UNICODE = "こんにちは 🌐 Привет"
        const val MULTILINE = "line1\nline2\nline3"
        val LONG = "x".repeat(4096)
    }

    object BinaryPayloads {
        val EMPTY = byteArrayOf()
        val SMALL = byteArrayOf(0x01, 0x02, 0x03)
        val LARGE = ByteArray(16_384) { (it % 255).toByte() }
    }

    object Errors {
        val network = SocketXError.NetworkError("network failure")
        val transport = SocketXError.TransportError("transport failure")
        val codec = SocketXError.CodecError("codec failure")
        val handshake = SocketXError.HandshakeError("handshake failure")
        val proxy = SocketXError.ProxyError("proxy failure")
        val internal = SocketXError.InternalError("internal failure")
        val unknown = SocketXError.Unknown("unknown failure")
        val malformed = IllegalArgumentException("malformed payload")
    }
}
