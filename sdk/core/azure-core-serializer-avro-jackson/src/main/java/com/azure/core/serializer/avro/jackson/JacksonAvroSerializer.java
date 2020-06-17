// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.avro.jackson;

import com.azure.core.serializer.ObjectSerializer;
import com.fasterxml.jackson.dataformat.avro.AvroMapper;
import com.fasterxml.jackson.dataformat.avro.AvroSchema;
import reactor.core.publisher.Mono;

/**
 * Jackson Avro based implementation of the {@link ObjectSerializer} interface.
 */
public final class JacksonAvroSerializer implements ObjectSerializer {
    private final AvroSchema avroSchema;
    private final AvroMapper avroMapper;

    JacksonAvroSerializer(AvroSchema avroSchema, AvroMapper avroMapper) {
        this.avroSchema = avroSchema;
        this.avroMapper = avroMapper;
    }

    @Override
    public <T> Mono<T> deserialize(byte[] input, Class<T> clazz) {
        return Mono.fromCallable(() -> {
            if (input == null) {
                return null;
            }

            if ("null".equalsIgnoreCase(avroSchema.getAvroSchema().getType().getName())) {
                return null;
            }

            return avroMapper.readerFor(clazz).with(avroSchema).readValue(input);
        });
    }

    @Override
    public Mono<byte[]> serialize(Object value) {
        return Mono.fromCallable(() -> avroMapper.writer().with(avroSchema).writeValueAsBytes(value));
    }
}
