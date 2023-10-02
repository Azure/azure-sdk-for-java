// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.implementation.jackson;

import com.typespec.core.implementation.ReflectionUtils;
import com.typespec.core.util.logging.ClientLogger;
import com.typespec.json.JsonReader;
import com.typespec.json.JsonSerializable;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

final class JsonSerializableDeserializer extends JsonDeserializer<JsonSerializable<?>> {
    private static final ClientLogger LOGGER = new ClientLogger(JsonSerializableDeserializer.class);

    private static final Module MODULE = new SimpleModule()
        .setDeserializerModifier(new BeanDeserializerModifier() {
            @SuppressWarnings("unchecked")
            @Override
            public JsonDeserializer<?> modifyDeserializer(DeserializationConfig config, BeanDescription beanDesc,
                JsonDeserializer<?> deserializer) {
                return (JsonSerializable.class.isAssignableFrom(beanDesc.getBeanClass()))
                    ? new JsonSerializableDeserializer((Class<? extends JsonSerializable<?>>) beanDesc.getBeanClass())
                    : deserializer;
            }
        });

    private final Class<? extends JsonSerializable<?>> jsonSerializableType;
    private final MethodHandle readJson;

    /**
     * Gets a module wrapping this deserializer as an adapter for the Jackson ObjectMapper.
     *
     * @return A module to be plugged into Jackson ObjectMapper.
     */
    public static Module getModule() {
        return MODULE;
    }

    /**
     * Creates an instance of {@link JsonSerializableDeserializer}.
     *
     * @param jsonSerializableType The type implementing {@link JsonSerializable} being deserialized.
     */
    JsonSerializableDeserializer(Class<? extends JsonSerializable<?>> jsonSerializableType) {
        this.jsonSerializableType = jsonSerializableType;
        try {
            MethodHandles.Lookup lookup = ReflectionUtils.getLookupToUse(jsonSerializableType);
            this.readJson = lookup.unreflect(jsonSerializableType.getDeclaredMethod("fromJson", JsonReader.class));
        } catch (Exception e) {
            throw LOGGER.logExceptionAsError(new IllegalStateException(e));
        }
    }

    @Override
    public JsonSerializable<?> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        try {
            return jsonSerializableType.cast(readJson.invokeWithArguments(AzureJsonUtils.createReader(p)));
        } catch (Throwable e) {
            if (e instanceof IOException) {
                throw (IOException) e;
            } else if (e instanceof Exception) {
                throw new IOException(e);
            } else {
                throw (Error) e;
            }
        }
    }
}
