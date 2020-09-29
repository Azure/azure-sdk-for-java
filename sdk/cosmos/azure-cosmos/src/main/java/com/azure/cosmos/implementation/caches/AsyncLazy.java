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
    private final Optional<TValue> oldValue;

    private final Mono<TValue> single;

    private volatile boolean succeeded;
    private volatile boolean failed;

    public AsyncLazy(Callable<Mono<TValue>> func) {
        this(func, Optional.empty());
    }

    public AsyncLazy(Callable<Mono<TValue>> func, Optional<TValue> oldValue) {
        this(Mono.defer(() -> {
            logger.debug("using Function<Mono<TValue>> {}", func);
            try {
                return func.call();
            } catch (Exception e) {
                return Mono.error(e);
            }
        }), oldValue);
    }

    public AsyncLazy(TValue value) {
        this.single = Mono.just(value);
        this.succeeded = true;
        this.failed = false;
        this.oldValue = Optional.empty();
    }

    private AsyncLazy(Mono<TValue> single, Optional<TValue> oldValue) {
        logger.debug("constructor");
        this.single = single
                .doOnSuccess(v -> this.succeeded = true)
                .doOnError(e -> this.failed = true)
                .cache();
        this.oldValue = oldValue;
    }

    public Mono<TValue> single() {
        if (succeeded || !oldValue.isPresent()) {
            return single;
        }

        assert oldValue.isPresent();

        return single.onErrorReturn(oldValue.get());
    }

    public boolean isSucceeded() {
        return succeeded;
    }

    public boolean isFaulted() {
        return failed;
    }

    public Optional<TValue> getOldValue() {
        return oldValue;
    }
}
