# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

# This script requires Powershell 6 which defaults LocalMachine to Restricted on Windows client machines.
# From a Powershell 6 prompt run 'Get-ExecutionPolicy -List' and if the LocalMachine is Restricted or Undefined then
# run the following command from an admin Powershell 6 prompt 'Set-ExecutionPolicy -ExecutionPolicy RemoteSigned'. This
# will enable running scripts locally in Powershell 6.

# Use case: This script verifies the following:
# 1. There are no duplicate entries in any of the version_*.txt files
# 2. There are no duplicate entries in the external_dependencies.txt file
# 3. POM file verification across the repo which includes the following:
#    a. There are no <dependencyManagement> sections
#    b. Every <dependency> and <plugin> has a <groupId>, <artifactId> and <version>
#    c. Every <version> has the appropriate x-version-update tag
#    d. The <version>'s value is the same as the value in the version_*txt file or external_dependency
#
# Output:
# This script will process the entire repo. If any errors are encountered, it will report them at
# the time they are encountered and continue processing.
# Errors for any duplicate entries in the various version files will be reported before any POM files
# are processed.
# Errors for a given pom file will appear after the "processing pomFile=<fullPathAndFileName>" line for
# that file and before the processing line for the next file. Missing version x-version-update tag
# errors will cause the output of what the tag should be in the cases where it is known (ie. pom file's
# version or parent's version). In the cases of dependencies it'll output a tag and the appropriate
# type (current, dependency or external_dependency) will have to be selected. For example:
# <!-- {x-version-update;<groupId>:<artifactId>;current|dependency|external_dependency<select one>} -->"
#
# This script can be run locally from the root of the repo. .\eng\versioning\pom_file_version_scanner.ps1

# Since we're skipping Management for the moment, only look for files with certain parents. These
# limitations will vanish once Management track is updated.
$ValidParents = ("azure-sdk-parent", "azure-client-sdk-parent", "azure-data-sdk-parent", "azure-cosmos-parent")

# SpringSampleParents is necessary for the spring samples which have to build using the spring-boot-starter-parent BOM.
# The problem with this is, it's a BOM file and the spring dependencies are pulled in through that which means any
# dependencies may or may not have versions. Unfortunately, there are still version tags azure sdk client libraries
# which means these files have to be "sort of" scanned.
$SpringSampleParents = ("spring-boot-starter-parent")

$Path = Resolve-Path ($PSScriptRoot + "/../../")

# Not all POM files have a parent entry
$PomFilesIgnoreParent = ("$($Path)\parent\pom.xml")
$script:FoundError = $false
$DependencyTypeCurrent = "current"
$DependencyTypeDependency = "dependency"
$DependencyTypeExternal = "external_dependency"
$DependencyTypeForError = "$($DependencyTypeCurrent)|$($DependencyTypeDependency)|$($DependencyTypeExternal)"
$UpdateTagFormat = "{x-version-update;<groupId>:<artifactId>;$($DependencyTypeForError)}"
$StartTime = $(get-date)

# This is the for the bannedDependencies include exceptions. All <include> entries need to be of the
# form <include>groupId:artifactId:[version]</include> which locks to a specific version. The exception
# to this is the blanket, wildcard include for com.azure and com.microsoft.azure libraries.
$ComAzureAllowlistIncludes = ("com.azure:*", "com.azure.resourcemanager:*", "com.microsoft.azure:*")

function Write-Error-With-Color([string]$msg)
{
    Write-Host "$($msg)" -ForegroundColor Red
}

# The expected format for a depenency, as found in the eng\versioning\version_*.txt files, is as follows:
# groupId:artifactId;dependency-version;current-version
class Dependency {
    [string]$id
    [string]$depVer
    [string]$curVer
    Dependency(
        [string]$inputString
    ){
        $split = $inputString.Split(";")
        if (($split.Count -ne 3) -and ($split.Count -ne 2))
        {
            # throw and let the caller handle the error since it'll have access to the
            # filename of the file with the malformed line for reporting
            throw
        }
        $this.id = $split[0]
        $this.depVer = $split[1]
        if ($split.Count -eq 3)
        {
            $this.curVer = $split[2]
        }
    }
}

# The expected format for an external depenency, as found in the eng\versioning\external_dependencies.txt file, is as follows:
# groupId:artifactId;dependency-version
class ExternalDependency {
    [string]$id
    [string]$ver
    ExternalDependency(
        [string]$inputString
    ){
        $split = $inputString.Split(";")
        if ($split.Count -ne 2)
        {
            # throw and let the caller handle the error since it'll have access to the
            # filename of the file with the malformed line for reporting
            throw
        }
        $this.id = $split[0]
        $this.ver = $split[1]
    }
}

function Build-Dependency-Hash-From-File {
    param(
        [hashtable]$depHash,
        [string]$depFile,
        [boolean]$extDepHash)
    foreach($line in Get-Content $depFile)
    {
        if (!$line -or $line.Trim() -eq '' -or $line.StartsWith("#"))
        {
            continue
        }
        if (!$extDepHash)
        {
            try {
                [Dependency]$dep = [Dependency]::new($line)
                if ($depHash.ContainsKey($dep.id))
                {
                    Write-Error-With-Color "Error: Duplicate dependency encountered. '$($dep.id)' defined in '$($depFile)' already exists in the dependency list which means it is defined in multiple version_*.txt files."
                    $script:FoundError = $true
                    continue
                }
                $depHash.Add($dep.id, $dep)
            }
            catch {
                Write-Error-With-Color "Invalid dependency line='$($line) in file=$($depFile)"
            }
        }
        else
        {
            try {
                [ExternalDependency]$dep = [ExternalDependency]::new($line)
                if ($depHash.ContainsKey($dep.id))
                {
                    Write-Error-With-Color "Error: Duplicate external_dependency encountered. '$($dep.id)' has a duplicate entry defined in '$($depFile)'. Please ensure that all entries are unique."
                    $script:FoundError = $true
                    continue
                }
                $depHash.Add($dep.id, $dep)
            }
            catch {
                Write-Error-With-Color "Invalid external dependency line='$($line) in file=$($depFile)"
            }
        }
    }
}

function Test-Dependency-Tag-And-Version {
    param(
        [hashtable]$libHash,
        [hashtable]$extDepHash,
        [string]$versionString,
        [string]$versionUpdateString)

    # This is the format of the versionUpdateString and there should be 3 parts:
    # 1. The update tag, itself eg. x-version-update
    # 2. The <groupId>:<artifactId> which is verified using the hash lookup
    # 3. The dependency type which will be current or dependency or external_dependency

    # instead of creating the key from the groupId/artifactId it's necessary to pull the key
    # from the versionUpdateString in case it ends up being one of the dependency exceptions
    # which will have a <unique identifier>_ prepended to the groupId:artifactId
    $split = $versionUpdateString.Trim().Split(";")
    if ($split.Count -ne 3)
    {
        return "Error: malformed dependency update tag='$($versionUpdateString)'. The dependency tag should have the following format: $($UpdateTagFormat)"
    }
    $depKey = $split[1]
    $depType = $split[2]
    # remove the trailing end brace
    if (-not $depType.EndsWith("}"))
    {
        return "Error: malformed dependency update tag='$($versionUpdateString)' is missing the end brace."
    }
    $depType = $depType.Substring(0, $depType.IndexOf("}"))

    if ($depType -eq $DependencyTypeExternal)
    {
        if (!$extDepHash.ContainsKey($depKey))
        {
            return "Error: external_dependency '$($depKey)' does not exist in the external_dependencies. Please ensure the dependency type is correct or the dependency is appropriately added to the file."
        }
        else
        {
            if ($versionString -ne $extDepHash[$depKey].ver)
            {
                return "Error: $($depKey)'s <version> is '$($versionString)' but the external_dependency version is listed as $($extDepHash[$depKey].ver)"
            }
        }
    }
    # at this point the dependency type is "current" or "dependency"
    else
    {
        if (!$libHash.ContainsKey($depKey))
        {
            return "Error: $($depKey)'s dependency type is '$($depType)' but the dependency does not exist in any of the version_*.txt files. Should this be an external_dependency? Please ensure the dependency type is correct or the dependency is added to the appropriate file."
        }
        else
        {
            if ($depType -eq $DependencyTypeDependency)
            {
                if ($versionString -ne $libHash[$depKey].depVer)
                {
                    return "Error: $($depKey)'s <version> is '$($versionString)' but the dependency version is listed as $($libHash[$depKey].depVer)"
                }
            }
            elseif ($depType -eq $DependencyTypeCurrent)
            {
                # Verify that none of the 'current' dependencies are using a groupId that starts with 'unreleased_' or 'beta_'
                if ($depKey.StartsWith('unreleased_') -or $depKey.StartsWith('beta_'))
                {
                    return "Error: $($versionUpdateString) is using an unreleased_ or beta_ dependency and trying to set current value. Only dependency versions can be set with an unreleased or beta dependency."
                }
                if ($versionString -ne $libHash[$depKey].curVer)
                {
                    return "Error: $($depKey)'s <version> is '$($versionString)' but the current version is listed as $($libHash[$depKey].curVer)"
                }
            }
            # At this point the version update string, itself, has an incorrect dependency tag
            else
            {
                return "Error: Invalid dependency type '$($depType)' in version update string $($versionUpdateString). Dependency type must be one of $($DependencyTypeForError)"
            }
        }
    }
}

# There are some configurations, like org.apache.maven.plugins:maven-enforcer-plugin,
# that have plugin and dependency configuration entries that are string patterns. This
# function will be called if the groupId and artifactId for a given plugin or dependency
# are both empty. It'll climb up the parents it finds a configuration entry or there are
# no more parents. If the node is part of a configuration entry then return true, otherwise
# return false.
function Confirm-Node-Is-Part-Of-Configuration {
    param(
        [System.Xml.XmlNode]$theNode
    )
    # Climbing up the parents the nodes will be System.Xml.XmlElement until we're at the very end
    # which will have a type of just 'xml'. If we encounter a configuration node return true
    # otherwise return false.
    while ($theNode.GetType() -ieq [System.Xml.XmlElement]) {
        if ($theNode.Name -ieq 'configuration')
        {
            return $true
        }
        $theNode = $theNode.ParentNode
    }
    return $false
}

# Spring samples will pull in most dependencies through use of the spring bom. Any dependency that is an
# an azure sdk client dependency needs to be verified and must have a groupId, artifactId, version and version tag.
# Similarly, any dependency with a version needs to have a version tag. Dependencies without a version tag are
# ignored as those are assumed to be coming from the BOM.
function Assert-Spring-Sample-Version-Tags {
    param(
        [hashtable]$libHash,
        [hashtable]$extDepHash,
        [xml]$xmlPomFile
    )
    Write-Host "processing Spring Sample pomFile=$($pomFile)"
    $xmlNsManagerSpring = New-Object -TypeName "Xml.XmlNamespaceManager" -ArgumentList $xmlPomFile.NameTable
    $xmlNsManagerSpring.AddNamespace("ns", $xmlPomFile.DocumentElement.NamespaceURI)

    if (-not $xmlPomFile.project.parent.groupId)
    {
        $script:FoundError = $true
        Write-Error-With-Color "Error: parent/groupId is missing."
    }

    $versionNode = $xmlPomFile.SelectSingleNode("/ns:project/ns:parent/ns:version", $xmlNsManagerSpring)
    if (-not $versionNode)
    {
        $script:FoundError = $true
        Write-Error-With-Color "Error: parent/version is missing."
        Write-Error-With-ColorWrite-Error-With-Color "Error: Missing project/version update tag. The tag should be <!-- {x-version-update;$($groupId):$($artifactId);current} -->"
    } else {
        $retVal = Test-Dependency-Tag-And-Version $libHash $extDepHash $versionNode.InnerText.Trim() $versionNode.NextSibling.Value
        if ($retVal)
        {
            $script:FoundError = $true
            Write-Error-With-Color "$($retVal)"
        }
    }

    # Loop through the dependencies. If any dependency is in the libHash (aka, the libraries we build)
    # then it needs to have a version element and update tag.
    foreach($dependencyNode in $xmlPomFile.GetElementsByTagName("dependency"))
    {
        $artifactId = $dependencyNode.artifactId
        $groupId = $dependencyNode.groupId
        # If the artifactId and groupId are both empty then check to see if this
        # is part of a configuration entry. If so then just continue.
        if (!$artifactId -and !$groupId)
        {
            $isPartOfConfig = Confirm-Node-Is-Part-Of-Configuration $dependencyNode
            if (!$isPartOfConfig)
            {
                $script:FoundError = $true
                # Because this particular case is harder to track down, print the OuterXML which is effectively the entire tag
                Write-Error-With-Color "Error: dependency is missing version element and/or artifactId and groupId elements dependencyNode=$($dependencyNode.OuterXml)"
            }
            continue
        }
        $hashKey = "$($groupId):$($artifactId)"
        $versionNode = $dependencyNode.GetElementsByTagName("version")[0]
        # If this is something we build and release, it better have a version and a version tag
        if ($libHash.ContainsKey($hashKey))
        {
            if (-not $versionNode)
            {
                $script:FoundError = $true
                Write-Error-With-Color "Error: dependency is missing version element and tag groupId=$($groupId), artifactId=$($artifactId) should be <version></version> <!-- {x-version-update;$($groupId):$($artifactId);current|dependency|external_dependency<select one>} -->"
            } else {
                # verify the version tag and version are correct
                if ($versionNode.NextSibling -and $versionNode.NextSibling.NodeType -eq "Comment")
                {
                    $retVal = Test-Dependency-Tag-And-Version $libHash $extDepHash $versionNode.InnerText.Trim() $versionNode.NextSibling.Value
                    if ($retVal)
                    {
                        $script:FoundError = $true
                        Write-Error-With-Color "$($retVal)"
                    }
                } else {
                    $script:FoundError = $true
                    Write-Error-With-Color "Error: dependency is missing version tag groupId=$($groupId), artifactId=$($artifactId) tag should be <!-- {x-version-update;$($groupId):$($artifactId);current|dependency|external_dependency<select one>} -->"
                }
            }
        } else {
            # else, if there's a version tag verify it, otherwise just skip it since the version should be coming
            # from the bom
            if ($versionNode)
            {
                if  ($versionNode.NextSibling -and $versionNode.NextSibling.NodeType -eq "Comment")
                {
                    $retVal = Test-Dependency-Tag-And-Version $libHash $extDepHash $versionNode.InnerText.Trim() $versionNode.NextSibling.Value
                    if ($retVal)
                    {
                        $script:FoundError = $true
                        Write-Error-With-Color "$($retVal)"
                    }
                # If there's no version tag then error, if there's a version then it must be tagged
                } else {
                    $script:FoundError = $true
                    Write-Error-With-Color "Error: dependency is missing version tag groupId=$($groupId), artifactId=$($artifactId) tag should be <!-- {x-version-update;$($groupId):$($artifactId);current|dependency|external_dependency<select one>} -->"
                }
            }
        }
    }
}

# Create one dependency hashtable for libraries we build (the groupIds will make the entries unique) and
# one hash for external dependencies
$libHash = @{}
Build-Dependency-Hash-From-File $libHash $Path\eng\versioning\version_client.txt $false
Build-Dependency-Hash-From-File $libHash $Path\eng\versioning\version_data.txt $false

$extDepHash = @{}
Build-Dependency-Hash-From-File $extDepHash $Path\eng\versioning\external_dependencies.txt $true

# Loop through every client and data POM file and perform the verification. Right now
# management isn't being processed, when it is the checks below will go away and every
# POM file under the sdk directory will get processed.
Get-ChildItem -Path $Path -Filter pom*.xml -Recurse -File | ForEach-Object {
    $pomFile = $_.FullName
    $xmlPomFile = $null

    if ($_.FullName -like "*azure-arm-parent*")
    {
        return
    }

    if ($PomFilesIgnoreParent -contains $pomFile)
    {
        $xmlPomFile = New-Object xml
        $xmlPomFile.Load($pomFile)

    } else {
        $xmlPomFile = New-Object xml
        $xmlPomFile.Load($pomFile)
        if ($ValidParents -notcontains $xmlPomFile.project.parent.artifactId)
        {
            if ($SpringSampleParents -contains $xmlPomFile.project.parent.artifactId)
            {
                Assert-Spring-Sample-Version-Tags $libHash $extDepHash $xmlPomFile
            }
            # This may look odd but ForEach-Object is a cmdlet which means that "continue"
            # exits the loop altogether and "return" behaves like continue for a particular
            # loop
            return
        }
    }

    Write-Host "processing pomFile=$($pomFile)"
    if ($xmlPomFile.project.dependencyManagement)
    {
        $script:FoundError = $true
        Write-Error-With-Color "Error: <dependencyManagement> is not allowed. Every dependency must have its own version and version update tag"
    }

    $xmlNsManager = New-Object -TypeName "Xml.XmlNamespaceManager" -ArgumentList $xmlPomFile.NameTable
    $xmlNsManager.AddNamespace("ns", $xmlPomFile.DocumentElement.NamespaceURI)

    # Ensure that the project has a version tag with the exception of projects under the eng directory which
    # aren't releasing libraries but still need to have their dependencies checked
    if ($pomFile.Split([IO.Path]::DirectorySeparatorChar) -notcontains "eng")
    {
        $versionNode = $xmlPomFile.SelectSingleNode("/ns:project/ns:version", $xmlNsManager)
        if ($xmlPomFile.project.version -and $versionNode)
        {
            $artifactId = $xmlPomFile.project.artifactId
            $groupId = $xmlPomFile.project.groupId
            if ($versionNode.NextSibling -and $versionNode.NextSibling.NodeType -eq "Comment")
            {
                # the project's version will always be an update type of "current"
                if ($versionNode.NextSibling.Value.Trim() -ne "{x-version-update;$($groupId):$($artifactId);current}")
                {
                    $script:FoundError = $true
                    # every project string needs to have an update tag and projects version tags are always 'current'
                    Write-Error-With-Color "Error: project/version update tag should be <!-- {x-version-update;$($groupId):$($artifactId);current} -->"
                }
                else
                {
                    # verify the version tag and version are correct
                    $retVal = Test-Dependency-Tag-And-Version $libHash $extDepHash $versionNode.InnerText.Trim() $versionNode.NextSibling.Value
                    if ($retVal)
                    {
                        $script:FoundError = $true
                        Write-Error-With-Color "$($retVal)"
                    }
                }
            }
            else
            {
                $script:FoundError = $true
                # <!-- {x-version-update;<groupId>:<artifactId>;current} -->
                # every project string needs to have an update tag and projects version tags are always 'current'
                Write-Error-With-Color "Error: Missing project/version update tag. The tag should be <!-- {x-version-update;$($groupId):$($artifactId);current} -->"
            }

        }
        else
        {
            # output an error for missing version element
            $script:FoundError = $true
            Write-Error-With-Color "Error: Could not find project/version node for $($pomFile)"
        }
    }

    if ($xmlPomFile.project.parent) {
        # Verify the parent's version
        $versionNode = $xmlPomFile.SelectSingleNode("/ns:project/ns:parent/ns:version", $xmlNsManager)
        if ($xmlPomFile.project.parent.version -and $versionNode)
        {
            $artifactId = $xmlPomFile.project.parent.artifactId
            $groupId = $xmlPomFile.project.parent.groupId
            # versionNode.NextSibling.Value should be the actual XML tag starting with {x-version-update
            if ($versionNode.NextSibling -and $versionNode.NextSibling.NodeType -eq "Comment")
            {
                # parent version
                if ($versionNode.NextSibling.Value.Trim() -ne "{x-version-update;$($groupId):$($artifactId);current}")
                {
                    $script:FoundError = $true
                    Write-Error-With-Color "Error: project/parent/version update tag should be <!-- {x-version-update;$($groupId):$($artifactId);current} -->"
                }
                else
                {
                    # verify the version tag and version are correct
                    $retVal = Test-Dependency-Tag-And-Version $libHash $extDepHash $versionNode.InnerText.Trim() $versionNode.NextSibling.Value
                    if ($retVal)
                    {
                        $script:FoundError = $true
                        Write-Error-With-Color "$($retVal)"
                    }
                }
            }
            else
            {
                $script:FoundError = $true
                # every project string needs to have an update tag and projects version tags are always 'current'
                Write-Error-With-Color "Error: Missing project/parent/version update tag. The tag should be <!-- {x-version-update;$($groupId):$($artifactId);current} -->"
            }
        }
        else
        {
            # output an error for missing version element
            $script:FoundError = $true
            Write-Error-With-Color "Error: Could not find project/parent/version node for $($pomFile)"
        }
    }

    # Verify every dependency as a group, artifact and version
    # GetElementsByTagName should get all dependencies including dependencies under plugins
    foreach($dependencyNode in $xmlPomFile.GetElementsByTagName("dependency"))
    {
        $artifactId = $dependencyNode.artifactId
        $groupId = $dependencyNode.groupId
        # If the artifactId and groupId are both empty then check to see if this
        # is part of a configuration entry. If so then just continue.
        if (!$artifactId -and !$groupId)
        {
            $isPartOfConfig = Confirm-Node-Is-Part-Of-Configuration $dependencyNode
            if (!$isPartOfConfig)
            {
                $script:FoundError = $true
                # Because this particular case is harder to track down, print the OuterXML which is effectively the entire tag
                Write-Error-With-Color "Error: dependency is missing version element and/or artifactId and groupId elements dependencyNode=$($dependencyNode.OuterXml)"
            }
            continue
        }

        $versionNode = $dependencyNode.GetElementsByTagName("version")[0]
        if (!$versionNode)
        {
            $script:FoundError = $true
            Write-Error-With-Color "Error: dependency is missing version element for groupId=$($groupId), artifactId=$($artifactId) should be <version></version> <!-- {x-version-update;$($groupId):$($artifactId);current|dependency|external_dependency<select one>} -->"
            continue
        }
        if ($versionNode.NextSibling -and $versionNode.NextSibling.NodeType -eq "Comment")
        {
            # unfortunately because there are POM exceptions we need to wildcard the group which may be
            # something like <area>_groupId
            if ($versionNode.NextSibling.Value.Trim() -notmatch "{x-version-update;(\w+)?$($groupId):$($artifactId);\w+}")
            {
                $script:FoundError = $true
                Write-Error-With-Color "Error: dependency version update tag for groupId=$($groupId), artifactId=$($artifactId) should be <!-- {x-version-update;$($groupId):$($artifactId);current|dependency|external_dependency<select one>} -->"
            }
            else
            {
                # verify the version tag and version are correct
                $retVal = Test-Dependency-Tag-And-Version $libHash $extDepHash $versionNode.InnerText.Trim() $versionNode.NextSibling.Value
                if ($retVal)
                {
                    $script:FoundError = $true
                    Write-Error-With-Color $retVal
                }
            }
        }
        else
        {
            $script:FoundError = $true
            Write-Error-With-Color "Error: Missing dependency version update tag for groupId=$($groupId), artifactId=$($artifactId). The tag should be <!-- {x-version-update;$($groupId):$($artifactId);current|dependency|external_dependency<select one>} -->"
        }
    }
    # Verify every plugin has a group, artifact and version
    # Verify every dependency has a group, artifact and version
    # GetElementsByTagName should get all dependencies including dependencies under plugins
    foreach($pluginNode in $xmlPomFile.GetElementsByTagName("plugin"))
    {
        $artifactId = $pluginNode.artifactId
        $groupId = $pluginNode.groupId
        # If the artifactId and groupId are both empty then check to see if this
        # is part of a configuration entry.
        if (!$artifactId -and !$groupId)
        {
            $isPartOfConfig = Confirm-Node-Is-Part-Of-Configuration $pluginNode
            if (!$isPartOfConfig)
            {
                $script:FoundError = $true
                # Because this particular case is harder to track down, print the OuterXML which is effectively the entire tag
                Write-Error-With-Color "Error: plugin is missing version element and/or artifactId and groupId elements pluginNode=$($pluginNode.OuterXml)"
            }
            continue
        }
        # plugins should always have an artifact but may not have a groupId
        if (!$groupId)
        {
            $script:FoundError = $true
            Write-Error-With-Color "Error: plugin $($artifactId) is missing its groupId tag"
            continue
        }
        $versionNode = $pluginNode.GetElementsByTagName("version")[0]
        if (!$versionNode)
        {
            $script:FoundError = $true
            Write-Error-With-Color "Error: plugin is missing version element for groupId=$($groupId), artifactId=$($artifactId) should be <version></version> <!-- {x-version-update;$($groupId):$($artifactId);current|dependency|external_dependency<select one>} -->"
            continue
        }
        if ($versionNode.NextSibling -and $versionNode.NextSibling.NodeType -eq "Comment")
        {
            # unfortunately because there are POM exceptions we need to wildcard the group which may be
            # something like <area>_groupId
            if ($versionNode.NextSibling.Value.Trim() -notmatch "{x-version-update;(\w+)?$($groupId):$($artifactId);\w+}")
            {
                $script:FoundError = $true
                Write-Error-With-Color "Error: plugin version update tag for groupId=$($groupId), artifactId=$($artifactId) should be <!-- {x-version-update;$($groupId):$($artifactId);current|dependency|external_dependency<select one>} -->"
            }
            else
            {
                # verify the version tag and version are correct
                $retVal = Test-Dependency-Tag-And-Version $libHash $extDepHash $versionNode.InnerText.Trim() $versionNode.NextSibling.Value
                if ($retVal)
                {
                    $script:FoundError = $true
                    Write-Error-With-Color "$($retVal)"
                }
            }
        }
        else
        {
            $script:FoundError = $true
            Write-Error-With-Color "Error: Missing plugin version update tag for groupId=$($groupId), artifactId=$($artifactId). The tag should be <!-- {x-version-update;$($groupId):$($artifactId);current|dependency|external_dependency<select one>} -->"
        }
    }

    # This is for the allowlist dependencies. Fetch the banned dependencies
    foreach($bannedDependencies in $xmlPomFile.GetElementsByTagName("bannedDependencies"))
    {
        # Include nodes will look like the following:
        # <include>groupId:artifactId:[version]</include> <!-- {x-include-update;groupId:artifactId;external_dependency} -->
        foreach($includeNode in $bannedDependencies.GetElementsByTagName("include"))
        {
            $rawIncludeText = $includeNode.InnerText.Trim()
            $split = $rawIncludeText.Split(":")
            if ($split.Count -eq 3)
            {
                $groupId = $split[0]
                $artifactId = $split[1]
                $version = $split[2]
                # The groupId match has to be able to deal with <area>_ for external dependency exceptions
                if (!$includeNode.NextSibling -or $includeNode.NextSibling.NodeType -ne "Comment")
                {
                    $script:FoundError = $true
                    Write-Error-With-Color "Error: <include> is missing the update tag which should be <!-- {x-include-update;$($groupId):$($artifactId);current|dependency|external_dependency<select one>} -->"
                }
                elseif ($includeNode.NextSibling.Value.Trim() -notmatch "{x-include-update;(\w+)?$($groupId):$($artifactId);(current|dependency|external_dependency)}")
                {
                    $script:FoundError = $true
                    Write-Error-With-Color "Error: <include> version update tag for $($includeNode.InnerText) should be <!-- {x-include-update;$($groupId):$($artifactId);current|dependency|external_dependency<select one>} -->"
                }
                else
                {
                    # verify that the version is formatted correctly
                    if (!$version.StartsWith("[") -or !$version.EndsWith("]"))
                    {
                        $script:FoundError = $true
                        Write-Error-With-Color "Error: the version entry '$($version)' for <include> '$($rawIncludeText)' is not formatted correctly. The include version needs to of the form '[<version>]', the braces lock the include to a specific version for these entries. -->"
                    }
                    # verify the version has the correct value
                    else
                    {
                        $versionWithoutBraces = $version.Substring(1, $version.Length -2)
                        # the key into the dependency has needs to be created from the tag's group/artifact
                        # entries in case it's an external dependency entry. Because this has already
                        # been validated for format, grab the group:artifact
                        $depKey = $includeNode.NextSibling.Value.Trim().Split(";")[1]
                        $depType = $includeNode.NextSibling.Value.Trim().Split(";")[2]
                        $depType = $depType.Substring(0, $depType.IndexOf("}"))
                        if ($depType -eq $DependencyTypeExternal)
                        {
                            if ($extDepHash.ContainsKey($depKey))
                            {
                                if ($versionWithoutBraces -ne $extDepHash[$depKey].ver)
                                {
                                    $script:FoundError = $true
                                    Write-Error-With-Color "Error: $($depKey)'s version is '$($versionWithoutBraces)' but the external_dependency version is listed as $($extDepHash[$depKey].ver)"
                                }
                            }
                            else
                            {
                                $script:FoundError = $true
                                Write-Error-With-Color "Error: the groupId:artifactId entry '$($depKey)' for <include> '$($rawIncludeText)' is not a valid external dependency. Please verify the entry exists in the external_dependencies.txt file. -->"
                            }
                        }
                        else
                        {
                            # If the tag isn't external_dependency then verify it exists in the library hash
                            if (!$libHash.ContainsKey($depKey))
                            {
                                $script:FoundError = $true
                                return "Error: $($depKey)'s dependency type is '$($depType)' but the dependency does not exist in any of the version_*.txt files. Should this be an external_dependency? Please ensure the dependency type is correct or the dependency is added to the appropriate file."

                            }
                            if ($depType -eq $DependencyTypeDependency)
                            {
                                if ($versionWithoutBraces -ne $libHash[$depKey].depVer)
                                {
                                    $script:FoundError = $true
                                    return "Error: $($depKey)'s <version> is '$($versionString)' but the dependency version is listed as $($libHash[$depKey].depVer)"
                                }
                            }
                            elseif ($depType -eq $DependencyTypeCurrent)
                            {
                                # Verify that none of the 'current' dependencies are using a groupId that starts with 'unreleased_' or 'beta_'
                                if ($depKey.StartsWith('unreleased_') -or $depKey.StartsWith('beta_'))
                                {
                                    $script:FoundError = $true
                                    return "Error: $($versionUpdateString) is using an unreleased_ or beta_ dependency and trying to set current value. Only dependency versions can be set with an unreleased or beta dependency."
                                }
                                if ($versionWithoutBraces -ne $libHash[$depKey].curVer)
                                {
                                    $script:FoundError = $true
                                    return "Error: $($depKey)'s <version> is '$($versionString)' but the current version is listed as $($libHash[$depKey].curVer)"
                                }
                            }
                        }
                    }
                }
            }
            # The only time a split count of 2 is allowed is in the following case.
            # <include>com.azure:*</include>
            # These entries will not and should not have an update tag
            elseif ($split.Count -eq 2)
            {
                if ($ComAzureAllowlistIncludes -notcontains $rawIncludeText)
                {
                    $script:FoundError = $true
                    $AllowListIncludeForError = $ComAzureAllowlistIncludes -join " and "
                    Write-Error-With-Color "Error:  $($rawIncludeText) is not a valid <include> entry. With the exception of the $($AllowListIncludeForError), every <include> entry must be of the form <include>groupId:artifactId:[version]<include>"
                }
            }
            else
            {
                # At this point the include entry is wildly incorrect.
                $script:FoundError = $true
                Write-Error-With-Color "Error:  $($rawIncludeText) is not a valid <include> entry. Every <include> entry must be of the form <include>groupId:artifactId:[version]<include>"
            }
        }
    }
}
$ElapsedTime = $(get-date) - $StartTime
$TotalRunTime = "{0:HH:mm:ss}" -f ([datetime]$ElapsedTime.Ticks)
Write-Host "Total run time=$($TotalRunTime)"

if ($script:FoundError)
{
    Write-Error-With-Color "There were errors encountered during execution. Please fix errors and run the script again."
    Write-Error-With-Color "This script can be run locally from the root of the repo. .\eng\versioning\pom_file_version_scanner.ps1"
    exit(1)
}
