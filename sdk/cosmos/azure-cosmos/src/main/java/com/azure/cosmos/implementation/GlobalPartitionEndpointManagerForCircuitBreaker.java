package com.azure.cosmos.implementation;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class GlobalPartitionEndpointManagerForCircuitBreaker implements IGlobalPartitionEndpointManager {

    private final ConcurrentHashMap<PartitionKeyRange, PartitionLevelFailoverInfoForCircuitBreaker> pkRangeToFailover;

    public GlobalPartitionEndpointManagerForCircuitBreaker() {
        this.pkRangeToFailover = new ConcurrentHashMap<>();
    }

    @Override
    public boolean tryMarkPartitionKeyRangeAsUnavailable(RxDocumentServiceRequest request) {
        return false;
    }

    // what is the point of an inner class?
    // at a high-level, the below class needs:
    //  1. consecutive failure count tracker
    //  2. unavailable since
    //  3. regions unavailable in
    //  4. failure type
    static class PartitionLevelFailoverInfoForCircuitBreaker {
        private final AtomicInteger consecutiveFailureCount = new AtomicInteger();
        private final AtomicReference<Instant> unavailableSince = new AtomicReference<>(Instant.now());
    }
}
