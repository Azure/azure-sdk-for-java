# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

# IMPORTANT: Do not invoke this file directly. Please instead run eng/New-TestResources.ps1 from the repository root.

#Requires -Version 6.0
#Requires -PSEdition Core

# Use same parameter names as declared in eng/New-TestResources.ps1 (assume validation therein).
[CmdletBinding(SupportsShouldProcess = $true, ConfirmImpact = 'Medium')]
param (
    [Parameter()]
    [hashtable] $DeploymentOutputs,

    # Captures any arguments from eng/New-TestResources.ps1 not declared here (no parameter errors).
    [Parameter(ValueFromRemainingArguments = $true)]
    $RemainingArguments
)

$ctx = New-AzStorageContext `
    -StorageAccountName $DeploymentOutputs['STORAGE_ACCOUNT_NAME'] `
    -StorageAccountKey $DeploymentOutputs['STORAGE_ACCOUNT_KEY']

New-AzStorageContainer -Name 'mycontainer' -Context $ctx