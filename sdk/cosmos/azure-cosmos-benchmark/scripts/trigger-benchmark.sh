#!/bin/bash
# trigger-benchmark.sh — Checkout a branch/PR, build, and run benchmark
#
# Usage:
#   ./trigger-benchmark.sh --branch <name> --scenario <scenario> --tenants <file> [options]
#   ./trigger-benchmark.sh --pr <number> --scenario <scenario> --tenants <file> [options]
#   ./trigger-benchmark.sh --compare <branch-a> <branch-b> --scenario <scenario> --tenants <file>

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
SDK_DIR="${SDK_DIR:-$(cd "$SCRIPT_DIR/../../.." && pwd)}"
BRANCH=""
PR_NUMBER=""
COMPARE_A=""
COMPARE_B=""
SCENARIO="SCALING"
TENANTS_FILE="tenants.json"
RESULT_SINK="CSV"
SKIP_BUILD=false
EXTRA_ARGS=""

while [[ $# -gt 0 ]]; do
    case $1 in
        --branch)       BRANCH="$2"; shift ;;
        --pr)           PR_NUMBER="$2"; shift ;;
        --compare)      COMPARE_A="$2"; COMPARE_B="$3"; shift 2 ;;
        --scenario)     SCENARIO="$2"; shift ;;
        --tenants)      TENANTS_FILE="$2"; shift ;;
        --result-sink)  RESULT_SINK="$2"; shift ;;
        --sdk-dir)      SDK_DIR="$2"; shift ;;
        --skip-build)   SKIP_BUILD=true ;;
        *)              EXTRA_ARGS="$EXTRA_ARGS $1" ;;
    esac
    shift
done

build_and_run() {
    local ref="$1"
    local label="$2"

    echo ""
    echo "════════════════════════════════════════════════════════"
    echo "  Building and running: $label ($ref)"
    echo "════════════════════════════════════════════════════════"

    cd "$SDK_DIR"

    # Checkout
    if [[ "$ref" =~ ^[0-9]+$ ]]; then
        echo "Fetching PR #${ref}..."
        git fetch origin pull/${ref}/head:pr-${ref}
        git checkout pr-${ref}
    else
        echo "Checking out branch: $ref"
        git fetch origin "$ref"
        git checkout "$ref"
        git pull origin "$ref" 2>/dev/null || true
    fi

    COMMIT_ID=$(git rev-parse --short HEAD)
    BRANCH_NAME=$(git rev-parse --abbrev-ref HEAD)
    echo "  Commit: $COMMIT_ID"

    # Build
    if [[ "$SKIP_BUILD" == "false" ]]; then
        echo "Building azure-cosmos + benchmark module..."
        mvn install -pl sdk/cosmos/azure-cosmos -am -DskipTests -q
        mvn package -pl sdk/cosmos/azure-cosmos-benchmark -DskipTests -q
        echo "Build complete."
    fi

    # Run
    local OUTPUT_DIR="./results/$(date +%Y%m%dT%H%M%S)-${label}-${SCENARIO}"
    "$SCRIPT_DIR/run-benchmark.sh" "$SCENARIO" "$TENANTS_FILE" "$OUTPUT_DIR" \
        --branch "$BRANCH_NAME" \
        ${PR_NUMBER:+--pr "$PR_NUMBER"} \
        --result-sink "$RESULT_SINK" \
        $EXTRA_ARGS

    echo "  Results: $OUTPUT_DIR"
    echo "$OUTPUT_DIR" >> .last-benchmark-runs
}

if [[ -n "$COMPARE_A" && -n "$COMPARE_B" ]]; then
    build_and_run "$COMPARE_A" "before"
    build_and_run "$COMPARE_B" "after"
    echo ""
    echo "════════════════════════════════════════════════════════"
    echo "  Both runs complete. Compare results:"
    tail -2 .last-benchmark-runs
    echo "════════════════════════════════════════════════════════"
elif [[ -n "$PR_NUMBER" ]]; then
    build_and_run "$PR_NUMBER" "pr-${PR_NUMBER}"
elif [[ -n "$BRANCH" ]]; then
    build_and_run "$BRANCH" "$BRANCH"
else
    echo "Usage: $0 --branch <name> | --pr <number> | --compare <branch-a> <branch-b>"
    exit 1
fi
