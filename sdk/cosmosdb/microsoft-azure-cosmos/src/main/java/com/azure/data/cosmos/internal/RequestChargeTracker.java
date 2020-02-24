// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Tracks requests charge in the Azure Cosmos DB database service.
 */
public final class RequestChargeTracker {
    private final static int NUMBER_OF_DECIMAL_POINT_TO_RESERVE_FACTOR = 1000;
    private final AtomicLong totalRUs = new AtomicLong();

    public double getTotalRequestCharge() {
        return ((double) this.totalRUs.get()) / NUMBER_OF_DECIMAL_POINT_TO_RESERVE_FACTOR;
    }

    public void addCharge(double ruUsage) {
        this.totalRUs.addAndGet((long) (ruUsage * NUMBER_OF_DECIMAL_POINT_TO_RESERVE_FACTOR));
    }

    public double getAndResetCharge() {
        return (double) this.totalRUs.getAndSet(0) / NUMBER_OF_DECIMAL_POINT_TO_RESERVE_FACTOR;
    }
}