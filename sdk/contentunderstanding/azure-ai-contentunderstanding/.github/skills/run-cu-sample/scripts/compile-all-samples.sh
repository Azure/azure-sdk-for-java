#!/bin/bash
# Compile all Content Understanding SDK samples
# Usage: ./compile-all-samples.sh

set -e  # Exit on error

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"

cd "$PROJECT_ROOT"

# Check if main SDK is compiled
if [ ! -d target/classes ] || [ -z "$(ls -A target/classes 2>/dev/null)" ]; then
    echo "Error: CU SDK not compiled. Please use the compile-cu-sdk-in-place skill first."
    echo "Or run: ../compile-cu-sdk-in-place/scripts/compile-cu-sdk.sh"
    exit 1
fi

# Check if classpath exists
if [ ! -f target/classpath.txt ]; then
    echo "Classpath not found. Building classpath..."
    "$SCRIPT_DIR/build-classpath.sh"
fi

CLASSPATH=$(cat target/classpath.txt):target/classes

# Ensure target/classes exists
mkdir -p target/classes

echo "Compiling all samples..."
javac -cp "$CLASSPATH" --release 8 -d target/classes \
    src/samples/java/com/azure/ai/contentunderstanding/samples/*.java

echo "All samples compiled successfully to target/classes"
