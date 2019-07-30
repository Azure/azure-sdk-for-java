// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.implementation;

import com.azure.core.amqp.RetryPolicy;
import com.azure.core.util.logging.ClientLogger;
import org.reactivestreams.Publisher;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.concurrent.TimeoutException;

/**
 * Handles {@link TimeoutException TimeoutExceptions} thrown from {@link Publisher Publishers} and applies the retry
 * strategy to them.
 */
class TimeoutHandler {
    private final ClientLogger logger = new ClientLogger(TimeoutHandler.class);
    private final RetryPolicy policy;
    private final int numberOfAttempts;

    TimeoutHandler(RetryPolicy policy) {
        this.policy = policy;
        this.numberOfAttempts = policy.getMaxRetries() + 1;
    }

    /**
     * Given a {@link Flux} of errors, applies the retry strategy when the error is a {@link TimeoutException}.
     *
     * @param errors A stream of errors from the upstream.
     * @return A publisher that completes if the retry attempts are successful. Otherwise, propagates the error.
     */
    Publisher<Void> timeoutHandler(Flux<Throwable> errors) {
        return errors.zipWith(Flux.range(1, numberOfAttempts),
            (error, attempt) -> {
                if (!(error instanceof TimeoutException) || attempt > policy.getMaxRetries()) {
                    throw Exceptions.propagate(error);
                }

                return policy.calculateRetryDelay((TimeoutException) error, attempt);
            })
            .flatMap(Mono::delay)
            .doOnNext(delay -> {
                logger.info("Retried attempt after: {}ms", delay);
            })
            .then();
    }
}
