param(
    [Parameter(Mandatory=$true, Position=0)]
    [System.String] $inputGroupId,
    [Parameter(Mandatory=$true, Position=1)]
    [System.String] $inputArtifactId,
    [Parameter(Mandatory=$true, Position=2)]
    [System.String] $serviceDirectory
)

# Given an input groupId, artifactId and root service directory, scan the service directory for the
# POM file that matches the group/artifact. If the POM file is found, scan for any unreleased_ dependency
# tags otherwise report an error. If there are unreleased dependency tags then report them and return an
# error, otherwise report success and allow the release to continue.

$script:FoundPomFile = $false
$script:FoundError = $false
function Write-Error-With-Color([string]$msg)
{
    Write-Host "$($msg)" -ForegroundColor Red
}

Write-Host "inputGroupId=$($inputGroupId)"
Write-Host "inputArtifactId=$($inputArtifactId)"
Write-Host "serviceDirectory=$($serviceDirectory)"

# Scan each pom file under the service directory until we find the pom file for the input groupId/artifactId. If
# found then scan that pomfile for any unreleased dependency tags.
Get-ChildItem -Path $serviceDirectory -Filter pom*.xml -Recurse -File | ForEach-Object {
    $libraryIsBeta = $false
    $pomFile = $_.FullName
    $xmlPomFile = New-Object xml
    $xmlPomFile.Load($pomFile)
    if (($xmlPomFile.project.groupId -eq $inputGroupId) -and ($xmlPomFile.project.artifactId -eq $inputArtifactId)) {
        $script:FoundPomFile = $true
        Write-Host "Found pom file with matching groupId($($inputGroupId))/artifactId($($inputArtifactId)), pomFile=$($pomFile)"
        $version = $xmlPomFile.project.version
        if ($version -match '.*-beta(\.\d*)?')
        {
            $libraryIsBeta = $true
            Write-Host "Library is releasing as Beta, version=$($version)"
        } else {
            Write-Host "Library is not releasing as Beta, version=$($version)"
        }

        # Verify there are no unreleased dependencies
        foreach($dependencyNode in $xmlPomFile.GetElementsByTagName("dependency"))
        {
            $artifactId = $dependencyNode.artifactId
            $groupId = $dependencyNode.groupId
            $versionNode = $dependencyNode.GetElementsByTagName("version")[0]
            if (!$versionNode)
            {
                $script:FoundError = $true
                Write-Error-With-Color "Error: dependency is missing version element for groupId=$($groupId), artifactId=$($artifactId) should be <version></version> <!-- {x-version-update;$($groupId):$($artifactId);current|dependency|external_dependency<select one>} -->"
                continue
            }
            # if there is no version update tag for the dependency then fail
            if ($versionNode.NextSibling -and $versionNode.NextSibling.NodeType -eq "Comment")
            {
                $versionUpdateTag = $versionNode.NextSibling.Value.Trim()
                if ($versionUpdateTag -match "{x-version-update;unreleased_$($groupId)")
                {
                    # mvn dependency:copy-dependencies is used to copy the dependencies from maven for analysis
                    # as part of the doc build which requires pulling down all dependencies including test
                    # dependencies. Until such a time that a better solution is found, disable release of a
                    # library with unreleased test dependencies. When a better solution is found, just uncomment
                    # the code below.

                    # # before reporting an error, check to see if there's a scope element and
                    # # if the scope is test then don't fail
                    # $scopeNode = $dependencyNode.GetElementsByTagName("scope")[0]
                    # if ($scopeNode -and $scopeNode.InnerText.Trim() -eq "test")
                    # {
                    #    continue
                    # }

                    $script:FoundError = $true
                    Write-Error-With-Color "Error: Cannot release libraries with unreleased dependencies. dependency=$($versionUpdateTag)"
                    continue
                } elseif ($versionUpdateTag -match "{x-version-update;beta_$($groupId)") {
                    # before reporting an error, check to see if there's a scope element and
                    # if the scope is test then don't fail
                    $scopeNode = $dependencyNode.GetElementsByTagName("scope")[0]
                    if ($scopeNode -and $scopeNode.InnerText.Trim() -eq "test")
                    {
                        continue
                    }
                    # if the library being released is beta then a beta_ dependency is fine otherwise
                    # report an error since we can't have a library that isn't beta releasing with a
                    # beta dependency
                    if (!$libraryIsBeta)
                    {
                        $script:FoundError = $true
                        Write-Error-With-Color "Error: Cannot release non-beta libraries with beta_ dependencies. dependency=$($versionUpdateTag)"
                    }
                    continue
                } else {
                    # If this is an external dependency then continue
                    if ($versionUpdateTag -match "external_dependency}") {
                        continue
                    }
                    # If the scope is test then a beta dependency is allowed
                    $scopeNode = $dependencyNode.GetElementsByTagName("scope")[0]
                    if ($scopeNode -and $scopeNode.InnerText.Trim() -eq "test") {
                        continue
                    }
                    # If this isn't an external dependency then ensure that if the dependency
                    # version is beta, that we're releasing a beta, otherwise fail
                    if ($versionNode.InnerText -match '.*-beta(\.\d*)?')
                    {
                        if (!$libraryIsBeta)
                        {
                            $script:FoundError = $true
                            Write-Error-With-Color "Error: Cannot release non-beta libraries with beta dependencies. dependency=$($versionUpdateTag), version=$($versionNode.InnerText.Trim())"
                        }
                    }
                }
            }
            else
            {
                $script:FoundError = $true
                Write-Error-With-Color "Error: Missing dependency version update tag for groupId=$($groupId), artifactId=$($artifactId). The tag should be <!-- {x-version-update;$($groupId):$($artifactId);current|dependency|external_dependency<select one>} -->"
                continue
            }
        }
    } else {
        return
    }
}

if (-Not $script:FoundPomFile) {
    Write-Error-With-Color "Did not find pom file with matching groupId=$($groupId) and artifactId=$($artifactId) under serviceDirectory=$($serviceDirectory)"
    exit(1)
}
if ($script:FoundError) {
    exit(1)
}

Write-Host "$($inputGroupId):$($inputArtifactId) looks good to release" -ForegroundColor Green