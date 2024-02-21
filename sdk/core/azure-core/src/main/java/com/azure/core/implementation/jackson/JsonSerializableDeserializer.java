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
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.deser.ResolvableDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.io.IOException;

final class JsonSerializableDeserializer extends StdDeserializer<Object> implements ResolvableDeserializer {
    private static final long serialVersionUID = -3097250410847767845L;

    private static final ClientLogger LOGGER = new ClientLogger(JsonSerializableDeserializer.class);

    /**
     * The default mapperAdapter for the current type.
     */
    private final JsonDeserializer<?> defaultDeserializer;

    private final Class<? extends JsonSerializable<?>> jsonSerializableType;
    private final ReflectiveInvoker readJson;

    @SuppressWarnings("unchecked")
    private JsonSerializableDeserializer(Class<?> t, JsonDeserializer<?> defaultDeserializer) {
        super(t);
        this.defaultDeserializer = defaultDeserializer;

        this.jsonSerializableType = (Class<? extends JsonSerializable<?>>) t;
        try {
            this.readJson = ReflectionUtils.getMethodInvoker(t, t.getDeclaredMethod("fromJson", JsonReader.class));
        } catch (Exception e) {
            throw LOGGER.logExceptionAsError(new IllegalStateException(e));
        }
    }

    /**
     * Gets a module wrapping this deserializer as an adapter for the Jackson ObjectMapper.
     *
     * @return A module to be plugged into Jackson ObjectMapper.
     */
    public static Module getModule() {
        return new SimpleModule().setDeserializerModifier(new BeanDeserializerModifier() {
            @Override
            public JsonDeserializer<?> modifyDeserializer(DeserializationConfig config, BeanDescription beanDesc,
                JsonDeserializer<?> serializer) {
                return ReflectionSerializable.supportsJsonSerializable(beanDesc.getBeanClass())
                    ? new JsonSerializableDeserializer(beanDesc.getBeanClass(), serializer)
                    : serializer;
            }
        });
    }

    @Override
    public JsonSerializable<?> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        try {
            return jsonSerializableType.cast(readJson.invokeWithArguments(AzureJsonUtils.createReader(p)));
        } catch (Exception e) {
            IOException ioException = (e instanceof IOException) ? (IOException) e : new IOException(e);
            throw LOGGER.logThrowableAsError(ioException);
        }
    }

    @Override
    public void resolve(DeserializationContext ctxt) throws JsonMappingException {
        ((ResolvableDeserializer) defaultDeserializer).resolve(ctxt);
    }
}
