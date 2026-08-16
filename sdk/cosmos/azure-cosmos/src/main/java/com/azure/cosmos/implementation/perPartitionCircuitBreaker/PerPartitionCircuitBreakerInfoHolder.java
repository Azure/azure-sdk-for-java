// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.perPartitionCircuitBreaker;

import com.azure.cosmos.implementation.Utils;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@JsonSerialize(using = PerPartitionCircuitBreakerInfoHolder.PerPartitionCircuitBreakerInfoHolderSerializer.class)
public class PerPartitionCircuitBreakerInfoHolder implements Serializable {

    public static final PerPartitionCircuitBreakerInfoHolder EMPTY = new PerPartitionCircuitBreakerInfoHolder();

    private final Utils.ValueHolder<Map<String, LocationSpecificHealthContext>> perPartitionCircuitBreakerInfoHolder = new Utils.ValueHolder<Map<String, LocationSpecificHealthContext>>();
    private boolean initialized;

    public synchronized void setPerPartitionCircuitBreakerInfoHolder(final Map<String, LocationSpecificHealthContext> locationSpecificHealthContext) {
        this.initialized = true;
        this.perPartitionCircuitBreakerInfoHolder.v = locationSpecificHealthContext == null
            ? Collections.emptyMap()
            : Collections.unmodifiableMap(new LinkedHashMap<>(locationSpecificHealthContext));
    }

    public synchronized Map<String, LocationSpecificHealthContext> getPerPartitionCircuitBreakerInfoHolder() {
        return perPartitionCircuitBreakerInfoHolder.v;
    }

    public synchronized PerPartitionCircuitBreakerInfoHolder snapshot() {
        PerPartitionCircuitBreakerInfoHolder snapshot = new PerPartitionCircuitBreakerInfoHolder();
        if (this.initialized) {
            snapshot.initialized = true;
            snapshot.perPartitionCircuitBreakerInfoHolder.v = this.perPartitionCircuitBreakerInfoHolder.v;
        }
        return snapshot;
    }

    synchronized boolean isInitialized() {
        return this.initialized;
    }

    public static class PerPartitionCircuitBreakerInfoHolderSerializer extends com.fasterxml.jackson.databind.JsonSerializer<PerPartitionCircuitBreakerInfoHolder> {

        @Override
        public void serialize(PerPartitionCircuitBreakerInfoHolder value, JsonGenerator gen, SerializerProvider serializers) throws IOException {

            Map<String, LocationSpecificHealthContext> locationToLocationSpecificHealthContext = value.getPerPartitionCircuitBreakerInfoHolder();

            if (value.isInitialized()) {
                gen.writeStartObject();

                gen.writePOJOField("stateByRegion", locationToLocationSpecificHealthContext);

                gen.writeEndObject();
            } else {
                gen.writeNull();
            }
        }
    }
}
