// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.jackson;

import com.azure.core.implementation.ReflectionSerializable;
import com.azure.json.JsonSerializable;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.io.IOException;

@SuppressWarnings("rawtypes")
final class JsonSerializableSerializer extends JsonSerializer<JsonSerializable> {
    /*
     * The object mapper for default serializations.
     */
    private final ObjectMapper mapper;

    /**
     * Gets a module wrapping this serializer as an adapter for the Jackson ObjectMapper.
     *
     * @param mapper the object mapper for default serializations
     * @return A module to be plugged into Jackson ObjectMapper.
     */
    public static Module getModule(ObjectMapper mapper) {
        return new SimpleModule().addSerializer(JsonSerializable.class, new JsonSerializableSerializer(mapper));
    }

    JsonSerializableSerializer(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void serialize(JsonSerializable value, JsonGenerator gen, SerializerProvider serializers)
        throws IOException {
        if (ReflectionSerializable.supportsJsonSerializable(value.getClass())) {
            // Do not use this JacksonJsonWriter in a try-with-resources block as closing it closes the underlying
            // JsonGenerator which could cause problems in further serialization done.
            new JacksonJsonWriter(gen).writeJson(value);
        } else {
            mapper.writeValue(gen, value);
        }
    }

    @Override
    public Class<JsonSerializable> handledType() {
        return JsonSerializable.class;
    }
}
