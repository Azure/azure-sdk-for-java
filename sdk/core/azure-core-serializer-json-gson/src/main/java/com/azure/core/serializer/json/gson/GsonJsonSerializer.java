// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.json.gson;

import com.azure.core.serializer.JsonSerializer;
import com.azure.core.util.CoreUtils;
import com.google.gson.Gson;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * GSON based implementation of the {@link JsonSerializer} interface.
 */
public final class GsonJsonSerializer implements JsonSerializer {
    private final Gson gson;

    /**
     * Constructs a {@link JsonSerializer} using the passed {@link Gson} serializer.
     *
     * @param gson Configured {@link Gson} serializer.
     */
    GsonJsonSerializer(Gson gson) {
        this.gson = gson;
    }

    @Override
    public <T> T read(byte[] input, Class<T> clazz) {
        return gson.fromJson(CoreUtils.bomAwareToString(input, null), clazz);
    }

    @Override
    public byte[] write(Object value) {
        return gson.toJson(value).getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public void write(Object value, OutputStream stream) {
        Objects.requireNonNull(stream, "'stream' cannot be null.");

        gson.toJson(value, new OutputStreamWriter(stream, StandardCharsets.UTF_8));
    }
}
