// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.changefeed.common;

import com.azure.cosmos.implementation.changefeed.ChangeFeedObserver;
import com.azure.cosmos.implementation.changefeed.ChangeFeedObserverCloseReason;
import com.azure.cosmos.implementation.changefeed.ChangeFeedObserverContext;
import com.azure.cosmos.ChangeFeedProcessorContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class DefaultObserver<T> implements ChangeFeedObserver<T> {
    private static final Logger log = LoggerFactory.getLogger(DefaultObserver.class);
    private final Consumer<List<T>> consumer;
    private final BiConsumer<List<T>, ChangeFeedProcessorContext> biConsumer;

    public DefaultObserver(Consumer<List<T>> consumer) {
        this.consumer = consumer;
        this.biConsumer = null;
    }

    public DefaultObserver(BiConsumer<List<T>, ChangeFeedProcessorContext> biConsumer) {
        this.biConsumer = biConsumer;
        this.consumer = null;
    }

    @Override
    public void open(ChangeFeedObserverContext<T> context) {
        log.info("Open processing from thread {}", Thread.currentThread().getId());
    }

    @Override
    public void close(ChangeFeedObserverContext<T> context, ChangeFeedObserverCloseReason reason) {
        log.info("Close processing from thread {}", Thread.currentThread().getId());
    }

    @Override
    public Mono<Void> processChanges(ChangeFeedObserverContext<T> context, List<T> docs) {
        log.info("Start processing from thread {}", Thread.currentThread().getId());
        try {

            if (consumer != null) {
                consumer.accept(docs);
            } else if (biConsumer != null) {
                biConsumer.accept(docs, new ChangeFeedProcessorContextImpl<>(context));
            }

            log.info("Done processing from thread {}", Thread.currentThread().getId());
        } catch (Exception ex) {
            log.warn("Unexpected exception thrown from thread {}", Thread.currentThread().getId(), ex);
            return Mono.error(ex);
        }
        return Mono.empty();
    }
}
