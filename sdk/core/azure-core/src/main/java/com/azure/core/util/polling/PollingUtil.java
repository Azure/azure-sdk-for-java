// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.polling;

import com.azure.core.util.logging.ClientLogger;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Utility class for Polling APIs.
 */
class PollingUtil {
    private static final ClientLogger LOGGER = new ClientLogger(PollingUtil.class);


    static <T> PollResponse<T> pollingLoop(PollingContext<T> pollingContext, Optional<Duration> timeout,
                                                  Optional<LongRunningOperationStatus> statusToWaitFor,
                                                  Function<PollingContext<T>, PollResponse<T>> pollOperation,
                                                  Duration pollInterval) {
        final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        boolean timeBound = timeout.isPresent();

        long startTime = System.currentTimeMillis();
        pollingContext.setLatestResponse(pollOperation.apply(pollingContext));
        PollResponse<T> intermediatePollResponse = pollingContext.getLatestResponse();
        while (!intermediatePollResponse.getStatus().isComplete()) {
            long elapsedTime = System.currentTimeMillis() - startTime;
            if (timeBound ?  elapsedTime >= timeout.get().toMillis() : false) {
                scheduler.shutdown();
                return intermediatePollResponse;
            }
            if (statusToWaitFor.isPresent() && intermediatePollResponse.getStatus().equals(statusToWaitFor.get())) {
                scheduler.shutdown();
                return intermediatePollResponse;
            }
            final ScheduledFuture<?> pollOp =
                scheduler.schedule(() -> {
                    PollResponse<T> pollResponse1 = pollOperation.apply(pollingContext);
                    pollingContext.setLatestResponse(pollResponse1);
                    }, getDelay(intermediatePollResponse, pollInterval).toMillis(), TimeUnit.MILLISECONDS);
            try {
                if (timeBound) {
                    pollOp.get(timeout.get().toMillis() - elapsedTime, TimeUnit.MILLISECONDS);
                } else {
                    pollOp.get();
                }
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                throw LOGGER.logExceptionAsError(Exceptions.propagate(e));
            }
            intermediatePollResponse = pollingContext.getLatestResponse();
        }

        scheduler.shutdown();
        return intermediatePollResponse;
    }

    static <T, U> Flux<AsyncPollResponse<T, U>> pollingLoopAsync(PollingContext<T> pollingContext,
                                                Function<PollingContext<T>, Mono<PollResponse<T>>> pollOperation,
                                                BiFunction<PollingContext<T>, PollResponse<T>, Mono<T>> cancelOperation,
                                                Function<PollingContext<T>, Mono<U>> fetchResultOperation,
                                                Duration pollInterval) {
        return Flux.using(
            // Create a Polling Context per subscription
            () -> pollingContext,
            // Do polling
            // set|read to|from context as needed, reactor guarantee thread-safety of cxt object.
            cxt -> Mono.defer(() -> pollOperation.apply(cxt))
                .delaySubscription(getDelay(cxt.getLatestResponse(), pollInterval))
                .switchIfEmpty(Mono.error(() -> new IllegalStateException("PollOperation returned Mono.empty().")))
                .repeat()
                .takeUntil(currentPollResponse -> currentPollResponse.getStatus().isComplete())
                .concatMap(currentPollResponse -> {
                    cxt.setLatestResponse(currentPollResponse);
                    return Mono.just(new AsyncPollResponse<>(cxt,
                        cancelOperation,
                        fetchResultOperation));
                }),
            cxt -> { });
    }

    /**
     * Get the duration to wait before making next poll attempt.
     *
     * @param pollResponse the poll response to retrieve delay duration from
     * @return the delay
     */
    private static <T> Duration getDelay(PollResponse<T> pollResponse, Duration pollInterval) {
        Duration retryAfter = pollResponse.getRetryAfter();
        if (retryAfter == null) {
            return pollInterval;
        } else {
            return retryAfter.compareTo(Duration.ZERO) > 0
                ? retryAfter
                : pollInterval;
        }
    }

    static <T, U> PollResponse<T> toPollResponse(AsyncPollResponse<T, U> asyncPollResponse) {
        return new PollResponse<>(asyncPollResponse.getStatus(),
            asyncPollResponse.getValue(),
            asyncPollResponse.getRetryAfter());
    }

    static <T, U> boolean matchStatus(AsyncPollResponse<T, U> currentPollResponse,
                                LongRunningOperationStatus statusToWaitFor) {
        if (currentPollResponse == null || statusToWaitFor == null) {
            return false;
        }
        if (statusToWaitFor == currentPollResponse.getStatus()) {
            return true;
        }
        return false;
    }
}
