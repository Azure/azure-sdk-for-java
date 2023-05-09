// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.json.gson.implementation;

import com.azure.core.implementation.ReflectionUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.json.JsonSerializable;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

/**
 * Implementation of GSON's {@link TypeAdapter} that is capable of handling {@link JsonSerializable} types.
 */
public class JsonSerializableTypeAdapter extends TypeAdapter<JsonSerializable<?>> {
    private static final ClientLogger LOGGER = new ClientLogger(JsonSerializableTypeAdapter.class);

    private final Class<? extends JsonSerializable<?>> jsonSerializableType;
    private final MethodHandle readJson;

    /**
     * Creates an instance of {@link JsonSerializableTypeAdapter}.
     *
     * @param jsonSerializableType The type implementing {@link JsonSerializable} being handled by this
     * {@link TypeAdapter}.
     */
    public JsonSerializableTypeAdapter(Class<? extends JsonSerializable<?>> jsonSerializableType) {
        this.jsonSerializableType = jsonSerializableType;
        try {
            MethodHandles.Lookup lookup = ReflectionUtils.getLookupToUse(jsonSerializableType);
            this.readJson = lookup.unreflect(jsonSerializableType.getDeclaredMethod("fromJson",
                com.azure.json.JsonReader.class));
        } catch (Exception e) {
            throw LOGGER.logExceptionAsError(new IllegalStateException(e));
        }
    }

    @Override
    public void write(JsonWriter out, JsonSerializable<?> value) throws IOException {
        new GsonJsonWriter(out).writeJson(value);
    }

    @Override
    public JsonSerializable<?> read(JsonReader in) throws IOException {
        try {
            return jsonSerializableType.cast(readJson.invokeWithArguments(new GsonJsonReader(in, null, true)));
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
