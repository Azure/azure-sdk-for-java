# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

<#
.SYNOPSIS
    Updates parent-level and root-level pom.xml files and ci.yml for a Java SDK package.

.DESCRIPTION
    This script handles two cases:
    Case 1 - Service exists, new resourcemanager package:
        - Skips root pom (service already listed)
        - Updates service-level pom.xml with new module
        - Updates existing ci.yml with new artifact entry
    Case 2 - Brand new service:
        - Updates root pom.xml with new service module
        - Creates service-level pom.xml from template
        - Skips ci.yml (creation handled by a separate script)

.PARAMETER PackagePath
    Absolute path to the root folder of the local SDK project (containing pom.xml).

.PARAMETER SdkRepoPath
    Absolute path to the root folder of the local SDK repository.

.EXAMPLE
    .\Automation-Sdk-UpdateMetadata.ps1 -PackagePath "C:\repos\azure-sdk-for-java\sdk\network\azure-resourcemanager-network" -SdkRepoPath "C:\repos\azure-sdk-for-java"
#>

param(
    [Parameter(Mandatory = $true)]
    [ValidateScript({Test-Path $_})]
    [string]$PackagePath,

    [Parameter(Mandatory = $true)]
    [ValidateScript({Test-Path $_})]
    [string]$SdkRepoPath
)

$ErrorActionPreference = "Stop"

# Import common scripts for logging functions
$commonScriptPath = Join-Path $PSScriptRoot ".." "common" "scripts" "common.ps1"
. $commonScriptPath

# Import metadata helper functions
$helperPath = Join-Path $PSScriptRoot "helpers" "Metadata-Helpers.ps1"
. $helperPath

try {
    LogInfo "========================================"
    LogInfo "Azure SDK Metadata Update Tool"
    LogInfo "========================================"
    LogInfo ""

    LogInfo "Step 1: Deriving service and module from package path..."
    $pathInfo = Get-ServiceAndModuleFromPath -PackagePath $PackagePath -SdkRepoPath $SdkRepoPath
    $service = $pathInfo.Service
    $module = $pathInfo.Module
    LogInfo "  Service: $service"
    LogInfo "  Module: $module"
    LogInfo ""

    LogInfo "Step 2: Reading groupId from package POM..."
    $pomPath = Join-Path $PackagePath "pom.xml"
    $groupId = Get-GroupIdFromPom -PomPath $pomPath
    LogInfo "  Group ID: $groupId"
    LogInfo ""

    LogInfo "Step 3: Updating root pom.xml..."
    Update-RootPom -SdkRepoPath $SdkRepoPath -Service $service
    LogInfo ""

    LogInfo "Step 4: Updating service-level pom.xml..."
    Update-ServicePom -SdkRepoPath $SdkRepoPath -Service $service -Module $module
    LogInfo ""

    LogInfo "Step 5: Updating ci.yml..."
    Update-CiYml -SdkRepoPath $SdkRepoPath -Service $service -Module $module -GroupId $groupId
    LogInfo ""

    LogInfo "✅ Metadata updated successfully!"
    exit 0
}
catch {
    LogError "An error occurred: $_"
    LogError "Stack trace: $($_.ScriptStackTrace)"
    exit 1
}
