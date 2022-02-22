// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity.rntbd;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serializable;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

@JsonSerialize(using = RntbdConnectionStateListenerMetrics.RntbdConnectionStateListenerMetricsJsonSerializer.class)
public final class RntbdConnectionStateListenerMetrics implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(RntbdConnectionStateListenerMetrics.class);

    private final AtomicReference lastCallTimestamp;
    private final AtomicReference<Instant> lastActionableTimestamp;
    private final AtomicLong lastAddressesUpdatedCount;

    public RntbdConnectionStateListenerMetrics() {

        this.lastCallTimestamp = new AtomicReference();
        this.lastActionableTimestamp = new AtomicReference<>();
        this.lastAddressesUpdatedCount = new AtomicLong(0L);
    }

    public void recordAddressUpdated(int addressEntryUpdatedCount) {
        this.lastActionableTimestamp.set(Instant.now());
        this.lastAddressesUpdatedCount.set(addressEntryUpdatedCount);
    }

    public void recordLatestCallTimestamp() {
        this.lastCallTimestamp.set(Instant.now());
    }

    final static class RntbdConnectionStateListenerMetricsJsonSerializer extends com.fasterxml.jackson.databind.JsonSerializer<RntbdConnectionStateListenerMetrics> {

        public RntbdConnectionStateListenerMetricsJsonSerializer() {
        }

        @Override
        public void serialize(RntbdConnectionStateListenerMetrics metrics, JsonGenerator writer, SerializerProvider serializers) throws IOException {
            writer.writeStartObject();

            writer.writeStringField(
                "lastCallTimestamp",
                metrics.lastCallTimestamp.get() == null ? "N/A" : metrics.lastCallTimestamp.get().toString());

            if (metrics.lastActionableTimestamp.get() != null) {
                writer.writeStringField("lastActionableTimestamp", metrics.lastActionableTimestamp.get().toString());
                writer.writeNumberField("lastAddressesUpdatedCount", metrics.lastAddressesUpdatedCount.get());
            }

            writer.writeEndObject();
        }
    }
}
