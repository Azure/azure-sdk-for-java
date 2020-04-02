// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer;

import reactor.core.publisher.Mono;

import java.io.Writer;

public interface AvroSerializer {
    <T> T read(byte[] input, String schema);
    <T> T read(byte[] input, String schema, Class<T> clazz);

    <T> Mono<T> readAsync(byte[] input, String schema);
    <T> Mono<T> readAsync(byte[] input, String schema, Class<T> clazz);

    byte[] write(Object value, String schema);
    byte[] write(Object value, String schema, Class<?> clazz);

    Mono<byte[]> writeAsync(Object value, String schema);
    Mono<byte[]> writeAsync(Object value, String schema, Class<?> clazz);

    void write(Object value, String schema, Writer writer);
    void write(Object value, String schema, Writer writer, Class<?> clazz);

    Mono<Void> writeAsync(Object value, String schema, Writer writer);
    Mono<Void> writeAsync(Object value, String schema, Writer writer, Class<?> clazz);
}
