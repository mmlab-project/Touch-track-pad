package com.glidedeck.infinity.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {
    
    companion object {
        val KEY_CURSOR_SPEED = floatPreferencesKey("cursor_speed")
        val KEY_SCROLL_SPEED = floatPreferencesKey("scroll_speed")
        val KEY_HAPTIC_STRENGTH = intPreferencesKey("haptic_strength") // 0: Off, 1: Weak, 2: Strong
        val KEY_MENU_POSITION_RIGHT = booleanPreferencesKey("menu_position_right")
        val KEY_KEYBOARD_LAYOUT = stringPreferencesKey("keyboard_layout")
        val KEY_SCROLL_REVERSE = booleanPreferencesKey("scroll_reverse")
        val KEY_THEME_MODE = stringPreferencesKey("theme_mode") // "Dark", "Light"
        val KEY_LANGUAGE = stringPreferencesKey("language") // "en", "ja"

        // Settings are stored here, but language is usually system managed or per-app locale.
        // For simplicity, we stick to system default or can added locale override later.
        
        val KEY_LAST_IP = stringPreferencesKey("last_ip")
        val KEY_LAST_PORT = intPreferencesKey("last_port")
        val KEY_LAST_TOKEN = stringPreferencesKey("last_token")
    }

    val cursorSpeed: Flow<Float> = context.dataStore.data.map { preferences ->
        preferences[KEY_CURSOR_SPEED] ?: 1.5f
    }

    val scrollSpeed: Flow<Float> = context.dataStore.data.map { preferences ->
        preferences[KEY_SCROLL_SPEED] ?: 0.25f
    }

    val hapticStrength: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[KEY_HAPTIC_STRENGTH] ?: 2 // Default: Strong
    }
    
    val isMenuRight: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_MENU_POSITION_RIGHT] ?: false // Default: Left
    }

    val scrollReverse: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_SCROLL_REVERSE] ?: false
    }

    val themeMode: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_THEME_MODE] ?: "Dark"
    }

    val language: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_LANGUAGE] ?: "en"
    }

    val lastConnectionInfo: Flow<Triple<String?, Int?, String?>> = context.dataStore.data.map { preferences ->
        Triple(
            preferences[KEY_LAST_IP],
            preferences[KEY_LAST_PORT],
            preferences[KEY_LAST_TOKEN]
        )
    }

    suspend fun setCursorSpeed(speed: Float) {
        context.dataStore.edit { it[KEY_CURSOR_SPEED] = speed }
    }

    suspend fun setScrollSpeed(speed: Float) {
        context.dataStore.edit { it[KEY_SCROLL_SPEED] = speed }
    }

    suspend fun setHapticStrength(strength: Int) {
        context.dataStore.edit { it[KEY_HAPTIC_STRENGTH] = strength }
    }
    
    suspend fun setMenuPositionRight(isRight: Boolean) {
        context.dataStore.edit { it[KEY_MENU_POSITION_RIGHT] = isRight }
    }

    suspend fun setScrollReverse(reverse: Boolean) {
        context.dataStore.edit { it[KEY_SCROLL_REVERSE] = reverse }
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { it[KEY_THEME_MODE] = mode }
    }

    suspend fun setLanguage(lang: String) {
        context.dataStore.edit { it[KEY_LANGUAGE] = lang }
    }

    suspend fun saveConnectionInfo(ip: String, port: Int, token: String) {
        context.dataStore.edit {
            it[KEY_LAST_IP] = ip
            it[KEY_LAST_PORT] = port
            it[KEY_LAST_TOKEN] = token
        }
    }
}

