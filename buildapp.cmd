@echo off
setlocal
cd /d "%~dp0"
echo Building Toby with Java 17. Android SDK 35 must be installed.
call gradlew.bat --no-daemon testDebugUnitTest lintDebug assembleDebug
if errorlevel 1 goto failed
echo.
echo APK ready: %~dp0app\build\outputs\apk\debug\app-debug.apk
start "" "%~dp0app\build\outputs\apk\debug"
pause
exit /b 0
:failed
echo.
echo Build failed. Check the error above and README setup instructions.
pause
exit /b 1
