package com.eclypses.socketx_client_android.facade

import com.eclypses.socketx_client_android.fixtures.TestFixtures
import com.eclypses.socketx_client_android.infrastructure.FakeSocketClient
import com.eclypses.socketx_client_android.infrastructure.TestSocketFacade
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PublicApiFacadeTests {

    @Test
    fun `connect send disconnect delegates to lower layer fake`() {
        val fake = FakeSocketClient()
        val facade = TestSocketFacade(fake)

        facade.connect(TestFixtures.Endpoints.URL_GENERAL, TestFixtures.Headers.auth)
        facade.sendText(TestFixtures.TextPayloads.SIMPLE)
        facade.sendBinary(TestFixtures.BinaryPayloads.SMALL)
        facade.disconnect()

        assertFalse(facade.isConnected)
        assertEquals(1, fake.connectCalls)
        assertEquals(1, fake.sendTextCalls)
        assertEquals(1, fake.sendBinaryCalls)
        assertEquals(1, fake.disconnectCalls)
        assertEquals(TestFixtures.Endpoints.URL_GENERAL, fake.lastEndpoint)
        assertEquals(TestFixtures.Headers.auth, fake.lastHeaders)
    }

    @Test
    fun `reconnect workflow is deterministic`() {
        val fake = FakeSocketClient()
        val facade = TestSocketFacade(fake)

        facade.connect(TestFixtures.Endpoints.URL_GENERAL)
        facade.disconnect()
        facade.connect(TestFixtures.Endpoints.URL_PRIVATE)

        assertTrue(facade.isConnected)
        assertEquals(2, fake.connectCalls)
        assertEquals(1, fake.disconnectCalls)
        assertEquals(TestFixtures.Endpoints.URL_PRIVATE, fake.lastEndpoint)
    }

    @Test
    fun `rapid sends and mixed payloads preserve ordering and payload fidelity`() {
        val fake = FakeSocketClient()
        val facade = TestSocketFacade(fake)
        facade.connect(TestFixtures.Endpoints.URL_GENERAL)

        repeat(100) { index ->
            facade.sendText("txt-$index")
            facade.sendBinary(byteArrayOf(index.toByte()))
        }

        assertEquals(100, fake.sendTextCalls)
        assertEquals(100, fake.sendBinaryCalls)
        assertEquals("txt-0", fake.sentTextHistory.first())
        assertEquals("txt-99", fake.sentTextHistory.last())
        assertArrayEquals(byteArrayOf(0), fake.sentBinaryHistory.first())
        assertArrayEquals(byteArrayOf(99.toByte()), fake.sentBinaryHistory.last())
    }

    @Test
    fun `empty and large payloads are accepted`() {
        val fake = FakeSocketClient()
        val facade = TestSocketFacade(fake)
        facade.connect(TestFixtures.Endpoints.URL_GENERAL)

        facade.sendText(TestFixtures.TextPayloads.EMPTY)
        facade.sendText(TestFixtures.TextPayloads.LONG)
        facade.sendBinary(TestFixtures.BinaryPayloads.EMPTY)
        facade.sendBinary(TestFixtures.BinaryPayloads.LARGE)

        assertEquals(TestFixtures.TextPayloads.LONG, fake.lastTextPayload)
        assertArrayEquals(TestFixtures.BinaryPayloads.LARGE, fake.lastBinaryPayload)
    }

    @Test
    fun `error propagation from lower layer is surfaced`() {
        val fake = FakeSocketClient().apply {
            failureToggles.connect = TestFixtures.Errors.network
            failureToggles.sendText = TestFixtures.Errors.transport
            failureToggles.sendBinary = TestFixtures.Errors.codec
            failureToggles.disconnect = TestFixtures.Errors.internal
        }
        val facade = TestSocketFacade(fake)

        assertThrows(TestFixtures.Errors.network::class.java) {
            facade.connect(TestFixtures.Endpoints.URL_GENERAL)
        }

        fake.failureToggles.connect = null
        facade.connect(TestFixtures.Endpoints.URL_GENERAL)

        assertThrows(TestFixtures.Errors.transport::class.java) {
            facade.sendText(TestFixtures.TextPayloads.SIMPLE)
        }
        assertThrows(TestFixtures.Errors.codec::class.java) {
            facade.sendBinary(TestFixtures.BinaryPayloads.SMALL)
        }
        assertThrows(TestFixtures.Errors.internal::class.java) {
            facade.disconnect()
        }
    }
}
