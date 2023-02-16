// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import com.azure.cosmos.implementation.apachecommons.lang.tuple.Pair;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.IOException;
import java.io.Serializable;
import java.time.Instant;

@JsonSerialize(using = RntbdConnectionStateListenerMetricsDiagnostics.RntbdConnectionStateListenerDiagnosticsJsonSerializer.class)
public class RntbdConnectionStateListenerMetricsDiagnostics implements Serializable {
    private static final long serialVersionUID = 1L;
    private final Instant lastCallTimestamp;
    private final Pair<Instant, Integer> lastActionableContext;

    public RntbdConnectionStateListenerMetricsDiagnostics(Instant lastCallTimestamp, Pair<Instant, Integer> lastActionableContext) {
        this.lastCallTimestamp = lastCallTimestamp;
        this.lastActionableContext = lastActionableContext;
    }

    final static class RntbdConnectionStateListenerDiagnosticsJsonSerializer extends com.fasterxml.jackson.databind.JsonSerializer<RntbdConnectionStateListenerMetricsDiagnostics> {

        public RntbdConnectionStateListenerDiagnosticsJsonSerializer() {
        }

        @Override
        public void serialize(RntbdConnectionStateListenerMetricsDiagnostics diagnostics, JsonGenerator writer, SerializerProvider serializers) throws IOException {
            writer.writeStartObject();

            if (diagnostics.lastCallTimestamp != null) {
                writer.writeStringField(
                        "lastCallTimestamp", diagnostics.lastCallTimestamp.toString());
            }

            if (diagnostics.lastActionableContext != null) {
                writer.writeStringField("lastActionableContext", diagnostics.lastActionableContext.toString());
            }

            writer.writeEndObject();
        }
    }
}
