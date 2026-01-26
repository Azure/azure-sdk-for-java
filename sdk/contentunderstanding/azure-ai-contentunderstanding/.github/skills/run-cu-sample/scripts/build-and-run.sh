#!/bin/bash
# Build and run a Content Understanding SDK sample
# Prerequisite: CU SDK must be compiled first (use compile-cu-sdk-in-place skill)
# Usage: ./build-and-run.sh <SampleClassName>
# Example: ./build-and-run.sh Sample02_AnalyzeUrlAsync

set -e  # Exit on error

if [ $# -eq 0 ]; then
    echo "Usage: $0 <SampleClassName>"
    echo "Example: $0 Sample02_AnalyzeUrlAsync"
    exit 1
fi

SAMPLE_CLASS=$1
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"

cd "$PROJECT_ROOT"

# Check if CU SDK is compiled
if [ ! -d target/classes ] || [ -z "$(ls -A target/classes 2>/dev/null)" ]; then
    echo "Error: CU SDK not compiled. Please use the compile-cu-sdk-in-place skill first."
    echo "Or run: ../compile-cu-sdk-in-place/scripts/compile-cu-sdk.sh"
    exit 1
fi

# Build classpath
"$SCRIPT_DIR/build-classpath.sh"

# Compile all samples
"$SCRIPT_DIR/compile-all-samples.sh"

# Run the sample
"$SCRIPT_DIR/run-sample.sh" "$SAMPLE_CLASS"
