package com.glidedeck.infinity.network

import android.util.Log
import kotlinx.coroutines.*
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.Socket
import java.net.SocketTimeoutException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

class NetworkClient {
    companion object {
        private const val TAG = "NetworkClient"
        private const val HEARTBEAT_INTERVAL_MS = 5000L
        private const val HEARTBEAT_TIMEOUT_MS = 3000L
        private const val MAX_MISSED_HEARTBEATS = 3
        private const val RECONNECT_BASE_DELAY_MS = 1000L
        private const val RECONNECT_MAX_DELAY_MS = 30000L
        private const val TCP_READ_TIMEOUT_MS = 10000
    }

    private var tcpSocket: Socket? = null
    private var udpSocket: DatagramSocket? = null
    private var writer: OutputStreamWriter? = null
    private var reader: BufferedReader? = null
    private var serverIp: String = ""
    private var serverPort: Int = 50000
    private var authToken: String = ""
    private var address: InetAddress? = null
    
    private val isConnected = AtomicBoolean(false)
    private val isReconnecting = AtomicBoolean(false)
    private val autoReconnectEnabled = AtomicBoolean(true)
    
    // Heartbeat tracking
    private val lastPongTime = AtomicLong(0L)
    private var heartbeatJob: Job? = null
    private var missedHeartbeats = 0
    
    // UDP Executor for efficient packet sending
    private var udpExecutor: ExecutorService? = null
    
    // Coroutine scope for background tasks
    private val networkScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    var onClipboardReceived: ((String) -> Unit)? = null
    var onConnectionStateChanged: ((Boolean) -> Unit)? = null
    var onReconnectingStateChanged: ((Boolean) -> Unit)? = null
    var onMacrosReceived: ((List<Macro>) -> Unit)? = null

    suspend fun connect(ip: String, port: Int, token: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                disconnectInternal(triggerCallback = false)
                
                serverIp = ip
                serverPort = port
                authToken = token
                address = InetAddress.getByName(ip)

                // TCP Connection with timeout and keep-alive
                tcpSocket = Socket().apply {
                    soTimeout = TCP_READ_TIMEOUT_MS
                    keepAlive = true
                    connect(java.net.InetSocketAddress(ip, port), 10000)
                }
                writer = OutputStreamWriter(tcpSocket!!.getOutputStream(), "UTF-8")
                reader = BufferedReader(InputStreamReader(tcpSocket!!.getInputStream(), "UTF-8"))

                // Auth
                val authJson = JSONObject().apply {
                    put("type", "AUTH")
                    put("token", token)
                    put("version", 1)
                    put("device", android.os.Build.MODEL)
                }
                sendTcpMessage(authJson.toString())
                
                // Read Auth Response
                val responseLine = reader!!.readLine()
                val response = JSONObject(responseLine)
                if (response.optString("type") == "AUTH_RESULT" && response.optBoolean("success")) {
                    // UDP Setup
                    udpSocket = DatagramSocket()
                    udpExecutor = Executors.newSingleThreadExecutor()
                    
                    isConnected.set(true)
                    isReconnecting.set(false)
                    missedHeartbeats = 0
                    lastPongTime.set(System.currentTimeMillis())
                    
                    onConnectionStateChanged?.invoke(true)
                    onReconnectingStateChanged?.invoke(false)
                    
                    // Start Listening Loop
                    startListening()
                    
                    // Start Heartbeat
                    startHeartbeat()
                    
                    Log.i(TAG, "Connected to $ip:$port")
                    true
                } else {
                    Log.w(TAG, "Auth failed")
                    false
                }
            } catch (e: Exception) {
                Log.e(TAG, "Connect error", e)
                false
            }
        }
    }

    private fun startListening() {
        networkScope.launch {
            try {
                while (isConnected.get() && tcpSocket != null && !tcpSocket!!.isClosed) {
                    try {
                        val line = reader?.readLine()
                        if (line == null) {
                            Log.w(TAG, "Connection closed by server")
                            break
                        }
                        
                        val json = JSONObject(line)
                        
                        when (json.optString("type")) {
                            "CLIPBOARD" -> {
                                val text = json.optString("text")
                                if (text.isNotEmpty()) {
                                    onClipboardReceived?.invoke(text)
                                }
                            }
                            "PONG" -> {
                                lastPongTime.set(System.currentTimeMillis())
                                missedHeartbeats = 0
                            }
                            "MACROS" -> {
                                val macrosJson = json.optJSONArray("macros")
                                val macroList = mutableListOf<Macro>()
                                if (macrosJson != null) {
                                    for (i in 0 until macrosJson.length()) {
                                        val m = macrosJson.optJSONObject(i)
                                        val id = m?.optString("id") ?: ""
                                        val name = m?.optString("name") ?: ""
                                        if (id.isNotEmpty()) {
                                            macroList.add(Macro(id, name))
                                        }
                                    }
                                }
                                onMacrosReceived?.invoke(macroList)
                            }
                        }
                    } catch (e: SocketTimeoutException) {
                        // Timeout is expected, continue loop
                        continue
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Listen error", e)
            } finally {
                handleDisconnection()
            }
        }
    }

    private fun startHeartbeat() {
        heartbeatJob?.cancel()
        heartbeatJob = networkScope.launch {
            while (isActive && isConnected.get()) {
                delay(HEARTBEAT_INTERVAL_MS)
                
                if (!isConnected.get()) break
                
                // Send PING
                try {
                    sendTcpMessage(JSONObject().apply {
                        put("type", "PING")
                        put("time", System.currentTimeMillis())
                    }.toString())
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to send PING", e)
                    missedHeartbeats++
                }
                
                // Check for PONG response
                delay(HEARTBEAT_TIMEOUT_MS)
                
                val timeSinceLastPong = System.currentTimeMillis() - lastPongTime.get()
                if (timeSinceLastPong > HEARTBEAT_INTERVAL_MS + HEARTBEAT_TIMEOUT_MS) {
                    missedHeartbeats++
                    Log.w(TAG, "Missed heartbeat: $missedHeartbeats")
                    
                    if (missedHeartbeats >= MAX_MISSED_HEARTBEATS) {
                        Log.e(TAG, "Connection lost: too many missed heartbeats")
                        handleDisconnection()
                        break
                    }
                }
            }
        }
    }

    private fun handleDisconnection() {
        if (!isConnected.getAndSet(false)) return
        
        heartbeatJob?.cancel()
        onConnectionStateChanged?.invoke(false)
        
        // Attempt auto-reconnect if enabled
        if (autoReconnectEnabled.get() && serverIp.isNotEmpty() && authToken.isNotEmpty()) {
            attemptReconnect()
        }
    }

    private fun attemptReconnect() {
        if (isReconnecting.getAndSet(true)) return
        
        onReconnectingStateChanged?.invoke(true)
        
        networkScope.launch {
            var delay = RECONNECT_BASE_DELAY_MS
            var attempts = 0
            
            while (autoReconnectEnabled.get() && !isConnected.get()) {
                attempts++
                Log.i(TAG, "Reconnection attempt $attempts (delay: ${delay}ms)")
                
                delay(delay)
                
                val success = try {
                    connect(serverIp, serverPort, authToken)
                } catch (e: Exception) {
                    Log.e(TAG, "Reconnect attempt failed", e)
                    false
                }
                
                if (success) {
                    Log.i(TAG, "Reconnected successfully after $attempts attempts")
                    break
                }
                
                // Exponential backoff
                delay = (delay * 2).coerceAtMost(RECONNECT_MAX_DELAY_MS)
            }
            
            if (!isConnected.get()) {
                isReconnecting.set(false)
                onReconnectingStateChanged?.invoke(false)
            }
        }
    }

    fun disconnect() {
        autoReconnectEnabled.set(false)
        disconnectInternal(triggerCallback = true)
    }

    private fun disconnectInternal(triggerCallback: Boolean) {
        heartbeatJob?.cancel()
        isConnected.set(false)
        isReconnecting.set(false)
        
        if (triggerCallback) {
            onConnectionStateChanged?.invoke(false)
            onReconnectingStateChanged?.invoke(false)
        }
        
        try {
            udpExecutor?.shutdownNow()
            udpSocket?.close()
            tcpSocket?.close()
        } catch (e: Exception) {
            // ignore
        }
        udpExecutor = null
        udpSocket = null
        tcpSocket = null
        writer = null
        reader = null
    }

    fun sendMouseMove(dx: Int, dy: Int) {
        if (!isConnected.get() || udpSocket == null || address == null) return
        
        val socket = udpSocket ?: return
        val addr = address ?: return
        
        try {
            val message = "$authToken|M|$dx|$dy"
            val data = message.toByteArray()
            val packet = DatagramPacket(data, data.size, addr, serverPort)
            
            udpExecutor?.execute {
                try {
                    socket.send(packet)
                } catch (e: Exception) {
                    // Silently ignore UDP errors
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "UDP Send error", e)
        }
    }

    fun sendScroll(sx: Int, sy: Int) {
        if (!isConnected.get() || udpSocket == null || address == null) return
        
        val socket = udpSocket ?: return
        val addr = address ?: return
        
        try {
            val message = "$authToken|S|$sx|$sy"
            val data = message.toByteArray()
            val packet = DatagramPacket(data, data.size, addr, serverPort)
            
            udpExecutor?.execute {
                try {
                    socket.send(packet)
                } catch (e: Exception) {
                    // Silently ignore UDP errors
                }
            }
        } catch (e: Exception) {
            // ignore
        }
    }

    fun sendClick(button: String) {
        sendTcpAsync(JSONObject().apply {
            put("type", "CLICK")
            put("button", button)
        })
    }

    fun sendMouseDown(button: String) {
        sendTcpAsync(JSONObject().apply {
            put("type", "MOUSE_DOWN")
            put("button", button)
        })
    }

    fun sendMouseUp(button: String) {
        sendTcpAsync(JSONObject().apply {
            put("type", "MOUSE_UP")
            put("button", button)
        })
    }

    fun sendKey(code: String, modifiers: List<String> = emptyList()) {
        sendTcpAsync(JSONObject().apply {
            put("type", "KEY")
            put("code", code)
            if (modifiers.isNotEmpty()) {
                put("modifiers", org.json.JSONArray(modifiers))
            }
        })
    }
    
    fun sendText(text: String) {
        sendTcpAsync(JSONObject().apply {
            put("type", "TEXT")
            put("text", text)
        })
    }

    fun sendClipboard(text: String) {
        sendTcpAsync(JSONObject().apply {
            put("type", "CLIPBOARD")
            put("text", text)
            put("source", "ANDROID")
        })
    }

    fun getMacros() {
        sendTcpAsync(JSONObject().apply {
            put("type", "GET_MACROS")
        })
    }

    fun executeMacro(id: String) {
        sendTcpAsync(JSONObject().apply {
            put("type", "EXEC_MACRO")
            put("id", id)
        })
    }

    private fun sendTcpAsync(json: JSONObject) {
        if (!isConnected.get()) return
        networkScope.launch {
            try {
                sendTcpMessage(json.toString())
            } catch (e: Exception) {
                Log.w(TAG, "TCP send failed", e)
            }
        }
    }

    @Synchronized
    private fun sendTcpMessage(message: String) {
        writer?.write(message + "\n")
        writer?.flush()
    }
    
    fun enableAutoReconnect(enabled: Boolean) {
        autoReconnectEnabled.set(enabled)
    }
    
    fun isConnected(): Boolean = isConnected.get()
    
    fun isReconnecting(): Boolean = isReconnecting.get()
}
