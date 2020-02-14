#!/usr/bin/env pwsh

# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

<#
    .Synopsis
    Gets the modules that are built from the given service directory and SDK type.

    .Parameter ServiceDirectory
    Name of the service being built. For example, "eventhubs".
    .Parameter SdkType
    Type of SDK to find modules for.

    .Example
    .\eng\Get-ServiceModules "eventhubs" "client"
    Will output "azure-messaging-eventhubs,azure-messaging-eventhubs-checkpointstore-blob" because
    those are modules that are built by the 'client' SDK type.

    .Example
    .\eng\Get-ServiceModules "eventhubs" "data"
    Will output "microsoft-azure-eventhubs,microsoft-azure-eventhubs-eph,microsoft-azure-eventhubs-extensions"
    because those are modules that are built by the 'data' SDK type.
#>
[CmdletBinding()]
param(
    [Parameter(Mandatory = $true, Position = 0)]
    [ValidateNotNullOrEmpty()]
    [string]$ServiceDirectory,

    [Parameter(Mandatory = $true, Position = 1)]
    [ValidateSet("data", "client")]
    [string]$SdkType
)

# By default stop for any error.
if (!$PSBoundParameters.ContainsKey("ErrorAction")) {
    $ErrorActionPreference = "Stop"
}

switch($SdkType) {
    "client" {
        $parentArtifactId = "azure-client-sdk-parent"
        break
    }
    "data" {
        $parentArtifactId = "azure-data-sdk-parent"
        break
    }
    default {
        throw "'$SdkType' is not supported."
    }
}

$modules = New-Object -TypeName "System.Collections.ArrayList"
$root = Join-Path "$PSScriptRoot/../sdk" $ServiceDirectory | Resolve-Path

foreach($file in $(Get-ChildItem $root -Filter pom*.xml -Recurse -File)) {
    [xml]$xml = Get-Content -Path $file.FullName
    $project = $xml.project

    if ($project.packaging -eq "pom") {
        continue
    }

    Write-Host "Processing POM file: $($file.FullName)"

    if (($null -eq $project.parent) -or ($project.parent.artifactId -ne $parentArtifactId)) {
        Write-Host "Parent does not match. Skipping."
        continue
    }

    $modules.Add("$($project.groupId):$($project.artifactId)") | Out-Null
}

$($modules -join ",")
