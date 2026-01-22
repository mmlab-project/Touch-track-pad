package com.glidedeck.infinity.util

data class AppStrings(
    val settingsTitle: String,
    val motionHeader: String,
    val cursorSpeed: String,
    val scrollSpeed: String,
    val scrollDirection: String,
    val scrollStandard: String,
    val scrollReverse: String,
    val scrollExplanationStandard: String,
    val scrollExplanationReverse: String,
    val generalHeader: String,
    val haptic: String,
    val hapticOff: String,
    val hapticWeak: String,
    val hapticStrong: String,
    val menuRight: String,
    val theme: String,
    val themeLight: String,
    val themeDark: String,
    val close: String,
    val language: String
)

object Localization {
    val En = AppStrings(
        settingsTitle = "Settings",
        motionHeader = "Motion",
        cursorSpeed = "Cursor Speed",
        scrollSpeed = "Scroll Speed",
        scrollDirection = "Scroll Direction",
        scrollStandard = "Standard",
        scrollReverse = "Reverse",
        scrollExplanationStandard = "Standard: Move fingers UP to scroll UP content (like wheel)",
        scrollExplanationReverse = "Reverse: Move fingers UP to scroll DOWN content (like touch)",
        generalHeader = "General",
        haptic = "Haptic",
        hapticOff = "Off",
        hapticWeak = "Weak",
        hapticStrong = "Strong",
        menuRight = "Menu on Right",
        theme = "Theme",
        themeLight = "Light",
        themeDark = "Dark",
        close = "Close",
        language = "Language"
    )

    val Ja = AppStrings(
        settingsTitle = "設定",
        motionHeader = "動作",
        cursorSpeed = "カーソル速度",
        scrollSpeed = "スクロール速度",
        scrollDirection = "スクロール方向",
        scrollStandard = "標準",
        scrollReverse = "逆方向",
        scrollExplanationStandard = "標準: 指を上に動かすと上にスクロール (ホイール動作)",
        scrollExplanationReverse = "逆: 指を上に動かすと下にスクロール (タッチ動作)",
        generalHeader = "一般",
        haptic = "触覚フィードバック",
        hapticOff = "オフ",
        hapticWeak = "弱",
        hapticStrong = "強",
        menuRight = "メニューを右に配置",
        theme = "テーマ",
        themeLight = "ライト",
        themeDark = "ダーク",
        close = "閉じる",
        language = "言語"
    )

    fun get(lang: String): AppStrings {
        return if (lang == "ja") Ja else En
    }
}

