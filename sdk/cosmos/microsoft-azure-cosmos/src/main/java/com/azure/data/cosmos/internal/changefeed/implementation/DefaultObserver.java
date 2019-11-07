// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.internal.changefeed.implementation;

import com.azure.data.cosmos.CosmosItemProperties;
import com.azure.data.cosmos.internal.changefeed.ChangeFeedObserver;
import com.azure.data.cosmos.internal.changefeed.ChangeFeedObserverCloseReason;
import com.azure.data.cosmos.internal.changefeed.ChangeFeedObserverContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Consumer;

class DefaultObserver implements ChangeFeedObserver {
    private static final Logger log = LoggerFactory.getLogger(DefaultObserver.class);
    private final Consumer<List<CosmosItemProperties>> consumer;

    public DefaultObserver(Consumer<List<CosmosItemProperties>> consumer) {
        this.consumer = consumer;
    }

    @Override
    public void open(ChangeFeedObserverContext context) {
        log.info("Open processing from thread {}", Thread.currentThread().getId());
    }

    @Override
    public void close(ChangeFeedObserverContext context, ChangeFeedObserverCloseReason reason) {
        log.info("Close processing from thread {}", Thread.currentThread().getId());
    }

    @Override
    public Mono<Void> processChanges(ChangeFeedObserverContext context, List<CosmosItemProperties> docs) {
        log.info("Start processing from thread {}", Thread.currentThread().getId());
        try {
            consumer.accept(docs);
            log.info("Done processing from thread {}", Thread.currentThread().getId());
        } catch (Exception ex) {
            log.warn("Unexpected exception thrown from thread {}", Thread.currentThread().getId(), ex);
            return Mono.error(ex);
        }

        return Mono.empty();
    }
}
