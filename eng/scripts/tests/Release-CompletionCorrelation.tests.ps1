# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.
# Parsed YAML contracts for the template-only release-completion correlation pilot.
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
    $idExpression = '${{ parameters.ReleasePlanId }}'
    $templateGuard = '${{ if eq(parameters.ServiceDirectory, ''template'') }}'
    $autoGuard = '${{ if and(eq(parameters.ServiceDirectory, ''template''), eq(variables[''Build.Reason''], ''IndividualCI''), eq(variables[''Build.SourceBranch''], ''refs/heads/main'')) }}'

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

    $manualRelay = @(Find-YamlMap $client template 'archetype-java-release-batch.yml')[0]
    $autoRelay = @(Find-YamlMap $client template 'archetype-java-auto-release-batch.yml')[0]
    $manualCompletion = @(Find-YamlMap $manual template '/eng/common/pipelines/templates/steps/mark-release-completion.yml')[0]
    $autoStage = $auto.stages[0]
    $autoPublish = @(Find-YamlMap $autoStage job PublishDevFeedPackage)[0]
    $autoCompletion = @($autoPublish.steps | Where-Object { $_['displayName'] -eq 'Mark auto-release packages as released' })[0]
}

Describe 'Java template release-completion correlation' {
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

    It 'passes only the manual ID to common completion and only for template' {
        $parameters = $manualCompletion.parameters
        $parameters.Contains('ReleasePlanId') | Should -BeFalse
        $parameters.Contains('SdkPullRequest') | Should -BeFalse
        $parameters[$templateGuard].ReleasePlanId | Should -BeExactly $idExpression
        @($parameters[$templateGuard].Keys) | Should -Be @('ReleasePlanId')
        $parameters.PackageArtifactName | Should -BeExactly '${{artifact.name}}'
        $parameters.ConfigFileDir | Should -BeExactly '$(Pipeline.Workspace)/packages-signed/PackageInfo'
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

    It 'binds the triggering PR output at release-stage scope only for template IndividualCI main' {
        $binding = @($autoStage.variables | Where-Object { $_.Contains($autoGuard) })
        $binding.Count | Should -Be 1
        $binding[0][$autoGuard][0].name | Should -BeExactly 'AutoReleaseSdkPullRequestUrl'
        $binding[0][$autoGuard][0].value | Should -BeExactly '$[ stageDependencies.AutoReleasePrepare.ResolveAutoReleasePackages.outputs[''resolve.AutoReleaseSdkPullRequestUrl''] ]'
        @(Find-YamlMap $autoStage.jobs name AutoReleaseSdkPullRequestUrl).Count | Should -Be 0
    }

    It 'uses a file-based template auto consumer over only the filtered PackageInfo directory' {
        $inputs = $autoCompletion.inputs[$templateGuard]
        $inputs.scriptLocation | Should -BeExactly 'scriptPath'
        $inputs.scriptPath | Should -BeExactly '$(Pipeline.Workspace)/azure-sdk-for-java/eng/common/scripts/Mark-ReleasePlanCompletion.ps1'
        $inputs.Contains('inlineScript') | Should -BeFalse
        $expected = '-PackageInfoFilePath ''$(Pipeline.Workspace)/packages-signed-auto-release/PackageInfo'' -AzsdkExePath ''$(AZSDK)'' -ReleasePlanId ''${{ parameters.ReleasePlanId }}'' -SdkPullRequest ''$(AutoReleaseSdkPullRequestUrl)'''
        $inputs.arguments.Trim() | Should -BeExactly $expected
        $autoCompletion.inputs.Contains('arguments') | Should -BeFalse
        $filter = @(Find-YamlMap $autoStage displayName 'Filter signed packages for auto-release')[0]
        $filter.inputs.arguments | Should -Match '-TargetDirectory "\$\(Pipeline.Workspace\)/packages-signed-auto-release"'
        $filter.inputs.arguments | Should -Match '-ArtifactsJson ''\$\(AutoReleaseArtifactsJson\)'''
    }

    It 'leaves non-template auto completion on the existing uncorrelated per-artifact loop' {
        $legacy = $autoCompletion.inputs['${{ else }}']
        $legacy.scriptLocation | Should -BeExactly 'inlineScript'
        $legacy.inlineScript | Should -Match 'foreach \(\$artifact in \$artifacts\)'
        $legacy.inlineScript | Should -Match 'PackageInfo/\$\(\$artifact.name\).json'
        $legacy.inlineScript | Should -Not -Match 'ReleasePlanId|SdkPullRequest'
        $legacy.Contains('arguments') | Should -BeFalse
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
        $manualCompletion.parameters[$templateGuard].ReleasePlanId | Should -BeExactly $idExpression
        $argument = $autoCompletion.inputs[$templateGuard].arguments.Replace($idExpression, $value)
        $argument | Should -Match ([regex]::Escape("-ReleasePlanId '$Id'"))
    }
}
