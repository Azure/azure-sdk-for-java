# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.
#Requires -Version 7.0

<#
.SYNOPSIS
Generates a Java API review bundle using a staged executable JAR.

.PARAMETER SourcesJar
Optional prebuilt sources JAR for local validation. Otherwise builds the selected SDK package.
#>
[CmdletBinding(SupportsShouldProcess)]
param(
    [Parameter(Mandatory)][string]$RepositoryRoot,
    [Parameter(Mandatory)][ValidatePattern('^[A-Za-z0-9][A-Za-z0-9_.-]*$')][string]$PackageName,
    [Parameter(Mandatory)][string]$ToolJar,
    [Parameter(Mandatory)][string]$OutputDirectory,
    [string]$SourcesJar
)

Set-StrictMode -Version 4
$ErrorActionPreference = 'Stop'
$PSNativeCommandUseErrorActionPreference = $false

$root = (Resolve-Path -LiteralPath $RepositoryRoot).Path
$tool = (Resolve-Path -LiteralPath $ToolJar).Path
if (-not (Test-Path -LiteralPath $tool -PathType Leaf)) {
    throw "Expected an executable Java API review JAR: $tool"
}
$sdkRoot = Join-Path $root 'sdk'
$packages = @(Get-ChildItem -LiteralPath $sdkRoot -Directory -Recurse -Filter $PackageName |
    Where-Object { Test-Path -LiteralPath (Join-Path $_.FullName 'pom.xml') -PathType Leaf })
if ($packages.Count -ne 1) {
    throw "Expected one package directory for $PackageName; found $($packages.Count)."
}
$package = $packages[0].FullName
$pom = [xml](Get-Content -LiteralPath (Join-Path $package 'pom.xml') -Raw)
$artifact = $pom.SelectSingleNode("/*[local-name()='project']/*[local-name()='artifactId']")
$version = $pom.SelectSingleNode("/*[local-name()='project']/*[local-name()='version']")
if (-not $artifact -or $artifact.InnerText -ne $PackageName -or -not $version -or
    [string]::IsNullOrWhiteSpace($version.InnerText) -or $version.InnerText.Contains('${')) {
    throw 'The package POM must declare the expected artifact ID and a literal project version.'
}
$packageVersion = $version.InnerText.Trim()
$relativePath = [IO.Path]::GetRelativePath($root, $package).Replace('\', '/')
$output = $ExecutionContext.SessionState.Path.GetUnresolvedProviderPathFromPSPath($OutputDirectory)
if ($output.TrimEnd('\', '/') -eq $package.TrimEnd('\', '/') -or
    $output.TrimEnd('\', '/') -eq $root.TrimEnd('\', '/')) {
    throw 'The output directory must not be the package or repository root.'
}
if ($SourcesJar) {
    $SourcesJar = (Resolve-Path -LiteralPath $SourcesJar).Path
}
if (-not $PSCmdlet.ShouldProcess($output, 'Generate Java API review bundle')) {
    return
}

$previous = @{}
$published = [Collections.Generic.List[string]]::new()
foreach ($name in @('api.md', 'api.metadata.yml')) {
    $path = Join-Path $output $name
    if (Test-Path -LiteralPath $path -PathType Leaf) {
        $previous[$path] = [IO.File]::ReadAllBytes($path)
    }
}
$temporary = Join-Path ([IO.Path]::GetTempPath()) "java-api-review-$([guid]::NewGuid().ToString('N'))"
try {
    if (-not $SourcesJar) {
        $arguments = @(
            '--batch-mode', '--no-transfer-progress',
            'clean', 'package', '-f', (Join-Path $root 'pom.xml'), '-pl', $relativePath, '-am',
            '-Dmaven.javadoc.skip=true', '-Dgpg.skip=true', '-Dmaven.test.skip=true', '-DskipTestCompile=true',
            '-Djacoco.skip=true', '-Drevapi.skip=true', '-Dcheckstyle.skip=true', '-Dspotbugs.skip=true',
            '-Dspotless.skip=true', '-Dcodesnippet.skip=true', '-Danimal.sniffer.skip=true'
        )
        & mvn @arguments
        if ($LASTEXITCODE -ne 0) {
            throw "SDK package build failed with exit code $LASTEXITCODE."
        }
        $archives = @(Get-ChildItem -LiteralPath (Join-Path $package 'target') -Filter '*-sources.jar' -File)
        if ($archives.Count -ne 1) {
            throw "Expected one sources JAR; found $($archives.Count)."
        }
        $SourcesJar = $archives[0].FullName
    }
    if (-not (Test-Path -LiteralPath $SourcesJar -PathType Leaf) -or
        [IO.Path]::GetFileName($SourcesJar) -ne "$PackageName-$packageVersion-sources.jar") {
        throw 'The sources JAR filename must match the checked-out package artifact ID and version.'
    }
    New-Item -ItemType Directory -Path $temporary -Force | Out-Null
    & java -jar $tool --sources-jar $SourcesJar --package-relative-path $relativePath --output-directory $temporary
    if ($LASTEXITCODE -ne 0) {
        throw "Java API review generation failed with exit code $LASTEXITCODE."
    }
    $metadata = Join-Path $temporary 'api.metadata.yml'
    $expectedVersion = "packageVersion: '$($packageVersion.Replace("'", "''"))'"
    if (-not (Test-Path -LiteralPath $metadata -PathType Leaf) -or
        $expectedVersion -notin (Get-Content -LiteralPath $metadata)) {
        throw 'Generated package version does not match the checked-out package POM.'
    }
    foreach ($name in @('api.md', 'api.metadata.yml')) {
        if (-not (Test-Path -LiteralPath (Join-Path $temporary $name) -PathType Leaf)) {
            throw "The API review tool did not produce $name."
        }
    }
    New-Item -ItemType Directory -Path $output -Force | Out-Null
    foreach ($name in @('api.md', 'api.metadata.yml')) {
        $destination = Join-Path $output $name
        [IO.File]::Move((Join-Path $temporary $name), $destination, $true)
        $published.Add($destination)
    }
    Write-Output "Generated Java API review bundle: $output"
}
catch {
    if ($previous.Count -gt 0) {
        New-Item -ItemType Directory -Path $output -Force | Out-Null
        foreach ($path in $previous.Keys) {
            [IO.File]::WriteAllBytes($path, $previous[$path])
        }
    }
    foreach ($path in $published) {
        if (-not $previous.ContainsKey($path)) {
            Remove-Item -LiteralPath $path -Force
        }
    }
    throw
}
finally {
    if (Test-Path -LiteralPath $temporary) {
        Remove-Item -LiteralPath $temporary -Recurse -Force
    }
}
