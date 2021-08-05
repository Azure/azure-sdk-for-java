// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.changefeed.implementation;

import com.azure.cosmos.implementation.changefeed.ChangeFeedObserver;
import com.azure.cosmos.implementation.changefeed.ChangeFeedObserverCloseReason;
import com.azure.cosmos.implementation.changefeed.ChangeFeedObserverContext;
import com.azure.cosmos.implementation.changefeed.exceptions.ObserverException;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Exception wrapping decorator implementation for {@link ChangeFeedObserver}.
 */
class ObserverExceptionWrappingChangeFeedObserverDecorator implements ChangeFeedObserver {
    private final Logger logger = LoggerFactory.getLogger(ObserverExceptionWrappingChangeFeedObserverDecorator.class);

    private ChangeFeedObserver changeFeedObserver;

    public ObserverExceptionWrappingChangeFeedObserverDecorator(ChangeFeedObserver changeFeedObserver) {
        this.changeFeedObserver = changeFeedObserver;
    }

    @Override
    public void open(ChangeFeedObserverContext context) {
        try {
            this.changeFeedObserver.open(context);
        }
        catch (RuntimeException userException)
        {
            this.logger.warn("Exception thrown during ChangeFeedObserver.open from thread {}", Thread.currentThread().getId(), userException);
            throw new ObserverException(userException);
        }
    }

    @Override
    public void close(ChangeFeedObserverContext context, ChangeFeedObserverCloseReason reason) {
        try {
            this.changeFeedObserver.close(context, reason);
        }
        catch (RuntimeException userException)
        {
            this.logger.warn("Exception thrown during ChangeFeedObserver.close from thread {}", Thread.currentThread().getId(), userException);
            throw new ObserverException(userException);
        }
    }

    @Override
    public Mono<Void> processChanges(ChangeFeedObserverContext context, List<JsonNode> docs) {
        return this.changeFeedObserver.processChanges(context, docs)
            .doOnError(throwable -> {
                this.logger.warn("Exception thrown during ChangeFeedObserver.processChanges from thread {}", Thread.currentThread().getId(), throwable);
            });
    }
}
