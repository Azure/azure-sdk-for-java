# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

# Use case: This script verifies the following:
# 1. There are no duplicate entries in any of the version_*.txt files
# 2. There are no duplicate entries in the external_dependencies.txt file
# 3. POM file verification across the enlistment which includes the following:
#    a. There are no <dependencyManagement> sections
#    a. Every <dependency> and <plugin> has a <groupId>, <artifactId> and <version>
#    b. Every <version> has the appropriate x-version-update tag
#    c. The <version>'s value is the same as the value in the version_*txt file or external_dependency
#
# Output:
# This script will process the entire enlistment. If any errors are encountered, will report them at
# time they are encountered and continue processing.
# Errors for any duplicate entries in the various version files will be reported before any POM files
# are processed.
# Errors for a given pom file will appear after the "processing pomFile=<fullPathAndFileName" line for
# that file and before the processing line for the next file. Missing version x-version-update tag
# errors will cause the output of what the tag should be in the cases where it is known (ie. pom file's
# version or parent's version). In the cases of dependencies it'll output a tag and the appropriate
# type (current, dependency or external_dependency) will have to be selected. For example:
# <!-- {x-version-update;<groupId>:<artifactId>;current|dependency|external_dependency<select one>} -->"
#
# This script can be run locally from the root of an enlisment. .\eng\versioning\pom_file_version_scanner.ps1

# Since we're skipping Management for the moment, only look for files with certain parents. These
# limitations will vanish once Management track is updated.
$ValidParents = ("azure-client-sdk-parent", "azure-data-sdk-parent", "azure-cosmos-parent")
$Path = Resolve-Path ($PSScriptRoot + "/../../")

# Not all POM files have a parent entry
$PomFilesIgnoreParent = ("$($Path)\parent\pom.xml")
$script:FoundError = $false
$StartTime = $(get-date)

class Dependency {
    [string]$id
    [string]$depVer
    [string]$curVer
    Dependency(
        [string]$inputString
    ){
        $split = $inputString.Split(";")
        $this.id = $split[0]
        $this.depVer = $split[1]
        $this.curVer = $split[2]
    }    
}

class ExternalDependency {
    [string]$id
    [string]$ver
    ExternalDependency(
        [string]$inputString
    ){
        $split = $inputString.Split(";")
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
            [Dependency]$dep = [Dependency]::new($line)
            if ($depHash.ContainsKey($dep.id))
            {
                # shouldn't happen but error anyways
                Write-Error "Error: Duplicate dependency encountered. '$($dep.id)' defined in '$($depFile)' already exists in the dependency list which means it is defined in multiple version_*.txt files."
                $script:FoundError = $true
                continue
            }
            $depHash.Add($dep.id, $dep)
        } 
        else 
        {
            [ExternalDependency]$dep = [ExternalDependency]::new($line)
            if ($depHash.ContainsKey($dep.id))
            {
                # shouldn't happen but error anyways
                Write-Error "Error: Duplicate external_dependency encountered. '$($dep.id)' has a duplicate entry defined in '$($depFile)'. Please ensure that all entries are unique."
                $script:FoundError = $true
                continue
            }
            $depHash.Add($dep.id, $dep)
        }
    }
}

function Test-Dependency-Tag-And-Version {
    param(
        [hashtable]$libHash,
        [hashtable]$extDepHash,
        [string]$versionString, 
        [string]$versionUpdateString)

    # instead of creating the key from the groupId/artifactId it's necessary to pull the key
    # from the versionUpdateString in case it ends up being one of the dependency exceptions
    # which will have a <unique identifier>_ prepended to the groupId:artifactId
    $depKey = $versionUpdateString.Split(";")[1]

    if ($versionUpdateString.Contains(";external_dependency}"))
    {
        if (!$extDepHash.ContainsKey($depKey))
        {
            return "Error: external_dependency '$($depKey)' does not exist in the external_dependencies. Please ensure the dependency type is correct or the dependency is appropriately added to the file."
        }
        else
        {
            if ($versionString -ne $extDepHash[$depKey].ver)
            {
                return "Error: $($depKey)'s <version> is $($versionString) but the external_dependency version is listed as $($extDepHash[$depKey].ver)"
            }
        }
    }
    # at this point the dependency type is "current" or "dependency"
    else
    {
        if (!$libHash.ContainsKey($depKey))
        {
            return "Error: library dependency '$($depKey)' does not exist in any of the version_*.txt files. Please ensure the dependency type is correct or the dependency is added to the appropriate file."
        }
        else
        {
            if ($versionUpdateString.Contains(";dependency}"))
            {
                if ($versionString -ne $libHash[$depKey].depVer)
                {
                    return "Error: $($depKey)'s <version> is $($versionString) but the dependency version is listed as $($libHash[$depKey].depVer)"
                }
            }
            elseif ($versionUpdateString.Contains(";current}")) 
            {
                if ($versionString -ne $libHash[$depKey].curVer)
                {
                    return "Error: $($depKey)'s <version> is $($versionString) but the current version is listed as $($libHash[$depKey].curVer)"
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

    if ($PomFilesIgnoreParent -contains $pomFile)
    {
        $xmlPomFile = New-Object xml
        $xmlPomFile.Load($pomFile)

    } else {
        $xmlPomFile = New-Object xml
        $xmlPomFile.Load($pomFile)
        if ($ValidParents -notcontains $xmlPomFile.project.parent.artifactId) 
        {
            # This may look odd but ForEach-Object is a cmdlet which means that "continue"
            # exits the loop altogether and "return" behaves like continue for a particular
            # loop
            return
        }
    }
    Write-Output "processing pomFile=$($pomFile)"
    $dependencyManagement = $xmlPomFile.GetElementsByTagName("dependencyManagement")[0]
    if ($dependencyManagement)
    {
        Write-Error "Error: <dependencyManagement> is not allowed. Every dependency must have its own version and version update tag"
    }

    # Ensure that the project has a version tag with the exception of projects under the eng directory which
    # aren't releasing libraries but still need to have their dependencies checked
    if ($pomFile.Split([IO.Path]::DirectorySeparatorChar) -notcontains "eng") 
    {

        $xmlNsManager = New-Object -TypeName "Xml.XmlNamespaceManager" -ArgumentList $xmlPomFile.NameTable
        $xmlNsManager.AddNamespace("ns", $xmlPomFile.DocumentElement.NamespaceURI)

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
                    Write-Output "Error: project/version update tag should be <!-- {x-version-update;$($groupId):$($artifactId);current} -->"
                }
                else
                {
                    # verify the version tag and version are correct
                    $retVal = Test-Dependency-Tag-And-Version $libHash $extDepHash $versionNode.InnerText $versionNode.NextSibling.Value
                    if ($retVal)
                    {
                        $script:FoundError = $true
                        Write-Output "$($retVal)"
                    }
                }
            }
            else 
            {   
                $script:FoundError = $true
                # <!-- {x-version-update;<groupId>:<artifactId>;current} -->
                # every project string needs to have an update tag and projects version tags are always 'current'
                Write-Output "Error: Missing project/version update tag. The tag should be <!-- {x-version-update;$($groupId):$($artifactId);current} -->"
            }
            
        }
        else 
        {
            # output an error for missing version element
            $script:FoundError = $true
            Write-Output "Error: Could not find project/version node for $($pomFile)"
        }
    }

    if ($xmlPomFile.parent) {
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
                    Write-Output "Error: project/parent/version update tag should be <!-- {x-version-update;$($groupId):$($artifactId);current} -->"
                }
                else
                {
                    # verify the version tag and version are correct
                    $retVal = Test-Dependency-Tag-And-Version $libHash $extDepHash $versionNode.InnerText $versionNode.NextSibling.Value
                    if ($retVal)
                    {
                        $script:FoundError = $true
                        Write-Output "$($retVal)"
                    }
                }
            }
            else 
            {   
                $script:FoundError = $true
                # every project string needs to have an update tag and projects version tags are always 'current'
                Write-Output "Error: Missing project/parent/version update tag. The tag should be <!-- {x-version-update;$($groupId):$($artifactId);current} -->"
            }
        }
        else 
        {
            # output an error for missing version element
            $script:FoundError = $true
            Write-Output "Error: Could not find project/parent/version node for $($pomFile)"
        }    
    }

    # Verify every dependency as a group, artifact and version
    # GetElementsByTagName should get all dependencies including dependencies under plugins
    foreach($dependencyNode in $xmlPomFile.GetElementsByTagName("dependency"))
    {
        $artifactId = $dependencyNode.artifactId
        $groupId = $dependencyNode.groupId
        $versionNode = $dependencyNode.GetElementsByTagName("version")[0]
        if ($versionNode.NextSibling -and $versionNode.NextSibling.NodeType -eq "Comment") 
        {
            # unfortunately because there are POM exceptions we need to wildcard the group which may be 
            # something like <area>_groupId
            if (!$versionNode.NextSibling.Value.Trim() -like ("{x-version-update;*$($groupId):$($artifactId);"))
            {
                $script:FoundError = $true
                Write-Output "Error: dependency version update tag for groupId=$($groupId), artifactId=$($artifactId) should be <!-- {x-version-update;$($groupId):$($artifactId);current|dependency|external_dependency<select one>} -->"
            }
            else
            {
                # verify the version tag and version are correct
                $retVal = Test-Dependency-Tag-And-Version $libHash $extDepHash $versionNode.InnerText $versionNode.NextSibling.Value
                if ($retVal)
                {
                    $script:FoundError = $true
                    Write-Output "$($retVal)"
                }
            }
        }
        else 
        {   
            $script:FoundError = $true
            # <!-- {x-version-update;<groupId>:<artifactId>;current} -->
            Write-Output "Error: Missing dependency version update tag for groupId=$($groupId), artifactId=$($artifactId). The tag should be <!-- {x-version-update;$($groupId):$($artifactId);current|dependency|external_dependency<select one>} -->"
        }
    }
    # Verify every plugin as a group, artifact and version
    # Verify every dependency as a group, artifact and version
    # GetElementsByTagName should get all dependencies including dependencies under plugins
    foreach($pluginNode in $xmlPomFile.GetElementsByTagName("plugin"))
    {
        $artifactId = $pluginNode.artifactId
        $groupId = $pluginNode.groupId
        # plugins will always have an artifact but may not have a groupId
        if (!$groupId)
        {
            Write-Output "Error: plugin $($artifactId) is missing its groupId tag"
        }
        $versionNode = $pluginNode.GetElementsByTagName("version")[0]
        if ($versionNode.NextSibling -and $versionNode.NextSibling.NodeType -eq "Comment") 
        {
            # unfortunately because there are POM exceptions we need to wildcard the group which may be 
            # something like <area>_groupId
            if (!$versionNode.NextSibling.Value.Trim() -like ("{x-version-update;*$($groupId):$($artifactId);"))
            {
                $script:FoundError = $true
                Write-Output "Error: plugin version update tag for groupId=$($groupId), artifactId=$($artifactId) should be <!-- {x-version-update;$($groupId):$($artifactId);current|dependency|external_dependency<select one>} -->"
            }
            else
            {
                # verify the version tag and version are correct
                $retVal = Test-Dependency-Tag-And-Version $libHash $extDepHash $versionNode.InnerText $versionNode.NextSibling.Value
                if ($retVal)
                {
                    $script:FoundError = $true
                    Write-Output "$($retVal)"
                }
            }
        }
        else 
        {   
            $script:FoundError = $true
            # <!-- {x-version-update;<groupId>:<artifactId>;current} -->
            Write-Output "Error: Missing plugin version update tag for groupId=$($groupId), artifactId=$($artifactId). The tag should be <!-- {x-version-update;$($groupId):$($artifactId);current|dependency|external_dependency<select one>} -->"
        }
    }    
}
$ElapsedTime = $(get-date) - $StartTime
$TotalRunTime = "{0:HH:mm:ss}" -f ([datetime]$ElapsedTime.Ticks)
Write-Output "Total run time=$($TotalRunTime)"

if ($script:FoundError)
{
    Write-Output "There were errors encountered during execution. Please fix any errors and run the script again."
    Write-Output "This script can be run locally from the root of an enlisment. .\eng\pom_file_version_scanner.ps1"
    exit(1)
}

