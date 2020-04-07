// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.avro.jackson;

import com.azure.core.serializer.AvroSerializer;
import com.fasterxml.jackson.dataformat.avro.AvroMapper;
import com.fasterxml.jackson.dataformat.avro.AvroSchema;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Jackson based implementation of the {@link AvroSerializer} interface.
 */
public final class JacksonAvroSerializer implements AvroSerializer<AvroSchema> {
    private final AvroMapper mapper;

    /**
     * Constructs a {@link AvroSerializer} using the default Jackson serializer.
     */
    public JacksonAvroSerializer() {
        this.mapper = new AvroMapper();
    }

    /**
     * Constructs a {@link AvroSerializer} using the passed Jackson serializer.
     *
     * @param mapper Configured Jackson serializer.
     */
    public JacksonAvroSerializer(AvroMapper mapper) {
        this.mapper = mapper.copy();
    }

    @Override
    public <T> Mono<T> read(byte[] input, AvroSchema schema) {
        return Mono.defer(() -> {
            try {
                return Mono.just(mapper.reader().with(schema).readValue(input));
            } catch (IOException ex) {
                return Mono.error(ex);
            }
        });
    }

    @Override
    public Mono<byte[]> write(Object value, AvroSchema schema) {
        return Mono.defer(() -> {
            try {
                return Mono.just(mapper.writer().with(schema).writeValueAsBytes(value));
            } catch (IOException ex) {
                return Mono.error(ex);
            }
        });
    }

    @Override
    public Mono<Void> write(Object value, AvroSchema schema, OutputStream stream) {
        return Mono.defer(() -> Mono.fromRunnable(() -> {
            try {
                mapper.writer().with(schema).writeValue(stream, value);
            } catch (IOException ex) {
                throw Exceptions.propagate(ex);
            }
        }));
    }
}
