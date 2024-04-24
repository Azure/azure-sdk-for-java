function Get-java-OnboardedDocsMsPackages($DocRepoLocation) {
    $packageOnboardingFiles = "$DocRepoLocation/package.json"

    $onboardingSpec = ConvertFrom-Json (Get-Content $packageOnboardingFiles -Raw)
    $allPackages = @{}
    foreach ($spec in $onboardingSpec) {
      $spec.packages | ForEach-Object {$allPackages["$($_.packageGroupId):$($_.packageArtifactId)"] = $null}
    }
    return $allPackages
}

function Get-java-OnboardedDocsMsPackagesForMoniker ($DocRepoLocation, $moniker) {
    $packageOnboardingFiles = "$DocRepoLocation/package.json"

    $onboardingSpec = ConvertFrom-Json (Get-Content $packageOnboardingFiles -Raw)
    if ("preview" -eq $moniker) {
        $onboardingSpec = $onboardingSpec | Where-Object { $_.output_path -eq "preview/docs-ref-autogen" }
    } elseif("latest" -eq $moniker) {
        $onboardingSpec = $onboardingSpec | Where-Object { $_.output_path -eq "docs-ref-autogen" }
    } elseif ("legacy" -eq $moniker) {
        $onboardingSpec = $onboardingSpec | Where-Object { $_.output_path -eq "legacy/docs-ref-autogen" }
    }

    $onboardedPackages = @{}
    foreach ($spec in $onboardingSpec.packages) {
        $packageName = $spec.packageArtifactId
        $groupId = $spec.packageGroupId
        $jsonFile = "$DocRepoLocation/metadata/$moniker/$packageName.json"
        if (Test-Path $jsonFile) {
          $onboardedPackages["$groupId`:$packageName"] = ConvertFrom-Json (Get-Content $jsonFile -Raw)
        }
        else{
          $onboardedPackages["$groupId`:$packageName"] = $null
        }
    }
    return $onboardedPackages
}

function GetPackageReadmeName ($packageMetadata) {
    # Fallback to get package-level readme name if metadata file info does not exist
    $packageLevelReadmeName = $packageMetadata.Package.Replace('azure-', '')

    # If there is a metadata json for the package use the DocsMsReadmeName from
    # the metadata function
    if ($packageMetadata.PSObject.Members.Name -contains "FileMetadata") {
        $readmeMetadata = &$GetDocsMsMetadataForPackageFn -PackageInfo $packageMetadata.FileMetadata
        $packageLevelReadmeName = $readmeMetadata.DocsMsReadMeName
    }

    return $packageLevelReadmeName
}
function Get-java-PackageLevelReadme($packageMetadata) {
    return GetPackageReadmeName -packageMetadata $packageMetadata
}

# Defined in common.ps1
# $GetDocsMsTocDataFn = "Get-${Language}-DocsMsTocData"
function Get-java-DocsMsTocData($packageMetadata, $docRepoLocation) {
    $packageLevelReadmeName = GetPackageReadmeName -packageMetadata $packageMetadata
    $packageTocHeader = GetDocsTocDisplayName -pkg $packageMetadata

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

# This function is called within a loop. To prevent multiple reads of the same
# file data, this uses a script-scoped cache variable.
$script:PackageMetadataJsonLookup = $null
function GetPackageMetadataJsonLookup($docRepoLocation) {
    if ($script:PackageMetadataJsonLookup) {
        return $script:PackageMetadataJsonLookup
    }

    $script:PackageMetadataJsonLookup = @{}
    $packageJsonFiles = Get-ChildItem $docRepoLocation/metadata/ -Filter *.json -Recurse
    foreach ($packageJsonFile in $packageJsonFiles) {
        $packageJson = Get-Content $packageJsonFile -Raw | ConvertFrom-Json -AsHashtable

        if (!$script:PackageMetadataJsonLookup.ContainsKey($packageJson.Name)) {
            $script:PackageMetadataJsonLookup[$packageJson.Name] = @($packageJson)
        } else {
            $script:PackageMetadataJsonLookup[$packageJson.Name] += $packageJson
        }
    }

    return $script:PackageMetadataJsonLookup
}


# Grab the namespaces from the json file
function Get-Toc-Children($package, $docRepoLocation) {
    $packageTable = GetPackageMetadataJsonLookup $docRepoLocation

    $namespaces = @()
    if ($packageTable.ContainsKey($package)) {
        foreach ($entry in $packageTable[$package]) {
            if ($entry.ContainsKey('Namespaces')) {
                $namespaces += $entry['Namespaces']
            }
        }
    }
    # Sort the array and clean out any dupes (there shouldn't be any but better safe than sorry)
    $namespaces = @($namespaces | Sort-Object -Unique)
    # Ensure that this always returns an array, even if there's one item or 0 items
    Write-Output -NoEnumerate $namespaces
}

# Given a library, groupId and version, return the list of namespaces
function Fetch-Namespaces-From-Javadoc($package, $groupId, $version) {

    $namespaces = @()
    # Create a temporary directory to drop the jar into
    $tempDirectory = Join-Path ([System.IO.Path]::GetTempPath()) "${groupId}-${package}-${version}"
    New-Item $tempDirectory -ItemType Directory | Out-Null
    $artifact = "${groupId}:${package}:${version}:jar:javadoc"
    try {
        # Download the Jar file
        Write-Host "mvn dependency:copy -Dartifact=""$artifact"" -DoutputDirectory=""$tempDirectory"""
        $mvnResults = mvn `
          dependency:copy `
          -Dartifact="$artifact" `
          -DoutputDirectory="$tempDirectory"

        if ($LASTEXITCODE -ne 0) {
            LogWarning "Could not download javadoc artifact: $artifact"
            $mvnResults | Write-Host
        } else {
            # Unpack the Jar file
            $javadocLocation = "$tempDirectory/$package-$version-javadoc.jar"
            $unpackDirectory = Join-Path $tempDirectory "unpackedJavadoc"
            New-Item $unpackDirectory -ItemType Directory | Out-Null
            Add-Type -AssemblyName System.IO.Compression.FileSystem
            [System.IO.Compression.ZipFile]::ExtractToDirectory($javadocLocation, $unpackDirectory)
            if (Test-Path "$unpackDirectory/element-list") {
                # Grab the namespaces from the element-list.
                Write-Host "Fetching Namespaces: processing element-list"
                foreach($line in [System.IO.File]::ReadLines("$unpackDirectory/element-list")) {
                    if (-not [string]::IsNullOrWhiteSpace($line)) {
                        $namespaces += $line
                    }
                }
            }
            elseif (Test-Path "$unpackDirectory/overview-frame.html") {
                # Grab the namespaces from the overview-frame.html's package elements
                Write-Host "Fetching Namespaces: processing overview-frame.html"
                $htmlBody = Get-Content "$unpackDirectory/overview-frame.html"
                $packages = [RegEx]::Matches($htmlBody, "<li><a.*?>(?<package>.*?)<\/a><\/li>")
                $namespaces = $packages | ForEach-Object { $_.Groups["package"].Value }
            }
            elseif (Test-Path "$unpackDirectory/com") {
                # If all else fails, scrape the namespaces from the directories
                Write-Host "Fetching Namespaces: searching the /com directores"
                $originLocation = Get-Location
                try {
                    Set-Location $unpackDirectory
                    $allFolders = Get-ChildItem "$unpackDirectory/com" -Recurse -Directory |
                        Where-Object {$_.GetFiles().Count -gt 0 -and $_.name -notmatch "class-use"}
                    foreach ($path in $allFolders) {
                        $path = (Resolve-Path $path -Relative) -replace "\./|\.\\"
                        $path = $path -replace "\\|\/", "."
                        # add the namespace to the list
                        $namespaces += $path.Trim()
                    }
                }
                finally {
                    Set-Location $originLocation
                }
            }
            else {
                LogWarning "Unable to determine namespaces from $artifact."
            }
        }
    }
    catch {
        LogError "Exception while trying to download: $artifact"
        LogError $_
        LogError $_.ScriptStackTrace
    }
    finally {
        # everything is contained within the temp directory, clean it up every time
        if (Test-Path $tempDirectory) {
            Remove-Item $tempDirectory -Recurse -Force
        }
    }

    $namespaces = @($namespaces | Sort-Object -Unique)
    # Make sure this always returns an array
    Write-Output -NoEnumerate $namespaces
}

function Get-java-RepositoryLink ($packageInfo) {
    $groupIdPath = $packageInfo.GroupId -replace "\.", "/"
    return "$PackageRepositoryUri/$groupIdPath/$($packageInfo.Package)"
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
        href  = "~/docs-ref-services/{moniker}/activedirectory.md"
        landingPageType = "Service"
        items = @(
            [PSCustomObject]@{
                name  = "Resource Management"
                href  = "~/docs-ref-services/{moniker}/resourcemanager-msi-readme.md"
                children = @("com.azure.resourcemanager.msi*")
            },
            [PSCustomObject]@{
                name  = "Client"
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
