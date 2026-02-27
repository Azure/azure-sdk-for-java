# Kusto Table Schema for Benchmark Results

## Table: BenchmarkResults

```kql
.create table BenchmarkResults (
    RunId: string,
    Timestamp: datetime,
    Branch: string,
    CommitSha: string,
    Scenario: string,
    TenantCount: int,
    Operation: string,
    ConnectionMode: string,
    // Monitor metrics (from monitor.csv)
    Threads: int,
    FileDescriptors: int,
    RssKb: long,
    CpuPct: real,
    HeapUsedKb: long,
    HeapMaxKb: long,
    GcCount: int,
    GcTimeMs: long,
    // Computed metrics
    Phase: string,
    ThreadDelta: int,
    HeapRatio: real,
    // Verdict
    Passed: bool,
    FailReason: string
)
```

## Ingestion from CSV

### Step 1: Prepare CSV for ingestion

Add run metadata columns to monitor.csv:

```bash
# On the VM or locally after downloading results:
RUN_ID="<run-name>"
BRANCH=$(jq -r .branch results/<run-name>/git-info.json)
COMMIT=$(jq -r .commitId results/<run-name>/git-info.json)

awk -v run="$RUN_ID" -v branch="$BRANCH" -v commit="$COMMIT" \
  'NR>1 {print run","$0","branch","commit}' \
  results/<run-name>/monitor.csv > results/<run-name>/monitor-enriched.csv
```

### Step 2: Ingest into Kusto

```kql
.ingest into table BenchmarkResults (
    h'https://<storage-account>.blob.core.windows.net/<container>/monitor-enriched.csv'
) with (format='csv', ignoreFirstRecord=true)
```

Or inline from local file via Kusto Explorer / Azure Data Explorer web UI.

### Step 3: Query examples

```kql
// Latest runs summary
BenchmarkResults
| summarize MaxThreads=max(Threads), MaxHeapMB=max(HeapUsedKb)/1024,
            FinalThreads=arg_max(Timestamp, Threads), FinalHeapKb=arg_max(Timestamp, HeapUsedKb)
  by RunId, Branch, Scenario
| order by Timestamp desc

// Compare two runs
let baseline = "20260226-CHURN-main-baseline";
let fix = "20260226-CHURN-fix-leak";
BenchmarkResults
| where RunId in (baseline, fix)
| summarize MaxThreads=max(Threads), MaxHeapMB=max(HeapUsedKb)/1024 by RunId
| order by RunId

// Trend over time (multiple runs)
BenchmarkResults
| where Scenario == "CHURN"
| summarize FinalThreads=arg_max(Timestamp, Threads) by RunId, Branch
| order by Timestamp asc
| render timechart
```

## Table: BenchmarkSummary

Aggregated per-run summary (one row per run):

```kql
.create table BenchmarkSummary (
    RunId: string,
    Timestamp: datetime,
    Branch: string,
    CommitSha: string,
    Scenario: string,
    TenantCount: int,
    BaselineThreads: int,
    PeakThreads: int,
    FinalThreads: int,
    ThreadDelta: int,
    BaselineHeapKb: long,
    PeakHeapKb: long,
    FinalHeapKb: long,
    HeapRatio: real,
    PeakFDs: int,
    TotalGcCount: int,
    TotalGcTimeMs: long,
    Passed: bool,
    FailReasons: string
)
```
