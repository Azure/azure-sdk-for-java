// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.json.gson;

import com.azure.core.serializer.JsonSerializer;
import com.azure.core.util.CoreUtils;
import com.google.gson.Gson;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * GSON based implementation of the {@link JsonSerializer} interface.
 */
public final class GsonJsonSerializer implements JsonSerializer {
    private final Gson gson;

    /**
     * Constructs a {@link JsonSerializer} using the passed {@link Gson} serializer.
     *
     * @param gson Configured {@link Gson} serializer.
     */
    GsonJsonSerializer(Gson gson) {
        this.gson = gson;
    }

    @Override
    public <T> Mono<T> read(byte[] input, Class<T> clazz) {
        Reader reader = new Reader() {
            @Override
            public int read(char[] cbuf, int off, int len) throws IOException {
                return 0;
            }

            @Override
            public void close() throws IOException {

            }
        }
        return Mono.fromCallable(() -> gson.fromJson(new String(input, StandardCharsets.UTF_8), clazz));
    }

    @Override
    public Mono<byte[]> write(Object value) {
        return Mono.fromCallable(() -> gson.toJson(value));
    }

    @Override
    public Mono<byte[]> write(Object value, Class<?> clazz) {
        return Mono.fromCallable(() -> gson.toJson(value, clazz));
    }

    @Override
    public Mono<Void> write(Object value, OutputStream stream) {
        Objects.requireNonNull(stream, "'stream' cannot be null.");

        return Mono.fromRunnable(() -> gson.toJson(value, new OutputStreamWriter(stream)));
    }

    @Override
    public Mono<Void> write(Object value, OutputStream stream, Class<?> clazz) {
        Objects.requireNonNull(stream, "'stream' cannot be null.");

        return Mono.fromRunnable(() -> gson.toJson(value, clazz, new OutputStreamWriter(stream)));
    }
}
