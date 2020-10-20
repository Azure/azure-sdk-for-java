# Overides the project file and CHANGELOG.md for the template project using a custom preview version
# This is to help with testing the release pipeline.

[CmdletBinding(SupportsShouldProcess = $true)]
param(
  [Parameter(Mandatory = $true)]
  [string]$PreviewVersionNumber
)

. "${PSScriptRoot}\..\common\scripts\common.ps1"

$changeLogFile = "${PSScriptRoot}\..\..\sdk\template\azure-sdk-template\CHANGELOG.md"
$pomFile = "${PSScriptRoot}\..\..\sdk\template\azure-sdk-template\pom.xml"

$pomFileContent = New-Object -TypeName XML
$pomFileContent.PreserveWhitespace = $true
$pomFileContent.load((Resolve-Path $pomFile))
$pomFileContent.project.version = $newVersion.ToString()
Set-Content -Path $pomFile -Value $pomFileContent.OuterXml
Set-TestChangeLog -TestVersion $newVersion -changeLogFile $changeLogFile -ReleaseEntry "Test Release Pipeline"