# Test Results

This directory stores benchmark results downloaded from the VM.

Each run is stored in a subdirectory named by run ID:
```
test-results/
  R2-20260220T055015-CHURN/
    resource_snapshots.csv
    benchmark.log
    gc.log
    git-info.json
    thread-dumps/
    heap-dumps/
  R3-20260220T064730-CHURN-prefix/
    ...
```

## Downloading Results from VM

```bash
# Download a specific run:
scp -r benchuser@<VM_IP>:~/azure-sdk-for-java/results/<run-dir> test-results/

# Download all results:
scp -r benchuser@<VM_IP>:~/azure-sdk-for-java/results/* test-results/
```

## Analyzing Results

Use the multi-tenancy-benchmark-analyze skill or run:
```bash
# Compare heap dumps:
python3 .github/skills/multi-tenancy-benchmark-heapdump/references/parse_hprof.py \
  --diff test-results/<run>/heap-dumps/heap-PRE_CLOSE-*.hprof \
         test-results/<run>/heap-dumps/heap-POST_CLOSE-*.hprof --top 20
```

All files in this directory are gitignored.
