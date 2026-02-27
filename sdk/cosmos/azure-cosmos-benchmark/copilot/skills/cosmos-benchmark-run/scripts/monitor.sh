#!/bin/bash
# monitor.sh  External resource monitor for Java benchmark process
#
# Usage:
#   ./monitor.sh <PID> [interval_sec] [output_dir]
#   ./monitor.sh $(pgrep -f MultiTenancyBenchmark) 10 ./results/run1
#
# Writes monitor.csv with columns:
#   timestamp, threads, fds, rss_kb, cpu_pct, heap_used_kb, heap_max_kb, gc_count, gc_time_ms

set -euo pipefail

PID=${1:?Usage: monitor.sh <PID> [interval] [output_dir]}
INTERVAL=${2:-10}
OUTDIR=${3:-.}

mkdir -p "$OUTDIR"
OUTFILE="$OUTDIR/monitor.csv"

echo "timestamp,threads,fds,rss_kb,cpu_pct,heap_used_kb,heap_max_kb,gc_count,gc_time_ms" > "$OUTFILE"
echo "Monitoring PID=$PID every ${INTERVAL}s -> $OUTFILE"

while kill -0 "$PID" 2>/dev/null; do
    TS=$(date -u +%Y-%m-%dT%H:%M:%SZ)
    THREADS=$(ls /proc/$PID/task 2>/dev/null | wc -l || echo 0)
    FDS=$(ls /proc/$PID/fd 2>/dev/null | wc -l || echo 0)
    RSS=$(ps -p $PID -o rss= 2>/dev/null | tr -d ' ' || echo 0)
    CPU=$(ps -p $PID -o %cpu= 2>/dev/null | tr -d ' ' || echo 0)

    # Parse jstat -gc output for heap usage
    JSTAT=$(jstat -gc $PID 2>/dev/null | tail -1 || echo "")
    if [[ -n "$JSTAT" ]]; then
        # Columns: S0C S1C S0U S1U EC EU OC OU MC MU CCSC CCSU YGC YGCT FGC FGCT CGC CGCT GCT
        HEAP_USED=$(echo "$JSTAT" | awk '{printf "%.0f", ($4+$6+$8)}')  # S1U+EU+OU in KB
        HEAP_MAX=$(echo "$JSTAT" | awk '{printf "%.0f", ($1+$2+$5+$7)}')  # S0C+S1C+EC+OC in KB
        GC_COUNT=$(echo "$JSTAT" | awk '{printf "%.0f", ($13+$15+$17)}')  # YGC+FGC+CGC
        GC_TIME=$(echo "$JSTAT" | awk '{printf "%.0f", $NF * 1000}')  # GCT in ms
    else
        HEAP_USED=0; HEAP_MAX=0; GC_COUNT=0; GC_TIME=0
    fi

    echo "$TS,$THREADS,$FDS,$RSS,$CPU,$HEAP_USED,$HEAP_MAX,$GC_COUNT,$GC_TIME" >> "$OUTFILE"
    sleep "$INTERVAL"
done

echo "Process $PID exited. Monitor stopped. Results in $OUTFILE"
