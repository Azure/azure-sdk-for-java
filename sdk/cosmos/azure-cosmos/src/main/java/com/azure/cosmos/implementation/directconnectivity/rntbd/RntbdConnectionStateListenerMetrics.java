// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity.rntbd;

import com.azure.cosmos.implementation.apachecommons.lang.tuple.Pair;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serializable;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

@JsonSerialize(using = RntbdConnectionStateListenerMetrics.RntbdConnectionStateListenerMetricsJsonSerializer.class)
public final class RntbdConnectionStateListenerMetrics implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(RntbdConnectionStateListenerMetrics.class);

    private final AtomicReference<Instant> lastCallTimestamp;
    private final AtomicReference<Pair<Instant, Integer>> lastActionableContext;

    public RntbdConnectionStateListenerMetrics() {

        this.lastCallTimestamp = new AtomicReference<>();
        this.lastActionableContext = new AtomicReference<>();
    }

    public void recordAddressUpdated(int addressEntryUpdatedCount) {
        this.lastActionableContext.set(Pair.of(this.lastCallTimestamp.get(), addressEntryUpdatedCount));
    }

    public void record() {
        this.lastCallTimestamp.set(Instant.now());
    }

    final static class RntbdConnectionStateListenerMetricsJsonSerializer extends com.fasterxml.jackson.databind.JsonSerializer<RntbdConnectionStateListenerMetrics> {

        public RntbdConnectionStateListenerMetricsJsonSerializer() {
        }

        @Override
        public void serialize(RntbdConnectionStateListenerMetrics metrics, JsonGenerator writer, SerializerProvider serializers) throws IOException {
            writer.writeStartObject();

            if (metrics.lastCallTimestamp.get() != null) {
                writer.writeStringField(
                        "lastCallTimestamp", metrics.lastCallTimestamp.toString());
            }

            if (metrics.lastActionableContext.get() != null) {
                writer.writeStringField("lastActionableContext", metrics.lastActionableContext.get().toString());
            }

            writer.writeEndObject();
        }
    }
}
