#Requires -Version 7.0

[CmdletBinding()]
param(
    [Parameter(Mandatory=$true)]
    [string]$GroupId,
    [Parameter(Mandatory=$true)]
    [string]$ArtifactId
)

$externalDependenciesPath = Join-Path $PSScriptRoot '..\versioning\external_dependencies.txt'
$content = Get-Content -Path $externalDependenciesPath -Raw

$regexEscapedPrefix = [Regex]::Escape("$GroupId`:$ArtifactId")

if ($content -match "(?m)^$regexEscapedPrefix;(?<Version>.*)$") {
    $version = $Matches.Version.Trim()
    Write-Output $version
    exit 0
}

exit 1
