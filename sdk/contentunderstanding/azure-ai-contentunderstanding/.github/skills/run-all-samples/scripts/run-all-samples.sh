#!/bin/bash
# Run all Content Understanding SDK samples (both sync and async variants)
# Ensures SDK and samples are compiled, then runs each sample and saves output to files
# Usage: ./run-all-samples.sh [--reset]
#   --reset: Force re-run all samples, even if output files already exist

set -e  # Exit on error

# Check for reset flag
RESET_MODE=false
if [ "$1" == "--reset" ] || [ "$1" == "reset" ] || [ "${RESET_AND_RUN_ALL_SAMPLES}" == "true" ]; then
    RESET_MODE=true
    echo "Reset mode enabled: Will delete output directory and re-run all samples."
fi

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../../../.." && pwd)"

cd "$PROJECT_ROOT"

# Load .env file if it exists
if [ -f .env ]; then
    echo "Loading environment variables from .env..."
    set -a
    source .env
    set +a
else
    echo "Warning: .env file not found. Samples may fail without proper configuration."
fi

# Step 1: Ensure CU SDK is compiled
echo "=========================================="
echo "Step 1: Checking CU SDK compilation..."
echo "=========================================="

if [ ! -d target/classes ] || [ -z "$(ls -A target/classes 2>/dev/null)" ]; then
    echo "CU SDK not compiled. Compiling now..."
    COMPILE_SDK_SCRIPT=".github/skills/compile-cu-sdk-in-place/scripts/compile-cu-sdk.sh"
    if [ -f "$COMPILE_SDK_SCRIPT" ]; then
        bash "$COMPILE_SDK_SCRIPT"
    else
        echo "Error: compile-cu-sdk.sh not found. Running mvn compile directly..."
        mvn compile -DskipTests
    fi
else
    echo "CU SDK already compiled."
fi

# Step 2: Ensure samples are compiled
echo ""
echo "=========================================="
echo "Step 2: Checking sample compilation..."
echo "=========================================="

SAMPLE_CLASS_FILE="target/classes/com/azure/ai/contentunderstanding/samples/Sample00_UpdateDefaults.class"
if [ ! -f "$SAMPLE_CLASS_FILE" ]; then
    echo "Samples not compiled. Compiling now..."
    COMPILE_SAMPLES_SCRIPT=".github/skills/run-cu-sample/scripts/compile-all-samples.sh"
    if [ -f "$COMPILE_SAMPLES_SCRIPT" ]; then
        bash "$COMPILE_SAMPLES_SCRIPT"
    else
        echo "Error: compile-all-samples.sh not found. Compiling samples directly..."
        # Build classpath if needed
        if [ ! -f target/classpath.txt ]; then
            mvn dependency:build-classpath -Dmdep.outputFile=target/classpath.txt -q
        fi
        CLASSPATH=$(cat target/classpath.txt):target/classes
        mkdir -p target/classes
        javac -cp "$CLASSPATH" --release 8 -d target/classes \
            src/samples/java/com/azure/ai/contentunderstanding/samples/*.java
    fi
else
    echo "Samples already compiled."
fi

# Ensure classpath exists
if [ ! -f target/classpath.txt ]; then
    echo "Building classpath..."
    mvn dependency:build-classpath -Dmdep.outputFile=target/classpath.txt -q
fi

CLASSPATH=$(cat target/classpath.txt):target/classes

# Step 3: Enumerate all samples
echo ""
echo "=========================================="
echo "Step 3: Enumerating samples..."
echo "=========================================="

SAMPLES_DIR="src/samples/java/com/azure/ai/contentunderstanding/samples"
OUTPUT_DIR="target/sample_result_out_txt"

# Delete output directory if reset mode is enabled
if [ "$RESET_MODE" = true ]; then
    if [ -d "$OUTPUT_DIR" ]; then
        echo "Reset mode: Deleting existing output directory: $OUTPUT_DIR"
        rm -rf "$OUTPUT_DIR"
    fi
fi

# Create output directory
mkdir -p "$OUTPUT_DIR"

# Find all sample files and extract base names
declare -A SAMPLE_BASES
for sample_file in "$SAMPLES_DIR"/*.java; do
    if [ -f "$sample_file" ]; then
        filename=$(basename "$sample_file" .java)
        # Extract base name (remove Async suffix if present)
        if [[ "$filename" == *Async ]]; then
            base_name="${filename%Async}"
            SAMPLE_BASES["$base_name"]=1
        else
            SAMPLE_BASES["$filename"]=1
        fi
    fi
done

# Sort base names
IFS=$'\n' sorted_bases=($(sort <<<"${!SAMPLE_BASES[*]}"))
unset IFS

echo "Found ${#SAMPLE_BASES[@]} sample groups:"
for base in "${sorted_bases[@]}"; do
    sync_exists=""
    async_exists=""
    if [ -f "target/classes/com/azure/ai/contentunderstanding/samples/${base}.class" ]; then
        sync_exists="[sync]"
    fi
    if [ -f "target/classes/com/azure/ai/contentunderstanding/samples/${base}Async.class" ]; then
        async_exists="[async]"
    fi
    echo "  - $base $sync_exists $async_exists"
done

# Step 4: Run each sample
echo ""
echo "=========================================="
echo "Step 4: Running all samples..."
echo "=========================================="

TOTAL_SAMPLES=0
SUCCESSFUL=0
FAILED=0
SKIPPED=0

for base in "${sorted_bases[@]}"; do
    # Run sync version if it exists
    if [ -f "target/classes/com/azure/ai/contentunderstanding/samples/${base}.class" ]; then
        OUTPUT_FILE="$OUTPUT_DIR/${base}.out.txt"
        
        # Check if output already exists (unless reset mode)
        if [ "$RESET_MODE" = false ] && [ -f "$OUTPUT_FILE" ]; then
            echo ""
            echo "Skipping: $base (sync) - output already exists: $OUTPUT_FILE"
            SKIPPED=$((SKIPPED + 1))
        else
            TOTAL_SAMPLES=$((TOTAL_SAMPLES + 1))
            echo ""
            echo "Running: $base (sync) -> $OUTPUT_FILE"
            
            if java -cp "$CLASSPATH" "com.azure.ai.contentunderstanding.samples.$base" > "$OUTPUT_FILE" 2>&1; then
                echo "  ✓ Success"
                SUCCESSFUL=$((SUCCESSFUL + 1))
            else
                echo "  ✗ Failed (exit code: $?)"
                FAILED=$((FAILED + 1))
            fi
        fi
    fi
    
    # Run async version if it exists
    if [ -f "target/classes/com/azure/ai/contentunderstanding/samples/${base}Async.class" ]; then
        OUTPUT_FILE="$OUTPUT_DIR/${base}Async.out.txt"
        
        # Check if output already exists (unless reset mode)
        if [ "$RESET_MODE" = false ] && [ -f "$OUTPUT_FILE" ]; then
            echo ""
            echo "Skipping: ${base}Async - output already exists: $OUTPUT_FILE"
            SKIPPED=$((SKIPPED + 1))
        else
            TOTAL_SAMPLES=$((TOTAL_SAMPLES + 1))
            echo ""
            echo "Running: ${base}Async -> $OUTPUT_FILE"
            
            if java -cp "$CLASSPATH" "com.azure.ai.contentunderstanding.samples.${base}Async" > "$OUTPUT_FILE" 2>&1; then
                echo "  ✓ Success"
                SUCCESSFUL=$((SUCCESSFUL + 1))
            else
                echo "  ✗ Failed (exit code: $?)"
                FAILED=$((FAILED + 1))
            fi
        fi
    fi
done

# Summary
echo ""
echo "=========================================="
echo "Summary"
echo "=========================================="
echo "Total samples run: $TOTAL_SAMPLES"
echo "Successful: $SUCCESSFUL"
echo "Failed: $FAILED"
if [ $SKIPPED -gt 0 ]; then
    echo "Skipped (already have output): $SKIPPED"
fi
echo "Output directory: $OUTPUT_DIR"
echo ""
if [ $TOTAL_SAMPLES -eq 0 ] && [ $SKIPPED -gt 0 ]; then
    echo "All samples already have output files. Use --reset to re-run all samples."
    exit 0
elif [ $FAILED -eq 0 ]; then
    if [ $SKIPPED -gt 0 ]; then
        echo "All remaining samples completed successfully! ($SKIPPED samples were skipped)"
    else
        echo "All samples completed successfully!"
    fi
    exit 0
else
    echo "Some samples failed. Check output files in $OUTPUT_DIR for details."
    exit 1
fi
