// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.perPartitionCircuitBreaker;

import com.azure.cosmos.implementation.Utils;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

public class PerPartitionCircuitBreakerInfoHolder implements Serializable {

    private final Utils.ValueHolder<Map<String, LocationSpecificHealthContext>> locationToLocationSpecificHealthContextHolder = new Utils.ValueHolder<Map<String, LocationSpecificHealthContext>>();

    public synchronized void setLocationToLocationSpecificHealthContext(final Map<String, LocationSpecificHealthContext> locationSpecificHealthContext) {
        this.locationToLocationSpecificHealthContextHolder.v = locationSpecificHealthContext;
    }

    public synchronized Map<String, LocationSpecificHealthContext> getLocationToLocationSpecificHealthContext() {
        return locationToLocationSpecificHealthContextHolder.v;
    }

    public static class PerPartitionCircuitBreakerInfoHolderSerializer extends com.fasterxml.jackson.databind.JsonSerializer<PerPartitionCircuitBreakerInfoHolder> {

        @Override
        public void serialize(PerPartitionCircuitBreakerInfoHolder value, JsonGenerator gen, SerializerProvider serializers) throws IOException {

            Map<String, LocationSpecificHealthContext> locationToLocationSpecificHealthContext = value.getLocationToLocationSpecificHealthContext();

            if (locationToLocationSpecificHealthContext != null && !locationToLocationSpecificHealthContext.isEmpty()) {
                gen.writeStartObject();

                gen.writePOJOField("locSpecificHealthCtx", locationToLocationSpecificHealthContext);

                gen.writeEndObject();
            }
        }
    }
}
