// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal.directconnectivity.rntbd;

import com.azure.data.cosmos.BridgeInternal;
import com.azure.data.cosmos.RequestTimeoutException;
import com.azure.data.cosmos.internal.directconnectivity.StoreResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import io.micrometer.core.instrument.Timer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import static com.google.common.base.Preconditions.checkNotNull;

@JsonSerialize(using = RntbdRequestRecord.JsonSerializer.class)
public final class RntbdRequestRecord extends CompletableFuture<StoreResponse> {

    private static final AtomicReferenceFieldUpdater<RntbdRequestRecord, Stage>
        stageUpdater = AtomicReferenceFieldUpdater.newUpdater(RntbdRequestRecord.class, Stage.class, "stage");

    private final RntbdRequestArgs args;
    private final RntbdRequestTimer timer;
    private volatile Stage stage;

    public RntbdRequestRecord(final RntbdRequestArgs args, final RntbdRequestTimer timer) {

        checkNotNull(args, "args");
        checkNotNull(timer, "timer");

        this.stage = Stage.CREATED;
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

    public Stage stage() {
        return stageUpdater.get(this);
    }

    public RntbdRequestRecord stage(Stage value) {
        stageUpdater.set(this, value);
        return this;
    }

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

    public enum Stage {
        CREATED, QUEUED, SENT, UNSENT, CANCELLED_BY_CLIENT
    }

    static final class JsonSerializer extends StdSerializer<RntbdRequestRecord> {

        JsonSerializer() {
            super(RntbdRequestRecord.class);
        }

        @Override
        public void serialize(
            final RntbdRequestRecord value,
            final JsonGenerator generator,
            final SerializerProvider provider) throws IOException {

            generator.writeStartObject();
            generator.writeObjectFieldStart("status");
            generator.writeBooleanField("done", value.isDone());
            generator.writeBooleanField("cancelled", value.isCancelled());
            generator.writeBooleanField("completedExceptionally", value.isCompletedExceptionally());

            if (value.isCompletedExceptionally()) {

                try {

                    value.get();

                } catch (final ExecutionException executionException) {

                    final Throwable error = executionException.getCause();

                    generator.writeObjectFieldStart("error");
                    generator.writeStringField("type", error.getClass().getName());
                    generator.writeObjectField("value", error);
                    generator.writeEndObject();

                } catch (CancellationException | InterruptedException exception) {

                    generator.writeObjectFieldStart("error");
                    generator.writeStringField("type", exception.getClass().getName());
                    generator.writeObjectField("value", exception);
                    generator.writeEndObject();
                }
            }

            generator.writeEndObject();
            generator.writeObjectField("args", value.args);
            generator.writeEndObject();
        }
    }
    // endregion
}
