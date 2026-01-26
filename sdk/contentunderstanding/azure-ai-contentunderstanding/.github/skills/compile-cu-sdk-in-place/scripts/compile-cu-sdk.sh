#!/bin/bash
# Compile Content Understanding SDK main code in place
# This script is for SDK development or debugging SDK issues.
# For consuming CU SDK directly, see sdk/contentunderstanding/azure-ai-contentunderstanding/README.md
# Usage: ./compile-cu-sdk.sh

set -e  # Exit on error

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"

cd "$PROJECT_ROOT"

echo "Compiling CU SDK main code (src/main/java)..."
mvn compile -DskipTests

if [ ! -d target/classes ] || [ -z "$(ls -A target/classes 2>/dev/null)" ]; then
    echo "Error: CU SDK compilation failed - target/classes is empty or missing"
    exit 1
fi

echo "CU SDK compiled successfully to target/classes"
