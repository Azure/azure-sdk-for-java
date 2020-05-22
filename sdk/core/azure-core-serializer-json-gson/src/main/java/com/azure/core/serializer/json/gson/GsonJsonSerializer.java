// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.json.gson;

import com.azure.core.util.CoreUtils;
import com.azure.core.implementation.serializer.JsonSerializer;
import com.google.gson.Gson;

import java.nio.charset.StandardCharsets;

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
    public <T> T deserialize(byte[] input, Class<T> clazz) {
        return gson.fromJson(CoreUtils.bomAwareToString(input, null), clazz);
    }

    @Override
    public byte[] serialize(Object value) {
        return gson.toJson(value).getBytes(StandardCharsets.UTF_8);
    }
}
