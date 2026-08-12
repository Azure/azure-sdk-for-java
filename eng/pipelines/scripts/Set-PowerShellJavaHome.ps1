<#
.SYNOPSIS
Sets JAVA_HOME for PowerShell scripts based on the Java version specified by $(JavaTestVersion).

.PARAMETER JavaTestVersion
The Java version to set JAVA_HOME for, e.g. 1.8, 1.11, etc.
#>
param(
    [Parameter(Mandatory=$true)]
    [string]$JavaTestVersion
)
# Remove preceding "1." in the version specified by $(JavaTestVersion)
# e.g. 1.8 -> 8
# This is required to set JAVA_HOME correctly for the Java version
# Java versions are expected to always start with "1."
$javaHomeValue = [System.Environment]::GetEnvironmentVariable("JAVA_HOME_$($JavaTestVersion.Substring(2))`_X64")
Write-Host "Setting PowerShellJavaHome to '$javaHomeValue'"
Write-Host "##vso[task.setvariable variable=PowerShellJavaHome;]$javaHomeValue"
