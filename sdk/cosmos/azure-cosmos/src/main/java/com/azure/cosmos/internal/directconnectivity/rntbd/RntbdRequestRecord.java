// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.internal.directconnectivity.rntbd;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.RequestTimeoutException;
import com.azure.cosmos.internal.directconnectivity.StoreResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.micrometer.core.instrument.Timer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import static com.google.common.base.Preconditions.checkNotNull;

public final class RntbdRequestRecord extends CompletableFuture<StoreResponse> {

    private static final AtomicReferenceFieldUpdater<RntbdRequestRecord, State>
        stateUpdater = AtomicReferenceFieldUpdater.newUpdater(RntbdRequestRecord.class, State.class,"state");

    private final RntbdRequestArgs args;
    private final RntbdRequestTimer timer;
    private volatile State state;

    public RntbdRequestRecord(final RntbdRequestArgs args, final RntbdRequestTimer timer) {

        checkNotNull(args, "args");
        checkNotNull(timer, "timer");

        this.state = State.CREATED;
        this.args = args;
        this.timer = timer;
    }

    // region Accessors

    public UUID activityId() {
        return this.args.activityId();
    }

    @JsonProperty
    public RntbdRequestArgs args() {
        return this.args;
    }

    public long creationTime() {
        return this.args.creationTime();
    }

    public boolean expire() {
        final RequestTimeoutException error = new RequestTimeoutException(this.toString(), this.args.physicalAddress());
        BridgeInternal.setRequestHeaders(error, this.args.serviceRequest().getHeaders());
        return this.completeExceptionally(error);
    }

    public Duration lifetime() {
        return this.args.lifetime();
    }

    public Timeout newTimeout(final TimerTask task) {
        return this.timer.newTimeout(task);
    }

    @JsonProperty
    public State state() {
        return stateUpdater.get(this);
    }

    RntbdRequestRecord state(State value) {
        stateUpdater.set(this, value);
        return this;
    }

    @JsonProperty
    public long timeoutIntervalInMillis() {
        return this.timer.getRequestTimeout(TimeUnit.MILLISECONDS);
    }

    public long transportRequestId() {
        return this.args.transportRequestId();
    }

    // endregion

    // region Methods

    public long stop(Timer requests, Timer responses) {
        return this.args.stop(requests, responses);
    }

    @Override
    public String toString() {
        return RntbdObjectMapper.toString(this);
    }

    // endregion

    // region Types

    enum State {
        CREATED, QUEUED, SENT, UNSENT
    }

    // endregion
}
