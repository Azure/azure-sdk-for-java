param(
    [string] $Alias,
    [string] $SubscriptionId,
    [switch] $Execute
)

$ErrorActionPreference = "Stop"

Get-Command az | Out-Null

if ($SubscriptionId) {
    az account set --subscription $SubscriptionId
}

$currentSubscription = az account show --query "{name:name, id:id}" --output tsv
Write-Host "Using subscription: $currentSubscription"
Write-Host "Looking for resource groups starting with 'SSS3PT_$Alias'..."

$resourceGroups = @(az group list `
    --query "[?starts_with(name, 'SSS3PT_$Alias')].name" `
    --output tsv)

if ($resourceGroups.Count -eq 0) {
    Write-Host "No matching resource groups found."
    exit 0
}

Write-Host ""
Write-Host "Matching resource groups:"
$resourceGroups | ForEach-Object { Write-Host "  $_" }

if (-not $Execute) {
    Write-Host ""
    Write-Host "Dry run only. To delete these resource groups, run:"
    Write-Host "  .\delete-matching-resource-groups.ps1 -Execute"
    Write-Host ""
    Write-Host "To target a specific subscription, run:"
    Write-Host "  .\delete-matching-resource-groups.ps1 -SubscriptionId <subscription-id> -Execute"
    exit 0
}

Write-Host ""
$confirmation = Read-Host "Type DELETE to permanently delete these resource groups"

if ($confirmation -ne "DELETE") {
    Write-Host "Cancelled."
    exit 1
}

foreach ($resourceGroup in $resourceGroups) {
    Write-Host "Deleting resource group: $resourceGroup"
    az group delete `
        --name $resourceGroup `
        --yes `
        --no-wait
}

Write-Host ""
Write-Host "Delete operations submitted."
