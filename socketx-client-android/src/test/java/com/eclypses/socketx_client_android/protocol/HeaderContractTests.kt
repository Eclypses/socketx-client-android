package com.eclypses.socketx_client_android.protocol

import com.eclypses.socketx_client_android.ActionByte
import com.eclypses.socketx_client_android.Header
import com.eclypses.socketx_client_android.fixtures.TestFixtures
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class HeaderContractTests {

    @Test
    fun `wrap and unwrap preserve action, type, and payload`() {
        val wrapped = Header.wrap(
            action = ActionByte.PROXY_DATA,
            messageType = 0,
            payload = TestFixtures.TextPayloads.MULTILINE.toByteArray()
        )

        val unwrapped = Header.unwrap(wrapped)
        assertNotNull(unwrapped)
        assertEquals(ActionByte.PROXY_DATA, unwrapped?.action)
        assertEquals(0, unwrapped?.messageType)
        assertArrayEquals(TestFixtures.TextPayloads.MULTILINE.toByteArray(), unwrapped?.payload)
    }

    @Test
    fun `wrap default message type uses protocol sentinel`() {
        val wrapped = Header.wrap(action = ActionByte.REQUEST, payload = byteArrayOf())
        val unwrapped = Header.unwrap(wrapped)

        assertNotNull(unwrapped)
        assertEquals(-2, unwrapped?.messageType)
    }

    @Test
    fun `unwrap rejects malformed payloads`() {
        assertNull(Header.unwrap(byteArrayOf(1, 2, 3)))

        val wrongHeader = byteArrayOf(0, 0, 0, 0, 1, ActionByte.REQUEST.value, -2)
        assertNull(Header.unwrap(wrongHeader))

        val wrongVersion = byteArrayOf(77, 84, 69, 2, 2, ActionByte.REQUEST.value, -2)
        assertNull(Header.unwrap(wrongVersion))
    }

    @Test
    fun `binary payload contract is preserved`() {
        val wrapped = Header.wrap(
            action = ActionByte.PROXY_DATA,
            messageType = 1,
            payload = TestFixtures.BinaryPayloads.LARGE
        )
        val unwrapped = Header.unwrap(wrapped)

        assertNotNull(unwrapped)
        assertEquals(1, unwrapped?.messageType)
        assertArrayEquals(TestFixtures.BinaryPayloads.LARGE, unwrapped?.payload)
    }
}
