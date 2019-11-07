// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.internal.changefeed.implementation;

import com.azure.data.cosmos.CosmosItemProperties;
import com.azure.data.cosmos.internal.changefeed.ChangeFeedObserver;
import com.azure.data.cosmos.internal.changefeed.ChangeFeedObserverCloseReason;
import com.azure.data.cosmos.internal.changefeed.ChangeFeedObserverContext;
import com.azure.data.cosmos.internal.changefeed.exceptions.ObserverException;
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
    public Mono<Void> processChanges(ChangeFeedObserverContext context, List<CosmosItemProperties> docs) {
        return this.changeFeedObserver.processChanges(context, docs)
            .doOnError(throwable -> {
                this.logger.warn("Exception thrown during ChangeFeedObserver.processChanges from thread {}", Thread.currentThread().getId(), throwable);
            });
    }
}
