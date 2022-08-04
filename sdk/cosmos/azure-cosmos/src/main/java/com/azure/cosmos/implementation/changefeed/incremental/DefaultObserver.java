// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.changefeed.incremental;

import com.azure.cosmos.implementation.changefeed.ChangeFeedObserver;
import com.azure.cosmos.implementation.changefeed.ChangeFeedObserverCloseReason;
import com.azure.cosmos.implementation.changefeed.ChangeFeedObserverContext;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Consumer;

class DefaultObserver implements ChangeFeedObserver {
    private static final Logger log = LoggerFactory.getLogger(DefaultObserver.class);
    private final Consumer<List<JsonNode>> consumer;

    public DefaultObserver(Consumer<List<JsonNode>> consumer) {
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
    public Mono<Void> processChanges(ChangeFeedObserverContext context, List<JsonNode> docs) {
        log.info("Start processing from thread {}", Thread.currentThread().getId());
        try {
            //TODO for later: convert to user T here unless T is JsonNode when we want to add additional support to
            // user types.
            consumer.accept(docs);
            log.info("Done processing from thread {}", Thread.currentThread().getId());
        } catch (Exception ex) {
            log.warn("Unexpected exception thrown from thread {}", Thread.currentThread().getId(), ex);
            return Mono.error(ex);
        }
        return Mono.empty();
    }


}
