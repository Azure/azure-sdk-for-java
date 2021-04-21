// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.serializer;

import com.azure.core.implementation.UnixTime;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.deser.InstantDeserializer;

import java.io.IOException;

/**
 * Custom deserializer for deserializing epoch formats into {@link UnixTime} objects.
 */
final class UnixTimeDeserializer extends JsonDeserializer<UnixTime> {
    private static final SimpleModule MODULE = new SimpleModule()
        .addDeserializer(UnixTime.class, new UnixTimeDeserializer());

    /**
     * Gets a module wrapping this serializer as an adapter for the Jackson
     * ObjectMapper.
     *
     * @return a simple module to be plugged onto Jackson ObjectMapper.
     */
    public static SimpleModule getModule() {
        return MODULE;
    }

    @Override
    public UnixTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        return new UnixTime(InstantDeserializer.OFFSET_DATE_TIME.deserialize(p, ctxt));
    }
}
