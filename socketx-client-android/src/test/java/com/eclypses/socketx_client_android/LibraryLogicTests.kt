package com.eclypses.socketx_client_android

import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

/**
 * Local unit tests for the SocketX client library.
 * These tests run on the local JVM and are for testing pure logic without Android dependencies.
 */
class LibraryLogicTests {

    @Test
    fun `captureRoomPath correctly splits URLs`() {
        // Test case 1: URL with a room path
        val result1 = HandshakeManager.captureRoomPath("wss://server.com/ws/dogs")
        assertEquals("wss://server.com", result1.first)
        assertEquals("/ws/dogs", result1.second)

        // Test case 2: URL with only /ws
        val result2 = HandshakeManager.captureRoomPath("wss://server.com/ws")
        assertEquals("wss://server.com", result2.first)
        assertEquals("/ws", result2.second)

        // Test case 3: URL with a trailing slash
        val result3 = HandshakeManager.captureRoomPath("wss://server.com/ws/")
        assertEquals("wss://server.com", result3.first)
        assertEquals("/ws/", result3.second)

        // Test case 4: URL with a query parameter
        val result4 = HandshakeManager.captureRoomPath("wss://server.com/ws/private?token=123")
        assertEquals("wss://server.com", result4.first)
        assertEquals("/ws/private?token=123", result4.second)

        // Test case 5: URL with arbitrary path still splits host and path
        val result5 = HandshakeManager.captureRoomPath("wss://otherserver.com/another/path")
        assertEquals("wss://otherserver.com", result5.first)
        assertEquals("/another/path", result5.second)
    }

    @Test
    fun `Header can wrap and unwrap a message`() {
        // 1. Define a sample message.
        val originalAction = ActionByte.PROXY_DATA
        val originalMessageType: Byte = 0 // Represents text
        val originalPayload = "Hello, World!".toByteArray(Charsets.UTF_8)

        // 2. Wrap the message into a raw byte array.
        val wrappedMessage = Header.wrap(originalAction, originalMessageType, originalPayload)

        // 3. Unwrap the raw byte array.
        val unwrappedResult = Header.unwrap(wrappedMessage)

        // 4. Assert that the unwrapped result is not null and all components match the originals.
        assertNotNull(unwrappedResult, "Unwrap result should not be null")
        unwrappedResult?.let {
            assertEquals(originalAction, it.action, "Action should match")
            assertEquals(originalMessageType, it.messageType, "Message type should match")
            assertArrayEquals(originalPayload, it.payload, "Payload should match")
        }
    }
}
