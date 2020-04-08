// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.json.jackson;

import com.azure.core.serializer.JsonSerializer;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.fasterxml.jackson.databind.ObjectMapper;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.OutputStream;

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
    public <T> Mono<T> read(String input, Class<T> clazz) {
        return Mono.defer(() -> {
            try {
                return Mono.just(mapper.readValue(input, clazz));
            } catch (IOException ex) {
                return FluxUtil.throwableMonoError(logger, ex);
            }
        });
    }

    @Override
    public Mono<String> write(Object value) {
        return Mono.defer(() -> {
            try {
                return Mono.just(mapper.writeValueAsString(value));
            } catch (IOException ex) {
                return FluxUtil.throwableMonoError(logger, ex);
            }
        });
    }

    @Override
    public Mono<String> write(Object value, Class<?> clazz) {
        return Mono.defer(() -> {
            try {
                return Mono.just(mapper.writerFor(clazz).writeValueAsString(value));
            } catch (IOException ex) {
                return FluxUtil.throwableMonoError(logger, ex);
            }
        });
    }

    @Override
    public Mono<Void> write(Object value, OutputStream stream) {
        return Mono.fromRunnable(() -> {
            try {
                mapper.writeValue(stream, value);
            } catch (IOException ex) {
                throw logger.logExceptionAsError(Exceptions.propagate(ex));
            }
        });
    }

    @Override
    public Mono<Void> write(Object value, OutputStream stream, Class<?> clazz) {
        return Mono.defer(() -> Mono.fromRunnable(() -> {
            try {
                mapper.writerFor(clazz).writeValue(stream, value);
            } catch (IOException ex) {
                throw logger.logExceptionAsError(Exceptions.propagate(ex));
            }
        }));
    }
}
