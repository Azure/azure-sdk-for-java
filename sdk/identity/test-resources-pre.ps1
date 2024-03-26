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
    [string] $TenantId
)

pip install azure-cli=="2.56.0" | Write-Host

$az_version = az version
Write-Host "Azure CLI version: $az_version"

Import-Module -Name $PSScriptRoot/../../eng/common/scripts/X509Certificate2 -Verbose

ssh-keygen -t rsa -b 4096 -f $PSScriptRoot/sshKey -N '' -C ''
$sshKey = Get-Content $PSScriptRoot/sshKey.pub

$templateFileParameters['sshPubKey'] = $sshKey

# Get the max version that is not preview and then get the name of the patch version with the max value
az login --service-principal -u $TestApplicationId -p $TestApplicationSecret --tenant $TenantId
$versions = az aks get-versions -l westus -o json | ConvertFrom-Json
Write-Host "AKS versions: $($versions | ConvertTo-Json -Depth 100)"
$patchVersions = $versions.values | Where-Object { $_.isPreview -eq $null } | Select-Object -ExpandProperty patchVersions
Write-Host "AKS patch versions: $($patchVersions | ConvertTo-Json -Depth 100)"
$latestAksVersion = $patchVersions | Get-Member -MemberType NoteProperty | Select-Object -ExpandProperty Name | Sort-Object -Descending | Select-Object -First 1
Write-Host "Latest AKS version: $latestAksVersion"
$templateFileParameters['latestAksVersion'] = $latestAksVersion
