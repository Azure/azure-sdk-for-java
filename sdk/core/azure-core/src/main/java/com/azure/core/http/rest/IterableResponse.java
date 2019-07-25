// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.rest;

import reactor.core.publisher.Flux;

/**
 * This class provides utility to iterate over values.
 * @param <T> value
 */
public class IterableResponse<T> {
    //private final Flux<T> flux;

    /**
     * Creates instance given {@link Flux}.
     * @param flux to iterate over
     */
    public IterableResponse(Flux<T> flux) {
    }

}
