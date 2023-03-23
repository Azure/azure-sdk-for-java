// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity.rntbd;

import com.azure.cosmos.implementation.OpenConnectionResponse;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;

public class OpenConnectionRntbdRequestRecord extends CompletableFuture<OpenConnectionResponse> implements IRequestRecord {
    private static final AtomicLong instanceCount = new AtomicLong();
    private final RntbdRequestArgs requestArgs;
    private final long openConnectionRequestId;

    public OpenConnectionRntbdRequestRecord(RntbdRequestArgs requestArgs) {
        this.requestArgs = requestArgs;
        this.openConnectionRequestId = instanceCount.incrementAndGet();
    }

    @Override
    public RntbdChannelAcquisitionTimeline getChannelAcquisitionTimeline() {
        return null;
    }

    @Override
    public RntbdRequestArgs args() {
        return this.requestArgs;
    }

    @Override
    public long getRequestId() {
        return this.openConnectionRequestId;
    }
}
