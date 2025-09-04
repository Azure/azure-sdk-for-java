[CmdletBinding()]
param(
    [Parameter(Mandatory=$true)]
    [string]$JdkFeatureVersion
)

# Query Adoptium for the list of installs for the JDK feature version.
$adoptiumApiUrl = "https://api.adoptium.net"
$os

if ($IsWindows) {
  $os = "windows"
} elseif ($IsMacOS) {
  $os = "mac"
} else {
  $os = "linux"
}

$jdkFeatureVersionJavaHome = "JAVA_HOME_" + $JdkFeatureVersion + "_X64"
Write-Host "Checking if $jdkFeatureVersionJavaHome is already set and exists..."
$javaHomeValue = [System.Environment]::GetEnvironmentVariable($jdkFeatureVersionJavaHome)
if ($javaHomeValue) {
    $jdkBinPath = Join-Path -Path $javaHomeValue -ChildPath "bin/java"
    if (Test-Path -Path $jdkBinPath) {
        Write-Host "$jdkFeatureVersionJavaHome is already set to $javaHomeValue"
        exit 0
    }
} else {
    Write-Host "$jdkFeatureVersionJavaHome is not set, proceeding with installation..."
}

$getInstalls = "$adoptiumApiUrl/v3/assets/latest/$JdkFeatureVersion/hotspot?architecture=x64&image_type=jdk&os=$os&vendor=eclipse"
$jdkUnzipName = "jdk-$JdkFeatureVersion"

Write-Host "Downloading latest JDK to" (Get-Location)

if (!(Test-Path -Path $jdkUnzipName -PathType container)) {
  # Query Adoptium for the list of installs for the JDK feature version.
  Write-Host "Invoking web request to '$getInstalls' to find JDK $JdkFeatureVersion installs available on $os."
  $installsAvailable = Invoke-WebRequest -URI $getInstalls | ConvertFrom-Json
  $jdkLink = $installsAvailable.binary.package.link
  $jdkZipName = $jdkLink.split("/")[-1]

  Write-Host "Downloading install from '$jdkLink' to '$jdkZipName'."
  Invoke-WebRequest -URI $jdkLink -OutFile $jdkZipName

  if ($IsWindows) {
    Expand-Archive -Path $jdkZipName -Destination "jdk-temp"
    Move-Item -Path (Join-Path -Path "jdk-temp" -ChildPath (Get-ChildItem "jdk-temp")[0].Name) -Destination $jdkUnzipName
  } else {
    New-Item -Path "jdk-temp" -ItemType "directory"
    tar -xvf $jdkZipName -C "jdk-temp"
    Move-Item -Path (Join-Path -Path "jdk-temp" -ChildPath (Get-ChildItem "jdk-temp")[0].Name) -Destination $jdkUnzipName
  }

}

$javaHome = (Convert-Path $jdkUnzipName)

if ($IsMacOS) {
    # On macOS, the JDK is inside a subdirectory of the unzipped folder.
    $correctJavaHome = Join-Path -Path $javaHome -ChildPath "Contents/Home"
    $javaBinPath = Join-Path -Path $correctJavaHome -ChildPath "bin/java"
    if (Test-Path $javaBinPath) {
        $javaHome = $correctJavaHome
        Write-Host "Updated JAVA_HOME on macOS: $correctJavaHome"
    } else {
        Write-Error "Failed to find Java at: $correctJavaHome"
        exit 1
    }
}

Write-Host "Latest JDK: $javaHome"
Write-Host "Current JAVA_HOME: $Env:JAVA_HOME"
Write-Host "##vso[task.setvariable variable=JAVA_HOME;]$javaHome"
Write-Host "Updated JAVA_HOME to : $javaHome"
Write-Host "##vso[task.setvariable variable=$jdkFeatureVersionJavaHome;]$javaHome"
