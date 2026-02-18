package com.eclypses.socketx_client_android.infrastructure

import com.eclypses.socketx_client_android.CodecEngine

class FakeCodecEngine : CodecEngine {
    var completePairingCalls = 0
    var encodeCalls = 0
    var decodeCalls = 0
    var pairingKeysPayload: ByteArray = "pair-keys".toByteArray()
    var encodeTransform: (ByteArray) -> ByteArray = { input -> input }
    var decodeTransform: (ByteArray) -> ByteArray = { input -> input }
    var completePairingError: Throwable? = null

    override fun getPairingKeys(): ByteArray = pairingKeysPayload

    override fun completePairing(payload: ByteArray) {
        completePairingError?.let { throw it }
        completePairingCalls += 1
    }

    override fun encode(data: ByteArray): ByteArray {
        encodeCalls += 1
        return encodeTransform(data)
    }

    override fun decode(data: ByteArray): ByteArray {
        decodeCalls += 1
        return decodeTransform(data)
    }
}
