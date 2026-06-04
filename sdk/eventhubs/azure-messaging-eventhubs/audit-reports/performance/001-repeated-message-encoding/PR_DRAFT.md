# PR Title: [performance] Reuse cached AMQP messages for Java EventDataBatch sends

## Summary

Eliminates duplicate batch-send serialization by caching Proton `Message` instances inside `EventDataBatch` when events are added, then reusing those cached messages in `EventHubProducerAsyncClient.send(EventDataBatch)`. This keeps the existing size calculation behavior but removes the second serialization pass during publish.

## Changes

- `EventDataBatch.java`: store the Proton `Message` created during `tryAdd()` in a batch-local message list
- `EventDataBatch.java`: keep the first-message envelope sizing on a separate temporary message so the cached send message stays intact
- `EventHubProducerAsyncClient.java`: reuse `batch.getMessages()` instead of serializing `batch.getEvents()` again
- `EventDataBatchTest.java`: add coverage that `tryAdd()` caches the serialized message and preserves partition-key annotations
- `EventHubProducerAsyncClientTest.java`: add coverage that `send(batch)` reuses cached messages and does not call the serializer again

### Core Implementation

```java
public boolean tryAdd(final EventData eventData) {
    final Message amqpMessage = createAmqpMessage(eventData, partitionKey);
    final int size = getSize(eventData, amqpMessage, events.isEmpty());

    if (this.sizeInBytes + size > this.maxMessageSize) {
        return false;
    }

    this.sizeInBytes += size;
    this.events.add(eventData);
    this.amqpMessages.add(amqpMessage);
    return true;
}

public Mono<Void> send(EventDataBatch batch) {
    final List<Message> messages = batch.getMessages();
    return getSendLink(batch.getPartitionId())
        .flatMap(link -> messages.size() == 1 ? link.send(messages.get(0)) : link.send(messages));
}
```

## Expected Impact

- Remove the second serialization pass for every event in `send(EventDataBatch)`
- Lower CPU spent on batch publishing under high-throughput workloads
- Reduce temporary AMQP object churn and associated GC pressure

## Testing

- `mvn -Dtest=EventDataBatchTest,EventHubProducerAsyncClientTest test`
- Result: `BUILD SUCCESS`
- `EventDataBatchTest`: 5 tests, 0 failures/errors
- `EventHubProducerAsyncClientTest`: 38 tests, 0 failures/errors

## Benchmark Snapshot

Repeated emulator-backed runs on commit `48383fb`, 5 samples:

- send time, 100 events: samples `[20, 10, 10, 10, 10]`, median `10 ms`
- send time, 500 events: samples `[10, 11, 9, 6, 7]`, median `9 ms`
- send time, 1000 events: samples `[7, 4, 4, 5, 7]`, median `5 ms`
- simple throughput section: samples `[70490, 71758, 63597, 75279, 76092]`, median `71758 msgs/sec`

## Related

- **Performance Report**: [REPORT.md](./REPORT.md)
- **Proposed Patch**: [fix.patch](./fix.patch)

---

**Breaking Changes**: None - all changes are internal to Java batch creation and send handling

**Migration Path**: No action required from users

**Risk Assessment**: Low - the cache is batch-local and validated with focused unit coverage