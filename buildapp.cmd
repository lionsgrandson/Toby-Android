@echo off
setlocal
cd /d "%~dp0"
powershell.exe -NoLogo -NoProfile -ExecutionPolicy Bypass -File "%~dp0buildapp.ps1"
set "tobyBuildResult=%ERRORLEVEL%"
echo.
pause
exit /b %tobyBuildResult%
