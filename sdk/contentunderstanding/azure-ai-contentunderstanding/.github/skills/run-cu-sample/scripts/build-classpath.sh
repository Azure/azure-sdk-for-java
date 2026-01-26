#!/bin/bash
# Build classpath for Content Understanding SDK
# Usage: ./build-classpath.sh

set -e  # Exit on error

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"

cd "$PROJECT_ROOT"

echo "Building classpath..."
mvn dependency:build-classpath -Dmdep.outputFile=target/classpath.txt -q

if [ -f target/classpath.txt ]; then
    echo "Classpath saved to target/classpath.txt"
    echo "Classpath length: $(wc -c < target/classpath.txt) bytes"
else
    echo "Error: Failed to generate classpath.txt"
    exit 1
fi
