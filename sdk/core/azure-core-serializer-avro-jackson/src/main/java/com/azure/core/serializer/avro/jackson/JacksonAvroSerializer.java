// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.avro.jackson;

import com.azure.core.experimental.serializer.AvroSerializer;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.TypeReference;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.dataformat.avro.AvroMapper;
import com.fasterxml.jackson.dataformat.avro.AvroSchema;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;

/**
 * Jackson Avro based implementation of the {@link AvroSerializer} interface.
 */
public final class JacksonAvroSerializer implements AvroSerializer {
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
    public void serialize(OutputStream stream, Object value) {
        try {
            avroMapper.writer().with(avroSchema).writeValue(stream, value);
        } catch (IOException ex) {
            throw logger.logExceptionAsError(new UncheckedIOException(ex));
        }
    }

    @Override
    public Mono<Void> serializeAsync(OutputStream stream, Object value) {
        return Mono.fromRunnable(() -> serialize(stream, value));
    }
}
