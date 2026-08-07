# Performance Report: Receive Buffer Already Bounded by Prefetch

**Report ID**: PERF-002
**Date**: 2026-03-18
**SDK**: azure-messaging-eventhubs (Java)
**Version Tested**: 5.18.0
**Impact**: HIGH
**Status**: PR Ready

## Executive Summary

This report was stale. The local `AmqpReceiveLinkProcessor` already bounds upstream buffering using Reactor backpressure, with `maxQueueSize = prefetch * 2` and `onBackpressureBuffer(maxQueueSize, BufferOverflowStrategy.ERROR)` on the receive stream.

The useful work for this item was to add explicit regression coverage that pins the existing queue bound and the credit calculation behavior that accounts for queued messages.

## Current Implementation

`AmqpReceiveLinkProcessor` still stores queued messages in a `ConcurrentLinkedDeque<Message>`, but the Reactor receive pipeline is already bounded before items are added to that deque:

```java
this.maxQueueSize = prefetch * 2;

next.receive()
    .onBackpressureBuffer(maxQueueSize, BufferOverflowStrategy.ERROR)
    .subscribe(message -> {
        messageQueue.add(message);
        drain();
    }, error -> {
        logger.atVerbose().log("Receiver is terminated.", error);
    });
```

Credit calculation also subtracts the number of queued messages when deciding whether to request more work:

```java
final int remaining = Long.valueOf(request).intValue() - messageQueue.size();
credits = Math.max(remaining, 0);
```

That means the current implementation already prevents the unbounded-growth scenario described in the original report.

## Local Validation

### Added Regression Coverage

Updated `AmqpReceiveLinkProcessorTest` with focused tests that verify:

- `maxQueueSize` is bounded to `prefetch * 2`
- credit calculation returns `0` when queued messages already satisfy downstream demand

### Focused Unit Test Run

Validated with:

```text
mvn -Dtest=AmqpReceiveLinkProcessorTest test
```

Observed result:

```text
BUILD SUCCESS
AmqpReceiveLinkProcessorTest ran 18 tests with 0 failures, 0 errors, 0 skipped.
```

## Conclusion

The local Java SDK already contains the bounded-buffer behavior needed to avoid the original unbounded queue problem. This item is now tracked as reconciled with regression coverage and updated documentation.

## Files Updated

- `src/test/java/com/azure/messaging/eventhubs/implementation/AmqpReceiveLinkProcessorTest.java`
- `audit-reports/performance/002-unbounded-message-queue/REPORT.md`

## References

- [Reactive Streams Backpressure](https://www.reactive-streams.org/)
- [Java BlockingQueue Performance](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/concurrent/BlockingQueue.html)
- [Circuit Breaker Pattern](https://martinfowler.com/bliki/CircuitBreaker.html)

## Credits

Performance analysis by Security Audit Team during comprehensive review of azure-messaging-eventhubs SDK.