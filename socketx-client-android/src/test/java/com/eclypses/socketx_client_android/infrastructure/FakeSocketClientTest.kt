package com.eclypses.socketx_client_android.infrastructure

import com.eclypses.socketx_client_android.fixtures.TestFixtures
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.runCurrent
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class FakeSocketClientTest {

    @Test
    fun `tracks call counters, arguments, and send history`() {
        val fake = FakeSocketClient()

        fake.connect(TestFixtures.Endpoints.URL_GENERAL, TestFixtures.Headers.auth)
        fake.sendText(TestFixtures.TextPayloads.SIMPLE)
        fake.sendBinary(TestFixtures.BinaryPayloads.SMALL)
        fake.disconnect()

        assertEquals(1, fake.connectCalls)
        assertEquals(1, fake.sendTextCalls)
        assertEquals(1, fake.sendBinaryCalls)
        assertEquals(1, fake.disconnectCalls)
        assertEquals(TestFixtures.Endpoints.URL_GENERAL, fake.lastEndpoint)
        assertEquals(TestFixtures.Headers.auth, fake.lastHeaders)
        assertEquals(TestFixtures.TextPayloads.SIMPLE, fake.lastTextPayload)
        assertNotNull(fake.lastBinaryPayload)
        assertArrayEquals(TestFixtures.BinaryPayloads.SMALL, fake.lastBinaryPayload)
        assertEquals(listOf("connect", "sendText", "sendBinary", "disconnect"), fake.operationHistory)
        assertEquals(listOf(TestFixtures.TextPayloads.SIMPLE), fake.sentTextHistory)
        assertEquals(1, fake.sentBinaryHistory.size)
        assertArrayEquals(TestFixtures.BinaryPayloads.SMALL, fake.sentBinaryHistory.first())
    }

    @Test
    fun `supports per operation failure toggles`() {
        val fake = FakeSocketClient()
        fake.failureToggles.connect = TestFixtures.Errors.network
        fake.failureToggles.sendText = TestFixtures.Errors.transport
        fake.failureToggles.sendBinary = TestFixtures.Errors.codec
        fake.failureToggles.disconnect = TestFixtures.Errors.internal

        assertThrows(TestFixtures.Errors.network::class.java) {
            fake.connect(TestFixtures.Endpoints.URL_GENERAL)
        }
        assertThrows(TestFixtures.Errors.transport::class.java) {
            fake.sendText(TestFixtures.TextPayloads.SIMPLE)
        }
        assertThrows(TestFixtures.Errors.codec::class.java) {
            fake.sendBinary(TestFixtures.BinaryPayloads.SMALL)
        }
        assertThrows(TestFixtures.Errors.internal::class.java) {
            fake.disconnect()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `emits events to multiple subscribers deterministically`() = runTest {
        val fake = FakeSocketClient()
        val collectorA = mutableListOf<FakeSocketClient.Event>()
        val collectorB = mutableListOf<FakeSocketClient.Event>()

        val jobA = backgroundScope.launch {
            fake.events.take(3).collect { collectorA += it }
        }
        val jobB = backgroundScope.launch {
            fake.events.take(3).collect { collectorB += it }
        }

        runCurrent()

        fake.emitConnected()
        fake.emitTextMessage(TestFixtures.TextPayloads.UNICODE)
        fake.emitClosed(1000, "done")

        jobA.join()
        jobB.join()

        assertEquals(3, collectorA.size)
        assertEquals(3, collectorB.size)
        assertTrue(collectorA[0] is FakeSocketClient.Event.Connected)
        assertTrue(collectorA[1] is FakeSocketClient.Event.TextMessage)
        assertTrue(collectorA[2] is FakeSocketClient.Event.Closed)
    }

    @Test
    fun `reset and dispose lifecycle helpers clear and mark state`() {
        val fake = FakeSocketClient()
        fake.connect(TestFixtures.Endpoints.URL_GENERAL)
        fake.sendText(TestFixtures.TextPayloads.SIMPLE)
        fake.dispose()

        assertTrue(fake.isDisposed)
        assertTrue(fake.operationHistory.contains("dispose"))

        fake.reset()

        assertFalse(fake.isDisposed)
        assertEquals(0, fake.connectCalls)
        assertEquals(0, fake.sendTextCalls)
        assertEquals(0, fake.operationHistory.size)
        assertEquals(0, fake.sentTextHistory.size)
    }
}
