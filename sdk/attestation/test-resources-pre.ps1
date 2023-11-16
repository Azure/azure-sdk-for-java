# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

# IMPORTANT: Do not invoke this file directly. Please instead run eng/common/TestResources/New-TestResources.ps1 from
# the repository root.

#Requires -Version 6.0
#Requires -PSEdition Core

using namespace System.Security.Cryptography
using namespace System.Security.Cryptography.X509Certificates

# Use same parameter names as declared in eng/common/New-TestResources.ps1 (assume validation therein).
[CmdletBinding(SupportsShouldProcess = $true, ConfirmImpact = 'Medium')]
param (
    # Captures any arguments from eng/common/New-TestResources.ps1 not declared here (no parameter errors).
    [Parameter(ValueFromRemainingArguments = $true)]
    $RemainingArguments
)

$ServiceRegionMap = @{
    "east asia" = "EastAsia";
    "southeast asia" = "SoutheastAsia";
    "east us" = "EastUS";
    "east us 2" = "EastUS2";
    "west us" = "WestUS";
    "west us 2" = "WestUS2";
    "central us" = "CentralUS";
    "north central us" = "NorthCentralUS";
    "south central us" = "SouthCentralUS";
    "north europe" = "NorthEurope";
    "west europe" = "WestEurope";
    "japan east" = "JapanEast";
    "japan west" = "JapanWest";
    "brazil south" = "BrazilSouth";
    "australia east" = "AustraliaEast";
    "australia southeast" = "AustraliaSoutheast";
    "central india" = "CentralIndia";
    "south india" = "SouthIndia";
    "west india" = "WestIndia";
    "china east" = "ChinaEast";
    "china north" = "ChinaNorth";
    "us gov iowa" = "USGovIowa";
    "usgov virginia" = "USGovVirginia";
    "germany central" = "GermanyCentral";
    "germany northeast" = "GermanyNortheast";
    "uk south" = "UKSouth";
    "canada east" = "CanadaEast";
    "canada central" = "CanadaCentral";
    "canada west" = "CanadaWest";
    "central us euap" = "CentralUSEUAP";
}
$AbbreviatedRegionMap = @{
    "eastasia" = "easia";
    "southeastasia" = "sasia";
    "eastus" = "eus";
    "eastus2" = "eus2";
    "westus" = "wus";
    "westus2" = "wus2";
    "centralus" = "cus";
    "northcentralus" = "ncus";
    "southcentralus" = "scus";
    "northeurope" = "neu";
    "westeurope" = "weu";
    "japaneast" = "ejp";
    "japanwest" = "wjp";
    "brazilsouth" = "sbr";
    "australiaeast" = "eau";
    "australiasoutheast" = "sau";
    "centralindia" = "cin";
    "southindia" = "sin";
    "westindia" = "win";
    "chinaeast" = "ecn";
    "chinanorth" = "ncn";
    "usgoviowa" = "iusg";
    "usgovvirginia" = "vusg";
    "germanycentral" = "cde";
    "germanynortheast" = "nde";
    "uksouth" = "uks";
    "canadaeast" = "cae";
    "canadacentral" = "cac";
    "canadawest" = "caw";
    "centraluseuap" = "cuse";
}

# By default stop for any error.
if (!$PSBoundParameters.ContainsKey('ErrorAction')) {
    $ErrorActionPreference = 'Stop'
}

function Log($Message) {
    Write-Host ('{0} - {1}' -f [DateTime]::Now.ToLongTimeString(), $Message)
}

function New-X509Certificate2([RSA] $rsa, [string] $SubjectName) {

    try {
        $req = [CertificateRequest]::new(
            [string] $SubjectName,
            $rsa,
            [HashAlgorithmName]::SHA256,
            [RSASignaturePadding]::Pkcs1
        )

        # TODO: Add any KUs necessary to $req.CertificateExtensions

        $req.CertificateExtensions.Add([X509BasicConstraintsExtension]::new($true, $false, 0, $false))

        $NotBefore = [DateTimeOffset]::Now.AddDays(-1)
        $NotAfter = $NotBefore.AddDays(365)

        $req.CreateSelfSigned($NotBefore, $NotAfter)
    }
    finally {
    }
}

Log "Running PreConfig script".

$shortLocation = $AbbreviatedRegionMap.Get_Item($Location.ToLower())
Log "Mapped long location name ${Location} to short name: ${shortLocation}"

try {
   $isolatedKey = [RSA]::Create(2048)
   $isolatedCertificate = New-X509Certificate2 $isolatedKey "CN=AttestationIsolatedManagementCertificate"

   $isolatedSigningCertificate = $([Convert]::ToBase64String($isolatedCertificate.RawData, 'None'))
   $EnvironmentVariables.Add("ISOLATED_SIGNING_CERTIFICATE", $isolatedSigningCertificate)
   $templateFileParameters.Add("isolatedSigningCertificate", $isolatedSigningCertificate)
   $isolatedSigningCertificate | Out-File -FilePath "$PSScriptRoot\ISOLATED_SIGNING_CERTIFICATE" -NoNewline

   $isolatedSigningKey = $([Convert]::ToBase64String($isolatedKey.ExportPkcs8PrivateKey()))
   $EnvironmentVariables.Add("ISOLATED_SIGNING_KEY", $isolatedSigningKey)
   $isolatedSigningKey | Out-File -FilePath "$PSScriptRoot\ISOLATED_SIGNING_KEY" -NoNewline
}
finally {
   $isolatedKey.Dispose()
}

$EnvironmentVariables.Add("LOCATION_SHORT_NAME", $shortLocation)
$templateFileParameters.Add("locationShortName", $shortLocation)

Log 'Creating 3 X509 certificates which can be used to sign policies.'
$wrappingFiles = foreach ($i in 0..2) {
    try {
        $certificateKey = [RSA]::Create(2048)
        $certificate = New-X509Certificate2 $certificateKey "CN=AttestationCertificate$i"
        $policySigningCertificate = $([Convert]::ToBase64String($certificate.RawData))
        $EnvironmentVariables.Add("POLICY_SIGNING_CERTIFICATE" + $i, $policySigningCertificate)
        $policySigningCertificate | Out-File -FilePath "$PSScriptRoot\POLICY_SIGNING_CERTIFICATE$i" -NoNewline

        $policySigningKey = $([Convert]::ToBase64String($certificateKey.ExportPkcs8PrivateKey()))
        $EnvironmentVariables.Add("POLICY_SIGNING_KEY" + $i, $policySigningKey)
        $policySigningKey | Out-File -FilePath "$PSScriptRoot\POLICY_SIGNING_KEY$i" -NoNewline
    }
    finally {
        $certificateKey.Dispose()
    }
}
