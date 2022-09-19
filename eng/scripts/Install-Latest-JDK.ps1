param(
    [Parameter(Mandatory=$true)][string]$ToolsDirectory,
    [Parameter(Mandatory=$true)][string]$JdkName,
    [Parameter(Mandatory=$true)][string]$JdkUri,
    [Parameter(Mandatory=$true)][string]$JdkZip
)

$latestJdkPath = "$ToolsDirectory/$JdkZip"
if (!(Test-Path -Path $latestJdkPath -PathType leaf)) {
  $latestJdkUri = "https://github.com/adoptium/temurin18-binaries/releases/download/jdk-18.0.2.1%2B1/" + $JdkZip
  Invoke-WebRequest -URI $JdkUri -OutFile $latestJdkPath
}

tar -xf $latestJdkPath -C '$(Agent.ToolsDirectory)'

$javaHome = "$ToolsDirectory/$JdkName/bin"
$env:JAVA_HOME=$javaHome
$env:JAVA_HOME_18_X64=$javaHome
$env:PATH = $javaHome + ':' + $env:PATH
