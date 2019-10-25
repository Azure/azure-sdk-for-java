// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.polling;

import com.azure.core.util.logging.ClientLogger;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.function.BiFunction;
import java.util.function.Supplier;

/**
 * A Flux that simplifies the task of executing long-running operations against an Azure service.
 * A subscription to {@link PollerFlux} initiate a long-running operation and polls the status
 * until it completes.
 *
 * <p><strong>Code samples</strong></p>
 *
 * <p><strong>Instantiating and subscribing to PollerFlux</strong></p>
 * {@codesnippet com.azure.core.util.polling.poller.instantiationAndSubscribe}
 *
 * <p><strong>Asynchronously wait for polling to complete and then retrieve the final result</strong></p>
 * {@codesnippet com.azure.core.util.polling.poller.getResult}
 *
 * <p><strong>Block for polling to complete and then retrieve the final result</strong></p>
 * {@codesnippet com.azure.core.util.polling.poller.blockAndGetResult}
 *
 * <p><strong>Asynchronously poll until poller receives matching status</strong></p>
 * {@codesnippet com.azure.core.util.polling.poller.pollUntil}
 *
 * <p><strong>Asynchronously cancel the long running operation</strong></p>
 * {@codesnippet com.azure.core.util.polling.poller.cancelOperation}
 *
 * @param <T> The type of poll response value.
 * @param <U> The type of the final result of long-running operation.
 */
public final class PollerFlux<T, U> extends Flux<AsyncPollResponse<T, U>> {
    private final ClientLogger logger = new ClientLogger(PollerFlux.class);
    private final Duration defaultPollInterval;
    private final Supplier<Mono<T>> activationOperation;
    private final BiFunction<PollResponse<T>, PollResponse<T>, Mono<PollResponse<T>>> pollOperation;
    private final BiFunction<PollResponse<T>, PollResponse<T>, Mono<T>> cancelOperation;
    private final BiFunction<PollResponse<T>, PollResponse<T>, Mono<U>> fetchResultOperation;
    private final Mono<PollResponse<T>> oneTimeActivationMono;
    private volatile PollResponse<T> activationResponse;
    private volatile int activationGuardFlag = 0;
    @SuppressWarnings({"rawtypes"})
    private final AtomicIntegerFieldUpdater<PollerFlux> guardActivationCall =
        AtomicIntegerFieldUpdater.newUpdater(PollerFlux.class, "activationGuardFlag");

    /**
     * Creates PollerFlux.
     *
     * @param defaultPollInterval the default polling interval
     * @param activationOperation the activation operation to be invoked at most once across all subscriptions,
     *                            this parameter is required, if there is no specific activation work to be
     *                            done then invocation should return Mono.empty().
     * @param pollOperation the operation to poll the current state of long running operation, this parameter
     *                      is required and the operation will be called with the activation {@link PollResponse}
     *                      and last {@link PollResponse}.
     * @param cancelOperation a {@link BiFunction} that represents the operation to cancel the long-running operation
     *                        if service supports cancellation, this parameter is required and if service does not
     *                        support cancellation then the implementer should return Mono.error with an error message
     *                        indicating absence of cancellation support, the operation will be called with the
     *                        activation {@link PollResponse} and latest {@link PollResponse}.
     * @param fetchResultOperation a {@link BiFunction} that represents the  operation to retrieve final result of
     *                             the long-running operation if service support it, this parameter is required and
     *                             operation will be called with the activation {@link PollResponse} and final
     *                             {@link PollResponse}, if service does not have an api to fetch final result and
     *                             if final result is same as final poll response value then implementer can choose
     *                             to simply return value from provided final poll response.
     */
    public PollerFlux(Duration defaultPollInterval,
                      Supplier<Mono<T>> activationOperation,
                      BiFunction<PollResponse<T>, PollResponse<T>, Mono<PollResponse<T>>> pollOperation,
                      BiFunction<PollResponse<T>, PollResponse<T>, Mono<T>> cancelOperation,
                      BiFunction<PollResponse<T>, PollResponse<T>, Mono<U>> fetchResultOperation) {
        Objects.requireNonNull(defaultPollInterval, "'defaultPollInterval' cannot be null.");
        if (defaultPollInterval.compareTo(Duration.ZERO) <= 0) {
            throw logger.logExceptionAsWarning(new IllegalArgumentException(
                "Negative or zero value for 'defaultPollInterval' is not allowed."));
        }
        this.defaultPollInterval = defaultPollInterval;
        this.activationOperation = Objects.requireNonNull(activationOperation,
            "'activationOperation' cannot be null.");
        this.oneTimeActivationMono = oneTimeActivationMono(activationOperation);
        this.pollOperation = Objects.requireNonNull(pollOperation, "'pollOperation' cannot be null.");
        this.cancelOperation = Objects.requireNonNull(cancelOperation, "'cancelOperation' cannot be null.");
        this.fetchResultOperation = Objects.requireNonNull(fetchResultOperation,
            "'fetchResultOperation' cannot be null.");
    }

    @Override
    public void subscribe(CoreSubscriber<? super AsyncPollResponse<T, U>> actual) {
        this.oneTimeActivationMono
            .flatMapMany(activationResponse -> pollingLoop(activationResponse))
            .subscribe(actual);
    }

    /**
     * @return a synchronous blocking poller.
     */
    public SyncPoller<T, U> getBlockingPoller() {
        return new DefaultSyncPoller<>(this.defaultPollInterval,
            this.activationOperation,
            this.pollOperation,
            this.cancelOperation,
            this.fetchResultOperation);
    }

    /**
     * Returns a decorated Mono, upon subscription it internally subscribes to the Mono that perform one
     * time activation. The decorated Mono caches the result of activation operation in a PollResponse,
     * this cached response will be replayed for any future subscriptions.
     *
     * Note: we can't use standard cache() operator, because it caches error terminal signal and forward
     * it to any future subscriptions. If there is an error from activation Mono then we don't want to cache
     * it but just forward it to subscription that initiated the failed activation. For any future subscriptions
     * we don't want to forward the past error instead activation should again invoked. Once a subscription
     * received a successful event from activation Mono then we want to cache it and replay it to any future
     * subscriptions.
     *
     * The decorated Mono also handles concurrent calls to activation. Only one of them will be able to call
     * activation and other subscriptions will keep resubscribing until it sees a cached response or get a chance
     * to call activation as the one previously entered the critical section got an error on activation.
     *
     * @param activationOperation a supplier that provide a Mono upon subscription execute activation operation.
     * @return a one time activation mono
     */
    private Mono<PollResponse<T>> oneTimeActivationMono(final Supplier<Mono<T>> activationOperation) {
        return Mono.defer(() -> {
            if (this.activationResponse != null) {
                return Mono.just(this.activationResponse);
            }
            if (this.guardActivationCall.compareAndSet(this, 0, 1)) {
                final Mono<T> activationMono;
                try {
                    activationMono = activationOperation.get();
                } catch (Throwable throwable) {
                    return Mono.error(throwable);
                }
                return activationMono.map((T result) -> {
                    this.activationResponse = new PollResponse<>(LongRunningOperationStatus.NOT_STARTED, result);
                    return this.activationResponse;
                })
                .switchIfEmpty(Mono.defer(() -> {
                    this.activationResponse = new PollResponse<>(LongRunningOperationStatus.NOT_STARTED, null);
                    return Mono.just(this.activationResponse);
                }))
                .doOnError(throwable -> guardActivationCall.compareAndSet(this, 1, 0));
            } else {
                return Mono.empty();
            }
        }).repeatWhenEmpty((Flux<Long> longFlux) -> longFlux.concatMap(ignored -> Flux.just(true)));
    }

    /**
     * Do the polling until it reaches a terminal state.
     *
     * @param activationResponse the response from activation operation
     * @return a Flux that emits polling event.
     */
    private Flux<AsyncPollResponse<T, U>> pollingLoop(PollResponse<T> activationResponse) {
        return Flux.using(
            // Create a state per subscription
            () -> new State<>(activationResponse),
            // Do polling
            // set|read in state as needed, reactor guarantee thread-safety of state object.
            state -> Mono.defer(() -> pollOperation.apply(activationResponse, state.getLastResponse()))
                .delaySubscription(getDelay(state.getLastResponse()))
                .repeat()
                .takeUntil(currentPollResponse -> currentPollResponse.getStatus().isComplete())
                .onErrorResume(throwable -> {
                    logger.warning("Received an error from pollOperation. Any error from pollOperation " +
                        "will be ignored and polling will be continued. Error:" + throwable.getMessage());
                    return Mono.empty();
                })
                .concatMap(currentPollResponse -> {
                    state.setLastResponse(currentPollResponse);
                    return Mono.just(new AsyncPollResponse<>(activationResponse,
                        currentPollResponse,
                        this.cancelOperation,
                        this.fetchResultOperation));
                }),
            //
            // No cleaning needed, state will be GC-ed
            state -> {});
    }

    /**
     * Get the duration to wait before making next poll attempt.
     *
     * @param pollResponse the poll response to retrieve delay duration from
     * @return the delay
     */
    private Duration getDelay(PollResponse<T> pollResponse) {
        Duration retryAfter = pollResponse.getRetryAfter();
        if (retryAfter == null) {
            return this.defaultPollInterval;
        } else {
            return retryAfter.compareTo(Duration.ZERO) > 0
                ? retryAfter
                : this.defaultPollInterval;
        }
    }

    /**
     * Type representing state of per subscription to the polling loop.
     *
     * @param <PollResultT> Type of poll response value
     */
    private static final class State<PollResultT> {
        private PollResponse<PollResultT> lastResponse;

        /**
         * Creates a state with first poll response.
         *
         * @param firstResponse the first response.
         */
        private State(PollResponse<PollResultT> firstResponse) {
            this.lastResponse = Objects.requireNonNull(firstResponse);
        }

        /**
         * @return the recent poll response.
         */
        private PollResponse<PollResultT> getLastResponse() {
            return this.lastResponse;
        }

        /**
         * Set the recent poll response.
         *
         * @param lastResponse the recent poll response.
         */
        private void setLastResponse(PollResponse<PollResultT> lastResponse) {
            this.lastResponse = Objects.requireNonNull(lastResponse);
        }
    }
}
