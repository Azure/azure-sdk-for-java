# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

Set-StrictMode -Version 3

$script:JavaDocEligibleReasons = @('javadoc-only', 'whitespace-only', 'ordinary-comment-only', 'non-code-only')
$script:JavaDocKnownReasons = $script:JavaDocEligibleReasons + @(
    'no-source-edit', 'non-documentation-change', 'parse-error', 'unsupported-unicode-escape',
    'unsupported-documentation', 'documentation-attachment-change', 'unsupported-comment-directive'
)

function Start-JavaDocProcess {
    param(
        [Parameter(Mandatory)][string]$FilePath,
        [Parameter(Mandatory)][string[]]$Arguments,
        [Parameter(Mandatory)][string]$WorkingDirectory,
        [switch]$RedirectInput
    )

    $startInfo = [System.Diagnostics.ProcessStartInfo]::new()
    $startInfo.FileName = $FilePath
    $startInfo.WorkingDirectory = $WorkingDirectory
    $startInfo.UseShellExecute = $false
    $startInfo.RedirectStandardOutput = $true
    $startInfo.RedirectStandardError = $true
    $startInfo.RedirectStandardInput = $RedirectInput.IsPresent
    if ($RedirectInput) {
        $startInfo.StandardInputEncoding = [System.Text.UTF8Encoding]::new($false)
    }
    $startInfo.StandardOutputEncoding = [System.Text.UTF8Encoding]::new($false, $true)
    $startInfo.StandardErrorEncoding = [System.Text.UTF8Encoding]::new($false, $true)
    foreach ($argument in $Arguments) {
        $startInfo.ArgumentList.Add($argument)
    }
    $process = [System.Diagnostics.Process]::new()
    $process.StartInfo = $startInfo
    try {
        if (-not $process.Start()) {
            throw "Could not start $FilePath."
        }
        return $process
    } catch {
        $process.Dispose()
        throw
    }
}

function Invoke-JavaDocProcess {
    param(
        [Parameter(Mandatory)][string]$FilePath,
        [Parameter(Mandatory)][string[]]$Arguments,
        [Parameter(Mandatory)][string]$WorkingDirectory,
        [int]$TimeoutSeconds = 120,
        [switch]$BinaryOutput
    )

    $process = Start-JavaDocProcess -FilePath $FilePath -Arguments $Arguments -WorkingDirectory $WorkingDirectory
    $bytes = [System.IO.MemoryStream]::new()
    try {
        $stdout = if ($BinaryOutput) {
            $process.StandardOutput.BaseStream.CopyToAsync($bytes)
        } else {
            $process.StandardOutput.ReadToEndAsync()
        }
        $stderr = $process.StandardError.ReadToEndAsync()
        if (-not $process.WaitForExit($TimeoutSeconds * 1000)) {
            $process.Kill($true)
            $process.WaitForExit()
            throw "$FilePath exceeded the $TimeoutSeconds second timeout."
        }
        $output = $stdout.GetAwaiter().GetResult()
        $errorOutput = $stderr.GetAwaiter().GetResult()
        if ($process.ExitCode -ne 0) {
            $details = "$errorOutput`n$output"
            if ($details.Length -gt 8000) {
                $details = $details.Substring($details.Length - 8000)
            }
            throw "$FilePath exited with code $($process.ExitCode): $details"
        }
        if ($BinaryOutput) {
            return ,$bytes.ToArray()
        }
        return $output
    } finally {
        $process.Dispose()
        $bytes.Dispose()
    }
}

function Invoke-JavaDocGit {
    param([string]$RepositoryRoot, [string[]]$Arguments)

    return Invoke-JavaDocProcess -FilePath 'git' -WorkingDirectory $RepositoryRoot `
        -Arguments (@('--no-pager', '--no-replace-objects', '-c', 'core.quotepath=false') + $Arguments)
}

function Get-JavaDocMergeSnapshot {
    param(
        [string]$RepositoryRoot,
        [string]$ExpectedHeadSha,
        [string]$ExpectedSourceSha
    )

    if ($ExpectedHeadSha -cnotmatch '^[0-9a-f]{40}$') {
        throw 'An exact 40-character merge commit SHA is required.'
    }
    $head = (Invoke-JavaDocGit $RepositoryRoot @('rev-parse', '--verify', 'HEAD^{commit}')).Trim()
    if ($head -cne $ExpectedHeadSha) {
        throw 'The checked-out HEAD does not match the requested merge commit.'
    }
    $parents = (Invoke-JavaDocGit $RepositoryRoot @('rev-list', '--parents', '-n', '1', $head)).Trim().Split(' ')
    if ($parents.Count -ne 3 -or $parents[0] -cne $head) {
        throw 'A complete two-parent synthetic merge commit is required.'
    }
    foreach ($parent in $parents[1..2]) {
        $null = Invoke-JavaDocGit $RepositoryRoot @('cat-file', '-e', "$parent`^{commit}")
    }
    if ($ExpectedSourceSha -and $parents[2] -cne $ExpectedSourceSha) {
        throw 'The second parent does not match the expected PR source commit.'
    }

    $raw = Invoke-JavaDocGit $RepositoryRoot @(
        'diff', '--raw', '-z', '--no-abbrev', '--no-renames', '--no-ext-diff', '--no-textconv',
        '--no-relative', '--ignore-submodules=none',
        $parents[1], $head, '--'
    )
    $changes = @()
    if ($raw.Length -gt 0) {
        $fields = $raw.Split([char]0)
        if ($fields[-1] -cne '' -or ($fields.Count - 1) % 2 -ne 0) {
            throw 'The NUL-delimited Git diff is incomplete.'
        }
        for ($i = 0; $i -lt $fields.Count - 1; $i += 2) {
            if ($fields[$i] -cnotmatch '^:(\d{6}) (\d{6}) ([0-9a-f]{40}) ([0-9a-f]{40}) ([A-Z])$') {
                throw 'The Git diff contains unsupported metadata.'
            }
            $metadata = $Matches.Clone()
            $path = $fields[$i + 1]
            if (-not $path -or $path -cmatch '[\x00-\x1f\x7f\\]' -or
                $path.StartsWith('/') -or $path.Split('/') -contains '..') {
                throw 'The Git diff contains an unsupported repository path.'
            }
            $changes += [PSCustomObject]@{
                Path = $path
                OldMode = $metadata[1]
                NewMode = $metadata[2]
                OldBlob = $metadata[3]
                NewBlob = $metadata[4]
                Status = $metadata[5]
            }
        }
    }
    return [PSCustomObject]@{
        HeadSha = $head
        BaseSha = $parents[1]
        SourceSha = $parents[2]
        Changes = $changes
    }
}

function Get-JavaDocModuleClassification {
    param([string]$RepositoryRoot, [string]$HeadSha, [string]$ModulePath)

    $pomPath = "$ModulePath/pom.xml"
    $entry = Invoke-JavaDocGit $RepositoryRoot @('ls-tree', '--full-tree', '-z', $HeadSha, '--', $pomPath)
    if ($entry -cnotmatch '^100644 blob ([0-9a-f]{40})\t([^\x00]+)\x00$' -or $Matches[2] -cne $pomPath) {
        throw "Cannot classify $ModulePath without a regular POM in the PR snapshot."
    }
    $blob = $Matches[1]
    $size = (Invoke-JavaDocGit $RepositoryRoot @('cat-file', '-s', $blob)).Trim()
    if ($size -cnotmatch '^\d+$' -or [long]$size -gt 1048576) {
        throw "The POM for $ModulePath exceeds the supported metadata size."
    }
    $bytes = Invoke-JavaDocProcess -FilePath 'git' -WorkingDirectory $RepositoryRoot -BinaryOutput `
        -Arguments @('--no-pager', '--no-replace-objects', 'cat-file', 'blob', $blob)
    $stream = [System.IO.MemoryStream]::new($bytes, $false)
    $settings = [System.Xml.XmlReaderSettings]::new()
    $settings.DtdProcessing = [System.Xml.DtdProcessing]::Prohibit
    $settings.XmlResolver = $null
    $settings.MaxCharactersInDocument = 1048576
    $reader = [System.Xml.XmlReader]::Create($stream, $settings)
    try {
        $pom = [System.Xml.XmlDocument]::new()
        $pom.XmlResolver = $null
        $pom.Load($reader)
    } finally {
        $reader.Dispose()
        $stream.Dispose()
    }
    $project = $pom.DocumentElement
    if ($project.LocalName -cne 'project' -or
        $project.NamespaceURI -cnotin @('', 'http://maven.apache.org/POM/4.0.0')) {
        throw "The POM for $ModulePath has an unsupported root element."
    }
    $namespaces = [System.Xml.XmlNamespaceManager]::new($pom.NameTable)
    $namespaces.AddNamespace('m', $project.NamespaceURI)
    $values = @{}
    foreach ($field in @('groupId', 'artifactId', 'packaging', 'parent/groupId', 'parent/artifactId')) {
        $query = if ($project.NamespaceURI) {
            ($field.Split('/') | ForEach-Object { "m:$_" }) -join '/'
        } else {
            $field
        }
        $nodes = $project.SelectNodes($query, $namespaces)
        if ($nodes.Count -gt 1) {
            throw "The POM for $ModulePath declares $field more than once."
        }
        $values[$field] = if ($nodes.Count -eq 1) { $nodes[0].InnerText.Trim() } else { '' }
    }
    $group = if ($values.groupId) { $values.groupId } else { $values['parent/groupId'] }
    $artifact = $values.artifactId
    $parent = "$($values['parent/groupId']):$($values['parent/artifactId'])"
    # Match the Azure client groups and Track 2 parent family used by the repository's metadata/build tools.
    $isTrack2DataPlane = $group -cin @('com.azure', 'com.azure.v2') -and
        $artifact -cmatch '^[A-Za-z0-9][A-Za-z0-9._-]*$' -and $artifact -notmatch 'mgmt|resourcemanager|spring' -and
        $parent -cin @('com.azure:azure-client-sdk-parent', 'com.azure.v2:azure-client-sdk-parent') -and
        $values.packaging -cin @('', 'jar')
    return [PSCustomObject]@{
        IsTrack2DataPlane = $isTrack2DataPlane
        GroupId = $group
        ArtifactId = $artifact
    }
}

function Get-JavaDocSource {
    param([string]$RepositoryRoot, [string]$Blob)

    $size = (Invoke-JavaDocGit $RepositoryRoot @('cat-file', '-s', $Blob)).Trim()
    if ($size -cnotmatch '^\d+$') {
        throw 'Git returned an invalid source size.'
    }
    if ([long]$size -gt 2MB) {
        return $null
    }
    $bytes = Invoke-JavaDocProcess -FilePath 'git' -WorkingDirectory $RepositoryRoot -BinaryOutput `
        -Arguments @('--no-pager', '--no-replace-objects', 'cat-file', 'blob', $Blob)
    $source = [System.Text.UTF8Encoding]::new($false, $true).GetString($bytes)
    if ($source.StartsWith([string][char]0xfeff, [System.StringComparison]::Ordinal)) {
        throw 'A byte-order mark in Java source is unsupported.'
    }
    return $source
}

function Resolve-JavaDocParserJar {
    param([string]$ParserJar)

    $toolDirectory = Join-Path $PSScriptRoot '..' '..' 'java-doc-classifier'
    if (-not $ParserJar) {
        $buildCommand = '$ErrorActionPreference = "Stop"; & mvn -B --no-transfer-progress -q -f pom.xml package -DskipTests; exit $LASTEXITCODE'
        $null = Invoke-JavaDocProcess -FilePath (Get-Process -Id $PID).Path -Arguments @(
            '-NoLogo', '-NoProfile', '-NonInteractive', '-Command', $buildCommand
        ) -WorkingDirectory $toolDirectory -TimeoutSeconds 600
        $ParserJar = Join-Path $toolDirectory 'target' 'java-doc-classifier.jar'
    }
    if (-not (Test-Path -LiteralPath $ParserJar -PathType Leaf)) {
        throw 'The Java documentation parser JAR is missing.'
    }
    return (Resolve-Path -LiteralPath $ParserJar).Path
}

function Wait-JavaDocComparisonOperation {
    param(
        [System.Threading.Tasks.Task]$Task,
        [System.Diagnostics.Stopwatch]$Timer,
        [int]$TimeoutSeconds
    )

    $remaining = [int]($TimeoutSeconds * 1000 - $Timer.ElapsedMilliseconds)
    if ($remaining -le 0 -or -not $Task.Wait($remaining)) {
        throw "Java documentation comparison exceeded the $TimeoutSeconds second timeout."
    }
    return $Task.GetAwaiter().GetResult()
}

function Invoke-JavaDocComparisons {
    param(
        [string]$RepositoryRoot,
        [object[]]$Candidates,
        [string]$ParserJar,
        [ValidateRange(1, 120)][int]$TimeoutSeconds = 120
    )

    $process = $null
    $candidate = $null
    $comparisonTimer = [System.Diagnostics.Stopwatch]::StartNew()
    $blockedModules = [System.Collections.Generic.HashSet[string]]::new([System.StringComparer]::Ordinal)
    $sequence = 0
    try {
        foreach ($candidate in $Candidates) {
            $module = $candidate.File.Module
            if ($blockedModules.Contains($module)) {
                continue
            }
            if ($comparisonTimer.IsRunning -and $comparisonTimer.Elapsed.TotalSeconds -ge $TimeoutSeconds) {
                throw "Java documentation comparison exceeded the $TimeoutSeconds second timeout."
            }
            try {
                $before = Get-JavaDocSource $RepositoryRoot $candidate.Change.OldBlob
                $after = if ($null -ne $before) {
                    Get-JavaDocSource $RepositoryRoot $candidate.Change.NewBlob
                } else {
                    $null
                }
            } catch {
                $candidate.File.Reason = 'source-read-error'
                $candidate.File.Error = $_.Exception.Message
                $null = $blockedModules.Add($module)
                continue
            }
            if ($null -eq $before -or $null -eq $after) {
                $candidate.File.Reason = 'source-size-limit-exceeded'
                $null = $blockedModules.Add($module)
                continue
            }
            if (-not $process) {
                $comparisonTimer.Stop()
                try {
                    $jar = Resolve-JavaDocParserJar -ParserJar $ParserJar
                } finally {
                    $comparisonTimer.Start()
                }
                $process = Start-JavaDocProcess -FilePath 'java' -Arguments @('-jar', $jar, '--stdio') `
                    -WorkingDirectory $RepositoryRoot -RedirectInput
                $process.StandardInput.AutoFlush = $true
                $stderr = $process.StandardError.ReadToEndAsync()
            }
            $oldContent = [Convert]::ToBase64String([System.Text.Encoding]::UTF8.GetBytes($before))
            $newContent = [Convert]::ToBase64String([System.Text.Encoding]::UTF8.GetBytes($after))
            $null = Wait-JavaDocComparisonOperation `
                $process.StandardInput.WriteLineAsync("$sequence`t$oldContent`t$newContent") `
                $comparisonTimer $TimeoutSeconds
            $line = Wait-JavaDocComparisonOperation $process.StandardOutput.ReadLineAsync() `
                $comparisonTimer $TimeoutSeconds
            if ($null -eq $line) {
                throw 'The Java documentation parser returned an incomplete result set.'
            }
            $fields = $line.Split("`t")
            if ($fields.Count -ne 2 -or $fields[0] -cne [string]$sequence -or
                $fields[1] -cnotin $script:JavaDocKnownReasons) {
                throw 'The Java documentation parser returned an invalid result.'
            }
            $candidate.File.Reason = $fields[1]
            $candidate.File.Compared = $true
            $sequence++
            if ($fields[1] -cnotin $script:JavaDocEligibleReasons) {
                $null = $blockedModules.Add($module)
            }
        }
        if ($process) {
            $process.StandardInput.Close()
            $remainingOutput = $process.StandardOutput.ReadToEndAsync()
            $null = Wait-JavaDocComparisonOperation $process.WaitForExitAsync() $comparisonTimer $TimeoutSeconds
            $extraOutput = Wait-JavaDocComparisonOperation $remainingOutput $comparisonTimer $TimeoutSeconds
            $errorOutput = Wait-JavaDocComparisonOperation $stderr $comparisonTimer $TimeoutSeconds
            if ($process.ExitCode -ne 0) {
                throw "The Java documentation parser exited with code $($process.ExitCode): $errorOutput"
            }
            if ($extraOutput.Length -ne 0) {
                throw 'The Java documentation parser returned unexpected extra output.'
            }
        }
    } catch {
        if ($candidate) {
            $candidate.File.Reason = 'comparison-error'
        }
        throw
    } finally {
        if ($process) {
            if (-not $process.HasExited) {
                $process.Kill($true)
                $process.WaitForExit()
            }
            $process.Dispose()
        }
        $comparisonTimer.Stop()
    }
}

function Get-JavaDocLibraryRoot {
    param([string]$RepositoryRoot, [string]$HeadSha, [string]$BaseSha, [string]$ModulePath)

    if ($ModulePath -cmatch '^sdk/(parents|boms|tools)/') {
        return $null
    }
    $pomPath = "$ModulePath/pom.xml"
    foreach ($revision in @($HeadSha, $BaseSha)) {
        $entry = Invoke-JavaDocGit $RepositoryRoot @('ls-tree', '--full-tree', '-z', $revision, '--', $pomPath)
        if ($entry -cmatch '^100644 blob [0-9a-f]{40}\t([^\x00]+)\x00$' -and $Matches[1] -ceq $pomPath) {
            return $ModulePath
        }
    }
    return $null
}

function Write-JavaDocReportWarning {
    param([string]$Message)

    $logError = $Message.Replace("`r", ' ').Replace("`n", ' ').Replace('##vso[', '##vso%5B')
    Write-Warning "Java documentation classification is inconclusive: $logError"
}

function Complete-JavaDocLibraryReport {
    param($Library, [string]$ComparisonError)

    $Library.ComparedFileCount = @($Library.Files | Where-Object Compared).Count
    if ($Library.Decision -ceq 'Pending') {
        $failedFile = $Library.Files | Where-Object {
            $_.Reason -cnotin ($script:JavaDocEligibleReasons + @(
                'pending-source-comparison', 'existing-non-runtime-validation'
            ))
        } | Select-Object -First 1
        if ($ComparisonError) {
            $Library.Decision = 'Inconclusive'
            $Library.Reason = 'classifier-error'
            $Library.Error = $ComparisonError
        } elseif ($failedFile) {
            $Library.DecisionFile = $failedFile.Path
            switch -CaseSensitive ($failedFile.Reason) {
                'parse-error' {
                    $Library.Decision = 'Inconclusive'
                    $Library.Reason = 'source-parse-error'
                }
                'source-read-error' {
                    $Library.Decision = 'Inconclusive'
                    $Library.Reason = 'source-read-error'
                    $Library.Error = $failedFile.Error
                    Write-JavaDocReportWarning $Library.Error
                }
                'source-size-limit-exceeded' {
                    $Library.Decision = 'NotEligible'
                    $Library.Reason = 'source-size-limit-exceeded'
                }
                default {
                    $Library.Decision = 'NotEligible'
                    $Library.Reason = 'source-comparison-rejected'
                }
            }
        } elseif ($Library.ComparedFileCount -ne $Library.CandidateFileCount) {
            $Library.Decision = 'Inconclusive'
            $Library.Reason = 'classifier-error'
            $Library.Error = 'The Java documentation parser returned an incomplete result set.'
            Write-JavaDocReportWarning $Library.Error
        } else {
            $Library.Decision = 'Eligible'
            $Library.Reason = if ($Library.CandidateFileCount -gt 0) {
                'supported-non-code-changes'
            } else {
                'existing-non-runtime-validation'
            }
        }
    }
    if (-not $Library.DecisionFile) {
        $decisionFile = $Library.Files | Where-Object {
            $_.Reason -cnotin ($script:JavaDocEligibleReasons + @(
                'pending-source-comparison', 'existing-non-runtime-validation'
            ))
        } | Select-Object -First 1
        if ($decisionFile) {
            $Library.DecisionFile = $decisionFile.Path
        }
    }
}

function Get-JavaDocChangeReport {
    [CmdletBinding()]
    param(
        [string]$RepositoryRoot,
        [string]$ExpectedHeadSha,
        [string]$ExpectedSourceSha,
        [string]$ParserJar,
        [switch]$ForceFullValidation,
        [switch]$Pipeline
    )

    $timer = [System.Diagnostics.Stopwatch]::StartNew()
    $result = [PSCustomObject][ordered]@{
        SchemaVersion = 2
        Mode = 'report-only'
        Decision = 'NotEligible'
        Reason = 'not-evaluated'
        SuppressionApplied = $false
        WouldSuppressTests = $false
        ForceFullValidation = [bool]$ForceFullValidation
        HeadSha = $ExpectedHeadSha
        BaseSha = $null
        SourceSha = $null
        ChangedFileCount = 0
        CandidateFileCount = 0
        ComparedFileCount = 0
        DecisionFile = $null
        DurationMilliseconds = 0
        Error = $null
        LibraryScope = 'track2-data-plane'
        DependencyImpactEvaluated = $false
        EligibleLibraryCount = 0
        Libraries = @()
        SharedChanges = @()
        Files = @()
    }
    try {
        if ($ForceFullValidation) {
            $result.Reason = 'forced-full-validation'
            return $result
        }
        if ($Pipeline -and ($env:BUILD_REASON -cne 'PullRequest' -or
            $env:SYSTEM_TEAMPROJECT -cne 'public' -or
            $env:BUILD_REPOSITORY_NAME -cne 'Azure/azure-sdk-for-java' -or
            $env:BUILD_SOURCEVERSION -cne $ExpectedHeadSha -or
            $env:BUILD_SOURCEBRANCH -cnotmatch '^refs/pull/[0-9]+/merge$' -or
            $env:SYSTEM_PULLREQUEST_TARGETBRANCH -cnotin @('main', 'refs/heads/main') -or
            $ExpectedSourceSha -cnotmatch '^[0-9a-f]{40}$' -or
            $env:SYSTEM_PULLREQUEST_SOURCECOMMITID -cne $ExpectedSourceSha)) {
            $result.Reason = 'unsupported-pipeline-context'
            return $result
        }
        $snapshot = Get-JavaDocMergeSnapshot $RepositoryRoot $ExpectedHeadSha $ExpectedSourceSha
        $result.BaseSha = $snapshot.BaseSha
        $result.SourceSha = $snapshot.SourceSha
        $result.ChangedFileCount = $snapshot.Changes.Count
        if ($snapshot.Changes.Count -eq 0) {
            $result.Reason = 'no-changes'
            return $result
        }
        # Reuse path policy without publishing the legacy script's RunTests/RunBuild output variables.
        $legacyClassifier = Join-Path $PSScriptRoot '..' 'Classify-PRChanges.ps1'
        $pathClassification = & $legacyClassifier -ChangedFiles $snapshot.Changes.Path -PassThru 6>$null
        $candidates = @()
        $owners = [System.Collections.Generic.Dictionary[string, object]]::new([System.StringComparer]::Ordinal)
        $libraries = [System.Collections.Generic.Dictionary[string, object]]::new([System.StringComparer]::Ordinal)
        foreach ($change in $snapshot.Changes) {
            $file = [PSCustomObject]@{
                Path = $change.Path
                Status = $change.Status
                Module = $null
                ServiceDirectory = $null
                SharedScope = $null
                MavenCoordinates = $null
                Compared = $false
                Error = $null
                Reason = 'unsupported-path'
            }
            $result.Files += $file
            if ($change.Path -cmatch '^sdk/([^/]+)/') {
                $file.ServiceDirectory = $Matches[1]
            }
            if ($change.Path -cmatch '^(sdk/[^/]+/[^/]+)/') {
                $module = $Matches[1]
                if (-not $owners.ContainsKey($module)) {
                    $owners[$module] = Get-JavaDocLibraryRoot $RepositoryRoot $snapshot.HeadSha `
                        $snapshot.BaseSha $module
                }
                $file.Module = $owners[$module]
            }
            if ($file.Module) {
                if (-not $libraries.ContainsKey($file.Module)) {
                    $library = [PSCustomObject][ordered]@{
                        Module = $file.Module
                        MavenCoordinates = $null
                        Decision = 'Pending'
                        Reason = 'not-evaluated'
                        CandidateFileCount = 0
                        ComparedFileCount = 0
                        DecisionFile = $null
                        Error = $null
                        Files = @()
                    }
                    $libraries[$file.Module] = $library
                    $result.Libraries += $library
                }
                $libraries[$file.Module].Files += $file
            }
            $pathResult = @($pathClassification.Paths | Where-Object { $_.Path -ceq $change.Path })
            if ($pathResult.Count -eq 1 -and -not $pathResult[0].RequiresJavaTests) {
                $file.Reason = 'existing-non-runtime-validation'
                continue
            }
            if (-not $file.Module -or $change.Path -cnotmatch '^sdk/[^/]+/[^/]+/src/main/java/.+\.java$') {
                continue
            }
            if ($change.Status -cne 'M' -or $change.OldMode -cne '100644' -or $change.NewMode -cne '100644') {
                $file.Reason = 'unsupported-source-status'
                continue
            }
            if ([System.IO.Path]::GetFileName($change.Path) -cin @('package-info.java', 'module-info.java')) {
                $file.Reason = 'unsupported-source-layout'
                continue
            }
            $file.Reason = 'pending-source-comparison'
            $libraries[$file.Module].CandidateFileCount++
            $candidates += [PSCustomObject]@{ Change = $change; File = $file }
        }
        $result.CandidateFileCount = $candidates.Count
        $result.SharedChanges = @($result.Files | Where-Object {
            -not $_.Module -and $_.Reason -cne 'existing-non-runtime-validation'
        })
        foreach ($sharedChange in $result.SharedChanges) {
            $sharedChange.SharedScope = if ($sharedChange.ServiceDirectory -and
                $sharedChange.ServiceDirectory -cnotin @('parents', 'boms', 'tools')) { 'service' } else { 'repository' }
        }
        $repositoryChanges = @($result.SharedChanges | Where-Object SharedScope -CEQ 'repository')
        if ($repositoryChanges.Count -gt 0) {
            foreach ($library in $result.Libraries) {
                $library.Decision = 'NotEligible'
                $library.Reason = 'shared-input-change'
                $library.DecisionFile = $repositoryChanges[0].Path
            }
            $result.Reason = 'unsupported-or-mixed-changes'
            return $result
        }
        if ($result.Libraries.Count -eq 0) {
            $result.Reason = if ($result.SharedChanges.Count -gt 0) {
                'unsupported-or-mixed-changes'
            } else {
                'no-java-candidates'
            }
            return $result
        }
        foreach ($library in $result.Libraries) {
            $service = $library.Module.Split('/')[1]
            $serviceChange = $result.SharedChanges | Where-Object {
                $_.SharedScope -ceq 'service' -and $_.ServiceDirectory -ceq $service
            } | Select-Object -First 1
            if ($serviceChange) {
                $library.Decision = 'NotEligible'
                $library.Reason = 'shared-input-change'
                $library.DecisionFile = $serviceChange.Path
                continue
            }
            $triggeringFile = $library.Files | Where-Object {
                $_.Reason -cnotin @('pending-source-comparison', 'existing-non-runtime-validation')
            } | Select-Object -First 1
            if ($triggeringFile) {
                $library.Decision = 'NotEligible'
                $library.Reason = 'unsupported-or-mixed-changes'
                $library.DecisionFile = $triggeringFile.Path
                continue
            }
            try {
                $metadata = Get-JavaDocModuleClassification $RepositoryRoot $snapshot.HeadSha $library.Module
                $library.MavenCoordinates = "$($metadata.GroupId):$($metadata.ArtifactId)"
                foreach ($file in $library.Files) {
                    $file.MavenCoordinates = $library.MavenCoordinates
                }
            } catch {
                $library.Decision = 'Inconclusive'
                $library.Reason = 'classifier-error'
                $library.Error = $_.Exception.Message
                Write-JavaDocReportWarning $library.Error
                continue
            }
            if (-not $metadata.IsTrack2DataPlane) {
                $library.Decision = 'NotEligible'
                $library.Reason = 'unsupported-library-kind'
                foreach ($file in $library.Files | Where-Object Reason -CEQ 'pending-source-comparison') {
                    $file.Reason = 'not-track2-data-plane'
                }
            }
        }
        $readyCandidates = @($candidates | Where-Object { $libraries[$_.File.Module].Decision -ceq 'Pending' })
        $comparisonError = $null
        if ($readyCandidates.Count -gt 0) {
            try {
                Invoke-JavaDocComparisons -RepositoryRoot $RepositoryRoot -Candidates $readyCandidates -ParserJar $ParserJar
            } catch {
                $comparisonError = $_.Exception.Message
                $result.Error = $comparisonError
                Write-JavaDocReportWarning $comparisonError
            }
        }
        foreach ($library in $result.Libraries) {
            $errorForLibrary = if ($library.CandidateFileCount -gt 0) { $comparisonError } else { $null }
            Complete-JavaDocLibraryReport $library $errorForLibrary
        }
        $result.EligibleLibraryCount = @($result.Libraries | Where-Object Decision -CEQ 'Eligible').Count
        if ($result.EligibleLibraryCount -eq $result.Libraries.Count -and $result.SharedChanges.Count -eq 0) {
            $result.Decision = 'Eligible'
            $result.Reason = 'supported-non-code-changes'
            $result.WouldSuppressTests = $true
        } elseif ($result.EligibleLibraryCount -gt 0) {
            $result.Decision = 'PartiallyEligible'
            $result.Reason = 'mixed-library-results'
        } elseif ($result.Libraries.Count -eq 1) {
            $result.Decision = $result.Libraries[0].Decision
            $result.Reason = $result.Libraries[0].Reason
            $result.Error = $result.Libraries[0].Error
        } elseif (@($result.Libraries | Where-Object Decision -CEQ 'Inconclusive').Count -gt 0) {
            $result.Decision = 'Inconclusive'
            $result.Reason = 'library-evaluation-incomplete'
        } else {
            $result.Reason = 'no-eligible-libraries'
        }
        return $result
    } catch {
        # Reporting must not replace existing validation with a failed or partial comparison.
        $result.Decision = 'Inconclusive'
        $result.Reason = 'classifier-error'
        $result.Error = $_.Exception.Message
        Write-JavaDocReportWarning $result.Error
        return $result
    } finally {
        $result.ComparedFileCount = @($result.Files | Where-Object Compared).Count
        foreach ($library in $result.Libraries) {
            if ($library.Decision -ceq 'Pending') {
                $library.Decision = 'Inconclusive'
                $library.Reason = 'classifier-error'
                $library.Error = $result.Error
            }
            Complete-JavaDocLibraryReport $library
        }
        $decisionFile = $result.Files | Where-Object {
            $_.Reason -cnotin ($script:JavaDocEligibleReasons + @(
                'pending-source-comparison', 'existing-non-runtime-validation'
            ))
        } | Select-Object -First 1
        if ($decisionFile) {
            $result.DecisionFile = $decisionFile.Path
        } else {
            $libraryDecision = $result.Libraries | Where-Object DecisionFile | Select-Object -First 1
            if ($libraryDecision) {
                $result.DecisionFile = $libraryDecision.DecisionFile
            }
        }
        foreach ($file in $result.Files) {
            if ($file.Reason -ceq 'pending-source-comparison') {
                $file.Reason = 'not-evaluated'
            }
        }
        $timer.Stop()
        $result.DurationMilliseconds = $timer.ElapsedMilliseconds
    }
}
