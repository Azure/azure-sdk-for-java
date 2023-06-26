// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.json.gson;

import com.azure.core.serializer.json.gson.implementation.AzureJsonUtils;
import com.azure.core.serializer.json.gson.implementation.JsonSerializableTypeAdapter;
import com.azure.json.JsonOptions;
import com.azure.json.JsonProvider;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonWriter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

/**
 * GSON-based implementation of {@link JsonProvider}.
 */
public final class GsonJsonProvider implements JsonProvider {
    /**
     * Creates an instance of {@link GsonJsonProvider}.
     */
    public GsonJsonProvider() {
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
     * Creates an instance of {@link JsonReader} wrapping a GSON {@link com.google.gson.stream.JsonReader}.
     *
     * @param reader The {@link com.google.gson.stream.JsonReader} parsing JSON.
     * @param options The options used to create the reader.
     * @return A {@link JsonReader} wrapping the {@link com.google.gson.stream.JsonReader}.
     * @throws NullPointerException If {@code reader} is null.
     */
    public JsonReader createReader(com.google.gson.stream.JsonReader reader, JsonOptions options) {
        return AzureJsonUtils.createReader(reader, options);
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
     * Creates an instance of {@link JsonWriter} wrapping a GSON {@link com.google.gson.stream.JsonWriter}.
     *
     * @param writer The {@link com.google.gson.stream.JsonWriter} writing JSON.
     * @return A {@link JsonWriter} wrapping the {@link com.google.gson.stream.JsonWriter}.
     * @throws NullPointerException If {@code writer} is null.
     */
    public JsonWriter createWriter(com.google.gson.stream.JsonWriter writer) {
        return AzureJsonUtils.createWriter(writer);
    }

    /**
     * Returns a GSON {@link TypeAdapterFactory} that allows for {@code com.azure.json} implementations to handle
     * deserialization and serialization of {@link JsonSerializable} types within a GSON context.
     * <p>
     * Use the {@link TypeAdapterFactory} returned by this method when creating your {@link Gson} with
     * {@link GsonBuilder} to have GSON support {@link JsonSerializable} types.
     *
     * @return A GSON {@link TypeAdapterFactory} that handles deserialization and serialization of
     * {@link JsonSerializable} types.
     */
    public static TypeAdapterFactory getJsonSerializableTypeAdapterFactory() {
        return new TypeAdapterFactory() {
            @SuppressWarnings("unchecked")
            @Override
            public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
                return JsonSerializable.class.isAssignableFrom(type.getRawType())
                    ? (TypeAdapter<T>) new JsonSerializableTypeAdapter(
                        (Class<? extends JsonSerializable<?>>) type.getRawType())
                    : null;
            }
        };
    }
}
