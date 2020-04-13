[CmdletBinding()]
Param (
    $RepoRoot,
    $DocGenDir
)

Write-Verbose "Name Reccuring paths with variable names"
$DocFxTool = "${RepoRoot}/docfx/docfx.exe"
$DocOutDir = "${RepoRoot}/docfx_project"

Write-Verbose "Initializing Default DocFx Site..."
& "${DocFxTool}" init -q -o "${DocOutDir}"

Write-Verbose "Copying template and configuration..."
New-Item -Path "${DocOutDir}" -Name "templates" -ItemType "directory"
Copy-Item "${DocGenDir}/templates/*" -Destination "${DocOutDir}/templates" -Force -Recurse
Copy-Item "${DocGenDir}/docfx.json" -Destination "${DocOutDir}/" -Force

Write-Verbose "Creating Index using service directory and package names from repo..."
# The service mapper is used to map the directory names to the service names to produce
# a more friendly index. If something isn't in the mapper then the default will just be
# the service name in all caps
$serviceMapHash = Get-Content -Path "${DocGenDir}/service-mapper.json" | ConvertFrom-Json -AsHashtable

# There are some artifact that show up, due to the way we do discovery, that are never shipped.
# Keep a list of those here and anything we don't want to ship can be added to here which will
# cause them to get skipped when generating the DocIndex
$ArtifactsToSkip = (
'azure-cosmos-benchmark',
'azure-sdk-template'
)

# The list of services is being constructed from the directory list under the sdk folder
# which, right now, only contains client/data directories. When management is moved to
# the under sdk it'll automatically get picked up.
$ServiceListData = Get-ChildItem "${RepoRoot}/sdk" -Directory
$YmlPath = "${DocOutDir}/api"
New-Item -Path $YmlPath -Name "toc.yml" -Force
foreach ($Dir in $ServiceListData)
{   
    $mappedDir = ""
    if ($serviceMapHash.ContainsKey($Dir.Name)) 
    {
        $mappedDir = $serviceMapHash[$Dir.Name]
    }
    else
    {
        $mappedDir = $Dir.Name.ToUpper()
    }

    # Store the list of artifacts into the arrays and write them into the .md file
    # after processing the list of subdirectories. This will allow the correct 
    # division of the artifacts under the Client or Management headings
    $clientArr = @()
    $mgmtArr = @()

    $PkgList = Get-ChildItem $Dir.FullName -Directory -Exclude changelog, faq, .github, build
    if (($PkgList | Measure-Object).count -eq 0)
    {
        continue
    }
    foreach ($Pkg in $PkgList)
    {
        # Load the pom file to pull the artifact name and grab the
        # parent's relative path to see which parent pom is being
        # used to determine whether or not the artifact is client 
        # or management.
        $PomPath = Join-Path -Path $Pkg -ChildPath "pom.xml"

        # no pom file = nothing to process
        if (Test-Path -path $PomPath) 
        {
            $xml = New-Object xml
            $xml.Load($PomPath)

            # Get the artifactId from the POM
            $artifactId = $xml.project.artifactId

            $parent = $xml.project.parent.relativePath

            # If this is an artifact that isn't shipping then just
            # move on to the next one
            if ($ArtifactsToSkip -contains $artifactId)
            {
                Write-Output "skipping $artifactId"
                continue
            }

            # If the parent is null or empty then the pom isn't directly including 
            # one of the pom.[client|data|management].xml and needs to be specially
            # handled
            if (("" -eq $parent) -or ($null -eq $parent))
            {
                # Cosmos has a root pom which includes pom.client.xml that won't
                # be detected by this logic. It's easier to deal with specially
                # than it is to try and climb the pom chain here.
                if ($Dir.BaseName -eq 'cosmos') 
                {
                    $clientArr += $artifactId
                } 
                else 
                {
                    Write-Host "*snowflake* Pom $PomPath, has a null or empty relative path."
                }
            } 
            else 
            {
                if (($parent.IndexOf('azure-client-sdk-parent') -ne -1) -or ($parent.IndexOf('azure-data-sdk-parent') -ne -1))
                {
                    $clientArr += $artifactId
                } 
                else 
                {
                    $mgmtArr += $artifactId
                }
            }
        }
    }
    # Only create this if there's something to create
    #if (($clientArr.Count -gt 0) -or ($mgmtArr.Count -gt 0))
    if ($clientArr.Count -gt 0)
    {
    New-Item -Path $YmlPath -Name "$($Dir.Name).md" -Force
    Add-Content -Path "$($YmlPath)/toc.yml" -Value "- name: $($mappedDir)`r`n  href: $($Dir.Name).md"
    # loop through the arrays and add the appropriate artifacts under the appropriate headings
    if ($clientArr.Count -gt 0)
    {
        Add-Content -Path "$($YmlPath)/$($Dir.Name).md" -Value "# Client Libraries"
        foreach($lib in $clientArr) 
        {
            Write-Host "Write $($lib) to $($Dir.Name).md"
            Add-Content -Path "$($YmlPath)/$($Dir.Name).md" -Value "#### $lib"
        }
    }
    # For the moment there are no management docs and with the way some of the libraries
    # in management are versioned is a bit wonky. They aren't versioned by releasing a new
    # version with the same groupId/artifactId, they're versioned with the same artifactId
    # and version with a different groupId and the groupId happens to include the date. For 
    # example, the artifact/version of azure-mgmt-storage:1.0.0-beta has several different 
    # groupIds. com.microsoft.azure.storage.v2016_01_01, com.microsoft.azure.storage.v2017_10_01,
    # com.microsoft.azure.storage.v2018_11_01 etc.
    #if ($mgmtArr.Count -gt 0) 
    #{
    #    Add-Content -Path "$($YmlPath)/$($Dir.Name).md" -Value "# Management Libraries"
    #    foreach($lib in $mgmtArr) 
    #    {
    #        Write-Output "Write $($lib) to $($Dir.Name).md"
    #        Add-Content -Path "$($YmlPath)/$($Dir.Name).md" -Value "#### $lib"
    #    }
    #}
    }
}

Write-Verbose "Creating Site Title and Navigation..."
New-Item -Path "${DocOutDir}" -Name "toc.yml" -Force
Add-Content -Path "${DocOutDir}/toc.yml" -Value "- name: Azure SDK for Java APIs`r`n  href: api/`r`n  homepage: api/index.md"

Write-Verbose "Copying root markdowns"
Copy-Item "$($RepoRoot)/README.md" -Destination "${DocOutDir}/api/index.md" -Force

Write-Verbose "Building site..."
& "${DocFxTool}" build "${DocOutDir}/docfx.json"

Copy-Item "${DocGenDir}/assets/logo.svg" -Destination "${DocOutDir}/_site/" -Force