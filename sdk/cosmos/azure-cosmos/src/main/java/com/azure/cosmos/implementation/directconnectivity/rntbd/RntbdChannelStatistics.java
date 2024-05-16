// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity.rntbd;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.IOException;
import java.io.Serializable;
import java.time.Instant;

@JsonSerialize(using = RntbdChannelStatistics.RntbdChannelStatsJsonSerializer.class)
public class RntbdChannelStatistics implements Serializable {
    private static final long serialVersionUID = 1L;
    private String channelId;
    private int channelTaskQueueSize;
    private int pendingRequestsCount;
    private Instant lastReadTime;
    private int transitTimeoutCount;
    private Instant transitTimeoutStartingTime;
    private boolean waitForConnectionInit;

    public RntbdChannelStatistics channelId(String channelId) {
        this.channelId = channelId;
        return this;
    }

    public String getChannelId() {
        return this.channelId;
    }

    public RntbdChannelStatistics channelTaskQueueSize(int channelTaskQueueSize) {
        this.channelTaskQueueSize = channelTaskQueueSize;
        return this;
    }

    public int getChannelTaskQueueSize() {
        return this.channelTaskQueueSize;
    }

    public RntbdChannelStatistics pendingRequestsCount(int pendingRequestsCount) {
        this.pendingRequestsCount = pendingRequestsCount;
        return this;
    }

    public int getPendingRequestsCount() {
        return this.pendingRequestsCount;
    }

    public RntbdChannelStatistics lastReadTime(Instant lastReadTime) {
        this.lastReadTime = lastReadTime;
        return this;
    }

    public Instant getLastReadTime() {
        return this.lastReadTime;
    }

    public RntbdChannelStatistics transitTimeoutCount(int transitTimeoutCount) {
        this.transitTimeoutCount = transitTimeoutCount;
        return this;
    }

    public int getTransitTimeoutCount() {
        return this.transitTimeoutCount;
    }

    public RntbdChannelStatistics transitTimeoutStartingTime(Instant transitTimeoutStartingTime) {
        this.transitTimeoutStartingTime = transitTimeoutStartingTime;
        return this;
    }

    public Instant getTransitTimeoutStartingTime() {
        return this.transitTimeoutStartingTime;
    }

    public RntbdChannelStatistics waitForConnectionInit(boolean waitForConnectionInit) {
        this.waitForConnectionInit = waitForConnectionInit;
        return this;
    }

    public boolean isWaitForConnectionInit() {
        return this.waitForConnectionInit;
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
            this.writeNonNullInstantField(writer, "lastReadTime", stats.lastReadTime);
            if (stats.transitTimeoutCount > 0) {
                writer.writeNumberField("transitTimeoutCount", stats.transitTimeoutCount);
                this.writeNonNullInstantField(
                    writer,
                    "transitTimeoutStartingTime",
                    stats.transitTimeoutStartingTime);
            }
            writer.writeBooleanField("waitForConnectionInit", stats.waitForConnectionInit);
            writer.writeEndObject();
        }

        private void writeNonNullInstantField(JsonGenerator jsonGenerator, String fieldName, Instant value) throws IOException {
            if (value == null) {
                return;
            }

            jsonGenerator.writeStringField(fieldName, value.toString());
        }
    }
}
