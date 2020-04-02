// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer;

import reactor.core.publisher.Mono;

import java.io.Reader;
import java.io.Writer;

public interface JsonSerializer {
    <T> T read(String input, Class<T> clazz);
    <T> T read(Reader reader, Class<T> clazz);

    <T> Mono<T> readAsync(String input, Class<T> clazz);
    <T> Mono<T> readAsync(Reader reader, Class<T> clazz);

    String write(Object value);
    String write(Object value, Class<?> clazz);

    Mono<String> writeAsync(Object value);
    Mono<String> writeAsync(Object value, Class<?> clazz);

    void write(Object value, Writer writer);
    void write(Object value, Writer writer, Class<?> clazz);

    Mono<Void> writeAsync(Object value, Writer writer);
    Mono<Void> writeAsync(Object value, Writer writer, Class<?> clazz);
}
