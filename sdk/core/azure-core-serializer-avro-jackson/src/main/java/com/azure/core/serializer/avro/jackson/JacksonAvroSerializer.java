// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.avro.jackson;

import com.azure.core.serializer.AvroSerializer;
import com.azure.core.util.logging.ClientLogger;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.avro.AvroMapper;
import com.fasterxml.jackson.dataformat.avro.AvroSchema;
import reactor.core.Exceptions;

import java.io.IOException;
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

        if (input == null) {
            return null;
        }

        try {
            AvroSchema jacksonAvroSchema = avroMapper.schemaFrom(schema);

            if ("null".equalsIgnoreCase(jacksonAvroSchema.getAvroSchema().getType().getName())) {
                return null;
            }

            ObjectReader reader = avroMapper.readerFor(getReaderClass(jacksonAvroSchema.getAvroSchema().getFullName()));
            return reader.with(jacksonAvroSchema).readValue(input);
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
        switch (typeFullName) {
            case "null":
                return void.class;
            case "boolean":
                return boolean.class;
            case "int":
                return int.class;
            case "long":
                return long.class;
            case "float":
                return float.class;
            case "double":
                return double.class;
            case "string":
                return String.class;
            case "bytes":
                return byte[].class;
            default:
                try {
                    return Class.forName(typeFullName);
                } catch (ClassNotFoundException ex) {
                    return Object.class;
                }
        }
    }
}
