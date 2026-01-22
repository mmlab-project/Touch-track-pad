@echo off
echo ===================================================
echo   GlideDeck Helper: Clean Install & Launch
echo ===================================================

echo [1/3] Uninstalling old versions...
adb uninstall com.smartmause.infinity
adb uninstall com.glidedeck.infinity

echo.
echo [2/3] Installing new Release APK...
adb install -r d:\smartmause\android\app\build\outputs\apk\release\app-release.apk

echo.
echo [3/3] Launching App...
adb shell am start -n com.glidedeck.infinity/.MainActivity

echo.
echo Done! If you see a Success message above, the app should be open on your phone.
pause
