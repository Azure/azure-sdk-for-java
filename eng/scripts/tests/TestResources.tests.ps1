# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

BeforeAll {
    $script:RepositoryRoot = (Resolve-Path (Join-Path $PSScriptRoot '..' '..' '..')).Path
    . (Join-Path $script:RepositoryRoot 'eng' 'common' 'TestResources' 'SubConfig-Helpers.ps1')
    . (Join-Path $script:RepositoryRoot 'eng' 'common' 'TestResources' 'TestResources-Helpers.ps1')
    . (Join-Path $script:RepositoryRoot 'sdk' 'identity' 'test-resources-helpers.ps1')

    function Get-AzResourceGroup {
        [CmdletBinding()]
        param([string] $Name)
        throw 'Get-AzResourceGroup must be mocked.'
    }

    function ConvertFrom-DevOpsLoggingValue([string] $Value) {
        return $Value.Replace('%3B', ';').Replace('%0D', "`r").Replace('%0A', "`n").
            Replace('%5D', ']').Replace('%AZP25', '%')
    }

    function Assert-NoMultilineSecretVariables([string[]] $Lines) {
        foreach ($command in $Lines) {
            if ($command -match '^##vso\[task\.setvariable [^]]*issecret=true') {
                $value = $command.Substring($command.IndexOf(']') + 1)
                ConvertFrom-DevOpsLoggingValue $value | Should -Not -Match "[`r`n]"
            }
        }
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

Describe 'Test-resource pipeline secret handling' -Tag 'UnitTest' {
    BeforeEach {
        $script:FakeSecret = "first-line`r`nsecond-line%0A;]"
    }

    It 'round-trips Azure Pipelines special characters' {
        $escaped = ConvertTo-DevOpsLoggingValue $script:FakeSecret

        $escaped | Should -BeExactly 'first-line%0D%0Asecond-line%AZP250A%3B%5D'
        ConvertFrom-DevOpsLoggingValue $escaped | Should -BeExactly $script:FakeSecret
    }

    It 'does not print subscription configuration and preserves multiline variables' {
        $configuration = @{
            PIPELINE_SECURITY_TEST_TOP = $script:FakeSecret
            EnvironmentVariables = @{ PIPELINE_SECURITY_TEST_MULTILINE = $script:FakeSecret }
        } | ConvertTo-Json -Compress
        $lines = @(
            BuildAndSetSubscriptionConfig -baseSubConfigJson $configuration 6>&1 |
                ForEach-Object { $_.ToString() }
        )
        Assert-NoMultilineSecretVariables $lines
        $secretCommands = @($lines | Where-Object { $_.StartsWith('##vso[task.setsecret]') })
        $secretCommands.Count | Should -Be 4
        $maskedValues = @()
        foreach ($command in $secretCommands) {
            $command | Should -Not -Match "[`r`n]"
            $value = $command.Substring($command.IndexOf(']') + 1)
            $maskedValues += ConvertFrom-DevOpsLoggingValue $value
        }
        @($maskedValues | Where-Object { $_ -ceq 'first-line' }).Count | Should -Be 2
        @($maskedValues | Where-Object { $_ -ceq 'second-line%0A;]' }).Count | Should -Be 2

        $configCommands = @($lines | Where-Object {
            $_.StartsWith('##vso[task.setvariable variable=SubscriptionConfiguration;issecret=true]')
        })
        $configCommands.Count | Should -Be 1
        $value = $configCommands[0].Substring($configCommands[0].IndexOf(']') + 1)
        $restored = ConvertFrom-DevOpsLoggingValue $value | ConvertFrom-Json -AsHashtable
        $restored.EnvironmentVariables.PIPELINE_SECURITY_TEST_MULTILINE | Should -BeExactly $script:FakeSecret
        $restored.PIPELINE_SECURITY_TEST_TOP | Should -BeExactly $script:FakeSecret

        $ordinaryLogs = @($lines | Where-Object { -not $_.StartsWith('##vso[') }) -join "`n"
        $ordinaryLogs | Should -Not -Match 'first-line|second-line'
    }

    It 'masks multiline values from additional subscription configurations' {
        $additionalConfigurations = ConvertTo-Json -Compress -Depth 4 -InputObject @(
            @{ EnvironmentVariables = @{ PIPELINE_SECURITY_TEST_ADDITIONAL = $script:FakeSecret } }
        )
        $lines = @(
            BuildAndSetSubscriptionConfig -additionalSubConfigsJson $additionalConfigurations 6>&1 |
                ForEach-Object { $_.ToString() }
        )
        Assert-NoMultilineSecretVariables $lines
        $secretCommands = @($lines | Where-Object { $_.StartsWith('##vso[task.setsecret]') })
        $secretCommands.Count | Should -Be 2
        $values = @($secretCommands | ForEach-Object {
            ConvertFrom-DevOpsLoggingValue ($_.Substring($_.IndexOf(']') + 1))
        })
        $values | Should -Contain 'first-line'
        $values | Should -Contain 'second-line%0A;]'
    }

    It 'escapes multiline secrets when publishing deployment outputs' {
        $CI = $true
        $SuppressVsoCommands = $false
        $OutFile = $false
        $Language = 'Java'
        Mock BuildDeploymentOutputs {
            return @{ PIPELINE_SECURITY_TEST_MULTILINE = $script:FakeSecret }
        }

        $lines = @(
            SetDeploymentOutputs -serviceName 'identity' -azContext $null -deployment $null `
                -templateFile @{} -environmentVariables @{} 6>&1 |
                Where-Object { $_ -is [System.Management.Automation.InformationRecord] } |
                ForEach-Object { $_.ToString() }
        )
        Assert-NoMultilineSecretVariables $lines
        $secretCommands = @($lines | Where-Object { $_.StartsWith('##vso[task.setsecret]') })
        $secretCommands.Count | Should -Be 2
        $maskedValues = @($secretCommands | ForEach-Object {
            ConvertFrom-DevOpsLoggingValue ($_.Substring($_.IndexOf(']') + 1))
        })
        $maskedValues | Should -Contain 'first-line'
        $maskedValues | Should -Contain 'second-line%0A;]'
        $commands = @($lines | Where-Object { $_.StartsWith('##vso[task.setvariable variable=') })
        $commands.Count | Should -Be 1
        $commands[0] | Should -Not -Match "[`r`n]"
        $value = $commands[0].Substring($commands[0].IndexOf(']') + 1)
        ConvertFrom-DevOpsLoggingValue $value | Should -BeExactly $script:FakeSecret
    }

    It 'keeps single-line secrets in a secret variable' {
        $lines = @(Set-TestResourceSecret -name 'PIPELINE_SECURITY_TEST' -value 'fake%0A;]' 6>&1 |
            ForEach-Object { $_.ToString() })

        $lines.Count | Should -Be 1
        Assert-NoMultilineSecretVariables $lines
        $lines[0] | Should -BeExactly `
            '##vso[task.setvariable variable=_PIPELINE_SECURITY_TEST;issecret=true]fake%AZP250A%3B%5D'
    }

    It 'registers each nonempty line regardless of line-ending style' {
        $lines = @(Set-TestResourceSecret -name 'PIPELINE_SECURITY_TEST' -value "first`n`rsecond`rthird" 6>&1 |
            ForEach-Object { $_.ToString() })

        $lines.Count | Should -Be 3
        $values = @($lines | ForEach-Object {
            $_ | Should -Match '^##vso\[task\.setsecret\]'
            ConvertFrom-DevOpsLoggingValue ($_.Substring($_.IndexOf(']') + 1))
        })
        $values | Should -Contain 'first'
        $values | Should -Contain 'second'
        $values | Should -Contain 'third'
        Assert-NoMultilineSecretVariables $lines
    }

    It 'does not dump subscription configuration in the deployment step' {
        $deploymentStep = Get-Content (
            Join-Path $script:RepositoryRoot 'eng' 'common' 'TestResources' 'deploy-test-resources.yml'
        ) -Raw

        $deploymentStep | Should -Not -Match 'Write-Host\s+\(\$subscriptionConfiguration\s+\|\s+ConvertTo-Json'
        $deploymentStep | Should -Match "Set-PipelineVariable -Name 'SubscriptionConfiguration'"
    }
}
