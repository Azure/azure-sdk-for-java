<#
.SYNOPSIS
Monitors Java processes by taking periodic jstack thread dumps.

.DESCRIPTION
This script runs in the background, periodically capturing jstack thread dumps of all running Java processes.
It writes the output to a log file in the troubleshooting directory. This is useful for diagnosing CI pipeline
hangs caused by deadlocked or stuck Java processes.

.PARAMETER StagingDirectory
The directory where jstack dump files will be written.

.PARAMETER IntervalSeconds
The interval in seconds between jstack captures. Default is 180 (3 minutes).

.PARAMETER DurationMinutes
The maximum duration in minutes to run the monitor. Default is 55 minutes.
#>

param(
    [Parameter(Mandatory = $true)]
    [string]$StagingDirectory,

    [Parameter(Mandatory = $false)]
    [int]$IntervalSeconds = 180,

    [Parameter(Mandatory = $false)]
    [int]$DurationMinutes = 55
)

$troubleshootingDir = "$StagingDirectory/troubleshooting"
if (-not (Test-Path $troubleshootingDir)) {
    New-Item -ItemType Directory -Path $troubleshootingDir | Out-Null
}

$outputFile = "$troubleshootingDir/jstack-dumps.log"
$endTime = (Get-Date).AddMinutes($DurationMinutes)

Write-Host "Starting Java process monitor. Writing jstack dumps to $outputFile every $IntervalSeconds seconds for up to $DurationMinutes minutes."

while ((Get-Date) -lt $endTime) {
    Start-Sleep -Seconds $IntervalSeconds

    $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    Add-Content -Path $outputFile -Value "`n========== jstack dump at $timestamp =========="

    # List all Java processes
    $javaHome = $env:JAVA_HOME
    $jpsPath = if ($javaHome) { "$javaHome/bin/jps" } else { "jps" }
    $jstackPath = if ($javaHome) { "$javaHome/bin/jstack" } else { "jstack" }

    try {
        $jpsOutput = & $jpsPath -l 2>&1
        Add-Content -Path $outputFile -Value "`n--- Java processes (jps -l) ---"
        Add-Content -Path $outputFile -Value $jpsOutput

        # Get PIDs of Java processes (excluding jps itself)
        $pids = $jpsOutput | ForEach-Object {
            if ($_ -match '^\d+' -and $_ -notmatch 'jps') {
                ($_ -split '\s+')[0]
            }
        } | Where-Object { $_ }

        foreach ($pid in $pids) {
            Add-Content -Path $outputFile -Value "`n--- jstack for PID $pid ---"
            try {
                $stackTrace = & $jstackPath $pid 2>&1
                Add-Content -Path $outputFile -Value $stackTrace
            } catch {
                Add-Content -Path $outputFile -Value "Failed to get jstack for PID $pid : $_"
            }
        }
    } catch {
        Add-Content -Path $outputFile -Value "Error running jps: $_"
    }
}

Write-Host "Java process monitor finished after $DurationMinutes minutes."
# Mark that we have troubleshooting artifacts
if (Test-Path $outputFile) {
    Write-Host "##vso[task.setvariable variable=HAS_TROUBLESHOOTING]true"
}
