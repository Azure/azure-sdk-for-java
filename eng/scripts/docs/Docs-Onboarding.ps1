#$SetDocsPackageOnboarding = "Set-${Language}-DocsPackageOnboarding"
function Set-java-DocsPackageOnboarding($moniker, $metadata, $docRepoLocation, $packageSourceOverride) { 
    $packageJsonPath = Join-Path $docRepoLocation "package.json"
    $onboardingInfo = Get-Content $packageJsonPath | ConvertFrom-Json

    $monikerOutputPath = "docs-ref-autogen"
    if ($moniker -ne 'latest') { 
        $monikerOutputPath = "$moniker/docs-ref-autogen"
    }
    $monikerIndex = -1
    for($i = 0; $i -lt $onboardingInfo.Count; $i++) {
        if ($onboardingInfo[$i].output_path -eq $monikerOutputPath) {
            $monikerIndex = $i
            break
        }
    }

    if ($monikerIndex -eq -1) {
        Write-Error "No appropriate index for moniker $moniker"
    }

    $onboardedPackages = @()
    foreach ($package in $metadata) { 
        $packageInfo = [ordered]@{
            packageArtifactId = $package.Name
            packageGroupId = $package.Group
            packageVersion = $package.Version
            
            # packageDownloadUrl is required by docs build and other values are
            # rejected. This is a temporary workaround until the docs build
            # supports more package stores.
            packageDownloadUrl = 'https://repo1.maven.org/maven2'
        }

        # Add items from 'DocsCiConfigProperties' into onboarding info. If a
        # property already exists, it will be overwritten.
        if ($package.ContainsKey('DocsCiConfigProperties')) { 
            foreach ($key in $package['DocsCiConfigProperties'].Keys) { 
                $packageInfo[$key] = $package['DocsCiConfigProperties'][$key]
            }
        }

        $onboardedPackages += $packageInfo
    }

    $onboardingInfo[$monikerIndex].packages = $onboardedPackages

    Set-Content -Path $packageJsonPath -Value (ConvertTo-Json -InputObject $onboardingInfo -Depth 100)
}

#$GetDocsPackagesAlreadyOnboarded = "Get-${Language}-DocsPackagesAlreadyOnboarded"
function Get-java-DocsPackagesAlreadyOnboarded($docRepoLocation, $moniker) { 
    return Get-java-OnboardedDocsMsPackagesForMoniker $docRepoLocation $moniker
}

# $GetPackageIdentity = "Get-${Language}-PackageIdentity"
function Get-java-PackageIdentity($package) { 
    return "$($package['Group']):$($package['Name'])"
}

# Declared in common.ps1 as 
# $GetPackageIdentityFromCsvMetadata = "Get-${Language}-PackageIdentityFromCsvMetadata"
function Get-java-PackageIdentityFromCsvMetadata($package) { 
    return "$($package.GroupId):$($Package.Package)"
}