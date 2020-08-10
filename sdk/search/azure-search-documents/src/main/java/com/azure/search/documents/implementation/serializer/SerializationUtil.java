// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents.implementation.serializer;

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
     * @param mapper The Jackson ObjectMapper.
     */
    public static void configureMapper(ObjectMapper mapper) {
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE);
        UntypedObjectDeserializer defaultDeserializer = new UntypedObjectDeserializer(null, null);
        Iso8601DateDeserializer iso8601DateDeserializer = new Iso8601DateDeserializer(defaultDeserializer);
        //GeoPointDeserializer geoPointDeserializer = new GeoPointDeserializer(iso8601DateDeserializer);
        SimpleModule module = new SimpleModule();
        module.addDeserializer(Object.class, iso8601DateDeserializer);
        mapper.registerModule(Iso8601DateSerializer.getModule());
//        mapper.registerModule(GeometrySerializer.getModule());
//        mapper.registerModule(GeometryDeserializer.getModule());

        mapper.registerModule(module);
    }
}
