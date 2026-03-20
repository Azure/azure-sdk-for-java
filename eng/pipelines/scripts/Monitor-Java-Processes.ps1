<#
.SYNOPSIS
Monitors Java processes by taking periodic jstack thread dumps.

.DESCRIPTION
This script runs in the background, periodically capturing thread dumps of all running Java processes.
It uses both 'ps' (to reliably find Java processes on Linux) and 'jstack' (for thread dumps).
It writes the output to a log file in the troubleshooting directory. This is useful for diagnosing CI pipeline
hangs caused by deadlocked or stuck Java processes.

.PARAMETER StagingDirectory
The directory where jstack dump files will be written.

.PARAMETER IntervalSeconds
The interval in seconds between captures. Default is 120 (2 minutes).

.PARAMETER DurationMinutes
The maximum duration in minutes to run the monitor. Default is 55 minutes.
#>

param(
    [Parameter(Mandatory = $true)]
    [string]$StagingDirectory,

    [Parameter(Mandatory = $false)]
    [int]$IntervalSeconds = 120,

    [Parameter(Mandatory = $false)]
    [int]$DurationMinutes = 55
)

$troubleshootingDir = "$StagingDirectory/troubleshooting"
if (-not (Test-Path $troubleshootingDir)) {
    New-Item -ItemType Directory -Path $troubleshootingDir | Out-Null
}

$outputFile = "$troubleshootingDir/jstack-dumps.log"
$endTime = (Get-Date).AddMinutes($DurationMinutes)

Add-Content -Path $outputFile -Value "Monitor started at $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')"
Add-Content -Path $outputFile -Value "JAVA_HOME=$($env:JAVA_HOME)"

while ((Get-Date) -lt $endTime) {
    Start-Sleep -Seconds $IntervalSeconds

    $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    Add-Content -Path $outputFile -Value "`n========== Snapshot at $timestamp =========="

    # Use 'ps' to find Java processes (more reliable than jps on CI agents)
    try {
        if ($IsLinux -or $IsMacOS) {
            $psOutput = bash -c "ps aux | grep '[j]ava'" 2>&1
        } else {
            $psOutput = Get-Process -Name java -ErrorAction SilentlyContinue | Format-Table Id, CPU, WorkingSet64, CommandLine -AutoSize | Out-String
        }
        Add-Content -Path $outputFile -Value "`n--- Java processes (ps) ---"
        if ($psOutput) {
            Add-Content -Path $outputFile -Value $psOutput
        } else {
            Add-Content -Path $outputFile -Value "(no Java processes found)"
        }
    } catch {
        Add-Content -Path $outputFile -Value "Error listing processes: $_"
    }

    # Also try jps for comparison
    $javaHome = $env:JAVA_HOME
    $jpsPath = if ($javaHome) { "$javaHome/bin/jps" } else { "jps" }
    $jstackPath = if ($javaHome) { "$javaHome/bin/jstack" } else { "jstack" }

    try {
        $jpsOutput = & $jpsPath -l 2>&1
        Add-Content -Path $outputFile -Value "`n--- Java processes (jps -l) ---"
        Add-Content -Path $outputFile -Value $jpsOutput
    } catch {
        Add-Content -Path $outputFile -Value "Error running jps: $_"
    }

    # Extract PIDs from ps output and take jstack dumps
    if ($IsLinux -or $IsMacOS) {
        try {
            $javaPids = bash -c "ps -eo pid,comm | grep '[j]ava' | awk '{print \$1}'" 2>&1
            if ($javaPids) {
                foreach ($pid in ($javaPids -split "`n" | Where-Object { $_.Trim() })) {
                    $pid = $pid.Trim()
                    Add-Content -Path $outputFile -Value "`n--- jstack for PID $pid ---"
                    try {
                        $stackTrace = & $jstackPath $pid 2>&1
                        Add-Content -Path $outputFile -Value $stackTrace
                    } catch {
                        Add-Content -Path $outputFile -Value "Failed to get jstack for PID $pid : $_"
                    }
                }
            }
        } catch {
            Add-Content -Path $outputFile -Value "Error extracting PIDs: $_"
        }
    }
}

Add-Content -Path $outputFile -Value "`nMonitor finished at $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')"
# Mark that we have troubleshooting artifacts
if (Test-Path $outputFile) {
    Write-Host "##vso[task.setvariable variable=HAS_TROUBLESHOOTING]true"
}
