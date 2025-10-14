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

The ArtifactStagingDirectory

#>
[CmdletBinding()]
Param (
    [Parameter(Mandatory = $True)]
    [string] $ArtifactStagingDirectory
)

. (Join-Path $PSScriptRoot ".." common scripts common.ps1)

Write-Host "ArtifactStagingDirectory=$ArtifactStagingDirectory"
if (-not (Test-Path -Path $ArtifactStagingDirectory)) {
    LogError "ArtifactStagingDirectory '$ArtifactStagingDirectory' does not exist."
    exit 1
}

$packageInfoDirectory = Join-Path $ArtifactStagingDirectory "PackageInfo"

$foundError = $false
# At this point the packageInfo files should have been already been created.
$packageInfoFiles = Get-ChildItem -Path $packageInfoDirectory -File -Filter "*.json"

foreach($packageInfoFile in $packageInfoFiles) {
    Write-Host "processing $($packageInfoFile.FullName)"
    $packageInfo = ConvertFrom-Json (Get-Content $packageInfoFile -Raw)

    # ArtifactDetails will be null for AdditionalModules
    if ($packageInfo.ArtifactDetails) {
        # If skipPublishDocMs isn't there, then by default docs are being published for that library
        if ($packageInfo.ArtifactDetails.PSobject.Properties.Name -contains "skipPublishDocMs") {
            # If skipPublishDocMs is there and it's true, then skip publishing
            if ($packageInfo.ArtifactDetails.skipPublishDocMs) {
                Write-Host "Skipping DocsMS publishing for $($packageInfo.Name). skipPublishDocMs is set to false."
                continue
            }
        }
    } else {
        Write-Host "Skipping DocsMS publishing for $($packageInfo.Name). ArtifactDetails is null meaning this is an AdditionalModule"
        continue
    }

    $version = $packageInfo.Version
    # If the dev version is set, use that. This will be set for nightly builds
    if ($packageInfo.DevVersion) {
      $version = $packageInfo.DevVersion
    }
    # This is a workaround until https://github.com/Azure/azure-sdk-for-java/issues/42701
    # has been fixed. Before checking the javadoc jar and reporting an error, check the
    # if the library's jar file exists. If the jar doesn't exist then skip this library.
    # From the $packageInfo piece together the path to the javadoc jar file
    # BEGIN-GreedyPackageInfoSkip
    $jarFile = Join-Path $ArtifactStagingDirectory $packageInfo.Group $packageInfo.ArtifactName "$($packageInfo.ArtifactName)-$($version).jar"
    if (!(Test-Path $jarFile -PathType Leaf)) {
        Write-Host "Jar $jarFile doesn't exist, skipping..."
        continue
    }
    # END-GreedyPackageInfoSkip

    $javadocJar = Join-Path $ArtifactStagingDirectory $packageInfo.Group $packageInfo.ArtifactName "$($packageInfo.ArtifactName)-$($version)-javadoc.jar"
    if (!(Test-Path $javadocJar -PathType Leaf)) {
        LogError "Javadoc Jar file, $javadocJar, was not found. Please ensure that a Javadoc jar is being created for the library."
        $foundError = $true
        continue
    }
    $namespaces = Fetch-Namespaces-From-Javadoc $packageInfo.ArtifactName $packageInfo.Group $version $javadocJar
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
        LogError "Unable to determine namespaces for $($packageInfo.Group):$($packageInfo.ArtifactName). Please ensure that skipPublishDocMs isn't incorrectly set to true or that the library isn't producing an empty java doc jar."
        $foundError = $true
    }
}

if ($foundError) {
    exit 1
}
exit 0
