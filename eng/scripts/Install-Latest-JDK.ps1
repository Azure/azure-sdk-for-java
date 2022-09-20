param(
    [Parameter(Mandatory=$true)][string]$ToolsDirectory,
    [Parameter(Mandatory=$true)][string]$JdkName,
    [Parameter(Mandatory=$true)][string]$JdkUri,
    [Parameter(Mandatory=$true)][string]$JdkZip
)

$latestJdkPath = Join-Path -Path $ToolsDirectory -ChildPath $JdkZip
if (!(Test-Path -Path $latestJdkPath -PathType leaf)) {
  if (!(Test-Path -Path $ToolsDirectory)) {
    New-Item -Path $ToolsDirectory -ItemType "directory"
  }
  $latestJdkUri = "https://github.com/adoptium/temurin18-binaries/releases/download/jdk-18.0.2.1%2B1/$JdkZip"
  Invoke-WebRequest -URI $JdkUri -OutFile $latestJdkPath
}

$latestJdkUnzipPath = Join-Path -Path $ToolsDirectory -ChildPath $JdkName
tar -xf $latestJdkPath -C $latestJdkUnzipPath

$javaHome = Join-Path -Path $latestJdkUnzipPath -ChildPath "bin"
[System.Environment]::SetEnvironmentVariable("JAVA_HOME", $javaHome, [System.EnvironmentVariableTarget]::Machine)
[System.Environment]::SetEnvironmentVariable("JAVA_HOME_18_X64", $javaHome, [System.EnvironmentVariableTarget]::Machine)
$path = [System.Environment]::GetEnvironmentVariable("PATH", [System.EnvironmentVariableTarget]::Machine)
$path = $javaHome + ":" + $path
[System.Environment]::SetEnvironmentVariable("PATH", $path, [System.EnvironmentVariableTarget]::Machine)
