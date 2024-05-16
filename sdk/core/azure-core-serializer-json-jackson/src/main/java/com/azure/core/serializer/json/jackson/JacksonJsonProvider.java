// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.json.jackson;

import com.azure.core.serializer.json.jackson.implementation.AzureJsonUtils;
import com.azure.core.serializer.json.jackson.implementation.JsonSerializableDeserializer;
import com.azure.core.serializer.json.jackson.implementation.JsonSerializableSerializer;
import com.azure.json.JsonOptions;
import com.azure.json.JsonProvider;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonWriter;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

/**
 * Jackson-based implementation of {@link JsonProvider}.
 */
public class JacksonJsonProvider implements JsonProvider {
    /**
     * Creates an instance of {@link JacksonJsonProvider}.
     */
    public JacksonJsonProvider() {
    }

    @Override
    public JsonReader createReader(byte[] json, JsonOptions options) throws IOException {
        return AzureJsonUtils.createReader(json, options);
    }

    @Override
    public JsonReader createReader(String json, JsonOptions options) throws IOException {
        return AzureJsonUtils.createReader(json, options);
    }

    @Override
    public JsonReader createReader(InputStream json, JsonOptions options) throws IOException {
        return AzureJsonUtils.createReader(json, options);
    }

    @Override
    public JsonReader createReader(Reader json, JsonOptions options) throws IOException {
        return AzureJsonUtils.createReader(json, options);
    }

    /**
     * Creates an instance of {@link JsonReader} wrapping a Jackson {@link JsonParser}.
     *
     * @param parser The {@link JsonParser} parsing JSON.
     * @return A {@link JsonReader} wrapping the {@link JsonParser}.
     * @throws NullPointerException If {@code parser} is null.
     */
    public JsonReader createReader(JsonParser parser) {
        return AzureJsonUtils.createReader(parser);
    }

    @Override
    public JsonWriter createWriter(OutputStream json, JsonOptions options) throws IOException {
        return AzureJsonUtils.createWriter(json, options);
    }

    @Override
    public JsonWriter createWriter(Writer json, JsonOptions options) throws IOException {
        return AzureJsonUtils.createWriter(json, options);
    }

    /**
     * Creates an instance of {@link JsonWriter} wrapping a Jackson {@link JsonGenerator}.
     *
     * @param generator The {@link JsonGenerator} writing JSON.
     * @return A {@link JsonWriter} wrapping the {@link JsonGenerator}.
     * @throws NullPointerException If {@code generator} is null.
     */
    public JsonWriter createWriter(JsonGenerator generator) {
        return AzureJsonUtils.createWriter(generator);
    }

    /**
     * Returns a Jackson Databind {@link Module} that allows for {@code com.azure.json} implementations to handle
     * deserialization and serialization of {@link JsonSerializable} types within a Jackson Databind context.
     * <p>
     * Use the {@link Module} returned by this method with your instance of {@link ObjectMapper} to have Jackson
     * Databind support {@link JsonSerializable} types.
     *
     * @return A Jackson Databind {@link Module} that handles deserialization and serialization of
     * {@link JsonSerializable} types.
     */
    public static Module getJsonSerializableDatabindModule() {
        return new SimpleModule().setDeserializerModifier(new BeanDeserializerModifier() {
            @SuppressWarnings("unchecked")
            @Override
            public JsonDeserializer<?> modifyDeserializer(DeserializationConfig config, BeanDescription beanDesc,
                JsonDeserializer<?> deserializer) {
                return (JsonSerializable.class.isAssignableFrom(beanDesc.getBeanClass()))
                    ? new JsonSerializableDeserializer((Class<? extends JsonSerializable<?>>) beanDesc.getBeanClass())
                    : deserializer;
            }
        }).addSerializer(JsonSerializable.class, new JsonSerializableSerializer());
    }
}
