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
The SocketX Mobile Client establishes a secure, persistent WebSocket connection to your SocketX Server, creating an encrypted tunnel for your application's data.

The data flow for an outgoing message is as follows:

1. The SocketX client in your app takes the data payload (e.g., a JSON object) and encodes it using MTE.
2. It sends the encoded payload over the WebSocket connection to the SocketX Server.
3. The server decodes the payload to retrieve the original data.
4. The server then forwards this original data to the final destination service or API.

Any response from the destination service travels the same path in reverse: the server encodes the response and sends it back through the WebSocket, where the client decodes it before delivering it to your application.

## Adding the SocketX Mobile Client to Your Application
1.  Add the dependency to your app-level `build.gradle.kts` (Kotlin) or `build.gradle` (Groovy) file:

    **Kotlin DSL (`build.gradle.kts`):**
    ```kotlin
    dependencies {
        implementation("com.eclypses:socketx-client-android:1.0.5")
    }
    ```

    **Groovy DSL (`build.gradle`):**
    ```groovy
    dependencies {
        implementation 'com.eclypses:socketx-client-android:1.0.5'
    }
    ```

2. Set up the corresponding SocketX Server to receive requests from your application, where they will be decoded and relayed to the original destination API.

## Table of Contents
- [Getting Started](#getting-started)
- [Contact Eclypses](#contact-eclypses)

## Getting Started
The minimal setup consists of configuring the SocketX Server URL and updating your Android application to use the `SocketXClient`. The `SocketXClient` is a wrapper around `OkHttp`'s `WebSocketListener`, so similar functionality is available.

- Confirm that you have the SocketX Server URL available to instantiate the `SocketXClient`.
- In your application where you make WebSocket calls:
    - Import `com.eclypses.socketx_client_android.SocketXClient`
    - Create a `SocketXClient` instance.

### Kotlin Example
Your class interacting with `SocketXClient` must contain these elements:

```kotlin
import com.eclypses.socketx_client_android.SocketXClient
import com.eclypses.socketx_client_android.SocketXError

class YourClass {

    private var socketXClient: SocketXClient? = null
    var isConnected: Boolean = false

    fun connect(url: String) {
        try {
            socketXClient = SocketXClient(url)
            isConnected = true
        } catch (e: Exception) {
            // Handle error appropriately
            isConnected = false
            return
        }

        socketXClient?.onMessageReceived = { text ->
            // Handle response text as you wish
        }
        socketXClient?.onBinaryReceived = { data ->
            // Handle response binary (ByteArray) as you wish
        }
        socketXClient?.onError = { error ->
            when (error) {
                is SocketXError.CodecError -> {
                    // Handle error appropriately
                }
                is SocketXError.NetworkError -> {
                    // Handle error appropriately
                }
                else -> {
                    // Handle error appropriately
                }
            }
            isConnected = false
        }
        socketXClient?.onConnected = {
            isConnected = true
        }
        socketXClient?.connect()
    }

    // Also available are these standard socket functions ...

    fun sendText(text: String) {
        socketXClient?.send(text)
    }

    fun sendBinary(data: ByteArray) {
        socketXClient?.send(data)
    }

    fun disconnect() {
        socketXClient?.disconnect()
    }
}
```

### Java Example
Your class interacting with `SocketXClient` must contain these elements:

```java
import com.eclypses.socketx_client_android.SocketXClient;
import com.eclypses.socketx_client_android.SocketXError;

public class YourClass {

    private SocketXClient socketXClient;
    private boolean isConnected = false;

    public void connect(String url) {
        try {
            socketXClient = new SocketXClient(url);
            isConnected = true;
        } catch (Exception e) {
            // Handle error appropriately
            isConnected = false;
            return;
        }

        socketXClient.setOnMessageReceived(text -> {
            // Handle response text as you wish
        });

        socketXClient.setOnBinaryReceived(data -> {
            // Handle response binary (byte[]) as you wish
        });

        socketXClient.setOnError(error -> {
            if (error instanceof SocketXError.CodecError) {
                // Handle CodecError
            } else if (error instanceof SocketXError.NetworkError) {
                // Handle NetworkError
            } else {
                // Handle other errors
            }
            isConnected = false;
        });

        socketXClient.setOnConnected(() -> {
            isConnected = true;
        });

        socketXClient.connect();
    }

    // Also available are these standard socket functions ...

    public void sendText(String text) {
        if (socketXClient != null) {
            socketXClient.send(text);
        }
    }

    public void sendBinary(byte[] data) {
        if (socketXClient != null) {
            socketXClient.send(data);
        }
    }

    public void disconnect() {
        if (socketXClient != null) {
            socketXClient.disconnect();
        }
    }
}
```

## Contact Eclypses

**Email:** [info@eclypses.com](mailto:info@eclypses.com)
**Web:** [www.eclypses.com](https://www.eclypses.com)
**Chat with us:** [Developer Portal](https://developers.eclypses.com/dashboard)

---
**All trademarks of Eclypses Inc.** may not be used without Eclypses Inc.'s prior written consent.
