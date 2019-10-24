// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.internal.directconnectivity.rntbd;

import com.azure.cosmos.internal.RxDocumentServiceRequest;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.google.common.base.Stopwatch;
import io.micrometer.core.instrument.Timer;
import io.netty.channel.ChannelHandlerContext;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.math.BigDecimal;
import java.net.URI;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.micrometer.core.instrument.Timer.Sample;

@JsonPropertyOrder({
    "transportRequestId", "origin", "replicaPath", "activityId", "operationType", "resourceType", "creationTime",
    "lifetime"
})
public final class RntbdRequestArgs {

    private static final AtomicLong instanceCount = new AtomicLong();

    private final Sample sample;
    private final UUID activityId;
    private final long creationTime;
    private final Stopwatch lifetime;
    private final String origin;
    private final URI physicalAddress;
    private final String replicaPath;
    private final RxDocumentServiceRequest serviceRequest;
    private final long transportRequestId;

    public RntbdRequestArgs(final RxDocumentServiceRequest serviceRequest, final URI physicalAddress) {
        this.sample = Timer.start();
        this.activityId = UUID.fromString(serviceRequest.getActivityId());
        this.creationTime = System.nanoTime();
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
    public long creationTime() {
        return this.creationTime;
    }

    @JsonSerialize(using = ToStringSerializer.class)
    @JsonProperty
    public Duration lifetime() {
        return this.lifetime.elapsed();
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

    public void traceOperation(final Logger logger, final ChannelHandlerContext context, final String operationName, final Object... args) {

        checkNotNull(logger, "logger");

        if (logger.isTraceEnabled()) {
            final BigDecimal lifetime = BigDecimal.valueOf(this.lifetime.elapsed().toNanos(), 6);
            logger.trace("{},{},\"{}({})\",\"{}\",\"{}\"", this.creationTime, lifetime, operationName,
                Stream.of(args).map(arg ->
                    arg == null ? "null" : arg.toString()).collect(Collectors.joining(",")
                ),
                this, context
            );
        }
    }

    // endregion
}
