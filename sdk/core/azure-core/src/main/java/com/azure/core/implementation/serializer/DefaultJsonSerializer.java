// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.serializer;

import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.JsonSerializer;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.core.util.serializer.TypeReference;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;

/**
 * Jackson based implementation of the {@link JsonSerializer}. This can be used as a default Json serializer if
 * no JsonSerializerProvider is in the class path.
 */
public final class DefaultJsonSerializer implements JsonSerializer {
    private final SerializerAdapter jacksonAdapter = JacksonAdapter.createDefaultSerializerAdapter();
    private final ClientLogger logger = new ClientLogger(DefaultJsonSerializer.class);

    @Override
    public <T> T deserializeFromBytes(byte[] data, TypeReference<T> typeReference) {
        try {
            return jacksonAdapter.deserialize(data, typeReference.getJavaType(), SerializerEncoding.JSON);
        } catch (IOException e) {
            throw logger.logExceptionAsError(new UncheckedIOException(e));
        }
    }

    @Override
    public <T> T deserialize(InputStream stream, TypeReference<T> typeReference) {
        try {
            return jacksonAdapter.deserialize(stream, typeReference.getJavaType(), SerializerEncoding.JSON);
        } catch (IOException e) {
            throw logger.logExceptionAsError(new UncheckedIOException(e));
        }
    }

    @Override
    public <T> Mono<T> deserializeFromBytesAsync(byte[] data, TypeReference<T> typeReference) {
        return Mono.defer(() -> Mono.fromCallable(() -> deserializeFromBytes(data, typeReference)));
    }

    @Override
    public <T> Mono<T> deserializeAsync(InputStream stream, TypeReference<T> typeReference) {
        return Mono.defer(() -> Mono.fromCallable(() -> deserialize(stream, typeReference)));
    }

    @Override
    public byte[] serializeToBytes(Object value) {
        try {
            return jacksonAdapter.serializeToBytes(value, SerializerEncoding.JSON);
        } catch (IOException e) {
            throw logger.logExceptionAsError(new UncheckedIOException(e));
        }
    }

    @Override
    public void serialize(OutputStream stream, Object value) {
        try {
            jacksonAdapter.serialize(value, SerializerEncoding.JSON, stream);
        } catch (IOException e) {
            throw logger.logExceptionAsError(new UncheckedIOException(e));
        }
    }

    @Override
    public Mono<byte[]> serializeToBytesAsync(Object value) {
        return Mono.defer(() -> Mono.fromCallable(() -> serializeToBytes(value)));
    }

    @Override
    public Mono<Void> serializeAsync(OutputStream stream, Object value) {
        return Mono.fromRunnable(() -> serialize(stream, value));
    }
}
