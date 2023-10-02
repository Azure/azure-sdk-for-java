// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.implementation.jackson;

import com.typespec.json.JsonSerializable;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.io.IOException;

@SuppressWarnings("rawtypes")
final class JsonSerializableSerializer extends JsonSerializer<JsonSerializable> {
    private static final Module MODULE = new SimpleModule()
        .addSerializer(JsonSerializable.class, new JsonSerializableSerializer());

    /**
     * Gets a module wrapping this serializer as an adapter for the Jackson ObjectMapper.
     *
     * @return A module to be plugged into Jackson ObjectMapper.
     */
    public static Module getModule() {
        return MODULE;
    }

    @Override
    public void serialize(JsonSerializable value, JsonGenerator gen, SerializerProvider serializers)
        throws IOException {
        // Do not use this JacksonJsonWriter in a try-with-resources block as closing it closes the underlying
        // JsonGenerator which could cause problems in further serialization done.
        new JacksonJsonWriter(gen).writeJson(value);
    }

    @Override
    public Class<JsonSerializable> handledType() {
        return JsonSerializable.class;
    }
}
