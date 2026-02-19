<center>
<img src="Eclypses.png" style="width:50%;"/>
</center>

<div align="center" style="font-size:40pt; font-weight:900; font-family:arial; margin-top:50px;" >
SocketX Mobile Client  
Android Library</div>

![Latest Release](https://img.shields.io/github/v/release/Eclypses/socketx-client-android?style=flat-square)

## Introduction
This Android library provides the Eclypses SocketX Mobile Client for Android. It enables secure, persistent WebSocket communication between your Android app and your backend services via a SocketX server. You must have licensed access to a SocketX server instance. [More Info](https://eclypses.com/mte-technology/amazon-web-services-aws/)

**Purpose of SocketX:**
- Establish persistent, encrypted WebSocket tunnels to your server
- Protect real-time data with MTE encryption
- Support bidirectional messaging with automatic encryption/decryption
- Enable secure pub/sub and room-based communication patterns

## Quick Links

📚 **[Official Getting Started Guide](https://public-docs.eclypses.com/docs/socketx/client-libraries/android)** - Concise guide for experienced developers

💡 This README provides comprehensive reference documentation with detailed examples suitable for developers at all experience levels. If you're already familiar with OkHttp WebSockets and MTE concepts, the official docs above offer a faster quick-start path.

## Prerequisites

- **Android 8.0 (API 26) or later** - Required for modern WebSocket support and cryptographic capabilities needed for MTE encryption
- **Kotlin 1.9 or later / Java 8 or later** - The library is written in Kotlin but fully supports Java 8+
- **OkHttp 4.12.0 or later** - This library wraps the OkHttp WebSocket implementation
- **Gradle 7.0 or later** - For dependency management and building
- **Access to a licensed SocketX server instance** - This client library communicates with a SocketX server that handles the MTE encryption/decryption relay. You'll need the server URL and proper licensing credentials

## How SocketX Works

Before diving into the code, it's helpful to understand how SocketX creates a secure communication tunnel:

**Traditional WebSocket (Unencrypted):**
```
[Your Android App] ←→ WebSocket ←→ [Your Backend Server]
    ⚠️ Data transmitted in plaintext (even with WSS/TLS, data is visible at endpoints)
```

**With SocketX (MTE-Encrypted):**
```
[Your Android App] ←→ Encrypted WebSocket Tunnel ←→ [SocketX Server] ←→ [Your Backend Service]
     ↑                                                    ↑
  Encrypts here                                   Decrypts & forwards here
```

**Data Flow:**

1. **Pairing:** When your app connects, it performs an automatic MTE pairing handshake with the SocketX server using post-quantum Kyber-512 key exchange
2. **Encode:** Your app sends data (text or binary) through the SocketX client, which encrypts it using MTE
3. **Tunnel:** The encrypted payload travels over the WebSocket connection to the SocketX server
4. **Decode:** The server decrypts the payload using its paired MTE decoder
5. **Forward:** The server forwards the original data to your designated backend service or room
6. **Response:** Any response follows the same path in reverse - encrypted by the server, sent through the tunnel, decrypted by your app

**Key Benefits:**
- End-to-end encryption that's quantum-resistant (Kyber-512)
- Zero-trust architecture - data is encrypted before leaving your app
- Persistent connections for real-time, low-latency messaging
- Automatic reconnection and pairing management

## Installation

### Gradle (Recommended)

Add the dependency to your app-level `build.gradle` or `build.gradle.kts` file:

**Kotlin DSL (`build.gradle.kts`):**
```kotlin
dependencies {
    implementation("com.eclypses:socketx-client-android:1.2.0")
    // OkHttp is also required
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
}
```

**Groovy DSL (`build.gradle`):**
```groovy
dependencies {
    implementation 'com.eclypses:socketx-client-android:1.2.0'
    implementation 'com.squareup.okhttp3:okhttp:4.12.0'
}
```

### Maven

If your project uses Maven, add this to your `pom.xml`:

```xml
<dependency>
    <groupId>com.eclypses</groupId>
    <artifactId>socketx-client-android</artifactId>
    <version>1.0.8</version>
</dependency>
<dependency>
    <groupId>com.squareup.okhttp3</groupId>
    <artifactId>okhttp</artifactId>
    <version>4.12.0</version>
</dependency>
```

**Common Installation Issues:**
- **"Failed to resolve"** - Make sure Maven Central is included in your repositories
- **Build errors after adding dependency** - Try **Build > Clean Project** and **Build > Rebuild Project**
- **Version conflicts** - Check that OkHttp versions are compatible (4.12.0+ recommended)

## Setup

### Step 1: Configure Your SocketX Server

**Important:** 
Before using this client library, ensure your SocketX server is set up and configured to receive encrypted WebSocket connections from your Android app. The server acts as a secure relay, handling MTE encryption/decryption and forwarding messages to your backend services or managing room-based communication.

### Step 2: Understanding the Factory Pattern

**How does SocketXClient work?**
The SocketX client uses a factory pattern. You create and configure an `OkHttpClient` with your settings, then pass it to `SocketXClient`. When you call `newWebSocket()`, the factory creates a secure WebSocket wrapper that automatically handles MTE encryption/decryption while using your configured HTTP client for the underlying connection.

**Why configure the OkHttpClient?**
- ✅ Add custom HTTP headers (authentication, API keys, etc.)
- ✅ Implement certificate pinning for enhanced security
- ✅ Configure timeouts, interceptors, and connection pools
- ✅ Use your own connection specs and DNS configurations
- ✅ Test with mock OkHttp clients

### Step 3: Add Internet Permission

In your `AndroidManifest.xml`, ensure you have the internet permission:

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

### Step 4: Import the Required Classes

In your Kotlin or Java file, import the necessary classes:

**Kotlin:**
```kotlin
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okhttp3.Response
import okio.ByteString
import okio.ByteString.Companion.toByteString
import com.eclypses.socketx_client_android.SocketXClient
import com.eclypses.socketx_client_android.SocketXError
```

**Java:**
```java
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okhttp3.Response;
import okio.ByteString;
import com.eclypses.socketx_client_android.SocketXClient;
import com.eclypses.socketx_client_android.SocketXError;
```

### Step 5: Create a Complete Chat Manager Example

Here's a comprehensive example of setting up SocketX in a typical Android app:

**Kotlin Example:**

```kotlin
import android.util.Log
import okhttp3.*
import okio.ByteString
import okio.ByteString.Companion.toByteString
import com.eclypses.socketx_client_android.SocketXClient
import com.eclypses.socketx_client_android.SocketXError
import java.util.concurrent.TimeUnit
import org.json.JSONObject

/**
 * This class manages your secure WebSocket communication through SocketX.
 * It handles connection lifecycle, message encryption/decryption, and error handling.
 */
class ChatManager(
    private val socketXServerUrl: String,
    private val roomPath: String
) {
    // The underlying OkHttp client - configured with your custom settings
    private var okHttpClient: OkHttpClient? = null
    
    // The SocketX client factory - handles MTE initialization
    private var socketXClient: SocketXClient? = null
    
    // The secure WebSocket instance
    private var webSocket: WebSocket? = null
    
    // Connection state
    var isConnected: Boolean = false
        private set
    
    // Callbacks that can be set by the UI layer
    var onConnectedCallback: (() -> Unit)? = null
    var onMessageReceivedCallback: ((String) -> Unit)? = null
    var onBinaryReceivedCallback: ((ByteArray) -> Unit)? = null
    var onErrorCallback: ((String) -> Unit)? = null
    var onDisconnectedCallback: (() -> Unit)? = null
    
    /**
     * Establishes a secure connection to the SocketX server.
     * This method:
     * 1. Configures OkHttpClient with custom settings
     * 2. Creates the SocketXClient factory (wraps the OkHttpClient)
     * 3. Builds the WebSocket request
     * 4. Calls newWebSocket() to create a secure WebSocket and initiate connection
     */
    fun connect() {
        try {
            // Step 1: Configure OkHttpClient
            // This is where you add custom headers, timeouts, interceptors, etc.
            okHttpClient = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .pingInterval(20, TimeUnit.SECONDS) // Keep connection alive
                .retryOnConnectionFailure(true)
                // Example: Add custom headers via interceptor
                .addInterceptor { chain ->
                    val original = chain.request()
                    val request = original.newBuilder()
                        .header("Authorization", "Bearer your-auth-token")
                        .header("X-API-Key", "your-api-key")
                        .header("X-Client-Version", "1.0.0")
                        .build()
                    chain.proceed(request)
                }
                // Example: Certificate pinning (uncomment and configure as needed)
                // .certificatePinner(
                //     CertificatePinner.Builder()
                //         .add("your-domain.com", "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=")
                //         .build()
                // )
                .build()
            
            // Step 2: Create the SocketXClient factory
            // This wraps your OkHttpClient and validates MTE licensing
            socketXClient = SocketXClient(okHttpClient!!)
            
            // Step 3: Build the WebSocket URL
            // Format: wss://your-server.com/room-name
            // The path (e.g., "/chat-room-1") determines which room you join
            val fullUrl = "$socketXServerUrl$roomPath"
            
            // Step 4: Create the WebSocket request
            val request = Request.Builder()
                .url(fullUrl)
                .build()
            
            // Step 5: Create the WebSocket listener
            val listener = object : WebSocketListener() {
                /**
                 * Called when connection is established AND MTE pairing is complete.
                 * IMPORTANT: This is called AFTER the secure handshake, not just
                 * after the TCP/WebSocket connection.
                 */
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    isConnected = true
                    Log.d(TAG, "✅ Securely connected to SocketX server")
                    Log.d(TAG, "🔐 MTE encryption active")
                    
                    // Notify callback on main thread
                    onConnectedCallback?.invoke()
                }
                
                /**
                 * Called when receiving text messages (automatically decrypted).
                 * The text has already been decrypted by the SocketX client.
                 */
                override fun onMessage(webSocket: WebSocket, text: String) {
                    Log.d(TAG, "📨 Received text: $text")
                    
                    // Parse the message (e.g., JSON)
                    try {
                        val json = JSONObject(text)
                        val messageType = json.optString("type", "unknown")
                        val content = json.optString("message", "")
                        
                        Log.d(TAG, "Message type: $messageType, content: $content")
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to parse JSON: $text", e)
                    }
                    
                    // Notify callback
                    onMessageReceivedCallback?.invoke(text)
                }
                
                /**
                 * Called when receiving binary data (automatically decrypted).
                 * The data has already been decrypted by the SocketX client.
                 */
                override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                    val data = bytes.toByteArray()
                    Log.d(TAG, "📦 Received binary: ${data.size} bytes")
                    
                    // Process binary data (images, files, etc.)
                    onBinaryReceivedCallback?.invoke(data)
                }
                
                /**
                 * Called when the WebSocket connection is closing.
                 */
                override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                    Log.d(TAG, "🔌 Connection closing: $code - $reason")
                    isConnected = false
                }
                
                /**
                 * Called when the WebSocket connection is fully closed.
                 */
                override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                    isConnected = false
                    Log.d(TAG, "🔌 Disconnected from SocketX server: $code - $reason")
                    onDisconnectedCallback?.invoke()
                }
                
                /**
                 * Called when errors occur during connection, handshake, or messaging.
                 */
                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    isConnected = false
                    
                    when (t) {
                        is SocketXError.HandshakeError -> {
                            // MTE pairing handshake failed
                            Log.e(TAG, "❌ MTE Handshake Error: ${t.message}", t)
                            onErrorCallback?.invoke("Handshake failed: ${t.message}")
                            // This might indicate:
                            // - Server not responding to handshake
                            // - MTE version mismatch
                            // - Server configuration issue
                        }
                        
                        is SocketXError.CodecError -> {
                            // MTE encryption/decryption error
                            Log.e(TAG, "❌ MTE Codec Error: ${t.message}", t)
                            onErrorCallback?.invoke("Encryption error: ${t.message}")
                            // This might indicate:
                            // - Pairing state mismatch
                            // - Corrupted data
                            // - Need to reconnect
                        }
                        
                        is SocketXError.NetworkError -> {
                            // WebSocket or network error
                            Log.e(TAG, "❌ Network Error: ${t.message}", t)
                            onErrorCallback?.invoke("Network error: ${t.message}")
                            // This might indicate:
                            // - Connection lost
                            // - Server unavailable
                            // - Timeout
                        }
                        
                        is SocketXError -> {
                            // Other SocketX errors
                            Log.e(TAG, "❌ SocketX Error: ${t.message}", t)
                            onErrorCallback?.invoke("SocketX error: ${t.message}")
                        }
                        
                        else -> {
                            // General errors
                            Log.e(TAG, "❌ Error: ${t.message}", t)
                            onErrorCallback?.invoke("Error: ${t.message}")
                        }
                    }
                }
            }
            
            // Step 6: Create the secure WebSocket
            // The factory creates a secure WebSocket wrapper and initiates:
            // 1. Underlying WebSocket connection
            // 2. Automatic MTE pairing handshake
            // 3. Room connection
            webSocket = socketXClient!!.newWebSocket(request, listener)
            
            Log.d(TAG, "🔄 Connecting to SocketX server at $fullUrl...")
            
        } catch (e: Exception) {
            isConnected = false
            Log.e(TAG, "❌ SocketX initialization failed", e)
            onErrorCallback?.invoke("Initialization failed: ${e.message}")
            // Common errors:
            // - MTE license validation failure
            // - Invalid configuration
        }
    }
    
    /**
     * Disconnects from the SocketX server.
     */
    fun disconnect() {
        webSocket?.close(1000, "Client disconnect")
        webSocket = null
        isConnected = false
        Log.d(TAG, "🔌 Disconnecting from SocketX server")
    }
    
    /**
     * Sends a text message (will be encrypted automatically).
     * 
     * @param text The text message to send (typically JSON string)
     * @return true if message was queued successfully, false if not connected
     */
    fun sendMessage(text: String): Boolean {
        if (!isConnected) {
            Log.w(TAG, "⚠️ Cannot send - not connected")
            return false
        }
        
        return try {
            webSocket?.send(text) ?: false.also {
                Log.w(TAG, "📤 Failed to send text - WebSocket is null")
            }
        } catch (e: Exception) {
            Log.e(TAG, "📤 Error sending text message", e)
            false
        }.also { success ->
            if (success) {
                Log.d(TAG, "📤 Sent text: $text")
            }
        }
    }
    
    /**
     * Sends binary data (will be encrypted automatically).
     * 
     * @param data The binary data to send (images, files, etc.)
     * @return true if data was queued successfully, false if not connected
     */
    fun sendBinary(data: ByteArray): Boolean {
        if (!isConnected) {
            Log.w(TAG, "⚠️ Cannot send - not connected")
            return false
        }
        
        return try {
            webSocket?.send(data.toByteString()) ?: false.also {
                Log.w(TAG, "📤 Failed to send binary - WebSocket is null")
            }
        } catch (e: Exception) {
            Log.e(TAG, "📤 Error sending binary data", e)
            false
        }.also { success ->
            if (success) {
                Log.d(TAG, "📤 Sent binary: ${data.size} bytes")
            }
        }
    }
    
    /**
     * Clean up resources when done.
     * Call this in onDestroy() or when the manager is no longer needed.
     */
    fun cleanup() {
        disconnect()
        socketXClient = null
        okHttpClient = null
    }
    
    companion object {
        private const val TAG = "ChatManager"
    }
}
```

**Java Example:**

```java
import android.util.Log;
import okhttp3.*;
import okio.ByteString;
import com.eclypses.socketx_client_android.SocketXClient;
import com.eclypses.socketx_client_android.SocketXError;
import org.json.JSONObject;
import java.util.concurrent.TimeUnit;

/**
 * This class manages your secure WebSocket communication through SocketX.
 * It handles connection lifecycle, message encryption/decryption, and error handling.
 */
public class ChatManager {
    private static final String TAG = "ChatManager";
    
    private final String socketXServerUrl;
    private final String roomPath;
    
    private OkHttpClient okHttpClient;
    private SocketXClient socketXClient;
    private WebSocket webSocket;
    
    private boolean isConnected = false;
    
    // Callbacks
    public interface OnConnectedCallback {
        void onConnected();
    }
    
    public interface OnMessageReceivedCallback {
        void onMessageReceived(String text);
    }
    
    public interface OnBinaryReceivedCallback {
        void onBinaryReceived(byte[] data);
    }
    
    public interface OnErrorCallback {
        void onError(String error);
    }
    
    public interface OnDisconnectedCallback {
        void onDisconnected();
    }
    
    private OnConnectedCallback onConnectedCallback;
    private OnMessageReceivedCallback onMessageReceivedCallback;
    private OnBinaryReceivedCallback onBinaryReceivedCallback;
    private OnErrorCallback onErrorCallback;
    private OnDisconnectedCallback onDisconnectedCallback;
    
    public ChatManager(String socketXServerUrl, String roomPath) {
        this.socketXServerUrl = socketXServerUrl;
        this.roomPath = roomPath;
    }
    
    // Setter methods for callbacks
    public void setOnConnectedCallback(OnConnectedCallback callback) {
        this.onConnectedCallback = callback;
    }
    
    public void setOnMessageReceivedCallback(OnMessageReceivedCallback callback) {
        this.onMessageReceivedCallback = callback;
    }
    
    public void setOnBinaryReceivedCallback(OnBinaryReceivedCallback callback) {
        this.onBinaryReceivedCallback = callback;
    }
    
    public void setOnErrorCallback(OnErrorCallback callback) {
        this.onErrorCallback = callback;
    }
    
    public void setOnDisconnectedCallback(OnDisconnectedCallback callback) {
        this.onDisconnectedCallback = callback;
    }
    
    public boolean isConnected() {
        return isConnected;
    }
    
    /**
     * Establishes a secure connection to the SocketX server.
     */
    public void connect() {
        try {
            // Step 1: Configure OkHttpClient
            okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .pingInterval(20, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                // Add custom headers via interceptor
                .addInterceptor(chain -> {
                    Request original = chain.request();
                    Request request = original.newBuilder()
                        .header("Authorization", "Bearer your-auth-token")
                        .header("X-API-Key", "your-api-key")
                        .header("X-Client-Version", "1.0.0")
                        .build();
                    return chain.proceed(request);
                })
                .build();
            
            // Step 2: Create the SocketXClient factory
            // This wraps your OkHttpClient and validates MTE licensing
            socketXClient = new SocketXClient(okHttpClient);
            
            // Step 3: Build the WebSocket URL
            String fullUrl = socketXServerUrl + roomPath;
            
            // Step 4: Create the WebSocket request
            Request request = new Request.Builder()
                .url(fullUrl)
                .build();
            
            // Step 5: Create the WebSocket listener
            WebSocketListener listener = new WebSocketListener() {
                @Override
                public void onOpen(WebSocket webSocket, Response response) {
                    isConnected = true;
                    Log.d(TAG, "✅ Securely connected to SocketX server");
                    Log.d(TAG, "🔐 MTE encryption active");
                    
                    if (onConnectedCallback != null) {
                        onConnectedCallback.onConnected();
                    }
                }
                
                @Override
                public void onMessage(WebSocket webSocket, String text) {
                    Log.d(TAG, "📨 Received text: " + text);
                    
                    // Parse JSON
                    try {
                        JSONObject json = new JSONObject(text);
                        String messageType = json.optString("type", "unknown");
                        String content = json.optString("message", "");
                        
                        Log.d(TAG, "Message type: " + messageType + ", content: " + content);
                    } catch (Exception e) {
                        Log.w(TAG, "Failed to parse JSON: " + text, e);
                    }
                    
                    if (onMessageReceivedCallback != null) {
                        onMessageReceivedCallback.onMessageReceived(text);
                    }
                }
                
                @Override
                public void onMessage(WebSocket webSocket, ByteString bytes) {
                    byte[] data = bytes.toByteArray();
                    Log.d(TAG, "📦 Received binary: " + data.length + " bytes");
                    
                    if (onBinaryReceivedCallback != null) {
                        onBinaryReceivedCallback.onBinaryReceived(data);
                    }
                }
                
                @Override
                public void onClosing(WebSocket webSocket, int code, String reason) {
                    Log.d(TAG, "🔌 Connection closing: " + code + " - " + reason);
                    isConnected = false;
                }
                
                @Override
                public void onClosed(WebSocket webSocket, int code, String reason) {
                    isConnected = false;
                    Log.d(TAG, "🔌 Disconnected from SocketX server: " + code + " - " + reason);
                    
                    if (onDisconnectedCallback != null) {
                        onDisconnectedCallback.onDisconnected();
                    }
                }
                
                @Override
                public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                    isConnected = false;
                    
                    if (t instanceof SocketXError.HandshakeError) {
                        Log.e(TAG, "❌ MTE Handshake Error: " + t.getMessage(), t);
                        if (onErrorCallback != null) {
                            onErrorCallback.onError("Handshake failed: " + t.getMessage());
                        }
                    } else if (t instanceof SocketXError.CodecError) {
                        Log.e(TAG, "❌ MTE Codec Error: " + t.getMessage(), t);
                        if (onErrorCallback != null) {
                            onErrorCallback.onError("Encryption error: " + t.getMessage());
                        }
                    } else if (t instanceof SocketXError.NetworkError) {
                        Log.e(TAG, "❌ Network Error: " + t.getMessage(), t);
                        if (onErrorCallback != null) {
                            onErrorCallback.onError("Network error: " + t.getMessage());
                        }
                    } else if (t instanceof SocketXError) {
                        Log.e(TAG, "❌ SocketX Error: " + t.getMessage(), t);
                        if (onErrorCallback != null) {
                            onErrorCallback.onError("SocketX error: " + t.getMessage());
                        }
                    } else {
                        Log.e(TAG, "❌ Error: " + t.getMessage(), t);
                        if (onErrorCallback != null) {
                            onErrorCallback.onError("Error: " + t.getMessage());
                        }
                    }
                }
            };
            
            // Step 6: Create the secure WebSocket
            // The factory creates a secure WebSocket wrapper and initiates connection
            webSocket = socketXClient.newWebSocket(request, listener);
            
            Log.d(TAG, "🔄 Connecting to SocketX server at " + fullUrl + "...");
            
        } catch (Exception e) {
            isConnected = false;
            Log.e(TAG, "❌ SocketX initialization failed", e);
            if (onErrorCallback != null) {
                onErrorCallback.onError("Initialization failed: " + e.getMessage());
            }
        }
    }
    
    /**
     * Disconnects from the SocketX server.
     */
    public void disconnect() {
        if (webSocket != null) {
            webSocket.close(1000, "Client disconnect");
            webSocket = null;
        }
        isConnected = false;
        Log.d(TAG, "🔌 Disconnecting from SocketX server");
    }
    
    /**
     * Sends a text message (will be encrypted automatically).
     */
    public boolean sendMessage(String text) {
        if (!isConnected) {
            Log.w(TAG, "⚠️ Cannot send - not connected");
            return false;
        }
        
        try {
            boolean success = webSocket != null && webSocket.send(text);
            if (success) {
                Log.d(TAG, "📤 Sent text: " + text);
            } else {
                Log.w(TAG, "📤 Failed to send text - WebSocket is null");
            }
            return success;
        } catch (Exception e) {
            Log.e(TAG, "📤 Error sending text message", e);
            return false;
        }
    }
    
    /**
     * Sends binary data (will be encrypted automatically).
     */
    public boolean sendBinary(byte[] data) {
        if (!isConnected) {
            Log.w(TAG, "⚠️ Cannot send - not connected");
            return false;
        }
        
        try {
            boolean success = webSocket != null && webSocket.send(ByteString.of(data));
            if (success) {
                Log.d(TAG, "📤 Sent binary: " + data.length + " bytes");
            } else {
                Log.w(TAG, "📤 Failed to send binary - WebSocket is null");
            }
            return success;
        } catch (Exception e) {
            Log.e(TAG, "📤 Error sending binary data", e);
            return false;
        }
    }
    
    /**
     * Clean up resources when done.
     */
    public void cleanup() {
        disconnect();
        socketXClient = null;
        okHttpClient = null;
    }
}
```

### Step 6: Usage in Your Activity or Fragment

**Kotlin (in an Activity):**

```kotlin
class ChatActivity : AppCompatActivity() {
    private lateinit var chatManager: ChatManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        
        // Initialize the chat manager
        chatManager = ChatManager(
            socketXServerUrl = "wss://your-socketx-server.com",
            roomPath = "/chat-room-1"
        )
        
        // Set up callbacks
        chatManager.onConnectedCallback = {
            runOnUiThread {
                statusTextView.text = "🟢 Connected"
                sendButton.isEnabled = true
            }
        }
        
        chatManager.onMessageReceivedCallback = { text ->
            runOnUiThread {
                // Update UI with received message
                messagesAdapter.addMessage(text)
            }
        }
        
        chatManager.onErrorCallback = { error ->
            runOnUiThread {
                statusTextView.text = "🔴 Error: $error"
                sendButton.isEnabled = false
            }
        }
        
        // Connect to SocketX server
        chatManager.connect()
        
        // Set up send button
        sendButton.setOnClickListener {
            val message = messageEditText.text.toString()
            if (message.isNotEmpty()) {
                chatManager.sendMessage(message)
                messageEditText.text.clear()
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        chatManager.cleanup()
    }
}
```

**Java (in an Activity):**

```java
public class ChatActivity extends AppCompatActivity {
    private ChatManager chatManager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        
        // Initialize the chat manager
        chatManager = new ChatManager(
            "wss://your-socketx-server.com",
            "/chat-room-1"
        );
        
        // Set up callbacks
        chatManager.setOnConnectedCallback(() -> {
            runOnUiThread(() -> {
                statusTextView.setText("🟢 Connected");
                sendButton.setEnabled(true);
            });
        });
        
        chatManager.setOnMessageReceivedCallback(text -> {
            runOnUiThread(() -> {
                // Update UI with received message
                messagesAdapter.addMessage(text);
            });
        });
        
        chatManager.setOnErrorCallback(error -> {
            runOnUiThread(() -> {
                statusTextView.setText("🔴 Error: " + error);
                sendButton.setEnabled(false);
            });
        });
        
        // Connect to SocketX server
        chatManager.connect();
        
        // Set up send button
        sendButton.setOnClickListener(v -> {
            String message = messageEditText.getText().toString();
            if (!message.isEmpty()) {
                chatManager.sendMessage(message);
                messageEditText.setText("");
            }
        });
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        chatManager.cleanup();
    }
}
```

## Usage

### Sending Messages

Once connected, you can send text or binary messages. The SocketX client automatically encrypts all outgoing data before transmission.

#### Sending Text Messages

Text messages are typically JSON strings, chat messages, or any string-based protocol.

**Kotlin:**
```kotlin
// Send a simple text message
chatManager.sendMessage("Hello, World!")

// Send JSON data
val jsonObject = JSONObject().apply {
    put("type", "chat")
    put("message", "Hello")
    put("userId", "123")
}
chatManager.sendMessage(jsonObject.toString())

// Send using Gson
data class ChatMessage(val type: String, val content: String, val timestamp: Long)

val message = ChatMessage("chat", "Hello", System.currentTimeMillis())
val json = Gson().toJson(message)
chatManager.sendMessage(json)
```

**Java:**
```java
// Send a simple text message
chatManager.sendMessage("Hello, World!");

// Send JSON data
try {
    JSONObject json = new JSONObject();
    json.put("type", "chat");
    json.put("message", "Hello");
    json.put("userId", "123");
    chatManager.sendMessage(json.toString());
} catch (JSONException e) {
    e.printStackTrace();
}

// Send using Gson
class ChatMessage {
    String type;
    String content;
    long timestamp;
}

ChatMessage message = new ChatMessage();
message.type = "chat";
message.content = "Hello";
message.timestamp = System.currentTimeMillis();

Gson gson = new Gson();
String json = gson.toJson(message);
chatManager.sendMessage(json);
```

#### Sending Binary Data

Binary messages are useful for images, files, audio, video, or custom binary protocols.

**Kotlin:**
```kotlin
// Send raw bytes
val bytes = byteArrayOf(0x48, 0x65, 0x6C, 0x6C, 0x6F) // "Hello" in hex
chatManager.sendBinary(bytes)

// Send an image
val bitmap = BitmapFactory.decodeResource(resources, R.drawable.photo)
val stream = ByteArrayOutputStream()
bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
val imageData = stream.toByteArray()
chatManager.sendBinary(imageData)
Log.d(TAG, "📤 Sent image: ${imageData.size} bytes")

// Send a file
val file = File("/path/to/file.pdf")
val fileData = file.readBytes()
chatManager.sendBinary(fileData)
Log.d(TAG, "📤 Sent file: ${fileData.size} bytes")
```

**Java:**
```java
// Send raw bytes
byte[] bytes = new byte[]{0x48, 0x65, 0x6C, 0x6C, 0x6F}; // "Hello" in hex
chatManager.sendBinary(bytes);

// Send an image
Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.photo);
ByteArrayOutputStream stream = new ByteArrayOutputStream();
bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
byte[] imageData = stream.toByteArray();
chatManager.sendBinary(imageData);
Log.d(TAG, "📤 Sent image: " + imageData.length + " bytes");

// Send a file
try {
    File file = new File("/path/to/file.pdf");
    byte[] fileData = Files.readAllBytes(file.toPath());
    chatManager.sendBinary(fileData);
    Log.d(TAG, "📤 Sent file: " + fileData.length + " bytes");
} catch (IOException e) {
    e.printStackTrace();
}
```

**Important Notes:**
- All data is automatically encrypted using MTE before transmission
- There's no separate "secure send" method - all sends are secure
- Make sure you're connected before sending (check `isConnected` or rely on `onConnected` callback)

### Receiving Messages

Messages arrive through the callback handlers you set up. The SocketX client automatically decrypts all incoming data.

#### Receiving Text Messages

**Kotlin:**
```kotlin
chatManager.onMessageReceivedCallback = { text ->
    Log.d(TAG, "📨 Received: $text")
    
    // Parse JSON
    try {
        val json = JSONObject(text)
        val messageType = json.optString("type", "unknown")
        val content = json.optString("message", "")
        
        Log.d(TAG, "Type: $messageType, Content: $content")
        
        // Update UI on main thread
        runOnUiThread {
            messagesTextView.append("$content\n")
        }
    } catch (e: JSONException) {
        Log.w(TAG, "Failed to parse JSON", e)
    }
    
    // Or decode using Gson
    try {
        val message = Gson().fromJson(text, ChatMessage::class.java)
        Log.d(TAG, "Decoded message: ${message.content} at ${message.timestamp}")
    } catch (e: Exception) {
        Log.w(TAG, "Failed to decode with Gson", e)
    }
}
```

**Java:**
```java
chatManager.setOnMessageReceivedCallback(text -> {
    Log.d(TAG, "📨 Received: " + text);
    
    // Parse JSON
    try {
        JSONObject json = new JSONObject(text);
        String messageType = json.optString("type", "unknown");
        String content = json.optString("message", "");
        
        Log.d(TAG, "Type: " + messageType + ", Content: " + content);
        
        // Update UI on main thread
        runOnUiThread(() -> {
            messagesTextView.append(content + "\n");
        });
    } catch (JSONException e) {
        Log.w(TAG, "Failed to parse JSON", e);
    }
    
    // Or decode using Gson
    try {
        Gson gson = new Gson();
        ChatMessage message = gson.fromJson(text, ChatMessage.class);
        Log.d(TAG, "Decoded message: " + message.content + " at " + message.timestamp);
    } catch (Exception e) {
        Log.w(TAG, "Failed to decode with Gson", e);
    }
});
```

#### Receiving Binary Data

**Kotlin:**
```kotlin
chatManager.onBinaryReceivedCallback = { data ->
    Log.d(TAG, "📦 Received binary: ${data.size} bytes")
    
    // Display an image
    val bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
    if (bitmap != null) {
        runOnUiThread {
            imageView.setImageBitmap(bitmap)
        }
    }
    
    // Save a file
    val file = File(getExternalFilesDir(null), "received-file.dat")
    try {
        file.writeBytes(data)
        Log.d(TAG, "💾 Saved to: ${file.absolutePath}")
    } catch (e: IOException) {
        Log.e(TAG, "Failed to save file", e)
    }
}
```

**Java:**
```java
chatManager.setOnBinaryReceivedCallback(data -> {
    Log.d(TAG, "📦 Received binary: " + data.length + " bytes");
    
    // Display an image
    Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
    if (bitmap != null) {
        runOnUiThread(() -> {
            imageView.setImageBitmap(bitmap);
        });
    }
    
    // Save a file
    File file = new File(getExternalFilesDir(null), "received-file.dat");
    try {
        Files.write(file.toPath(), data);
        Log.d(TAG, "💾 Saved to: " + file.getAbsolutePath());
    } catch (IOException e) {
        Log.e(TAG, "Failed to save file", e);
    }
});
```

### Connection Lifecycle Management

#### Monitoring Connection Status

**Kotlin:**
```kotlin
// Track connection state
chatManager.onConnectedCallback = {
    runOnUiThread {
        statusTextView.text = "🟢 Connected"
        statusTextView.setTextColor(Color.GREEN)
        sendButton.isEnabled = true
    }
}

chatManager.onDisconnectedCallback = {
    runOnUiThread {
        statusTextView.text = "🔴 Disconnected"
        statusTextView.setTextColor(Color.RED)
        sendButton.isEnabled = false
    }
}

chatManager.onErrorCallback = { error ->
    runOnUiThread {
        statusTextView.text = "🔴 Error: $error"
        statusTextView.setTextColor(Color.RED)
        sendButton.isEnabled = false
    }
}
```

**Java:**
```java
chatManager.setOnConnectedCallback(() -> {
    runOnUiThread(() -> {
        statusTextView.setText("🟢 Connected");
        statusTextView.setTextColor(Color.GREEN);
        sendButton.setEnabled(true);
    });
});

chatManager.setOnDisconnectedCallback(() -> {
    runOnUiThread(() -> {
        statusTextView.setText("🔴 Disconnected");
        statusTextView.setTextColor(Color.RED);
        sendButton.setEnabled(false);
    });
});

chatManager.setOnErrorCallback(error -> {
    runOnUiThread(() -> {
        statusTextView.setText("🔴 Error: " + error);
        statusTextView.setTextColor(Color.RED);
        sendButton.setEnabled(false);
    });
});
```

#### Manual Disconnection

**Kotlin:**
```kotlin
// Gracefully disconnect when done
override fun onDestroy() {
    super.onDestroy()
    chatManager.cleanup() // Disconnects and cleans up resources
}

// Or just disconnect without cleanup
override fun onPause() {
    super.onPause()
    chatManager.disconnect()
}
```

**Java:**
```java
@Override
protected void onDestroy() {
    super.onDestroy();
    chatManager.cleanup(); // Disconnects and cleans up resources
}

@Override
protected void onPause() {
    super.onPause();
    chatManager.disconnect();
}
```

#### Room-Based Communication

The SocketX server supports room-based messaging, where the URL path determines which room you join. This enables pub/sub patterns and isolated communication channels.

**Kotlin:**
```kotlin
// Different rooms for different purposes
class MultiRoomManager(private val serverUrl: String) {
    private var chatClient: ChatManager? = null
    private var notificationClient: ChatManager? = null
    
    fun connectToChatRoom() {
        chatClient = ChatManager(serverUrl, "/chat/room-1")
        chatClient?.onMessageReceivedCallback = { text ->
            Log.d(TAG, "💬 Chat message: $text")
        }
        chatClient?.connect()
    }
    
    fun connectToNotifications() {
        notificationClient = ChatManager(serverUrl, "/notifications/user-123")
        notificationClient?.onMessageReceivedCallback = { text ->
            Log.d(TAG, "🔔 Notification: $text")
        }
        notificationClient?.connect()
    }
    
    fun broadcastToRoom(message: String) {
        // Send to chat room - all connected clients in that room will receive it
        chatClient?.sendMessage(message)
    }
    
    fun cleanup() {
        chatClient?.cleanup()
        notificationClient?.cleanup()
    }
}
```

**Java:**
```java
public class MultiRoomManager {
    private static final String TAG = "MultiRoomManager";
    private final String serverUrl;
    
    private ChatManager chatClient;
    private ChatManager notificationClient;
    
    public MultiRoomManager(String serverUrl) {
        this.serverUrl = serverUrl;
    }
    
    public void connectToChatRoom() {
        chatClient = new ChatManager(serverUrl, "/chat/room-1");
        chatClient.setOnMessageReceivedCallback(text -> {
            Log.d(TAG, "💬 Chat message: " + text);
        });
        chatClient.connect();
    }
    
    public void connectToNotifications() {
        notificationClient = new ChatManager(serverUrl, "/notifications/user-123");
        notificationClient.setOnMessageReceivedCallback(text -> {
            Log.d(TAG, "🔔 Notification: " + text);
        });
        notificationClient.connect();
    }
    
    public void broadcastToRoom(String message) {
        if (chatClient != null) {
            chatClient.sendMessage(message);
        }
    }
    
    public void cleanup() {
        if (chatClient != null) {
            chatClient.cleanup();
        }
        if (notificationClient != null) {
            notificationClient.cleanup();
        }
    }
}
```

**URL Path Examples:**
- `/chat/general` - General chat room
- `/chat/private/user123` - Private user-specific room
- `/notifications/user123` - User-specific notification channel
- `/game/match-456` - Game-specific room
- `/stream/video-1` - Streaming channel

The SocketX server determines routing and message distribution based on these paths.

## Error Handling

Understanding and properly handling errors is crucial for a robust implementation.

### Error Types

The `SocketXError` sealed class defines all possible error cases:

**Kotlin:**
```kotlin
sealed class SocketXError : Throwable() {
    class HandshakeError(message: String) : SocketXError()
    class CodecError(message: String) : SocketXError()
    class NetworkError(message: String) : SocketXError()
    class InternalError(message: String) : SocketXError()
}
```

### Comprehensive Error Handling

**Kotlin:**
```kotlin
chatManager.onErrorCallback = { errorMessage ->
    Log.e(TAG, "Error occurred: $errorMessage")
    
    // Show user-friendly error messages
    runOnUiThread {
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
    }
    
    // Optionally attempt reconnection
    if (errorMessage.contains("Network")) {
        scheduleReconnect()
    }
}

// In your WebSocketListener
override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
    when (t) {
        is SocketXError.HandshakeError -> {
            // MTE pairing handshake failed
            Log.e(TAG, "❌ MTE Handshake Error: ${t.message}", t)
            // Possible causes:
            // - Server not responding to handshake
            // - MTE version mismatch
            // - Server configuration issue
            
            // Recommended actions:
            // 1. Log the error for diagnostics
            // 2. Show user message
            // 3. Retry connection after delay
        }
        
        is SocketXError.CodecError -> {
            // MTE encryption/decryption error
            Log.e(TAG, "❌ MTE Codec Error: ${t.message}", t)
            // Possible causes:
            // - Pairing state desynchronized
            // - Corrupted data
            // - Need to reconnect
            
            // Recommended actions:
            // 1. Disconnect and reconnect (forces re-pairing)
            // 2. Alert user if problem persists
        }
        
        is SocketXError.NetworkError -> {
            // WebSocket or network error
            Log.e(TAG, "❌ Network Error: ${t.message}", t)
            // Possible causes:
            // - Connection lost (user went offline)
            // - Server shut down or restarted
            // - Network timeout
            
            // Recommended actions:
            // 1. Check network connectivity
            // 2. Implement exponential backoff reconnection
            // 3. Show user-friendly message
        }
        
        is SocketXError.InternalError -> {
            // Internal SocketX error (e.g., MTE license failure)
            Log.e(TAG, "❌ Internal Error: ${t.message}", t)
            // This usually indicates a configuration issue
        }
        
        else -> {
            // General errors
            Log.e(TAG, "❌ General Error: ${t.message}", t)
        }
    }
}

private fun scheduleReconnect() {
    Handler(Looper.getMainLooper()).postDelayed({
        chatManager.connect()
    }, 5000) // Retry after 5 seconds
}
```

**Java:**
```java
chatManager.setOnErrorCallback(errorMessage -> {
    Log.e(TAG, "Error occurred: " + errorMessage);
    
    runOnUiThread(() -> {
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
    });
    
    if (errorMessage.contains("Network")) {
        scheduleReconnect();
    }
});

// In your WebSocketListener
@Override
public void onFailure(WebSocket webSocket, Throwable t, Response response) {
    if (t instanceof SocketXError.HandshakeError) {
        Log.e(TAG, "❌ MTE Handshake Error: " + t.getMessage(), t);
        // Handle handshake error
    } else if (t instanceof SocketXError.CodecError) {
        Log.e(TAG, "❌ MTE Codec Error: " + t.getMessage(), t);
        // Handle codec error - disconnect and reconnect
    } else if (t instanceof SocketXError.NetworkError) {
        Log.e(TAG, "❌ Network Error: " + t.getMessage(), t);
        // Handle network error - check connectivity
    } else if (t instanceof SocketXError.InternalError) {
        Log.e(TAG, "❌ Internal Error: " + t.getMessage(), t);
        // Handle internal error
    } else {
        Log.e(TAG, "❌ General Error: " + t.getMessage(), t);
    }
}

private void scheduleReconnect() {
    new Handler(Looper.getMainLooper()).postDelayed(() -> {
        chatManager.connect();
    }, 5000); // Retry after 5 seconds
}
```

### Preventing Common Errors

**1. Don't send before connected:**

**Kotlin:**
```kotlin
fun sendMessage(text: String) {
    if (!chatManager.isConnected) {
        Log.w(TAG, "⚠️ Not connected - message queued")
        pendingMessages.add(text)
        return
    }
    chatManager.sendMessage(text)
}

// Send queued messages when connected
chatManager.onConnectedCallback = {
    pendingMessages.forEach { message ->
        chatManager.sendMessage(message)
    }
    pendingMessages.clear()
}
```

**Java:**
```java
private List<String> pendingMessages = new ArrayList<>();

public void sendMessage(String text) {
    if (!chatManager.isConnected()) {
        Log.w(TAG, "⚠️ Not connected - message queued");
        pendingMessages.add(text);
        return;
    }
    chatManager.sendMessage(text);
}

// Send queued messages when connected
chatManager.setOnConnectedCallback(() -> {
    for (String message : pendingMessages) {
        chatManager.sendMessage(message);
    }
    pendingMessages.clear();
});
```

**2. Handle initialization errors:**

**Kotlin:**
```kotlin
try {
    socketXClient = SocketXClient(okHttpClient)
} catch (e: SocketXError.InternalError) {
    Log.e(TAG, "❌ Initialization error: ${e.message}", e)
    // Possible issues:
    // - Invalid MTE license
    // - Invalid configuration
}
```

**Java:**
```java
try {
    socketXClient = new SocketXClient(okHttpClient);
} catch (SocketXError.InternalError e) {
    Log.e(TAG, "❌ Initialization error: " + e.getMessage(), e);
    // Possible issues:
    // - Invalid MTE license
    // - Invalid configuration
}
```

**3. Clean up resources:**

**Kotlin:**
```kotlin
override fun onDestroy() {
    super.onDestroy()
    chatManager.cleanup() // Always clean up to free resources
}
```

**Java:**
```java
@Override
protected void onDestroy() {
    super.onDestroy();
    chatManager.cleanup(); // Always clean up to free resources
}
```

## Troubleshooting

This section covers common issues and their solutions.

### Connection Issues

**Problem: Connection fails immediately**
```
Error: "Network Error: Failed to connect"
```

**Solutions:**

1. **Verify server URL is correct:**
   
   **Kotlin:**
   ```kotlin
   // ✅ Correct
   val url = "wss://your-server.com/room"
   
   // ❌ Wrong - missing protocol
   val url = "your-server.com/room"
   
   // ❌ Wrong - using http instead of ws
   val url = "https://your-server.com/room"
   ```
   
   **Java:**
   ```java
   // ✅ Correct
   String url = "wss://your-server.com/room";
   
   // ❌ Wrong - missing protocol
   String url = "your-server.com/room";
   
   // ❌ Wrong - using http instead of ws
   String url = "https://your-server.com/room";
   ```

2. **Check server is running and accessible:**
   ```bash
   # Test WebSocket connectivity (requires wscat)
   wscat -c wss://your-server.com/test
   ```

3. **Verify network permissions in AndroidManifest.xml:**
   ```xml
   <uses-permission android:name="android.permission.INTERNET" />
   
   <!-- For HTTP servers (not recommended for production) -->
   <application
       android:usesCleartextTraffic="true"
       ...>
   </application>
   ```

4. **Check network connectivity in your app:**
   
   **Kotlin:**
   ```kotlin
   fun isNetworkAvailable(): Boolean {
       val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
       val network = connectivityManager.activeNetwork ?: return false
       val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
       return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
   }
   ```
   
   **Java:**
   ```java
   public boolean isNetworkAvailable() {
       ConnectivityManager connectivityManager = 
           (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
       Network network = connectivityManager.getActiveNetwork();
       if (network == null) return false;
       NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
       return capabilities != null && 
              capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
   }
   ```

---

**Problem: "Handshake Error: ..." during connection**

**Solutions:**

1. **Verify MTE license is valid** - Check with Eclypses support if needed
2. **Confirm server MTE configuration matches client**
3. **Check server logs** for pairing rejection reasons

---

**Problem: Connects in emulator but fails on device**

**Solutions:**

1. **Check device network connectivity** (Wi-Fi vs cellular)
2. **Verify server URL is accessible from device** (not localhost)
3. **Review certificate pinning configuration** if implemented
4. **Check if firewall/VPN is blocking WebSocket connections**

---

### Message Issues

**Problem: Messages sent but not received by server**

**Solutions:**

1. **Verify you're connected before sending:**
   
   **Kotlin:**
   ```kotlin
   if (!chatManager.isConnected) {
       Log.w(TAG, "⚠️ Not connected")
       return
   }
   chatManager.sendMessage(message)
   ```
   
   **Java:**
   ```java
   if (!chatManager.isConnected()) {
       Log.w(TAG, "⚠️ Not connected");
       return;
   }
   chatManager.sendMessage(message);
   ```

2. **Check server logs** to confirm messages are arriving
3. **Verify room path is correct** (messages only go to the specified room)
4. **Enable debugging** to inspect traffic

---

**Problem: Can send but not receive messages**

**Solutions:**

1. **Verify callbacks are set up before connecting:**
   
   **Kotlin:**
   ```kotlin
   // ✅ Correct order
   chatManager.onMessageReceivedCallback = { text ->
       Log.d(TAG, "📨 $text")
   }
   chatManager.connect()
   
   // ❌ Wrong - callback set after connect
   chatManager.connect()
   chatManager.onMessageReceivedCallback = { /* ... */ }
   ```

2. **Ensure messages are being sent to your room** (check server configuration)

---

**Problem: "Codec Error: ..." when receiving messages**

**Solutions:**

1. **Disconnect and reconnect to re-establish pairing:**
   
   **Kotlin:**
   ```kotlin
   chatManager.disconnect()
   Handler(Looper.getMainLooper()).postDelayed({
       chatManager.connect()
   }, 1000)
   ```
   
   **Java:**
   ```java
   chatManager.disconnect();
   new Handler(Looper.getMainLooper()).postDelayed(() -> {
       chatManager.connect();
   }, 1000);
   ```

2. **Verify all clients/servers are using compatible MTE versions**
3. **Check for data corruption** (network issues, proxy interference)

---

### Performance Issues

**Problem: High memory usage**

**Solutions:**

1. **Don't accumulate messages indefinitely:**
   
   **Kotlin:**
   ```kotlin
   val messages = mutableListOf<String>()
   val maxMessages = 100
   
   chatManager.onMessageReceivedCallback = { text ->
       messages.add(text)
       
       // Trim old messages
       if (messages.size > maxMessages) {
           messages.removeAt(0)
       }
   }
   ```

2. **Process binary data immediately rather than storing:**
   
   **Kotlin:**
   ```kotlin
   chatManager.onBinaryReceivedCallback = { data ->
       // ✅ Process immediately
       processImage(data)
       
       // ❌ Don't store large data unnecessarily
       // allImages.add(data) // Accumulates memory
   }
   ```

3. **Disconnect when not needed:**
   
   **Kotlin:**
   ```kotlin
   override fun onPause() {
       super.onPause()
       chatManager.disconnect()
   }
   
   override fun onResume() {
       super.onResume()
       chatManager.connect()
   }
   ```

---

**Problem: Slow message delivery**

**Solutions:**

1. **Check network conditions** (Wi-Fi vs cellular, signal strength)
2. **Use binary format instead of JSON for large payloads**
3. **Minimize message size** (compress data if needed)
4. **Check server performance** (may be bottleneck)

---

### Platform-Specific Issues

**Problem: App crashes when reconnecting**

**Solutions:**

1. **Properly clean up old instances:**
   
   **Kotlin:**
   ```kotlin
   fun reconnect() {
       // Clean up existing connection
       chatManager.disconnect()
       
       // Create new connection
       chatManager.connect()
   }
   ```

2. **Check for memory leaks** using Android Profiler

---

**Problem: Connection drops in background**

**Solutions:**

1. **Android kills background network connections by design** - This is normal behavior

2. **Use a foreground service** for persistent connections:
   
   **Kotlin:**
   ```kotlin
   class ChatService : Service() {
       private lateinit var chatManager: ChatManager
       
       override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
           // Create foreground notification
           val notification = createNotification()
           startForeground(NOTIFICATION_ID, notification)
           
           // Connect
           chatManager.connect()
           
           return START_STICKY
       }
   }
   ```

3. **Reconnect when app returns to foreground:**
   
   **Kotlin:**
   ```kotlin
   override fun onResume() {
       super.onResume()
       if (!chatManager.isConnected) {
           chatManager.connect()
       }
   }
   ```

---

### Debugging Tips

1. **Enable verbose logging:**
   
   **Kotlin:**
   ```kotlin
   // Add logging interceptor to OkHttp
   val loggingInterceptor = HttpLoggingInterceptor().apply {
       level = HttpLoggingInterceptor.Level.BODY
   }
   
   okHttpClient = OkHttpClient.Builder()
       .addInterceptor(loggingInterceptor)
       .build()
   ```

2. **Monitor callback invocations:**
   
   **Kotlin:**
   ```kotlin
   chatManager.onConnectedCallback = {
       Log.d(TAG, "✅ CALLBACK: onConnected fired")
   }
   
   chatManager.onErrorCallback = { error ->
       Log.e(TAG, "❌ CALLBACK: onError fired - $error")
   }
   ```

3. **Use Android Profiler** to monitor network activity and memory

4. **Check both client and server logs** - issues can be on either side

5. **Test with a simple echo server first** to isolate client vs server issues

---

### Getting Help

If you're still experiencing issues after trying these solutions:

1. **Gather information:**
   - Android version and device model
   - SocketX client library version
   - Complete error messages and stack traces
   - Steps to reproduce
   - Server-side logs (if available)

2. **Check official documentation:**
   - [Getting Started Guide](https://public-docs.eclypses.com/docs/socketx-server/client-libraries/android)
   - Server-side SocketX documentation

3. **Contact support:**
   - **Email:** [info@eclypses.com](mailto:info@eclypses.com)
   - **Developer Portal:** [developers.eclypses.com/dashboard](https://developers.eclypses.com/dashboard)
   - Include all gathered information and logs

## API Reference

Complete reference of the SocketX Android client API.

### SocketXClient Class

The main factory class for creating secure WebSocket connections.

#### Constructor

```kotlin
SocketXClient(client: OkHttpClient)
```

**Kotlin:**
```kotlin
val okHttpClient = OkHttpClient.Builder().build()
val socketXClient = SocketXClient(okHttpClient)
```

**Java:**
```java
OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
SocketXClient socketXClient = new SocketXClient(okHttpClient);
```

**Parameters:**
- `client` - A configured `OkHttpClient` instance. The factory will use this client to create the underlying WebSocket connections.

**Throws:**
- `SocketXError.InternalError` if MTE license validation fails

**Description:**
Initializes the SocketX client factory with the provided OkHttp client. This validates MTE licensing. When you later call `newWebSocket()`, the factory will create a secure WebSocket wrapper around an OkHttp WebSocket connection.

---

#### Methods

##### `newWebSocket()`

```kotlin
fun newWebSocket(request: Request, listener: WebSocketListener): WebSocket
```

**Kotlin:**
```kotlin
val request = Request.Builder()
    .url("wss://your-server.com/room")
    .build()

val listener = object : WebSocketListener() {
    override fun onOpen(webSocket: WebSocket, response: Response) {
        // Connection established and MTE handshake complete
    }
}

val webSocket = socketXClient.newWebSocket(request, listener)
```

**Java:**
```java
Request request = new Request.Builder()
    .url("wss://your-server.com/room")
    .build();

WebSocketListener listener = new WebSocketListener() {
    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        // Connection established and MTE handshake complete
    }
};

WebSocket webSocket = socketXClient.newWebSocket(request, listener);
```

**Parameters:**
- `request` - An OkHttp `Request` object containing the WebSocket URL and any headers
- `listener` - A `WebSocketListener` implementation to handle WebSocket events

**Returns:**
- A `WebSocket` wrapper that provides transparent MTE encryption/decryption

**Description:**
Creates a new secure WebSocket wrapper. This method:
1. Uses your OkHttpClient to establish the underlying WebSocket connection
2. Wraps it in a secure WebSocket that intercepts send/receive operations
3. Automatically performs MTE pairing handshake with the server
4. Provides transparent encryption for all data sent and decryption for all data received
5. Returns a standard `WebSocket` interface for sending/receiving messages

**Important:** The `onOpen` callback will be called AFTER the MTE handshake completes, not immediately after the TCP connection is established.

---

### WebSocket Interface

The returned `WebSocket` implements the standard OkHttp `WebSocket` interface. All data sent/received is automatically encrypted/decrypted.

#### `send(text: String)`

```kotlin
fun send(text: String): Boolean
```

**Kotlin:**
```kotlin
val success = webSocket.send("Hello, World!")
```

**Java:**
```java
boolean success = webSocket.send("Hello, World!");
```

**Parameters:**
- `text` - The text message to send (automatically encrypted)

**Returns:**
- `true` if the message was queued successfully, `false` otherwise

---

#### `send(bytes: ByteString)`

```kotlin
fun send(bytes: ByteString): Boolean
```

**Kotlin:**
```kotlin
val data = byteArrayOf(0x01, 0x02, 0x03)
val success = webSocket.send(data.toByteString())
```

**Java:**
```java
byte[] data = new byte[]{0x01, 0x02, 0x03};
boolean success = webSocket.send(ByteString.of(data));
```

**Parameters:**
- `bytes` - The binary data to send (automatically encrypted)

**Returns:**
- `true` if the data was queued successfully, `false` otherwise

---

#### `close(code: Int, reason: String?)`

```kotlin
fun close(code: Int, reason: String?): Boolean
```

**Kotlin:**
```kotlin
webSocket.close(1000, "Goodbye")
```

**Java:**
```java
webSocket.close(1000, "Goodbye");
```

**Parameters:**
- `code` - WebSocket close code (1000 = normal closure)
- `reason` - Optional reason string

**Returns:**
- `true` if close was initiated, `false` otherwise

---

#### `cancel()`

```kotlin
fun cancel()
```

**Kotlin:**
```kotlin
webSocket.cancel()
```

**Java:**
```java
webSocket.cancel();
```

**Description:**
Immediately and violently release resources held by this connection. This closes the socket without sending a close frame.

---

### WebSocketListener Callbacks

Implement these callbacks to handle WebSocket events.

#### `onOpen(webSocket: WebSocket, response: Response)`

```kotlin
override fun onOpen(webSocket: WebSocket, response: Response)
```

**Called when:**
- WebSocket connection is established AND
- MTE pairing handshake is complete

**Important:** This is called AFTER the secure handshake, not just after the TCP connection.

**Kotlin:**
```kotlin
override fun onOpen(webSocket: WebSocket, response: Response) {
    Log.d(TAG, "✅ Connected and paired")
    webSocket.send("Hello!")
}
```

**Java:**
```java
@Override
public void onOpen(WebSocket webSocket, Response response) {
    Log.d(TAG, "✅ Connected and paired");
    webSocket.send("Hello!");
}
```

---

#### `onMessage(webSocket: WebSocket, text: String)`

```kotlin
override fun onMessage(webSocket: WebSocket, text: String)
```

**Called when:**
- A text message is received (already decrypted)

**Kotlin:**
```kotlin
override fun onMessage(webSocket: WebSocket, text: String) {
    Log.d(TAG, "📨 Received: $text")
}
```

**Java:**
```java
@Override
public void onMessage(WebSocket webSocket, String text) {
    Log.d(TAG, "📨 Received: " + text);
}
```

---

#### `onMessage(webSocket: WebSocket, bytes: ByteString)`

```kotlin
override fun onMessage(webSocket: WebSocket, bytes: ByteString)
```

**Called when:**
- Binary data is received (already decrypted)

**Kotlin:**
```kotlin
override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
    val data = bytes.toByteArray()
    Log.d(TAG, "📦 Received: ${data.size} bytes")
}
```

**Java:**
```java
@Override
public void onMessage(WebSocket webSocket, ByteString bytes) {
    byte[] data = bytes.toByteArray();
    Log.d(TAG, "📦 Received: " + data.length + " bytes");
}
```

---

#### `onClosing(webSocket: WebSocket, code: Int, reason: String)`

```kotlin
override fun onClosing(webSocket: WebSocket, code: Int, reason: String)
```

**Called when:**
- The remote peer has initiated a graceful shutdown

**Kotlin:**
```kotlin
override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
    Log.d(TAG, "Closing: $code - $reason")
    webSocket.close(1000, null) // Confirm closure
}
```

**Java:**
```java
@Override
public void onClosing(WebSocket webSocket, int code, String reason) {
    Log.d(TAG, "Closing: " + code + " - " + reason);
    webSocket.close(1000, null); // Confirm closure
}
```

---

#### `onClosed(webSocket: WebSocket, code: Int, reason: String)`

```kotlin
override fun onClosed(webSocket: WebSocket, code: Int, reason: String)
```

**Called when:**
- Both peers have indicated shutdown, connection is fully closed

**Kotlin:**
```kotlin
override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
    Log.d(TAG, "Closed: $code - $reason")
}
```

**Java:**
```java
@Override
public void onClosed(WebSocket webSocket, int code, String reason) {
    Log.d(TAG, "Closed: " + code + " - " + reason);
}
```

---

#### `onFailure(webSocket: WebSocket, t: Throwable, response: Response?)`

```kotlin
override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?)
```

**Called when:**
- An error occurs during connection, handshake, or messaging

**Kotlin:**
```kotlin
override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
    when (t) {
        is SocketXError.HandshakeError -> Log.e(TAG, "Handshake failed", t)
        is SocketXError.CodecError -> Log.e(TAG, "Codec error", t)
        is SocketXError.NetworkError -> Log.e(TAG, "Network error", t)
        else -> Log.e(TAG, "Error", t)
    }
}
```

**Java:**
```java
@Override
public void onFailure(WebSocket webSocket, Throwable t, Response response) {
    if (t instanceof SocketXError.HandshakeError) {
        Log.e(TAG, "Handshake failed", t);
    } else if (t instanceof SocketXError.CodecError) {
        Log.e(TAG, "Codec error", t);
    } else if (t instanceof SocketXError.NetworkError) {
        Log.e(TAG, "Network error", t);
    } else {
        Log.e(TAG, "Error", t);
    }
}
```

---

### SocketXError Types

Sealed class hierarchy defining all error types.

```kotlin
sealed class SocketXError : Throwable() {
    class HandshakeError(message: String) : SocketXError()
    class CodecError(message: String) : SocketXError()
    class NetworkError(message: String) : SocketXError()
    class InternalError(message: String) : SocketXError()
}
```

**Error Types:**

- **`HandshakeError`** - MTE pairing handshake failed. Usually indicates server issues or version mismatch. Reconnecting typically resolves this.

- **`CodecError`** - MTE encoder/decoder error. Usually indicates corrupted data or desynchronized pairing state. Reconnecting typically resolves this.

- **`NetworkError`** - Network connectivity issue. Connection lost, server unreachable, timeout, etc. Check network availability and server status.

- **`InternalError`** - Internal SocketX error (e.g., MTE license validation failure). Usually indicates a configuration issue.

---

## Contact Eclypses

**Email:** [info@eclypses.com](mailto:info@eclypses.com)  
**Web:** [www.eclypses.com](https://www.eclypses.com)  
**Developer Portal:** [developers.eclypses.com/dashboard](https://developers.eclypses.com/dashboard)

---
**All trademarks of Eclypses Inc.** may not be used without Eclypses Inc.'s prior written consent.
