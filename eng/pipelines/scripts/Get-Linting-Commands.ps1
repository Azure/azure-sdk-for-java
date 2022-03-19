<#
.SYNOPSIS
Determines the Checkstyle, RevApi, and Spotbugs linting commands that should be ran as validation in the
code-quality-reports pipeline.

.DESCRIPTION
Given a build type and target branch this will determine which Checkstyle, RevApi, and Spotbugs linting commands
will be used as validation in the code-quality-reports pipeline.

If the build type is "Scheduled" all linting steps will be performed as this is a daily validation. This will verify
all code changes since the last scheduled build against Checkstyle, RevApi, and Spotbugs linting.

Otherwise, the linting steps are determined based on which directories have code modifications. They are the following:

* checkstyle:check - eng/code-quality-reports/src/main/java/* (indicates source code changes to our custom Checkstyle rules)
* checkstyle:check - eng/code-quality-reports/src/main/resources/checkstyle/* (indicates Checkstyle configuration changes)
* revapi:check - eng/code-quality-reports/src/main/resources/revapi/* (indicates RevApi configuration changes)
* spotbugs:check - eng/code-quality-reports/src/main/resources/spotbugs/* (indicates Spotbugs configuration changes)

.PARAMETER BuildReason
Which pipeline build reason triggered execution of code-quality-reports.

.PARAMETER SourceBranch
The branch containing changes.

.PARAMETER TargetBranch
The branch being merged into once CI passes.

.PARAMETER LintingPipelineVariable
The pipeline variable that should be set containing the linting goals.
#>

param(
  [Parameter(Mandatory = $true)]
  [string]$BuildReason,

  [Parameter(Mandatory = $true)]
  [string]$SourceBranch,

  [string]$TargetBranch = ("origin/${env:SYSTEM_PULLREQUEST_TARGETBRANCH}" -replace "refs/heads/"),

  [Parameter(Mandatory = $true)]
  [string]$LintingPipelineVariable
)

Write-Host "Build reason: ${BuildReason}"
Write-Host "Source branch: ${SourceBranch}"
Write-Host "Target branch: ${TargetBranch}"
Write-Host "Linting pipeline variable: ${LintingPipelineVariable}"

if ($BuildReason -ne "PullRequest") {
    Write-Host "Non-PR pipeline runs always use linting goals 'checkstyle:check revapi:check spotbugs:check'"
    Write-Host "##vso[task.setvariable variable=${LintingPipelineVariable};]checkstyle:check revapi:check spotbugs:check"
    exit 0
}

$lintingGoals = ''
$baseDiffDirectory = 'eng/code-quality-reports/src/main'

$checkstyleSourceChanged = (git diff $TargetBranch $SourceBranch --name-only --relative -- "${baseDiffDirectory}/java/com/azure/tools/checkstyle/*").Count -gt 0
$checkstyleConfigChanged = (git diff $TargetBranch $SourceBranch --name-only --relative -- "${baseDiffDirectory}/resources/checkstyle/*").Count -gt 0
if ($checkstyleSourceChanged -or $checkstyleConfigChanged) {
    $lintingGoals += 'checkstyle:check'
}

$revapiSourceChanged = (git diff $TargetBranch $SourceBranch --name-only --relative -- "${baseDiffDirectory}/java/com/azure/tools/revapi/*").Count -gt 0
$revapiConfigChanged = (git diff $TargetBranch $SourceBranch --name-only --relative -- "${baseDiffDirectory}/resources/revapi/*").Count -gt 0
if ($revapiSourceChanged -or $revapiConfigChanged) {
    $lintingGoals += ' revapi:check'
}

$spotbugsConfigChanged = (git diff $TargetBranch $SourceBranch --name-only --relative -- "${baseDiffDirectory}/resources/spotbugs/*").Count -gt 0
if ($spotbugsConfigChanged) {
    $lintingGoals += ' spotbugs:check'
}

Write-Host "Using linting goals '${lintingGoals}'"
Write-Host "##vso[task.setvariable variable=${LintingPipelineVariable};]${lintingGoals}"

exit 0
