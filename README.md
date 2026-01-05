<center>
<img src="Eclypses.png" style="width:50%;">
</center>

<div align="center" style="font-size:40pt; font-weight:900; font-family:arial; margin-top:50px;">
SocketX Mobile Client<br>Android Library</div>

![Latest Release](https://img.shields.io/github/v/release/Eclypses/socketx-client-android?style=flat-square)

## Introduction
This package provides the Eclypses SocketX Mobile Client Library for Android and requires licensed access to a SocketX server instance to receive the secure transmission. [Info](https://eclypses.com/mte-technology/amazon-web-services-aws/)

**Purpose of SocketX:**
- Securely relay socket packets to your server.
- Protect sensitive data with MTE encryption.

## Overview
The SocketX Mobile Client acts as a secure wrapper around the standard [OkHttp](https://square.github.io/okhttp/) WebSocket implementation. It establishes a secure, persistent WebSocket connection to your SocketX Server, automatically handling the MTE handshake and encryption/decryption of data.

From your application's perspective, you interact with it just like a standard OkHttp WebSocket, but the data transmitted over the wire is protected.

## Implementation Checklist
Before integrating the SocketX Mobile Client, ensure your project meets the following requirements:

- [ ] **SocketX Server:** You have the URL of a running SocketX Server instance.
- [ ] **Android Permissions:** Your application has the `android.permission.INTERNET` permission declared in `AndroidManifest.xml`.
- [ ] **OkHttp Client:** Your application uses `OkHttpClient` for network requests.
- [ ] **WebSocketListener:** You are familiar with implementing `okhttp3.WebSocketListener` to handle socket events.

## Installation
Add the dependency to your app-level build file.

**Kotlin DSL (`build.gradle.kts`):**
```kotlin
dependencies {
    implementation("com.eclypses:socketx-client-android:2.0.8")
    // Ensure OkHttp is also available if not already included
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
}
```

**Groovy DSL (`build.gradle`):**
```groovy
dependencies {
    implementation 'com.eclypses:socketx-client-android:2.0.8'
    implementation 'com.squareup.okhttp3:okhttp:4.12.0'
}
```

## Getting Started

The `SocketXClient` class functions as a factory for creating secure WebSockets. It wraps your existing `OkHttpClient` and provides a `newWebSocket` method that mirrors the standard OkHttp API.

### 1. Initialize OkHttpClient
Ensure you have an `OkHttpClient` instance configured. You can share this instance across your app.

### 2. Initialize SocketXClient
Create an instance of `SocketXClient` by passing your `OkHttpClient`. This step initializes the MTE license.

### 3. Create a Request and Listener
Prepare your `Request` (with the server URL) and your `WebSocketListener` (to handle callbacks).

### 4. Connect
Call `socketXClient.newWebSocket(request, listener)`. This initiates the connection and the automatic MTE handshake.

> **Note:** The `onOpen` callback in your listener will effectively be delayed until the secure MTE handshake is successfully completed.

### Kotlin Example

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

class WebSocketManager {
    private val client = OkHttpClient()
    private var webSocket: WebSocket? = null

    fun connect(url: String) {
        try {
            // 1. Create the SocketXClient factory
            val socketX = SocketXClient(client)

            // 2. Define the Request
            val request = Request.Builder()
                .url(url)
                .build()

            // 3. Define the Listener
            val listener = object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    println("Secure connection established and handshake complete!")
                    webSocket.send("Hello, Secure World!")
                }

                override fun onMessage(webSocket: WebSocket, text: String) {
                    println("Received encrypted message (decrypted): $text")
                }

                override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                    println("Received binary message: ${bytes.hex()}")
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    println("Error: ${t.message}")
                    if (t is SocketXError) {
                        // Handle specific SocketX errors (Handshake, MTE, etc.)
                    }
                }
            }

            // 4. Create the secure WebSocket
            webSocket = socketX.newWebSocket(request, listener)

        } catch (e: Exception) {
            // Handle initialization errors (e.g., MTE license issues)
            e.printStackTrace()
        }
    }

    fun sendMessage(text: String) {
        webSocket?.send(text)
    }

    fun sendBinary(data: ByteArray) {
        webSocket?.send(data.toByteString())
    }

    fun close() {
        webSocket?.close(1000, "Goodbye")
    }
}
```

### Java Example

```java
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okhttp3.Response;
import okio.ByteString;
import com.eclypses.socketx_client_android.SocketXClient;
import com.eclypses.socketx_client_android.SocketXError;

public class WebSocketManager {
    private final OkHttpClient client = new OkHttpClient();
    private WebSocket webSocket;

    public void connect(String url) {
        try {
            // 1. Create the SocketXClient factory
            SocketXClient socketX = new SocketXClient(client);

            // 2. Define the Request
            Request request = new Request.Builder()
                .url(url)
                .build();

            // 3. Define the Listener
            WebSocketListener listener = new WebSocketListener() {
                @Override
                public void onOpen(WebSocket webSocket, Response response) {
                    System.out.println("Secure connection established and handshake complete!");
                    webSocket.send("Hello, Secure World!");
                }

                @Override
                public void onMessage(WebSocket webSocket, String text) {
                    System.out.println("Received encrypted message (decrypted): " + text);
                }

                @Override
                public void onMessage(WebSocket webSocket, ByteString bytes) {
                    System.out.println("Received binary message: " + bytes.hex());
                }

                @Override
                public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                    System.out.println("Error: " + t.getMessage());
                    if (t instanceof SocketXError) {
                         // Handle specific SocketX errors
                    }
                }
            };

            // 4. Create the secure WebSocket
            webSocket = socketX.newWebSocket(request, listener);

        } catch (Exception e) {
            // Handle initialization errors (e.g., MTE license issues)
            e.printStackTrace();
        }
    }

    public void sendMessage(String text) {
        if (webSocket != null) {
            webSocket.send(text);
        }
    }

    public void sendBinary(byte[] data) {
        if (webSocket != null) {
            webSocket.send(ByteString.of(data));
        }
    }

    public void close() {
        if (webSocket != null) {
            webSocket.close(1000, "Goodbye");
        }
    }
}
```

## Troubleshooting

### MTE License Errors
If the MTE license check fails, the `SocketXClient` constructor will throw a `SocketXError.InternalError`. Ensure you catch exceptions when initializing the client.

### Connection Issues
Errors occurring during the connection or handshake process are delivered to the `onFailure` method of your `WebSocketListener`. The `Throwable` passed to `onFailure` may be a `SocketXError` containing details about handshake or codec failures.

### Common Errors
- **NetworkError:** Issues with the underlying network connection or socket.
- **HandshakeError:** Failure to complete the MTE handshake with the server.
- **CodecError:** Issues encoding or decoding MTE messages.

## Contact Eclypses

**Email:** [info@eclypses.com](mailto:info@eclypses.com)
**Web:** [www.eclypses.com](https://www.eclypses.com)
**Chat with us:** [Developer Portal](https://developers.eclypses.com/dashboard)

---
**All trademarks of Eclypses Inc.** may not be used without Eclypses Inc.'s prior written consent.
