# Copyright (c) Microsoft Corporation. All rights reserved.
# Licensed under the MIT License.

# This script uploads test data to the storage container used by the Health Deidentification live tests.

# It is invoked by eng/common/TestResources/New-TestResources.ps1 after test-resources.bicep is deployed.
# The template creates the storage account, container, DeID service, and role assignments needed for live tests.

param (
    [hashtable] $DeploymentOutputs,
    [string] $TenantId,
    [string] $TestApplicationId,
    [string] $TestApplicationSecret
)

# Retrieve the connection string from environment variables
$resourceGroup = $DeploymentOutputs['HEALTHDATAAISERVICES_RESOURCE_GROUP']
$endpoint = $DeploymentOutputs['HEALTHDATAAISERVICES_DEID_SERVICE_ENDPOINT']
$storageAccountName = $DeploymentOutputs['HEALTHDATAAISERVICES_STORAGE_ACCOUNT_NAME']
$containerName = $DeploymentOutputs['HEALTHDATAAISERVICES_STORAGE_CONTAINER_NAME']
$testMode = "live"

# Resolve the test data folder relative to this script so it works from any working directory
$localFolderPath = Join-Path $PSScriptRoot "azure-health-deidentification\src\test\resources\data"

# Upper bound on how long to wait for storage RBAC / network ACL changes to propagate
$storageReadinessTimeoutSeconds = 300

# Minimum time after the network rule update before tests may start. The retries below only prove the
# connected account's access; the DeID managed identity's access can't be observed here, so keep at
# least the previous fixed propagation window for it.
$deidIdentityMinimumWaitSeconds = 30

# Check if the connection string is present
if ([string]::IsNullOrWhiteSpace($storageAccountName)) {
    Write-Host "Error: Azure Storage Name string not found in environment variables."
    exit 1
}

# Retries $Action with exponential backoff until it succeeds or $TimeoutSeconds elapses.
# Role assignments and network rule changes made during deployment can take minutes to
# propagate, so early data-plane calls may fail with 403 AuthorizationFailure.
function Invoke-WithRetry {
    param (
        [Parameter(Mandatory)] [scriptblock] $Action,
        [Parameter(Mandatory)] [string] $Description,
        [int] $TimeoutSeconds = $storageReadinessTimeoutSeconds,
        [int] $InitialDelaySeconds = 5,
        [int] $MaxDelaySeconds = 30
    )

    $stopwatch = [System.Diagnostics.Stopwatch]::StartNew()
    $delay = $InitialDelaySeconds
    $attempt = 0
    while ($true) {
        $attempt++
        try {
            return & $Action
        } catch {
            $elapsed = [int]$stopwatch.Elapsed.TotalSeconds
            if ($elapsed + $delay -gt $TimeoutSeconds) {
                $message = "Timed out after $elapsed seconds and $attempt attempt(s) waiting to $Description. " +
                    "Storage role assignments or network rules may not have propagated. Last error: $($_.Exception.Message)"
                throw $message
            }
            Write-Host "Attempt $attempt to $Description failed: $($_.Exception.Message)"
            Write-Host "Retrying in $delay seconds (elapsed: $elapsed of $TimeoutSeconds seconds)."
            Start-Sleep -Seconds $delay
            $delay = [Math]::Min($delay * 2, $MaxDelaySeconds)
        }
    }
}

# Load the Azure Storage module
Import-Module Az.Storage

# Connect to the storage account
$storageContext = New-AzStorageContext -StorageAccountName $storageAccountName -UseConnectedAccount

# FIXME Remove once vpn team fixes the network acl issue
$networkRuleSet = New-Object -TypeName Microsoft.Azure.Commands.Management.Storage.Models.PSNetworkRuleSet
$networkRuleSet.DefaultAction = "Allow"
Set-AzStorageAccount -ResourceGroupName $resourceGroup -Name $storageAccountName -NetworkRuleSet $networkRuleSet
$propagationStopwatch = [System.Diagnostics.Stopwatch]::StartNew()

# Wait until the container is reachable with the connected account instead of sleeping a fixed time
Invoke-WithRetry -Description "access container '$containerName' in storage account '$storageAccountName'" -Action {
    Get-AzStorageContainer -Name $containerName -Context $storageContext -ErrorAction Stop
}

$files = @(Get-ChildItem -Path $localFolderPath -Recurse -File -ErrorAction Stop)
if ($files.Count -eq 0) {
    Write-Host "Error: No test data files found in '$localFolderPath'."
    exit 1
}

# Upload the folder and its contents to the container
# Gets last folder name + filename. example_patient_1/doctor_dictation.txt
foreach ($file in $files) {
    $folderName = Split-Path -Path $file.DirectoryName -Leaf
    $destinationBlob = "$folderName/$($file.Name -replace ':', '')"

    Write-Host "Uploading file '$destinationBlob'"
    # Container read access does not guarantee write access has propagated, so retry uploads too
    Invoke-WithRetry -Description "upload blob '$destinationBlob'" -Action {
        Set-AzStorageBlobContent -File $file.FullName -Container $containerName -Blob $destinationBlob `
            -Context $storageContext -Force -ErrorAction Stop
    } | Out-Null
}

Write-Host "Folder '$localFolderPath' uploaded to container '$containerName' successfully."

$remainingWaitSeconds = $deidIdentityMinimumWaitSeconds - [int]$propagationStopwatch.Elapsed.TotalSeconds
if ($remainingWaitSeconds -gt 0) {
    Write-Host "Waiting $remainingWaitSeconds more seconds for the DeID service identity's storage access to propagate."
    Start-Sleep -Seconds $remainingWaitSeconds
}

$endpoint = $endpoint -replace '^https://', ''
# Set environment variables
[Environment]::SetEnvironmentVariable("DEID_SERVICE_ENDPOINT", $endpoint, 'User')
[Environment]::SetEnvironmentVariable("AZURE_TEST_MODE", "live", 'User')
[Environment]::SetEnvironmentVariable("STORAGE_ACCOUNT_NAME", $storageAccountName, 'User')
[Environment]::SetEnvironmentVariable("STORAGE_CONTAINER_NAME", $containerName, 'User')
