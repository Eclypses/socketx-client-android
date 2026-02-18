package com.eclypses.socketx_client_android.model

import com.eclypses.socketx_client_android.ActionByte
import com.eclypses.socketx_client_android.HandshakeManager
import com.eclypses.socketx_client_android.SocketXError
import com.eclypses.socketx_client_android.fixtures.TestFixtures
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ErrorAndModelTests {

    @Test
    fun `action byte parsing is stable and unknown-safe`() {
        assertEquals(ActionByte.REQUEST, ActionByte.fromByte(0))
        assertEquals(ActionByte.PROXY_DATA, ActionByte.fromByte(6))
        assertEquals(ActionByte.UNKNOWN, ActionByte.fromByte(127))
    }

    @Test
    fun `capture room path handles base and suffix variants`() {
        val withRoom = HandshakeManager.captureRoomPath(TestFixtures.Endpoints.URL_PRIVATE)
        assertEquals("wss://socketx.example.com", withRoom.first)
        assertEquals("/ws/chat/private/user-123", withRoom.second)

        val withoutRoom = HandshakeManager.captureRoomPath(TestFixtures.Endpoints.BASE)
        assertEquals("wss://socketx.example.com", withoutRoom.first)
        assertEquals("/ws", withoutRoom.second)
    }

    @Test
    fun `socketx errors preserve reason and type`() {
        val errors = listOf(
            TestFixtures.Errors.network,
            TestFixtures.Errors.transport,
            TestFixtures.Errors.codec,
            TestFixtures.Errors.handshake,
            TestFixtures.Errors.proxy,
            TestFixtures.Errors.internal,
            TestFixtures.Errors.unknown
        )

        errors.forEach { error ->
            assertEquals(error.reason, error.message)
            assertTrue(error.toString().contains(error.reason))
        }
    }
}
