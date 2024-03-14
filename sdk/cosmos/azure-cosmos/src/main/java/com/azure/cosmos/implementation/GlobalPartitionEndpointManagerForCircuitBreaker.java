// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import java.net.URI;
import java.time.Instant;
import java.util.Set;
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

        private final AtomicReference<PartitionLevelFailureMetadata> partitionLevelFailureMetadata;
        private final Set<URI> failedLocations = ConcurrentHashMap.newKeySet();
        private final Object failedRegionLock = new Object();
        // points to the current location a request will be routed to
        private URI current;

        PartitionLevelFailoverInfoForCircuitBreaker(URI current) {
            this.partitionLevelFailureMetadata = new AtomicReference<>(new PartitionLevelFailureMetadata());
            this.current = current;
        }

        // method purpose - choose the next possible region for this partition
        public boolean tryMoveNextLocation(Set<URI> locations, URI failedLocation) {

            if (failedLocation != this.current) {
                // a different thread has moved it to the next location
                return true;
            }

            synchronized (failedRegionLock) {

                if (failedLocation != this.current) {
                    // a different thread has moved it to the next location
                    return true;
                }

                for (URI location : locations) {

                    if (this.current == location) {
                        continue;
                    }

                    if (this.failedLocations.contains(location)) {
                        continue;
                    }

                    this.failedLocations.add(failedLocation);
                    this.current = location;
                    return true;
                }
            }

            return false;
        }
    }

    static class PartitionLevelFailureMetadata {
        private final AtomicInteger consecutiveFailureCount = new AtomicInteger();
        private final AtomicReference<Instant> unavailableSince = new AtomicReference<>(Instant.now());
    }
}
