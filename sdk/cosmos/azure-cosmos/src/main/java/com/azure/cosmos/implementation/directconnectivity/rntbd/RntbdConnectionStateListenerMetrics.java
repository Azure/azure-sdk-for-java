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
    private static final Logger logger = LoggerFactory.getLogger(RntbdConnectionStateListenerMetrics.class);

    private final AtomicLong totalCount;
    private final AtomicLong totalApplicableCount;
    private final AtomicLong totalAddressesUpdatedCount;
    private final AtomicReference<Instant> lastApplicableTimestamp;

    public RntbdConnectionStateListenerMetrics() {

        this.totalCount = new AtomicLong(0L);
        this.totalApplicableCount = new AtomicLong(0L);
        this.totalAddressesUpdatedCount = new AtomicLong(0L);
        this.lastApplicableTimestamp = new AtomicReference<>();
    }

    public void recordAddressUpdated(int addressEntryUpdatedCount) {
        try {
            this.lastApplicableTimestamp.set(Instant.now());
            this.totalApplicableCount.getAndIncrement();
            this.totalAddressesUpdatedCount.accumulateAndGet(addressEntryUpdatedCount, (oldValue, newValue) -> oldValue + newValue);
        } catch (Exception exception) {
            logger.warn("Failed to record connection state listener metrics. ", exception);
        }
    }

    public void increaseTotalCount() {
        try{
            this.totalCount.getAndIncrement();
        } catch (Exception exception) {
            logger.warn("Failed to record total count of connection state listener. ", exception);
        }
    }

    final static class RntbdConnectionStateListenerMetricsJsonSerializer extends com.fasterxml.jackson.databind.JsonSerializer<RntbdConnectionStateListenerMetrics> {

        public RntbdConnectionStateListenerMetricsJsonSerializer() {
        }

        @Override
        public void serialize(RntbdConnectionStateListenerMetrics metrics, JsonGenerator writer, SerializerProvider serializers) throws IOException {
            writer.writeStartObject();

            writer.writeNumberField("totalCount", metrics.totalCount.get());
            writer.writeNumberField("totalApplicableCount", metrics.totalApplicableCount.get());
            writer.writeNumberField("totalAddressesUpdatedCount", metrics.totalAddressesUpdatedCount.get());
            if (metrics.lastApplicableTimestamp.get() != null) {
                writer.writeStringField("lastApplicableTimestamp", metrics.lastApplicableTimestamp.toString());
            }

            writer.writeEndObject();
        }
    }
}
