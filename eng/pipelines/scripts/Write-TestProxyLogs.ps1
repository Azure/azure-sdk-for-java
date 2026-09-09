<#
.SYNOPSIS
Writes the contents of any test-proxy log files to the console for easier debugging.

.PARAMETER LogFileDirectory
The directory where the log files are located.
#>
param(
    [Parameter(Mandatory = $true)]
    [string]$LogFileDirectory
)

$files = @(Get-ChildItem -Path $LogFileDirectory -Filter test-proxy.log)
Write-Host "###### Found $($files.Count) test-proxy.log file(s) under '$LogFileDirectory'"
foreach ($file in $files) {
    Write-Host "##[group]$file"
    Get-Content $file
    Write-Host "##[endgroup]"
}

Write-Host "###### Found test-proxy-error.log file(s) under '$LogFileDirectory'"
$files = @(Get-ChildItem -Path $LogFileDirectory -Filter test-proxy-error.log)
foreach ($file in $files) {
    Write-Host "##[group]$file"
    Get-Content $file
    Write-Host "##[endgroup]"
}
