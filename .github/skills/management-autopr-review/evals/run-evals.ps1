param(
    [ValidateSet("all", "true-negatives")]
    [string] $Suite = "all",

    [string] $VallyRepo = (Join-Path (Split-Path (git rev-parse --show-toplevel) -Parent) "vally"),

    [int] $Workers = 1
)

$ErrorActionPreference = "Stop"
$evalRoot = $PSScriptRoot
$repoRoot = git rev-parse --show-toplevel
$vallyCli = Join-Path $VallyRepo "packages\cli\dist\index.js"

$workflowSource = Join-Path $repoRoot ".github\workflows\management-autopr-review.md"
$workflowLock = Join-Path $repoRoot ".github\workflows\management-autopr-review.lock.yml"
$sourceContent = Get-Content $workflowSource -Raw
$lockContent = Get-Content $workflowLock -Raw
$preActivation = [regex]::Match(
    $lockContent,
    "(?ms)^  pre_activation:\s*$.*?(?=^  [a-zA-Z0-9_-]+:\s*$|\z)"
).Value

if ($sourceContent -notmatch "(?m)^\s+bots:\s*\[azure-sdk-automation\]\s*$") {
    throw "Management AutoPR workflow must allow azure-sdk-automation through pre-activation."
}

if ($preActivation -notmatch '(?m)^\s+GH_AW_ALLOWED_BOTS: "azure-sdk-automation"\s*$') {
    throw "Compiled management AutoPR workflow does not allow azure-sdk-automation. Run gh aw compile management-autopr-review."
}

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
