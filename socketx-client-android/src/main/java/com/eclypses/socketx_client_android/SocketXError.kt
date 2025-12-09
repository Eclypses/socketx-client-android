package com.eclypses.socketx_client_android

sealed class SocketXError(val reason: String) : Exception(reason) {
    class NetworkError(reason: String) : SocketXError(reason)
    class TransportError(reason: String) : SocketXError(reason)
    class CodecError(reason: String) : SocketXError(reason)
    class HandshakeError(reason: String) : SocketXError(reason)
    class ProxyError(reason: String) : SocketXError(reason)
    class InternalError(reason: String) : SocketXError(reason)
    class Unknown(reason: String) : SocketXError(reason)
}
