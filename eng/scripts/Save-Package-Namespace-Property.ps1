<#
.SYNOPSIS
Given an artifact staging directory, loop through all of the PackageInfo files
in the PackageInfo subdirectory and compute the namespaces if they don't already
exist. Yes, they're called packages in Java but namespaces are being used to keep
code that processes them common amongst the languages.

.DESCRIPTION
Given an artifact staging directory, loop through all of the PackageInfo files
in the PackageInfo subdirectory. For each PackageInfo file, find its corresponding
javadoc jar file and use that to compute the namespace information.

.PARAMETER ArtifactStagingDirectory
The root directory of the staged artifacts. The PackageInfo files will be in the
PackageInfo subdirectory. The artifacts root directories are GroupId based, meaning
any artifact with that GroupId will be in a subdirectory. For example: Most Spring
libraries are com.azure.spring and their javadoc jars will be under that subdirectory
but azure-spring-data-cosmos' GroupId is com.azure and its javadoc jar will be under
com.azure.

.PARAMETER ArtifactsList
The list of artifacts to gather namespaces for, this is only done for libraries that are
producing docs.
-ArtifactsList ('$(ArtifactsJson)' | ConvertFrom-Json | Select-Object name, groupId, skipPublishDocMs | Where-Object -Not "skipPublishDocMs")
#>
[CmdletBinding()]
Param (
    [Parameter(Mandatory = $True)]
    [string] $ArtifactStagingDirectory,
    # -ArtifactsList ('$(ArtifactsJson)' | ConvertFrom-Json | Select-Object name, groupId, skipPublishDocMs | Where-Object -Not "skipPublishDocMs")
    [Parameter(Mandatory=$true)]
    [AllowNull()]
    [array] $ArtifactsList
)

. (Join-Path $PSScriptRoot ".." common scripts common.ps1)

if (-not $ArtifactsList) {
    Write-Host "ArtifactsList is empty, nothing to process. This can happen if skipPublishDocMs is set to true for all libraries being built."
    exit 0
}

Write-Host "ArtifactStagingDirectory=$ArtifactStagingDirectory"
if (-not (Test-Path -Path $ArtifactStagingDirectory)) {
    LogError "ArtifactStagingDirectory '$ArtifactStagingDirectory' does not exist."
    exit 1
}

Write-Host ""
Write-Host "ArtifactsList:"
$ArtifactsList | Format-Table -Property GroupId, Name | Out-String | Write-Host

$packageInfoDirectory = Join-Path $ArtifactStagingDirectory "PackageInfo"

$foundError = $false
# At this point the packageInfo files should have been already been created.
# The only thing being done here is adding or updating namespaces for libraries
# that will be producing docs. This ArtifactsList is
foreach($artifact in $ArtifactsList) {
    # Get the version from the packageInfo file
    $packageInfoFile = Join-Path $packageInfoDirectory "$($artifact.Name).json"
    Write-Host "processing $($packageInfoFile.FullName)"
    $packageInfo = ConvertFrom-Json (Get-Content $packageInfoFile -Raw)
    $version = $packageInfo.Version
    # If the dev version is set, use that. This will be set for nightly builds
    if ($packageInfo.DevVersion) {
      $version = $packageInfo.DevVersion
    }
    # From the $packageInfo piece together the path to the javadoc jar file
    $javadocJar = Join-Path $ArtifactStagingDirectory $packageInfo.Group $packageInfo.Name "$($packageInfo.Name)-$($version)-javadoc.jar"
    if (!(Test-Path $javadocJar -PathType Leaf)) {
        LogError "Javadoc Jar file, $javadocJar, was not found. Please ensure that a Javadoc jar is being created for the library."
        $foundError = $true
        continue
    }
    $namespaces = Fetch-Namespaces-From-Javadoc $packageInfo.Name $packageInfo.Group $version $javadocJar
    if ($namespaces.Count -gt 0) {
        Write-Host "Adding/Updating Namespaces property with the following namespaces:"
        $namespaces | Write-Host
        if ($packageInfo.PSobject.Properties.Name -contains "Namespaces") {
            Write-Host "Contains Namespaces property, updating"
            $packageInfo.Namespaces = $namespaces
        }
        else {
            Write-Host "Adding Namespaces property"
            $packageInfo = $packageInfo | Add-Member -MemberType NoteProperty -Name Namespaces -Value $namespaces -PassThru
        }
        $packageInfoJson = ConvertTo-Json -InputObject $packageInfo -Depth 100
        Write-Host "The updated packageInfo for $packageInfoFile is:"
        Write-Host "$packageInfoJson"
        Set-Content `
            -Path $packageInfoFile `
            -Value $packageInfoJson
    } else {
        Write-Host "Fetch-Namespaces-From-Javadoc::returned no namespaces to add"
    }
}

if ($foundError) {
    exit 1
}
exit 0