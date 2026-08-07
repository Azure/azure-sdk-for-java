<#
.SYNOPSIS
Tests the Maven project using Federated auth.

.PARAMETER PomFile
The POM file used to run tests.

.PARAMETER TestGoals
The test goals to be used when running tests.

.PARAMETER ParallelTestPlayback
The parallel test playback option to be used when running tests.

.PARAMETER TestOptions
The test options to be used when running tests.

.PARAMETER TestParallelization
The test parallelization option to be used when running tests.

.PARAMETER BuildOptions
The build options to be used when running tests.
#>

param(
    [Parameter(Mandatory = $true)]
    [string]$PomFile,

    [Parameter(Mandatory = $true)]
    [string]$TestGoals,

    [Parameter(Mandatory = $true)]
    [string]$ParallelTestPlayback,

    [Parameter(Mandatory = $true)]
    [string]$TestOptions,

    [Parameter(Mandatory = $true)]
    [string]$TestParallelization,

    [Parameter(Mandatory = $false)]
    [string]$BuildOptions
)

$account = (Get-AzContext).Account
$env:AZURESUBSCRIPTION_CLIENT_ID = $account.Id
$env:AZURESUBSCRIPTION_TENANT_ID = $account.Tenants

mvn --% -f $PomFile $env:DefaultTestOptions $TestGoals -DAZURE_TEST_DEBUG=$env:IsDebug -Dparallel-test-playback=$ParallelTestPlayback $TestOptions $env:LiveTestSourceParams $BuildOptions -T $TestParallelization
exit $LASTEXITCODE
