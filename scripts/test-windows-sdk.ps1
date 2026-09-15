$ErrorActionPreference = 'Stop'
$script = Join-Path (Split-Path -Parent $PSScriptRoot) 'buildapp.ps1'
$temp = Join-Path ([IO.Path]::GetTempPath()) ('toby-sdk-test-' + [guid]::NewGuid())
$oldHome = $env:ANDROID_HOME
$oldRoot = $env:ANDROID_SDK_ROOT
$oldLocal = $env:LOCALAPPDATA
$oldPath = $env:PATH
function Assert([bool]$Condition, [string]$Message) {
    if (-not $Condition) { throw $Message }
}
function Run-Check([string]$Project, [int]$Expected = 0) {
    & "$env:SystemRoot\System32\WindowsPowerShell\v1.0\powershell.exe" -NoProfile -ExecutionPolicy Bypass -File $script -ProjectRoot $Project -CheckOnly -NonInteractive
    Assert ($LASTEXITCODE -eq $Expected) "Unexpected exit code $LASTEXITCODE (expected $Expected)"
}
try {
    New-Item -ItemType Directory -Path $temp | Out-Null
    $env:ANDROID_HOME = ''
    $env:ANDROID_SDK_ROOT = ''
    $env:LOCALAPPDATA = Join-Path $temp 'Local App Data'
    $env:PATH = "$env:SystemRoot\System32"
    $sdk = Join-Path $env:LOCALAPPDATA 'Android\Sdk'
    New-Item -ItemType Directory -Path (Join-Path $sdk 'platforms') -Force | Out-Null
    $project = Join-Path $temp 'Project With Spaces'
    New-Item -ItemType Directory -Path $project | Out-Null
    Run-Check $project
    $properties = Join-Path $project 'local.properties'
    Assert ((Get-Content $properties -Raw).Contains('Local App Data/Android/Sdk')) 'Default SDK detection failed'
    $first = Get-Content $properties -Raw
    Run-Check $project
    Assert ((Get-Content $properties -Raw) -ceq $first) 'Configuration is not idempotent'

    $custom = Join-Path $temp 'Custom SDK'
    New-Item -ItemType Directory -Path (Join-Path $custom 'platforms') -Force | Out-Null
    $escaped = $custom.Replace('\', '\\').Replace(':', '\:')
    [IO.File]::WriteAllText($properties, "# keep this comment`nother.setting=keep`nsdk.dir=$escaped`n")
    $env:ANDROID_HOME = $sdk
    Run-Check $project
    $saved = Get-Content $properties -Raw
    Assert ($saved.Contains('Custom SDK')) 'Valid local.properties should take precedence over environment'
    Assert ($saved.Contains('other.setting=keep') -and $saved.Contains('# keep this comment')) 'Unrelated properties were removed'

    [IO.File]::WriteAllText($properties, 'sdk.dir=C\:/missing/toby-sdk')
    Run-Check $project
    Assert ((Get-Content $properties -Raw).Contains('Local App Data/Android/Sdk')) 'Stale sdk.dir was not repaired'

    Remove-Item -LiteralPath $properties
    $env:ANDROID_HOME = ''
    $env:ANDROID_SDK_ROOT = $custom
    $env:LOCALAPPDATA = Join-Path $temp 'No SDK Here'
    Run-Check $project
    Assert ((Get-Content $properties -Raw).Contains('Custom SDK')) 'ANDROID_SDK_ROOT fallback failed'

    Remove-Item -LiteralPath $properties
    $env:ANDROID_SDK_ROOT = ''
    Run-Check $project 1
    Assert (-not (Test-Path $properties)) 'Missing SDK should not create invalid configuration'
    Write-Host 'All Windows SDK detection checks passed.'
} finally {
    $env:ANDROID_HOME = $oldHome
    $env:ANDROID_SDK_ROOT = $oldRoot
    $env:LOCALAPPDATA = $oldLocal
    $env:PATH = $oldPath
    Remove-Item -LiteralPath $temp -Recurse -Force
}
# The deliberately failing missing-SDK subprocess must not become the test suite exit code.
exit 0
