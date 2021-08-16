#Requires -Version 6.0

    [CmdletBinding()]
param(
    [Parameter(Mandatory = $true)]
    [string]$ReleaseDate, # Pass Date in the form MM/dd/yyyy"
    [string]$ServiceDirectory,
    [string]$ReleaseVersion,
    [string]$PackageName
)
Set-StrictMode -Version 3

. ${PSScriptRoot}\..\..\..\..\eng\common\scripts\common.ps1
. ${PSScriptRoot}\..\..\..\..\eng\common\scripts\Package-Properties.ps1

$packageProperties = $null
$packageProperties = Get-PkgProperties -PackageName $PackageName -ServiceDirectory $ServiceDirectory
if (!$packageProperties)
{
    Write-Error "Could not find a package with name [ $packageName ], please verify the package name matches the exact name."
    exit 1
}

$ParsedReleaseDate = [datetime]$ReleaseDate
$releaseDateString = $ParsedReleaseDate.ToString("MM/dd/yyyy")

$ReleaseVersionParsed = [AzureEngSemanticVersion]::ParseVersionString($ReleaseVersion)
if ($null -eq $ReleaseVersionParsed)
{
    Write-Error "Invalid version $ReleaseVersion. Version must follow standard SemVer rules, see https://aka.ms/azsdk/engsys/packageversioning"
    exit 1
}

&$EngCommonScriptsDir/Update-DevOps-Release-WorkItem.ps1 `
  -language $LanguageDisplayName `
  -packageName $packageProperties.Name `
  -version $ReleaseVersion `
  -plannedDate $releaseDateString `
  -packageRepoPath $packageProperties.serviceDirectory `
  -packageType $packageProperties.SDKType `
  -packageNewLibrary $packageProperties.IsNewSDK

if ($LASTEXITCODE -ne 0) {
    Write-Error "Updating of the Devops Release WorkItem failed."
    exit 1
}