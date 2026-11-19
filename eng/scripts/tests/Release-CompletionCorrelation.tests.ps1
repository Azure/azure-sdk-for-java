# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.
# Parsed YAML contracts for manual release IDs and the existing automatic SDK PR handoff.
# Requires Pester 5 and powershell-yaml. Run Invoke-Pester -Path this file.
# These are local contracts, not Azure Pipelines expansion or live publication validation.

BeforeAll {
    Set-StrictMode -Version 4
    $ErrorActionPreference = 'Stop'
    Import-Module powershell-yaml -ErrorAction Stop
    $root = Resolve-Path (Join-Path $PSScriptRoot '../../..')
    $paths = @(
        'sdk/template/ci.yml',
        'eng/pipelines/templates/stages/archetype-sdk-client.yml',
        'eng/pipelines/templates/stages/archetype-java-release-batch.yml',
        'eng/pipelines/templates/stages/archetype-java-auto-release-batch.yml'
    )
    $documents = @($paths | ForEach-Object {
        ConvertFrom-Yaml (Get-Content (Join-Path $root $_) -Raw)
    })
    $entry, $client, $manual, $auto = $documents
    $script:idExpression = '${{ parameters.ReleasePlanId }}'

    function Find-YamlMap {
        param([object]$Node, [string]$Key, [string]$Value)
        if ($Node -is [System.Collections.IDictionary]) {
            if ($Node.Contains($Key) -and $Node[$Key] -eq $Value) {
                Write-Output -NoEnumerate $Node
            }
            foreach ($child in $Node.Values) {
                Find-YamlMap -Node $child -Key $Key -Value $Value
            }
        } elseif ($Node -is [System.Collections.IList]) {
            foreach ($child in $Node) {
                Find-YamlMap -Node $child -Key $Key -Value $Value
            }
        }
    }

    $script:manualRelay = @(Find-YamlMap $client template 'archetype-java-release-batch.yml')[0]
    $script:autoRelay = @(Find-YamlMap $client template 'archetype-java-auto-release-batch.yml')[0]
    $script:manualCompletion = @(Find-YamlMap $manual template '/eng/common/pipelines/templates/steps/mark-release-completion.yml')[0]
    $autoStage = $auto.stages[0]
    $autoPublish = @(Find-YamlMap $autoStage job PublishDevFeedPackage)[0]
    $script:autoCompletion = @($autoPublish.steps | Where-Object { $_['displayName'] -eq 'Mark auto-release packages as released' })[0]
}

Describe 'Java manual release-plan ID forwarding' {
    It 'declares an optional raw string ID with zero default at all four boundaries' {
        foreach ($document in $documents) {
            $parameter = @($document.parameters | Where-Object { $_.name -eq 'ReleasePlanId' })
            $parameter.Count | Should -Be 1
            $parameter[0].type | Should -BeExactly 'string'
            $parameter[0].default | Should -BeOfType ([string])
            $parameter[0].default | Should -BeExactly '0'
        }
    }

    It 'relays the ID from the template entry through both client release paths' {
        $entry.extends.parameters.ServiceDirectory | Should -BeExactly 'template'
        $entry.extends.parameters.ReleasePlanId | Should -BeExactly $idExpression
        $manualRelay.parameters.ReleasePlanId | Should -BeExactly $idExpression
        $autoRelay.parameters.ReleasePlanId | Should -BeExactly $idExpression
    }

    It 'forwards the optional manual ID without changing common completion inputs' {
        $parameters = $manualCompletion.parameters
        $parameters.ReleasePlanId | Should -BeExactly $idExpression
        $parameters.Contains('SdkPullRequest') | Should -BeFalse
        @($parameters.Keys | Sort-Object) | Should -Be @('ConfigFileDir', 'PackageArtifactName', 'ReleasePlanId', 'SourceRootPath')
        $parameters.PackageArtifactName | Should -BeExactly '${{artifact.name}}'
        $parameters.ConfigFileDir | Should -BeExactly '$(Pipeline.Workspace)/packages-signed/PackageInfo'
        $parameters.SourceRootPath | Should -BeExactly '$(Pipeline.Workspace)/azure-sdk-for-java'
        $common = ConvertFrom-Yaml (Get-Content (Join-Path $root 'eng/common/pipelines/templates/steps/mark-release-completion.yml') -Raw)
        $common.parameters.Contains('ReleasePlanId') | Should -BeTrue
        $common.parameters.Contains('SdkPullRequest') | Should -BeTrue
    }

    It 'keeps auto expansion restricted to internal IndividualCI main and preserves the prepare dependency' {
        $guard = '${{ if and(eq(variables[''System.TeamProject''], ''internal''), eq(variables[''Build.Reason''], ''IndividualCI''), eq(variables[''Build.SourceBranch''], ''refs/heads/main''), not(contains(variables[''Build.DefinitionName''], ''tests-weekly''))) }}'
        $branch = @($client.extends.parameters.stages | Where-Object { $_.Contains($guard) })
        $branch.Count | Should -Be 1
        @($branch[0][$guard] | ForEach-Object { $_.template }) | Should -Be @(
            '/eng/common/pipelines/templates/stages/archetype-auto-release-prepare.yml',
            'archetype-java-auto-release-batch.yml'
        )
        $branch[0][$guard][0].parameters.DependsOn | Should -Be @('Signing')
        $autoRelay.parameters.DependsOn | Should -Be @('AutoReleasePrepare')
        $autoStage.dependsOn | Should -BeExactly '${{ parameters.DependsOn }}'
        @($auto.parameters | Where-Object { $_.name -eq 'DependsOn' })[0].default | Should -Be @('AutoReleasePrepare')
    }

    It 'preserves the existing automatic SDK PR binding for all services' {
        $binding = @($autoStage.variables | Where-Object { $_.Contains('name') -and $_.name -eq 'AutoReleaseSdkPullRequestUrl' })
        $binding.Count | Should -Be 1
        $binding[0].value | Should -BeExactly '$[ stageDependencies.AutoReleasePrepare.ResolveAutoReleasePackages.outputs[''resolve.AutoReleaseSdkPullRequestUrl''] ]'
        @(Find-YamlMap $autoStage.jobs name AutoReleaseSdkPullRequestUrl).Count | Should -Be 0
    }

    It 'adds the optional ID to the existing filtered automatic completion loop' {
        $inputs = $autoCompletion.inputs
        $inputs.scriptLocation | Should -BeExactly 'inlineScript'
        $inputs.inlineScript | Should -Match 'foreach \(\$artifact in \$artifacts\)'
        $inputs.inlineScript | Should -Match 'PackageInfo/\$\(\$artifact.name\).json'
        $inputs.inlineScript | Should -Match ([regex]::Escape('-ReleasePlanId ''${{ parameters.ReleasePlanId }}'''))
        $inputs.inlineScript | Should -Match ([regex]::Escape('-SdkPullRequest ''$(AutoReleaseSdkPullRequestUrl)'''))
        $inputs.Contains('arguments') | Should -BeFalse
        $filter = @(Find-YamlMap $autoStage displayName 'Filter signed packages for auto-release')[0]
        $filter.inputs.arguments | Should -Match '-TargetDirectory "\$\(Pipeline.Workspace\)/packages-signed-auto-release"'
        $filter.inputs.arguments | Should -Match '-ArtifactsJson ''\$\(AutoReleaseArtifactsJson\)'''
    }

    It 'does not add service-specific branches to automatic completion' {
        @($autoCompletion.inputs.Keys | Where-Object { $_ -like '${{*' }).Count | Should -Be 0
        $autoCompletion.inputs.inlineScript | Should -Match 'AutoReleaseArtifactsJson'
        $autoCompletion.inputs.Contains('scriptPath') | Should -BeFalse
    }

    It 'preserves completion publication gating, service connection and error policy' {
        $autoCompletion.condition | Should -BeExactly 'and(succeeded(), ne(variables[''Skip.MarkReleaseCompletion''], ''true''))'
        $autoCompletion.continueOnError | Should -BeTrue
        $autoCompletion.inputs.azureSubscription | Should -BeExactly 'opensource-api-connection'
        $autoCompletion.inputs.scriptType | Should -BeExactly 'pscore'
        $autoCompletion.inputs.workingDirectory | Should -BeExactly '$(Pipeline.Workspace)'
        $autoPublish.condition | Should -BeExactly 'and(succeeded(), ne(variables[''Skip.PublishPackage''], ''true''))'
        $autoStage.condition | Should -Match 'ResolveAutoReleasePackages.resolve.HasAutoReleaseArtifacts'
        $autoStage.condition | Should -Match "ne\(variables\['Skip.Release'\], 'true'\)"
    }

    It 'preserves manual approvals and automatic publication environment' {
        $manualDeployment = @(Find-YamlMap $manual deployment PublishESRPPackage)[0]
        $autoDeployment = @(Find-YamlMap $auto deployment PublishESRPPackage)[0]
        $manualDeployment.environment | Should -BeExactly '${{ parameters.PublicPublishEnvironment }}'
        $autoDeployment.environment | Should -BeExactly 'none'
        foreach ($deployment in @($manualDeployment, $autoDeployment)) {
            $deployment.dependsOn | Should -BeExactly 'TagRepository'
            $deployment.condition | Should -BeExactly 'and(succeeded(), ne(variables[''Skip.PublishPackage''], ''true''))'
        }
        @(Find-YamlMap $manual stage Signing)[0].dependsOn | Should -BeExactly '${{parameters.DependsOn}}'
    }

    It 'keeps raw ID <Id> unchanged for downstream validation, including invalid numeric text' -TestCases @(
        @{ Id = '0' }, @{ Id = '2147483647' }, @{ Id = '2147483648' },
        @{ Id = '9007199254740993' }, @{ Id = '1.5' }, @{ Id = '-1' }, @{ Id = ' 42 ' }
    ) {
        param([string]$Id)
        # Parse a quoted queue-time value and apply the exact, untransformed relay expressions.
        $value = (ConvertFrom-Yaml "ReleasePlanId: '$Id'").ReleasePlanId
        foreach ($relay in @($entry.extends, $manualRelay, $autoRelay)) {
            $relay.parameters.ReleasePlanId | Should -BeExactly $idExpression
            $value | Should -BeOfType ([string])
            $value | Should -BeExactly $Id
        }
        $manualCompletion.parameters.ReleasePlanId | Should -BeExactly $idExpression
        $argument = $autoCompletion.inputs.inlineScript.Replace($idExpression, $value)
        $argument | Should -Match ([regex]::Escape("-ReleasePlanId '$Id'"))
    }
}
