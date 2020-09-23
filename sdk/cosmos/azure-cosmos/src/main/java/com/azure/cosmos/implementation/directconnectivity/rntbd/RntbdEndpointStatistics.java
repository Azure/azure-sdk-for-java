// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity.rntbd;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.IOException;

@JsonSerialize(using = RntbdEndpointStatistics.RntbdEndpointStatsJsonSerializer.class)
public class RntbdEndpointStatistics {

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

    RntbdEndpointStatistics closed(boolean closed) {
        this.closed = closed;
        return this;
    }

    private int availableChannels;
    private int acquiredChannels;
    private int executorTaskQueueSize;
    private int inflightRequests;
    private boolean closed;

    public static class RntbdEndpointStatsJsonSerializer extends com.fasterxml.jackson.databind.JsonSerializer<RntbdEndpointStatistics> {
        @Override
        public void serialize(RntbdEndpointStatistics stats,
                              JsonGenerator writer,
                              SerializerProvider serializerProvider) throws IOException {
            writer.writeStartObject();
            writer.writeBooleanField("isClosed", stats.closed);
            writer.writeNumberField("availableChannels", stats.availableChannels);
            writer.writeNumberField("acquiredChannels", stats.acquiredChannels);
            writer.writeNumberField("executorTaskQueueSize", stats.executorTaskQueueSize);
            writer.writeNumberField("inflightRequests", stats.inflightRequests);
            writer.writeEndObject();
        }
    }
}
