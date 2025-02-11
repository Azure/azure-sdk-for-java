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
    Write-Host "Non-PR pipeline runs always run Checkstyle, RevApi, and Spotbugs."
    Write-Host "##vso[task.setvariable variable=${LintingPipelineVariable};]-Dcheckstyle.failOnViolation=false -Dcheckstyle.failsOnError=false -Dspotbugs.failOnError=false -Drevapi.failBuildOnProblemsFound=false"
    Write-Host "##vso[task.setvariable variable=RunLinting;]true"
    exit 0
}

$diffFiles = (git diff $TargetBranch $SourceBranch --name-only --relative)
if ($diffFiles -contains 'eng/code-quality-reports/ci.yml') {
    Write-Host "PR changed the CI configuration, running all linting steps."
    Write-Host "##vso[task.setvariable variable=${LintingPipelineVariable};]-Dcheckstyle.failOnViolation=false -Dcheckstyle.failsOnError=false -Dspotbugs.failOnError=false -Drevapi.failBuildOnProblemsFound=false"
    Write-Host "##vso[task.setvariable variable=RunLinting;]true"
    exit 0
}

$runLinting = 'false'
[string[]]$lintingGoals = @()
$baseDiffDirectory = 'eng/code-quality-reports/src/main'


$checkstyleSourceChanged = ($diffFiles -match "${baseDiffDirectory}/java/com/azure/tools/checkstyle/*").Count -gt 0
$checkstyleConfigChanged = ($diffFiles -match "${baseDiffDirectory}/resources/checkstyle/*").Count -gt 0
if ($checkstyleSourceChanged -or $checkstyleConfigChanged) {
    $runLinting = 'true'
    $lintingGoals += '-Dcheckstyle.failOnViolation=false'
    $lintingGoals += '-Dcheckstyle.failsOnError=false'
} else {
    $lintingGoals += '-Dcheckstyle.skip=true'
}

$revapiSourceChanged = ($diffFiles -match "${baseDiffDirectory}/java/com/azure/tools/revapi/*").Count -gt 0
$revapiConfigChanged = ($diffFiles -match "${baseDiffDirectory}/resources/revapi/*").Count -gt 0
if ($revapiSourceChanged -or $revapiConfigChanged) {
    $runLinting = 'true'
    $lintingGoals += '-Drevapi.failBuildOnProblemsFound=false'
} else {
    $lintingGoals += '-Drevapi.skip=true'
}

$spotbugsConfigChanged = ($diffFiles -match "${baseDiffDirectory}/resources/spotbugs/*").Count -gt 0
if ($spotbugsConfigChanged) {
    $runLinting = 'true'
    $lintingGoals += '-Dspotbugs.failOnError=false'
} else {
    $lintingGoals += '-Dspotbugs.skip=true'
}

$lintingCommand = $lintingGoals -join ' '
Write-Host "Using linting goals '${lintingCommand}'"
Write-Host "##vso[task.setvariable variable=${LintingPipelineVariable};]${lintingCommand}"
Write-Host "##vso[task.setvariable variable=RunLinting;]${runLinting}"

exit 0
