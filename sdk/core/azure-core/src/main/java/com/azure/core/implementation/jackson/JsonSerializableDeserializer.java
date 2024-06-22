// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.jackson;

import com.azure.core.implementation.ReflectionSerializable;
import com.azure.core.implementation.ReflectionUtils;
import com.azure.core.implementation.ReflectiveInvoker;
import com.azure.core.util.logging.ClientLogger;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.io.IOException;

final class JsonSerializableDeserializer extends JsonDeserializer<JsonSerializable<?>> {
    private static final ClientLogger LOGGER = new ClientLogger(JsonSerializableDeserializer.class);

    private final Class<? extends JsonSerializable<?>> jsonSerializableType;
    private final ReflectiveInvoker readJson;

    /**
     * Gets a module wrapping this deserializer as an adapter for the Jackson ObjectMapper.
     *
     * @return A module to be plugged into Jackson ObjectMapper.
     */
    public static Module getModule() {
        return new SimpleModule().setDeserializerModifier(new BeanDeserializerModifier() {
            @SuppressWarnings("unchecked")
            @Override
            public JsonDeserializer<?> modifyDeserializer(DeserializationConfig config, BeanDescription beanDesc,
                JsonDeserializer<?> deserializer) {
                return ReflectionSerializable.supportsJsonSerializable(beanDesc.getBeanClass())
                    ? new JsonSerializableDeserializer((Class<? extends JsonSerializable<?>>) beanDesc.getBeanClass())
                    : deserializer;
            }
        });
    }

    /**
     * Creates an instance of {@link JsonSerializableDeserializer}.
     *
     * @param jsonSerializableType The type implementing {@link JsonSerializable} being deserialized.
     */
    JsonSerializableDeserializer(Class<? extends JsonSerializable<?>> jsonSerializableType) {
        this.jsonSerializableType = jsonSerializableType;
        try {
            this.readJson = ReflectionUtils.getMethodInvoker(jsonSerializableType,
                jsonSerializableType.getDeclaredMethod("fromJson", JsonReader.class));
        } catch (Exception e) {
            throw LOGGER.logExceptionAsError(new IllegalStateException(e));
        }
    }

    @Override
    public JsonSerializable<?> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        try {
            return jsonSerializableType.cast(readJson.invokeStatic(AzureJsonUtils.createReader(p)));
        } catch (Exception e) {
            IOException ioException = (e instanceof IOException) ? (IOException) e : new IOException(e);
            throw LOGGER.logThrowableAsError(ioException);
        }
    }
}
