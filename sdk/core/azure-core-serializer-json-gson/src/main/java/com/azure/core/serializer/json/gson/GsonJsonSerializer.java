// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.json.gson;

import com.azure.core.serializer.JsonSerializer;
import com.google.gson.Gson;
import reactor.core.publisher.Mono;

import java.io.Reader;
import java.io.Writer;

public final class GsonJsonSerializer implements JsonSerializer {
    public final Gson gson;

    public GsonJsonSerializer(Gson gson) {
        this.gson = gson;
    }

    @Override
    public <T> T read(String input, Class<T> clazz) {
        return gson.fromJson(input, clazz);
    }

    @Override
    public <T> T read(Reader reader, Class<T> clazz) {
        return gson.fromJson(reader, clazz);
    }

    @Override
    public <T> Mono<T> readAsync(String input, Class<T> clazz) {
        return Mono.fromCallable(() -> read(input, clazz));
    }

    @Override
    public <T> Mono<T> readAsync(Reader reader, Class<T> clazz) {
        return Mono.fromCallable(() -> read(reader, clazz));
    }

    @Override
    public String write(Object value) {
        return gson.toJson(value);
    }

    @Override
    public String write(Object value, Class<?> clazz) {
        return gson.toJson(value, clazz);
    }

    @Override
    public Mono<String> writeAsync(Object value) {
        return Mono.fromCallable(() -> write(value));
    }

    @Override
    public Mono<String> writeAsync(Object value, Class<?> clazz) {
        return Mono.fromCallable(() -> write(value, clazz));
    }

    @Override
    public void write(Object value, Writer writer) {
        gson.toJson(value, writer);
    }

    @Override
    public void write(Object value, Writer writer, Class<?> clazz) {
        gson.toJson(value, clazz, writer);
    }

    @Override
    public Mono<Void> writeAsync(Object value, Writer writer) {
        return Mono.fromRunnable(() -> write(value, writer));
    }

    @Override
    public Mono<Void> writeAsync(Object value, Writer writer, Class<?> clazz) {
        return Mono.fromRunnable(() -> write(value, writer, clazz));
    }
}
