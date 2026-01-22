package com.glidedeck.infinity

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.glidedeck.infinity.data.SettingsRepository
import com.glidedeck.infinity.network.NetworkClient
import com.glidedeck.infinity.util.HapticManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import com.glidedeck.infinity.network.Macro
import com.glidedeck.infinity.util.Localization
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    
    // Make repository public or expose flow
    val settingsRepo = SettingsRepository(application)
    val networkClient = NetworkClient()
    val hapticManager = HapticManager(application)
    
    private val _isConnected = MutableStateFlow(false)
    val isConnected = _isConnected.asStateFlow()
    
    private val _isReconnecting = MutableStateFlow(false)
    val isReconnecting = _isReconnecting.asStateFlow()
    
    val cursorSpeed = settingsRepo.cursorSpeed
    val scrollSpeed = settingsRepo.scrollSpeed
    
    private val _hapticStrength = MutableStateFlow(2) // 0=Off, 1=Weak, 2=Strong
    val hapticStrength: StateFlow<Int> = _hapticStrength.asStateFlow()
    
    private val _macros = MutableStateFlow<List<Macro>>(emptyList())
    val macros: StateFlow<List<Macro>> = _macros.asStateFlow()

    val isMenuRight = settingsRepo.isMenuRight
    val scrollReverse = settingsRepo.scrollReverse
    val themeMode = settingsRepo.themeMode
    
    val uiStrings = settingsRepo.language.map { Localization.get(it) }
    val language = settingsRepo.language
    
    init {
        networkClient.onConnectionStateChanged = { connected ->
            _isConnected.value = connected
        }
        networkClient.onReconnectingStateChanged = { reconnecting ->
            _isReconnecting.value = reconnecting
        }
        networkClient.onMacrosReceived = { newMacros ->
            _macros.value = newMacros
        }
        
        viewModelScope.launch {
            val (ip, port, token) = settingsRepo.lastConnectionInfo.first()
            if (ip != null && port != null && token != null) {
                try {
                    // networkClient.connect(ip, port, token)
                } catch (e: Exception) {}
            }
        }
        
        viewModelScope.launch {
            settingsRepo.hapticStrength.collect { strength ->
                _hapticStrength.value = strength
                hapticManager.strengthLevel = strength
            }
        }
    }
    
    fun fetchMacros() {
        networkClient.getMacros()
    }
    
    fun executeMacro(id: String) {
        networkClient.executeMacro(id)
        hapticManager.performClick()
    }
    
    fun connect(ip: String, port: Int, token: String) {
        viewModelScope.launch {
            val success = networkClient.connect(ip, port, token)
            if (success) {
                settingsRepo.saveConnectionInfo(ip, port, token)
            }
        }
    }
    
    fun updateHapticStrength(strength: Int) {
        viewModelScope.launch {
            settingsRepo.setHapticStrength(strength)
            // hapticManager.strengthLevel is updated via collection in init
        }
    }
    
    fun updateCursorSpeed(speed: Float) {
        viewModelScope.launch { settingsRepo.setCursorSpeed(speed) }
    }
    
    fun updateScrollSpeed(speed: Float) {
        viewModelScope.launch { settingsRepo.setScrollSpeed(speed) }
    }
    
    fun updateMenuPosition(isRight: Boolean) {
        viewModelScope.launch { settingsRepo.setMenuPositionRight(isRight) }
    }

    fun updateScrollReverse(reverse: Boolean) {
        viewModelScope.launch { settingsRepo.setScrollReverse(reverse) }
    }

    fun updateThemeMode(mode: String) {
        viewModelScope.launch { settingsRepo.setThemeMode(mode) }
    }

    fun updateLanguage(lang: String) {
        viewModelScope.launch { settingsRepo.setLanguage(lang) }
    }
    
    override fun onCleared() {
        super.onCleared()
        networkClient.disconnect()
    }
}

