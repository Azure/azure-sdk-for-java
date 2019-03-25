/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.common.implementation.serializer.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.azure.common.implementation.DateTimeRfc1123;

import java.io.IOException;

/**
 * Custom serializer for serializing {@link DateTimeRfc1123} object into RFC1123 formats.
 */
final class DateTimeRfc1123Serializer extends JsonSerializer<DateTimeRfc1123> {
    /**
     * Gets a module wrapping this serializer as an adapter for the Jackson
     * ObjectMapper.
     *
     * @return a simple module to be plugged onto Jackson ObjectMapper.
     */
    public static SimpleModule getModule() {
        SimpleModule module = new SimpleModule();
        module.addSerializer(DateTimeRfc1123.class, new DateTimeRfc1123Serializer());
        return module;
    }

    @Override
    public void serialize(DateTimeRfc1123 value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        if (provider.isEnabled(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)) {
            jgen.writeNumber(value.dateTime().toInstant().toEpochMilli());
        } else {
            jgen.writeString(value.toString()); //Use the default toString as it is RFC1123.
        }
    }
}
