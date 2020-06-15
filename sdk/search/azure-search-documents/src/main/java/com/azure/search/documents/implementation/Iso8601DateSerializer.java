// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Custom serializer to serialize {@link java.util.Date} to Iso8601 standard date format "yyyy-MM-dd'T'hh:mm:ss.SSS'Z'".
 */
final class Iso8601DateSerializer extends JsonSerializer<Date> {

    /**
     * Gets a module wrapping this serializer as an adapter for the Jackson
     * ObjectMapper.
     *
     * @return a simple module to be plugged onto Jackson ObjectMapper.
     */
    public static SimpleModule getModule() {
        SimpleModule module = new SimpleModule();
        module.addSerializer(Date.class, new Iso8601DateSerializer());
        return module;
    }

    @Override
    public void serialize(Date dateValue, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        String dateString = format.format(dateValue);
        gen.writeString(dateString);
    }
}
