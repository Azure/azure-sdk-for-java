# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)]
    [string] $ReportsDirectory,
    [Parameter(Mandatory = $true)]
    [ValidateSet('Direct', 'Compute', 'ThinClient')]
    [string] $ConnectionMode
)

$ErrorActionPreference = 'Stop'
$report = Join-Path $ReportsDirectory 'TEST-com.azure.cosmos.rx.VectorSearchQueryTest.xml'
if (-not (Test-Path -LiteralPath $report)) {
    throw "Missing service-regression result for $ConnectionMode."
}

$settings = [System.Xml.XmlReaderSettings]::new()
$settings.DtdProcessing = [System.Xml.DtdProcessing]::Prohibit
$settings.XmlResolver = $null
$reader = [System.Xml.XmlReader]::Create((Resolve-Path -LiteralPath $report).Path, $settings)
try {
    $document = [System.Xml.XmlDocument]::new()
    $document.Load($reader)
} finally {
    $reader.Dispose()
}

$cases = @($document.SelectNodes('/testsuite/testcase'))
if ($cases.Count -eq 0 -or $document.SelectNodes('//failure | //error | //skipped').Count -ne 0) {
    throw "Service-regression tests must execute and pass in $ConnectionMode; failed, skipped, or empty results cannot sign off."
}
$required = @($cases | Where-Object {
    $_.GetAttribute('classname') -eq 'com.azure.cosmos.rx.VectorSearchQueryTest' -and
    $_.GetAttribute('name') -match '(^|\.)testVectorSearchAcrossEmbeddingTypes$'
})
if ($required.Count -ne 1) {
    throw "Expected one four-embedding regression execution in $ConnectionMode, found $($required.Count)."
}

$expectedHttp2 = if ($ConnectionMode -eq 'Direct') { 'false' } else { 'true' }
$expectedThinClient = if ($ConnectionMode -eq 'ThinClient') { 'true' } else { 'false' }
$expected = @{
    'COSMOS.HTTP2_ENABLED' = $expectedHttp2
    'COSMOS.THINCLIENT_ENABLED' = $expectedThinClient
    'COSMOS.THINCLIENT_QUERY_PLAN_ENABLED' = 'true'
}
foreach ($name in $expected.Keys) {
    $property = $document.SelectSingleNode("/testsuite/properties/property[@name='$name']")
    if ($null -eq $property -or $property.GetAttribute('value') -ne $expected[$name]) {
        throw "Service-regression result does not have the required $name setting for $ConnectionMode."
    }
}
Write-Host "Verified service-regression execution for $ConnectionMode."
