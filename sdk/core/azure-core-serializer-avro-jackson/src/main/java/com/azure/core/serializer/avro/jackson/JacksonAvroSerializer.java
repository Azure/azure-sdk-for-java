// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.avro.jackson;

import com.azure.core.serializer.AvroSerializer;
import com.azure.core.util.logging.ClientLogger;
import com.fasterxml.jackson.dataformat.avro.AvroMapper;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

/**
 * Jackson based implementation of the {@link AvroSerializer} interface.
 */
public final class JacksonAvroSerializer implements AvroSerializer {
    private final ClientLogger logger = new ClientLogger(JacksonAvroSerializer.class);

    private final AvroMapper mapper;

    /**
     * Constructs a {@link AvroSerializer} using the passed Jackson serializer.
     *
     * @param mapper Configured Jackson serializer.
     */
    public JacksonAvroSerializer(AvroMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public <T> Mono<T> read(byte[] input, String schema) {
        return Mono.fromCallable(() -> {
            if (input == null) {
                return null;
            }

            try {
                return mapper.reader().with(mapper.schemaFrom(schema)).readValue(input);
            } catch (IOException ex) {
                throw logger.logExceptionAsError(Exceptions.propagate(ex));
            }
        });
    }

    @Override
    public Mono<byte[]> write(Object value, String schema) {
        return Mono.fromCallable(() -> {
            try {
                return mapper.writer().with(mapper.schemaFrom(schema)).writeValueAsBytes(value);
            } catch (IOException ex) {
                throw logger.logExceptionAsError(Exceptions.propagate(ex));
            }
        });
    }

    @Override
    public Mono<Void> write(Object value, String schema, OutputStream stream) {
        Objects.requireNonNull(stream, "'stream' cannot be null.");

        return Mono.fromRunnable(() -> {
            try {
                mapper.writer().with(mapper.schemaFrom(schema)).writeValue(stream, value);
            } catch (IOException ex) {
                throw logger.logExceptionAsError(Exceptions.propagate(ex));
            }
        });
    }
}