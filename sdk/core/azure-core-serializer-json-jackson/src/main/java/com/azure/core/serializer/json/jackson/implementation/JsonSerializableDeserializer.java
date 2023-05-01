// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.json.jackson.implementation;

import com.azure.core.implementation.ReflectionUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

public class JsonSerializableDeserializer extends JsonDeserializer<JsonSerializable<?>> {
    private static final ClientLogger LOGGER = new ClientLogger(JsonSerializableDeserializer.class);

    private final Class<? extends JsonSerializable<?>> jsonSerializableType;
    private final MethodHandle readJson;

    /**
     * Creates an instance of {@link JsonSerializableDeserializer}.
     *
     * @param jsonSerializableType The type implementing {@link JsonSerializable} being deserialized.
     */
    public JsonSerializableDeserializer(Class<? extends JsonSerializable<?>> jsonSerializableType) {
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
            return jsonSerializableType.cast(readJson.invokeWithArguments(
                new JacksonJsonReader(p, null, null, false, null)));
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
