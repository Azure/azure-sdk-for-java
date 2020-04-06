// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer;

import reactor.core.publisher.Mono;

import java.io.OutputStream;

public interface AvroSerializer<SCHEMA> {
    <T> T read(byte[] input, SCHEMA schema);
    <T> Mono<T> readAsync(byte[] input, SCHEMA schema);

    byte[] write(Object value, SCHEMA schema);
    Mono<byte[]> writeAsync(Object value, SCHEMA schema);

    void write(Object value, SCHEMA schema, OutputStream stream);
    Mono<Void> writeAsync(Object value, SCHEMA schema, OutputStream stream);
}
