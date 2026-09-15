param(
    [string]$ProjectRoot = $PSScriptRoot,
    [string]$SdkPath,
    [switch]$CheckOnly,
    [switch]$NonInteractive
)
$ErrorActionPreference = 'Stop'

function Read-PropertyPath([string]$Value) {
    # Decode Java .properties escapes used by Android Studio on Windows.
    [regex]::Replace($Value.Trim(), '\\(u[0-9a-fA-F]{4}|.)', {
        param($Match)
        $escaped = $Match.Groups[1].Value
        if ($escaped -match '^u[0-9a-fA-F]{4}$') {
            return [string][char][Convert]::ToInt32($escaped.Substring(1), 16)
        }
        switch ($escaped) {
            't' { return "`t" }
            'n' { return "`n" }
            'r' { return "`r" }
            'f' { return "`f" }
            default { return $escaped }
        }
    })
}
function Test-SdkRoot([string]$Path) {
    if ([string]::IsNullOrWhiteSpace($Path)) { return $false }
    return (Test-Path -LiteralPath (Join-Path $Path 'platforms') -PathType Container) -or
        (Test-Path -LiteralPath (Join-Path $Path 'platform-tools') -PathType Container) -or
        (Test-Path -LiteralPath (Join-Path $Path 'cmdline-tools') -PathType Container)
}

try {
    $ProjectRoot = (Resolve-Path -LiteralPath $ProjectRoot).Path
    $propertiesPath = Join-Path $ProjectRoot 'local.properties'
    $lines = @()
    if (Test-Path -LiteralPath $propertiesPath) {
        $lines = @(Get-Content -LiteralPath $propertiesPath -Encoding UTF8)
    }
    $configuredSdk = $null
    foreach ($line in $lines) {
        if ($line -match '^\s*sdk\.dir\s*[=:]\s*(.*)$') {
            $configuredSdk = Read-PropertyPath $Matches[1]
        }
    }
    $candidates = @($SdkPath, $configuredSdk, $env:ANDROID_HOME, $env:ANDROID_SDK_ROOT)
    if ($env:LOCALAPPDATA) { $candidates += Join-Path $env:LOCALAPPDATA 'Android\Sdk' }
    $sdkManagerOnPath = Get-Command sdkmanager.bat -ErrorAction SilentlyContinue
    if ($sdkManagerOnPath) {
        $candidate = Split-Path -Parent $sdkManagerOnPath.Source
        for ($i = 0; $i -lt 4 -and $candidate; $i++) {
            $candidates += $candidate
            $candidate = Split-Path -Parent $candidate
        }
    }
    $sdk = $null
    foreach ($candidate in $candidates) {
        if (Test-SdkRoot $candidate) {
            $sdk = (Resolve-Path -LiteralPath $candidate).Path
            break
        }
    }
    if (-not $sdk -and -not $NonInteractive -and -not $CheckOnly) {
        Write-Host 'Android SDK was not found.'
        Write-Host 'In Android Studio, open Settings > Languages & Frameworks > Android SDK.'
        Write-Host 'Copy the Android SDK Location shown there, or install the SDK if none exists.'
        $candidate = (Read-Host 'Paste your SDK folder here, or press Enter to stop').Trim().Trim('"')
        if (Test-SdkRoot $candidate) { $sdk = (Resolve-Path -LiteralPath $candidate).Path }
    }
    if (-not $sdk) {
        throw 'Android SDK not found. Install it through Android Studio > SDK Manager, then rerun buildapp.cmd. The usual folder is %LOCALAPPDATA%\Android\Sdk. A Java installation alone does not include the Android SDK.'
    }
    # Use forward slashes and escape non-ASCII characters for Java Properties.load.
    $propertyValue = $sdk.Replace('\', '/').Replace(':', '\:')
    $propertyValue = [regex]::Replace($propertyValue, '[^\x20-\x7E]', {
        param($Match)
        '\u{0:x4}' -f [int][char]$Match.Value
    })
    $newLines = @($lines | Where-Object { $_ -notmatch '^\s*sdk\.dir\s*[=:]' })
    $newLines += "sdk.dir=$propertyValue"
    $newContents = ($newLines -join "`n") + "`n"
    if (-not (Test-Path -LiteralPath $propertiesPath) -or
        [IO.File]::ReadAllText($propertiesPath) -cne $newContents) {
        [IO.File]::WriteAllText($propertiesPath, $newContents, (New-Object Text.UTF8Encoding($false)))
    }
    $env:ANDROID_HOME = $sdk
    $env:ANDROID_SDK_ROOT = $sdk
    Write-Host "Android SDK: $sdk"
    Write-Host 'SDK location configured in local.properties.'
    if ($CheckOnly) { exit 0 }

    $missing = @()
    if (-not (Test-Path -LiteralPath (Join-Path $sdk 'platforms\android-35\android.jar'))) {
        $missing += 'platforms;android-35'
    }
    if (-not (Test-Path -LiteralPath (Join-Path $sdk 'build-tools\35.0.0\aapt2.exe'))) {
        $missing += 'build-tools;35.0.0'
    }
    if ($missing.Count -gt 0) {
        $manager = Join-Path $sdk 'cmdline-tools\latest\bin\sdkmanager.bat'
        if (-not (Test-Path -LiteralPath $manager)) {
            $manager = Get-ChildItem -LiteralPath (Join-Path $sdk 'cmdline-tools') -Filter sdkmanager.bat -Recurse -ErrorAction SilentlyContinue |
                Select-Object -First 1 -ExpandProperty FullName
        }
        if (-not $manager) {
            throw 'In Android Studio > SDK Manager, install Android SDK Platform 35, Android SDK Build-Tools 35.0.0, and Android SDK Command-line Tools (latest), then rerun buildapp.cmd.'
        }
        Write-Host "Installing required SDK packages: $($missing -join ', ')"
        Write-Host 'Review and accept the SDK license if sdkmanager prompts you.'
        & $manager "--sdk_root=$sdk" @missing
        if ($LASTEXITCODE -ne 0) { throw 'SDK package installation failed. Resolve the sdkmanager error above, or install these packages through Android Studio.' }
        if (-not (Test-Path -LiteralPath (Join-Path $sdk 'platforms\android-35\android.jar')) -or
            -not (Test-Path -LiteralPath (Join-Path $sdk 'build-tools\35.0.0\aapt2.exe'))) {
            throw 'Required SDK packages are still missing. Accept the SDK licenses and finish installing Platform 35 and Build-Tools 35.0.0 in Android Studio.'
        }
    }
    Push-Location -LiteralPath $ProjectRoot
    try {
        Write-Host 'Building Toby: tests, Android lint, and APK...'
        & (Join-Path $ProjectRoot 'gradlew.bat') --no-daemon testDebugUnitTest lintDebug assembleDebug
        if ($LASTEXITCODE -ne 0) { throw 'Gradle build failed. See the error above. Use JDK 17 and Android SDK 35.' }
    } finally { Pop-Location }
    $apkFolder = Join-Path $ProjectRoot 'app\build\outputs\apk\debug'
    Write-Host "APK ready: $apkFolder\app-debug.apk"
    Invoke-Item -LiteralPath $apkFolder
} catch {
    Write-Host "`nBuild failed: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}
