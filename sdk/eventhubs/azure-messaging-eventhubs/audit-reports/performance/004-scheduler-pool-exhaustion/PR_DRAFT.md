# PR Title: [performance] Reuse configured client scheduler for buffered Java publishing

## Summary

Removes a hard-coded global `Schedulers.boundedElastic()` hop from `EventHubBufferedPartitionProducer` and reuses the producer client’s configured scheduler instead. This preserves caller-configured scheduler behavior for buffered publishing and avoids bypassing client-level scheduler isolation decisions.

## Changes

- `EventHubBufferedPartitionProducer.java`: replace `publishOn(Schedulers.boundedElastic(), 1)` with `publishOn(client.getScheduler(), 1)`
- `EventHubProducerAsyncClient.java`: add package-private `getScheduler()` accessor used by the buffered producer
- `EventHubBufferedPartitionProducerTest.java`: add regression coverage verifying buffered publish callbacks run on the configured client scheduler

### Core Implementation

```java
this.publishSubscription = publishEvents(eventDataAggregator)
    .publishOn(client.getScheduler(), 1)
    .subscribeWith(publishResultSubscriber);
```

## Expected Impact

- buffered publishing now honors user-supplied scheduler configuration
- removes hidden dependency on a global Reactor bounded elastic pool
- avoids accidental contention between buffered publishing and unrelated components sharing the global scheduler

## Testing

- `mvn -Dtest=EventHubBufferedPartitionProducerTest test`
- Result: `BUILD SUCCESS`
- `Tests run: 8, Failures: 0, Errors: 0, Skipped: 0`

## Related

- **Benchmark Script**: [benchmark.java](./benchmark.java)
- **Thread Analysis**: [thread-analysis.java](./thread-analysis.java) 
- **Performance Report**: [REPORT.md](./REPORT.md)
- **Proposed Patch**: [fix.patch](./fix.patch)

---

**Breaking Changes**: None

**Migration Path**: No user action required. Existing custom scheduler configuration now applies consistently to buffered publishing as well.

**Risk Assessment**: Low - the change reuses an existing configured scheduler instead of introducing new scheduler semantics