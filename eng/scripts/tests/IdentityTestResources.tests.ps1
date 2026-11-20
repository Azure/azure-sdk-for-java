# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

BeforeAll {
    $script:RepositoryRoot = (Resolve-Path (Join-Path $PSScriptRoot '..' '..' '..')).Path
    . (Join-Path $script:RepositoryRoot 'sdk' 'identity' 'test-resources-helpers.ps1')

    function Get-AzResourceGroup {
        [CmdletBinding()]
        param([string] $Name)
        throw 'Get-AzResourceGroup must be mocked.'
    }
}

Describe 'Identity AKS test-resource selection' -Tag 'UnitTest' {
    It 'uses the resource group location when the template does not override it' {
        Mock Get-AzResourceGroup { [PSCustomObject]@{ Location = 'westus2' } }

        Get-IdentityAksLocation -TemplateParameters @{} -ResourceGroupName 'test-group' | Should -BeExactly 'westus2'
        Should -Invoke Get-AzResourceGroup -Times 1 -Exactly -ParameterFilter { $Name -eq 'test-group' }
    }

    It 'uses the explicit template location instead of the resource group location' {
        Mock Get-AzResourceGroup { throw 'Unexpected resource group lookup.' }

        Get-IdentityAksLocation -TemplateParameters @{ location = 'eastus' } -ResourceGroupName 'test-group' |
            Should -BeExactly 'eastus'
        Should -Invoke Get-AzResourceGroup -Times 0 -Exactly
    }

    It 'rejects a missing deployment location' {
        { Get-IdentityAksLocation -TemplateParameters @{ location = '' } -ResourceGroupName 'test-group' } |
            Should -Throw '*deployment location is missing*'
    }

    It 'sorts stable versions numerically and excludes preview versions and patches' {
        $versions = @'
{
  "values": [
    { "isPreview": false, "patchVersions": {
        "1.9.9": {}, "1.10.3": {}, "1.10.12": {}, "1.10.15": { "isPreview": true }
    }},
    { "isPreview": true, "patchVersions": { "1.99.0": {} }}
  ]
}
'@ | ConvertFrom-Json

        Get-LatestIdentityAksVersion -Versions $versions -Location 'westus2' | Should -BeExactly '1.10.12'
    }

    It 'fails when no stable patch is available' {
        $versions = '{"values":[{"isPreview":true,"patchVersions":{"1.99.0":{}}}]}' | ConvertFrom-Json

        { Get-LatestIdentityAksVersion -Versions $versions -Location 'westus2' } |
            Should -Throw "*No non-preview AKS patch versions are available in 'westus2'*"
    }
}
