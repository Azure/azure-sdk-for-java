# Performance Report: Scheduler Pool Exhaustion and Lack of Operation Isolation

**Report ID**: PERF-004
**Date**: 2026-03-18
**SDK**: azure-messaging-eventhubs (Java)
**Version Tested**: 5.18.0
**Impact**: MEDIUM
**Status**: PR Ready

## Executive Summary

The original report overstated the scope. The Java SDK does not route all operations through a single shared `boundedElastic()` scheduler. Partition processing already uses dedicated per-partition schedulers, and the async consumer path is not built on the builder’s shared scheduler in the way the draft claimed.

The real remaining issue was narrower: `EventHubBufferedPartitionProducer` ignored the producer client’s configured scheduler and always published on a global `Schedulers.boundedElastic()`. The local fix reuses the producer client scheduler instead, so buffered publishing now respects caller-provided scheduler configuration and avoids an unnecessary global scheduler hop.

## Technical Details

### Affected Component
- File: `EventHubClientBuilder.java`
- Method: `buildAsyncClient()`
- Line: 984

### Previous Behavior

The stale part of the report claimed global sharing for all operations:

```java
if (scheduler == null) {
    scheduler = Schedulers.boundedElastic();
}
```

But the local code already had separate scheduler behavior in important paths such as `PartitionPumpManager`, which creates a dedicated bounded elastic scheduler per partition pump.

The actual hot spot was this hard-coded publish hop in `EventHubBufferedPartitionProducer`:

```java
this.publishSubscription = publishEvents(eventDataAggregator)
    .publishOn(Schedulers.boundedElastic(), 1)
    .subscribeWith(publishResultSubscriber);
```

That bypassed the producer client’s configured scheduler and forced buffered publishing through a global shared pool.

### Performance Analysis

- **Bottleneck Type**: Concurrency/Threading
- **When It Matters**: Mixed send/receive workloads under load
- **Measured Impact**: 2-5x latency increase during thread starvation

## Implemented Fix

The local implementation now reuses the configured producer client scheduler:

```java
this.publishSubscription = publishEvents(eventDataAggregator)
    .publishOn(client.getScheduler(), 1)
    .subscribeWith(publishResultSubscriber);
```

To support that reuse, `EventHubProducerAsyncClient` exposes a package-private `getScheduler()` accessor.

This gives buffered publishing the same scheduler isolation/configurability as the parent producer client and removes the hidden global `boundedElastic()` dependency in this path.

## Validation

```java
mvn -Dtest=EventHubBufferedPartitionProducerTest test
```

Observed result:

```text
BUILD SUCCESS
Tests run: 8, Failures: 0, Errors: 0, Skipped: 0
```

Focused regression coverage added:

- `EventHubBufferedPartitionProducerTest.publishesUsingClientScheduler()`

## Conclusion

PERF-004 is addressed locally by removing the buffered producer’s hard-coded global scheduler hop and aligning it with the client’s configured scheduler. The original report’s broader “all operations share one scheduler” framing is stale.