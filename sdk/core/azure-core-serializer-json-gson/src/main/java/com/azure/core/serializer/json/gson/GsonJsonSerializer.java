// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.json.gson;

import com.azure.core.serializer.JsonSerializer;
import com.google.gson.Gson;
import reactor.core.publisher.Mono;

import java.io.OutputStream;
import java.io.OutputStreamWriter;

/**
 * GSON based implementation of the {@link JsonSerializer} interface.
 */
public final class GsonJsonSerializer implements JsonSerializer {
    public final Gson gson;

    /**
     * Constructs a {@link JsonSerializer} using the passed {@link Gson} serializer.
     *
     * @param gson Configured {@link Gson} serializer.
     */
    public GsonJsonSerializer(Gson gson) {
        this.gson = gson;
    }

    @Override
    public <T> Mono<T> read(String input, Class<T> clazz) {
        return Mono.defer(() -> Mono.just(gson.fromJson(input, clazz)));
    }

    @Override
    public Mono<String> write(Object value) {
        return Mono.defer(() -> Mono.just(gson.toJson(value)));
    }

    @Override
    public Mono<String> write(Object value, Class<?> clazz) {
        return Mono.defer(() -> Mono.just(gson.toJson(value, clazz)));
    }

    @Override
    public Mono<Void> write(Object value, OutputStream stream) {
        return Mono.defer(() -> Mono.fromRunnable(() -> gson.toJson(value, new OutputStreamWriter(stream))));
    }

    @Override
    public Mono<Void> write(Object value, OutputStream stream, Class<?> clazz) {
        return Mono.defer(() -> Mono.fromRunnable(() -> gson.toJson(value, clazz, new OutputStreamWriter(stream))));
    }
}
