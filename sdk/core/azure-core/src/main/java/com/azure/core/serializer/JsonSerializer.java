// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer;

import reactor.core.publisher.Mono;

import java.io.OutputStream;

public interface JsonSerializer {
    <T> Mono<T> read(String input, Class<T> clazz);

    Mono<String> write(Object value);
    Mono<String> write(Object value, Class<?> clazz);

    Mono<Void> write(Object value, OutputStream stream);
    Mono<Void> write(Object value, OutputStream stream, Class<?> clazz);
}
