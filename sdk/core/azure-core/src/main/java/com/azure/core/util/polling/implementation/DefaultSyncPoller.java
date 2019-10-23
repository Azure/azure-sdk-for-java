// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.polling.implementation;

import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.polling.AsyncPollResponse;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.PollerFlux;
import com.azure.core.util.polling.SyncPoller;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * INTERNAL CLASS.
 *
 * Default implementation of {@link SyncPoller} that uses {@link PollerFlux} underneath.
 *
 * @param <T> The type of poll response value
 * @param <U> The type of the final result of long-running operation
 */
public class DefaultSyncPoller<T, U> implements SyncPoller<T, U> {
    private final ClientLogger logger = new ClientLogger(DefaultSyncPoller.class);
    //
    private final Function<PollResponse<T>, Mono<PollResponse<T>>> pollOperation;
    private final BiFunction<PollResponse<T>, PollResponse<T>, Mono<T>> cancelOperation;
    private final BiFunction<PollResponse<T>, PollResponse<T>, Mono<U>> fetchResultOperation;
    //
    private final PollResponse<T> activationResponse;
    private final PollerFlux<T, U> pollerFlux;
    private PollResponse<T> lastResponse;

    /**
     * Creates DefaultSyncPoller.
     *
     * @param defaultPollInterval the default polling interval
     * @param activationOperation the activation operation to be invoked at most once across all subscriptions,
     *                            this parameter can be null indicating absence of activation operation.
     * @param pollOperation the operation to poll the current state of long running operation, this parameter
     *                      is required, the operation will be called with last {@link PollResponse}
     * @param cancelOperation the operation to cancel the long-running operation if service supports cancellation,
     *                       this parameter can be null indicating absence of cancellation support, the operation
     *                        will be called by passing {@link PollResponse} instances of activation result and last
     *                        response.
     * @param fetchResultOperation the operation to retrieve final result of the long-running operation if service
     *                             support it, this parameter can be null indicating absence of result retrieval
     *                             support, the operation will be called by passing {@link PollResponse} instances
     *                             of activation result and last response.
     */
    public DefaultSyncPoller(Duration defaultPollInterval,
                             Supplier<Mono<T>> activationOperation,
                             Function<PollResponse<T>, Mono<PollResponse<T>>> pollOperation,
                             BiFunction<PollResponse<T>, PollResponse<T>, Mono<T>> cancelOperation,
                             BiFunction<PollResponse<T>, PollResponse<T>, Mono<U>> fetchResultOperation) {
        Objects.requireNonNull(defaultPollInterval, "'defaultPollInterval' cannot be null.");
        if (defaultPollInterval.compareTo(Duration.ZERO) <= 0) {
            throw logger.logExceptionAsWarning(new IllegalArgumentException(
                "Negative or zero value for 'defaultPollInterval' is not allowed."));
        }
        this.pollOperation = Objects.requireNonNull(pollOperation, "' pollOperation' cannot be null.");
        this.cancelOperation = cancelOperation;
        this.fetchResultOperation = fetchResultOperation;
        if (activationOperation != null) {
            this.activationResponse = new PollResponse<>(LongRunningOperationStatus.NOT_STARTED,
                activationOperation.get().block());
        } else {
            this.activationResponse = new PollResponse<>(LongRunningOperationStatus.NOT_STARTED, null);
        }
        this.lastResponse = this.activationResponse;
        this.pollerFlux = new PollerFlux<>(defaultPollInterval,
            () -> Mono.empty(),
            pollOperation,
            cancelOperation,
            fetchResultOperation);
    }

    @Override
    public PollResponse<T> poll() {
        this.lastResponse = this.pollOperation
            .apply(this.lastResponse)
            .block();
        return this.lastResponse;
    }

    @Override
    public PollResponse<T> waitForCompletion() {
        AsyncPollResponse<T, U> finalAsyncPollResponse = this.pollerFlux
            .blockLast();
        return toPollResponse(finalAsyncPollResponse);
    }

    @Override
    public PollResponse<T> waitForCompletion(Duration timeout) {
        AsyncPollResponse<T, U> finalAsyncPollResponse = this.pollerFlux
            .timeout(timeout)
            .last()
            .block();
        return toPollResponse(finalAsyncPollResponse);
    }

    @Override
    public PollResponse<T> waitUntil(LongRunningOperationStatus statusToWaitFor) {
        if (statusToWaitFor == null) {
            throw logger.logExceptionAsWarning(new IllegalArgumentException("Null value for status is not allowed."));
        }
        AsyncPollResponse<T, U> asyncPollResponse = this.pollerFlux
            .takeUntil(apr -> matchStatus(apr, statusToWaitFor))
            .last()
            .switchIfEmpty(Mono.error(new NoSuchElementException("Polling completed without receiving the given status "
                + "'" + statusToWaitFor + "'.")))
            .block();
        return toPollResponse(asyncPollResponse);
    }

    @Override
    public PollResponse<T> waitUntil(LongRunningOperationStatus statusToWaitFor, Duration timeout) {
        if (statusToWaitFor == null) {
            throw logger.logExceptionAsWarning(new IllegalArgumentException("Null value for status is not allowed."));
        }
        if (timeout != null && timeout.toNanos() <= 0) {
            throw logger.logExceptionAsWarning(new IllegalArgumentException(
                "Negative or zero value for timeout is not allowed."));
        }
        AsyncPollResponse<T, U> asyncPollResponse = this.pollerFlux
            .takeUntil(apr -> matchStatus(apr, statusToWaitFor))
            .last()
            .timeout(timeout)
            .switchIfEmpty(Mono.error(new NoSuchElementException("Polling completed without receiving the given status "
                +  "'" + statusToWaitFor + "'.")))
            .block();
        return toPollResponse(asyncPollResponse);
    }

    @Override
    public U getFinalResult(PollResponse<T> finalPollResponse) {
        if (this.fetchResultOperation == null) {
            return null;
        } else {
            try {
                return this.fetchResultOperation
                    .apply(this.activationResponse, finalPollResponse)
                    .block();
            } catch (OperationRequirePollResponse crp) {
                Mono<AsyncPollResponse<T,U>> errorMono
                    = Mono.error(new IllegalStateException("GetResult operation requires final PollResponse "
                    +  "instance, but it is not provided"));
                return this.pollerFlux
                    .onErrorResume(t -> errorMono)
                    .blockLast()
                    .getFinalResult()
                    .block();
            }
        }
    }

    @Override
    public void cancelOperation(PollResponse<T> lastPollResponse) {
        if (this.cancelOperation == null) {
            try {
                this.cancelOperation.apply(this.activationResponse, lastPollResponse).block();
            } catch (OperationRequirePollResponse crp) {
                Mono<AsyncPollResponse<T,U>> errorMono
                    = Mono.error(new IllegalStateException("Cancel operation requires a PollResponse "
                    +  "instance, but it is not provided"));
                //
                AsyncPollResponse<T, U> asyncPollResponse = this.pollerFlux
                    .take(2)
                    .last()
                    .onErrorResume(t -> errorMono)
                    .switchIfEmpty(errorMono)
                    .block();
                this.cancelOperation
                    .apply(this.activationResponse, toPollResponse(asyncPollResponse))
                    .block();
            }
        }
    }

    private PollResponse<T> toPollResponse(AsyncPollResponse<T, U> asyncPollResponse) {
        return new PollResponse<>(asyncPollResponse.getStatus(),
            asyncPollResponse.getValue(),
            asyncPollResponse.getRetryAfter(),
            asyncPollResponse.getProperties());
    }

    private boolean matchStatus(AsyncPollResponse<T, U> currentPollResponse,
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
