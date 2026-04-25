#!/bin/bash
# Setup script for Azure AI Content Understanding Java SDK users
# This script sets up the environment for running samples (JDK + Maven based).
# cspell:ignore esac PSEOF

set -e

# Determine script directory and package root
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PACKAGE_ROOT="$(cd "$SCRIPT_DIR/../../../.." && pwd)"

echo "=== Azure AI Content Understanding (Java) - User Environment Setup ==="
echo "Package root: $PACKAGE_ROOT"
echo ""

cd "$PACKAGE_ROOT"

# --- helper: offer to install JDK/Maven via the platform's package manager ---
# Usage: offer_install_tool <tool>
#   tool: "jdk" | "maven"
# Returns 0 if install ran successfully (caller should re-probe), non-zero if
# the user declined, the platform isn't supported, or the install failed.
offer_install_tool() {
    local tool="$1"
    local os_name
    os_name="$(uname -s)"
    local cmd=""

    case "$os_name" in
        Darwin)
            if ! command -v brew >/dev/null 2>&1; then
                echo "  (Homebrew not found — install it first: https://brew.sh/)"
                return 1
            fi
            case "$tool" in
                jdk)   cmd="brew install openjdk@21" ;;
                maven) cmd="brew install maven" ;;
            esac
            ;;
        Linux)
            if ! command -v apt-get >/dev/null 2>&1; then
                echo "  (No apt-get detected — install JDK/Maven with your distro's package manager.)"
                return 1
            fi
            case "$tool" in
                jdk)   cmd="sudo apt-get update && sudo apt-get install -y openjdk-21-jdk" ;;
                maven) cmd="sudo apt-get update && sudo apt-get install -y maven" ;;
            esac
            ;;
        *)
            echo "  (Unsupported platform for auto-install: $os_name)"
            return 1
            ;;
    esac

    echo ""
    echo "  This script can run the following command for you:"
    echo "    $cmd"
    local reply=""
    read -r -p "  Run it now? (y/N): " reply || reply="n"
    if [[ ! "$reply" =~ ^[Yy]$ ]]; then
        echo "  Please run it yourself, then re-run this script."
        return 1
    fi
    if ! eval "$cmd"; then
        echo "  ✗ Installation command failed."
        return 1
    fi
    echo "  ✓ Installation complete. Re-probing..."
    hash -r 2>/dev/null || true
    return 0
}

# Step 0: Prerequisites check (JDK 8+ and Maven 3.6+)
echo "Step 0: Checking prerequisites..."
attempt=1
while :; do
    fail_reason=""
    need_tool=""

    if ! command -v java >/dev/null 2>&1; then
        echo "  ✗ 'java' not found on PATH."
        fail_reason="missing"
        need_tool="jdk"
    else
        java_ver_line="$(java -version 2>&1 | head -1)"
        java_ver="$(echo "$java_ver_line" | sed -n 's/.*version[[:space:]]*"\{0,1\}\([0-9][0-9.]*\).*/\1/p')"
        java_major="${java_ver%%.*}"
        # Handle 1.x style versions (JDK 8 reports as 1.8)
        if [ "$java_major" = "1" ]; then
            java_major="$(echo "$java_ver" | cut -d. -f2)"
        fi
        # Strict numeric check — fail loudly when parsing fails instead of
        # silently treating it as "version OK".
        if ! printf '%s' "$java_major" | grep -qE '^[0-9]+$'; then
            echo "  ✗ Could not parse Java major version from '$java_ver_line'."
            fail_reason="missing"
            need_tool="jdk"
        elif [ "$java_major" -lt 8 ]; then
            echo "  ✗ Found Java '$java_ver_line', need JDK 8+."
            fail_reason="too_old"
            need_tool="jdk"
        fi
    fi

    if [ -z "$fail_reason" ]; then
        if ! command -v mvn >/dev/null 2>&1; then
            echo "  ✗ 'mvn' not found on PATH."
            fail_reason="missing"
            need_tool="maven"
        else
            mvn_ver_line="$(mvn -version 2>&1 | head -1)"
            mvn_ver="$(echo "$mvn_ver_line" | sed -n 's/.*Maven \([0-9][0-9.]*\).*/\1/p')"
            mvn_major="$(echo "$mvn_ver" | cut -d. -f1)"
            mvn_minor="$(echo "$mvn_ver" | cut -d. -f2)"
            if ! printf '%s' "$mvn_major" | grep -qE '^[0-9]+$' || \
               ! printf '%s' "${mvn_minor:-0}" | grep -qE '^[0-9]+$'; then
                echo "  ✗ Could not parse Maven version from '$mvn_ver_line'."
                fail_reason="missing"
                need_tool="maven"
            elif [ "$mvn_major" -lt 3 ] || { [ "$mvn_major" -eq 3 ] && [ "${mvn_minor:-0}" -lt 6 ]; }; then
                echo "  ✗ Found Maven '$mvn_ver', need 3.6+."
                fail_reason="too_old"
                need_tool="maven"
            fi
        fi
    fi

    if [ -z "$fail_reason" ]; then
        echo "  ✓ Java:  $java_ver_line"
        echo "  ✓ Maven: $mvn_ver_line"
        break
    fi

    if [ "$attempt" -ge 2 ]; then
        echo "  ✗ Prerequisites still not satisfied after install attempt. Aborting."
        exit 1
    fi
    if ! offer_install_tool "$need_tool"; then
        exit 1
    fi
    attempt=$((attempt + 1))
done
echo ""

# Marker written after a successful dependency resolution / install. Used to
# avoid reprompting on repeat runs. Removed by `mvn clean`, so a clean tree
# correctly triggers re-resolution. POM mtime is also tracked so a changed
# pom.xml invalidates the marker.
DEPS_MARKER="target/.cu-setup-deps-ok"

deps_marker_valid() {
    [ -f "$DEPS_MARKER" ] || return 1
    [ -f "pom.xml" ] || return 0
    # Marker must be at least as new as pom.xml
    if [ "pom.xml" -nt "$DEPS_MARKER" ]; then
        return 1
    fi
    return 0
}

# Step 1: Install SDK dependencies
echo "Step 1: Installing SDK dependencies..."
if deps_marker_valid; then
    echo "  ✓ Dependencies already resolved (marker $DEPS_MARKER present and up-to-date); skipping"
    echo "    To force re-resolution: rm $DEPS_MARKER  (or run 'mvn clean')"
else
    read -r -p "  Installation mode — (A) Download deps only (recommended) | (B) Local build from source [A/b]: " install_mode || install_mode="A"
    install_mode="${install_mode:-A}"
    if [[ "$install_mode" =~ ^[Bb]$ ]]; then
        echo "  Running: mvn install -DskipTests -Djacoco.skip=true"
        mvn install -DskipTests -Djacoco.skip=true -q
    else
        echo "  Running: mvn dependency:resolve"
        mvn dependency:resolve -q
    fi
    mkdir -p target
    : > "$DEPS_MARKER"
    echo "  ✓ Dependencies ready"
fi
echo ""

# Step 2: Configure .env file
echo "Step 2: Configuring .env file..."
ENV_FILE="$PACKAGE_ROOT/.env"
if [ -f "$ENV_FILE" ]; then
    echo "  ⚠ .env file already exists — NOT overwriting."
    echo "    To start fresh, delete it manually: rm \"$ENV_FILE\""
    read -r -p "  Continue with existing .env? (Y/n): " keep_env || keep_env=""
    if [[ "$keep_env" =~ ^[Nn]$ ]]; then
        echo "  Aborting. Remove .env and re-run this script."
        exit 1
    fi
    CREATE_ENV=false
else
    CREATE_ENV=true
fi

# Escape a value for safe inclusion in a .env file consumed by
# `set -a && source .env && set +a` in bash. Wraps in single quotes
# and escapes internal single quotes as '\''.
#
# Contract (must stay in sync with load-env.ps1):
#   - Every value written by this script is wrapped in single quotes.
#   - Internal single quotes are encoded as the 4-char sequence: '\''
#   - bash `source .env` strips the wrapping quotes natively.
#   - PowerShell load-env.ps1 strips the wrapping quotes and reverses the
#     '\'' escape on read.
escape_env_val() {
    local v="$1"
    # bash native parameter expansion: replace each ' with '\''
    local escaped=${v//\'/\'\\\'\'}
    printf "'%s'" "$escaped"
}

skip_update_defaults=0

if [ "$CREATE_ENV" = true ]; then
    read -r -p "Would you like to configure variables interactively now? (Y/n): " configure_now || configure_now="Y"
    configure_now="${configure_now:-Y}"
    if [[ "$configure_now" =~ ^[Yy]$ ]]; then
        echo ""

        # CONTENTUNDERSTANDING_ENDPOINT
        read -r -p "  CONTENTUNDERSTANDING_ENDPOINT (e.g., https://<resource>.services.ai.azure.com/): " endpoint || endpoint=""

        # Auth method
        echo "  Authentication:"
        echo "    (A) DefaultAzureCredential via 'az login' (recommended)"
        echo "    (B) API Key"
        read -r -p "  Choose [A/b]: " auth_mode || auth_mode="A"
        auth_mode="${auth_mode:-A}"
        api_key=""
        if [[ "$auth_mode" =~ ^[Bb]$ ]]; then
            read -r -p "  CONTENTUNDERSTANDING_KEY: " api_key || api_key=""
        else
            echo "  ℹ Using DefaultAzureCredential — remember to run 'az login' before invoking samples."
        fi

        # Probe existing model defaults on the Foundry resource before prompting.
        # Uses curl to call the defaults API directly.
        gpt41=""
        gpt41mini=""
        embedding=""
        if [ -n "$endpoint" ]; then
            echo ""
            echo "  Probing existing model defaults on the Foundry resource..."
            probe_endpoint="${endpoint%/}"
            # --connect-timeout / --max-time guard against the script hanging
            # when the user provided a wrong/unreachable endpoint.
            curl_opts=(--silent --show-error --connect-timeout 5 --max-time 15 -w "\n%{http_code}")
            set +e
            if [ -n "$api_key" ]; then
                probe_response=$(curl "${curl_opts[@]}" \
                    -H "Ocp-Apim-Subscription-Key: $api_key" \
                    "$probe_endpoint/contentunderstanding/defaults?api-version=2025-11-01" 2>/dev/null)
            else
                token=$(az account get-access-token --resource https://cognitiveservices.azure.com --query accessToken -o tsv 2>/dev/null)
                if [ -z "$token" ]; then
                    probe_response=$'\n403'
                else
                    probe_response=$(curl "${curl_opts[@]}" \
                        -H "Authorization: Bearer $token" \
                        "$probe_endpoint/contentunderstanding/defaults?api-version=2025-11-01" 2>/dev/null)
                fi
            fi
            curl_rc=$?
            set -e

            if [ "$curl_rc" -ne 0 ]; then
                # curl failed at the network/transport layer (DNS, TLS, timeout, ...)
                http_code="000"
                body=""
            else
                http_code=$(printf '%s' "$probe_response" | tail -n1)
                # Strip the trailing http_code line. Use awk to drop the last
                # newline-delimited record, which is robust regardless of body size.
                body=$(printf '%s' "$probe_response" | awk 'NR>1{print prev} {prev=$0}')
            fi

            if [ "$http_code" = "200" ]; then
                # Parse modelDeployments from JSON using grep/sed (no jq dependency)
                gpt41=$(echo "$body" | grep -o '"gpt-4\.1"[[:space:]]*:[[:space:]]*"[^"]*"' | sed 's/.*: *"//;s/"//' | head -1)
                gpt41mini=$(echo "$body" | grep -o '"gpt-4\.1-mini"[[:space:]]*:[[:space:]]*"[^"]*"' | sed 's/.*: *"//;s/"//' | head -1)
                embedding=$(echo "$body" | grep -o '"text-embedding-3-large"[[:space:]]*:[[:space:]]*"[^"]*"' | sed 's/.*: *"//;s/"//' | head -1)

                if [ -n "$gpt41" ] && [ -n "$gpt41mini" ] && [ -n "$embedding" ]; then
                    echo "  ✓ Detected existing defaults:"
                    echo "      gpt-4.1              = $gpt41"
                    echo "      gpt-4.1-mini         = $gpt41mini"
                    echo "      text-embedding-3-large = $embedding"
                    read -r -p "  Use these detected values? (Y/n): " use_detected || use_detected="y"
                    if [[ ! "$use_detected" =~ ^[Nn]$ ]]; then
                        skip_update_defaults=1
                    else
                        gpt41=""; gpt41mini=""; embedding=""
                    fi
                elif [ -n "$gpt41" ] || [ -n "$gpt41mini" ] || [ -n "$embedding" ]; then
                    echo "  ℹ Partial defaults detected; missing entries will be prompted below."
                else
                    echo "  ℹ No existing defaults detected; continuing with manual entry."
                fi
            elif [ "$http_code" = "401" ] || [ "$http_code" = "403" ]; then
                echo "  ⚠ Probe unavailable (authentication failed)."
                echo "    If you're using DefaultAzureCredential, run 'az login' and ensure"
                echo "    the Cognitive Services User role is assigned. Continuing with manual entry."
            elif [ "$http_code" = "000" ]; then
                echo "  ⚠ Probe failed (network error / timeout / unreachable endpoint);"
                echo "    continuing with manual entry. Double-check CONTENTUNDERSTANDING_ENDPOINT."
            else
                echo "  ⚠ Probe failed (HTTP $http_code); continuing with manual entry."
            fi
        fi

        echo ""
        echo "  Model deployment configuration (for Sample00_UpdateDefaults):"

        # GPT_4_1_DEPLOYMENT
        if [ -z "$gpt41" ]; then
            read -r -p "  GPT_4_1_DEPLOYMENT (default: gpt-4.1): " gpt41 || gpt41=""
            gpt41="${gpt41:-gpt-4.1}"
        else
            echo "  ✓ Using detected GPT_4_1_DEPLOYMENT=$gpt41"
        fi

        # GPT_4_1_MINI_DEPLOYMENT
        if [ -z "$gpt41mini" ]; then
            read -r -p "  GPT_4_1_MINI_DEPLOYMENT (default: gpt-4.1-mini): " gpt41mini || gpt41mini=""
            gpt41mini="${gpt41mini:-gpt-4.1-mini}"
        else
            echo "  ✓ Using detected GPT_4_1_MINI_DEPLOYMENT=$gpt41mini"
        fi

        # TEXT_EMBEDDING_3_LARGE_DEPLOYMENT
        if [ -z "$embedding" ]; then
            read -r -p "  TEXT_EMBEDDING_3_LARGE_DEPLOYMENT (default: text-embedding-3-large): " embedding || embedding=""
            embedding="${embedding:-text-embedding-3-large}"
        else
            echo "  ✓ Using detected TEXT_EMBEDDING_3_LARGE_DEPLOYMENT=$embedding"
        fi

        # Cross-resource copy
        read -r -p "  Configure cross-resource copy vars for Sample15? (y/N): " want_copy || want_copy=""
        src_rid=""; src_region=""; tgt_ep=""; tgt_key=""; tgt_rid=""; tgt_region=""
        if [[ "$want_copy" =~ ^[Yy]$ ]]; then
            read -r -p "    Source resource ID: " src_rid || src_rid=""
            read -r -p "    Source region (e.g., eastus): " src_region || src_region=""
            read -r -p "    Target endpoint: " tgt_ep || tgt_ep=""
            read -r -p "    Target API key (blank = DefaultAzureCredential): " tgt_key || tgt_key=""
            read -r -p "    Target resource ID: " tgt_rid || tgt_rid=""
            read -r -p "    Target region (e.g., swedencentral): " tgt_region || tgt_region=""
        fi

        cat > "$ENV_FILE" <<EOF
# Azure AI Content Understanding - Environment Variables
# Generated by cu-sdk-setup/scripts/setup_user_env.sh

# Required: Your Microsoft Foundry resource endpoint
CONTENTUNDERSTANDING_ENDPOINT=$(escape_env_val "$endpoint")

# Optional: API key (leave empty to use DefaultAzureCredential via az login)
CONTENTUNDERSTANDING_KEY=$(escape_env_val "$api_key")

# Model deployment names (used by Sample00_UpdateDefaults)
GPT_4_1_DEPLOYMENT=$(escape_env_val "$gpt41")
GPT_4_1_MINI_DEPLOYMENT=$(escape_env_val "$gpt41mini")
TEXT_EMBEDDING_3_LARGE_DEPLOYMENT=$(escape_env_val "$embedding")
EOF

        if [[ "$want_copy" =~ ^[Yy]$ ]]; then
            cat >> "$ENV_FILE" <<EOF

# Cross-resource copy settings (only for Sample15_GrantCopyAuth)
CONTENTUNDERSTANDING_SOURCE_RESOURCE_ID=$(escape_env_val "$src_rid")
CONTENTUNDERSTANDING_SOURCE_REGION=$(escape_env_val "$src_region")
CONTENTUNDERSTANDING_TARGET_ENDPOINT=$(escape_env_val "$tgt_ep")
CONTENTUNDERSTANDING_TARGET_KEY=$(escape_env_val "$tgt_key")
CONTENTUNDERSTANDING_TARGET_RESOURCE_ID=$(escape_env_val "$tgt_rid")
CONTENTUNDERSTANDING_TARGET_REGION=$(escape_env_val "$tgt_region")
EOF
        fi

        echo "  ✓ Wrote $ENV_FILE"
    else
        cat > "$ENV_FILE" <<'EOF'
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
EOF
        echo "  ✓ Wrote template to $ENV_FILE — please edit it before running samples."
    fi
fi
echo ""

# Generate a tiny PowerShell loader helper next to .env so Windows / PS users
# don't need a fragile copy-paste one-liner. The helper strips matching
# surrounding single/double quotes (which we add for bash safety) before
# exporting, so values reach the JVM unquoted.
#
# Skip overwrite if a load-env.ps1 already exists AND it is not the one we
# generated previously (identified by the LOADER_FINGERPRINT marker line).
# This protects user customisations from being silently clobbered.
LOADER_PATH="$PACKAGE_ROOT/load-env.ps1"
LOADER_FINGERPRINT="# cu-sdk-setup-load-env-v1"
if [ -f "$LOADER_PATH" ] && ! grep -q "$LOADER_FINGERPRINT" "$LOADER_PATH" 2>/dev/null; then
    echo "  ⚠ $LOADER_PATH already exists and looks user-modified — not overwriting."
else
    cat > "$LOADER_PATH" <<'PSEOF'
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
PSEOF
fi

# Summary
echo "=== Setup Complete ==="
echo ""
echo "Next steps:"
echo ""
echo "  1. Load .env into your current shell (Java reads System.getenv, so this is REQUIRED):"
echo "       cd $PACKAGE_ROOT"
echo "       set -a && source .env && set +a   # bash / zsh"
echo "       . ./load-env.ps1                  # PowerShell"
echo ""
if [ "$skip_update_defaults" = "1" ]; then
    echo "  2. Model defaults already configured on your Foundry resource; skip Sample00_UpdateDefaults."
else
    echo "  2. (One-time per Foundry resource) Configure model defaults:"
    echo "       mvn exec:java \\"
    echo "         -Dexec.mainClass=\"com.azure.ai.contentunderstanding.samples.Sample00_UpdateDefaults\" \\"
    echo "         -Dexec.classpathScope=test -Djacoco.skip=true -q"
fi
echo ""
echo "  3. Run a sample:"
echo "       mvn exec:java \\"
echo "         -Dexec.mainClass=\"com.azure.ai.contentunderstanding.samples.Sample02_AnalyzeUrl\" \\"
echo "         -Dexec.classpathScope=test -Djacoco.skip=true -q"
echo ""
echo "     Or use the sample-run helper:"
echo "       .github/skills/cu-sdk-sample-run/scripts/run_sample.sh Sample02_AnalyzeUrl --env .env"
echo ""
