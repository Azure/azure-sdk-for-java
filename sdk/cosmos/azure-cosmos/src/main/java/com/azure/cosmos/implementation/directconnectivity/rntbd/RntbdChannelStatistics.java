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

@JsonSerialize(using = RntbdChannelStatistics.RntbdChannelStatsJsonSerializer.class)
public class RntbdChannelStatistics implements Serializable {
    private static final long serialVersionUID = 1L;
    private final static Instant referenceInstant = Instant.now();
    private final static long referenceNanoTime = System.nanoTime();
    private String channelId;
    private int channelTaskQueueSize;
    private int pendingRequestsCount;
    private long lastReadNanoTime;
    private long lastWriteNanoTime;
    private int transitTimeoutCount;
    private long transitTimeoutStartingNanoTime;
    private boolean waitForConnectionInit;

    public RntbdChannelStatistics channelId(String channelId) {
        this.channelId = channelId;
        return this;
    }

    public RntbdChannelStatistics channelTaskQueueSize(int channelTaskQueueSize) {
        this.channelTaskQueueSize = channelTaskQueueSize;
        return this;
    }

    public RntbdChannelStatistics pendingRequestsCount(int pendingRequestsCount) {
        this.pendingRequestsCount = pendingRequestsCount;
        return this;
    }

    public RntbdChannelStatistics lastReadNanoTime(long lastReadNanoTime) {
        this.lastReadNanoTime = lastReadNanoTime;
        return this;
    }

    public RntbdChannelStatistics lastWriteNanoTime(long lastWriteNanoTime) {
        this.lastWriteNanoTime = lastWriteNanoTime;
        return this;
    }

    public RntbdChannelStatistics transitTimeoutCount(int transitTimeoutCount) {
        this.transitTimeoutCount = transitTimeoutCount;
        return this;
    }

    public RntbdChannelStatistics transitTimeoutStartingNanoTime(long transitTimeoutStartingNanoTime) {
        this.transitTimeoutStartingNanoTime = transitTimeoutStartingNanoTime;
        return this;
    }

    public RntbdChannelStatistics waitForConnectionInit(boolean waitForConnectionInit) {
        this.waitForConnectionInit = waitForConnectionInit;
        return this;
    }

    public static class RntbdChannelStatsJsonSerializer extends com.fasterxml.jackson.databind.JsonSerializer<RntbdChannelStatistics> {
        @Override
        public void serialize(RntbdChannelStatistics stats,
                              JsonGenerator writer,
                              SerializerProvider serializerProvider) throws IOException {
            writer.writeStartObject();
            writer.writeStringField("channelId", stats.channelId);
            writer.writeNumberField("channelTaskQueueSize", stats.channelTaskQueueSize);
            writer.writeNumberField("pendingRequestsCount", stats.pendingRequestsCount);
            writer.writeStringField("lastReadNanoTime", toInstantString(stats.lastReadNanoTime));
            writer.writeStringField("lastWriteNanoTime", toInstantString(stats.lastWriteNanoTime));
            writer.writeNumberField("transitTimeoutCount", stats.transitTimeoutCount);
            writer.writeStringField("transitTimeoutStartingNanoTime", toInstantString(stats.transitTimeoutStartingNanoTime));
            writer.writeBooleanField("waitForConnectionInit", stats.waitForConnectionInit);
            writer.writeEndObject();
        }

        private String toInstantString(long nanoTime) {
            Instant time = Instant.ofEpochMilli(referenceInstant.plusNanos(nanoTime - referenceNanoTime).toEpochMilli());
            return DiagnosticsInstantSerializer.fromInstant(time);
        }
    }
}
