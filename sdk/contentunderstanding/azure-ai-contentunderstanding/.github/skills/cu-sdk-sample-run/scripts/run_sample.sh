#!/usr/bin/env bash
set -euo pipefail
# cspell:ignore envfile esac

# run_sample.sh
# Run a specific Java sample for the Azure AI Content Understanding SDK.
# Compiles the samples module (if needed) and runs the specified sample class
# using mvn exec:java.
#
# Usage:
#   run_sample.sh <SampleClassName> [--env <env-file>] [--dry-run]
# Examples:
#   run_sample.sh Sample02_AnalyzeUrl
#   run_sample.sh Sample02_AnalyzeUrlAsync
#   run_sample.sh Sample02_AnalyzeUrl --env .env
#   run_sample.sh --list

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# Package root is 4 levels up from scripts: .github/skills/cu-sdk-sample-run/scripts -> package root
PACKAGE_ROOT="$(cd "$SCRIPT_DIR/../../../.." && pwd)"
SAMPLES_DIR="$PACKAGE_ROOT/src/samples/java/com/azure/ai/contentunderstanding/samples"
PACKAGE="com.azure.ai.contentunderstanding.samples"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Defaults
DRY_RUN=0
ENV_FILE=""
SAMPLE_NAME=""

print_info() { echo -e "${BLUE}$1${NC}"; }
print_success() { echo -e "${GREEN}$1${NC}"; }
print_warning() { echo -e "${YELLOW}$1${NC}"; }
print_error() { echo -e "${RED}$1${NC}"; }

print_help() {
  cat <<EOF
Usage: $(basename "$0") <SampleClassName> [OPTIONS]

Run a specific Java sample for the Azure AI Content Understanding SDK.

Arguments:
  <SampleClassName>   Sample class name (e.g., Sample02_AnalyzeUrl).
                      The .java extension is optional.

Options:
  --env <file>        Load environment variables from the given .env file before running.
  --dry-run           Print what would be executed without running.
  --list              List available samples and exit.
  --help, -h          Show this help message.

Examples:
  $(basename "$0") Sample02_AnalyzeUrl
  $(basename "$0") Sample02_AnalyzeUrlAsync
  $(basename "$0") Sample02_AnalyzeUrl --env .env
  $(basename "$0") --list
EOF
}

list_samples() {
  echo ""
  print_info "=== Available Sync Samples ==="
  for f in "$SAMPLES_DIR"/Sample*.java; do
    [ -f "$f" ] || continue
    local name
    name="$(basename "$f" .java)"
    # Skip async samples in this section
    [[ "$name" == *Async ]] && continue
    echo "  $name"
  done
  echo ""
  print_info "=== Available Async Samples ==="
  for f in "$SAMPLES_DIR"/Sample*Async.java; do
    [ -f "$f" ] || continue
    local name
    name="$(basename "$f" .java)"
    echo "  $name"
  done
  echo ""
}

# Load environment variables from a .env file.
#
# Only simple NAME=VALUE assignments are accepted (with an optional leading
# `export `). Names must be valid shell identifiers ([A-Za-z_][A-Za-z0-9_]*).
# A single matching pair of surrounding double or single quotes is stripped
# from the value. Anything else is skipped with a warning. We deliberately
# avoid `eval` so a malicious or malformed .env file cannot execute arbitrary
# commands or trigger command substitution.
load_env_file() {
  local envfile="$1"
  if [[ ! -f "$envfile" ]]; then
    print_error "Error: .env file not found: $envfile"
    exit 1
  fi
  print_info "Loading environment variables from: $envfile"
  local line name value lineno=0
  while IFS= read -r line || [[ -n "$line" ]]; do
    lineno=$((lineno + 1))
    # Skip empty lines and comments
    [[ -z "$line" || "$line" =~ ^[[:space:]]*# ]] && continue
    # Strip optional leading `export ` (with surrounding whitespace)
    line="${line#"${line%%[![:space:]]*}"}"  # strip leading whitespace
    line="${line#export }"
    # Require NAME=VALUE with a valid identifier on the left
    if [[ ! "$line" =~ ^([A-Za-z_][A-Za-z0-9_]*)=(.*)$ ]]; then
      print_warning "  Skipping line $lineno (not a NAME=VALUE assignment)"
      continue
    fi
    name="${BASH_REMATCH[1]}"
    value="${BASH_REMATCH[2]}"
    # Strip a single matching pair of surrounding double or single quotes
    if [[ "$value" =~ ^\"(.*)\"$ ]]; then
      value="${BASH_REMATCH[1]}"
    elif [[ "$value" =~ ^\'(.*)\'$ ]]; then
      value="${BASH_REMATCH[1]}"
    fi
    export "$name=$value"
  done < "$envfile"
  print_success "✓ Environment variables loaded"
}

# Parse arguments
while [[ $# -gt 0 ]]; do
  case "$1" in
    --help|-h)
      print_help
      exit 0
      ;;
    --list|-l)
      list_samples
      exit 0
      ;;
    --dry-run)
      DRY_RUN=1
      shift
      ;;
    --env)
      if [[ -z "${2:-}" ]]; then
        print_error "Error: --env requires a file path argument"
        exit 1
      fi
      ENV_FILE="$2"
      shift 2
      ;;
    -*)
      print_error "Unknown option: $1"
      print_help
      exit 1
      ;;
    *)
      if [[ -z "$SAMPLE_NAME" ]]; then
        SAMPLE_NAME="$1"
      else
        print_error "Error: Multiple samples specified. Only one sample is supported."
        exit 1
      fi
      shift
      ;;
  esac
done

if [[ -z "$SAMPLE_NAME" ]]; then
  print_error "Error: No sample name provided"
  echo ""
  print_help
  exit 1
fi

# Normalize: strip .java extension if provided
SAMPLE_NAME="${SAMPLE_NAME%.java}"

# Verify the sample file exists
SAMPLE_FILE="$SAMPLES_DIR/${SAMPLE_NAME}.java"
if [[ ! -f "$SAMPLE_FILE" ]]; then
  print_error "Error: Sample not found: $SAMPLE_FILE"
  echo ""
  echo "Did you mean one of these?"
  ls "$SAMPLES_DIR"/Sample*.java 2>/dev/null | xargs -n1 basename | sed 's/\.java$//' | grep -i "${SAMPLE_NAME}" | head -5 || true
  echo ""
  echo "Run '$(basename "$0") --list' to see all available samples"
  exit 1
fi

FULL_CLASS="${PACKAGE}.${SAMPLE_NAME}"

echo ""
print_info "=== Run Java Sample ==="
echo "Package root: $PACKAGE_ROOT"
echo "Sample class: $FULL_CLASS"
echo "Sample file:  $SAMPLE_FILE"
echo ""

# Navigate to package root
cd "$PACKAGE_ROOT"

# Load .env file if specified
if [[ -n "$ENV_FILE" ]]; then
  # Resolve relative path from original cwd
  if [[ "$ENV_FILE" != /* ]]; then
    ENV_FILE="$PACKAGE_ROOT/$ENV_FILE"
  fi
  load_env_file "$ENV_FILE"
  echo ""
fi

# Check for required environment variable
if [[ -z "${CONTENTUNDERSTANDING_ENDPOINT:-}" ]]; then
  print_warning "⚠ CONTENTUNDERSTANDING_ENDPOINT is not set. Most samples will fail without it."
  echo "  Set it with: export CONTENTUNDERSTANDING_ENDPOINT=\"https://your-foundry.services.ai.azure.com/\""
  echo "  Or use: $(basename "$0") $SAMPLE_NAME --env .env"
  echo ""
fi

# Sample16 demo-mode banner: warn if the user is about to run the labeled-data
# sample without configuring either Option A (SAS URL) or Option B (storage
# account + container) — the sample will still run but skip the labeled-data
# code path.
if [[ "$SAMPLE_NAME" == Sample16* ]]; then
  if [[ -z "${CONTENTUNDERSTANDING_TRAINING_DATA_SAS_URL:-}" ]]; then
    if [[ -z "${CONTENTUNDERSTANDING_TRAINING_DATA_STORAGE_ACCOUNT:-}" \
          || -z "${CONTENTUNDERSTANDING_TRAINING_DATA_CONTAINER:-}" ]]; then
      print_warning "⚠ DEMO MODE: no training data configured for $SAMPLE_NAME."
      echo "  The analyzer will be created without labeled data ('Knowledge sources: 0')."
      echo "  To exercise the labeled-data API path, configure ONE of:"
      echo "    Option A: CONTENTUNDERSTANDING_TRAINING_DATA_SAS_URL=<container SAS URL>"
      echo "    Option B: CONTENTUNDERSTANDING_TRAINING_DATA_STORAGE_ACCOUNT=<account>"
      echo "              CONTENTUNDERSTANDING_TRAINING_DATA_CONTAINER=<container>"
      echo "  then re-run: set -a && source .env && set +a"
      echo ""
    fi
  fi
fi

# Build command. Sample classes live under src/samples/java and are compiled
# as test sources, so we must run test-compile before exec:java; otherwise on
# a clean checkout the sample class will not exist on the classpath.
MVN_CMD="mvn -DskipTests test-compile exec:java -Dexec.mainClass=\"${FULL_CLASS}\" -Dexec.classpathScope=test"

if [[ $DRY_RUN -eq 1 ]]; then
  echo "DRY RUN: would execute:"
  echo "  cd $PACKAGE_ROOT"
  [[ -n "${ENV_FILE:-}" ]] && echo "  (env loaded from $ENV_FILE)"
  echo "  $MVN_CMD"
  exit 0
fi

# Run the sample
print_info "Running: $SAMPLE_NAME"
echo ""
mvn -DskipTests test-compile exec:java -Dexec.mainClass="${FULL_CLASS}" -Dexec.classpathScope=test

echo ""
print_success "✓ Sample completed: $SAMPLE_NAME"
