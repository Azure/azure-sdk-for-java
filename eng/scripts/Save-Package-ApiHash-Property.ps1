# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.
#Requires -Version 7.0

[CmdletBinding(SupportsShouldProcess)]
param(
    [Parameter(Mandatory)][string]$ArtifactStagingDirectory,
    [Parameter(Mandatory)][string]$ToolJar,
    [string]$JavaExecutable = 'java'
)

Set-StrictMode -Version 4
$ErrorActionPreference = 'Stop'
$PSNativeCommandUseErrorActionPreference = $false

. (Join-Path $PSScriptRoot 'MavenPackaging.ps1')

$root = (Resolve-Path -LiteralPath $ArtifactStagingDirectory).Path
$tool = (Resolve-Path -LiteralPath $ToolJar).Path
$packages = @(Get-MavenPackageDetails -ArtifactDirectory $root)
$updates = @{}

foreach ($file in Get-ChildItem -LiteralPath (Join-Path $root 'PackageInfo') -Filter '*.json' -File) {
    $info = Get-Content -LiteralPath $file.FullName -Raw | ConvertFrom-Json
    $version = $info.Version
    if ($info.PSObject.Properties.Name -contains 'DevVersion' -and $info.DevVersion) {
        $version = $info.DevVersion
    }
    $candidates = @($packages | Where-Object {
        $_.GroupID -eq $info.Group -and $_.ArtifactID -eq $info.ArtifactName
    })
    if ($candidates.Count -eq 0) {
        continue
    }
    if ($candidates.Count -ne 1) {
        throw "Expected one staged package for $($info.Group):$($info.ArtifactName):$version."
    }
    $package = $candidates[0]
    if ($package.Version -ne $version) {
        throw "Staged package version does not match PackageInfo for $($package.FullyQualifiedName)."
    }
    $binaries = @($package.AssociatedArtifacts | Where-Object {
        -not $_.Classifier -and $_.Type -in @('jar', 'aar')
    })
    if ($binaries.Count -eq 0) {
        continue
    }
    $sources = @($package.AssociatedArtifacts | Where-Object {
        $_.Classifier -eq 'sources' -and $_.Type -eq 'jar'
    })
    if ($sources.Count -ne 1) {
        throw "Expected one staged sources JAR for $($package.FullyQualifiedName)."
    }
    $path = $info.DirectoryPath.Replace('\', '/')
    if ($path -notmatch '^sdk/[^/]+/[^/]+$') {
        throw "Expected a repository-relative SDK package path for $($info.Name): $path"
    }
    if (-not $PSCmdlet.ShouldProcess($file.FullName, 'Save API hash from the staged sources JAR')) {
        continue
    }
    $temporary = Join-Path ([IO.Path]::GetTempPath()) "java-release-api-$([guid]::NewGuid().ToString('N'))"
    try {
        & $JavaExecutable -jar $tool --sources-jar $sources[0].File.FullName --package-relative-path $path `
            --output-directory $temporary
        if ($LASTEXITCODE -ne 0) {
            throw "API hash generation failed for $($package.FullyQualifiedName) with exit code $LASTEXITCODE."
        }
        $markdown = Join-Path $temporary 'api.md'
        $metadata = @(Get-Content -LiteralPath (Join-Path $temporary 'api.metadata.yml'))
        $hash = (Get-FileHash -LiteralPath $markdown -Algorithm SHA256).Hash.ToLowerInvariant()
        $expectedVersion = "packageVersion: '$($version.Replace("'", "''"))'"
        $expectedPath = "packageRelativePath: '$($path.Replace("'", "''"))'"
        if ("apiHash: $hash" -notin $metadata -or $expectedVersion -notin $metadata -or
            $expectedPath -notin $metadata -or (Get-Item -LiteralPath $markdown).Length -eq 0) {
            throw "API review metadata does not match the staged package $($package.FullyQualifiedName)."
        }
        $info | Add-Member -MemberType NoteProperty -Name ApiHash -Value $hash -Force
        $updates[$file.FullName] = $info | ConvertTo-Json -Depth 100
    }
    finally {
        if (Test-Path -LiteralPath $temporary) {
            Remove-Item -LiteralPath $temporary -Recurse -Force
        }
    }
}

foreach ($path in $updates.Keys) {
    Set-Content -LiteralPath $path -Value $updates[$path] -Encoding utf8
}
