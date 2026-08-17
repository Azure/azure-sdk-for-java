// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.perPartitionCircuitBreaker;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

@JsonSerialize(using = PerPartitionCircuitBreakerInfoHolder.PerPartitionCircuitBreakerInfoHolderSerializer.class)
public class PerPartitionCircuitBreakerInfoHolder implements Serializable {

    public static final PerPartitionCircuitBreakerInfoHolder EMPTY = new PerPartitionCircuitBreakerInfoHolder();

    private volatile Map<String, LocationSpecificHealthContext> state;

    public void setPerPartitionCircuitBreakerInfoHolder(final Map<String, LocationSpecificHealthContext> locationSpecificHealthContext) {
        Map<String, LocationSpecificHealthContext> immutableSnapshot = locationSpecificHealthContext == null
            ? Collections.emptyMap()
            : Collections.unmodifiableMap(new LinkedHashMap<>(locationSpecificHealthContext));
        this.state = immutableSnapshot;
    }

    public Map<String, LocationSpecificHealthContext> getPerPartitionCircuitBreakerInfoHolder() {
        return this.state;
    }

    public void setPerPartitionCircuitBreakerInfoHolderSnapshot(
        final Map<String, LocationSpecificHealthContext> snapshot) {

        this.state = Objects.requireNonNull(snapshot, "snapshot cannot be null");
    }

    public PerPartitionCircuitBreakerInfoHolder snapshot() {
        PerPartitionCircuitBreakerInfoHolder snapshot = new PerPartitionCircuitBreakerInfoHolder();
        snapshot.state = this.state;
        return snapshot;
    }

    boolean isInitialized() {
        return this.state != null;
    }

    public static class PerPartitionCircuitBreakerInfoHolderSerializer extends com.fasterxml.jackson.databind.JsonSerializer<PerPartitionCircuitBreakerInfoHolder> {

        @Override
        public void serialize(PerPartitionCircuitBreakerInfoHolder value, JsonGenerator gen, SerializerProvider serializers) throws IOException {

            Map<String, LocationSpecificHealthContext> state = value.state;

            if (state != null) {
                gen.writeStartObject();

                gen.writePOJOField("stateByRegion", state);

                gen.writeEndObject();
            } else {
                gen.writeNull();
            }
        }
    }
}
