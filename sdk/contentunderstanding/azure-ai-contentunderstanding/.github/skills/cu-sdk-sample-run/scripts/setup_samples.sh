#!/usr/bin/env bash
set -euo pipefail

# setup_samples.sh
# Sets up the environment for running Azure AI Content Understanding Java SDK samples.
# This includes:
# 1. Check Java and Maven are installed
# 2. Try resolving the SDK package from Maven Central (if published)
# 3. If not available, fall back to building locally with mvn install
# 4. Create a sample .env file if none exists
#
# Usage:
#   setup_samples.sh [--local] [--skip-build]
# Options:
#   --local         Force local build (skip Maven Central check)
#   --skip-build    Skip building even in local mode (assumes already built)

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PACKAGE_ROOT="$(cd "$SCRIPT_DIR/../../../.." && pwd)"

GROUP_ID="com.azure"
ARTIFACT_ID="azure-ai-contentunderstanding"
# Extract version from pom.xml
VERSION="$(grep -m1 '<version>' "$PACKAGE_ROOT/pom.xml" | sed 's/.*<version>\(.*\)<\/version>.*/\1/' | sed 's/\s*//g')"

FORCE_LOCAL=0
SKIP_BUILD=0

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

print_info() { echo -e "${BLUE}$1${NC}"; }
print_success() { echo -e "${GREEN}$1${NC}"; }
print_warning() { echo -e "${YELLOW}$1${NC}"; }
print_error() { echo -e "${RED}$1${NC}"; }

print_help() {
  cat <<EOF
Usage: $(basename "$0") [OPTIONS]

Sets up the Azure AI Content Understanding Java SDK samples environment.

By default, checks if the SDK package is available on Maven Central.
If not yet published, automatically falls back to building locally.

Options:
  --local         Force local build (skip Maven Central check)
  --skip-build    Skip building the package (assumes already built locally)
  --help, -h      Show this help message

Examples:
  $(basename "$0")                        # Try Maven Central first, fall back to local
  $(basename "$0") --local                # Force local build
  $(basename "$0") --local --skip-build   # Skip build (already built)
EOF
}

# Parse arguments
while [[ $# -gt 0 ]]; do
  case "$1" in
    --help|-h)
      print_help
      exit 0
      ;;
    --local)
      FORCE_LOCAL=1
      shift
      ;;
    --skip-build)
      SKIP_BUILD=1
      shift
      ;;
    *)
      print_error "Unknown option: $1"
      print_help
      exit 1
      ;;
  esac
done

echo "========================================="
echo "Azure AI Content Understanding Java SDK"
echo "Sample Setup Script"
echo "========================================="
echo ""
echo "Package root:  $PACKAGE_ROOT"
echo "Artifact:      ${GROUP_ID}:${ARTIFACT_ID}:${VERSION}"
echo ""

# =========================================
# Step 0: Check prerequisites
# =========================================
echo "Step 0: Checking prerequisites..."

if ! command -v java &>/dev/null; then
  print_error "Error: Java is not installed or not on PATH."
  echo "  Install JDK 8 or later: https://learn.microsoft.com/java/openjdk/download"
  exit 1
fi
JAVA_VER="$(java -version 2>&1 | head -1)"
echo "  Java: $JAVA_VER"

if ! command -v mvn &>/dev/null; then
  print_error "Error: Maven is not installed or not on PATH."
  echo "  Install Maven: https://maven.apache.org/install.html"
  exit 1
fi
MVN_VER="$(mvn -version 2>&1 | head -1)"
echo "  Maven: $MVN_VER"

print_success "✓ Prerequisites OK"
echo ""

# =========================================
# Step 1: Install the SDK package
# =========================================
# Default: check if published on Maven Central. If available, no build needed
# (Maven will download it automatically when running samples).
# If not published, fall back to local build.

check_maven_central() {
  echo "Step 1: Checking Maven Central for ${GROUP_ID}:${ARTIFACT_ID}:${VERSION}..."
  local group_path="${GROUP_ID//\.//}"
  local url="https://repo1.maven.org/maven2/${group_path}/${ARTIFACT_ID}/${VERSION}/${ARTIFACT_ID}-${VERSION}.pom"

  if curl -sf --head "$url" &>/dev/null; then
    print_success "✓ Package is available on Maven Central"
    echo "  Maven will download it automatically when running samples."
    return 0
  else
    echo "  Package not yet published on Maven Central"
    return 1
  fi
}

build_local() {
  echo "Step 1: Building package locally..."
  cd "$PACKAGE_ROOT"

  if [[ $SKIP_BUILD -eq 1 ]]; then
    echo "  Skipping build (--skip-build)"
    # Verify the artifact exists in local Maven repo
    local local_jar="$HOME/.m2/repository/${GROUP_ID//\.//}/${ARTIFACT_ID}/${VERSION}/${ARTIFACT_ID}-${VERSION}.jar"
    if [[ -f "$local_jar" ]]; then
      print_success "✓ Package found in local Maven repository"
    else
      print_warning "⚠ Package not found in local Maven repository: $local_jar"
      echo "  Run without --skip-build to build it."
    fi
    return 0
  fi

  echo "  Building with: mvn install -DskipTests"
  if mvn install -DskipTests; then
    print_success "✓ Package built and installed to local Maven repository"
  else
    print_error "Error: Build failed."
    echo "  Common fixes:"
    echo "    - Ensure JDK 8+ is installed: java -version"
    echo "    - Build parent POM first: mvn install -DskipTests -f ../../parents/azure-client-sdk-parent/pom.xml"
    exit 1
  fi
}

if [[ $FORCE_LOCAL -eq 1 ]]; then
  echo "(--local flag: skipping Maven Central check, building locally)"
  echo ""
  build_local
else
  if ! check_maven_central; then
    echo "  Falling back to local build..."
    echo ""
    build_local
  fi
fi
echo ""

# =========================================
# Step 2: Create .env file if needed
# =========================================
echo "Step 2: Checking .env file..."
ENV_FILE="$PACKAGE_ROOT/.env"

if [[ -f "$ENV_FILE" ]]; then
  print_success "✓ .env file already exists at $ENV_FILE"
else
  print_info "Creating sample .env file..."
  cat > "$ENV_FILE" <<'ENVEOF'
# Azure AI Content Understanding - Environment Variables
# Fill in your values below. See SKILL.md for details.

# Required: Your Microsoft Foundry resource endpoint
CONTENTUNDERSTANDING_ENDPOINT=https://your-foundry.services.ai.azure.com/

# Optional: API key (leave empty to use DefaultAzureCredential via az login)
CONTENTUNDERSTANDING_KEY=

# Model deployment names (used by Sample00_UpdateDefaults)
GPT_4_1_DEPLOYMENT=gpt-4.1
GPT_4_1_MINI_DEPLOYMENT=gpt-4.1-mini
TEXT_EMBEDDING_3_LARGE_DEPLOYMENT=text-embedding-3-large

# Cross-resource copy (only needed for Sample15_GrantCopyAuth)
# CONTENTUNDERSTANDING_TARGET_ENDPOINT=https://your-target-foundry.services.ai.azure.com/
# CONTENTUNDERSTANDING_TARGET_RESOURCE_ID=/subscriptions/{subscriptionId}/resourceGroups/{resourceGroup}/providers/Microsoft.CognitiveServices/accounts/{targetAccountName}
ENVEOF
  print_success "✓ Created .env file at $ENV_FILE"
  print_warning "⚠ Please edit $ENV_FILE and fill in your actual values before running samples"
fi
echo ""

echo "========================================="
echo "✓ Setup complete!"
echo "========================================="
echo ""
echo "Next steps:"
echo "  1. Edit .env with your endpoint and credentials (if not done already):"
echo "     $ENV_FILE"
echo ""
echo "  2. Run a sample:"
echo "     cd $PACKAGE_ROOT"
echo "     .github/skills/cu-sdk-sample-run/scripts/run_sample.sh Sample02_AnalyzeUrl --env .env"
echo ""
echo "  Or export variables manually and use Maven directly:"
echo "     export CONTENTUNDERSTANDING_ENDPOINT=\"https://...\""
echo "     mvn exec:java -Dexec.mainClass=\"com.azure.ai.contentunderstanding.samples.Sample02_AnalyzeUrl\""
echo ""
