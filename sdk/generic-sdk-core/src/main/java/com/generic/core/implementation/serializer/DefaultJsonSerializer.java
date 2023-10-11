// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.implementation.serializer;

import com.generic.core.util.logging.ClientLogger;
import com.generic.core.util.serializer.JacksonAdapter;
import com.generic.core.util.serializer.JsonSerializer;
import com.generic.core.util.serializer.SerializerAdapter;
import com.generic.core.util.serializer.SerializerEncoding;
import com.generic.core.util.serializer.TypeReference;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;

/**
 * Jackson based implementation of the {@link JsonSerializer}. This can be used as a default Json serializer if
 * no JsonSerializerProvider is in the class path.
 */
public final class DefaultJsonSerializer implements JsonSerializer {
    // DefaultJsonSerializer is a commonly used class, use a static logger.
    private static final ClientLogger LOGGER = new ClientLogger(DefaultJsonSerializer.class);

    private final SerializerAdapter jacksonAdapter = JacksonAdapter.createDefaultSerializerAdapter();

    @Override
    public <T> T deserializeFromBytes(byte[] data, TypeReference<T> typeReference) {
        try {
            return jacksonAdapter.deserialize(data, typeReference.getJavaType(), SerializerEncoding.JSON);
        } catch (IOException e) {
            throw LOGGER.logExceptionAsError(new UncheckedIOException(e));
        }
    }

    @Override
    public <T> T deserialize(InputStream stream, TypeReference<T> typeReference) {
        try {
            return jacksonAdapter.deserialize(stream, typeReference.getJavaType(), SerializerEncoding.JSON);
        } catch (IOException e) {
            throw LOGGER.logExceptionAsError(new UncheckedIOException(e));
        }
    }

    @Override
    public byte[] serializeToBytes(Object value) {
        try {
            return jacksonAdapter.serializeToBytes(value, SerializerEncoding.JSON);
        } catch (IOException e) {
            throw LOGGER.logExceptionAsError(new UncheckedIOException(e));
        }
    }

    @Override
    public void serialize(OutputStream stream, Object value) {
        try {
            jacksonAdapter.serialize(value, SerializerEncoding.JSON, stream);
        } catch (IOException e) {
            throw LOGGER.logExceptionAsError(new UncheckedIOException(e));
        }
    }
}
