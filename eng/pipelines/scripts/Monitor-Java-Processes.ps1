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

    try {
        $jpsOutput = & $jpsPath -l 2>&1
        Add-Content -Path $outputFile -Value "`n--- Java processes (jps -l) ---"
        Add-Content -Path $outputFile -Value $jpsOutput
    } catch {
        Add-Content -Path $outputFile -Value "Error running jps: $_"
    }

    # Extract PIDs from ps output and take jstack dumps
    # IMPORTANT: Only jstack the Maven launcher process, NOT ForkedBooter processes.
    # jstack uses SIGQUIT (signal 3) as part of the HotSpot attach mechanism on Linux.
    # ForkedBooter processes running native code (Docker/Netty/JNA) can crash with
    # exit code 131 (128+3=SIGQUIT) if the signal arrives during a native call.
    # Note: $PID is a read-only automatic variable in PowerShell, so we use $javaPid instead
    if ($IsLinux -or $IsMacOS) {
        try {
            # Get PID and full command to extract the JDK path
            $javaPidLines = bash -c "ps -eo pid,args | grep '[j]ava' | grep -v grep" 2>&1
            if ($javaPidLines) {
                foreach ($line in ($javaPidLines -split "`n" | Where-Object { $_.Trim() })) {
                    $line = $line.Trim()
                    # Extract PID (first field)
                    if ($line -match '^\s*(\d+)\s+(.*)') {
                        $javaPid = $Matches[1]
                        $cmdLine = $Matches[2]

                        # Skip ForkedBooter processes — only jstack the Maven launcher.
                        # ForkedBooter forks run test code with native libs (Docker, Netty epoll, JNA)
                        # and are vulnerable to SIGQUIT crashes during native calls.
                        if ($cmdLine -match 'ForkedBooter') {
                            Add-Content -Path $outputFile -Value "`n--- Skipping jstack for ForkedBooter PID $javaPid (SIGQUIT unsafe for native code) ---"
                            continue
                        }
                        
                        # Try to find jstack from the same JDK as the running java process
                        $jstackForPid = $null
                        if ($cmdLine -match '(/[^\s]+)/bin/java\b') {
                            $detectedJavaHome = $Matches[1]
                            $candidateJstack = "$detectedJavaHome/bin/jstack"
                            if (Test-Path $candidateJstack) {
                                $jstackForPid = $candidateJstack
                            }
                        }
                        if (-not $jstackForPid) {
                            $jstackForPid = if ($javaHome) { "$javaHome/bin/jstack" } else { "jstack" }
                        }

                        Add-Content -Path $outputFile -Value "`n--- jstack for PID $javaPid (using $jstackForPid) ---"
                        try {
                            $stackTrace = & $jstackForPid $javaPid 2>&1
                            Add-Content -Path $outputFile -Value $stackTrace
                        } catch {
                            Add-Content -Path $outputFile -Value "Failed to get jstack for PID $javaPid : $_"
                        }
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
