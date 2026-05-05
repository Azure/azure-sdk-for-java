<#
.SYNOPSIS
    AVAD Soak Test — Local Mode (Windows)

.DESCRIPTION
    Runs ingestor + avad-reader + lv-reader as local JVM processes.
    No AKS/Helm required. For dev-box validation before deploying to AKS.

.EXAMPLE
    .\run-local.ps1 -ConfigFile config.json
    .\run-local.ps1 -ConfigFile config.json -DurationSeconds 1800 -OpsPerSec 200
    $env:COSMOS_KEY = "xxx"; .\run-local.ps1 -ConfigFile config.json
#>

param(
    [Parameter(Mandatory)] [string]$ConfigFile,
    [int]$DurationSeconds = 0,
    [int]$OpsPerSec = 0
)

$ErrorActionPreference = "Stop"
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$ModuleDir = Split-Path -Parent $ScriptDir

# Apply overrides
if ($DurationSeconds -gt 0) { $env:DURATION_SECONDS = $DurationSeconds }
if ($OpsPerSec -gt 0) { $env:OPS_PER_SEC = $OpsPerSec }

# ── Build if needed ───────────────────────────────────────────────────────
$Jar = "$ModuleDir\target\azure-cosmos-benchmark-4.0.1-beta.1.jar"
$CpFile = "$ModuleDir\target\cp.txt"

if (-not (Test-Path $Jar)) {
    Write-Host "=== Building module ==="
    Push-Location $ModuleDir
    mvn package "-DskipTests" "-DskipCheckstyle" "-Dspotbugs.skip=true" "-Drevapi.skip=true" -B -q
    Pop-Location
}

if (-not (Test-Path $CpFile)) {
    Push-Location $ModuleDir
    mvn dependency:build-classpath "-Dmdep.outputFile=target\cp.txt" -B -q
    Pop-Location
}

# Build classpath: logback dir first, exclude log4j-slf4j-impl
$CpRaw = Get-Content $CpFile
$CpFiltered = ($CpRaw -split ';' | Where-Object { $_ -notmatch 'log4j-slf4j-impl' }) -join ';'
$Classpath = "$ScriptDir;$Jar;$CpFiltered"

$JavaCmd = "java"
$MainClass = "com.azure.cosmos.avadtest.Main"

# ── Output directory ──────────────────────────────────────────────────────
$RunId = Get-Date -Format "yyyyMMdd-HHmmss"
$OutputDir = "$ScriptDir\local-run-$RunId"
New-Item -ItemType Directory -Path $OutputDir -Force | Out-Null

function Log($msg) {
    $ts = Get-Date -Format "HH:mm:ss"
    $line = "[$ts] $msg"
    Write-Host $line
    Add-Content "$OutputDir\run.log" $line
}

# ── Launch processes ──────────────────────────────────────────────────────

Log "=== AVAD Local Soak Test ==="
Log "Config: $ConfigFile"
Log "Output: $OutputDir"

$ConfigPath = Resolve-Path $ConfigFile

Log "Starting ingestor (port 8080)..."
$ingestor = Start-Process -FilePath $JavaCmd -ArgumentList @(
    "-cp", $Classpath, $MainClass,
    "--mode", "ingestor", "--config", $ConfigPath, "--health-port", "8080"
) -RedirectStandardOutput "$OutputDir\ingestor.log" `
  -RedirectStandardError "$OutputDir\ingestor-err.log" `
  -PassThru -NoNewWindow
Log "  Ingestor PID: $($ingestor.Id)"

Start-Sleep -Seconds 10

Log "Starting avad-reader (port 8081)..."
$env:CONSUMED_LOG = "$OutputDir\consumed-avad.log"
$avadReader = Start-Process -FilePath $JavaCmd -ArgumentList @(
    "-cp", $Classpath, $MainClass,
    "--mode", "avad-reader", "--config", $ConfigPath, "--health-port", "8081"
) -RedirectStandardOutput "$OutputDir\avad-reader.log" `
  -RedirectStandardError "$OutputDir\avad-reader-err.log" `
  -PassThru -NoNewWindow
Log "  AVAD reader PID: $($avadReader.Id)"

Log "Starting lv-reader (port 8082)..."
$env:CONSUMED_LOG = "$OutputDir\consumed-lv.log"
$lvReader = Start-Process -FilePath $JavaCmd -ArgumentList @(
    "-cp", $Classpath, $MainClass,
    "--mode", "lv-reader", "--config", $ConfigPath, "--health-port", "8082"
) -RedirectStandardOutput "$OutputDir\lv-reader.log" `
  -RedirectStandardError "$OutputDir\lv-reader-err.log" `
  -PassThru -NoNewWindow
Log "  LV reader PID: $($lvReader.Id)"

Log "All 3 processes running"
Log "  Ingestor log: $OutputDir\ingestor.log"
Log "  AVAD log:     $OutputDir\avad-reader.log"
Log "  LV log:       $OutputDir\lv-reader.log"

# ── Monitor loop ──────────────────────────────────────────────────────────

try {
    Log "Monitoring (Ctrl+C to stop)..."
    while ($true) {
        Start-Sleep -Seconds 30

        $dead = @()
        if ($ingestor.HasExited)   { $dead += "ingestor (exit $($ingestor.ExitCode))" }
        if ($avadReader.HasExited) { $dead += "avad-reader (exit $($avadReader.ExitCode))" }
        if ($lvReader.HasExited)   { $dead += "lv-reader (exit $($lvReader.ExitCode))" }

        if ($dead.Count -gt 0) {
            Log "❌ Process(es) exited: $($dead -join ', ')"
            Log "Check logs in $OutputDir"
            break
        }

        # Print last ingestor progress
        $lastLine = Get-Content "$OutputDir\ingestor.log" -Tail 1 -ErrorAction SilentlyContinue
        if ($lastLine -match 'Progress:') { Log "  $lastLine" }
    }
} finally {
    Log "=== Stopping all processes ==="
    if (-not $ingestor.HasExited)   { Stop-Process -Id $ingestor.Id -Force -ErrorAction SilentlyContinue }
    if (-not $avadReader.HasExited) { Stop-Process -Id $avadReader.Id -Force -ErrorAction SilentlyContinue }
    if (-not $lvReader.HasExited)   { Stop-Process -Id $lvReader.Id -Force -ErrorAction SilentlyContinue }
    Log "All processes stopped"
    Log "Logs: $OutputDir"
}
