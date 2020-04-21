// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.json.jackson;

import com.azure.core.serializer.JsonSerializer;
import com.azure.core.util.logging.ClientLogger;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.Objects;

/**
 * Jackson based implementation of the {@link JsonSerializer} interface.
 */
public final class JacksonJsonSerializer implements JsonSerializer {
    private final ClientLogger logger = new ClientLogger(JacksonJsonSerializer.class);

    private final ObjectMapper mapper;

    /**
     * Constructs a {@link JsonSerializer} using the passed Jackson serializer.
     *
     * @param mapper Configured Jackson serializer.
     */
    JacksonJsonSerializer(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public <T> T read(byte[] input, Class<T> clazz) {
        if (input == null) {
            return null;
        }

        try {
            return mapper.readValue(input, clazz);
        } catch (IOException ex) {
            throw logger.logExceptionAsError(new UncheckedIOException(ex));
        }
    }

    @Override
    public byte[] write(Object value) {
        try {
            return mapper.writeValueAsBytes(value);
        } catch (IOException ex) {
            throw logger.logExceptionAsError(new UncheckedIOException(ex));
        }
    }

    @Override
    public void write(Object value, OutputStream stream) {
        Objects.requireNonNull(stream, "'stream' cannot be null.");

        try {
            mapper.writeValue(stream, value);
        } catch (IOException ex) {
            throw logger.logExceptionAsError(new UncheckedIOException(ex));
        }
    }
}
