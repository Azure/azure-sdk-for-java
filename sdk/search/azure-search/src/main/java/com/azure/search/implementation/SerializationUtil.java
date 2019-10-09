// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.implementation;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.deser.std.UntypedObjectDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * Utility type to configure JSON serialization behavior.
 */
public class SerializationUtil {
    /**
     * Configures an {@link ObjectMapper} with custom behavior needed to work with the Azure Search REST API.
     * @param mapper the mapper to be configured
     */
    public static void configureMapper(ObjectMapper mapper) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        df.setTimeZone(TimeZone.getDefault());
        mapper.setDateFormat(df);

        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE);

        UntypedObjectDeserializer defaultDeserializer = new UntypedObjectDeserializer(null, null);
        ISO8601DateDeserializer dateDeserializer = new ISO8601DateDeserializer(defaultDeserializer);
        GeoPointDeserializer geoPointDeserializer = new GeoPointDeserializer(dateDeserializer);
        SimpleModule module = new SimpleModule();
        module.addDeserializer(Object.class, geoPointDeserializer);
        mapper.registerModule(module);
    }
}
