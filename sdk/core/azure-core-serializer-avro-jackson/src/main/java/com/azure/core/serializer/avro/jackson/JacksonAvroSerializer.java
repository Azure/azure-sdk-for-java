// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.avro.jackson;

import com.azure.core.serializer.AvroSerializer;
import com.fasterxml.jackson.dataformat.avro.AvroMapper;
import com.fasterxml.jackson.dataformat.avro.AvroSchema;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.OutputStream;

public final class JacksonAvroSerializer implements AvroSerializer<AvroSchema> {
    private final AvroMapper mapper;

    public JacksonAvroSerializer() {
        this.mapper = new AvroMapper();
    }

    public JacksonAvroSerializer(AvroMapper mapper) {
        this.mapper = mapper.copy();
    }

    @Override
    public <T> T read(byte[] input, AvroSchema schema) {
        try {
            return mapper.reader()
                .with(schema)
                .readValue(input);
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public <T> Mono<T> readAsync(byte[] input, AvroSchema schema) {
        return Mono.fromCallable(() -> read(input, schema));
    }

    @Override
    public byte[] write(Object value, AvroSchema schema) {
        try {
            return mapper.writer()
                .with(schema)
                .writeValueAsBytes(value);
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public Mono<byte[]> writeAsync(Object value, AvroSchema schema) {
        return Mono.fromCallable(() -> write(value, schema));
    }

    @Override
    public void write(Object value, AvroSchema schema, OutputStream stream) {
        try {
            mapper.writer()
                .with(schema)
                .writeValue(stream, value);
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public Mono<Void> writeAsync(Object value, AvroSchema schema, OutputStream stream) {
        return Mono.fromRunnable(() -> write(value, schema, stream));
    }
}
