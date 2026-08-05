param(
    [ValidateSet("all", "true-negatives")]
    [string] $Suite = "all",

    [string] $VallyRepo = (Join-Path (Split-Path (git rev-parse --show-toplevel) -Parent) "vally"),

    [int] $Workers = 1
)

$ErrorActionPreference = "Stop"
$evalRoot = $PSScriptRoot
$vallyCli = Join-Path $VallyRepo "packages\cli\dist\index.js"

if (-not (Test-Path $vallyCli)) {
    throw "Vally CLI not found at $vallyCli. Clone microsoft/vally, authenticate npm for its private packages, then run npm install and npm run build."
}

$timestamp = (Get-Date).ToUniversalTime().ToString("yyyy-MM-ddTHH-mm-ssZ")
$outputDir = Join-Path $evalRoot "results\$timestamp"

Push-Location $evalRoot
try {
    & node $vallyCli eval --suite $Suite --output-dir $outputDir --workers $Workers --junit
    if ($LASTEXITCODE -ne 0) {
        exit $LASTEXITCODE
    }
} finally {
    Pop-Location
}

