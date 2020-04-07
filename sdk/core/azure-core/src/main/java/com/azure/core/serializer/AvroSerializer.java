// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer;

import reactor.core.publisher.Mono;

import java.io.OutputStream;

public interface AvroSerializer<SCHEMA> {
    <T> Mono<T> read(byte[] input, SCHEMA schema);

    Mono<byte[]> write(Object value, SCHEMA schema);

    Mono<Void> write(Object value, SCHEMA schema, OutputStream stream);
}
