#!/usr/bin/env pwsh

# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

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