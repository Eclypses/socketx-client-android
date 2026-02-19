# SocketX Android Testing Summary

## Layered Test Architecture

Implemented baseline layered tests for the Android client with deterministic unit coverage:

1. **Test Infrastructure**
   - `FakeSocketClient` with per-operation failure toggles (`connect`, `disconnect`, `sendText`, `sendBinary`)
   - Call counters, last-argument capture, ordered send history
   - Event simulation helpers (`connected`, `message`, `binary`, `error`, `closed`)
   - Lifecycle helpers (`reset()`, `dispose()`)
   - Dedicated infrastructure tests (`FakeSocketClientTest`)

2. **Centralized Fixtures**
   - Endpoints/rooms, auth headers, text payload variants (empty/unicode/multiline/long), binary payload variants (empty/small/large), and error fixtures
   - All new tests reference fixture constants (no scattered inline literals)

3. **Error/Model Tests**
   - `ActionByte` parsing, unknown fallback behavior
   - `SocketXError` reason/type behavior
   - `captureRoomPath` contract behavior

4. **Protocol/Contract Tests**
   - Header wrap/unwrap contract, malformed packet handling, text/binary message type behavior

5. **Bridge/Callback Boundary Tests**
   - Internal listener handshake start behavior on `onOpen`
   - Forwarding of `onClosing`/`onClosed`/`onFailure` to consumer listener
   - Inbound proxy payload decode and callback dispatch validation

6. **Public API/Facade Tests**
   - `MteSecureWebSocket` delegation and error propagation tests
   - Test facade over fake client validating connect/send/disconnect, reconnect, burst traffic, mixed payloads, and edge payload sizes

## Reusable Test Patterns

- Hand-written fakes and doubles over mocking frameworks
- Deterministic flow/event assertions using `kotlinx-coroutines-test` (`runTest`)
- Layer-focused tests validating externally observable behavior/contracts only
- Stable fixtures to reduce brittle inline constants

## Test Stack

- JUnit5 (`org.junit.jupiter:junit-jupiter`)
- `kotlinx-coroutines-test`
- Gradle configured with `useJUnitPlatform()` for unit tests

## Run Commands

Targeted layered suites first:

```bash
./gradlew :socketx-client-android:testDebugUnitTest \
  --tests "com.eclypses.socketx_client_android.infrastructure.FakeSocketClientTest" \
  --tests "com.eclypses.socketx_client_android.protocol.HeaderContractTests" \
  --tests "com.eclypses.socketx_client_android.model.ErrorAndModelTests" \
  --tests "com.eclypses.socketx_client_android.bridge.HandshakeAndListenerBridgeTests" \
  --tests "com.eclypses.socketx_client_android.facade.MteSecureWebSocketFacadeTests" \
  --tests "com.eclypses.socketx_client_android.facade.PublicApiFacadeTests"
```

Then full unit suite:

```bash
./gradlew :socketx-client-android:testDebugUnitTest
```

Generate coverage report (XML + HTML):

```bash
./gradlew :socketx-client-android:jacocoTestReport
```

Enforce minimum line coverage threshold:

```bash
./gradlew :socketx-client-android:jacocoTestCoverageVerification
```

Override threshold (example 45%):

```bash
./gradlew :socketx-client-android:jacocoTestCoverageVerification -PminLineCoverage=0.45
```

## Results

- **Targeted layered run:** PASS
- **Full unit test run:** PASS
- **Current passing tests:** 24
- **Coverage tooling:** JaCoCo configured (`:socketx-client-android:jacocoTestReport`)
- **Minimum coverage threshold:** configurable via `-PminLineCoverage` (default: `0.40`)
- **Coverage outputs:**
   - XML: `socketx-client-android/build/reports/jacoco/jacocoTestReport/jacocoTestReport.xml`
   - HTML: `socketx-client-android/build/reports/jacoco/jacocoTestReport/html/index.html`

## CI Guidance

Azure pipeline updated to run lint + unit tests:

```bash
./gradlew :socketx-client-android:lint :socketx-client-android:testDebugUnitTest
```

Coverage behavior by branch:

- `develop`: runs `jacocoTestReport` + `jacocoTestCoverageVerification`, then publishes JaCoCo results.
- `master`: runs lint + tests only (no coverage run/publish), matching iOS behavior.

## Workflow Guardrails

- No branch history rewrites are required for this testing setup
- Avoid force-push/rebase guidance in release/test workflows unless explicitly requested
