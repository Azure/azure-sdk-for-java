// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.jackson;

import com.azure.core.implementation.ReflectionSerializable;
import com.azure.json.JsonSerializable;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.BeanSerializerBuilder;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;

import java.io.IOException;

@SuppressWarnings("rawtypes")
final class JsonSerializableSerializer extends JsonSerializer<JsonSerializable> {
    /**
     * Gets a module wrapping this serializer as an adapter for the Jackson ObjectMapper.
     *
     * @return A module to be plugged into Jackson ObjectMapper.
     */
    public static Module getModule() {
        return new SimpleModule().setSerializerModifier(new BeanSerializerModifier() {
            @Override
            public BeanSerializerBuilder updateBuilder(SerializationConfig config, BeanDescription beanDesc,
                BeanSerializerBuilder builder) {
                if (ReflectionSerializable.supportsJsonSerializable(beanDesc.getBeanClass())) {
                    // Modify what the BeanSerializerBuilder will build for the bean class.
                    return new BeanSerializerBuilder(beanDesc) {
                        @Override
                        public JsonSerializer<?> build() {
                            return new JsonSerializableSerializer();
                        }
                    };
                }

                return builder;
            }

            @Override
            public JsonSerializer<?> modifySerializer(SerializationConfig config, BeanDescription beanDesc,
                JsonSerializer<?> serializer) {
                return ReflectionSerializable.supportsJsonSerializable(beanDesc.getBeanClass())
                    ? new JsonSerializableSerializer()
                    : serializer;
            }
        });
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
