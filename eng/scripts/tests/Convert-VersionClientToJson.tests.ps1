# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

BeforeAll {
    $script:ConverterPath = Join-Path $PSScriptRoot '..' 'Convert-VersionClientToJson.ps1'
}

Describe 'Convert-VersionClientToJson' -Tag 'UnitTest' {
    It 'creates an object keyed by artifact coordinate' {
        $json = & $script:ConverterPath -Lines @(
            '# Header',
            '',
            'com.azure:azure-core;1.57.1;1.58.0-beta.1',
            'com.azure:azure-storage-blob;12.33.2;12.34.0-beta.2 # retained only in text',
            'unreleased_com.azure.v2:azure-core;2.0.0-beta.1'
        )
        $versions = $json | ConvertFrom-Json -AsHashtable

        $versions.Keys | Should -Be @(
            'com.azure:azure-core',
            'com.azure:azure-storage-blob',
            'unreleased_com.azure.v2:azure-core'
        )
        $versions['com.azure:azure-core'].dependency | Should -Be '1.57.1'
        $versions['com.azure:azure-core'].current | Should -Be '1.58.0-beta.1'
        $versions['com.azure:azure-storage-blob'].dependency | Should -Be '12.33.2'
        $versions['com.azure:azure-storage-blob'].current | Should -Be '12.34.0-beta.2'
        $versions['unreleased_com.azure.v2:azure-core'].dependency | Should -Be '2.0.0-beta.1'
        $versions['unreleased_com.azure.v2:azure-core'].current | Should -BeNullOrEmpty
    }

    It 'writes the generated JSON to an output path' {
        $outputPath = Join-Path $TestDrive 'version_client.json'

        & $script:ConverterPath `
            -Lines @('com.azure:azure-core;1.57.1;1.58.0-beta.1') `
            -OutputPath $outputPath

        $versions = Get-Content -LiteralPath $outputPath -Raw | ConvertFrom-Json -AsHashtable
        $versions['com.azure:azure-core'].dependency | Should -Be '1.57.1'
    }

    It 'rejects malformed entries' {
        {
            & $script:ConverterPath -Lines @('com.azure:azure-core')
        } | Should -Throw
    }

    It 'rejects duplicate artifacts' {
        {
            & $script:ConverterPath -Lines @(
                'com.azure:azure-core;1.57.1;1.58.0-beta.1',
                'com.azure:azure-core;1.57.2;1.58.0-beta.2'
            )
        } | Should -Throw
    }
}
