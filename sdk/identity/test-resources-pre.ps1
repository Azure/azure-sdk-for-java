# cspell:ignore OIDC, oidc

[CmdletBinding(SupportsShouldProcess = $true, ConfirmImpact = 'Medium')]
param (
    # Captures any arguments from eng/New-TestResources.ps1 not declared here (no parameter errors).
    [Parameter(ValueFromRemainingArguments = $true)]
    $RemainingArguments,

    [Parameter()]
    [ValidatePattern('^[0-9a-f]{8}(-[0-9a-f]{4}){3}-[0-9a-f]{12}$')]
    [string] $TestApplicationId,

    [Parameter()]
    [string] $TestApplicationSecret,

    [Parameter(ParameterSetName = 'Provisioner', Mandatory = $true)]
    [ValidateNotNullOrEmpty()]
    [string] $TenantId,

    [Parameter(Mandatory = $true)]
    [ValidateNotNullOrEmpty()]
    [string] $SubscriptionId,

    [Parameter(Mandatory = $true)]
    [ValidateNotNullOrEmpty()]
    [string] $ResourceGroupName,

    [Parameter(Mandatory = $true)]
    [ValidateNotNullOrEmpty()]
    [string] $Environment
)

. (Join-Path $PSScriptRoot test-resources-helpers.ps1)

$az_version = az version
Write-Host "Azure CLI version: $az_version"

Import-Module -Name $PSScriptRoot/../../eng/common/scripts/X509Certificate2 -Verbose

ssh-keygen -t rsa -b 4096 -f $PSScriptRoot/sshKey -N '' -C ''
$sshKey = Get-Content $PSScriptRoot/sshKey.pub

$templateFileParameters['sshPubKey'] = $sshKey

az cloud set --name $Environment
az login --service-principal -u $TestApplicationId --tenant $TenantId --allow-no-subscriptions --federated-token $env:ARM_OIDC_TOKEN
az account set --subscription $SubscriptionId
$aksLocation = Get-IdentityAksLocation -TemplateParameters $templateFileParameters -ResourceGroupName $ResourceGroupName
$versionsJson = az aks get-versions -l $aksLocation -o json
if ($LASTEXITCODE -ne 0) {
    throw "Failed to get AKS versions for location '$aksLocation'."
}
$versions = $versionsJson | ConvertFrom-Json
$latestAksVersion = Get-LatestIdentityAksVersion -Versions $versions -Location $aksLocation
Write-Host "Latest AKS version in ${aksLocation}: $latestAksVersion"
$templateFileParameters['latestAksVersion'] = $latestAksVersion
