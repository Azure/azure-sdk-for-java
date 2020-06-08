// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation;

import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.publisher.ReplayProcessor;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

/**
 * Synchronizes reactor threads accessing/instantiating a common value {@code T}.
 *
 * @param <T> The value being instantiated / accessed.
 */
public class SynchronizedAccessor<T> {
    private final AtomicBoolean wip;
    private volatile T cache;
    private final ReplayProcessor<T> replayProcessor = ReplayProcessor.create(1);
    private final FluxSink<T> sink = replayProcessor.sink(FluxSink.OverflowStrategy.BUFFER);
    private final Supplier<T> supplier;

    public SynchronizedAccessor(Supplier<T> supplier) {
        this.wip = new AtomicBoolean(false);
        this.supplier = supplier;
    }

    /**
     * Get the value from the configured supplier.
     *
     * @return the output {@code T}
     */
    public Mono<T> getValue() {
        return Mono.defer(() -> {
            if (cache != null) {
                return Mono.just(cache);
            }
            if (!wip.getAndSet(true)) {
                try {
                    cache = supplier.get();
                    sink.next(cache);
                } catch (Exception e) {
                    sink.error(e);
                }
            }
            return replayProcessor.next();
        });
    }
}
