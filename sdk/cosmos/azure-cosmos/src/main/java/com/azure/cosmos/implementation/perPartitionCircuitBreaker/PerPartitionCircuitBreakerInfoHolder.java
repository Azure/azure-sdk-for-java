// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.perPartitionCircuitBreaker;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

@JsonSerialize(using = PerPartitionCircuitBreakerInfoHolder.PerPartitionCircuitBreakerInfoHolderSerializer.class)
public class PerPartitionCircuitBreakerInfoHolder implements Serializable {

    public static final PerPartitionCircuitBreakerInfoHolder EMPTY = new PerPartitionCircuitBreakerInfoHolder();

    private volatile Map<String, LocationSpecificHealthContext> perPartitionCircuitBreakerInfoHolder;
    private volatile Map<String, String> latestFailbackMessageByRegion = Collections.emptyMap();

    public PerPartitionCircuitBreakerInfoHolder() {
    }

    private PerPartitionCircuitBreakerInfoHolder(
        Map<String, LocationSpecificHealthContext> perPartitionCircuitBreakerInfoHolder,
        Map<String, String> latestFailbackMessageByRegion) {

        this.perPartitionCircuitBreakerInfoHolder = perPartitionCircuitBreakerInfoHolder;
        this.latestFailbackMessageByRegion = latestFailbackMessageByRegion;
    }

    public void setPerPartitionCircuitBreakerInfoHolder(final Map<String, LocationSpecificHealthContext> locationSpecificHealthContext) {
        this.setPerPartitionCircuitBreakerInfoHolder(locationSpecificHealthContext, this.latestFailbackMessageByRegion);
    }

    void setPerPartitionCircuitBreakerInfoHolder(
        Map<String, LocationSpecificHealthContext> locationSpecificHealthContext,
        Map<String, String> latestFailbackMessageByRegion) {

        if (this == EMPTY) {
            return;
        }

        this.perPartitionCircuitBreakerInfoHolder = locationSpecificHealthContext == null
            ? Collections.emptyMap()
            : locationSpecificHealthContext;
        this.latestFailbackMessageByRegion = latestFailbackMessageByRegion == null
            ? Collections.emptyMap()
            : latestFailbackMessageByRegion;
    }

    public Map<String, LocationSpecificHealthContext> getPerPartitionCircuitBreakerInfoHolder() {
        return this.perPartitionCircuitBreakerInfoHolder;
    }

    public PerPartitionCircuitBreakerInfoHolder snapshot() {
        Map<String, LocationSpecificHealthContext> snapshot = this.perPartitionCircuitBreakerInfoHolder;

        return snapshot == null
            ? EMPTY
            : new PerPartitionCircuitBreakerInfoHolder(snapshot, this.latestFailbackMessageByRegion);
    }

    public static class PerPartitionCircuitBreakerInfoHolderSerializer extends com.fasterxml.jackson.databind.JsonSerializer<PerPartitionCircuitBreakerInfoHolder> {

        @Override
        public void serialize(PerPartitionCircuitBreakerInfoHolder value, JsonGenerator gen, SerializerProvider serializers) throws IOException {

            Map<String, LocationSpecificHealthContext> locationToLocationSpecificHealthContext = value.getPerPartitionCircuitBreakerInfoHolder();

            if (locationToLocationSpecificHealthContext != null) {
                gen.writeStartObject();

                gen.writePOJOField("stateByRegion", locationToLocationSpecificHealthContext);

                if (!value.latestFailbackMessageByRegion.isEmpty()) {
                    gen.writePOJOField("latestFailbackMessageByRegion", value.latestFailbackMessageByRegion);
                }

                gen.writeEndObject();
            }
        }
    }
}
