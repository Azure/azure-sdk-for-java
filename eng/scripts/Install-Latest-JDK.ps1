$jdkUri = "https://github.com/adoptium/temurin19-binaries/releases/download/jdk-19%2B36/OpenJDK19U-jdk_x64_linux_hotspot_19_36.tar.gz"
$jdkZipName = "OpenJDK19U-jdk_x64_linux_hotspot_19_36.tar.gz"
$jdkUnzipName = "jdk-19+36"

Write-Host "Downloading latest JDK to" (Get-Location)

if (!(Test-Path -Path $jdkUnzipName -PathType container)) {
  Invoke-WebRequest -URI $jdkUri -OutFile $jdkZipName
  tar -xvf $jdkZipName
}

$javaHome = (Convert-Path $jdkUnzipName)
Write-Host "Latest JDK: $javaHome"

Write-Host "Current JAVA_HOME: $Env:JAVA_HOME"
Write-Host "##vso[task.setvariable variable=JAVA_HOME;]$javaHome"
Write-Host "Updated JAVA_HOME: $Env:JAVA_HOME"

Write-Host "##vso[task.setvariable variable=JAVA_HOME_19_X64;]$javaHome"
