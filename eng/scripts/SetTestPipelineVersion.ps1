# Overides the project file and CHANGELOG.md for the template project.
# This is to help with testing the release pipeline.

param (
  [Parameter(mandatory = $true)]
  $BuildID
)

. "${PSScriptRoot}\..\common\scripts\common.ps1"
$latestTags = git tag -l "azure-sdk-template_*"
$semVars = @()

$changeLogFile = "${PSScriptRoot}\..\..\sdk\template\azure-sdk-template\CHANGELOG.md"
$pomFile = "${PSScriptRoot}\..\..\sdk\template\azure-sdk-template\pom.xml"

Foreach ($tags in $latestTags)
{
  $semVars += $tags.Replace("azure-sdk-template_", "")
}

$semVarsSorted = [AzureEngSemanticVersion]::SortVersionStrings($semVars)
LogDebug "Last Published Version $($semVarsSorted[0])"

$newVersion = [AzureEngSemanticVersion]::ParseVersionString($semVarsSorted[0])
$newVersion.PrereleaseLabel = "beta"
$newVersion.PrereleaseNumber = $BuildID

LogDebug "Version to publish [ $($newVersion.ToString()) ]"

$pomFileContent = New-Object -TypeName XML
$pomFileContent.PreserveWhitespace = $true
$pomFileContent.load((Resolve-Path $pomFile))
$pomFileContent.project.version = $newVersion.ToString()
Set-Content -Path $pomFile -Value $pomFileContent.OuterXml
Set-Content -Path $changeLogFile -Value @"
# Release History
## $($newVersion.ToString()) ($(Get-Date -f "yyyy-MM-dd"))
- Test Release Pipeline
"@
