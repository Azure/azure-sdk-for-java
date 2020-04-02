// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.avro.jackson;

import com.azure.core.serializer.AvroSerializer;
import reactor.core.publisher.Mono;

import java.io.Writer;

public final class JacksonAvroSerializer implements AvroSerializer {
    @Override
    public <T> T read(byte[] input, String schema) {
        return null;
    }

    @Override
    public <T> T read(byte[] input, String schema, Class<T> clazz) {
        return null;
    }

    @Override
    public <T> Mono<T> readAsync(byte[] input, String schema) {
        return null;
    }

    @Override
    public <T> Mono<T> readAsync(byte[] input, String schema, Class<T> clazz) {
        return null;
    }

    @Override
    public byte[] write(Object value, String schema) {
        return new byte[0];
    }

    @Override
    public byte[] write(Object value, String schema, Class<?> clazz) {
        return new byte[0];
    }

    @Override
    public Mono<byte[]> writeAsync(Object value, String schema) {
        return null;
    }

    @Override
    public Mono<byte[]> writeAsync(Object value, String schema, Class<?> clazz) {
        return null;
    }

    @Override
    public void write(Object value, String schema, Writer writer) {

    }

    @Override
    public void write(Object value, String schema, Writer writer, Class<?> clazz) {

    }

    @Override
    public Mono<Void> writeAsync(Object value, String schema, Writer writer) {
        return null;
    }

    @Override
    public Mono<Void> writeAsync(Object value, String schema, Writer writer, Class<?> clazz) {
        return null;
    }
}
