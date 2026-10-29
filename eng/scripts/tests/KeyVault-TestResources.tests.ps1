# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

BeforeAll {
    Import-Module powershell-yaml -ErrorAction Stop
    $script:RepositoryRoot = Resolve-Path (Join-Path $PSScriptRoot '..' '..' '..')
    $pipeline = Get-Content (Join-Path $script:RepositoryRoot 'sdk' 'keyvault' 'tests.yml') -Raw |
        ConvertFrom-Yaml -Ordered
    $script:ResourceDirectories = @($pipeline.extends.parameters.TestResourceDirectories)
    $script:Template = Join-Path $script:RepositoryRoot 'sdk' $script:ResourceDirectories[0] 'test-resources.json'
    $script:PostScript = Join-Path (Split-Path $script:Template) 'test-resources-post.ps1'

    function Export-AzKeyVaultSecurityDomain {
        [CmdletBinding()]
        param($Name, $Quorum, $Certificates, $OutputPath)
        throw 'Azure calls must be mocked in this test.'
    }

    function New-AzKeyVaultRoleAssignment {
        [CmdletBinding()]
        param($HsmName, $RoleDefinitionName, $ObjectID)
        throw 'Azure calls must be mocked in this test.'
    }
}

Describe 'Key Vault test resource activation hook' -Tag 'UnitTest' {
    It 'places the HSM activation hook beside the pipeline-selected ARM template' {
        $script:ResourceDirectories.Count | Should -Be 1
        Test-Path $script:Template | Should -BeTrue
        Test-Path $script:PostScript | Should -BeTrue
        $template = Get-Content $script:Template -Raw | ConvertFrom-Json
        $template.outputs.AZURE_MANAGEDHSM_ENDPOINT.condition | Should -Be "[parameters('enableHsm')]"
        $template.outputs.CLIENT_OBJECTID.value | Should -Be "[parameters('testApplicationOid')]"
    }

    Context 'Offline hook execution' {
        BeforeEach {
            $script:FixtureRoot = Join-Path $script:RepositoryRoot 'target' "keyvault-hsm-hook-$([guid]::NewGuid())"
            New-Item -ItemType Directory -Path $script:FixtureRoot -Force | Out-Null
            $script:FixtureHook = Join-Path $script:FixtureRoot 'test-resources-post.ps1'
            Copy-Item $script:PostScript $script:FixtureHook
            Mock Export-AzKeyVaultSecurityDomain {}
            Mock New-AzKeyVaultRoleAssignment {}
            Mock Start-Sleep {}
        }

        AfterEach {
            Remove-Item $script:FixtureRoot -Recurse -Force
        }

        It 'does not activate or assign roles when no Managed HSM was deployed' {
            & $script:FixtureHook -DeploymentOutputs @{}

            Should -Invoke Export-AzKeyVaultSecurityDomain -Times 0
            Should -Invoke New-AzKeyVaultRoleAssignment -Times 0
            Should -Invoke Start-Sleep -Times 0
        }

        It 'activates the deployed HSM and assigns roles for <Endpoint>' -TestCases @(
            @{ Endpoint = 'https://fixture.managedhsm.azure.net/' }
            @{ Endpoint = 'https://fixture.managedhsm.azure.cn/' }
            @{ Endpoint = 'https://fixture.managedhsm.usgovcloudapi.net/' }
        ) {
            param($Endpoint)

            & $script:FixtureHook -DeploymentOutputs @{
                AZURE_MANAGEDHSM_ENDPOINT = $Endpoint
                CLIENT_OBJECTID = 'fixture-principal'
            }

            Should -Invoke Export-AzKeyVaultSecurityDomain -Times 1 -Exactly -ParameterFilter {
                $Name -eq 'fixture' -and $Quorum -eq 2 -and @($Certificates).Count -eq 3 -and
                    @($Certificates | Where-Object { !(Test-Path $_) }).Count -eq 0 -and
                    $OutputPath -eq (Join-Path $script:FixtureRoot 'fixture-security-domain.key')
            }
            Should -Invoke Start-Sleep -Times 1 -Exactly -ParameterFilter { $Seconds -eq 30 }
            Should -Invoke New-AzKeyVaultRoleAssignment -Times 1 -Exactly -ParameterFilter {
                $HsmName -eq 'fixture' -and $ObjectID -eq 'fixture-principal' -and
                    $RoleDefinitionName -eq 'Managed HSM Crypto Officer'
            }
            Should -Invoke New-AzKeyVaultRoleAssignment -Times 1 -Exactly -ParameterFilter {
                $HsmName -eq 'fixture' -and $ObjectID -eq 'fixture-principal' -and
                    $RoleDefinitionName -eq 'Managed HSM Crypto User'
            }
        }

        It 'does not assign roles after activation fails' {
            Mock Export-AzKeyVaultSecurityDomain { throw 'Activation failed.' }

            {
                & $script:FixtureHook -DeploymentOutputs @{
                    AZURE_MANAGEDHSM_ENDPOINT = 'https://fixture.managedhsm.azure.net/'
                    CLIENT_OBJECTID = 'fixture-principal'
                }
            } | Should -Throw '*Activation failed.*'

            Should -Invoke New-AzKeyVaultRoleAssignment -Times 0
            Should -Invoke Start-Sleep -Times 0
        }
    }
}
