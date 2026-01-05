# SocketX Client - Android Library Context

## 1. Project Overview
This project (`socketx-client-android`) is an Android library that provides a secure WebSocket client implementation using **MTE (MicroToken Exchange)** technology. It enables Android applications to establish persistent, end-to-end encrypted WebSocket connections with a SocketX Server.

**Primary Goal:** To provide a secure, drop-in replacement for standard WebSockets that automatically handles MTE handshaking, encryption, and decryption, ensuring that data is protected while in transit.

## 2. Intended Use
This library is designed to be used by Android developers who are already familiar with **OkHttp**. It wraps the standard `OkHttpClient` and `WebSocket` interfaces.

*   **Supported Stack:** OkHttp (v4.x).
*   **Core Features:**
    *   Transparent MTE Handshake (Kyber pairing).
    *   Automatic encryption of outgoing messages (Text/Binary).
    *   Automatic decryption of incoming messages.
    *   Standard `WebSocketListener` interface for integration.

## 3. Core Architecture
The library uses a **Factory** and **Decorator** pattern to inject security into the standard WebSocket lifecycle.

### A. `SocketXClient` (Factory)
*   **Role**: The main entry point.
*   **Responsibility**:
    *   Initializes the MTE license.
    *   Wraps an existing `OkHttpClient`.
    *   Creates instances of `MteSecureWebSocket`.

### B. `MteSecureWebSocket` (Decorator)
*   **Role**: A wrapper around the native OkHttp `WebSocket`.
*   **Responsibility**:
    *   Implements the `WebSocket` interface.
    *   Intercepts `send()` calls to encrypt data before transmission.
    *   Delegates connection management (`close`, `cancel`) to the underlying socket.

### C. `HandshakeManager` (State Machine)
*   **Role**: The central brain for a single connection.
*   **Responsibility**:
    *   **State Management**: Tracks the connection state (Unpaired -> Handshaking -> Paired).
    *   **Protocol Handling**: Interprets the custom 7-byte SocketX header.
    *   **Crypto Operations**: Delegates encoding/decoding to `MteCodec`.
    *   **Routing**: Dispatches decrypted messages to the user's listener or internal handlers.

### D. `MteInternalWebSocketListener`
*   **Role**: The internal listener attached to the real OkHttp socket.
*   **Responsibility**:
    *   Intercepts raw binary messages from the wire.
    *   Passes raw data to `HandshakeManager` for processing.
    *   Only calls the user's `onOpen` when the *secure handshake* is complete, not just the TCP connection.

## 4. Data Flow

### Connection Sequence
1.  **TCP/WS Connect**: Standard WebSocket connection established.
2.  **Handshake**: `HandshakeManager` initiates the MTE exchange (Hello -> Kyber Pair -> Encrypted Path).
3.  **Secure Open**: Once paired, `listener.onOpen()` is called for the consumer.

### Message Transmission (Send)
1.  **App Call**: `webSocket.send("Hello")`
2.  **Encryption**: `MteSecureWebSocket` passes data to `HandshakeManager`.
3.  **MTE Encode**: Payload is encrypted via MTE.
4.  **Framing**: A 7-byte header (ActionByte + Type) is prepended.
5.  **Wire Send**: The binary packet is sent via the underlying OkHttp socket.

### Message Reception (Receive)
1.  **Wire Receive**: `MteInternalWebSocketListener` receives a binary blob.
2.  **Deframing**: `Header` unwraps the 7-byte header to identify the Action.
3.  **Processing**:
    *   If **Handshake Action**: Processed internally to update state.
    *   If **Proxy Data**: Payload is MTE decoded.
4.  **Callback**: The decrypted text/binary is passed to `userListener.onMessage()`.

## 5. Key File Structure
*   **`SocketXClient.kt`**: Public factory for creating connections.
*   **`MteSecureWebSocket.kt`**: The secure socket implementation (Decorator).
*   **`HandshakeManager.kt`**: Internal logic for handshake state and message routing.
*   **`Header.kt`**: Definition of the custom binary protocol header.
*   **`SocketXError.kt`**: Sealed class defining specific error types (Handshake, Crypto, Network).
*   **`Settings.kt`**: Library configuration (Versions, License Keys).

## 6. Configuration
Configuration is minimal and primarily handled during initialization:
*   **`Settings.kt`**: Hardcoded license keys (in this version) and version strings.
*   **`SocketXClient`**: Accepts an `OkHttpClient` to allow the app to configure timeouts, interceptors, and DNS.
