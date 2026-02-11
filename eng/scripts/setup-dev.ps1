# eng/scripts/setup-dev.ps1
# One-time setup script for Azure SDK for Java development

$settingsSource = Join-Path $PSScriptRoot "..\settings.xml"
$m2Dir = if ($env:USERPROFILE) { Join-Path $env:USERPROFILE ".m2" } else { Join-Path $HOME ".m2" }
$settingsTarget = Join-Path $m2Dir "settings.xml"

# Ensure .m2 directory exists
if (-not (Test-Path $m2Dir)) {
    New-Item -ItemType Directory -Path $m2Dir | Out-Null
}

# Copy settings.xml if not present
if (-not (Test-Path $settingsTarget)) {
    Copy-Item $settingsSource $settingsTarget
    Write-Host "✓ Copied settings.xml to $settingsTarget"
} else {
    Write-Host "⚠ settings.xml already exists at $settingsTarget"
    $response = Read-Host "Overwrite? (y/N)"
    if ($response -eq 'y') {
        Copy-Item $settingsSource $settingsTarget -Force
        Write-Host "✓ Overwritten settings.xml"
    }
}

Write-Host ""
Write-Host "Setup complete! You can now run 'mvn' commands."
