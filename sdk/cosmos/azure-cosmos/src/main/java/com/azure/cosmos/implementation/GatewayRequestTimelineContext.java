// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

public class GatewayRequestTimelineContext {

    private final RequestTimeline requestTimeline;
    private final long transportRequestId;

    public GatewayRequestTimelineContext(
        RequestTimeline requestTimeline, long transportRequestId) {

        this.requestTimeline = requestTimeline;
        this.transportRequestId = transportRequestId;
    }

    public RequestTimeline getRequestTimeline() {
        return this.requestTimeline;
    }

    public long getTransportRequestId() {
        return this.transportRequestId;
    }
}
