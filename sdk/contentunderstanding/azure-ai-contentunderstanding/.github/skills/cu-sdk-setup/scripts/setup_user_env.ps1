# Setup script for Azure AI Content Understanding Java SDK users (PowerShell)
# Mirrors scripts/setup_user_env.sh for Windows / cross-platform PowerShell.

[CmdletBinding()]
param()

$ErrorActionPreference = 'Stop'

# Determine script directory and package root
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$PackageRoot = (Resolve-Path (Join-Path $ScriptDir '..\..\..\..')).Path

Write-Host "=== Azure AI Content Understanding (Java) - User Environment Setup ==="
Write-Host "Package root: $PackageRoot"
Write-Host ""

Set-Location $PackageRoot

# --- helper: offer to install JDK/Maven via the platform's package manager ---
function Invoke-OfferInstallTool {
    param([string]$Tool) # 'jdk' | 'maven'
    $isWin = $IsWindows -or $PSVersionTable.PSEdition -eq 'Desktop'
    $cmds = @()
    if ($isWin) {
        $winget = Get-Command winget -ErrorAction SilentlyContinue
        if (-not $winget) {
            Write-Host "  (winget not found — install JDK/Maven manually.)"
            return $false
        }
        switch ($Tool) {
            'jdk'   { $cmds = @('winget install -e --id Microsoft.OpenJDK.21 --accept-source-agreements --accept-package-agreements') }
            'maven' { $cmds = @('winget install -e --id Apache.Maven --accept-source-agreements --accept-package-agreements') }
        }
    } elseif ($IsMacOS) {
        $brew = Get-Command brew -ErrorAction SilentlyContinue
        if (-not $brew) {
            Write-Host "  (Homebrew not found — install it first: https://brew.sh/)"
            return $false
        }
        switch ($Tool) {
            'jdk'   { $cmds = @('brew install openjdk@21') }
            'maven' { $cmds = @('brew install maven') }
        }
    } elseif ($IsLinux) {
        $apt = Get-Command apt-get -ErrorAction SilentlyContinue
        if (-not $apt) {
            Write-Host "  (No apt-get detected — install JDK/Maven with your distro's package manager.)"
            return $false
        }
        switch ($Tool) {
            'jdk'   { $cmds = @('sudo apt-get update && sudo apt-get install -y openjdk-21-jdk') }
            'maven' { $cmds = @('sudo apt-get update && sudo apt-get install -y maven') }
        }
    } else {
        Write-Host "  (Unsupported platform for auto-install.)"
        return $false
    }

    Write-Host ""
    Write-Host "  This script can run the following command(s) for you:"
    foreach ($c in $cmds) { Write-Host "    $c" }
    $reply = Read-Host "  Run them now? (y/N)"
    if ($reply -notmatch '^[Yy]$') {
        Write-Host "  Please run them yourself, then re-run this script."
        return $false
    }

    foreach ($c in $cmds) {
        try {
            if ($isWin) {
                Invoke-Expression $c
            } else {
                bash -lc $c
            }
            if ($LASTEXITCODE -ne 0) {
                Write-Host "  [FAIL] Command failed (exit $LASTEXITCODE): $c"
                return $false
            }
        } catch {
            Write-Host "  [FAIL] Command failed: $c"
            Write-Host "         $_"
            return $false
        }
    }
    Write-Host "  [OK] Installation complete. Re-probing..."
    return $true
}

# Step 0: Prerequisites check (JDK 8+ and Maven 3.6+)
Write-Host "Step 0: Checking prerequisites..."
$attempt = 1
while ($true) {
    $failReason = $null
    $needTool = $null
    $javaVerLine = $null
    $mvnVerLine = $null

    $javaBin = Get-Command java -ErrorAction SilentlyContinue
    if (-not $javaBin) {
        Write-Host "  [FAIL] 'java' not found on PATH."
        $failReason = 'missing'
        $needTool = 'jdk'
    } else {
        $javaVerLine = (& java -version 2>&1 | Select-Object -First 1).ToString()
        if ($javaVerLine -match 'version "?(\d[\d.]+)') {
            $javaVer = $Matches[1]
            $javaMajor = [int]($javaVer -split '\.')[0]
            if ($javaMajor -eq 1) { $javaMajor = [int]($javaVer -split '\.')[1] }
            if ($javaMajor -lt 8) {
                Write-Host "  [FAIL] Found Java '$javaVerLine', need JDK 8+."
                $failReason = 'too_old'
                $needTool = 'jdk'
            }
        } else {
            Write-Host "  [FAIL] Cannot parse Java version from '$javaVerLine'."
            $failReason = 'missing'
            $needTool = 'jdk'
        }
    }

    if (-not $failReason) {
        $mvnBin = Get-Command mvn -ErrorAction SilentlyContinue
        if (-not $mvnBin) {
            Write-Host "  [FAIL] 'mvn' not found on PATH."
            $failReason = 'missing'
            $needTool = 'maven'
        } else {
            $mvnVerLine = (& mvn -version 2>&1 | Select-Object -First 1).ToString()
            if ($mvnVerLine -match 'Maven (\d[\d.]+)') {
                $mvnVer = $Matches[1]
                $parts = $mvnVer -split '\.'
                $mvnMajor = [int]$parts[0]; $mvnMinor = [int]$parts[1]
                if ($mvnMajor -lt 3 -or ($mvnMajor -eq 3 -and $mvnMinor -lt 6)) {
                    Write-Host "  [FAIL] Found Maven '$mvnVer', need 3.6+."
                    $failReason = 'too_old'
                    $needTool = 'maven'
                }
            } else {
                Write-Host "  [FAIL] Cannot parse Maven version from '$mvnVerLine'."
                $failReason = 'missing'
                $needTool = 'maven'
            }
        }
    }

    if (-not $failReason) {
        Write-Host "  [OK] Java:  $javaVerLine"
        Write-Host "  [OK] Maven: $mvnVerLine"
        break
    }

    if ($attempt -ge 2) {
        Write-Host "  [FAIL] Prerequisites still not satisfied after install attempt. Aborting."
        exit 1
    }
    if (-not (Invoke-OfferInstallTool -Tool $needTool)) {
        exit 1
    }
    $attempt++
}
Write-Host ""

# Marker written after a successful dependency resolution / install. Mirrors
# the .sh script. Removed by `mvn clean`; pom.xml mtime invalidates it.
$DepsMarker = Join-Path 'target' '.cu-setup-deps-ok'

function Test-DepsMarkerValid {
    if (-not (Test-Path $DepsMarker)) { return $false }
    if (-not (Test-Path 'pom.xml')) { return $true }
    $markerTime = (Get-Item $DepsMarker).LastWriteTimeUtc
    $pomTime = (Get-Item 'pom.xml').LastWriteTimeUtc
    return $markerTime -ge $pomTime
}

# Step 1: Install SDK dependencies
Write-Host "Step 1: Installing SDK dependencies..."
if (Test-DepsMarkerValid) {
    Write-Host "  [OK] Dependencies already resolved (marker $DepsMarker present and up-to-date); skipping"
    Write-Host "    To force re-resolution: Remove-Item $DepsMarker  (or run 'mvn clean')"
} else {
    $modeChoice = Read-Host "  Installation mode — (A) Download deps only (recommended) | (B) Local build from source [A/b]"
    if ($modeChoice -match '^[Bb]$') {
        Write-Host "  Running: mvn install -DskipTests -Djacoco.skip=true"
        & mvn install -DskipTests -Djacoco.skip=true -q
        if ($LASTEXITCODE -ne 0) { Write-Host "  [ERROR] mvn install failed." -ForegroundColor Red; exit 1 }
    } else {
        Write-Host "  Running: mvn dependency:resolve"
        & mvn dependency:resolve -q
        if ($LASTEXITCODE -ne 0) { Write-Host "  [ERROR] mvn dependency:resolve failed." -ForegroundColor Red; exit 1 }
    }
    if (-not (Test-Path 'target')) { New-Item -ItemType Directory -Path 'target' | Out-Null }
    New-Item -ItemType File -Path $DepsMarker -Force | Out-Null
    Write-Host "  [OK] Dependencies ready"
}
Write-Host ""

# Step 2: Configure .env file
Write-Host "Step 2: Configuring .env file..."
$envFile = Join-Path $PackageRoot '.env'
$createEnv = $true
if (Test-Path $envFile) {
    Write-Host "  [WARN] .env file already exists - NOT overwriting"
    Write-Host "  If you want to start fresh, delete .env manually: Remove-Item $envFile"
    $keepEnv = Read-Host "  Continue with existing .env? (Y/n)"
    if ($keepEnv -match '^[Nn]$') {
        Write-Host "  Aborting. Remove .env and re-run this script."
        exit 1
    }
    $createEnv = $false
}

# Escape a value for safe inclusion in a .env file consumed by
# `set -a && source .env && set +a` in bash. Wraps in single quotes
# and escapes internal single quotes as '\''.
#
# Contract (must stay in sync with setup_user_env.sh / load-env.ps1):
#   - Every value written by this script is wrapped in single quotes.
#   - Internal single quotes are encoded as the 4-char sequence: '\''
#   - bash `source .env` strips the wrapping quotes natively.
#   - load-env.ps1 strips the wrapping quotes and reverses the '\'' escape.
function Format-EnvValue {
    param([string]$Value)
    if ($null -eq $Value) { $Value = '' }
    $escaped = $Value -replace "'", "'\''"
    return "'$escaped'"
}

# Write a UTF-8 (no BOM) text file; cross-platform safe for downstream `source .env`.
function Write-Utf8NoBom {
    param([string]$Path, [string]$Content)
    $utf8NoBom = New-Object System.Text.UTF8Encoding($false)
    [System.IO.File]::WriteAllText($Path, $Content, $utf8NoBom)
}

$skipUpdateDefaults = $false

if ($createEnv) {
    $configureNow = Read-Host "Would you like to configure variables interactively now? (Y/n)"
    if ($configureNow -notmatch '^[Nn]$') {
        Write-Host ""

        # CONTENTUNDERSTANDING_ENDPOINT
        $endpoint = Read-Host "  CONTENTUNDERSTANDING_ENDPOINT (e.g., https://<resource>.services.ai.azure.com/)"

        # Auth method
        Write-Host "  Authentication:"
        Write-Host "    (A) DefaultAzureCredential via 'az login' (recommended)"
        Write-Host "    (B) API Key"
        $authMode = Read-Host "  Choose [A/b]"
        $apiKey = ''
        if ($authMode -match '^[Bb]$') {
            $apiKey = Read-Host "  CONTENTUNDERSTANDING_KEY"
        } else {
            Write-Host "  [INFO] Using DefaultAzureCredential - make sure to run 'az login'"
        }

        # Probe existing model defaults on the Foundry resource before prompting
        $gpt41 = ''; $gpt41mini = ''; $embedding = ''
        if ($endpoint) {
            Write-Host ""
            Write-Host "  Probing existing model defaults on the Foundry resource..."
            $probeEndpoint = $endpoint.TrimEnd('/')
            $headers = @{}
            $probeOk = $false
            $skipProbe = $false
            try {
                if ($apiKey) {
                    $headers['Ocp-Apim-Subscription-Key'] = $apiKey
                } else {
                    $azCmd = Get-Command az -ErrorAction SilentlyContinue
                    if (-not $azCmd) {
                        Write-Host "  [WARN] Azure CLI ('az') not found; cannot acquire token. Continuing with manual entry."
                        $skipProbe = $true
                    } else {
                        $tokenJson = az account get-access-token --resource https://cognitiveservices.azure.com 2>$null | ConvertFrom-Json
                        if ($tokenJson -and $tokenJson.accessToken) {
                            $headers['Authorization'] = "Bearer $($tokenJson.accessToken)"
                        } else {
                            Write-Host "  [WARN] Probe unavailable (no token from 'az account get-access-token')."
                            Write-Host "    Run 'az login' and ensure Cognitive Services User role. Continuing with manual entry."
                            $skipProbe = $true
                        }
                    }
                }
                if (-not $skipProbe) {
                    # -TimeoutSec guards against the script hanging when the
                    # endpoint is unreachable (DNS, TLS, network outage).
                    $resp = Invoke-RestMethod `
                        -Uri "$probeEndpoint/contentunderstanding/defaults?api-version=2025-11-01" `
                        -Headers $headers `
                        -TimeoutSec 15 `
                        -ErrorAction Stop
                    $probeOk = $true
                }
            } catch {
                # $_.Exception.Response is null for transport-layer failures
                # (DNS, TLS, timeout). Guard before dereferencing.
                $statusCode = $null
                if ($_.Exception.Response) {
                    try { $statusCode = [int]$_.Exception.Response.StatusCode } catch { $statusCode = $null }
                }
                if ($statusCode -eq 401 -or $statusCode -eq 403) {
                    Write-Host "  [WARN] Probe unavailable (authentication failed)."
                    Write-Host "    If you're using DefaultAzureCredential, run 'az login' and ensure"
                    Write-Host "    the Cognitive Services User role is assigned. Continuing with manual entry."
                } else {
                    Write-Host "  [WARN] Probe failed: $($_.Exception.Message). Continuing with manual entry."
                }
            }

            if ($probeOk -and $resp.modelDeployments) {
                $md = $resp.modelDeployments
                $gpt41 = if ($md.'gpt-4.1') { $md.'gpt-4.1' } else { '' }
                $gpt41mini = if ($md.'gpt-4.1-mini') { $md.'gpt-4.1-mini' } else { '' }
                $embedding = if ($md.'text-embedding-3-large') { $md.'text-embedding-3-large' } else { '' }

                if ($gpt41 -and $gpt41mini -and $embedding) {
                    Write-Host "  [OK] Detected existing defaults:"
                    Write-Host "      gpt-4.1              = $gpt41"
                    Write-Host "      gpt-4.1-mini         = $gpt41mini"
                    Write-Host "      text-embedding-3-large = $embedding"
                    $useDetected = Read-Host "  Use these detected values? (Y/n)"
                    if ($useDetected -notmatch '^[Nn]$') {
                        $skipUpdateDefaults = $true
                    } else {
                        $gpt41 = ''; $gpt41mini = ''; $embedding = ''
                    }
                } elseif ($gpt41 -or $gpt41mini -or $embedding) {
                    Write-Host "  [INFO] Partial defaults detected; missing entries will be prompted below."
                } else {
                    Write-Host "  [INFO] No existing defaults detected; continuing with manual entry."
                }
            }
        }

        Write-Host ""
        Write-Host "  Model deployment configuration (for Sample00_UpdateDefaults):"

        if (-not $gpt41) {
            $gpt41 = Read-Host "  GPT_4_1_DEPLOYMENT (default: gpt-4.1)"
            if (-not $gpt41) { $gpt41 = 'gpt-4.1' }
        } else {
            Write-Host "  [OK] Using detected GPT_4_1_DEPLOYMENT=$gpt41"
        }

        if (-not $gpt41mini) {
            $gpt41mini = Read-Host "  GPT_4_1_MINI_DEPLOYMENT (default: gpt-4.1-mini)"
            if (-not $gpt41mini) { $gpt41mini = 'gpt-4.1-mini' }
        } else {
            Write-Host "  [OK] Using detected GPT_4_1_MINI_DEPLOYMENT=$gpt41mini"
        }

        if (-not $embedding) {
            $embedding = Read-Host "  TEXT_EMBEDDING_3_LARGE_DEPLOYMENT (default: text-embedding-3-large)"
            if (-not $embedding) { $embedding = 'text-embedding-3-large' }
        } else {
            Write-Host "  [OK] Using detected TEXT_EMBEDDING_3_LARGE_DEPLOYMENT=$embedding"
        }

        # Cross-resource copy
        $wantCopy = Read-Host "  Configure cross-resource copy vars for Sample15? (y/N)"
        $srcRid = ''; $srcRegion = ''; $tgtEp = ''; $tgtKey = ''; $tgtRid = ''; $tgtRegion = ''
        if ($wantCopy -match '^[Yy]$') {
            $srcRid = Read-Host "    Source resource ID"
            $srcRegion = Read-Host "    Source region (e.g., eastus)"
            $tgtEp = Read-Host "    Target endpoint"
            $tgtKey = Read-Host "    Target API key (blank = DefaultAzureCredential)"
            $tgtRid = Read-Host "    Target resource ID"
            $tgtRegion = Read-Host "    Target region (e.g., swedencentral)"
        }

        # Build .env content with safely-quoted values
        $endpointQ  = Format-EnvValue $endpoint
        $apiKeyQ    = Format-EnvValue $apiKey
        $gpt41Q     = Format-EnvValue $gpt41
        $gpt41miniQ = Format-EnvValue $gpt41mini
        $embeddingQ = Format-EnvValue $embedding

        $envContent = @"
# Azure AI Content Understanding - Environment Variables
# Generated by cu-sdk-setup/scripts/setup_user_env.ps1

# Required: Your Microsoft Foundry resource endpoint
CONTENTUNDERSTANDING_ENDPOINT=$endpointQ

# Optional: API key (leave empty to use DefaultAzureCredential via az login)
CONTENTUNDERSTANDING_KEY=$apiKeyQ

# Model deployment names (used by Sample00_UpdateDefaults)
GPT_4_1_DEPLOYMENT=$gpt41Q
GPT_4_1_MINI_DEPLOYMENT=$gpt41miniQ
TEXT_EMBEDDING_3_LARGE_DEPLOYMENT=$embeddingQ
"@

        if ($wantCopy -match '^[Yy]$') {
            $srcRidQ    = Format-EnvValue $srcRid
            $srcRegionQ = Format-EnvValue $srcRegion
            $tgtEpQ     = Format-EnvValue $tgtEp
            $tgtKeyQ    = Format-EnvValue $tgtKey
            $tgtRidQ    = Format-EnvValue $tgtRid
            $tgtRegionQ = Format-EnvValue $tgtRegion
            $envContent += @"

# Cross-resource copy settings (only for Sample15_GrantCopyAuth)
CONTENTUNDERSTANDING_SOURCE_RESOURCE_ID=$srcRidQ
CONTENTUNDERSTANDING_SOURCE_REGION=$srcRegionQ
CONTENTUNDERSTANDING_TARGET_ENDPOINT=$tgtEpQ
CONTENTUNDERSTANDING_TARGET_KEY=$tgtKeyQ
CONTENTUNDERSTANDING_TARGET_RESOURCE_ID=$tgtRidQ
CONTENTUNDERSTANDING_TARGET_REGION=$tgtRegionQ
"@
        }

        Write-Utf8NoBom -Path $envFile -Content $envContent
        Write-Host "  [OK] Wrote $envFile"
    } else {
        $templateContent = @'
# Azure AI Content Understanding - Environment Variables
# Fill in your values below.

# Required: Your Microsoft Foundry resource endpoint
CONTENTUNDERSTANDING_ENDPOINT=https://<your-resource>.services.ai.azure.com/

# Optional: API key (leave empty to use DefaultAzureCredential via az login)
CONTENTUNDERSTANDING_KEY=

# Model deployment names (used by Sample00_UpdateDefaults)
GPT_4_1_DEPLOYMENT=gpt-4.1
GPT_4_1_MINI_DEPLOYMENT=gpt-4.1-mini
TEXT_EMBEDDING_3_LARGE_DEPLOYMENT=text-embedding-3-large
'@
        Write-Utf8NoBom -Path $envFile -Content $templateContent
        Write-Host "  [OK] Wrote template to $envFile - please edit it before running samples."
    }
}
Write-Host ""

# Generate a tiny loader helper next to .env so users (and Copilot) don't have
# to remember a fragile one-liner. Strips matching surrounding single/double
# quotes and un-escapes '\'' for single-quoted values.
#
# Skip overwrite if a load-env.ps1 already exists AND it is not the one we
# generated previously (identified by the fingerprint marker on the first
# non-shebang line). This protects user customisations from being clobbered.
$loaderPath = Join-Path $PackageRoot 'load-env.ps1'
$loaderFingerprint = '# cu-sdk-setup-load-env-v1'
$shouldWriteLoader = $true
if (Test-Path $loaderPath) {
    $firstLines = Get-Content -LiteralPath $loaderPath -TotalCount 5 -ErrorAction SilentlyContinue
    if (-not ($firstLines -contains $loaderFingerprint)) {
        Write-Host "  [WARN] $loaderPath already exists and looks user-modified - not overwriting."
        $shouldWriteLoader = $false
    }
}
$loaderBody = @'
# cu-sdk-setup-load-env-v1
# Load .env into the current PowerShell session. Generated by cu-sdk-setup.
# Usage:  . ./load-env.ps1
param([string]$EnvFile = '.env')
if (-not (Test-Path $EnvFile)) {
    Write-Error "$EnvFile not found in $(Get-Location)"
    return
}
Get-Content -LiteralPath $EnvFile | ForEach-Object {
    $line = $_
    if ($line -match '^\s*#') { return }
    if ($line -notmatch '^\s*([^=\s]+)\s*=(.*)$') { return }
    $name = $Matches[1]
    $val = $Matches[2]
    if ($val -match "^'(.*)'$") {
        $val = $Matches[1] -replace "'\\''", "'"
    } elseif ($val -match '^"(.*)"$') {
        $val = $Matches[1]
    }
    [System.Environment]::SetEnvironmentVariable($name, $val, 'Process')
}
'@
if ($shouldWriteLoader) {
    Write-Utf8NoBom -Path $loaderPath -Content $loaderBody
}

# Summary
Write-Host "=== Setup Complete ==="
Write-Host ""
Write-Host "Next steps:"
Write-Host ""
Write-Host "  1. Load .env into your current shell (Java reads System.getenv, so this is REQUIRED):"
Write-Host "       cd $PackageRoot"
if ($IsWindows -or $PSVersionTable.PSEdition -eq 'Desktop') {
    Write-Host "       . ./load-env.ps1   # PowerShell (uses generated $loaderPath)"
} else {
    Write-Host "       set -a && source .env && set +a   # (in bash)"
    Write-Host "       . ./load-env.ps1                  # (in PowerShell)"
}
Write-Host ""
if ($skipUpdateDefaults) {
    Write-Host "  2. Model defaults already configured on your Foundry resource; skip Sample00_UpdateDefaults."
} else {
    Write-Host "  2. (One-time per Foundry resource) Configure model defaults:"
    Write-Host '       mvn exec:java \'
    Write-Host '         -Dexec.mainClass="com.azure.ai.contentunderstanding.samples.Sample00_UpdateDefaults" \'
    Write-Host '         -Dexec.classpathScope=test -Djacoco.skip=true -q'
}
Write-Host ""
Write-Host "  3. Run a sample:"
Write-Host '       mvn exec:java \'
Write-Host '         -Dexec.mainClass="com.azure.ai.contentunderstanding.samples.Sample02_AnalyzeUrl" \'
Write-Host '         -Dexec.classpathScope=test -Djacoco.skip=true -q'
Write-Host ""
