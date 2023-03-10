// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity.rntbd;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public final class RntbdDurableEndpointMetrics {
    private final AtomicInteger totalAcquiredChannels;
    private final AtomicInteger totalClosedChannels;
    private final AtomicReference<RntbdEndpoint> latestEndpoint;

    public RntbdDurableEndpointMetrics() {
        this.totalAcquiredChannels = new AtomicInteger(0);
        this.totalClosedChannels = new AtomicInteger(0);
        this.latestEndpoint = new AtomicReference<>();
    }

    public void setEndpoint(RntbdEndpoint endpoint) {
        this.latestEndpoint.set(endpoint);
    }

    public void incrementAcquiredChannels() {
        totalAcquiredChannels.incrementAndGet();
    }

    public void incrementClosedChannels() {
        totalClosedChannels.incrementAndGet();
    }

    public int channelsAvailableMetric() {
        RntbdEndpoint snapshot = latestEndpoint.get();
        if (snapshot != null) {
            return snapshot.channelsAvailableMetric();
        }

        return 0;
    }

    public int totalChannelsClosedMetric() {
        return totalClosedChannels.get();
    }

    public int totalChannelsAcquiredMetric() {
        return totalAcquiredChannels.get();
    }
}
