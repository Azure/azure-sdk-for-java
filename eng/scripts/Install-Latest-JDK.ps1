$jdkUri = "https://github.com/adoptium/temurin18-binaries/releases/download/jdk-18.0.2.1%2B1/OpenJDK18U-jdk_x64_linux_hotspot_18.0.2.1_1.tar.gz"
$jdkZipName = "OpenJDK18U-jdk_x64_linux_hotspot_18.0.2.1_1.tar.gz"
$jdkUnzipName = "jdk-18.0.2.1+1"

Write-Host "Installing latest JDK to" (Get-Location)

if (!(Test-Path -Path $jdkZipName -PathType leaf)) {
  Invoke-WebRequest -URI $jdkUri -OutFile $jdkZipName
}

tar -xvf $jdkZipName

$javaHome = Join-Path -Path (Convert-Path $jdkUnzipName) -ChildPath "bin"
Write-Host "Latest JDK: $javaHome"

[System.Environment]::SetEnvironmentVariable("JAVA_HOME", $javaHome, [System.EnvironmentVariableTarget]::Machine)
Write-Host "Set JAVA_HOME to" ([System.Environment]::GetEnvironmentVariable("JAVA_HOME", [System.EnvironmentVariableTarget]::Machine))

[System.Environment]::SetEnvironmentVariable("JAVA_HOME_18_X64", $javaHome, [System.EnvironmentVariableTarget]::Machine)
Write-Host "Set JAVA_HOME_18_X64 to" ([System.Environment]::GetEnvironmentVariable("JAVA_HOME_18_X64", [System.EnvironmentVariableTarget]::Machine))

if ($IsWindows) {
  $path = [System.Environment]::GetEnvironmentVariable("Path", [System.EnvironmentVariableTarget]::Machine)
  $path = $javaHome + ";" + $path
  [System.Environment]::SetEnvironmentVariable("Path", $path, [System.EnvironmentVariableTarget]::Machine)
  Write-Host "Set Path to" ([System.Environment]::GetEnvironmentVariable("Path", [System.EnvironmentVariableTarget]::Machine))
} else {
  $path = [System.Environment]::GetEnvironmentVariable("PATH", [System.EnvironmentVariableTarget]::Machine)
  $path = $javaHome + ":" + $path
  [System.Environment]::SetEnvironmentVariable("PATH", $path, [System.EnvironmentVariableTarget]::Machine)
  Write-Host "Set PATH to" ([System.Environment]::GetEnvironmentVariable("PATH", [System.EnvironmentVariableTarget]::Machine))
}
