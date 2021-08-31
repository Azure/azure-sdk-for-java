// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.polling;

import com.azure.core.http.rest.Response;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.TypeReference;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A Flux that simplifies the task of executing long running operations against an Azure service.
 * A subscription to {@link PollerFlux} initiates a long running operation and polls the status
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
 * <p><strong>Instantiating and subscribing to PollerFlux from a known polling strategy</strong></p>
 * {@codesnippet com.azure.core.util.polling.poller.instantiationAndSubscribeWithPollingStrategy}
 *
 * <p><strong>Instantiating and subscribing to PollerFlux from a custom polling strategy</strong></p>
 * {@codesnippet com.azure.core.util.polling.poller.initializeAndSubscribeWithCustomPollingStrategy}
 *
 * @param <T> The type of poll response value.
 * @param <U> The type of the final result of long running operation.
 */
public final class PollerFlux<T, U> extends Flux<AsyncPollResponse<T, U>> {

    private final ClientLogger logger = new ClientLogger(PollerFlux.class);
    private final PollingContext<T> rootContext = new PollingContext<>();
    private final Function<PollingContext<T>, Mono<PollResponse<T>>> pollOperation;
    private final BiFunction<PollingContext<T>, PollResponse<T>, Mono<T>> cancelOperation;
    private final Function<PollingContext<T>, Mono<U>> fetchResultOperation;
    private final Mono<Boolean> oneTimeActivationMono;
    private final Function<PollingContext<T>, PollResponse<T>> syncActivationOperation;
    private volatile Duration pollInterval;

    /**
     * Creates PollerFlux.
     *
     * @param pollInterval the polling interval
     * @param activationOperation the activation operation to activate (start) the long running operation.
     *     This operation will be invoked at most once across all subscriptions. This parameter is required.
     *     If there is no specific activation work to be done then invocation should return Mono.empty(),
     *     this operation will be called with a new {@link PollingContext}.
     * @param pollOperation the operation to poll the current state of long running operation. This parameter
     *     is required and the operation will be called with current {@link PollingContext}.
     * @param cancelOperation a {@link Function} that represents the operation to cancel the long running operation
     *     if service supports cancellation. This parameter is required. If service does not support cancellation
     *     then the implementer should return Mono.error with an error message indicating absence of cancellation
     *     support. The operation will be called with current {@link PollingContext}.
     * @param fetchResultOperation a {@link Function} that represents the  operation to retrieve final result of
     *     the long running operation if service support it. This parameter is required and operation will be called
     *     current {@link PollingContext}. If service does not have an api to fetch final result and if final result
     *     is same as final poll response value then implementer can choose to simply return value from provided
     *     final poll response.
     */
    public PollerFlux(Duration pollInterval,
                      Function<PollingContext<T>, Mono<T>> activationOperation,
                      Function<PollingContext<T>, Mono<PollResponse<T>>> pollOperation,
                      BiFunction<PollingContext<T>, PollResponse<T>, Mono<T>> cancelOperation,
                      Function<PollingContext<T>, Mono<U>> fetchResultOperation) {
        Objects.requireNonNull(pollInterval, "'pollInterval' cannot be null.");
        if (pollInterval.compareTo(Duration.ZERO) <= 0) {
            throw logger.logExceptionAsWarning(new IllegalArgumentException(
                "Negative or zero value for 'defaultPollInterval' is not allowed."));
        }
        this.pollInterval = pollInterval;
        Objects.requireNonNull(activationOperation, "'activationOperation' cannot be null.");
        this.pollOperation = Objects.requireNonNull(pollOperation, "'pollOperation' cannot be null.");
        this.cancelOperation = Objects.requireNonNull(cancelOperation, "'cancelOperation' cannot be null.");
        this.fetchResultOperation = Objects.requireNonNull(fetchResultOperation,
            "'fetchResultOperation' cannot be null.");
        this.oneTimeActivationMono = new OneTimeActivation<>(this.rootContext,
            activationOperation,
            // mapper
            activationResult -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED, activationResult)).getMono();
        this.syncActivationOperation =
            cxt -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED, activationOperation.apply(cxt).block());
    }

    /**
     * Creates PollerFlux.
     *
     * This create method differs from the PollerFlux constructor in that the constructor uses an
     * activationOperation which returns a Mono that emits result, the create method uses an activationOperation
     * which returns a Mono that emits {@link PollResponse}. The {@link PollResponse} holds the result.
     * If the {@link PollResponse} from the activationOperation indicate that long running operation is
     * completed then the pollOperation will not be called.
     *
     * @param pollInterval the polling interval
     * @param activationOperation the activation operation to activate (start) the long running operation.
     *     This operation will be invoked at most once across all subscriptions. This parameter is required.
     *     If there is no specific activation work to be done then invocation should return Mono.empty(),
     *     this operation will be called with a new {@link PollingContext}.
     * @param pollOperation the operation to poll the current state of long running operation. This parameter
     *     is required and the operation will be called with current {@link PollingContext}.
     * @param cancelOperation a {@link Function} that represents the operation to cancel the long running operation
     *     if service supports cancellation. This parameter is required. If service does not support cancellation
     *     then the implementer should return Mono.error with an error message indicating absence of cancellation
     *     support. The operation will be called with current {@link PollingContext}.
     * @param fetchResultOperation a {@link Function} that represents the  operation to retrieve final result of
     *     the long running operation if service support it. This parameter is required and operation will be called
     *     current {@link PollingContext}. If service does not have an api to fetch final result and if final result
     *     is same as final poll response value then implementer can choose to simply return value from provided
     *     final poll response.
     *
     * @param <T> The type of poll response value.
     * @param <U> The type of the final result of long running operation.
     * @return PollerFlux
     */
    public static <T, U> PollerFlux<T, U>
        create(Duration pollInterval,
               Function<PollingContext<T>, Mono<PollResponse<T>>> activationOperation,
               Function<PollingContext<T>, Mono<PollResponse<T>>> pollOperation,
               BiFunction<PollingContext<T>, PollResponse<T>, Mono<T>> cancelOperation,
               Function<PollingContext<T>, Mono<U>> fetchResultOperation) {
        return new PollerFlux<>(pollInterval,
            activationOperation,
            pollOperation,
            cancelOperation,
            fetchResultOperation,
            true);
    }

    /**
     * Creates PollerFlux.
     *
     * This create method uses a {@link PollingStrategy} to poll the status of a long running operation after the
     * activation operation is invoked. See {@link PollingStrategy} for more details of known polling strategies
     * and how to create a custom strategy.
     *
     * @param pollInterval the polling interval
     * @param initialOperation the activation operation to activate (start) the long running operation.
     *     This operation will be invoked at most once across all subscriptions. This parameter is required.
     *     If there is no specific activation work to be done then invocation should return Mono.empty(),
     *     this operation will be called with a new {@link PollingContext}.
     * @param strategy a known strategy for polling a long running operation in Azure
     * @param pollResponseType the {@link TypeReference} of the response type from a polling call, or BinaryData if raw
     *                         response body should be kept. This should match the generic parameter {@link U}.
     * @param resultType the {@link TypeReference} of the final result object to deserialize into, or BinaryData if raw
     *                   response body should be kept. This should match the generic parameter {@link U}.
     * @param <T> The type of poll response value.
     * @param <U> The type of the final result of long running operation.
     * @return PollerFlux
     */
    @SuppressWarnings("unchecked")
    public static <T, U> PollerFlux<T, U>
        create(Duration pollInterval,
               Supplier<Mono<? extends Response<?>>> initialOperation,
               PollingStrategy<T, U> strategy,
               TypeReference<T> pollResponseType,
               TypeReference<U> resultType) {
        return create(
            pollInterval,
            context -> initialOperation.get()
                .flatMap(response -> strategy.canPoll(response).flatMap(canPoll -> {
                    if (!canPoll) {
                        return Mono.error(new IllegalStateException(
                            "Cannot poll with strategy " + strategy.getClass().getSimpleName()));
                    }
                    return strategy.onInitialResponse(response, context, pollResponseType);
                })),
            context -> strategy.poll(context, pollResponseType),
            strategy::cancel,
            context -> strategy.getResult(context, resultType));
    }

    private PollerFlux(Duration pollInterval,
                       Function<PollingContext<T>, Mono<PollResponse<T>>> activationOperation,
                       Function<PollingContext<T>, Mono<PollResponse<T>>> pollOperation,
                       BiFunction<PollingContext<T>, PollResponse<T>, Mono<T>> cancelOperation,
                       Function<PollingContext<T>, Mono<U>> fetchResultOperation,
                       boolean ignored) {
        Objects.requireNonNull(pollInterval, "'pollInterval' cannot be null.");
        if (pollInterval.isNegative() || pollInterval.isZero()) {
            throw logger.logExceptionAsWarning(new IllegalArgumentException(
                "Negative or zero value for 'pollInterval' is not allowed."));
        }
        this.pollInterval = pollInterval;
        Objects.requireNonNull(activationOperation, "'activationOperation' cannot be null.");
        this.pollOperation = Objects.requireNonNull(pollOperation, "'pollOperation' cannot be null.");
        this.cancelOperation = Objects.requireNonNull(cancelOperation, "'cancelOperation' cannot be null.");
        this.fetchResultOperation = Objects.requireNonNull(fetchResultOperation,
            "'fetchResultOperation' cannot be null.");
        this.oneTimeActivationMono = new OneTimeActivation<>(this.rootContext,
            activationOperation,
            // mapper
            Function.identity()).getMono();
        this.syncActivationOperation = cxt -> activationOperation.apply(cxt).block();
    }

    /**
     * Creates a PollerFlux instance that returns an error on subscription.
     *
     * @param ex The exception to be returned on subscription of this {@link PollerFlux}.
     * @param <T> The type of poll response value.
     * @param <U> The type of the final result of long running operation.
     * @return A poller flux instance that returns an error without emitting any data.
     *
     * @see Mono#error(Throwable)
     * @see Flux#error(Throwable)
     */
    public static <T, U> PollerFlux<T, U> error(Exception ex) {
        return new PollerFlux<>(Duration.ofMillis(1L), context -> Mono.error(ex), context -> Mono.error(ex),
            (context, response) -> Mono.error(ex), context -> Mono.error(ex));
    }

    /**
     * Sets the poll interval for this poller. The new interval will be used for all subsequent polling operations
     * including the subscriptions that are already in progress.
     *
     * @param pollInterval The new poll interval for this poller.
     * @return The updated instance of {@link PollerFlux}.
     * @throws NullPointerException if the {@code pollInterval} is null.
     * @throws IllegalArgumentException if the {@code pollInterval} is zero or negative.
     */
    public PollerFlux<T, U> setPollInterval(Duration pollInterval) {
        Objects.requireNonNull(pollInterval, "'pollInterval' cannot be null.");
        if (pollInterval.isNegative() || pollInterval.isZero()) {
            throw logger.logExceptionAsWarning(new IllegalArgumentException(
                "Negative or zero value for 'pollInterval' is not allowed."));
        }
        this.pollInterval = pollInterval;
        return this;
    }

    /**
     * Returns the current polling duration for this {@link PollerFlux} instance.
     *
     * @return The current polling duration.
     */
    public Duration getPollInterval() {
        return this.pollInterval;
    }

    @Override
    public void subscribe(CoreSubscriber<? super AsyncPollResponse<T, U>> actual) {
        this.oneTimeActivationMono
            .flatMapMany(ignored -> {
                final PollResponse<T> activationResponse = this.rootContext.getActivationResponse();
                if (activationResponse.getStatus().isComplete()) {
                    return Flux.just(new AsyncPollResponse<>(this.rootContext,
                        this.cancelOperation,
                        this.fetchResultOperation));
                } else {
                    return this.pollingLoop();
                }
            })
            .subscribe(actual);
    }

    /**
     * @return a synchronous blocking poller.
     */
    public SyncPoller<T, U> getSyncPoller() {
        return new DefaultSyncPoller<>(this.pollInterval,
                this.syncActivationOperation,
                this.pollOperation,
                this.cancelOperation,
                this.fetchResultOperation);
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
            cxt -> Mono.defer(() -> {
                final Mono<PollResponse<T>> pollOnceMono = this.pollOperation.apply(cxt);
                // Execute (subscribe to) the pollOnceMono after the default poll-interval
                // or duration specified in the last retry-after response header elapses.
                return pollOnceMono.delaySubscription(getDelay(cxt.getLatestResponse()));
            })
                .switchIfEmpty(Mono.error(new IllegalStateException("PollOperation returned Mono.empty().")))
                .repeat()
                .takeUntil(currentPollResponse -> currentPollResponse.getStatus().isComplete())
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
            return this.pollInterval;
        } else {
            return retryAfter.compareTo(Duration.ZERO) > 0
                ? retryAfter
                : this.pollInterval;
        }
    }

    /**
     * A utility to get One-Time-Executable-Mono that execute an activation function at most once.
     *
     * When subscribed to such a Mono it internally subscribes to a Mono that perform an activation
     * function. The One-Time-Executable-Mono caches the result of activation function as a PollResponse
     * in {@code rootContext}, this cached response will be used by any future subscriptions.
     *
     * Note: The standard cache() operator can't be used to achieve one time execution, because it caches
     * error terminal signal and forward it to any future subscriptions. If there is an error while executing
     * activation function then error should not be cached but it should be forward it to subscription that
     * initiated the failed activation. For any future subscriptions such past error should not be delivered
     * instead activation function should again invoked. Once a subscription result in successful execution
     * of activation function then it will be cached in {@code rootContext} and will be used by any future
     * subscriptions.
     *
     * The One-Time-Executable-Mono handles concurrent calls to activation. Only one of them will be able
     * to execute the activation function and other subscriptions will keep resubscribing until it sees
     * a activation happened or get a chance to call activation as the one previously entered the critical
     * section got an error on activation.
     *
     * @param <V> The type of value in poll response.
     * @param <R> The type of the activation operation result.
     */
    private class OneTimeActivation<V, R> {
        private final PollingContext<V> rootContext;
        private final Function<PollingContext<V>, Mono<R>> activationFunction;
        private final Function<R, PollResponse<V>> activationPollResponseMapper;
        // indicates whether activation executed and completed 'successfully'.
        private volatile boolean activated = false;
        // to guard one-time-activation area
        private final AtomicBoolean guardActivation = new AtomicBoolean(false);

        /**
         * Creates OneTimeActivation.
         *
         * @param rootContext the root context to store PollResponse holding activation result
         * @param activationFunction function upon call return a Mono representing activation work
         * @param activationPollResponseMapper mapper to map result of activation work execution to PollResponse
         */
        OneTimeActivation(PollingContext<V> rootContext,
                          Function<PollingContext<V>, Mono<R>> activationFunction,
                          Function<R, PollResponse<V>> activationPollResponseMapper) {
            this.rootContext = rootContext;
            this.activationFunction = activationFunction;
            this.activationPollResponseMapper = activationPollResponseMapper;
        }

        /**
         * Get the mono containing activation work which on subscription executed only once.
         *
         * @return the one time executable mono
         */
        Mono<Boolean> getMono() {
            return Mono.defer(() -> {
                if (this.activated) {
                    // already activated let subscriber get activation result from root context.
                    return Mono.just(true);
                }
                if (this.guardActivation.compareAndSet(false, true)) {
                    // one-time-activation-area
                    //
                    final Mono<R> activationMono;
                    try {
                        activationMono = this.activationFunction.apply(this.rootContext);
                    } catch (RuntimeException e) {
                        // onError: sync apply() failed
                        //    1. remove guard so that future subscriber can retry activation.
                        //    2. forward error to current subscriber.
                        this.guardActivation.set(false);
                        return FluxUtil.monoError(logger, e);
                    }
                    return activationMono
                        .map(this.activationPollResponseMapper)
                        .switchIfEmpty(Mono.defer(() ->
                            Mono.just(new PollResponse<>(LongRunningOperationStatus.NOT_STARTED, null))))
                        .map(activationResponse -> {
                            this.rootContext.setOnetimeActivationResponse(activationResponse);
                            this.activated = true;
                            return true;
                        })
                        // onError: async activation failed
                        // 1. remove guard so that future subscription can retry activation.
                        // 2. forward error to current subscriber.
                        .doOnError(throwable -> this.guardActivation.set(false));
                } else {
                    // Couldn't enter one-time-activation-area (there was already someone in the area
                    // trying to activate). Return empty() to outer "repeatWhenEmpty" that will result
                    // in another attempt to enter one-time-activation-area.
                    return Mono.empty();
                }
            })
            // Keep resubscribing as long as Mono.defer [holding activation work] emits empty().
            .repeatWhenEmpty((Flux<Long> longFlux) -> longFlux.concatMap(ignored -> Flux.just(true)));
        }
    }
}
