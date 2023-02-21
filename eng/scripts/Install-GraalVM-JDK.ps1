[CmdletBinding()]
param(
    [Parameter(Mandatory=$true)]
    [string]$GraalVMVersion
)

$os
$extn

if ($IsWindows) {
  $os = "windows"
  $extn = "zip"
} elseif ($IsMacOS) {
  $os = "mac"
  $extn = "tar.gz"
} else {
  $os = "linux"
  $extn = "tar.gz"
}

$graalVmReleaseUrl = "https://github.com/graalvm/graalvm-ce-builds/releases/download/vm-$GraalVMVersion/graalvm-ce-java11-$os-amd64-$GraalVMVersion.$extn"
$graalVmUnzipName = "graalvm-$GraalVMVersion"

Write-Host "Downloading GraalVM JDK to" (Get-Location)

if (!(Test-Path -Path $graalVmUnzipName -PathType container)) {
  $graalVmZipName = $graalVmReleaseUrl.split("/")[-1]
  Write-Host "Downloading install from '$graalVmReleaseUrl' to '$graalVmZipName'."
  Invoke-WebRequest -URI $graalVmReleaseUrl -OutFile $graalVmZipName

  if ($IsWindows) {
    Expand-Archive -Path $graalVmZipName -Destination "graalvm-temp"
    Move-Item -Path (Join-Path -Path "graalvm-temp" -ChildPath (Get-ChildItem "graalvm-temp")[0].Name) -Destination $graalVmUnzipName
  } else {
    New-Item -Path "graalvm-temp" -ItemType "directory"
    tar -xvf $graalVmZipName -C "graalvm-temp"
    Move-Item -Path (Join-Path -Path "graalvm-temp" -ChildPath (Get-ChildItem "graalvm-temp")[0].Name) -Destination $graalVmUnzipName
  }

}

$javaHome = (Convert-Path $graalVmUnzipName)
Write-Host "Current JAVA_HOME: $Env:JAVA_HOME"
Write-Host "GraalVM JDK: $javaHome"
Write-Host "##vso[task.setvariable variable=JAVA_HOME;]$javaHome"
Write-Host "Updated JAVA_HOME: $Env:JAVA_HOME"
