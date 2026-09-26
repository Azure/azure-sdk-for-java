# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

. (Join-Path $PSScriptRoot 'PR-Validation-Helpers.ps1')

function Get-PRChangeLogCoverage {
    param(
        [bool]$Enabled,
        [string]$ServiceDirectory,
        [string]$SdkType,
        [hashtable]$Context = @{}
    )

    $reason = if (-not $Enabled) {
        'Combined PR routing is disabled for this pipeline; Azure changelog verification remains enabled.'
    }
    elseif ($ServiceDirectory -cne 'auto' -or $SdkType -cne 'client') {
        'Only the unified auto/client pipeline is covered.'
    }
    elseif ($Context['TeamProject'] -cne 'public' -or
        $Context['CollectionUri'] -cnotin @('https://dev.azure.com/azure-sdk/', 'https://azure-sdk.visualstudio.com/')) {
        'Only the public azure-sdk project is covered.'
    }
    elseif ($Context['RepositoryName'] -cne 'Azure/azure-sdk-for-java' -or
        $Context['RepositoryProvider'] -cne 'GitHub' -or
        $Context['RepositoryUri'] -cnotin @(
            'https://github.com/Azure/azure-sdk-for-java',
            'https://github.com/Azure/azure-sdk-for-java.git'
        )) {
        'Private, mirrored, and other repositories are not covered.'
    }
    elseif ($Context['Reason'] -cne 'PullRequest' -or $Context['SourceBranch'] -cnotmatch '^refs/pull/[1-9][0-9]*/merge$') {
        'Only a pull request merge build is covered.'
    }
    elseif ($Context['TargetBranch'] -cnotin @('main', 'refs/heads/main')) {
        'Only main-target PRs can use Validate documentation coverage.'
    }
    else {
        ''
    }

    return [pscustomobject]@{
        Covered = $reason.Length -eq 0
        Reason = if ($reason) { $reason } else {
            'Validate documentation covers changelog validation for this public unified main PR.'
        }
    }
}

function Test-PRChangeLogCompanionDocument {
    param([string]$Path)

    # A docs trigger exclusion alone is not proof that a script or embedded build input is text-only.
    if ($Path -match '(^|/)(src|test|tests|test-shared|resources|test-resources|swagger|codegen|typespec|eng|scripts|pipeline|pipelines)(/|$)' -or
        $Path -cmatch '[\x00-\x1f\x7f]') {
        return $false
    }
    return (Test-PRRootDocumentationOnly @($Path)) -and
        [System.IO.Path]::GetExtension($Path) -cin @('.md', '.txt')
}

function Get-PRChangeLogOnlyEligibility {
    param(
        [Parameter(Mandatory = $true)][hashtable]$Snapshot,
        [Parameter(Mandatory = $true)][string]$RepositoryRoot,
        [string]$PackageInfoDirectory
    )

    $result = [pscustomobject]@{
        Eligible = $false
        RequiresFullValidation = $false
        Reason = 'No applicable package changelog change was found.'
        PackageChangeLogs = @()
    }
    $previousSdkType = $env:SDKTYPE
    $previousGitHubActions = $env:GITHUB_ACTIONS
    try {
        if ($Snapshot.SchemaVersion -ne 1 -or $Snapshot.SourceCommit -cnotmatch '^[a-f0-9]{40,64}$' -or
            $Snapshot.TargetCommit -cnotmatch '^[a-f0-9]{40,64}$' -or
            $Snapshot.ChangedFiles -isnot [array] -or $Snapshot.DeletedFiles -isnot [array]) {
            throw 'The complete merge-parent snapshot is unavailable or malformed.'
        }
        $paths = @($Snapshot.ChangedFiles) + @($Snapshot.DeletedFiles)
        if ($paths.Count -eq 0) {
            throw 'The complete PR diff is empty.'
        }
        $uniquePaths = [System.Collections.Generic.HashSet[string]]::new([System.StringComparer]::Ordinal)
        foreach ($path in $paths) {
            if ($path -isnot [string] -or [string]::IsNullOrWhiteSpace($path) -or
                $path -cmatch '(^/|^[A-Za-z]:|(^|/)\.\.?(/|$)|\\|[\x00-\x1f\x7f])') {
                throw "Invalid repository-relative path: $(ConvertTo-Json $path -Compress)"
            }
            if (-not $uniquePaths.Add($path)) {
                throw "Duplicate path in the complete PR snapshot: $(ConvertTo-Json $path -Compress)"
            }
        }

        # Basename matching is used only to require more validation, never to authorize a bypass.
        $hasPackageChangeLog = @($paths | Where-Object {
            $_ -match '(^|/)CHANGELOG\.md$' -and -not (Test-PRChangeLogCompanionDocument $_)
        }).Count -gt 0
        if (-not $hasPackageChangeLog) {
            return $result
        }
        $result.RequiresFullValidation = $true
        $changeLogs = @(
            foreach ($path in $paths) {
                if (Test-PRChangeLogCompanionDocument $path) { continue }
                if ($path -match '(^|/)(src|test|tests|test-shared|resources|test-resources|swagger|codegen|typespec|eng|scripts|pipeline|pipelines)(/|$)' -or
                    $path -cnotmatch '^sdk/([a-z0-9][a-z0-9-]*)/([a-z0-9][a-z0-9._-]*)/CHANGELOG\.md$') {
                    throw "Not an exact supported package-root changelog or reviewed companion document: $(ConvertTo-Json $path -Compress)"
                }
                if ($path -cin $Snapshot.DeletedFiles) {
                    throw "Deleted or renamed package changelogs require Java validation: $path"
                }
                $path
            }
        )
        if ($changeLogs.Count -eq 0) {
            throw 'No applicable package changelog change was found.'
        }
        if (-not $PackageInfoDirectory -or -not (Test-Path -LiteralPath $PackageInfoDirectory -PathType Container)) {
            throw 'Original PackageInfo is unavailable; eligibility must run before test-matrix suppression.'
        }
        $files = @(Get-ChildItem -LiteralPath $PackageInfoDirectory -File -Filter '*.json' -Recurse)
        if ($files.Count -eq 0) {
            throw 'Original PackageInfo is empty; it is not evidence of changelog coverage.'
        }
        $packages = @(
            foreach ($file in $files) {
                $package = Get-Content -LiteralPath $file.FullName -Raw | ConvertFrom-Json -AsHashtable
                if ($package -isnot [System.Collections.IDictionary]) {
                    throw "PackageInfo is not a metadata object: $($file.Name)"
                }
                foreach ($property in @('Name', 'ArtifactName', 'Group', 'Version', 'DirectoryPath', 'ServiceDirectory', 'SdkType')) {
                    if ($package[$property] -isnot [string] -or [string]::IsNullOrWhiteSpace($package[$property])) {
                        throw "PackageInfo $($file.Name) has missing or invalid $property."
                    }
                }
                if ($package.IsNewSdk -isnot [bool] -or -not $package.IsNewSdk -or
                    $package.SdkType -cnotin @('client', 'mgmt') -or
                    $package.IncludedForValidation -isnot [bool]) {
                    throw "PackageInfo $($file.Name) has an unsupported SDK type, track, or validation role."
                }
                $directory = [regex]::Match($package.DirectoryPath, '^sdk/([a-z0-9][a-z0-9-]*)/([a-z0-9][a-z0-9._-]*)$')
                if (-not $directory.Success -or $package.Name -cne $package.ArtifactName -or
                    $package.Name -cne $directory.Groups[2].Value -or
                    $package.ServiceDirectory -cnotin @($directory.Groups[1].Value, $package.DirectoryPath.Substring(4)) -or
                    $package.ChangeLogPath -cne "$($package.DirectoryPath)/CHANGELOG.md") {
                    throw "PackageInfo $($file.Name) does not describe an exact supported package-root CHANGELOG.md."
                }
                if ($package.ArtifactDetails -isnot [System.Collections.IDictionary] -or
                    $package.ArtifactDetails.name -cne $package.Name -or
                    $package.ArtifactDetails.groupId -cne $package.Group) {
                    throw "PackageInfo $($file.Name) has missing or inconsistent ArtifactDetails."
                }
                if ($package.ArtifactDetails.Contains('skipVerifyChangeLog') -and
                    $package.ArtifactDetails.skipVerifyChangeLog -isnot [bool]) {
                    throw "PackageInfo $($file.Name) has a non-boolean skipVerifyChangeLog."
                }
                $package
            }
        )
        foreach ($changeLog in $changeLogs) {
            $owners = @($packages | Where-Object { $_.ChangeLogPath -ceq $changeLog })
            if ($owners.Count -ne 1 -or $owners[0].IncludedForValidation) {
                throw "Expected one directly selected package for $changeLog; fallback or indirect packages are not proof."
            }
            if ($owners[0].ArtifactDetails['skipVerifyChangeLog']) {
                throw "skipVerifyChangeLog does not authorize skipping Java validation: $changeLog"
            }
            if (-not (Test-Path -LiteralPath (Join-Path $RepositoryRoot $changeLog) -PathType Leaf)) {
                throw "Changed package changelog is missing from the checkout: $changeLog"
            }
        }

        Initialize-PRValidationYaml -UseAvailableModule
        $env:SDKTYPE = 'client'
        $env:GITHUB_ACTIONS = 'true'
        . (Join-Path $RepositoryRoot 'eng' 'common' 'scripts' 'common.ps1')
        $pipeline = LoadFrom-Yaml (Join-Path $RepositoryRoot 'eng' 'pipelines' 'pullrequest.yml')
        $exclusions = GetValueSafelyFrom-Yaml $pipeline @('extends', 'parameters', 'ExcludePaths')
        if ($exclusions -isnot [array]) {
            throw 'Unified PR package-selection exclusions are unavailable.'
        }
        $selectedPaths = Update-TargetedFilesForExclude $changeLogs $exclusions
        if ($selectedPaths.Count -ne $changeLogs.Count) {
            throw 'An edited changelog belongs to a package excluded from unified PR validation.'
        }
        $errors = @(Get-PRPackageDiscoveryErrors $RepositoryRoot $changeLogs 'client')
        if ($errors.Count -gt 0) { throw ($errors -join "`n") }

        $sourcePackages = @(
            foreach ($service in ($packages | Where-Object { $_.ChangeLogPath -cin $changeLogs } |
                ForEach-Object { $_.ServiceDirectory } | Sort-Object -Unique)) {
                Get-AllPkgProperties -ServiceDirectory $service
            }
        )
        foreach ($changeLog in $changeLogs) {
            $saved = @($packages | Where-Object { $_.ChangeLogPath -ceq $changeLog })[0]
            $owners = @($sourcePackages | Where-Object {
                $_.Name -ceq $saved.Name -and $_.Group -ceq $saved.Group -and
                [System.IO.Path]::GetRelativePath($RepositoryRoot, $_.DirectoryPath).Replace('\', '/') -ceq $saved.DirectoryPath
            })
            if ($owners.Count -ne 1) {
                throw "Original PackageInfo cannot be matched to one real Java CI/POM package: $changeLog"
            }
            $owner = $owners[0]
            foreach ($property in @('Name', 'ArtifactName', 'Group', 'Version', 'ServiceDirectory', 'SdkType', 'IsNewSdk')) {
                if ($owner.$property -cne $saved[$property]) {
                    throw "Original PackageInfo $property disagrees with Java CI/POM metadata: $changeLog"
                }
            }
            if (-not $owner.ChangeLogPath -or
                [System.IO.Path]::GetRelativePath($RepositoryRoot, $owner.ChangeLogPath).Replace('\', '/') -cne $changeLog -or
                -not $owner.ArtifactDetails -or $owner.ArtifactDetails.name -cne $saved.Name -or
                $owner.ArtifactDetails.groupId -cne $saved.Group -or $owner.ArtifactDetails['skipVerifyChangeLog']) {
                throw "Real Java artifact metadata does not provide applicable changelog coverage: $changeLog"
            }
        }

        # Require regular files in both revisions. Additions, deletions, renames and type changes stay conservative.
        $packageInputs = @($changeLogs) + @($changeLogs | ForEach-Object { $_ -creplace 'CHANGELOG\.md$', 'pom.xml' })
        foreach ($revision in @(
            @{ Commit = $Snapshot.TargetCommit; Paths = $packageInputs + @($Snapshot.DeletedFiles) },
            @{ Commit = $Snapshot.SourceCommit; Paths = $packageInputs + @($Snapshot.ChangedFiles) }
        )) {
            $tree = Invoke-PRValidationGit $RepositoryRoot (@('--literal-pathspecs', 'ls-tree', '-z', $revision.Commit, '--') + $revision.Paths)
            $entries = @($tree.Split([char]0, [System.StringSplitOptions]::RemoveEmptyEntries))
            $regularFiles = @(
                foreach ($entry in $entries) {
                    if ($entry -cmatch '^100(644|755) blob [a-f0-9]+\t(.+)$') { $Matches[2] }
                }
            )
            foreach ($path in $revision.Paths) {
                if ($path -cnotin $regularFiles) {
                    throw "Routing input is not an existing regular file in the required PR revision: $path"
                }
            }
        }
        $result.Eligible = $true
        $result.RequiresFullValidation = $false
        $result.PackageChangeLogs = $changeLogs
        $result.Reason = "Every path is a verified package-root changelog or reviewed companion document; $($changeLogs.Count) applicable changelog(s)."
    }
    catch {
        $result.RequiresFullValidation = $true
        $result.Reason = $_.Exception.Message
    }
    finally {
        $env:SDKTYPE = $previousSdkType
        $env:GITHUB_ACTIONS = $previousGitHubActions
    }
    return $result
}
