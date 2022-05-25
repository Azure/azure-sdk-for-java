function Get-java-OnboardedDocsMsPackages($DocRepoLocation) {
    $packageOnboardingFiles = "$DocRepoLocation/package.json"
  
    $onboardingSpec = ConvertFrom-Json (Get-Content $packageOnboardingFiles -Raw)
    $allPackages = @{}
    foreach ($spec in $onboardingSpec) {
      $spec.packages | ForEach-Object {$allPackages["$($_.packageGroupId):$($_.packageArtifactId)"] = $null}
    }
    return $allPackages
}

function Get-java-DocsMsTocData($packageMetadata, $docRepoLocation) {
    # Fallback to get package-level readme name if metadata file info does not exist
    $packageLevelReadmeName = $packageMetadata.Package.Replace('azure-', '');

    # If there is a metadata json for the package use the DocsMsReadmeName from
    # the metadata function
    if ($packageMetadata.PSObject.Members.Name -contains "FileMetadata") {
        $readmeMetadata = &$GetDocsMsMetadataForPackageFn -PackageInfo $packageMetadata.FileMetadata
        $packageLevelReadmeName = $readmeMetadata.DocsMsReadMeName
    }

    $packageTocHeader = $packageMetadata.Package
    if ($packageMetadata.DisplayName) {
        $packageTocHeader = $packageMetadata.DisplayName
    }

    $children = @()
    # Children here combine namespaces in both preview and GA.
    if($packageMetadata.VersionPreview) {
        $children += Get-Toc-Children -package $packageMetadata.Package -groupId $packageMetadata.GroupId -version $packageMetadata.VersionPreview `
            -docRepoLocation $docRepoLocation -folderName "preview"
    }
    if($packageMetadata.VersionGA) {
        $children += Get-Toc-Children -package $packageMetadata.Package -groupId $packageMetadata.GroupId -version $packageMetadata.VersionGA `
            -docRepoLocation $docRepoLocation -folderName "latest"
    }
    if (!$children) {
        if ($packageMetadata.VersionPreview) {
            Write-Host "Did not find the package namespaces for $($packageMetadata.GroupId):$($packageMetadata.Package):$($packageMetadata.VersionPreview)"
        }
        if ($packageMetadata.VersionGA) {
            Write-Host "Did not find the package namespaces for $($packageMetadata.GroupId):$($packageMetadata.Package):$($packageMetadata.VersionGA)"
        }
    }
    $output = [PSCustomObject]@{
        PackageLevelReadmeHref = "~/docs-ref-services/{moniker}/$packageLevelReadmeName-readme.md"
        PackageTocHeader       = $packageTocHeader
        TocChildren            = $children
    }
    return $output
}

function Get-java-DocsMsTocChildrenForManagementPackages($packageMetadata, $docRepoLocation) {
    $children = @()
    foreach ($package in $packageMetadata) {
        if($package.VersionPreview) {
            $children += Get-Toc-Children -package $package.Package -groupId $package.GroupId -version $package.VersionPreview `
                -docRepoLocation $docRepoLocation -folderName "preview"
        }
        if($package.VersionGA) {
            $children += Get-Toc-Children -package $package.Package -groupId $package.GroupId -version $package.VersionGA `
                -docRepoLocation $docRepoLocation -folderName "latest"
        }
    }
    # Children here combine namespaces in both preview and GA.
    return ($children | Sort-Object | Get-Unique)
}

# This is a helper function which fetch the java package namespaces from javadoc jar.
# Here are the major workflow:
# 1. Read the ${package}.txt under /metadata folder
# 2. If file not found, then download javadoc jar from maven repository.
# 3. If there is 'element-list' in javadoc jar, then copy to destination ${package}.txt.
# 4. If no 'element-list', then parse the 'overview-frame.html' for the namespaces and copy to destination.
# 5. If no 'overview-frame.html', then read folder 'com/azure/...' for namespaces. E.g some mgmt packages use this structure.
# 6. Otherwise, return empty children.
function Get-Toc-Children($package, $groupId, $version, $docRepoLocation, $folderName) {
    # Looking for the txt
    $filePath = Join-Path "$docRepoLocation/metadata/$folderName" "$package.txt"
    if (!(Test-Path $filePath)) {
        # Download from maven
        # javadoc jar url. e.g.: https://repo1.maven.org/maven2/com/azure/azure-core/1.25.0/azure-core-1.25.0-javadoc.jar
        $artifact = "${groupId}:${package}:${version}:javadoc" 
        # A temp folder
        $tempDirectory = Join-Path ([System.IO.Path]::GetTempPath()) "javadoc"
        if (!(Test-Path $tempDirectory)) {
            New-Item $tempDirectory -ItemType Directory | Out-Null
        } 
        try {
            Write-Host "mvn dependency:copy -Dartifact=$artifact -DoutputDirectory=$tempDirectory"
            $javadocLocation = "$tempDirectory/$package-$version-javadoc.jar"
            & 'mvn' dependency:copy -Dartifact="$artifact" -DoutputDirectory="$tempDirectory" | Out-Null
            Write-Host "Download complete."
        }
        catch {
            Write-Error "Not able to download javadoc jar from $artifact."
            return @()
        }
        Fetch-Namespaces-From-Javadoc -jarFilePath $javadocLocation -destination $filePath
    }

    if (!(Test-Path $filePath)) {
        # Log and warn
        Write-Host "Not able to find namespaces from javadoc jar $package-$version-javadoc.jar"
    }
    return (Get-Content $filePath | ForEach-Object {$_.Trim()})
}
  
function Fetch-Namespaces-From-Javadoc ($jarFilePath, $destination) {
    $tempLocation = (Join-Path ([System.IO.Path]::GetTempPath()) "jarFiles")
    if (Test-Path $tempLocation) {
        Remove-Item $tempLocation/* -Recurse -Force 
    }
    else {
        New-Item -ItemType Directory -Path $tempLocation -Force | Out-Null
    }

    Add-Type -AssemblyName System.IO.Compression.FileSystem
    [System.IO.Compression.ZipFile]::ExtractToDirectory($jarFilePath, $tempLocation)
    Get-ChildItem $tempDirectory -Recurse
    if (Test-Path "$tempLocation/element-list") {
        # Rename and move to location
        Write-Host "Copying the element-list to $destination..."
        Copy-Item "$tempLocation/element-list" -Destination $destination
    }
    elseif (Test-Path "$tempLocation/overview-frame.html") {
        Parse-Overview-Frame -filePath "$tempLocation/overview-frame.html" -destination $destination
    }
    elseif (Test-Path "$tempLocation/com") {
        $originLocation = Get-Location 
        try {
            Set-Location $tempLocation
            $allFolders = Get-ChildItem "$tempLocation/com" -Recurse -Directory | 
                Where-Object {$_.GetFiles().Count -gt 0 -and $_.name -notmatch "class-use"}
            foreach ($path in $allFolders) {
                $path = (Resolve-Path $path -Relative) -replace "\./|\.\\"
                $path = $path -replace "\\|\/", "."
                Add-Content $destination -Value $path.Trim()
            }
        }
        finally {
            Set-Location $originLocation
        }
    }
    else {
        Write-Error "Can't find namespaces from javadoc jar jarFilePath."
    }
}
  
function Parse-Overview-Frame ($filePath, $destination) {
    $htmlBody = Get-Content $filePath
    $packages = [RegEx]::Matches($htmlBody, "<li><a.*?>(?<package>.*?)<\/a><\/li>")
    $namespaces = $packages | ForEach-Object { $_.Groups["package"].Value }    
    Add-Content -Path $destination -Value $namespaces
}

function Get-java-UpdatedDocsMsToc($toc) {
    $services = $toc[0].items
    # Add services exsting in old toc but missing in automation.
    $otherService = $services[-1]
    $sortableServices = $services | Where-Object { $_ –ne $otherService }
    foreach ($service in $sortableServices) {
        if ($service.name -eq "SQL") {
            $items = $service.items
            $service.items = @(
                [PSCustomObject]@{
                    name  = "Client"
                    landingPageType = "Service"
                    children = @("com.microsoft.azure.elasticdb*")
                }
            ) + $items
        }
        if ($service.name -eq "Log Analytics") {
            $items = $service.items
            $service.items = @(
                [PSCustomObject]@{
                    name  = "Client"
                    landingPageType = "Service"
                    children = @("com.microsoft.azure.loganalytics*")
                }
            ) + $items
        }
        if ($service.name -eq "Data Lake Analytics") {
            $service.items += @(
                [PSCustomObject]@{
                    name  = "Resource Management"
                    landingPageType = "Service"
                    children = @("com.microsoft.azure.management.datalake.analytics*")
                }
            )
        }
        if ($service.name -eq "Data Lake Store") {
            $service.items += @(
                [PSCustomObject]@{
                    name  = "Resource Management"
                    landingPageType = "Service"
                    children = @(
                        "com.microsoft.azure.management.datalakestore*",
                        "com.microsoft.azure.management.datalake.store*",
                        "com.microsoft.azure.management.datalake.store.models*"
                    )
                }
            )
        }
        if ($service.name -eq "Stream Analytics") {
            $service.items += @(
                [PSCustomObject]@{
                    name  = "Resource Management"
                    landingPageType = "Service"
                    children = @("com.microsoft.azure.management.streamanalytics*")
                }
            )
        }
    }
    $sortableServices += [PSCustomObject]@{
        name  = "Active Directory"
        href  = "~/docs-ref-services/{moniker}/resourcemanager-msi-readme.md"
        landingPageType = "Service"
        items = @(
            [PSCustomObject]@{
                name  = "Resource Management"
                href  = "~/docs-ref-services/{moniker}/resourcemanager-msi-readme.md"
                children = @("com.azure.resourcemanager.msi*")
            }, 
            [PSCustomObject]@{
                name  = "Client"
                href  = "~/docs-ref-services/{moniker}/resourcemanager-msi-readme.md"
                children = @(
                    "com.microsoft.aad.adal*",
                    "com.microsoft.aad.adal4j*",
                    "com.microsoft.identity.client*")
            })
    }
    $sortableServices += [PSCustomObject]@{
        name  = "Edge Gateway"
        landingPageType = "Service"
        items = @(
            [PSCustomObject]@{
                name  = "Resource Management"
                children = @("com.microsoft.azure.management.edgegateway*")
            })
    }
    $sortableServices += [PSCustomObject]@{
        name  = "Resource Mover"
        landingPageType = "Service"
        items = @(
            [PSCustomObject]@{
                name  = "Resource Management"
                children = @("com.microsoft.azure.management.resourcemover.v2021_01_01*")
            })
    }
    $sortableServices += [PSCustomObject]@{
        name  = "Bing AutoSuggest"
        landingPageType = "Service"
        items = @(
            [PSCustomObject]@{
                name  = "Management"
                href = "~/docs-ref-services/{moniker}/cognitiveservices/bing-autosuggest-readme.md"
                children = @("com.microsoft.azure.cognitiveservices.search.autosuggest*")
            })
    }
    $sortableServices += [PSCustomObject]@{
        name  = "Content Moderator"
        landingPageType = "Service"
        items = @(
            [PSCustomObject]@{
                name  = "Management"
                children = @("com.microsoft.azure.cognitiveservices.vision.contentmoderator*")
            })
    }
    $sortableServices += [PSCustomObject]@{
        name  = "Custom Vision"
        landingPageType = "Service"
        items = @(
            [PSCustomObject]@{
                name  = "Management"
                children = @("com.microsoft.azure.cognitiveservices.vision.customvision*")
            })
    }
    $sortableServices += [PSCustomObject]@{
        name  = "Face API"
        landingPageType = "Service"
        items = @(
            [PSCustomObject]@{
                name  = "Management"
                children = @("com.microsoft.azure.cognitiveservices.vision.faceapi*")
            })
    }
    $sortableServices += [PSCustomObject]@{
        name  = "Language Understanding"
        landingPageType = "Service"
        items = @(
            [PSCustomObject]@{
                name  = "Management"
                children = @(
                    "com.microsoft.azure.cognitiveservices.language.luis*", 
                    "com.microsoft.azure.cognitiveservices.language.luis.authoring*")
            })
    }
    $sortableServices += [PSCustomObject]@{
        name  = "Speech Service"
        landingPageType = "Service"
        items = @(
            [PSCustomObject]@{
                name  = "Management"
                children = @("com.microsoft.cognitiveservices.speech*")
            })
    }
    $sortableServices += [PSCustomObject]@{
        name  = "Text Analytics"
        landingPageType = "Service"
        items = @(
            [PSCustomObject]@{
                name  = "Management"
                children = @("com.microsoft.azure.cognitiveservices.language.text*")
            })
    }
    $sortableServices += [PSCustomObject]@{
        name  = "Cognitive Services"
        landingPageType = "Service"
        items = @(
            [PSCustomObject]@{
                name  = "Resource Management"
                children = @("com.microsoft.azure.management.cognitiveservices*")
            })
    }
    $toc[0].items = ($sortableServices | Sort-Object -Property name) + $otherService
    return , $toc
}
