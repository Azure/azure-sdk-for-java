// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity.rntbd;

import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.guava25.base.Stopwatch;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.micrometer.core.instrument.Timer;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;
import static io.micrometer.core.instrument.Timer.Sample;

@JsonPropertyOrder({
    "transportRequestId", "activityId", "origin", "replicaPath", "operationType", "resourceType", "timeCreated",
    "lifetime"
})
public final class RntbdRequestArgs {

    private static final AtomicLong instanceCount = new AtomicLong();

    private final Sample sample;
    private final UUID activityId;
    private final Instant timeCreated;
    private final long nanoTimeCreated;
    private final Stopwatch lifetime;
    private final String origin;
    private final URI physicalAddress;
    private final String replicaPath;
    private final RxDocumentServiceRequest serviceRequest;
    private final long transportRequestId;

    public RntbdRequestArgs(final RxDocumentServiceRequest serviceRequest, final URI physicalAddress) {
        this.sample = Timer.start();
        this.activityId = serviceRequest.getActivityId();
        this.timeCreated = Instant.now();
        this.nanoTimeCreated = System.nanoTime();
        this.lifetime = Stopwatch.createStarted();
        this.origin = physicalAddress.getScheme() + "://" + physicalAddress.getAuthority();
        this.physicalAddress = physicalAddress;
        this.replicaPath = StringUtils.stripEnd(physicalAddress.getPath(), "/");
        this.serviceRequest = serviceRequest;
        this.transportRequestId = instanceCount.incrementAndGet();
    }

    // region Accessors

    @JsonProperty
    public UUID activityId() {
        return this.activityId;
    }

    @JsonProperty
    public Duration lifetime() {
        return this.lifetime.elapsed();
    }

    @JsonIgnore
    public long nanoTimeCreated() {
        return this.nanoTimeCreated;
    }

    @JsonProperty
    public String origin() {
        return this.origin;
    }

    @JsonIgnore
    public URI physicalAddress() {
        return this.physicalAddress;
    }

    @JsonProperty
    public String replicaPath() {
        return this.replicaPath;
    }

    @JsonIgnore
    public RxDocumentServiceRequest serviceRequest() {
        return this.serviceRequest;
    }

    @JsonProperty
    public Instant timeCreated() {
        return this.timeCreated;
    }

    @JsonProperty
    public long transportRequestId() {
        return this.transportRequestId;
    }

    // endregion

    // region Methods

    public long stop(Timer requests, Timer responses) {
        this.lifetime.stop();
        this.sample.stop(requests);
        return this.sample.stop(responses);
    }

    @Override
    public String toString() {
        return RntbdObjectMapper.toString(this);
    }

    public void traceOperation(
        final Logger logger, final ChannelHandlerContext context, final String operationName, final Object... args) {

        checkNotNull(logger, "expected non-null logger");

        if (logger.isDebugEnabled()) {
            logger.debug("{},{},\"{}({})\",\"{}\",\"{}\"", this.timeCreated, this.lifetime.elapsed(), operationName,
                Stream.of(args)
                    .map(arg -> arg == null ? "null" : arg.toString())
                    .collect(Collectors.joining(",")),
                this, context);
        }
    }

    // endregion
}
