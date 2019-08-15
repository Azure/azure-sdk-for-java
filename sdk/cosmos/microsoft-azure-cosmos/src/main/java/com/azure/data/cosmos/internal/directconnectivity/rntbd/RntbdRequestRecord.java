// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal.directconnectivity.rntbd;

import com.azure.data.cosmos.BridgeInternal;
import com.azure.data.cosmos.RequestTimeoutException;
import com.azure.data.cosmos.internal.directconnectivity.StoreResponse;
import io.micrometer.core.instrument.Timer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkNotNull;

public final class RntbdRequestRecord extends CompletableFuture<StoreResponse> {

    private final RntbdRequestArgs args;
    private final RntbdRequestTimer timer;

    public RntbdRequestRecord(final RntbdRequestArgs args, final RntbdRequestTimer timer) {

        checkNotNull(args, "args");
        checkNotNull(timer, "timer");

        this.args = args;
        this.timer = timer;
    }

    // region Accessors

    public UUID activityId() {
        return this.args.activityId();
    }

    public RntbdRequestArgs args() {
        return this.args;
    }

    public long creationTime() {
        return this.args.creationTime();
    }

    public Duration lifetime() {
        return this.args.lifetime();
    }

    public long transportRequestId() {
        return this.args.transportRequestId();
    }

    // endregion

    // region Methods

    public boolean expire() {

        final long timeoutInterval = this.timer.getRequestTimeout(TimeUnit.MILLISECONDS);
        final String message = String.format("Request timeout interval (%,d ms) elapsed", timeoutInterval);
        final RequestTimeoutException error = new RequestTimeoutException(message, this.args.physicalAddress());

        BridgeInternal.setRequestHeaders(error, this.args.serviceRequest().getHeaders());

        return this.completeExceptionally(error);
    }

    public Timeout newTimeout(final TimerTask task) {
        return this.timer.newTimeout(task);
    }

    public long stop(Timer requests, Timer responses) {
        return this.args.stop(requests, responses);
    }

    @Override
    public String toString() {
        return RntbdObjectMapper.toString(this.args);
    }

    // endregion
}
