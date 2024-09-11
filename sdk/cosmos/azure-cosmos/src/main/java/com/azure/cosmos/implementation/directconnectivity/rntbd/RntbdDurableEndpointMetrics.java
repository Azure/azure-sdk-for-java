// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity.rntbd;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public final class RntbdDurableEndpointMetrics {
    private final AtomicInteger totalAcquiredChannels;
    private final AtomicInteger totalClosedChannels;
    private final AtomicReference<RntbdEndpoint> latestEndpoint;

    /**
     * Rntbd metrics have a dimension for an endpoint - because we can evict endpoints for the same
     * logical endpoint address over time there might be multiple RntbdServiceEndpoint instances
     * For ChannelsClosed/ChannelsAcquired metrics we need a monotonic increasing counter across all the
     * RntbdServiceEndpoint instances with the same logical address. For the available channels it is a snapshot
     * of the latest RntbdServiceEndpoint instance. This class is a handler for the monotonic counters and gets
     * updated with a reference to the current endpoint to be able to report on available channels.
     * That way meters can be created lazily based off of an RntbdServiceEndpoint.durableEndpointMetrics() instance
     * which will continue to report correct metrics even if the endpoint gets evicted and recreated.
     */
    public RntbdDurableEndpointMetrics() {
        this.totalAcquiredChannels = new AtomicInteger(0);
        this.totalClosedChannels = new AtomicInteger(0);
        this.latestEndpoint = new AtomicReference<>();
    }

    public RntbdEndpoint getEndpoint() {
        return this.latestEndpoint.get();
    }

    public void setEndpoint(RntbdEndpoint endpoint) {
        this.latestEndpoint.set(endpoint);
    }

    public void clearEndpoint(RntbdEndpoint endpoint) {
        this.latestEndpoint.compareAndSet(endpoint, null);
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
