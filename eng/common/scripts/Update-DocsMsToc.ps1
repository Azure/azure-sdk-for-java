<#
.SYNOPSIS
Update unified ToC file for publishing reference docs on docs.microsoft.com

.DESCRIPTION
Given a doc repo location and a location to output the ToC generate a Unified
Table of Contents:

* Get list of packages onboarded to docs.microsoft.com (domain specific)
* Get metadata for onboarded packages from metadata CSV
* Build a sorted list of services
* Add ToC nodes for the service
* Add "Core" packages to the bottom of the ToC under "Other"

ToC node layout:
* Service (service level overview page)
  * Client Package 1 (package level overview page)
  * Client Package 2 (package level overview page)
  ...
  * Management
    * Management Package 1
    * Management Package 2
    ...

.PARAMETER DocRepoLocation
Location of the documentation repo. This repo may be sparsely checked out
depending on the requirements for the domain

.PARAMETER OutputLocation
Output location for unified reference yml file

#>

param(
  [Parameter(Mandatory = $true)]
  [string] $DocRepoLocation,

  [Parameter(Mandatory = $true)]
  [string] $OutputLocation
)
. $PSScriptRoot/common.ps1

function GetClientPackageNode($clientPackage) {
  $packageInfo = &$GetDocsMsTocData `
    -packageMetadata $clientPackage `
    -docRepoLocation $DocRepoLocation

  return [PSCustomObject]@{
    name     = $packageInfo.PackageTocHeader
    href     = $packageInfo.PackageLevelReadmeHref
    # This is always one package and it must be an array
    children = $packageInfo.TocChildren
  };
}

$onboardedPackages = &$GetOnboardedDocsMsPackages `
  -DocRepoLocation $DocRepoLocation

# This criteria is different from criteria used in `Update-DocsMsPackages.ps1`
# because we need to generate ToCs for packages which are not necessarily "New"
# in the metadata AND onboard legacy packages (which `Update-DocsMsPackages.ps1`
# does not do)
$metadata = (Get-CSVMetadata).Where({
    $_.Package `
      -and $onboardedPackages.ContainsKey($_.Package) `
      -and $_.Hide -ne 'true'
  })

# Map metadata to package. Validate metadata entries.
$packagesForToc = @{}
foreach ($packageName in $onboardedPackages.Keys) {
  $metadataEntry = $metadata.Where({ $_.Package -eq $packageName })

  if (!$metadataEntry) {
    LogWarning "Could not find metadata for package $packageName. Skipping"
    continue
  }
  if ($metadataEntry -is [System.Collections.IEnumerable] -and ($metadataEntry | Measure-Object).Count -gt 1) {
    LogWarning "Duplicate metadata for package $packageName. Using the entry which appears first in metadata CSV file."
  }
  $metadataEntry = $metadataEntry[0]

  if (!$metadataEntry.ServiceName) {
    LogWarning "Empty ServiceName for package $packageName. Skipping."
    continue
  }

  $packagesForToc[$packageName] = $metadataEntry
}

# Get unique service names and sort alphabetically to act as the service nodes
# in the ToC
$services = @{}
foreach ($package in $packagesForToc.Values) {
  if ($package.ServiceName -eq 'Core') {
    # Skip packages under the service category "Core". Those will be handled
    # later
    continue
  }
  if (!$services.ContainsKey($package.ServiceName)) {
    $services[$package.ServiceName] = $true
  }
}
$serviceNameList = $services.Keys | Sort-Object


$toc = @()
foreach ($service in $serviceNameList) {
  Write-Host "Building service: $service"

  $packageItems = @()
  $packageCount = 0

  # Client packages get individual entries
  $clientPackages = $packagesForToc.Values.Where({ $_.ServiceName -eq $service -and (@('client', '') -contains $_.Type) })
  $clientPackages = $clientPackages | Sort-Object -Property Package
  if ($clientPackages) {
    foreach ($clientPackage in $clientPackages) {
      $packageItems += GetClientPackageNode -clientPackage $clientPackage
      $packageCount += 1
    }
  }

  # All management packages go under a single `Management` header in the ToC
  $mgmtPackages = $packagesForToc.Values.Where({ $_.ServiceName -eq $service -and ('mgmt' -eq $_.Type) })
  $mgmtPackages = $mgmtPackages | Sort-Object -Property Package
  foreach($mgmtPackage in $mgmtPackages) {
    $children = &$GetDocsMsTocChildrenForManagementPackages `
      -packageMetadata $mgmtPackages[$mgmtPackage] `
      -docRepoLocation $DocRepoLocation

    $packageItems += [PSCustomObject]@{
      name     = 'Management'
      # There could be multiple packages, ensure this is treated as an array
      # even if it is a single package
      children = @($children)
    };
    $packageCount += 1
  }

  $serviceReadmeBaseName = $service.ToLower().Replace(' ', '-')
  $serviceTocEntry = [PSCustomObject]@{
    name            = $service;
    href            = "~/docs-ref-services/{moniker}/$serviceReadmeBaseName.md"
    landingPageType = 'Service'
    items           = @($packageItems)
  }

  $toc += $serviceTocEntry
}

# Core packages belong under the "Other" node in the ToC
$corePackageItems = @()
$corePackages = $packagesForToc.Values.Where({ $_.ServiceName -eq 'Core' })
$corePackages = $corePackages | Sort-Object -Property Package

if ($corePackages) {
  foreach ($corePackage in $corePackages) {
    $corePackageItems += GetClientPackageNode $corePackage
  }
}

$toc += [PSCustomObject]@{
  name            = 'Other';
  landingPageType = 'Service';
  items           = @(
    [PSCustomObject]@{
      name            = 'Core';
      landingPageType = 'Service';
      items           = $corePackageItems;
    },
    [PSCustomObject]@{
      name            = "Uncategorized Packages";
      landingPageType = 'Service';
      children        = @('**');
    }
  )
}

$output = @([PSCustomObject]@{
    name            = 'Reference';
    landingPageType = 'Root';
    expanded        = $false;
    items           = $toc
  })

$outputYaml = ConvertTo-Yaml $output
Set-Content -Path $OutputLocation -Value $outputYaml
