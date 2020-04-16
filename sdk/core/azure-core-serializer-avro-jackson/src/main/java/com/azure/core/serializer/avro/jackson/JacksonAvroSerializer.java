// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.avro.jackson;

import com.azure.core.serializer.AvroSerializer;
import com.azure.core.util.logging.ClientLogger;
import com.fasterxml.jackson.dataformat.avro.AvroMapper;
import reactor.core.Exceptions;

import java.io.IOException;
import java.io.OutputStream;
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
            return avroMapper.reader().with(avroMapper.schemaFrom(schema)).readValue(input);
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

    @Override
    public void write(Object value, String schema, OutputStream stream) {
        Objects.requireNonNull(schema, "'schema' cannot be null.");
        Objects.requireNonNull(stream, "'stream' cannot be null.");

        try {
            avroMapper.writer().with(avroMapper.schemaFrom(schema)).writeValue(stream, value);
        } catch (IOException ex) {
            throw logger.logExceptionAsError(Exceptions.propagate(ex));
        }
    }
}
