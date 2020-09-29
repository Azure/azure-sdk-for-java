// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity.rntbd;

import com.azure.cosmos.implementation.DiagnosticsInstantSerializer;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.IOException;
import java.io.Serializable;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

@JsonSerialize(using = RntbdEndpointStatistics.RntbdEndpointStatsJsonSerializer.class)
public class RntbdEndpointStatistics implements Serializable {
    private static final long serialVersionUID = 1L;

    RntbdEndpointStatistics availableChannels(int availableChannels) {
        this.availableChannels = availableChannels;
        return this;
    }

    RntbdEndpointStatistics acquiredChannels(int acquiredChannels) {
        this.acquiredChannels = acquiredChannels;
        return this;
    }

    RntbdEndpointStatistics executorTaskQueueSize(int executorTaskQueueSize) {
        this.executorTaskQueueSize = executorTaskQueueSize;
        return this;
    }

    RntbdEndpointStatistics inflightRequests(int inflightRequests) {
        this.inflightRequests = inflightRequests;
        return this;
    }

    RntbdEndpointStatistics lastSuccessfulRequestNanoTime(long lastSuccessfulRequestNanoTime) {
        this.lastSuccessfulRequestNanoTime = lastSuccessfulRequestNanoTime;
        return this;
    }

    RntbdEndpointStatistics lastRequestNanoTime(long lastRequestNanoTime) {
        this.lastRequestNanoTime = lastRequestNanoTime;
        return this;
    }

    RntbdEndpointStatistics createdTime(Instant createdTime) {
        this.createdTime = createdTime;
        return this;
    }

    RntbdEndpointStatistics closed(boolean closed) {
        this.closed = closed;
        return this;
    }

    private int availableChannels;
    private int acquiredChannels;
    private int executorTaskQueueSize;
    private int inflightRequests;
    private boolean closed;
    private long lastSuccessfulRequestNanoTime;
    private long lastRequestNanoTime;
    private Instant createdTime;

    private final static Instant referenceInstant = Instant.now();
    private final static long referenceNanoTime = System.nanoTime();

    public static class RntbdEndpointStatsJsonSerializer extends com.fasterxml.jackson.databind.JsonSerializer<RntbdEndpointStatistics> {
        @Override
        public void serialize(RntbdEndpointStatistics stats,
                              JsonGenerator writer,
                              SerializerProvider serializerProvider) throws IOException {
            writer.writeStartObject();
            writer.writeNumberField("availableChannels", stats.availableChannels);
            writer.writeNumberField("acquiredChannels", stats.acquiredChannels);
            writer.writeNumberField("executorTaskQueueSize", stats.executorTaskQueueSize);
            writer.writeNumberField("inflightRequests", stats.inflightRequests);
            writer.writeStringField("lastSuccessfulRequestTime", toInstantString(stats.lastSuccessfulRequestNanoTime));
            writer.writeStringField("lastRequestTime", toInstantString(stats.lastRequestNanoTime));
            writer.writeStringField("createdTime", toInstantString(stats.createdTime));
            writer.writeBooleanField("isClosed", stats.closed);
            writer.writeEndObject();
        }

        private String toInstantString(Instant instant) {
            return DiagnosticsInstantSerializer.fromInstant(instant);
        }

        private String toInstantString(long nanoTime) {
            Instant time = Instant.ofEpochMilli(referenceInstant.plusNanos(nanoTime - referenceNanoTime).toEpochMilli());
            return DiagnosticsInstantSerializer.fromInstant(time);
        }
    }
}
