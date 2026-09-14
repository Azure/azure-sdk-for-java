# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

BeforeAll {
    $script:GuardPath = Join-Path $PSScriptRoot '..' 'Test-RootDocumentationExclusions.ps1'
    $script:RepositoryRoot = Resolve-Path (Join-Path $PSScriptRoot '..' '..' '..')

    function Invoke-InventoryTestGit {
        param([string]$RepositoryPath, [string[]]$Arguments)

        $output = & git -C $RepositoryPath @Arguments 2>&1
        if ($LASTEXITCODE -ne 0) {
            throw "Git fixture command failed ($Arguments): $output"
        }
        return $output
    }
}

Describe 'Root documentation exclusion guard' -Tag 'UnitTest' {
    It 'accepts the exact root document but rejects longer prefixes for <Document>' -TestCases @(
        @{ Document = 'AGENTS.md' }
        @{ Document = 'CODE_OF_CONDUCT.md' }
        @{ Document = 'CONTRIBUTING.md' }
        @{ Document = 'LICENSE.txt' }
        @{ Document = 'NOTICE.txt' }
        @{ Document = 'README.md' }
        @{ Document = 'SECURITY.md' }
        @{ Document = 'SUPPORT.md' }
    ) {
        param($Document)

        { & $script:GuardPath -TrackedPaths @($Document) } | Should -Not -Throw
        { & $script:GuardPath -TrackedPaths @("$Document.template") } |
            Should -Throw "*$Document.template*matches exclusion '$Document'*"
        { & $script:GuardPath -TrackedPaths @("$Document/src/Example.java") } |
            Should -Throw "*$Document/src/Example.java*matches exclusion '$Document'*"
    }

    It 'does not confuse nested documents, protected inputs, or safe sibling names with root prefixes' {
        $paths = @(
            'sdk/example/README.md',
            'sdk/example/example/README.md',
            'sdk/example/example/CHANGELOG.md',
            'sdk/example/example/README.md.template',
            'sdk/example/example/src/main/resources/LICENSE.txt',
            'sdk/example/example/src/test/resources/NOTICE.txt',
            'sdk/example/example/src/test-shared/AGENTS.md',
            'sdk/example/example/swagger/README.md',
            'sdk/example/example/src/main/java/Example.java',
            'sdk/example/example/tsp-location.yaml',
            'docs/README.md',
            'eng/scripts/README.md',
            'README.txt',
            'README.template.md',
            'NOTICE.md',
            'AGENTS.json',
            'unknown.md'
        )

        { & $script:GuardPath -TrackedPaths $paths } | Should -Not -Throw
    }

    It 'rejects case-only aliases and mixed-case prefixes for <Path>' -TestCases @(
        @{ Path = 'readme.md' }
        @{ Path = 'License.txt' }
        @{ Path = 'readme.md.template' }
        @{ Path = 'README.MD/src/Example.java' }
        @{ Path = 'nOtIcE.TxT.template' }
    ) {
        param($Path)

        { & $script:GuardPath -TrackedPaths @($Path) } | Should -Throw '*matches exclusion*'
    }

    It 'rejects ASCII case variants even in a culture with different casing rules' {
        $previousCulture = [System.Globalization.CultureInfo]::CurrentCulture
        try {
            [System.Globalization.CultureInfo]::CurrentCulture = [System.Globalization.CultureInfo]::GetCultureInfo('tr-TR')
            { & $script:GuardPath -TrackedPaths @('license.txt.template') } | Should -Throw '*matches exclusion*'
        }
        finally {
            [System.Globalization.CultureInfo]::CurrentCulture = $previousCulture
        }
    }

    It 'retains culture-equivalent Unicode root prefixes with suffix <Suffix>' -TestCases @(
        @{ Suffix = '.template' }
        @{ Suffix = '/src/Example.java' }
    ) {
        param($Suffix)

        $previousCulture = [System.Globalization.CultureInfo]::CurrentCulture
        try {
            [System.Globalization.CultureInfo]::CurrentCulture = [System.Globalization.CultureInfo]::GetCultureInfo('en-US')
            $path = 'READ' + [char]0x00AD + 'ME.md' + $Suffix
            $path.StartsWith('README.md', [System.StringComparison]::CurrentCultureIgnoreCase) | Should -BeTrue
            { & $script:GuardPath -TrackedPaths @($path) } | Should -Throw "*matches exclusion 'README.md'*"
        }
        finally {
            [System.Globalization.CultureInfo]::CurrentCulture = $previousCulture
        }
    }

    It 'reports every collision with its exact path and an actionable fix' {
        $message = try {
            & $script:GuardPath -TrackedPaths @('README.md.template', 'SUPPORT.md/tools.ps1', 'pom.xml')
        }
        catch {
            $_.Exception.Message
        }

        $message | Should -Match '"README\.md\.template" matches exclusion ''README\.md'''
        $message | Should -Match '"SUPPORT\.md/tools\.ps1" matches exclusion ''SUPPORT\.md'''
        $message | Should -Match 'both pr\.paths\.exclude and ExcludePaths in eng/pipelines/pullrequest\.yml'
    }

    It 'fails closed without an inventory or with an empty inventory entry' {
        { & $script:GuardPath -TrackedPaths @() } | Should -Throw '*without a tracked-path inventory*'
        { & $script:GuardPath -TrackedPaths @('README.md', '') } | Should -Throw '*contains an empty path*'
        { & $script:GuardPath -TrackedPaths @('README.md', $null) } | Should -Throw
    }

    It 'finds no collisions in the current tracked repository tree' {
        { & $script:GuardPath -RepositoryRoot $script:RepositoryRoot } | Should -Not -Throw
    }
}

Describe 'Root documentation Git inventory' -Tag 'UnitTest' {
    BeforeEach {
        $repositoryPath = Join-Path $TestDrive ([guid]::NewGuid().ToString())
        New-Item -ItemType Directory -Path $repositoryPath | Out-Null
        Invoke-InventoryTestGit $repositoryPath @('init', '--quiet') | Out-Null
    }

    It 'fails closed when Git fails or the repository has no tracked paths' {
        { & $script:GuardPath -RepositoryRoot (Join-Path $repositoryPath 'missing') } |
            Should -Throw '*git exited*'
        { & $script:GuardPath -RepositoryRoot $repositoryPath } |
            Should -Throw '*empty or invalid NUL-delimited output*'
    }

    It 'inventories newly tracked non-Markdown files without relying on the changed-file spelling list' {
        Set-Content -LiteralPath (Join-Path $repositoryPath 'README.md') -Value 'Reviewed root document'
        Set-Content -LiteralPath (Join-Path $repositoryPath 'README.md.template') -Value 'Functional input'
        Invoke-InventoryTestGit $repositoryPath @('add', '--', 'README.md') | Out-Null

        { & $script:GuardPath -RepositoryRoot $repositoryPath } | Should -Not -Throw
        & pwsh -NoLogo -NoProfile -NonInteractive -File $script:GuardPath -RepositoryRoot $repositoryPath
        $LASTEXITCODE | Should -Be 0

        Invoke-InventoryTestGit $repositoryPath @('add', '--', 'README.md.template') | Out-Null
        { & $script:GuardPath -RepositoryRoot $repositoryPath } | Should -Throw '*README.md.template*'
        $PSNativeCommandUseErrorActionPreference = $false
        $output = & pwsh -NoLogo -NoProfile -NonInteractive `
            -File $script:GuardPath -RepositoryRoot $repositoryPath 2>&1
        $LASTEXITCODE | Should -Be 1
        ($output -join "`n") | Should -Match 'README\.md\.template'
    }

    It 'uses the full root-relative inventory even when invoked on a subdirectory' {
        $nestedDirectory = Join-Path $repositoryPath 'sdk' 'example'
        New-Item -ItemType Directory -Path $nestedDirectory -Force | Out-Null
        Set-Content -LiteralPath (Join-Path $nestedDirectory 'README.md') -Value 'Package document'
        Invoke-InventoryTestGit $repositoryPath @('add', '--', 'sdk/example/README.md') | Out-Null
        { & $script:GuardPath -RepositoryRoot $nestedDirectory } | Should -Not -Throw

        Set-Content -LiteralPath (Join-Path $repositoryPath 'README.md.template') -Value 'Functional input'
        Invoke-InventoryTestGit $repositoryPath @('add', '--', 'README.md.template') | Out-Null
        { & $script:GuardPath -RepositoryRoot $nestedDirectory } | Should -Throw '*README.md.template*'
    }

    It 'rejects a tracked descendant of the prefix-named root directory <Directory>' -TestCases @(
        @{ Directory = 'README.md' }
        @{ Directory = 'README.md.template' }
        @{ Directory = 'rEaDmE.Md.template' }
    ) {
        param($Directory)

        $sourceDirectory = Join-Path $repositoryPath $Directory 'src'
        New-Item -ItemType Directory -Path $sourceDirectory -Force | Out-Null
        Set-Content -LiteralPath (Join-Path $sourceDirectory 'Example.java') -Value 'Functional input'
        $path = "$Directory/src/Example.java"
        Invoke-InventoryTestGit $repositoryPath @('add', '--', $path) | Out-Null

        { & $script:GuardPath -RepositoryRoot $repositoryPath } | Should -Throw "*$path*"
    }

    It 'preserves unusual Git paths in collision diagnostics for <Path>' -TestCases @(
        @{ Path = 'README.md [fixture].template' }
        @{ Path = 'README.md"quoted.template' }
        @{ Path = "README.md`nmultiline.template" }
        @{ Path = ('READ' + [char]0x00AD + 'ME.md.template') }
        @{ Path = ('READ' + [char]0x00AD + 'ME.md/src/Example.java') }
    ) {
        param($Path)

        $fixture = Join-Path $repositoryPath 'fixture.txt'
        Set-Content -LiteralPath $fixture -Value 'Functional input'
        $blob = Invoke-InventoryTestGit $repositoryPath @('hash-object', '-w', '--', $fixture)
        # Index-only fixtures also exercise names that Windows cannot materialize.
        Invoke-InventoryTestGit $repositoryPath @(
            '-c', 'core.protectNTFS=false', 'update-index', '--add', '--cacheinfo', '100644', $blob, $Path
        ) | Out-Null

        $message = try {
            & $script:GuardPath -RepositoryRoot $repositoryPath
        }
        catch {
            $_.Exception.Message
        }
        $message | Should -Match ([regex]::Escape((ConvertTo-Json -InputObject $Path -Compress)))
        $message | Should -Match "matches exclusion 'README.md'"
    }
}
