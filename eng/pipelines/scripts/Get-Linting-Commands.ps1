<#
.SYNOPSIS
Determines the Checkstyle, RevApi, and Spotbugs linting commands that should be ran as validation in the
linting-extensions pipeline.

.DESCRIPTION
Given a build type and target branch this will determine which Checkstyle, RevApi, and Spotbugs linting commands
will be used as validation in the linting-extensions pipeline.

If the build type is "Scheduled" all linting steps will be performed as this is a daily validation. This will verify
all code changes since the last scheduled build against Checkstyle, RevApi, and Spotbugs linting.

Otherwise, the linting steps are determined based on which directories have code modifications. They are the following:

* checkstyle:check - sdk/tools/linting-extensions/src/main/java/io/clientcore/linting/extensions/checkstyle/* (indicates source code changes to our custom Checkstyle rules)
* checkstyle:check - eng/lintingconfigs/checkstyle/* (indicates global Checkstyle configuration changes)
* revapi:check - sdk/tools/linting-extensions/src/main/java/io/clientcore/linting/extensions/revapi/* (indicates source code changes to our custom RevApi rules)
* revapi:check - eng/lintingconfigs/revapi/* (indicates global RevApi configuration changes)
* spotbugs:check - eng/lintingconfigs/spotbugs/* (indicates global Spotbugs configuration changes)

.PARAMETER BuildReason
Which pipeline build reason triggered execution of linting-extensions.

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
# Run all linting steps if any of the shared CI configuration files changed.
$runAll = $diffFiles -contains 'eng/code-quality-reports/ci.yml' `
    -or $diffFiles -contains 'eng/pipelines/code-quality-reports.yml' `
    -or $diffFiles -contains 'eng/pipelines/scripts/Get-Linting-Commands.ps1' `
    -or $diffFiles -contains 'eng/pipelines/scripts/Get-Linting-Reports.ps1' `
    -or $diffFiles -contains 'sdk/tools/linting-extensions/pom.xml'
if ($runAll) {
    Write-Host "PR changed the CI or project configuration, running all linting steps."
    Write-Host "##vso[task.setvariable variable=${LintingPipelineVariable};]-Dcheckstyle.failOnViolation=false -Dcheckstyle.failsOnError=false -Dspotbugs.failOnError=false -Drevapi.failBuildOnProblemsFound=false"
    Write-Host "##vso[task.setvariable variable=RunLinting;]true"
    exit 0
}

$runLinting = 'false'
[string[]]$lintingGoals = @()
$srcLintingExtensions = 'sdk/tools/linting-extensions/src/main/java/io/clientcore/linting/extensions'
$globalConfig = 'eng/lintingconfigs'

$checkstyleSourceChanged = ($diffFiles -match "${srcLintingExtensions}/checkstyle/*").Count -gt 0 `
    -or ($diffFiles -match "${globalConfig}/checkstyle/*").Count -gt 0
if ($checkstyleSourceChanged -or $checkstyleConfigChanged) {
    $runLinting = 'true'
    $lintingGoals += '-Dcheckstyle.failOnViolation=false'
    $lintingGoals += '-Dcheckstyle.failsOnError=false'
} else {
    $lintingGoals += '-Dcheckstyle.skip=true'
}

$revapiSourceChanged = ($diffFiles -match "${srcLintingExtensions}/revapi/*").Count -gt 0 `
    -or ($diffFiles -match "${globalConfig}/revapi/*").Count -gt 0
if ($revapiSourceChanged -or $revapiConfigChanged) {
    $runLinting = 'true'
    $lintingGoals += '-Drevapi.failBuildOnProblemsFound=false'
} else {
    $lintingGoals += '-Drevapi.skip=true'
}

$spotbugsConfigChanged = ($diffFiles -match "${globalConfig}/spotbugs/*").Count -gt 0
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
