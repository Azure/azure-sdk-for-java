#!/bin/bash
# Run a compiled Content Understanding SDK sample
# Usage: ./run-sample.sh <SampleClassName>
# Example: ./run-sample.sh Sample02_AnalyzeUrlAsync

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

# Check if classpath exists
if [ ! -f target/classpath.txt ]; then
    echo "Error: target/classpath.txt not found. Run build-classpath.sh first."
    exit 1
fi

# Check if sample is compiled
SAMPLE_CLASS_FILE="target/classes/com/azure/ai/contentunderstanding/samples/${SAMPLE_CLASS}.class"
if [ ! -f "$SAMPLE_CLASS_FILE" ]; then
    echo "Error: Sample not compiled. Run compile-all-samples.sh first."
    exit 1
fi

CLASSPATH=$(cat target/classpath.txt):target/classes

echo "Running sample: $SAMPLE_CLASS"
java -cp "$CLASSPATH" "com.azure.ai.contentunderstanding.samples.$SAMPLE_CLASS"
