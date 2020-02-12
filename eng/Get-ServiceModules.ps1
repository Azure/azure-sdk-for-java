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

$root = Join-Path "$PSScriptRoot/../sdk" $ServiceDirectory | Resolve-Path

switch($SdkType) {
    "client" {
        $filter = "azure-*"
        break
    }
    "data" {
        $filter = "microsoft-*"
        break
    }
    default {
        throw "'$SdkType' is not supported."
    }
}

$modules = @(Get-ChildItem $root -Filter $filter -Name -Directory)

if ($modules.Count -eq 0) {
    Write-Verbose "Could not locate matching modules to test."
    return
}

Write-Host $($modules -join ",")