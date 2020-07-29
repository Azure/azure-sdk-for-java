// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.avro.jackson;

import com.azure.core.experimental.serializer.ObjectSerializer;
import com.azure.core.experimental.serializer.TypeReference;
import com.azure.core.util.logging.ClientLogger;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.dataformat.avro.AvroMapper;
import com.fasterxml.jackson.dataformat.avro.AvroSchema;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;

/**
 * Jackson Avro based implementation of the {@link ObjectSerializer} interface.
 */
public final class JacksonAvroSerializer implements ObjectSerializer {
    private final ClientLogger logger = new ClientLogger(JacksonAvroSerializer.class);

    private final AvroSchema avroSchema;
    private final AvroMapper avroMapper;
    private final TypeFactory typeFactory;

    JacksonAvroSerializer(AvroSchema avroSchema, AvroMapper avroMapper) {
        this.avroSchema = avroSchema;
        this.avroMapper = avroMapper;
        this.typeFactory = avroMapper.getTypeFactory();
    }

    @Override
    public <T> T deserialize(InputStream stream, TypeReference<T> typeReference) {
        if (stream == null) {
            return null;
        }

        if ("null".equalsIgnoreCase(avroSchema.getAvroSchema().getType().getName())) {
            return null;
        }

        try {
            return avroMapper.readerFor(typeFactory.constructType(typeReference.getJavaType()))
                .with(avroSchema)
                .readValue(stream);
        } catch (IOException ex) {
            throw logger.logExceptionAsError(new UncheckedIOException(ex));
        }
    }

    @Override
    public <T> Mono<T> deserializeAsync(InputStream stream, TypeReference<T> typeReference) {
        return Mono.fromCallable(() -> deserialize(stream, typeReference));
    }

    @Override
    public <S extends OutputStream> S serialize(S stream, Object value) {
        try {
            avroMapper.writer().with(avroSchema).writeValue(stream, value);
        } catch (IOException ex) {
            throw logger.logExceptionAsError(new UncheckedIOException(ex));
        }

        return stream;
    }

    @Override
    public <S extends OutputStream> Mono<S> serializeAsync(S stream, Object value) {
        return Mono.fromCallable(() -> serialize(stream, value));
    }
}
