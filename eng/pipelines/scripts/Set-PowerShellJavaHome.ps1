# Remove preceding "1." in the version specified by $(JavaTestVersion)
# e.g. 1.8 -> 8
# This is required to set JAVA_HOME correctly for the Java version
# Java versions are expected to always start with "1."
$javaTestVersion = "$env:JavaTestVersion".Substring(2)
$javaHomeValue = [System.Environment]::GetEnvironmentVariable("JAVA_HOME_$javaTestVersion`_X64")
Write-Host "Setting PowerShellJavaHome to '$javaHomeValue'"
Write-Host "##vso[task.setvariable variable=PowerShellJavaHome;]$javaHomeValue"
