$jdkUri = "https://github.com/adoptium/temurin18-binaries/releases/download/jdk-18.0.2.1%2B1/OpenJDK18U-jdk_x64_linux_hotspot_18.0.2.1_1.tar.gz"
$jdkZipName = "OpenJDK18U-jdk_x64_linux_hotspot_18.0.2.1_1.tar.gz"
$jdkUnzipName = "jdk-18.0.2.1+1"

Write-Host "Downloading latest JDK to" (Get-Location)

if (!(Test-Path -Path $jdkZipName -PathType leaf)) {
  Invoke-WebRequest -URI $jdkUri -OutFile $jdkZipName
}

tar -xvf $jdkZipName

$javaHome = (Convert-Path $jdkUnzipName)
Write-Host "Latest JDK: $javaHome"

Write-Host "Current JAVA_HOME: $Env:JAVA_HOME"
$Env:JAVA_HOME = $javaHome
Write-Host "Updated JAVA_HOME: $Env:JAVA_HOME"

$Env:JAVA_HOME_18_X64 = $javaHome
