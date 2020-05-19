// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.avro.jackson;

import com.azure.core.serializer.AvroSerializer;
import com.azure.core.util.logging.ClientLogger;
import com.fasterxml.jackson.dataformat.avro.AvroMapper;
import com.fasterxml.jackson.dataformat.avro.AvroSchema;
import reactor.core.Exceptions;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Objects;

/**
 * Jackson based implementation of the {@link AvroSerializer} interface.
 */
public final class JacksonAvroSerializer implements AvroSerializer {
    private final ClientLogger logger = new ClientLogger(JacksonAvroSerializer.class);

    private final AvroMapper avroMapper;

    /**
     * Constructs a {@link AvroSerializer} using the passed Jackson serializer.
     *
     * @param avroMapper Configured Jackson serializer.
     */
    JacksonAvroSerializer(AvroMapper avroMapper) {
        this.avroMapper = avroMapper;
    }

    @Override
    public <T> T read(byte[] input, String schema) {
        Objects.requireNonNull(schema, "'schema' cannot be null.");

        return readInternal(input, schema, schema);
    }

    @Override
    public <T> T read(byte[] input, String readerSchema, String writerSchema) {
        Objects.requireNonNull(readerSchema, "'readerSchema' cannot be null.");
        Objects.requireNonNull(writerSchema, "'writerSchema' cannot be null.");

        return readInternal(input, readerSchema, writerSchema);
    }

    private <T> T readInternal(byte[] input, String readerSchema, String writerSchema) {
        if (input == null) {
            return null;
        }

        try {
            AvroSchema avroSchema = avroMapper.schemaFrom(writerSchema);
            AvroSchema avroReaderSchema = avroMapper.schemaFrom(readerSchema);

            avroSchema = avroSchema.withReaderSchema(avroReaderSchema);

            if ("null".equalsIgnoreCase(avroReaderSchema.getAvroSchema().getType().getName())) {
                return null;
            }

            return avroMapper.readerFor(getReaderClass(avroReaderSchema.getAvroSchema().getFullName()))
                .with(avroSchema)
                .readValue(input);
        } catch (IOException ex) {
            throw logger.logExceptionAsError(Exceptions.propagate(ex));
        }
    }

    @Override
    public byte[] write(Object value, String schema) {
        Objects.requireNonNull(schema, "'schema' cannot be null.");

        try {
            return avroMapper.writer().with(avroMapper.schemaFrom(schema)).writeValueAsBytes(value);
        } catch (IOException ex) {
            throw logger.logExceptionAsError(Exceptions.propagate(ex));
        }
    }

    private static Class<?> getReaderClass(String typeFullName) {
        return typeFullName.equalsIgnoreCase("bytes") ? ByteBuffer.class : Object.class;
    }
}
