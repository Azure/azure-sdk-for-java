// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.avro.jackson;

import com.azure.core.serializer.SchemaSerializer;
import com.fasterxml.jackson.dataformat.avro.AvroMapper;
import com.fasterxml.jackson.dataformat.avro.AvroSchema;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.util.Objects;

/**
 * Jackson Avro based implementation of the {@link SchemaSerializer} interface.
 */
public final class JacksonAvroSerializer implements SchemaSerializer {
    private final AvroMapper avroMapper;

    JacksonAvroSerializer(AvroMapper avroMapper) {
        this.avroMapper = avroMapper;
    }

    @Override
    public <T> Mono<T> deserialize(byte[] input, String schema) {
        return Mono.fromCallable(() -> {
            Objects.requireNonNull(schema, "'schema' cannot be null.");

            if (input == null) {
                return null;
            }

            AvroSchema avroSchema = avroMapper.schemaFrom(schema);
            if ("null".equalsIgnoreCase(avroSchema.getAvroSchema().getType().getName())) {
                return null;
            }

            return avroMapper.readerFor(getReaderClass(avroSchema.getAvroSchema().getFullName()))
                .with(avroSchema)
                .readValue(input);
        });
    }

    @Override
    public Mono<byte[]> serialize(Object value, String schema) {
        return Mono.fromCallable(() -> {
            Objects.requireNonNull(schema, "'schema' cannot be null.");

            return avroMapper.writer().with(avroMapper.schemaFrom(schema)).writeValueAsBytes(value);
        });
    }

    private static Class<?> getReaderClass(String typeFullName) {
        return typeFullName.equalsIgnoreCase("bytes") ? ByteBuffer.class : Object.class;
    }
}
