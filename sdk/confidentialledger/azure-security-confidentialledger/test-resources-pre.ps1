# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

# IMPORTANT: Do not invoke this file directly. Please instead run eng/common/New-TestResources.ps1 from the repository root.

#Requires -Version 6.0
#Requires -PSEdition Core

using namespace System.Security.Cryptography
using namespace System.Security.Cryptography.X509Certificates

# Use same parameter names as declared in eng/common/New-TestResources.ps1 (assume validation therein).
[CmdletBinding(SupportsShouldProcess = $true, ConfirmImpact = 'Medium')]
param (
    [Parameter()]
    [hashtable] $DeploymentOutputs,

    # Captures any arguments from eng/common/New-TestResources.ps1 not declared here (no parameter errors).
    [Parameter(ValueFromRemainingArguments = $true)]
    $RemainingArguments
)

# By default stop for any error.
if (!$PSBoundParameters.ContainsKey('ErrorAction')) {
    $ErrorActionPreference = 'Stop'
}

Import-Module -Name $PSScriptRoot/../../../eng/common/scripts/X509Certificate2 -Verbose
$cert = New-X509Certificate2 -SubjectName 'E=opensource@microsoft.com, CN=Azure SDK, OU=Azure SDK, O=Microsoft, L=Frisco, S=TX, C=US' -ValidDays 365

$pem = (Format-X509Certificate2 -Certificate $cert).ReplaceLineEndings([string]::Empty)
Write-Host $pem
$templateFileParameters['ConfidentialLedgerPrincipalPEM'] = $pem
$pemPk = (Format-X509Certificate2 -Type Pkcs8 -Certificate $cert).ReplaceLineEndings([string]::Empty)
$templateFileParameters['ConfidentialLedgerPrincipalPEMPK'] = $pemPk
