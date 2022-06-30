param(
    [Parameter(Mandatory=$true)]
    [System.String] $ServiceDirectory,
    # ArtifactsList will be using ('${{ convertToJson(parameters.Artifacts) }}' | ConvertFrom-Json | Select-Object name, groupId, uberJar)
    [Parameter(Mandatory=$false)]
    [AllowEmptyCollection()]
    [array] $ArtifactsList
)

. "${PSScriptRoot}/../common/scripts/common.ps1"

$resultsTime = [diagnostics.stopwatch]::StartNew()

# Given the service directory and a list of libraries to be released verify that the list
# of libraries has full transitive closure. This means that we have to look at each library's
# pom file's current dependencies and ensure that each dependency is contained within the
# list to be released.
$script:FoundError = $false
$librariesToRelease = @{}
$missingLibraries = @{}

Write-Host "ServiceDirectory=$($ServiceDirectory)"
Write-Host "ArtifactsList:"

if ($ArtifactsList.Count -eq 0) {
    LogError "The artifacts list is empty, meaning there were no artifacts selected to be released."
    exit 1
}

$uberJars = 0
if ($ArtifactsList -and $ArtifactsList.Count -gt 0) {

    $ArtifactsList | Format-Table -Property GroupId, Name, UberJar | Out-String | Write-Host
    foreach($artifact in $ArtifactsList) {
        if ($artifact.uberJar) {
            $uberJars++
        } else {
            $librariesToRelease.Add("$($artifact.groupId):$($artifact.name)", $true)
        }
    }
    # Short circuit if the list passed in contains nothing but uberJars
    if ($uberJars -eq $ArtifactsList.Count)
    {
        Write-Host "The library list to release contains only uber jars. No transitive closure check needed for uber-jars."
        exit 0
    }

    foreach($artifact in $ArtifactsList) {
        if ($artifact.uberJar) {
            continue
        }
        $script:FoundPomFile = $false
        $inputGroupId = $artifact.groupId
        $inputArtifactId = $artifact.name
        # It's unfortunate that we have to find the POM file this way for a given group/artifact.
        # The reason is because an sdk/<area> should have subdirectories named for each artifact but
        # that's not always the case so we need to discover.
        foreach($file in Get-ChildItem -Path $ServiceDirectory -Filter pom*.xml -Recurse -File) {
            $pomFile = $file.FullName
            $xmlPomFile = New-Object xml
            $xmlPomFile.Load($pomFile)
            if (($xmlPomFile.project.groupId -eq $inputGroupId) -and ($xmlPomFile.project.artifactId -eq $inputArtifactId)) {
                $script:FoundPomFile = $true
                # Verify that each current dependency is contained within the list of libraries to be released
                foreach($dependencyNode in $xmlPomFile.GetElementsByTagName("dependency"))
                {
                    $artifactId = $dependencyNode.artifactId
                    $groupId = $dependencyNode.groupId
                    $versionNode = $dependencyNode.GetElementsByTagName("version")[0]

                    $scopeNode = $dependencyNode.GetElementsByTagName("scope")[0]
                    if ($scopeNode -and $scopeNode.InnerText.Trim() -eq "test")
                    {
                        continue
                    }
                    # if there is no version update tag for the dependency then fail
                    if ($versionNode.NextSibling -and $versionNode.NextSibling.NodeType -eq "Comment")
                    {
                        $versionUpdateTag = $versionNode.NextSibling.Value.Trim()
                        if ($versionUpdateTag -match ";current}")
                        {
                            $libraryToVerify = "$($groupId):$($artifactId)"
                            if (!$librariesToRelease.ContainsKey($libraryToVerify)) {
                                if (!$missingLibraries.ContainsKey($libraryToVerify)) {
                                    $missingLibraries[$libraryToVerify] = $true
                                }
                                $script:FoundError = $true
                                LogError "Error: $($pomFile) contains a current dependency, groupId=$($groupId), artifactId=$($artifactId), which is not the list of libraries to be released."
                            }
                        }
                    }
                }
            }
        }

        if (!$script:FoundPomFile) {
            LogError "Did not find pom file with matching groupId=$($inputGroupId) and artifactId=$($inputArtifactId) under ServiceDirectory=$($ServiceDirectory)"
            $script:FoundError = $true
        }
    }

    Write-Host "Elapsed Time=$($resultstime.Elapsed.ToString('dd\.hh\:mm\:ss'))"
    if ($script:FoundError) {
        if ($missingLibraries.Count -gt 0) {
            LogError "The following library or libraries are dependencies of one or more of libaries to be released but not on the release list:"
            foreach ($missingLibrary in $missingLibraries.Keys) {
                LogError $missingLibrary
            }
            LogError "If any of the above libraries are not released from $($ServiceDirectory) then the tag is incorrectly set to current and should be dependency."
        }
        exit(1)
    }
    Write-Host "The library list to release contains full transitive closure and looks good to release"
}
