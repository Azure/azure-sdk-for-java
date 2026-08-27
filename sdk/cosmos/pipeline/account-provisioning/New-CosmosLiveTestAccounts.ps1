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

# Adds any missing capabilities to an existing account via ARM PATCH.
#
# Capabilities are handled here, never via New-/Update-AzCosmosDBAccount:
#   - Update-AzCosmosDBAccount cannot set them at all.
#   - New-AzCosmosDBAccount -Capabilities is silently ignored by some Az.CosmosDB versions
#     (observed on Az 12.2.0: accounts came up with no capabilities at all, which failed
#     every vector-search test until the script was run a second time).
# Reconciling after the account exists makes the outcome independent of module behaviour,
# so a fresh tenant rotation is a single pass.
#
# Cosmos capabilities are additive and cannot be removed, so we only ever add, and always
# send the full merged list.
function Sync-AccountCapability {
    # SupportsShouldProcess so -WhatIf propagates from the caller and this never PATCHes on a dry run.
    [CmdletBinding(SupportsShouldProcess = $true)]
    param(
        [Parameter(Mandatory)] [string]   $AccountName,
        [Parameter(Mandatory)] [AllowEmptyCollection()] [string[]] $DesiredCapabilities,
        [Parameter(Mandatory)] [string]   $ResourceGroupName,
        [Parameter(Mandatory)] [string]   $SubscriptionId,
        [string] $Selector
    )

    if ($DesiredCapabilities.Count -eq 0) { return }

    $account = Get-AzCosmosDBAccount -ResourceGroupName $ResourceGroupName -Name $AccountName -ErrorAction SilentlyContinue
    if (-not $account) {
        # Only reachable under -WhatIf, where the account was never actually created.
        return
    }

    $existingCaps = @()
    if ($account.Capabilities) { $existingCaps = @($account.Capabilities | ForEach-Object { $_.Name }) }

    $missingCaps = @($DesiredCapabilities | Where-Object { $existingCaps -notcontains $_ })
    if ($missingCaps.Count -eq 0) {
        Write-Info "Cosmos account '$AccountName' (selector=$Selector); capabilities up to date"
        return
    }

    $mergedCaps = @($existingCaps + $missingCaps | Select-Object -Unique)
    if (-not $PSCmdlet.ShouldProcess($AccountName, "Add capabilities [$($missingCaps -join ', ')]")) { return }

    Write-Info "Account '$AccountName' (selector=$Selector); adding missing capabilities: $($missingCaps -join ', ')"
    $resourceId = "/subscriptions/$SubscriptionId/resourceGroups/$ResourceGroupName/providers/Microsoft.DocumentDB/databaseAccounts/$AccountName"
    $body = @{
        properties = @{
            capabilities = @($mergedCaps | ForEach-Object { @{ name = $_ } })
        }
    } | ConvertTo-Json -Depth 6

    $resp = Invoke-AzRestMethod -Method PATCH -Path "$($resourceId)?api-version=2024-11-15" -Payload $body
    if ($resp.StatusCode -ge 300) {
        throw "Failed to add capabilities to '$AccountName' (HTTP $($resp.StatusCode)): $($resp.Content)"
    }

    # PATCH returns before the capability is durably applied; confirm it landed so a fresh
    # provisioning run cannot silently produce accounts the tests then fail against.
    $deadline = (Get-Date).AddMinutes(5)
    while ((Get-Date) -lt $deadline) {
        Start-Sleep -Seconds 10
        $check = Get-AzCosmosDBAccount -ResourceGroupName $ResourceGroupName -Name $AccountName -ErrorAction SilentlyContinue
        $nowCaps = @()
        if ($check -and $check.Capabilities) { $nowCaps = @($check.Capabilities | ForEach-Object { $_.Name }) }
        $stillMissing = @($DesiredCapabilities | Where-Object { $nowCaps -notcontains $_ })
        if ($stillMissing.Count -eq 0) { return }
    }

    throw "Capabilities [$($missingCaps -join ', ')] did not apply to '$AccountName' within 5 minutes"
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
    # An account may pin its own regions when the defaults do not suit it - the GSI account, for
    # example, must live in East US 2 because live-gsi-platform-matrix.json sets
    # PREFERRED_LOCATIONS=["East US 2"] on a single-region account, and a preferred region the account
    # does not have leaves the client with nothing to prefer.
    $regionList = if ($acct.PSObject.Properties.Name -contains 'regions' -and $acct.regions) {
        @($acct.regions)
    } elseif ($multiRegion) {
        $multiRegionList
    } else {
        $singleRegionList
    }
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
            $null = New-AzCosmosDBAccount @params
        }
    } else {
        Write-Info "Cosmos account '$accountName' already exists (selector=$selector)"
    }

    # Reconcile capabilities on both paths. Deliberately not passed to New-AzCosmosDBAccount:
    # some Az.CosmosDB versions ignore -Capabilities silently, which produced accounts with no
    # capabilities at all and needed a second run of this script to fix.
    Sync-AccountCapability `
        -AccountName $accountName `
        -DesiredCapabilities $capabilities `
        -ResourceGroupName $ResourceGroupName `
        -SubscriptionId $SubscriptionId `
        -Selector $selector

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
