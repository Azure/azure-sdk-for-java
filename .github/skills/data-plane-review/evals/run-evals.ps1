param(
    [ValidateSet("all", "findings")]
    [string] $Suite = "findings",

    [int] $Workers = 1,

    [int] $Runs = 1
)

$ErrorActionPreference = "Stop"
$evalRoot = $PSScriptRoot
$timestamp = (Get-Date).ToUniversalTime().ToString("yyyy-MM-ddTHH-mm-ssZ")
$outputDir = Join-Path $evalRoot "results\$timestamp"

Push-Location $evalRoot
try {
    & vally eval --suite $Suite --output-dir $outputDir --workers $Workers --runs $Runs --junit
    if ($LASTEXITCODE -ne 0) {
        exit $LASTEXITCODE
    }
} finally {
    Pop-Location
}
