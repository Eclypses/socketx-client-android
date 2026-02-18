package com.eclypses.socketx_client_android.infrastructure

class TestSocketFacade(
    private val client: FakeSocketClient
) {
    var isConnected: Boolean = false
        private set

    fun connect(endpoint: String, headers: Map<String, String> = emptyMap()) {
        client.connect(endpoint, headers)
        isConnected = true
    }

    fun disconnect() {
        client.disconnect()
        isConnected = false
    }

    fun sendText(payload: String) {
        client.sendText(payload)
    }

    fun sendBinary(payload: ByteArray) {
        client.sendBinary(payload)
    }
}
