package com.eclypses.socketx_client_android


import okhttp3.*
import okio.ByteString.Companion.toByteString
import java.net.InetAddress
import java.net.Proxy

internal class Transport {

    private val client: OkHttpClient

    init {
        val customDns = object : Dns {
            override fun lookup(hostname: String): List<InetAddress> {
                if (hostname.equals("dev-socketx-server.eclypses.com", ignoreCase = true)) {
                    return listOf(InetAddress.getByAddress(hostname, InetAddress.getByName("10.102.160.80").address))
                }
                return Dns.SYSTEM.lookup(hostname)
            }
        }

        client = OkHttpClient.Builder()
            .proxy(Proxy.NO_PROXY)
            .dns(customDns)
            .build()
    }

    private var webSocket: WebSocket? = null

    fun disconnect() {
        webSocket?.close(1000, "Client disconnect")
        webSocket = null
    }

    fun send(action: ActionByte, messageType: Byte = -2, payload: ByteArray = byteArrayOf()) {
        val message = Header.wrap(action, messageType, payload)
        webSocket?.send(message.toByteString())
    }
}
