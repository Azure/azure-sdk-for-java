<#
.SYNOPSIS
    (Re)creates the fixed Cosmos DB accounts used by the azure-sdk-for-java Cosmos live
    tests and outputs the sub-config-cosmos-azure-cloud-test-resources JSON (endpoints + keys).

.DESCRIPTION
    The Java Cosmos live tests run against fixed, self-owned accounts (Track A of the
    live-test retargeting). Because the ephemeral tenant is deleted/recreated roughly
    every 90 days, this script is re-run after each rotation to:
      1. Ensure the resource group (default: sdk-ci) exists (created if missing).
      2. Create (idempotently) one Cosmos account per entry in the definition file,
         with the requested consistency / multi-write / multi-region / thin-client /
         partition-merge configuration.
      3. Read each account's endpoint + primary (and optional secondary) key.
      4. Assemble the versioned account JSON and emit it (to stdout,
         and to -OutputPath if provided).

    This script does NOT touch Key Vault. Update the
    sub-config-cosmos-azure-cloud-test-resources secret / ADO variable manually with the
    JSON it outputs.

    Uses the Az PowerShell modules (Az.Accounts, Az.Resources, Az.CosmosDB).

.PARAMETER SubscriptionId
    Subscription hosting the resource group and the Cosmos accounts.

.PARAMETER ResourceGroupName
    Resource group for the accounts. Created if it does not exist. Defaults to 'sdk-ci'.

.PARAMETER Location
    Optional override for the primary/write region. When omitted, the primary region comes
    from the definition's regionDefaults (single source of truth; matches test-resources.json).

.PARAMETER SecondaryLocation
    Optional override for the secondary region of multi-region accounts. When omitted, it
    comes from the definition's regionDefaults.multiRegion.

.PARAMETER DefinitionPath
    Path to the account definition JSON. Defaults to the file next to this script.

.PARAMETER AccountNamePrefix
    Prefix for the globally-unique Cosmos account names. Defaults to 'sdkci'.

.PARAMETER OutputPath
    Optional path to write the assembled JSON to. The JSON is always also written to
    stdout. NOTE: the JSON contains account keys - treat any file you write as a secret.

.EXAMPLE
    # Create/refresh accounts and write the JSON to a file, then update the secret manually
    ./New-CosmosLiveTestAccounts.ps1 -SubscriptionId <sub> -OutputPath ./accounts.json

.EXAMPLE
    # Dry run - creates nothing, prints the assembled JSON with keys stubbed
    ./New-CosmosLiveTestAccounts.ps1 -SubscriptionId <sub> -WhatIf

.NOTES
    Requires: PowerShell 7+, Az modules, and Contributor on the subscription.
    Idempotent: safe to re-run.
#>
[CmdletBinding(SupportsShouldProcess = $true)]
param(
    [Parameter(Mandatory = $true)]
    [string] $SubscriptionId,

    [string] $ResourceGroupName = 'sdk-ci',

    [string] $Location,

    [string] $SecondaryLocation,

    [string] $DefinitionPath = (Join-Path $PSScriptRoot 'cosmos-live-test-accounts.definition.json'),

    [ValidatePattern('^[a-z0-9]{1,10}$')]
    [string] $AccountNamePrefix = 'sdkci',

    [string] $OutputPath
)

$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest

function Write-Info($msg) { Write-Host "==> $msg" -ForegroundColor Cyan }

# --- Prerequisites -----------------------------------------------------------
foreach ($m in @('Az.Accounts', 'Az.Resources', 'Az.CosmosDB')) {
    if (-not (Get-Module -ListAvailable -Name $m)) {
        throw "Required module '$m' is not installed. Install with: Install-Module $m -Scope CurrentUser"
    }
}

if (-not (Test-Path $DefinitionPath)) { throw "Definition file not found: $DefinitionPath" }
$definition = Get-Content -Raw -Path $DefinitionPath | ConvertFrom-Json

# Regions are defined once in the definition's regionDefaults so the provisioned topology
# matches sdk/cosmos/test-resources.json (the ARM template the old flow used). The optional
# -Location / -SecondaryLocation params override the primary/secondary region for ad-hoc runs.
if (-not ($definition.PSObject.Properties.Name -contains 'regionDefaults')) {
    throw "Definition '$DefinitionPath' is missing 'regionDefaults' (singleRegion / multiRegion)."
}
$singleRegionList = @($definition.regionDefaults.singleRegion)
$multiRegionList  = @($definition.regionDefaults.multiRegion)
if ($singleRegionList.Count -lt 1 -or $multiRegionList.Count -lt 2) {
    throw "regionDefaults must provide singleRegion (>=1) and multiRegion (>=2) entries."
}
if ($Location) {
    $singleRegionList[0] = $Location
    $multiRegionList[0]  = $Location
}
if ($SecondaryLocation) {
    $multiRegionList[1] = $SecondaryLocation
}
$primaryRegion = $multiRegionList[0]

Write-Info "Selecting subscription $SubscriptionId"
$null = Set-AzContext -Subscription $SubscriptionId

# --- Resource group (create if missing) --------------------------------------
if (-not (Get-AzResourceGroup -Name $ResourceGroupName -ErrorAction SilentlyContinue)) {
    if ($PSCmdlet.ShouldProcess($ResourceGroupName, 'Create resource group')) {
        Write-Info "Creating resource group $ResourceGroupName in $primaryRegion"
        $null = New-AzResourceGroup -Name $ResourceGroupName -Location $primaryRegion
    }
} else {
    Write-Info "Resource group $ResourceGroupName already exists"
}

# --- Helper: build the -LocationObject list from a region list --------------
# First region is the write region (failoverPriority 0); the rest are read regions.
function New-LocationObjects([string[]] $regionList) {
    $locations = @()
    for ($i = 0; $i -lt $regionList.Count; $i++) {
        $locations += New-AzCosmosDBLocationObject -LocationName $regionList[$i] -FailoverPriority $i -IsZoneRedundant $false
    }
    return ,$locations
}

# --- Create / update each account, then collect endpoint + keys --------------
$secret = [ordered]@{
    version  = 1
    accounts = [ordered]@{}
}

foreach ($acct in $definition.accounts) {
    $selector    = $acct.name
    $accountName = ("{0}-{1}" -f $AccountNamePrefix, $selector).ToLower()
    if ($accountName.Length -gt 44) {
        throw "Generated account name '$accountName' exceeds 44 chars. Shorten AccountNamePrefix or the selector '$selector'."
    }

    $multiRegion = [bool]$acct.enableMultipleRegions
    $multiWrite  = [bool]$acct.enableMultipleWriteLocations
    $regionList  = if ($multiRegion) { $multiRegionList } else { $singleRegionList }
    $locations   = New-LocationObjects $regionList

    $capabilities = @()
    if ($acct.PSObject.Properties.Name -contains 'capabilities') {
        # Any account-level capabilities (e.g. thin-client) come from the definition so we
        # never ship a guessed capability name. Leave empty if none are specified.
        $capabilities = @($acct.capabilities)
    }

    $existing = Get-AzCosmosDBAccount -ResourceGroupName $ResourceGroupName -Name $accountName -ErrorAction SilentlyContinue
    if (-not $existing) {
        if ($PSCmdlet.ShouldProcess($accountName, "Create Cosmos account [$selector]")) {
            Write-Info "Creating Cosmos account '$accountName' (selector=$selector, consistency=$($acct.defaultConsistencyLevel), multiWrite=$multiWrite, multiRegion=$multiRegion)"
            $params = @{
                ResourceGroupName            = $ResourceGroupName
                Name                         = $accountName
                LocationObject               = $locations
                DefaultConsistencyLevel      = $acct.defaultConsistencyLevel
                EnableMultipleWriteLocations = $multiWrite
                ApiKind                      = 'GlobalDocumentDB'
            }
            if ($acct.defaultConsistencyLevel -eq 'BoundedStaleness') {
                $params['MaxStalenessIntervalInSeconds'] = 5
                $params['MaxStalenessPrefix'] = 100
            }
            if ($acct.PSObject.Properties.Name -contains 'enablePartitionMerge' -and $acct.enablePartitionMerge) {
                $params['EnablePartitionMerge'] = $true
            }
            if ($capabilities.Count -gt 0) { $params['Capabilities'] = $capabilities }
            $null = New-AzCosmosDBAccount @params
        }
    } else {
        # Account already exists. Reconcile capabilities: Cosmos capabilities are additive
        # and cannot be removed, so we only add any desired capability that is missing
        # (e.g. EnableNoSQLVectorSearch). This makes the script idempotent for capability
        # changes on already-provisioned accounts.
        $existingCaps = @()
        if ($existing.Capabilities) { $existingCaps = @($existing.Capabilities | ForEach-Object { $_.Name }) }
        $missingCaps = @($capabilities | Where-Object { $existingCaps -notcontains $_ })
        if ($missingCaps.Count -gt 0) {
            $mergedCaps = @($existingCaps + $missingCaps | Select-Object -Unique)
            if ($PSCmdlet.ShouldProcess($accountName, "Add capabilities [$($missingCaps -join ', ')]")) {
                Write-Info "Account '$accountName' exists (selector=$selector); adding missing capabilities: $($missingCaps -join ', ')"
                # Capabilities cannot be set via Update-AzCosmosDBAccount, so PATCH the account
                # through ARM. Capabilities are additive; send the full merged list.
                $resourceId = "/subscriptions/$SubscriptionId/resourceGroups/$ResourceGroupName/providers/Microsoft.DocumentDB/databaseAccounts/$accountName"
                $body = @{
                    properties = @{
                        capabilities = @($mergedCaps | ForEach-Object { @{ name = $_ } })
                    }
                } | ConvertTo-Json -Depth 6
                $resp = Invoke-AzRestMethod -Method PATCH -Path "$($resourceId)?api-version=2024-11-15" -Payload $body
                if ($resp.StatusCode -ge 300) {
                    throw "Failed to add capabilities to '$accountName' (HTTP $($resp.StatusCode)): $($resp.Content)"
                }
            }
        } else {
            Write-Info "Cosmos account '$accountName' already exists (selector=$selector); capabilities up to date"
        }
    }

    # Read endpoint + keys. Under -WhatIf (dry run) never read real keys — stub them so a
    # preview never emits secrets, even for already-provisioned accounts.
    if ($WhatIfPreference) {
        $endpoint = "https://$accountName.documents.azure.com:443/"
        $primary  = 'WHATIF_KEY'
        $secondary = 'WHATIF_SECONDARY_KEY'
    } else {
        $account  = Get-AzCosmosDBAccount -ResourceGroupName $ResourceGroupName -Name $accountName
        $endpoint = $account.DocumentEndpoint
        $keys     = Get-AzCosmosDBAccountKey -ResourceGroupName $ResourceGroupName -Name $accountName -Type 'Keys'
        $primary  = $keys.PrimaryMasterKey
        $secondary = $keys.SecondaryMasterKey
    }

    $entry = [ordered]@{
        endpoint    = $endpoint
        key         = $primary
        consistency = $acct.defaultConsistencyLevel
        multiWrite  = $multiWrite
    }
    if ($acct.PSObject.Properties.Name -contains 'includeSecondaryKey' -and $acct.includeSecondaryKey) {
        $entry['secondaryKey'] = $secondary
    }
    if ($acct.PSObject.Properties.Name -contains 'thinClient' -and $acct.thinClient) {
        $entry['thinClient'] = $true
    }
    if ($acct.PSObject.Properties.Name -contains 'preferredLocations') {
        $entry['preferredLocations'] = [string[]]@($acct.preferredLocations)
    }
    $entry['regions'] = [string[]]$regionList

    $secret.accounts[$selector] = $entry
}

# --- Emit the assembled JSON -------------------------------------------------
$secretJson = $secret | ConvertTo-Json -Depth 8

if ($OutputPath) {
    if ($PSCmdlet.ShouldProcess($OutputPath, 'Write accounts JSON to file')) {
        Set-Content -Path $OutputPath -Value $secretJson -NoNewline
        Write-Info "Wrote accounts JSON to '$OutputPath' ($($secret.accounts.Count) accounts). Contains keys - treat as secret."
    }
}

Write-Info "Assembled $($secret.accounts.Count) accounts. Update the sub-config-cosmos-azure-cloud-test-resources secret manually with this JSON."
# Emit the JSON to stdout so it can be captured/redirected.
Write-Output $secretJson
