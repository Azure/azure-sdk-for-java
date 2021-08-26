// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.json.jackson;

import com.azure.core.implementation.jackson.ObjectMapperShim;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.JsonSerializer;
import com.azure.core.util.serializer.MemberNameConverter;
import com.azure.core.util.serializer.TypeReference;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.lang.reflect.Member;

/**
 * Jackson based implementation of the {@link JsonSerializer} and {@link MemberNameConverter} interfaces.
 */
public final class JacksonJsonSerializer implements JsonSerializer, MemberNameConverter {

    private final ClientLogger logger = new ClientLogger(JacksonJsonSerializer.class);

    private final ObjectMapperShim mapper;

    /**
     * Constructs a {@link JsonSerializer} using the passed Jackson serializer.
     *
     * @param mapper Configured Jackson serializer.
     */
    JacksonJsonSerializer(ObjectMapperShim mapper) {
        this.mapper = mapper;
    }

    @Override
    public <T> T deserializeFromBytes(byte[] data, TypeReference<T> typeReference) {
        if (data == null) {
            return null;
        }

        try {
            return mapper.readValue(data, typeReference.getJavaType());
        } catch (IOException ex) {
            throw logger.logExceptionAsError(new UncheckedIOException(ex));
        }
    }

    @Override
    public <T> T deserialize(InputStream stream, TypeReference<T> typeReference) {
        if (stream == null) {
            return null;
        }

        try {
            return mapper.readValue(stream, typeReference.getJavaType());
        } catch (IOException ex) {
            throw logger.logExceptionAsError(new UncheckedIOException(ex));
        }
    }

    @Override
    public <T> Mono<T> deserializeFromBytesAsync(byte[] data, TypeReference<T> typeReference) {
        return Mono.fromCallable(() -> deserializeFromBytes(data, typeReference));
    }

    @Override
    public <T> Mono<T> deserializeAsync(InputStream stream, TypeReference<T> typeReference) {
        return Mono.fromCallable(() -> deserialize(stream, typeReference));
    }

    @Override
    public byte[] serializeToBytes(Object value) {
        try {
            return mapper.writeValueAsBytes(value);
        } catch (IOException ex) {
            throw logger.logExceptionAsError(new UncheckedIOException(ex));
        }
    }

    @Override
    public void serialize(OutputStream stream, Object value) {
        try {
            mapper.writeValue(stream, value);
        } catch (IOException ex) {
            throw logger.logExceptionAsError(new UncheckedIOException(ex));
        }
    }

    @Override
    public Mono<byte[]> serializeToBytesAsync(Object value) {
        return Mono.fromCallable(() -> this.serializeToBytes(value));
    }

    @Override
    public Mono<Void> serializeAsync(OutputStream stream, Object value) {
        return Mono.fromRunnable(() -> serialize(stream, value));
    }

    @Override
    public String convertMemberName(Member member) {
        return mapper.convertMemberName(member);
    }
}
