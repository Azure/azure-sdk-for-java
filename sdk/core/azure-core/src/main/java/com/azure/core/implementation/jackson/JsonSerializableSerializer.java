// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.jackson;

import com.azure.core.implementation.ReflectionSerializable;
import com.azure.json.JsonSerializable;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.fasterxml.jackson.databind.ser.ResolvableSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

final class JsonSerializableSerializer extends StdSerializer<Object> implements ResolvableSerializer {
    private static final long serialVersionUID = 6099245008569550678L;

    /**
     * The default mapperAdapter for the current type.
     */
    private final JsonSerializer<?> defaultSerializer;

    private JsonSerializableSerializer(Class<?> t, JsonSerializer<?> defaultSerializer) {
        super(t, false);
        this.defaultSerializer = defaultSerializer;
    }

    /**
     * Gets a module wrapping this serializer as an adapter for the Jackson ObjectMapper.
     *
     * @return A module to be plugged into Jackson ObjectMapper.
     */
    public static Module getModule() {
        return new SimpleModule().setSerializerModifier(new BeanSerializerModifier() {
            @Override
            public JsonSerializer<?> modifySerializer(SerializationConfig config, BeanDescription beanDesc,
                JsonSerializer<?> serializer) {
                return ReflectionSerializable.supportsJsonSerializable(beanDesc.getBeanClass())
                    ? new JsonSerializableSerializer(beanDesc.getBeanClass(), serializer)
                    : serializer;
            }
        });
    }

    @Override
    public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        // Do not use this JacksonJsonWriter in a try-with-resources block as closing it closes the underlying
        // JsonGenerator which could cause problems in further serialization done.
        new JacksonJsonWriter(gen).writeJson((JsonSerializable<?>) value);
    }

    @Override
    public void resolve(SerializerProvider provider) throws JsonMappingException {
        ((ResolvableSerializer) defaultSerializer).resolve(provider);
    }

    @Override
    public void serializeWithType(Object value, JsonGenerator gen, SerializerProvider serializers,
        TypeSerializer typeSer) throws IOException {
        serialize(value, gen, serializers);
    }
}
