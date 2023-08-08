// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity.rntbd;

import com.azure.cosmos.implementation.apachecommons.lang.tuple.Pair;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

public final class RntbdConnectionStateListenerMetrics {
    private final AtomicReference<Instant> lastCallTimestamp;
    private final AtomicReference<Pair<Instant, Integer>> lastActionableContext;

    public RntbdConnectionStateListenerMetrics() {

        this.lastCallTimestamp = new AtomicReference<>();
        this.lastActionableContext = new AtomicReference<>();
    }

    public void recordAddressUpdated(int addressEntryUpdatedCount) {
        this.lastActionableContext.set(Pair.of(this.lastCallTimestamp.get(), addressEntryUpdatedCount));
    }

    public void record() {
        this.lastCallTimestamp.set(Instant.now());
    }

    public Instant getLastCallTimestamp() {
        return this.lastCallTimestamp.get();
    }

    public Pair<Instant, Integer> getLastActionableContext() {
        return this.lastActionableContext.get();
    }
}
