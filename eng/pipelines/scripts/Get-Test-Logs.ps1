<#
.SYNOPSIS
Captures any test.log files, JVM crash logs, surefire dumpstream files, and jstack dumps in the build directory
and moves them to a staging directory for artifact publishing.

.DESCRIPTION
This script is used to capture diagnostic files from the build directory and move them to a staging directory for
artifact publishing. It also sets a pipeline variable to indicate whether any diagnostic files were found.
Collected files include:
  - *test.log (test logs)
  - hs_err_pid*.log (JVM crash reports)
  - *.dumpstream (Surefire forked JVM crash/corruption reports)
  - jstack-dumps.log (periodic jstack thread dumps from the Java process monitor)

.PARAMETER StagingDirectory
The directory where the diagnostic files will be moved to.

.PARAMETER TestLogsArtifactName
The name of the artifact to be created.
#>

param(
    [Parameter(Mandatory = $true)]
    [string]$StagingDirectory,

    [Parameter(Mandatory = $true)]
    [string]$TestLogsArtifactName
)

$testLogs = Get-ChildItem -Path . -Recurse -Filter *test.log -File -Depth 4
$jvmCrashLogs = Get-ChildItem -Path . -Recurse -Filter hs_err_pid*.log -File -Depth 6
$dumpstreamFiles = Get-ChildItem -Path . -Recurse -Filter *.dumpstream -File -Depth 6
$jstackDumps = Get-ChildItem -Path "$StagingDirectory/troubleshooting" -Filter jstack-dumps.log -File -ErrorAction SilentlyContinue

$allFiles = @()
if ($testLogs) { $allFiles += $testLogs }
if ($jvmCrashLogs) { $allFiles += $jvmCrashLogs }
if ($dumpstreamFiles) { $allFiles += $dumpstreamFiles }
if ($jstackDumps) { $allFiles += $jstackDumps }

if ($allFiles.Count -gt 0) {
    if (-not (Test-Path "$StagingDirectory/troubleshooting")) {
        New-Item -ItemType Directory -Path "$StagingDirectory/troubleshooting" | Out-Null
    }
    Write-Host "##vso[task.setvariable variable=HAS_TROUBLESHOOTING]true"
    Write-Host "Found $($testLogs.Count) test log(s), $($jvmCrashLogs.Count) JVM crash log(s), $($dumpstreamFiles.Count) dumpstream file(s), $($jstackDumps.Count) jstack dump(s)"
    Compress-Archive -Path $allFiles -DestinationPath "$StagingDirectory/troubleshooting/$TestLogsArtifactName.zip"
}
