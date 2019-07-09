// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class ResponseHolder<T> {
    private Flux<T> collection;
    private Mono<T> single;

    public Mono<T> single() {
        return this.single;
    }

    public Flux<T> collection() {
        return this.collection;
    }

    void single(Mono<T> single) {
        this.single = single;
    }

    void collection(Flux<T> collection) {
        this.collection = collection;
    }
}
