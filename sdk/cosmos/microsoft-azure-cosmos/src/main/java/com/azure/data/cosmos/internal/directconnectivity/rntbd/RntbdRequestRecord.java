// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal.directconnectivity.rntbd;

import com.azure.data.cosmos.BridgeInternal;
import com.azure.data.cosmos.RequestTimeoutException;
import com.azure.data.cosmos.internal.directconnectivity.StoreResponse;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkNotNull;

public final class RntbdRequestRecord extends CompletableFuture<StoreResponse> {

    private static final String simpleClassName = RntbdRequestRecord.class.getSimpleName();

    private final RntbdRequestArgs args;
    private final RntbdRequestTimer timer;

    public RntbdRequestRecord(final RntbdRequestArgs args, final RntbdRequestTimer timer) {

        checkNotNull(args, "args");
        checkNotNull(timer, "timer");

        this.args = args;
        this.timer = timer;
    }

    public UUID getActivityId() {
        return this.args.getActivityId();
    }

    public RntbdRequestArgs getArgs() {
        return this.args;
    }

    public long getBirthTime() {
        return this.args.getBirthTime();
    }

    public Duration getLifetime() {
        return this.args.getLifetime();
    }

    public long getTransportRequestId() {
        return this.args.getTransportRequestId();
    }

    public boolean expire() {

        final long timeoutInterval = this.timer.getRequestTimeout(TimeUnit.MILLISECONDS);
        final String message = String.format("Request timeout interval (%,d ms) elapsed", timeoutInterval);
        final RequestTimeoutException error = new RequestTimeoutException(message, this.args.getPhysicalAddress());

        BridgeInternal.setRequestHeaders(error, this.args.getServiceRequest().getHeaders());

        return this.completeExceptionally(error);
    }

    public Timeout newTimeout(final TimerTask task) {
        return this.timer.newTimeout(task);
    }

    @Override
    public String toString() {
        return simpleClassName + '(' + RntbdObjectMapper.toJson(this.args) + ')';
    }
}
