package com.eclypses.socketx_client_android.infrastructure

import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okio.ByteString

class RecordingWebSocket(
    private val request: Request = Request.Builder().url("wss://socketx.example.com/ws").build()
) : WebSocket {
    val sentText = mutableListOf<String>()
    val sentBinary = mutableListOf<ByteString>()
    var closeCalls = 0
    var lastCloseCode: Int? = null
    var lastCloseReason: String? = null
    var cancelCalls = 0
    var queueSizeValue: Long = 0L

    override fun request(): Request = request

    override fun queueSize(): Long = queueSizeValue

    override fun send(text: String): Boolean {
        sentText += text
        return true
    }

    override fun send(bytes: ByteString): Boolean {
        sentBinary += bytes
        return true
    }

    override fun close(code: Int, reason: String?): Boolean {
        closeCalls += 1
        lastCloseCode = code
        lastCloseReason = reason
        return true
    }

    override fun cancel() {
        cancelCalls += 1
    }
}

fun testResponse(url: String = "wss://socketx.example.com/ws"): Response {
    val req = Request.Builder().url(url).build()
    return Response.Builder()
        .request(req)
        .protocol(Protocol.HTTP_1_1)
        .code(101)
        .message("Switching Protocols")
        .build()
}
