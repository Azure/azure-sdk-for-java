// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.avro.jackson;

import com.azure.core.experimental.serializer.ObjectSerializer;
import com.azure.core.util.logging.ClientLogger;
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
    private static final ClientLogger LOGGER = new ClientLogger(JacksonAvroSerializer.class);

    private final AvroSchema avroSchema;
    private final AvroMapper avroMapper;

    JacksonAvroSerializer(AvroSchema avroSchema, AvroMapper avroMapper) {
        this.avroSchema = avroSchema;
        this.avroMapper = avroMapper;
    }

    @Override
    public <T> T deserializeSync(InputStream stream, Class<T> clazz) {
        if (stream == null) {
            return null;
        }

        if ("null".equalsIgnoreCase(avroSchema.getAvroSchema().getType().getName())) {
            return null;
        }

        try {
            return avroMapper.readerFor(clazz).with(avroSchema).readValue(stream);
        } catch (IOException ex) {
            throw LOGGER.logExceptionAsError(new UncheckedIOException(ex));
        }
    }

    @Override
    public <T> Mono<T> deserialize(InputStream stream, Class<T> clazz) {
        return Mono.fromCallable(() -> deserializeSync(stream, clazz));
    }

    @Override
    public <S extends OutputStream> S serializeSync(S stream, Object value) {
        try {
            avroMapper.writer().with(avroSchema).writeValue(stream, value);
        } catch (IOException ex) {
            throw LOGGER.logExceptionAsError(new UncheckedIOException(ex));
        }

        return stream;
    }

    @Override
    public <S extends OutputStream> Mono<S> serialize(S stream, Object value) {
        return Mono.fromCallable(() -> serializeSync(stream, value));
    }
}
