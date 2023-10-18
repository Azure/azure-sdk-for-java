// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.implementation.serializer;

import com.generic.core.util.logging.ClientLogger;
import com.generic.core.util.serializer.JsonSerializer;
import com.generic.core.util.serializer.TypeReference;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Jackson based implementation of the {@link JsonSerializer}. This can be used as a default Json serializer if
 * no JsonSerializerProvider is in the class path.
 */
public final class DefaultJsonSerializer implements JsonSerializer {
    // DefaultJsonSerializer is a commonly used class, use a static logger.
    private static final ClientLogger LOGGER = new ClientLogger(DefaultJsonSerializer.class);

    @Override
    public <T> T deserializeFromBytes(byte[] data, TypeReference<T> typeReference) {
//        try {
//            return jacksonAdapter.deserialize(data, typeReference.getJavaType(), SerializerEncoding.JSON);
//        } catch (IOException e) {
//            throw LOGGER.logExceptionAsError(new UncheckedIOException(e));
//        }
        return null;
    }

    @Override
    public <T> T deserialize(InputStream stream, TypeReference<T> typeReference) {
        return null;
    }

    @Override
    public byte[] serializeToBytes(Object value) {
//        try {
//            return jacksonAdapter.serializeToBytes(value, SerializerEncoding.JSON);
//        } catch (IOException e) {
//            throw LOGGER.logExceptionAsError(new UncheckedIOException(e));
//        }
        return null;
    }

    @Override
    public void serialize(OutputStream stream, Object value) {
//        return null;
    }
}
