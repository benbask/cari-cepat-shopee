# Build helper (PowerShell)
# Jalankan setelah JDK 17 + Android SDK terpasang.

$ErrorActionPreference = "Stop"
$env:JAVA_HOME = "$env:LOCALAPPDATA\jdk17"
$env:ANDROID_HOME = "$env:LOCALAPPDATA\Android\Sdk"
$env:ANDROID_SDK_ROOT = $env:ANDROID_HOME
$env:Path = "$env:JAVA_HOME\bin;$env:Path"

Push-Location $PSScriptRoot
try {
    if (Test-Path ".\gradlew.bat") {
        & .\gradlew.bat assembleDebug
    } else {
        & gradle assembleDebug
    }
    Write-Host ""
    Write-Host "APK: app\build\outputs\apk\debug\app-debug.apk"
} finally {
    Pop-Location
}
