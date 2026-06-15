// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.metadatahedging;

import com.azure.cosmos.implementation.routing.RegionalRoutingContext;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Per-logical-operation context shared between {@link MetadataHedgingStrategy} and the
 * metadata retry path. Carries the cold-start signal, the dedupe set of attempted endpoints,
 * the winning endpoint, the "hedged this operation" latch, and the first-page flag for
 * partition-key-range pagination.
 * <p>
 * Java port of the .NET {@code MetadataHedgingStrategy.MetadataHedgingContext}
 * (Azure/azure-cosmos-dotnet-v3#5923, design &sect;5.2 / &sect;6.1).
 */
public final class MetadataHedgingContext {

    private volatile boolean isColdStart;
    private volatile boolean isFirstReadFeedPage = true;
    private final ConcurrentHashMap<String, Byte> attemptedEndpoints = new ConcurrentHashMap<>();
    private final AtomicReference<RegionalRoutingContext> winningEndpoint = new AtomicReference<>(null);
    private final AtomicBoolean hasHedgedThisOperation = new AtomicBoolean(false);

    public boolean isColdStart() {
        return this.isColdStart;
    }

    public void setColdStart(boolean coldStart) {
        this.isColdStart = coldStart;
    }

    public boolean isFirstReadFeedPage() {
        return this.isFirstReadFeedPage;
    }

    public void setFirstReadFeedPage(boolean firstReadFeedPage) {
        this.isFirstReadFeedPage = firstReadFeedPage;
    }

    /**
     * Dedupe set of endpoint URIs that have already been attempted on this operation. Keyed on
     * the absolute URI string so a metadata retry can skip a region the hedge already used.
     */
    public ConcurrentHashMap<String, Byte> getAttemptedEndpoints() {
        return this.attemptedEndpoints;
    }

    public RegionalRoutingContext getWinningEndpoint() {
        return this.winningEndpoint.get();
    }

    public boolean hasHedgedThisOperation() {
        return this.hasHedgedThisOperation.get();
    }

    /**
     * Single-publication of the winning endpoint. Late loser continuations that try to
     * re-publish observe a non-null existing value and leave it intact.
     */
    public void recordWinner(RegionalRoutingContext endpoint) {
        this.winningEndpoint.compareAndSet(null, endpoint);
    }

    /**
     * Returns {@code true} if this caller is the first to mark the operation as having
     * dispatched a hedge. Subsequent callers observe {@code false} and skip.
     */
    public boolean tryMarkHedgedThisOperation() {
        return this.hasHedgedThisOperation.compareAndSet(false, true);
    }
}
