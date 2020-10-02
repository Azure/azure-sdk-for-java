# Generates an index page for cataloging different versions of the Docs
[CmdletBinding()]
Param (
    $RepoRoot,
    $DocGenDir,
    $lang = "java",
    $packageNameRegex = "",
    $packageNameReplacement = ""
)
. (Join-Path $PSScriptRoot ../../eng/common/scripts/Package-Properties.ps1)

# A mapping is used to fill in 1. Title of home page, 2. The azure storage link
$LangMapping = @{
    ".net"       = @("NET", "dotnet")
    "java"       = @("Java", "java")
    "javascript" = @("JavaScript", "javascript")
    "python"     = @("Python", "python")
    "c"          = @("C", "c")
    "cpp"        = @("CPP", "cpp")
}

Write-Verbose "Name Reccuring paths with variable names"
$DocFxTool = "${RepoRoot}/docfx/docfx.exe"
$DocOutDir = "${RepoRoot}/docfx_project"

Write-Verbose "Initializing Default DocFx Site..."
& "${DocFxTool}" init -q -o "${DocOutDir}"

Write-Verbose "Copying template and configuration..."
New-Item -Path "${DocOutDir}" -Name "templates" -ItemType "directory" -Force
Copy-Item "${DocGenDir}/templates/*" -Destination "${DocOutDir}/templates" -Force -Recurse
Copy-Item "${DocGenDir}/docfx.json" -Destination "${DocOutDir}/" -Force
$YmlPath = "${DocOutDir}/api"
New-Item -Path $YmlPath -Name "toc.yml" -Force

Write-Verbose "Reading artifact from storage blob ..."
$metadata = GetMetaData -lang $lang
$langRegex = $LangMapping[$lang][1]
$titleRegex = $LangMapping[$lang][0]
$regex = "^$langRegex/(.*)/$"
$pageToken = ""
# Used for sorting the toc display order
$orderSet = @{}
$orderArray = @()
# This is a pagnation call as storage only return 5000 results as maximum.
Do {
    $resp = ""
    if (!$pageToken) {
        # First page call.
        $resp = Invoke-RestMethod -Method Get -Uri "https://azuresdkdocs.blob.core.windows.net/%24web?restype=container&comp=list&prefix=$langRegex%2F&delimiter=%2F"
    }
    else {
        # Next page call
        $resp = Invoke-RestMethod -Method Get -Uri "https://azuresdkdocs.blob.core.windows.net/%24web?restype=container&comp=list&prefix=$langRegex&marker=$pageToken"
    }
    # Storage returns some weired encoded string at the begining of response which needs to cutoff before use.
    $rawConent = $resp.Substring(3)
    # Convert to xml documents.
    $xmlDoc = [xml]$rawConent
    foreach ($elem in $xmlDoc.EnumerationResults.Blobs.BlobPrefix) {
        # What service return like "dotnet/Azure.AI.Anomalydetector/", needs to fetch out "Azure.AI.Anomalydetector"
        $aritifact = $elem.Name -replace $regex, '$1'
        # Some languages need to convert the artifact name, e.g azure-data-appconfiguration -> @azure/data-appconfiguration
        if ($packageNameRegex) {
            $aritifact = $aritifact -replace $packageNameRegex, $packageNameReplacement
        }
        # Read the artifact package infomation from csv of Azure/azure-sdk/_data/release/latest repo.
        $packageInfo = $metadata | ? { $_.Package -Contains $aritifact}
        if (!$packageInfo) {
            Write-Error "Did not find the artifacts from release csv. Please check and update."
        }
       
        # Ignore the one marked as Hide
        $hidden = $packageInfo[0].Hide
        if ($hidden -and $hidden.Trim() -eq "true") {
            continue
        }
        $serviceName = $packageInfo[0].ServiceName
        # If no service name retrieved, print out warning message, and put it into Other page.
        if (!$serviceName) {
            Write-Warning "Please double check and update the artifact correct service name to corresponding repo: https://github.com/Azure/azure-sdk/tree/master/_data/releases/latest."
            Write-Warning "If the package is not relavant, please set Hide value to true."
            $serviceName = "Other"
        }
        $serviceName = $serviceName.Trim()
        # The name of generating md files.
        $dirName = ($serviceName -replace '\s', '').ToLower()
        if ($orderSet.ContainsKey($dirName)) {
            Add-Content -Path "$($YmlPath)/${dirName}.md" -Value "#### $aritifact"
        }
        else {
            New-Item -Path $YmlPath -Name "${dirName}.md" -Force
            Add-Content -Path "$($YmlPath)/${dirName}.md" -Value "#### $aritifact"
            $orderArray += $dirName
            $orderSet[$dirName] = $serviceName
        }
    }
    # Fetch page token
    $pageToken = $xmlDoc.EnumerationResults.NextMarker
} while ($pageToken)

# Sort and display toc service name by alphabetical order.
$sortedDir = $orderArray | Sort-Object
foreach ($service in $sortedDir) {
    $serviceName = $orderSet[$service]
    Add-Content -Path "$($YmlPath)/toc.yml" -Value "- name: ${serviceName}`r`n  href: ${service}.md"
}


Write-Verbose "Creating Site Title and Navigation..."
New-Item -Path "${DocOutDir}" -Name "toc.yml" -Force
Add-Content -Path "${DocOutDir}/toc.yml" -Value "- name: Azure SDK for $titleRegex APIs`r`n  href: api/`r`n  homepage: api/index.md"

Write-Verbose "Copying root markdowns"
Copy-Item "$($RepoRoot)/README.md" -Destination "${DocOutDir}/api/index.md" -Force
Copy-Item "$($RepoRoot)/CONTRIBUTING.md" -Destination "${DocOutDir}/api/CONTRIBUTING.md" -Force

Write-Verbose "Building site..."
& "${DocFxTool}" build "${DocOutDir}/docfx.json"

Copy-Item "${DocGenDir}/assets/logo.svg" -Destination "${DocOutDir}/_site/" -Force
