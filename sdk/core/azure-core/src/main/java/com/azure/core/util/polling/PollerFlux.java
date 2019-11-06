// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.polling;

import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.function.BiFunction;
import java.util.function.Function;

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
    private final PollingContext<T> rootContext = new PollingContext<>();
    private final Duration defaultPollInterval;
    private final Function<PollingContext<T>, Mono<T>> activationOperation;
    private final Function<PollingContext<T>, Mono<PollResponse<T>>> pollOperation;
    private final BiFunction<PollingContext<T>, PollResponse<T>, Mono<T>> cancelOperation;
    private final Function<PollingContext<T>, Mono<U>> fetchResultOperation;
    private final Mono<Boolean> oneTimeActivationMono;
    private volatile boolean activated = false;
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
     *                            done then invocation should return Mono.empty(), this operation will be called
     *                            with a new {@link PollingContext}.
     * @param pollOperation the operation to poll the current state of long running operation, this parameter
     *                      is required and the operation will be called with current {@link PollingContext}.
     * @param cancelOperation a {@link Function} that represents the operation to cancel the long-running operation
     *                        if service supports cancellation, this parameter is required and if service does not
     *                        support cancellation then the implementer should return Mono.error with an error message
     *                        indicating absence of cancellation support, the operation will be called with current
     *                        {@link PollingContext}.
     * @param fetchResultOperation a {@link Function} that represents the  operation to retrieve final result of
     *                             the long-running operation if service support it, this parameter is required and
     *                             operation will be called current {@link PollingContext}, if service does not have an
     *                             api to fetch final result and if final result is same as final poll response value
     *                             then implementer can choose to simply return value from provided final poll response.
     */
    public PollerFlux(Duration defaultPollInterval,
                      Function<PollingContext<T>, Mono<T>> activationOperation,
                      Function<PollingContext<T>, Mono<PollResponse<T>>> pollOperation,
                      BiFunction<PollingContext<T>, PollResponse<T>, Mono<T>> cancelOperation,
                      Function<PollingContext<T>, Mono<U>> fetchResultOperation) {
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
            .flatMapMany(ignored -> pollingLoop())
            .subscribe(actual);
    }

    /**
     * @return a synchronous blocking poller.
     */
    public SyncPoller<T, U> getSyncPoller() {
        return new DefaultSyncPoller<>(this.defaultPollInterval,
                this.activationOperation,
                this.pollOperation,
                this.cancelOperation,
                this.fetchResultOperation);
    }

    /**
     * Returns a decorated Mono, upon subscription it internally subscribes to the Mono that perform one
     * time activation. The decorated Mono caches the result of activation operation as a PollResponse
     * in {@code rootContext}, this cached response will be used by any future subscriptions.
     *
     * Note: we can't use standard cache() operator, because it caches error terminal signal and forward
     * it to any future subscriptions. If there is an error from activation Mono then we don't want to cache
     * it but just forward it to subscription that initiated the failed activation. For any future subscriptions
     * we don't want to forward the past error instead activation should again invoked. Once a subscription
     * received a successful event from activation Mono then we cache it in {@code rootContext} and will be used
     * by any future subscriptions.
     *
     * The decorated Mono also handles concurrent calls to activation. Only one of them will be able to call
     * activation and other subscriptions will keep resubscribing until it sees a activation happened or get a chance
     * to call activation as the one previously entered the critical section got an error on activation.
     *
     * @param activationOperation a supplier that provide a Mono upon subscription execute activation operation.
     * @return a one time activation mono
     */
    private Mono<Boolean> oneTimeActivationMono(final Function<PollingContext<T>, Mono<T>> activationOperation) {
        return Mono.defer(() -> {
            if (this.activated) {
                return Mono.just(true);
            }
            if (this.guardActivationCall.compareAndSet(this, 0, 1)) {
                final Mono<T> activationMono;
                try {
                    activationMono = this.activationOperation.apply(this.rootContext);
                } catch (RuntimeException e) {
                    this.guardActivationCall.compareAndSet(this, 1, 0);
                    return FluxUtil.monoError(logger, e);
                }
                //
                return activationMono.map((T result) ->
                        new PollResponse<>(LongRunningOperationStatus.NOT_STARTED, result))
                .switchIfEmpty(Mono.defer(() ->
                        Mono.just(new PollResponse<>(LongRunningOperationStatus.NOT_STARTED, null))))
                .map(activationResponse -> {
                    this.rootContext.setOnetimeActivationResponse(activationResponse);
                    this.activated = true;
                    return true;
                })
                .doOnError(throwable -> this.guardActivationCall.compareAndSet(this, 1, 0));
            } else {
                return Mono.empty();
            }
        }).repeatWhenEmpty((Flux<Long> longFlux) -> longFlux.concatMap(ignored -> Flux.just(true)));
    }

    /**
     * Do the polling until it reaches a terminal state.
     *
     * @return a Flux that emits polling event.
     */
    private Flux<AsyncPollResponse<T, U>> pollingLoop() {
        return Flux.using(
            // Create a Polling Context per subscription
            () -> this.rootContext.copy(),
            // Do polling
            // set|read to|from context as needed, reactor guarantee thread-safety of cxt object.
            cxt -> Mono.defer(() -> this.pollOperation.apply(cxt))
                .delaySubscription(getDelay(cxt.getLatestResponse()))
                .switchIfEmpty(Mono.error(new IllegalStateException("PollOperation returned Mono.empty().")))
                .repeat()
                .takeUntil(currentPollResponse -> currentPollResponse.getStatus().isComplete())
                .onErrorResume(throwable -> {
                    logger.warning("Received an error from pollOperation. Any error from pollOperation "
                        + "will be ignored and polling will be continued. Error:" + throwable.getMessage());
                    return Mono.empty();
                })
                .concatMap(currentPollResponse -> {
                    cxt.setLatestResponse(currentPollResponse);
                    return Mono.just(new AsyncPollResponse<>(cxt,
                        this.cancelOperation,
                        this.fetchResultOperation));
                }),
            //
            // No cleaning needed, Polling Context will be GC-ed
            cxt -> { });
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
}
