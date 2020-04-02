// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.json.jackson;

import com.azure.core.serializer.JsonSerializer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.io.Writer;

public final class JacksonJsonSerializer implements JsonSerializer {
    private final ObjectMapper mapper;

    public JacksonJsonSerializer(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public <T> T read(String input, Class<T> clazz) {
        try {
            return mapper.readValue(input, clazz);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public <T> T read(Reader reader, Class<T> clazz) {
        try {
            return mapper.readValue(reader, clazz);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
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
        try {
            return mapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public String write(Object value, Class<?> clazz) {
        try {
            return mapper.writerFor(clazz).writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
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
        try {
            mapper.writeValue(writer, value);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void write(Object value, Writer writer, Class<?> clazz) {
        try {
            mapper.writerFor(clazz).writeValue(writer, value);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
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
