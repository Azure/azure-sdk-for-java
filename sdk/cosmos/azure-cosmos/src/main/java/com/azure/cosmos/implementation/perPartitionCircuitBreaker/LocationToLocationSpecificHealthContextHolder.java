// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.perPartitionCircuitBreaker;

import com.azure.cosmos.implementation.Utils;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

public class LocationToLocationSpecificHealthContextHolder implements Serializable {

    private final Utils.ValueHolder<Map<String, LocationSpecificHealthContext>> locationToLocationSpecificHealthContextHolder = new Utils.ValueHolder<Map<String, LocationSpecificHealthContext>>();

    public synchronized void setLocationToLocationSpecificHealthContext(final Map<String, LocationSpecificHealthContext> locationSpecificHealthContext) {
        this.locationToLocationSpecificHealthContextHolder.v = locationSpecificHealthContext;
    }

    public synchronized Map<String, LocationSpecificHealthContext> getLocationToLocationSpecificHealthContext() {
        return locationToLocationSpecificHealthContextHolder.v;
    }

    public static class LocationSpecificHealthContextHolderSerializer extends com.fasterxml.jackson.databind.JsonSerializer<LocationToLocationSpecificHealthContextHolder> {

        @Override
        public void serialize(LocationToLocationSpecificHealthContextHolder value, JsonGenerator gen, SerializerProvider serializers) throws IOException {

            gen.writeStartObject();

            Map<String, LocationSpecificHealthContext> locationToLocationSpecificHealthContext = value.getLocationToLocationSpecificHealthContext();

            gen.writePOJOField("partitionLevelCircuitBreakerCtx", locationToLocationSpecificHealthContext);

            gen.writeEndObject();
        }
    }
}
