// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.caches;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.concurrent.Callable;

class AsyncLazy<TValue> {

    private final static Logger logger = LoggerFactory.getLogger(AsyncLazy.class);

    private final Mono<TValue> single;

    private volatile TValue value;
    private volatile boolean failed;

    public AsyncLazy(Callable<Mono<TValue>> func) {
        this(Mono.defer(() -> {
            logger.debug("using Function<Mono<TValue>> {}", func);
            try {
                return func.call();
            } catch (Exception e) {
                return Mono.error(e);
            }
        }));
    }

    public AsyncLazy(TValue value) {
        this.single = Mono.just(value);
        this.value = value;
        this.failed = false;
    }

    private AsyncLazy(Mono<TValue> single) {
        logger.debug("constructor");
        this.single = single
                .doOnSuccess(v -> this.value = v)
                .doOnError(e -> this.failed = true)
                .cache();
    }

    public Mono<TValue> single() {
        return single;
    }

    public boolean isSucceeded() {
        return value != null;
    }

    public Optional<TValue> tryGet() {
        TValue result = this.value;
        if (result == null) {
            return Optional.empty();
        } else {
            return  Optional.of(result);
        }
    }

    public boolean isFaulted() {
        return failed;
    }
}
