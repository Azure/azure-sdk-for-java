# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

function Invoke-PRValidationProcess {
    param(
        [Parameter(Mandatory = $true)][string]$FilePath,
        [Parameter(Mandatory = $true)][string[]]$Arguments,
        [Parameter(Mandatory = $true)][string]$WorkingDirectory,
        [AllowEmptyString()][string]$InputText
    )

    $startInfo = [System.Diagnostics.ProcessStartInfo]::new()
    $startInfo.FileName = $FilePath
    $startInfo.WorkingDirectory = $WorkingDirectory
    $startInfo.UseShellExecute = $false
    $startInfo.RedirectStandardOutput = $true
    $startInfo.RedirectStandardError = $true
    $startInfo.StandardOutputEncoding = [System.Text.Encoding]::UTF8
    $startInfo.StandardErrorEncoding = [System.Text.Encoding]::UTF8
    $startInfo.RedirectStandardInput = $PSBoundParameters.ContainsKey('InputText')
    if ($startInfo.RedirectStandardInput) {
        $startInfo.StandardInputEncoding = [System.Text.UTF8Encoding]::new($false)
    }
    foreach ($argument in $Arguments) {
        $startInfo.ArgumentList.Add($argument)
    }

    $process = [System.Diagnostics.Process]::Start($startInfo)
    try {
        $outputTask = $process.StandardOutput.ReadToEndAsync()
        $errorTask = $process.StandardError.ReadToEndAsync()
        if ($startInfo.RedirectStandardInput) {
            $process.StandardInput.Write($InputText)
            $process.StandardInput.Close()
        }
        $process.WaitForExit()
        return [pscustomobject]@{
            ExitCode = $process.ExitCode
            Output = $outputTask.GetAwaiter().GetResult()
            Error = $errorTask.GetAwaiter().GetResult()
        }
    }
    finally {
        $process.Dispose()
    }
}

function Invoke-PRValidationGit {
    param([string]$RepositoryRoot, [string[]]$Arguments)

    $git = (Get-Command git -CommandType Application -ErrorAction Stop | Select-Object -First 1).Source
    $result = Invoke-PRValidationProcess -FilePath $git -WorkingDirectory $RepositoryRoot -Arguments $Arguments
    if ($result.ExitCode -ne 0) {
        throw "Git failed ($($result.ExitCode)): git $($Arguments -join ' '). $($result.Error)"
    }
    return $result.Output
}

function Get-PRValidationSnapshot {
    param(
        [Parameter(Mandatory = $true)][string]$RepositoryRoot,
        [string]$SourceCommittish = 'HEAD',
        [string]$TargetCommittish
    )

    $source = (Invoke-PRValidationGit $RepositoryRoot @(
        'rev-parse', '--verify', '--end-of-options', "$SourceCommittish^{commit}"
    )).Trim()
    if (-not $TargetCommittish) {
        $commit = Invoke-PRValidationGit $RepositoryRoot @('cat-file', '-p', $source)
        $parents = @([regex]::Matches(($commit -split "`n`n", 2)[0], '(?m)^parent ([a-f0-9]+)\r?$'))
        if ($parents.Count -ne 2) {
            throw 'Expected a synthetic PR merge commit with two parents. For local diagnostics, pass TargetCommittish explicitly.'
        }
        $TargetCommittish = $parents[0].Groups[1].Value
    }
    $target = (Invoke-PRValidationGit $RepositoryRoot @(
        'rev-parse', '--verify', '--end-of-options', "$TargetCommittish^{commit}"
    )).Trim()
    Invoke-PRValidationGit $RepositoryRoot @('merge-base', '--is-ancestor', $target, $source) | Out-Null

    # Disabling rename detection retains both sides of renames, including deleted package inputs.
    $output = Invoke-PRValidationGit $RepositoryRoot @(
        'diff', '--no-ext-diff', '--no-textconv', '--no-renames', '--name-status', '-z', $target, $source, '--'
    )
    $changed = [System.Collections.Generic.List[string]]::new()
    $deleted = [System.Collections.Generic.List[string]]::new()
    if ($output.Length -gt 0) {
        if (-not $output.EndsWith("`0", [System.StringComparison]::Ordinal)) {
            throw 'Git returned an incomplete NUL-delimited PR diff.'
        }
        $fields = $output.Substring(0, $output.Length - 1).Split([char]0)
        if ($fields.Count % 2 -ne 0) {
            throw 'Git returned an invalid status/path PR diff.'
        }
        for ($index = 0; $index -lt $fields.Count; $index += 2) {
            $status = $fields[$index]
            $path = $fields[$index + 1]
            if ($status -cnotmatch '^[AMDT]$' -or [string]::IsNullOrEmpty($path)) {
                throw "Unsupported PR diff entry: $(ConvertTo-Json @($status, $path) -Compress)"
            }
            if ($status -ceq 'D') {
                $deleted.Add($path)
            }
            else {
                $changed.Add($path)
            }
        }
    }

    return [ordered]@{
        SchemaVersion = 1
        SourceCommit = $source
        TargetCommit = $target
        ChangedFiles = @($changed.ToArray())
        DeletedFiles = @($deleted.ToArray())
        ChangedServices = @(
            @($changed.ToArray()) + @($deleted.ToArray()) |
                ForEach-Object { if ($_ -cmatch '^sdk/([^/]+)/') { $Matches[1] } } |
                Sort-Object -Unique
        )
        ExcludePaths = @()
        PRNumber = '-1'
    }
}

function Read-PRValidationSnapshot {
    param([Parameter(Mandatory = $true)][string]$Path)

    $snapshot = Get-Content -LiteralPath $Path -Raw -ErrorAction Stop | ConvertFrom-Json -AsHashtable
    if ($snapshot.SchemaVersion -ne 1 -or $snapshot.SourceCommit -cnotmatch '^[a-f0-9]{40,64}$' -or
        $snapshot.TargetCommit -cnotmatch '^[a-f0-9]{40,64}$') {
        throw "Invalid or unavailable PR snapshot in '$Path'."
    }
    foreach ($field in @('ChangedFiles', 'DeletedFiles')) {
        if (-not $snapshot.ContainsKey($field) -or $snapshot[$field] -isnot [array]) {
            throw "PR snapshot '$Path' is missing the complete $field array."
        }
        foreach ($file in $snapshot[$field]) {
            if ($file -isnot [string] -or [string]::IsNullOrEmpty($file) -or
                $file -cmatch '(^/|^[A-Za-z]:|(^|/)\.\.?(/|$)|\\)') {
                throw "Invalid repository-relative path in '$Path': $(ConvertTo-Json $file -Compress)"
            }
        }
    }
    return $snapshot
}

function Test-PRRootDocumentationOnly {
    param([AllowEmptyCollection()][string[]]$Paths)

    $rootDocuments = @(
        'AGENTS.md', 'CODE_OF_CONDUCT.md', 'CONTRIBUTING.md', 'LICENSE.txt',
        'NOTICE.txt', 'README.md', 'SECURITY.md', 'SUPPORT.md'
    )
    if ($Paths.Count -eq 0) {
        return $false
    }
    foreach ($path in $Paths) {
        if ($path -cnotin $rootDocuments -and -not $path.StartsWith('docs/', [System.StringComparison]::Ordinal)) {
            return $false
        }
    }
    return $true
}

function Initialize-PRValidationYaml {
    param(
        [Parameter(Mandatory = $true)][string]$DependencyDirectory,
        [string]$RequirementsPath = (Join-Path $PSScriptRoot '..' 'pr-validation-requirements.psd1')
    )

    $requirement = Import-PowerShellDataFile -LiteralPath $RequirementsPath
    $modulePath = Join-Path $DependencyDirectory $requirement.ModuleName $requirement.RequiredVersion `
        "$($requirement.ModuleName).psd1"
    if (-not (Test-Path -LiteralPath $modulePath -PathType Leaf)) {
        # The shared installer uses an Azure feed. Restore the declared module publicly into this run's directory.
        New-Item -ItemType Directory -Path $DependencyDirectory -Force | Out-Null
        Save-Module -Name $requirement.ModuleName -RequiredVersion $requirement.RequiredVersion `
            -Repository $requirement.Repository -Path $DependencyDirectory -Force -ErrorAction Stop
    }
    if (-not (Test-Path -LiteralPath $modulePath -PathType Leaf)) {
        throw "Could not restore $($requirement.ModuleName) $($requirement.RequiredVersion)."
    }
    $separator = [System.IO.Path]::PathSeparator
    if ($DependencyDirectory -notin ($env:PSModulePath -split [regex]::Escape($separator))) {
        $env:PSModulePath = $DependencyDirectory + $separator + $env:PSModulePath
    }
    $module = Get-Module -Name $requirement.ModuleName
    if (-not $module) {
        $module = Import-Module -Name $modulePath -PassThru -ErrorAction Stop |
            Where-Object Name -EQ $requirement.ModuleName
    }
    if ($module.Version -ne [version]$requirement.RequiredVersion) {
        throw "Unexpected YAML module version $($module.Version). Run validation in a fresh PowerShell process."
    }
    Write-Host "Using $($module.Name) $($module.Version)."
}

function ConvertTo-PRValidationAnnotation {
    param([AllowEmptyString()][string]$Text, [switch]$Property)

    $escaped = $Text.Replace('%', '%25').Replace("`r", '%0D').Replace("`n", '%0A')
    if ($Property) {
        $escaped = $escaped.Replace(':', '%3A').Replace(',', '%2C')
    }
    return $escaped
}

function ConvertTo-PRValidationHtml {
    param([AllowEmptyString()][string]$Text)

    return [System.Net.WebUtility]::HtmlEncode($Text).Replace('|', '&#124;')
}

function Invoke-PRSpellingValidation {
    param([hashtable]$Snapshot, [string]$RepositoryRoot, [string]$OutputDirectory)

    $files = @($Snapshot.ChangedFiles)
    if ($files.Count -eq 0) {
        return @{ Status = 'NotApplicable'; Messages = @('No added or modified files to spell-check.'); Packages = @() }
    }
    $paths = @(
        foreach ($file in $files) {
            if ($file.Contains("`n") -or $file.Contains("`r")) {
                throw "CSpell's line-delimited file list cannot represent $(ConvertTo-Json $file -Compress). Rename the file."
            }
            $path = Join-Path $RepositoryRoot $file
            if (-not (Test-Path -LiteralPath $path -PathType Leaf)) {
                throw "Changed spelling input is missing: $file"
            }
            $path
        }
    )
    $cache = Join-Path $OutputDirectory 'cspell'
    $manifestDirectory = Join-Path $RepositoryRoot 'eng' 'common' 'spelling'
    $lockPath = Join-Path $manifestDirectory 'package-lock.json'
    $lock = Get-Content -LiteralPath $lockPath -Raw | ConvertFrom-Json -AsHashtable
    $version = $lock.packages['node_modules/cspell'].version
    if ([string]::IsNullOrWhiteSpace($version)) {
        throw "CSpell version is missing from $lockPath."
    }
    $binary = Join-Path $cache 'node_modules' 'cspell' 'bin.mjs'
    if (-not (Test-Path -LiteralPath $binary -PathType Leaf)) {
        New-Item -ItemType Directory -Path $cache -Force | Out-Null
        Copy-Item -LiteralPath (Join-Path $manifestDirectory 'package.json'), $lockPath -Destination $cache
        & npm --prefix $cache ci --ignore-scripts --no-audit --no-fund --registry=https://registry.npmjs.org | Out-Host
        if ($LASTEXITCODE -ne 0) {
            throw "Pinned CSpell npm ci failed with exit code $LASTEXITCODE."
        }
    }
    $installed = Get-Content -LiteralPath (Join-Path $cache 'node_modules' 'cspell' 'package.json') -Raw |
        ConvertFrom-Json
    if ($installed.version -cne $version -or
        (Get-FileHash -LiteralPath $lockPath).Hash -ne
        (Get-FileHash -LiteralPath (Join-Path $cache 'package-lock.json')).Hash) {
        throw "CSpell cache does not match the CI lockfile. Use a fresh output directory: $cache"
    }
    $node = (Get-Command node -CommandType Application -ErrorAction Stop | Select-Object -First 1).Source
    $nodeVersion = Invoke-PRValidationProcess -FilePath $node -WorkingDirectory $RepositoryRoot -Arguments @('--version')
    if ($nodeVersion.ExitCode -ne 0) { throw "Node version probe failed: $($nodeVersion.Error)" }
    Write-Host "Using locked CSpell $version with $($nodeVersion.Output.Trim()); $($paths.Count) changed files before CSpell ignores."
    $result = Invoke-PRValidationProcess -FilePath $node -WorkingDirectory $RepositoryRoot -Arguments @(
        $binary, 'lint', '--config', (Join-Path $RepositoryRoot '.vscode' 'cspell.json'),
        '--no-must-find-files', '--root', $RepositoryRoot, '--file-list', 'stdin'
    ) -InputText (($paths -join "`n") + "`n")
    Write-Host $result.Output
    Write-Host $result.Error
    return @{
        Status = if ($result.ExitCode -eq 0) { 'Passed' } else { 'Failed' }
        Messages = @(
            "CSpell $version exited $($result.ExitCode); $($paths.Count) changed files before configured ignores."
            if ($result.ExitCode -ne 0) {
                $result.Output.Trim()
                $result.Error.Trim()
                'Correct spelling or update the existing CSpell configuration: https://aka.ms/azsdk/engsys/spellcheck'
            }
        )
        Packages = @()
    }
}

function Get-PRPackageDiscoveryErrors {
    param([string]$RepositoryRoot, [string[]]$Paths, [string]$SdkType)

    $services = @($Paths | ForEach-Object {
        if ($_ -cmatch '^sdk/([^/]+)/' -and $Matches[1] -notin @('parents', 'boms')) { $Matches[1] }
    } | Sort-Object -Unique)
    foreach ($service in $services) {
        $directory = Join-Path $RepositoryRoot 'sdk' $service
        if (-not (Test-Path -LiteralPath $directory -PathType Container)) {
            continue
        }
        foreach ($ci in (Get-ChildItem -LiteralPath $directory -Recurse -Depth 2 -File -Filter 'ci*.yml')) {
            try {
                $yaml = LoadFrom-Yaml $ci.FullName
                if (-not $yaml) { throw 'CI YAML is empty or could not be parsed.' }
                $parameters = GetValueSafelyFrom-Yaml $yaml @('extends', 'parameters')
                if (-not $parameters) { throw 'CI YAML is missing extends.parameters.' }
                $ciSdkType = if ($parameters['SDKType']) { $parameters['SDKType'] } else { 'client' }
                if ($ciSdkType -ne $SdkType) { continue }
                if (-not $parameters['ServiceDirectory'] -or -not $parameters['Artifacts']) {
                    throw 'Applicable CI YAML must declare ServiceDirectory and Artifacts.'
                }
                $ciService = [System.IO.Path]::GetRelativePath((Join-Path $RepositoryRoot 'sdk'), $ci.DirectoryName).Replace('\', '/')
                if ($parameters.ServiceDirectory -cne $ciService) {
                    throw "ServiceDirectory '$($parameters.ServiceDirectory)' does not match '$ciService'."
                }
                $poms = @(
                    foreach ($pom in (Get-ChildItem -LiteralPath $ci.DirectoryName -Recurse -Depth 1 -File -Filter 'pom.xml')) {
                        $xml = [xml]::new()
                        $xml.Load($pom.FullName)
                        $properties = @{ Path = $pom.FullName }
                        foreach ($property in @('artifactId', 'groupId', 'version')) {
                            $element = $xml.SelectSingleNode("/*[local-name()='project']/*[local-name()='$property']")
                            $properties[$property] = if ($element) { $element.InnerText } else { '' }
                        }
                        [pscustomobject]$properties
                    }
                )
                foreach ($artifact in $parameters.Artifacts) {
                    if (-not $artifact.name -or -not $artifact.groupId) {
                        throw 'An applicable artifact is missing name or groupId.'
                    }
                    $matches = @($poms | Where-Object {
                        $_.artifactId -eq $artifact.name -and $_.groupId -eq $artifact.groupId
                    })
                    if ($matches.Count -ne 1) {
                        "$($ci.FullName): expected one POM for $($artifact.groupId):$($artifact.name), found $($matches.Count)."
                    }
                    elseif (-not $matches[0].version) {
                        "$($matches[0].Path): missing version for $($artifact.groupId):$($artifact.name)."
                    }
                }
            }
            catch {
                "$($ci.FullName): package discovery failed: $($_.Exception.Message)"
            }
        }
    }
}

function Test-PRPackageChangeLog {
    param([Parameter(Mandatory = $true)]$Package)

    $result = [ordered]@{ Status = 'Failed'; Message = '' }
    foreach ($property in @('Name', 'Group', 'Version', 'SdkType', 'ChangeLogPath')) {
        $result[$property] = if ($Package.PSObject.Properties[$property]) { $Package.$property } else { '' }
    }
    try {
        foreach ($property in @('Name', 'Group', 'Version', 'DirectoryPath', 'SdkType')) {
            if (-not $Package.PSObject.Properties[$property] -or [string]::IsNullOrWhiteSpace($Package.$property)) {
                throw "Selected package metadata is missing $property."
            }
        }
        if ($Package.Name -cnotmatch '^[A-Za-z0-9][A-Za-z0-9._-]*$') {
            throw 'Selected package metadata has an invalid Maven artifact name.'
        }
        if (-not $Package.PSObject.Properties['ArtifactDetails'] -or
            $Package.ArtifactDetails -isnot [System.Collections.IDictionary] -or $Package.ArtifactDetails.Count -eq 0) {
            throw 'Selected package metadata is missing ArtifactDetails; changelog verification was not performed.'
        }
        if ($Package.ArtifactDetails.ContainsKey('skipVerifyChangeLog') -and $Package.ArtifactDetails.skipVerifyChangeLog) {
            $result.Status = 'Skipped'
            $result.Message = 'ArtifactDetails.skipVerifyChangeLog is set, as in Verify-ChangeLogs.ps1.'
            return $result
        }
        if (-not $Package.PSObject.Properties['ChangeLogPath'] -or [string]::IsNullOrWhiteSpace($Package.ChangeLogPath)) {
            throw "Selected package metadata has no ChangeLogPath. Restore the package's CHANGELOG.md."
        }
        $status = [pscustomobject]@{ IsValid = $false; Message = '' }
        $valid = Confirm-ChangeLogEntry -ChangeLogLocation $Package.ChangeLogPath `
            -VersionString $Package.Version -ForRelease $false -ChangeLogStatus $status
        if ($valid -isnot [bool]) {
            throw 'Confirm-ChangeLogEntry did not return a validation result.'
        }
        $result.Status = if ($valid) { 'Passed' } else { 'Failed' }
        $result.Message = $status.Message
    }
    catch {
        $result.Message = $_.Exception.Message
    }
    return $result
}

function Invoke-PRChangeLogValidation {
    param(
        [hashtable]$Snapshot,
        [string]$RepositoryRoot,
        [string]$OutputDirectory,
        [ValidateSet('client', 'data')][string]$SdkType = 'client'
    )

    $paths = @($Snapshot.ChangedFiles) + @($Snapshot.DeletedFiles)
    $result = @{ Status = 'NotApplicable'; Messages = @(); Packages = @(); DiscoveryPerformed = $false }
    if ($paths.Count -eq 0 -or (Test-PRRootDocumentationOnly $paths)) {
        $result.Messages = @('Complete PR diff contains only already-excluded root/docs paths, or no changes. No package metadata needed.')
        return $result
    }
    if ($SdkType -eq 'data') {
        $result.Messages = @('SDKType data does not run the Build changelog check.')
        return $result
    }

    Initialize-PRValidationYaml -DependencyDirectory (Join-Path $OutputDirectory 'modules')
    $previousSdkType = $env:SDKTYPE
    $previousGitHubActions = $env:GITHUB_ACTIONS
    try {
        $env:SDKTYPE = $SdkType
        # PackageProps only populates ArtifactDetails in CI, including local parity runs.
        $env:GITHUB_ACTIONS = 'true'
        . (Join-Path $RepositoryRoot 'eng' 'common' 'scripts' 'common.ps1')
        $pipeline = LoadFrom-Yaml (Join-Path $RepositoryRoot 'eng' 'pipelines' 'pullrequest.yml')
        $exclusions = GetValueSafelyFrom-Yaml $pipeline @('extends', 'parameters', 'ExcludePaths')
        if ($null -eq $exclusions -or $exclusions -isnot [array]) {
            throw 'Cannot read the unified PR package-selection ExcludePaths.'
        }
        $selection = $Snapshot.Clone()
        $selection.ExcludePaths = $exclusions
        $diffPath = Join-Path $OutputDirectory 'selection-diff.json'
        $selection | ConvertTo-Json -Depth 30 | Set-Content -LiteralPath $diffPath
        $targetedPaths = Update-TargetedFilesForExclude $paths $exclusions
        $result.DiscoveryPerformed = $true
        $result.Messages = @(Get-PRPackageDiscoveryErrors $RepositoryRoot $targetedPaths $SdkType)
        # Preserve Build's template fallback even when ExcludePaths removes every targeted path.
        $packages = @(Get-PrPkgProperties $diffPath)
        if ($packages.Count -eq 0) {
            throw 'Package discovery returned no packages, including no template fallback. Validation is unavailable.'
        }

        # Match Save-Package-Properties' preference for track 2 when exported names collide.
        $exported = [ordered]@{}
        foreach ($package in $packages) {
            if ($exported.Contains($package.Name) -and $exported[$package.Name].IsNewSdk) { continue }
            $exported[$package.Name] = $package
        }
        $packageDirectory = Join-Path $OutputDirectory 'PackageInfo'
        New-Item -ItemType Directory -Path $packageDirectory -Force | Out-Null
        foreach ($package in $exported.Values) {
            $result.Packages += Test-PRPackageChangeLog $package
            if ($package.Name -cmatch '^[A-Za-z0-9][A-Za-z0-9._-]*$') {
                $metadata = $package | ConvertTo-Json -Depth 100 | ConvertFrom-Json -AsHashtable
                foreach ($property in @('DirectoryPath', 'ReadMePath', 'ChangeLogPath')) {
                    if ($metadata[$property] -and [System.IO.Path]::IsPathRooted($metadata[$property])) {
                        $metadata[$property] = [System.IO.Path]::GetRelativePath($RepositoryRoot, $metadata[$property]).Replace('\', '/')
                    }
                }
                $metadata | ConvertTo-Json -Depth 100 |
                    Set-Content -LiteralPath (Join-Path $packageDirectory "$($package.Name).json")
            }
        }
        $result.Status = if ($result.Messages.Count -gt 0 -or @($result.Packages | Where-Object Status -EQ 'Failed').Count -gt 0) {
            'Failed'
        }
        else {
            'Passed'
        }
        $result.Messages += "Validated unified PR selection (SDKType $SdkType): $($result.Packages.Count) package result(s). Azure Build verification remains enabled."
        return $result
    }
    catch {
        $result.Status = 'Failed'
        $result.Messages += "Package discovery or validation failed: $($_.Exception.Message)"
        return $result
    }
    finally {
        $env:SDKTYPE = $previousSdkType
        $env:GITHUB_ACTIONS = $previousGitHubActions
    }
}
