<#
.SYNOPSIS

This script will update all the path triggers for the ci.yml files based on our conventions.

.DESCRIPTION

This script will update all the path triggers for the ci.yml files based on our conventions.

1) include paths
 - include paths will contains the full service directory if there is only a ci.yml.
 - include paths will contains either "mgmt-" for ci.mgmt.yml files, "microsoft-" for ci.data.yml files and everything else for ci.yml if there are multiple ci*.yml files
2) exclude paths
 - all pom.xml files will be in the exclude list to help prevent triggering all pipelines whenever we only do version number changes.

As a workaround to prevent every pipeline in the repo from
triggering when we increment the verison of azure-core we are
excluding pom.xml file only changes from triggering the other
pipelines.

The expectation is that most real changes, outside of our version
increments, actually change other files in a service directory as
well so they will still trigger. If there is ever a case where only
a pom.xml file is changed then we will have to manually trigger
the pipeline via a comment to test it.

.PARAMETER filtertoServiceDirectory

By default the script runs against all service directories but if you want to limit it to on directory pass the name of the service directory here.

.PARAMETER sdkRoot

By default it will find the sdk folder for the repo this script lives in but if you need to run it against another repo pass the path of the sdk folder.

.EXAMPLE

PS> ./eng/scripts/update-yml-triggers.ps1

Run the script whenever new packages are added to a service directory to ensure the path triggers are upto date.

#>
[CmdletBinding()]
param (
  [string]$filterToServiceDirectory = "",
  [string]$sdkRoot = "$PSScriptRoot\..\..\sdk"
)

# Skip these directories as part of the update because the don't follow the normal conventions
$skipServiceDirectories = @("boms", "core", "parents")

function WriteYmlTriggerPaths($ymlFile, $includePaths, $excludePaths)
{
  $paths =  "`r`n`$1paths:"
  $paths += "`r`n`$1`$1include:"
  foreach ($path in $includePaths) {
    $paths += "`r`n`$1`$1`$1- $path"
  }
  $paths += "`r`n`$1`$1exclude:"
  foreach ($path in $excludePaths) {
    $paths += "`r`n`$1`$1`$1- $path"
  }
  $paths += "`r`n`r`n"

  $ymlContent = Get-Content $ymlFile -Raw
  $ymlContent = $ymlContent -replace "(?ms)\r\n(\s+)paths:.*?\r\n\r\n", $paths
  Set-Content -Path $ymlFile -Value $ymlContent -NoNewLine
}

function UpdateYmlTriggerPaths($serviceDirectory, $relativeRootIncludePath, $ymlFile, $directories, $rootServiceIncludeOnly = $false)
{
  $includePaths = @()
  $excludePaths = @()

  $includePaths += $relativeRootIncludePath + [System.IO.Path]::GetFileName($ymlFile)

  $pomFile = Join-Path $serviceDirectory "pom.xml"
  if (Test-Path $pomFile)
  {
    $excludePaths += $relativeRootIncludePath + "pom.xml"
  }

  foreach ($pkgDir in $directories)
  {
    $pkgDirIncludePath = $relativeRootIncludePath + $pkgDir.Name + "/"
    $includePaths += $pkgDirIncludePath

    $pomFile = Join-Path $pkgDir "pom.xml"
    if (Test-Path $pomFile)
    {
      $excludePaths += $pkgDirIncludePath + "pom.xml"
    }
  }

  if ($rootServiceIncludeOnly)
  {
    # If we only have a ci.yml make the include paths simpler
    $includePaths = @($relativeRootIncludePath)
  }

  WriteYmlTriggerPaths $ymlFile $includePaths $excludePaths
}

$serviceDirectories = Get-ChildItem $sdkRoot -Attributes Directory

foreach ($sd in $serviceDirectories)
{
  $serviceDirectory = $sd.Name
  $includePath = "sdk/${serviceDirectory}/"

  if ($filterToServiceDirectory -and $filterToServiceDirectory -ne $serviceDirectory) {
    continue
  }

  if ($serviceDirectory -in $skipServiceDirectories) {
    Write-Host "Skipping $serviceDirectory because it has unique triggers"
    continue
  }

  $ciYml = Join-Path $sd "ci.yml"
  $ciMgmtYml = Join-Path $sd "ci.mgmt.yml"
  $ciDataYml = Join-Path $sd "ci.data.yml"

  if (!(Test-Path $ciYml))
  {
    Write-Host "Skipping $($sd.Name) because there is no ci.yml"
    continue
  }

  $pkgDirectories = Get-ChildItem $sd -Attributes Directory

  $hasMgmt = $false
  $hasData = $false

  if (Test-Path $ciMgmtYml)
  {
    $hasMgmt = $true
    $mgmtPkgDirs = $pkgDirectories.Where({ $_.Name.StartsWith("mgmt-") })
    $pkgDirectories = $pkgDirectories.Where({ !$_.Name.StartsWith("mgmt-") })

    UpdateYmlTriggerPaths $sd $includePath $ciMgmtYml $mgmtPkgDirs
  }

  if (Test-Path $ciDataYml)
  {
    $hasData = $true
    $dataPkgDirs = $pkgDirectories.Where({ $_.Name.StartsWith("microsoft-") })
    $pkgDirectories = $pkgDirectories.Where({ !$_.Name.StartsWith("microsoft-") })

    UpdateYmlTriggerPaths $sd $includePath $ciDataYml $dataPkgDirs
  }

  $rootServiceIncludeOnly = $false
  if (!$hasMgmt -and !$hasData)
  {
    # If we only have a ci.yml make the include paths empty
    $rootServiceIncludeOnly = $true
  }

  UpdateYmlTriggerPaths $sd $includePath $ciYml $pkgDirectories -rootServiceIncludeOnly $rootServiceIncludeOnly
}

