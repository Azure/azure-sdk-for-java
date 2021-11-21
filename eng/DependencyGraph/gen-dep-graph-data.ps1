# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

# This script requires Powershell 6 which defaults LocalMachine to Restricted on Windows client machines.
# From a Powershell 6 prompt run 'Get-ExecutionPolicy -List' and if the LocalMachine is Restricted or Undefined then
# run the following command from an admin Powershell 6 prompt 'Set-ExecutionPolicy -ExecutionPolicy RemoteSigned'. This
# will enable running scripts locally in Powershell 6.

# Use case: Generate the dependency graph data file (data.js) for track 2 (client) libraries.
#
# Output:
# This scipt will create a data.js file which contains the data entries that the dependency graph html
# file needs to correctly graph the interdependency data
# This script can be run locally from the root of the repo. .\eng\DependencyGraph\gen-dep-graph-data.ps1

# Since we're only dealing with client only items with the azure-client-sdk-parent are valid right now.
$ValidParents = ("azure-client-sdk-parent")
# Limit the path to only things under the SDK directory. While this isn't required when running locally
# the aggregate reports pipeline seems to have a number of duplicate pom files in a temp directory
# by the time this script runs.
$Path = Resolve-Path ($PSScriptRoot + "/../../sdk")
# We don't care about parent pom files, only libraries
$ParentPomFilesToIgnore = ("$($Path)\parent\pom.xml", "$($Path)\pom.client.xml")
$OutFile = "data.js"

class Library {
    [string]$id # <groupId>:<artifactId>:<version>
    [string]$name # <groupId>:<artifactId>
    [string]$version
    [string]$type = "internal"
    $DepHash = @{}
    Library(
        [string]$groupId,
        [string]$artifactId,
        [string]$version
    ){
        $this.name = "$($groupId):$($artifactId)"
        $this.version = $version
        $this.id = "$($this.name):$($version)"
    }
    [string]ToString()
    {
        $retString =  "  `"$($this.id)`": {`n"
        $retString += "    `"name`": `"$($this.name)`",`n"
        $retString += "    `"version`": `"$($this.version)`",`n"
        $retString += "    `"type`": `"internal`",`n"
        $retString += "    `"deps`": [`n"
        $first = $true
        foreach($item in $this.DepHash.GetEnumerator() | Sort-Object Name)
        {
            if (!$first)
            {
                $retString += ",`n"
            }
            $first = $false
            $retString += "      { name: `"$($item.Value.name)`", version: `"$($item.Value.version)`" }"
        }
        $retString += "`n"
        $retString += "     ]`n"
        $retString += "  }"
        return $retString
    }
}

# While this is similar to library there are no dependencies and the
# type will be "internalbinary". This entry is applicable to client libraries
# which aren't being built AKA dependencies. Note: If we opt to include
# external_dependencies then the type will be "external".
class Dependency {
    [string]$id # <groupId>:<artifactId>:<version>
    [string]$name # <groupId>:<artifactId>
    [string]$version
    [string]$type = "internalbinary"
    Dependency(
        [string]$groupId,
        [string]$artifactId,
        [string]$version
    ){
        $this.name = "$($groupId):$($artifactId)"
        $this.version = $version
        $this.id = "$($this.name):$($version)"
    }
    [string]ToString()
    {
        $retString =  "  `"$($this.id)`": {`n"
        $retString += "     `"name`": `"$($this.name)`",`n"
        $retString += "     `"version`": `"$($this.version)`",`n"
        $retString += "     `"type`": `"$($this.type)`",`n"
        $retString += "     `"deps`": []`n"
        $retString += "  }"
        return $retString
    }
}

$StartTime = $(get-date)

# Create one dependency hashtable for libraries we build and one for dependencies
$LibraryHash = @{}
$DependencyHash = @{}

# Loop through every client POM file and collect the library/dependency information. Only client
# track pom files are being processed for the inter-dependency graph.
Get-ChildItem -Path $Path -Filter pom*.xml -Recurse -File | ForEach-Object {
    $pomFile = $_.FullName
    $xmlPomFile = $null
    Write-Host "Processing POM file: $($pomFile)"

    if ($ParentPomFilesToIgnore -contains $pomFile)
    {
        return
    }
    if ($pomFile.Split([IO.Path]::DirectorySeparatorChar) -contains "eng")
    {
        return
    }

    $xmlPomFile = New-Object xml
    $xmlPomFile.Load($pomFile)
    if ($ValidParents -notcontains $xmlPomFile.project.parent.artifactId)
    {
        return
    }

    $xmlNsManager = New-Object -TypeName "Xml.XmlNamespaceManager" -ArgumentList $xmlPomFile.NameTable
    $xmlNsManager.AddNamespace("ns", $xmlPomFile.DocumentElement.NamespaceURI)

    # Create the library entry and add it to the hash
    $libKey = "$($xmlPomFile.project.groupId):$($xmlPomFile.project.artifactId):$($xmlPomFile.project.version)"
    [Library]$lib = [Library]::new($xmlPomFile.project.groupId, $xmlPomFile.project.artifactId, $xmlPomFile.project.version)
    Write-Host "adding $($libKey) to LibraryHash"
    $LibraryHash.Add($libKey, $lib)

    # Dependencies can be inserted into the hash for a given library before the library, itself
    # has been processed, if there's a dependency that matches the library key then remove it.
    if ($DependencyHash.ContainsKey($libKey))
    {
        $DependencyHash.Remove($libKey)
    }

    # Loop through all of the dependencies
    $dependencies = $xmlPomFile.SelectSingleNode("/ns:project/ns:dependencies", $xmlNsManager)
    foreach($dependency in $dependencies.ChildNodes)
    {
        # dependency.ToString is necessary to filter out comments which are of a different type.
        # Also, we're producing a graph of inter-dependencies so we only want things that of type
        # com.azure. If there's already a library entry with the same key, do not create a dependency
        # entry with the same key. If the library is also a dependency the
        # page handles that just fine but a dependency entry with the same key will throw off the
        # behavior in the page
        if (($dependency.ToString() -ne "dependency") -or ($dependency.groupId -ne "com.azure"))
        {
            continue
        }
        $depKey = "$($dependency.groupId):$($dependency.artifactId):$($dependency.version)"
        [Dependency]$dep = [Dependency]::new($dependency.groupId,$dependency.artifactId,$dependency.version)
        # don't add to the dependency hash if the library hash already has the key
        if (!$DependencyHash.ContainsKey($depKey) -and !$LibraryHash.ContainsKey($depKey))
        {
            $DependencyHash.Add($depKey, $dep)
        }

        # The Library's dependency hash can already contain a dependency entry. This can happen
        # when the library has a dependency entry on another library and it also has an test-jar
        # dependency.
        if ($lib.DepHash.ContainsKey($depKey))
        {
            continue
        }
        else
        {
            $lib.DepHash.Add($depKey, $dep)
        }
    }
}

# Write to the dependency file
"const data = {" | Set-Content $OutFile
# Loop through each Library and dump out it's information
$first = $true
foreach($item in $LibraryHash.GetEnumerator() | Sort-Object Name)
{
    if (!$first)
    {
        ",`n" | Add-Content $OutFile -NoNewline
    }
    $first = $false
    $item.Value.ToString() | Add-Content $OutFile -NoNewline
}
"`n" | Add-Content $OutFile -NoNewline

# Loop through each dependency
# Note: don't need to reset first, these entries are one long list which
# started with the library entries
foreach($item in $DependencyHash.GetEnumerator() | Sort-Object Name)
{
    if (!$first)
    {
        ",`n" | Add-Content $OutFile -NoNewline
    }
    $first = $false
    $item.Value.ToString() | Add-Content $OutFile -NoNewline
}
"`n" | Add-Content $OutFile -NoNewline
"};" | Add-Content $OutFile

$ElapsedTime = $(get-date) - $StartTime
$TotalRunTime = "{0:HH:mm:ss}" -f ([datetime]$ElapsedTime.Ticks)
Write-Host "Total run time=$($TotalRunTime)"
