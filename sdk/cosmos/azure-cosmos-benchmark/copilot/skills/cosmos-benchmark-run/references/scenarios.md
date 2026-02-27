# Operation Catalog & Custom Scenarios

## All 20 Operation Types

| Operation | Description | Use Case |
|---|---|---|
| `ReadThroughput` | Point reads, max throughput | Baseline read performance |
| `ReadLatency` | Point reads, measure latency | P50/P99 latency profiling |
| `WriteThroughput` | Point writes, max throughput | Write performance |
| `WriteLatency` | Point writes, measure latency | Write latency profiling |
| `QuerySingle` | Single-partition query | Query baseline |
| `QuerySingleMany` | Single-partition query, many results | Large result set handling |
| `QueryParallel` | Parallel cross-partition query | Fanout query performance |
| `QueryCross` | Cross-partition query | Cross-partition overhead |
| `QueryInClauseParallel` | IN clause with parallel execution | Batch lookup pattern |
| `QueryOrderby` | Query with ORDER BY | Sort performance |
| `QueryAggregate` | Aggregate query (COUNT, SUM) | Aggregation overhead |
| `QueryAggregateTopOrderby` | Aggregate + TOP + ORDER BY | Complex query performance |
| `QueryTopOrderby` | TOP + ORDER BY | Pagination pattern |
| `Mixed` | 90% read, 9% write, 1% query | Realistic mixed workload |
| `ReadMyWrites` | Write then read same document | Session consistency validation |
| `ReadManyLatency` | ReadMany API, measure latency | Batch read latency |
| `ReadManyThroughput` | ReadMany API, max throughput | Batch read throughput |
| `ReadAllItemsOfLogicalPartition` | Read all items in a partition | Partition scan performance |
| `CtlWorkload` | CTL benchmark workload | Internal benchmark |
| `LinkedInCtlWorkload` | LinkedIn-specific CTL variant | Partner-specific workload |

## Custom Scenario Examples

### High-concurrency write stress test

```bash
-operation WriteThroughput -concurrency 100 -numberOfOperations 100000 \
-documentDataFieldSize 1024 -documentDataFieldCount 5
```

### Mixed workload with large documents

```bash
-operation Mixed -concurrency 50 -numberOfOperations 50000 \
-documentDataFieldSize 4096 -documentDataFieldCount 10 \
-readWriteQueryReadManyPct "70,20,5,5"
```

### Query performance profiling

```bash
-operation QueryParallel -concurrency 20 -numberOfOperations 10000 \
-numberOfPreCreatedDocuments 10000
```

### Encryption benchmark

```bash
-operation ReadThroughput -concurrency 20 -numberOfOperations 10000 \
-encryptionEnabled true -encryptedStringFieldCount 3 -encryptedLongFieldCount 2
```

## Key Tuning Parameters

| Flag | Default | Description |
|---|---|---|
| `-concurrency` | 20 | Concurrent operations |
| `-numberOfOperations` | 100000 | Total operations per run |
| `-numberOfPreCreatedDocuments` | 1000 | Documents seeded before benchmark |
| `-documentDataFieldSize` | 20 | Bytes per data field |
| `-documentDataFieldCount` | 1 | Number of data fields per document |
| `-connectionMode` | GATEWAY | GATEWAY or DIRECT |
| `-consistencyLevel` | SESSION | Session, Eventual, Strong, etc. |
| `-maxConnectionPoolSize` | 1000 | Max HTTP connections per client |
| `-cycles` | 1 | Lifecycle repetitions (>1 enables leak detection) |
| `-settleTimeMs` | 90000 | Wait between cycles (auto when cycles>1) |
| `-gcBetweenCycles` | true | Force GC between cycles (auto when cycles>1) |
| `-printingInterval` | 10 | Metrics reporting interval (seconds) |
| `-maxRunningTimeDuration` | - | Max wall-clock time (ISO 8601 duration) |
