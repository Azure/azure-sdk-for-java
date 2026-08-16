<#
.DESCRIPTION
This script compiles the stress test module, optionally performs Azure login, and deploys 
the Event Hubs stress tests with the provided namespace and environment.

.PARAMETER Namespace
The namespace in AKS for the stress test run. This is a required parameter.

.PARAMETER Environment
The stress deployment environment name. Defaults to "storage".

.PARAMETER SkipCompile
Skips the Maven compile/install step when specified.

.PARAMETER SkipLogin
Skips the Azure CLI login step when specified.

.EXAMPLE
./New-StressTestRun.ps1 -Namespace stress-test-namespace

Builds the stress module, signs in to Azure, and deploys using the default environment.
#>
param(
    [Parameter(Mandatory = $true)]
    [string]$Namespace,

    [Parameter()]
    [string]$Environment = "storage",

    [Parameter()]
    [switch]$SkipCompile,

    [Parameter()]
    [switch]$SkipLogin
)

$ErrorActionPreference = "Stop"

if ([string]::IsNullOrWhiteSpace($Environment)) {
     throw "Environment is required"
}

$scriptDir = $PSScriptRoot
Set-Location $scriptDir

if ($SkipCompile) {
    Write-Host "Skipping compile step"
} else {
    mvn clean install --% -am -pl com.azure:azure-messaging-eventhubs-stress -Dcheckstyle.skip -Dgpg.skip -Dmaven.javadoc.skip -Drevapi.skip -Dspotbugs.skip -Djacoco.skip -Dmaven.test.skip -Dcodesnippet.skip -Dspotless.skip
}

if ($SkipLogin) {
    Write-Host "Skipping login step"
} else {
    az login --scope https://management.core.windows.net//.default
}

try {
    $resolvedDeployScript = Resolve-Path (Join-Path $PSScriptRoot "../../../eng/common/scripts/stress-testing/deploy-stress-tests.ps1")
} catch {
    throw "Unable to find deploy script relative to $PSScriptRoot"
}

$resolvedDeployScript = $resolvedDeployScript.Path
& $resolvedDeployScript -Namespace $Namespace -Environment $Environment
