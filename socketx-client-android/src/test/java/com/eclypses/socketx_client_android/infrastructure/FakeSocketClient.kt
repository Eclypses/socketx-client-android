package com.eclypses.socketx_client_android.infrastructure

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class FakeSocketClient {

    data class FailureToggles(
        var connect: Throwable? = null,
        var disconnect: Throwable? = null,
        var sendText: Throwable? = null,
        var sendBinary: Throwable? = null
    )

    sealed class Event {
        data object Connected : Event()
        data class TextMessage(val payload: String) : Event()
        data class BinaryMessage(val payload: ByteArray) : Event()
        data class Error(val throwable: Throwable) : Event()
        data class Closed(val code: Int, val reason: String) : Event()
    }

    val failureToggles = FailureToggles()

    var connectCalls: Int = 0
        private set
    var disconnectCalls: Int = 0
        private set
    var sendTextCalls: Int = 0
        private set
    var sendBinaryCalls: Int = 0
        private set

    var lastEndpoint: String? = null
        private set
    var lastHeaders: Map<String, String>? = null
        private set
    var lastTextPayload: String? = null
        private set
    var lastBinaryPayload: ByteArray? = null
        private set

    val operationHistory = mutableListOf<String>()
    val sentTextHistory = mutableListOf<String>()
    val sentBinaryHistory = mutableListOf<ByteArray>()

    private val _events = MutableSharedFlow<Event>(replay = 0, extraBufferCapacity = 64)
    val events: SharedFlow<Event> = _events.asSharedFlow()

    var isDisposed: Boolean = false
        private set

    fun connect(endpoint: String, headers: Map<String, String> = emptyMap()) {
        failureToggles.connect?.let { throw it }
        connectCalls += 1
        operationHistory += "connect"
        lastEndpoint = endpoint
        lastHeaders = headers
    }

    fun disconnect() {
        failureToggles.disconnect?.let { throw it }
        disconnectCalls += 1
        operationHistory += "disconnect"
    }

    fun sendText(payload: String) {
        failureToggles.sendText?.let { throw it }
        sendTextCalls += 1
        operationHistory += "sendText"
        lastTextPayload = payload
        sentTextHistory += payload
    }

    fun sendBinary(payload: ByteArray) {
        failureToggles.sendBinary?.let { throw it }
        sendBinaryCalls += 1
        operationHistory += "sendBinary"
        lastBinaryPayload = payload
        sentBinaryHistory += payload.copyOf()
    }

    fun emitConnected() {
        _events.tryEmit(Event.Connected)
    }

    fun emitTextMessage(payload: String) {
        _events.tryEmit(Event.TextMessage(payload))
    }

    fun emitBinaryMessage(payload: ByteArray) {
        _events.tryEmit(Event.BinaryMessage(payload))
    }

    fun emitError(throwable: Throwable) {
        _events.tryEmit(Event.Error(throwable))
    }

    fun emitClosed(code: Int, reason: String) {
        _events.tryEmit(Event.Closed(code, reason))
    }

    fun reset() {
        connectCalls = 0
        disconnectCalls = 0
        sendTextCalls = 0
        sendBinaryCalls = 0
        lastEndpoint = null
        lastHeaders = null
        lastTextPayload = null
        lastBinaryPayload = null
        operationHistory.clear()
        sentTextHistory.clear()
        sentBinaryHistory.clear()
        failureToggles.connect = null
        failureToggles.disconnect = null
        failureToggles.sendText = null
        failureToggles.sendBinary = null
        isDisposed = false
    }

    fun dispose() {
        isDisposed = true
        operationHistory += "dispose"
    }
}
