// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.avro.jackson;

import com.azure.core.experimental.serializer.ObjectSerializer;
import com.azure.core.experimental.serializer.TypeReference;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.dataformat.avro.AvroMapper;
import com.fasterxml.jackson.dataformat.avro.AvroSchema;
import reactor.core.publisher.Mono;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Jackson Avro based implementation of the {@link ObjectSerializer} interface.
 */
public final class JacksonAvroSerializer implements ObjectSerializer {
    private final AvroSchema avroSchema;
    private final AvroMapper avroMapper;
    private final TypeFactory typeFactory;

    JacksonAvroSerializer(AvroSchema avroSchema, AvroMapper avroMapper) {
        this.avroSchema = avroSchema;
        this.avroMapper = avroMapper;
        this.typeFactory = avroMapper.getTypeFactory();
    }

    @Override
    public <T> Mono<T> deserialize(InputStream stream, TypeReference<T> typeReference) {
        return Mono.fromCallable(() -> {
            if (stream == null) {
                return null;
            }

            if ("null".equalsIgnoreCase(avroSchema.getAvroSchema().getType().getName())) {
                return null;
            }

            return avroMapper.readerFor(typeFactory.constructType(typeReference.getJavaType()))
                .with(avroSchema)
                .readValue(stream);
        });
    }

    @Override
    public <S extends OutputStream> Mono<S> serialize(S stream, Object value) {
        return Mono.fromCallable(() -> {
            avroMapper.writer().with(avroSchema).writeValue(stream, value);

            return stream;
        });
    }
}
