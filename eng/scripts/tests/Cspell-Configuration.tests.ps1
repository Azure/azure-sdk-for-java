# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

BeforeAll {
    $script:RepositoryRoot = Resolve-Path (Join-Path $PSScriptRoot '..' '..' '..')
    $script:SdkRoot = Join-Path $script:RepositoryRoot 'sdk'

    function Get-RelativeRepositoryPath {
        param([Parameter(Mandatory = $true)][string]$Path)

        return [System.IO.Path]::GetRelativePath($script:RepositoryRoot, $Path).Replace('\', '/')
    }

    function Get-CspellConfigs {
        return @(
            Get-ChildItem -LiteralPath $script:SdkRoot -File -Recurse |
                Where-Object { $_.Name -in @('cspell.json', 'cspell.yml', 'cspell.yaml') }
        )
    }
}

Describe 'SDK CSpell configuration' -Tag 'UnitTest' {
    It 'imports the root config from every SDK area config' {
        $areaConfigs = @(
            Get-CspellConfigs |
                Where-Object { ((Get-RelativeRepositoryPath $_.FullName) -split '/').Count -eq 3 }
        )

        $areaConfigs.Count | Should -BeGreaterThan 0
        foreach ($config in $areaConfigs) {
            Get-Content -LiteralPath $config.FullName -Raw |
                Should -Match ([regex]::Escape('../../.vscode/cspell.json')) `
                -Because "$(Get-RelativeRepositoryPath $config.FullName) must inherit the root settings"
        }
    }

    It 'imports every package-level SDK config from its area config' {
        $packageConfigs = @(
            Get-CspellConfigs |
                Where-Object { ((Get-RelativeRepositoryPath $_.FullName) -split '/').Count -eq 4 }
        )

        $packageConfigs.Count | Should -BeGreaterThan 0
        foreach ($packageConfig in $packageConfigs) {
            $relativePath = Get-RelativeRepositoryPath $packageConfig.FullName
            $segments = $relativePath -split '/'
            $areaDirectory = Join-Path $script:SdkRoot $segments[1]
            $areaConfigs = @(
                Get-ChildItem -LiteralPath $areaDirectory -File |
                    Where-Object { $_.Name -in @('cspell.json', 'cspell.yml', 'cspell.yaml') }
            )

            $areaConfigs.Count | Should -Be 1 `
                -Because "$relativePath requires one unambiguous area config"

            $expectedImport = './' + ($segments[2..($segments.Count - 1)] -join '/')
            Get-Content -LiteralPath $areaConfigs[0].FullName -Raw |
                Should -Match ([regex]::Escape($expectedImport)) `
                -Because "$relativePath must be reachable from its area config"
        }
    }
}
