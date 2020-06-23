// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents.implementation;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.UntypedObjectDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Utility type to configure JSON serialization behavior.
 */
public class SerializationUtil {
    /**
     * Configures an {@link ObjectMapper} with custom behavior needed to work with the Azure Cognitive Search REST API.
     *
     * @param mapper the mapper to be configured
     */
    public static void configureMapper(ObjectMapper mapper) {
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE);
        UntypedObjectDeserializer defaultDeserializer = new UntypedObjectDeserializer(null, null);
        GeoPointDeserializer geoPointDeserializer = new GeoPointDeserializer(defaultDeserializer);
        Iso8601DateDeserializer iso8601DateDeserializer = new Iso8601DateDeserializer(geoPointDeserializer);
        SimpleModule module = new SimpleModule();
        module.addDeserializer(Object.class, iso8601DateDeserializer);
        mapper.registerModule(Iso8601DateSerializer.getModule());
        mapper.registerModule(module);
    }
}
