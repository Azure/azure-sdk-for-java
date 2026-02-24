#!/bin/bash
# capture-diagnostics.sh — Capture thread/heap dumps of a running benchmark
#
# Usage:
#   ./capture-diagnostics.sh [--threads] [--heap] [--jfr <duration>] [--output-dir <dir>] [--all]

set -euo pipefail

CAPTURE_THREADS=false
CAPTURE_HEAP=false
JFR_DURATION=""
OUTPUT_DIR="./diagnostics/$(date +%Y%m%dT%H%M%S)"

while [[ $# -gt 0 ]]; do
    case $1 in
        --threads)     CAPTURE_THREADS=true ;;
        --heap)        CAPTURE_HEAP=true ;;
        --jfr)         JFR_DURATION="$2"; shift ;;
        --output-dir)  OUTPUT_DIR="$2"; shift ;;
        --all)         CAPTURE_THREADS=true; CAPTURE_HEAP=true; JFR_DURATION="60" ;;
        *) echo "Unknown option: $1"; exit 1 ;;
    esac
    shift
done

mkdir -p "$OUTPUT_DIR"

# Find the benchmark JVM PID
BENCH_PID=$(jps -l 2>/dev/null | grep -E 'azure-cosmos-benchmark|MultiTenancyBenchmark' | awk '{print $1}')
if [[ -z "$BENCH_PID" ]]; then
    echo "ERROR: No running benchmark JVM found."
    echo "Running Java processes:"
    jps -l 2>/dev/null || echo "(jps not available)"
    exit 1
fi
echo "Found benchmark PID: $BENCH_PID"

# Thread dump
if [[ "$CAPTURE_THREADS" == "true" ]]; then
    THREAD_FILE="$OUTPUT_DIR/thread-dump-$(date +%H%M%S).txt"
    echo "Capturing thread dump..."
    jstack "$BENCH_PID" > "$THREAD_FILE" 2>&1
    echo "  Thread dump: $THREAD_FILE ($(wc -l < "$THREAD_FILE") lines)"
    echo "" >> "$THREAD_FILE"
    echo "=== Thread Name Prefix Summary ===" >> "$THREAD_FILE"
    grep '"' "$THREAD_FILE" | sed 's/"\([^"]*\)".*/\1/' | sed 's/-[0-9]*$//' | sort | uniq -c | sort -rn >> "$THREAD_FILE"
fi

# Heap dump
if [[ "$CAPTURE_HEAP" == "true" ]]; then
    HEAP_FILE="$OUTPUT_DIR/heap-dump-$(date +%H%M%S).hprof"
    echo "Capturing heap dump (this may take a minute)..."
    jmap -dump:live,format=b,file="$HEAP_FILE" "$BENCH_PID"
    HEAP_SIZE_MB=$(du -m "$HEAP_FILE" | awk '{print $1}')
    echo "  Heap dump: $HEAP_FILE (${HEAP_SIZE_MB} MB)"
fi

# JFR recording
if [[ -n "$JFR_DURATION" ]]; then
    JFR_FILE="$OUTPUT_DIR/recording-$(date +%H%M%S).jfr"
    echo "Starting JFR recording for ${JFR_DURATION}s..."
    jcmd "$BENCH_PID" JFR.start duration="${JFR_DURATION}s" filename="$JFR_FILE" settings=profile
    echo "  JFR will be saved to: $JFR_FILE (after ${JFR_DURATION}s)"
fi

# Quick process stats
echo ""
echo "=== Process Stats ==="
echo "Threads: $(ls /proc/$BENCH_PID/task 2>/dev/null | wc -l || echo 'N/A')"
echo "FDs:     $(ls /proc/$BENCH_PID/fd 2>/dev/null | wc -l || echo 'N/A')"
echo ""
echo "Diagnostics saved to: $OUTPUT_DIR"
