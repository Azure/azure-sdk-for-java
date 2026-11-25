// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.polling;

import com.azure.core.http.rest.Response;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.TypeReference;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A Flux that simplifies the task of executing long-running operations against an Azure service. A subscription to
 * {@link PollerFlux} initiates a long-running operation and polls the status until it completes.
 * Once activation starts, cancelling a subscription does not cancel the shared activation. Subsequent subscriptions
 * reuse its result.
 *
 * <p>
 * <strong>Code samples</strong>
 * </p>
 *
 * <p>
 * <strong>Instantiating and subscribing to PollerFlux</strong>
 * </p>
 * <!-- src_embed com.azure.core.util.polling.poller.instantiationAndSubscribe -->
 * <pre>
 * LocalDateTime timeToReturnFinalResponse = LocalDateTime.now&#40;&#41;.plus&#40;Duration.ofMillis&#40;800&#41;&#41;;
 *
 * &#47;&#47; Create poller instance
 * PollerFlux&lt;String, String&gt; poller = new PollerFlux&lt;&gt;&#40;Duration.ofMillis&#40;100&#41;,
 *     &#40;context&#41; -&gt; Mono.empty&#40;&#41;,
 *     &#47;&#47; Define your custom poll operation
 *     &#40;context&#41; -&gt;  &#123;
 *         if &#40;LocalDateTime.now&#40;&#41;.isBefore&#40;timeToReturnFinalResponse&#41;&#41; &#123;
 *             System.out.println&#40;&quot;Returning intermediate response.&quot;&#41;;
 *             return Mono.just&#40;new PollResponse&lt;&gt;&#40;LongRunningOperationStatus.IN_PROGRESS,
 *                     &quot;Operation in progress.&quot;&#41;&#41;;
 *         &#125; else &#123;
 *             System.out.println&#40;&quot;Returning final response.&quot;&#41;;
 *             return Mono.just&#40;new PollResponse&lt;&gt;&#40;LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
 *                     &quot;Operation completed.&quot;&#41;&#41;;
 *         &#125;
 *     &#125;,
 *     &#40;activationResponse, context&#41; -&gt; Mono.error&#40;new RuntimeException&#40;&quot;Cancellation is not supported&quot;&#41;&#41;,
 *     &#40;context&#41; -&gt; Mono.just&#40;&quot;Final Output&quot;&#41;&#41;;
 *
 * &#47;&#47; Listen to poll responses
 * poller.subscribe&#40;response -&gt; &#123;
 *     &#47;&#47; Process poll response
 *     System.out.printf&#40;&quot;Got response. Status: %s, Value: %s%n&quot;, response.getStatus&#40;&#41;, response.getValue&#40;&#41;&#41;;
 * &#125;&#41;;
 * &#47;&#47; Do something else
 *
 * </pre>
 * <!-- end com.azure.core.util.polling.poller.instantiationAndSubscribe -->
 *
 * <p>
 * <strong>Asynchronously wait for polling to complete and then retrieve the final result</strong>
 * </p>
 * <!-- src_embed com.azure.core.util.polling.poller.getResult -->
 * <pre>
 * LocalDateTime timeToReturnFinalResponse = LocalDateTime.now&#40;&#41;.plus&#40;Duration.ofMinutes&#40;5&#41;&#41;;
 *
 * &#47;&#47; Create poller instance
 * PollerFlux&lt;String, String&gt; poller = new PollerFlux&lt;&gt;&#40;Duration.ofMillis&#40;100&#41;,
 *     &#40;context&#41; -&gt; Mono.empty&#40;&#41;,
 *     &#40;context&#41; -&gt;  &#123;
 *         if &#40;LocalDateTime.now&#40;&#41;.isBefore&#40;timeToReturnFinalResponse&#41;&#41; &#123;
 *             System.out.println&#40;&quot;Returning intermediate response.&quot;&#41;;
 *             return Mono.just&#40;new PollResponse&lt;&gt;&#40;LongRunningOperationStatus.IN_PROGRESS,
 *                     &quot;Operation in progress.&quot;&#41;&#41;;
 *         &#125; else &#123;
 *             System.out.println&#40;&quot;Returning final response.&quot;&#41;;
 *             return Mono.just&#40;new PollResponse&lt;&gt;&#40;LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
 *                     &quot;Operation completed.&quot;&#41;&#41;;
 *         &#125;
 *     &#125;,
 *     &#40;activationResponse, context&#41; -&gt; Mono.just&#40;&quot;FromServer:OperationIsCancelled&quot;&#41;,
 *     &#40;context&#41; -&gt; Mono.just&#40;&quot;FromServer:FinalOutput&quot;&#41;&#41;;
 *
 * poller.take&#40;Duration.ofMinutes&#40;30&#41;&#41;
 *         .last&#40;&#41;
 *         .flatMap&#40;asyncPollResponse -&gt; &#123;
 *             if &#40;asyncPollResponse.getStatus&#40;&#41; == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED&#41; &#123;
 *                 &#47;&#47; operation completed successfully, retrieving final result.
 *                 return asyncPollResponse
 *                         .getFinalResult&#40;&#41;;
 *             &#125; else &#123;
 *                 return Mono.error&#40;new RuntimeException&#40;&quot;polling completed unsuccessfully with status:&quot;
 *                         + asyncPollResponse.getStatus&#40;&#41;&#41;&#41;;
 *             &#125;
 *         &#125;&#41;.block&#40;&#41;;
 *
 * </pre>
 * <!-- end com.azure.core.util.polling.poller.getResult -->
 *
 * <p>
 * <strong>Block for polling to complete and then retrieve the final result</strong>
 * </p>
 * <!-- src_embed com.azure.core.util.polling.poller.blockAndGetResult -->
 * <pre>
 * AsyncPollResponse&lt;String, String&gt; terminalResponse = pollerFlux.blockLast&#40;&#41;;
 * System.out.printf&#40;&quot;Polling complete. Final Status: %s&quot;, terminalResponse.getStatus&#40;&#41;&#41;;
 * if &#40;terminalResponse.getStatus&#40;&#41; == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED&#41; &#123;
 *     String finalResult = terminalResponse.getFinalResult&#40;&#41;.block&#40;&#41;;
 *     System.out.printf&#40;&quot;Polling complete. Final Status: %s&quot;, finalResult&#41;;
 * &#125;
 * </pre>
 * <!-- end com.azure.core.util.polling.poller.blockAndGetResult -->
 *
 * <p>
 * <strong>Asynchronously poll until poller receives matching status</strong>
 * </p>
 * <!-- src_embed com.azure.core.util.polling.poller.pollUntil -->
 * <pre>
 * final Predicate&lt;AsyncPollResponse&lt;String, String&gt;&gt; isComplete = response -&gt; &#123;
 *     return response.getStatus&#40;&#41; != LongRunningOperationStatus.IN_PROGRESS
 *         &amp;&amp; response.getStatus&#40;&#41; != LongRunningOperationStatus.NOT_STARTED;
 * &#125;;
 *
 * pollerFlux
 *     .takeUntil&#40;isComplete&#41;
 *     .subscribe&#40;completed -&gt; &#123;
 *         System.out.println&#40;&quot;Completed poll response, status: &quot; + completed.getStatus&#40;&#41;&#41;;
 *     &#125;&#41;;
 * </pre>
 * <!-- end com.azure.core.util.polling.poller.pollUntil -->
 *
 * <p>
 * <strong>Asynchronously cancel the long running operation</strong>
 * </p>
 * <!-- src_embed com.azure.core.util.polling.poller.cancelOperation -->
 * <pre>
 * LocalDateTime timeToReturnFinalResponse = LocalDateTime.now&#40;&#41;.plus&#40;Duration.ofMinutes&#40;5&#41;&#41;;
 *
 * &#47;&#47; Create poller instance
 * PollerFlux&lt;String, String&gt; poller = new PollerFlux&lt;&gt;&#40;Duration.ofMillis&#40;100&#41;,
 *     &#40;context&#41; -&gt; Mono.empty&#40;&#41;,
 *     &#40;context&#41; -&gt;  &#123;
 *         if &#40;LocalDateTime.now&#40;&#41;.isBefore&#40;timeToReturnFinalResponse&#41;&#41; &#123;
 *             System.out.println&#40;&quot;Returning intermediate response.&quot;&#41;;
 *             return Mono.just&#40;new PollResponse&lt;&gt;&#40;LongRunningOperationStatus.IN_PROGRESS,
 *                     &quot;Operation in progress.&quot;&#41;&#41;;
 *         &#125; else &#123;
 *             System.out.println&#40;&quot;Returning final response.&quot;&#41;;
 *             return Mono.just&#40;new PollResponse&lt;&gt;&#40;LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
 *                     &quot;Operation completed.&quot;&#41;&#41;;
 *         &#125;
 *     &#125;,
 *     &#40;activationResponse, context&#41; -&gt; Mono.just&#40;&quot;FromServer:OperationIsCancelled&quot;&#41;,
 *     &#40;context&#41; -&gt; Mono.just&#40;&quot;FromServer:FinalOutput&quot;&#41;&#41;;
 *
 * &#47;&#47; Asynchronously wait 30 minutes to complete the polling, if not completed
 * &#47;&#47; within in the time then cancel the server operation.
 * poller.take&#40;Duration.ofMinutes&#40;30&#41;&#41;
 *         .last&#40;&#41;
 *         .flatMap&#40;asyncPollResponse -&gt; &#123;
 *             if &#40;!asyncPollResponse.getStatus&#40;&#41;.isComplete&#40;&#41;&#41; &#123;
 *                 return asyncPollResponse
 *                         .cancelOperation&#40;&#41;
 *                         .then&#40;Mono.error&#40;new RuntimeException&#40;&quot;Operation is cancelled!&quot;&#41;&#41;&#41;;
 *             &#125; else &#123;
 *                 return Mono.just&#40;asyncPollResponse&#41;;
 *             &#125;
 *         &#125;&#41;.block&#40;&#41;;
 *
 * </pre>
 * <!-- end com.azure.core.util.polling.poller.cancelOperation -->
 *
 * <p>
 * <strong>Instantiating and subscribing to PollerFlux from a known polling strategy</strong>
 * </p>
 * <!-- src_embed com.azure.core.util.polling.poller.instantiationAndSubscribeWithPollingStrategy -->
 * <pre>
 * &#47;&#47; Create poller instance
 * PollerFlux&lt;BinaryData, String&gt; poller = PollerFlux.create&#40;
 *     Duration.ofMillis&#40;100&#41;,
 *     &#47;&#47; pass in your custom activation operation
 *     &#40;&#41; -&gt; Mono.just&#40;new SimpleResponse&lt;Void&gt;&#40;new HttpRequest&#40;
 *         HttpMethod.POST,
 *         &quot;http:&#47;&#47;httpbin.org&quot;&#41;,
 *         202,
 *         new HttpHeaders&#40;&#41;.set&#40;&quot;Operation-Location&quot;, &quot;http:&#47;&#47;httpbin.org&quot;&#41;,
 *         null&#41;&#41;,
 *     new OperationResourcePollingStrategy&lt;&gt;&#40;new HttpPipelineBuilder&#40;&#41;.build&#40;&#41;&#41;,
 *     TypeReference.createInstance&#40;BinaryData.class&#41;,
 *     TypeReference.createInstance&#40;String.class&#41;&#41;;
 *
 * &#47;&#47; Listen to poll responses
 * poller.subscribe&#40;response -&gt; &#123;
 *     &#47;&#47; Process poll response
 *     System.out.printf&#40;&quot;Got response. Status: %s, Value: %s%n&quot;, response.getStatus&#40;&#41;, response.getValue&#40;&#41;&#41;;
 * &#125;&#41;;
 * &#47;&#47; Do something else
 *
 * </pre>
 * <!-- end com.azure.core.util.polling.poller.instantiationAndSubscribeWithPollingStrategy -->
 *
 * <p>
 * <strong>Instantiating and subscribing to PollerFlux from a custom polling strategy</strong>
 * </p>
 * <!-- src_embed com.azure.core.util.polling.poller.initializeAndSubscribeWithCustomPollingStrategy -->
 * <pre>
 *
 * &#47;&#47; Create custom polling strategy based on OperationResourcePollingStrategy
 * PollingStrategy&lt;BinaryData, String&gt; strategy = new OperationResourcePollingStrategy&lt;BinaryData, String&gt;&#40;
 *         new HttpPipelineBuilder&#40;&#41;.build&#40;&#41;&#41; &#123;
 *     &#47;&#47; override any interface method to customize the polling behavior
 *     &#64;Override
 *     public Mono&lt;PollResponse&lt;BinaryData&gt;&gt; poll&#40;PollingContext&lt;BinaryData&gt; context,
 *                                                TypeReference&lt;BinaryData&gt; pollResponseType&#41; &#123;
 *         return Mono.just&#40;new PollResponse&lt;&gt;&#40;
 *             LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
 *             BinaryData.fromString&#40;&quot;&quot;&#41;&#41;&#41;;
 *     &#125;
 * &#125;;
 *
 * &#47;&#47; Create poller instance
 * PollerFlux&lt;BinaryData, String&gt; poller = PollerFlux.create&#40;
 *     Duration.ofMillis&#40;100&#41;,
 *     &#47;&#47; pass in your custom activation operation
 *     &#40;&#41; -&gt; Mono.just&#40;new SimpleResponse&lt;Void&gt;&#40;new HttpRequest&#40;
 *         HttpMethod.POST,
 *         &quot;http:&#47;&#47;httpbin.org&quot;&#41;,
 *         202,
 *         new HttpHeaders&#40;&#41;.set&#40;&quot;Operation-Location&quot;, &quot;http:&#47;&#47;httpbin.org&quot;&#41;,
 *         null&#41;&#41;,
 *     strategy,
 *     TypeReference.createInstance&#40;BinaryData.class&#41;,
 *     TypeReference.createInstance&#40;String.class&#41;&#41;;
 *
 * &#47;&#47; Listen to poll responses
 * poller.subscribe&#40;response -&gt; &#123;
 *     &#47;&#47; Process poll response
 *     System.out.printf&#40;&quot;Got response. Status: %s, Value: %s%n&quot;, response.getStatus&#40;&#41;, response.getValue&#40;&#41;&#41;;
 * &#125;&#41;;
 * &#47;&#47; Do something else
 *
 * </pre>
 * <!-- end com.azure.core.util.polling.poller.initializeAndSubscribeWithCustomPollingStrategy -->
 *
 * @see com.azure.core.util.polling
 * @param <T> The type of poll response value.
 * @param <U> The type of the final result of long-running operation.
 */
public final class PollerFlux<T, U> extends Flux<AsyncPollResponse<T, U>> {
    // PollerFlux is a commonly used class, use a static logger.
    private static final ClientLogger LOGGER = new ClientLogger(PollerFlux.class);
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
     * @param activationOperation the activation operation to activate (start) the long-running operation. This
     * operation will be invoked at most once across all subscriptions. This parameter is required. If there is no
     * specific activation work to be done then invocation should return Mono.empty(), this operation will be called
     * with a new {@link PollingContext}.
     * @param pollOperation the operation to poll the current state of long-running operation. This parameter is
     * required and the operation will be called with current {@link PollingContext}.
     * @param cancelOperation a {@link Function} that represents the operation to cancel the long-running operation if
     * service supports cancellation. This parameter is required. If service does not support cancellation then the
     * implementer should return {@link Mono#error}with an error message indicating absence of cancellation support. The
     * operation will be called with current {@link PollingContext}.
     * @param fetchResultOperation a {@link Function} that represents the  operation to retrieve final result of the
     * long-running operation if service support it. This parameter is required and operation will be called with the
     * current {@link PollingContext}. If service does not have an api to fetch final result and if final result is same
     * as final poll response value then implementer can choose to simply return value from provided final poll
     * response.
     * @throws NullPointerException if {@code pollInterval}, {@code activationOperation}, {@code pollOperation},
     * {@code cancelOperation} or {@code fetchResultOperation} is {@code null}.
     * @throws IllegalArgumentException if {@code pollInterval} is zero or negative.
     */
    public PollerFlux(Duration pollInterval, Function<PollingContext<T>, Mono<T>> activationOperation,
        Function<PollingContext<T>, Mono<PollResponse<T>>> pollOperation,
        BiFunction<PollingContext<T>, PollResponse<T>, Mono<T>> cancelOperation,
        Function<PollingContext<T>, Mono<U>> fetchResultOperation) {
        Objects.requireNonNull(pollInterval, "'pollInterval' cannot be null.");
        if (pollInterval.compareTo(Duration.ZERO) <= 0) {
            throw LOGGER.logExceptionAsWarning(
                new IllegalArgumentException("Negative or zero value for 'defaultPollInterval' is not allowed."));
        }
        this.pollInterval = pollInterval;
        Objects.requireNonNull(activationOperation, "'activationOperation' cannot be null.");
        this.pollOperation = Objects.requireNonNull(pollOperation, "'pollOperation' cannot be null.");
        this.cancelOperation = Objects.requireNonNull(cancelOperation, "'cancelOperation' cannot be null.");
        this.fetchResultOperation
            = Objects.requireNonNull(fetchResultOperation, "'fetchResultOperation' cannot be null.");
        this.oneTimeActivationMono = createActivationMono(this.rootContext, activationOperation,
            activationResult -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED, activationResult));
        this.syncActivationOperation
            = cxt -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED, activationOperation.apply(cxt).block());
    }

    /**
     * Creates PollerFlux.
     * <p>
     * This method differs from the PollerFlux constructor in that the constructor uses an activationOperation which
     * returns a Mono that emits result, the create method uses an activationOperation which returns a Mono that emits
     * {@link PollResponse}. The {@link PollResponse} holds the result. If the {@link PollResponse} from the
     * activationOperation indicate that long-running operation is completed then the pollOperation will not be called.
     *
     * @param pollInterval the polling interval
     * @param activationOperation the activation operation to activate (start) the long-running operation. This
     * operation will be invoked at most once across all subscriptions. This parameter is required. If there is no
     * specific activation work to be done then invocation should return Mono.empty(), this operation will be called
     * with a new {@link PollingContext}.
     * @param pollOperation the operation to poll the current state of long-running operation. This parameter is
     * required and the operation will be called with current {@link PollingContext}.
     * @param cancelOperation a {@link Function} that represents the operation to cancel the long-running operation if
     * service supports cancellation. This parameter is required. If service does not support cancellation then the
     * implementer should return {@link Mono#error} with an error message indicating absence of cancellation support.
     * The operation will be called with current {@link PollingContext}.
     * @param fetchResultOperation a {@link Function} that represents the  operation to retrieve final result of the
     * long-running operation if service support it. This parameter is required and operation will be called current
     * {@link PollingContext}. If service does not have an api to fetch final result and if final result is same as
     * final poll response value then implementer can choose to simply return value from provided final poll response.
     * @param <T> The type of poll response value.
     * @param <U> The type of the final result of long-running operation.
     * @return PollerFlux
     * @throws NullPointerException if {@code pollInterval}, {@code activationOperation}, {@code pollOperation},
     * {@code cancelOperation} or {@code fetchResultOperation} is {@code null}.
     * @throws IllegalArgumentException if {@code pollInterval} is zero or negative.
     */
    public static <T, U> PollerFlux<T, U> create(Duration pollInterval,
        Function<PollingContext<T>, Mono<PollResponse<T>>> activationOperation,
        Function<PollingContext<T>, Mono<PollResponse<T>>> pollOperation,
        BiFunction<PollingContext<T>, PollResponse<T>, Mono<T>> cancelOperation,
        Function<PollingContext<T>, Mono<U>> fetchResultOperation) {
        return new PollerFlux<>(pollInterval, activationOperation, pollOperation, cancelOperation, fetchResultOperation,
            true);
    }

    /**
     * Creates PollerFlux.
     * <p>
     * This method uses a {@link PollingStrategy} to poll the status of a long-running operation after the activation
     * operation is invoked. See {@link PollingStrategy} for more details of known polling strategies and how to create
     * a custom strategy.
     *
     * @param pollInterval the polling interval
     * @param initialOperation the activation operation to activate (start) the long-running operation. This operation
     * will be invoked at most once across all subscriptions. This parameter is required. If there is no specific
     * activation work to be done then invocation should return Mono.empty(), this operation will be called with a new
     * {@link PollingContext}.
     * @param strategy a known strategy for polling a long-running operation in Azure
     * @param pollResponseType the {@link TypeReference} of the response type from a polling call, or BinaryData if raw
     * response body should be kept. This should match the generic parameter {@link U}.
     * @param resultType the {@link TypeReference} of the final result object to deserialize into, or BinaryData if raw
     * response body should be kept. This should match the generic parameter {@link U}.
     * @param <T> The type of poll response value.
     * @param <U> The type of the final result of long-running operation.
     * @return PollerFlux
     * @throws NullPointerException if {@code pollInterval}, {@code initialOperation}, {@code strategy},
     * {@code pollResponseType} or {@code resultType} is {@code null}.
     * @throws IllegalArgumentException if {@code pollInterval} is zero or negative.
     */
    public static <T, U> PollerFlux<T, U> create(Duration pollInterval,
        Supplier<Mono<? extends Response<?>>> initialOperation, PollingStrategy<T, U> strategy,
        TypeReference<T> pollResponseType, TypeReference<U> resultType) {
        return create(pollInterval,
            context -> initialOperation.get().flatMap(response -> strategy.canPoll(response).flatMap(canPoll -> {
                if (!canPoll) {
                    return Mono.error(
                        new IllegalStateException("Cannot poll with strategy " + strategy.getClass().getSimpleName()));
                }
                return strategy.onInitialResponse(response, context, pollResponseType);
            })), context -> strategy.poll(context, pollResponseType), strategy::cancel,
            context -> strategy.getResult(context, resultType));
    }

    private PollerFlux(Duration pollInterval, Function<PollingContext<T>, Mono<PollResponse<T>>> activationOperation,
        Function<PollingContext<T>, Mono<PollResponse<T>>> pollOperation,
        BiFunction<PollingContext<T>, PollResponse<T>, Mono<T>> cancelOperation,
        Function<PollingContext<T>, Mono<U>> fetchResultOperation, boolean ignored) {
        Objects.requireNonNull(pollInterval, "'pollInterval' cannot be null.");
        if (pollInterval.isNegative() || pollInterval.isZero()) {
            throw LOGGER.logExceptionAsWarning(
                new IllegalArgumentException("Negative or zero value for 'pollInterval' is not allowed."));
        }
        this.pollInterval = pollInterval;
        Objects.requireNonNull(activationOperation, "'activationOperation' cannot be null.");
        this.pollOperation = Objects.requireNonNull(pollOperation, "'pollOperation' cannot be null.");
        this.cancelOperation = Objects.requireNonNull(cancelOperation, "'cancelOperation' cannot be null.");
        this.fetchResultOperation
            = Objects.requireNonNull(fetchResultOperation, "'fetchResultOperation' cannot be null.");
        this.oneTimeActivationMono = createActivationMono(this.rootContext, activationOperation, Function.identity());
        this.syncActivationOperation = cxt -> activationOperation.apply(cxt).block();
    }

    /**
     * Creates a PollerFlux instance that returns an error on subscription.
     *
     * @param ex The exception to be returned on subscription of this {@link PollerFlux}.
     * @param <T> The type of poll response value.
     * @param <U> The type of the final result of long-running operation.
     * @return A poller flux instance that returns an error without emitting any data.
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
            throw LOGGER.logExceptionAsWarning(
                new IllegalArgumentException("Negative or zero value for 'pollInterval' is not allowed."));
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
        this.oneTimeActivationMono.flatMapMany(ignored -> {
            final PollResponse<T> activationResponse = this.rootContext.getActivationResponse();
            if (activationResponse.getStatus().isComplete()) {
                return Flux
                    .just(new AsyncPollResponse<>(this.rootContext, this.cancelOperation, this.fetchResultOperation));
            } else {
                return this.pollingLoop();
            }
        }).subscribe(actual);
    }

    /**
     * Gets a synchronous blocking poller.
     *
     * @return a synchronous blocking poller.
     */
    public SyncPoller<T, U> getSyncPoller() {
        return new SyncOverAsyncPoller<>(this.pollInterval, this.syncActivationOperation, this.pollOperation,
            this.cancelOperation, this.fetchResultOperation);
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
                .switchIfEmpty(Mono.error(() -> new IllegalStateException("PollOperation returned Mono.empty().")))
                .repeat()
                .takeUntil(currentPollResponse -> currentPollResponse.getStatus().isComplete())
                .concatMap(currentPollResponse -> {
                    cxt.setLatestResponse(currentPollResponse);
                    return Mono.just(new AsyncPollResponse<>(cxt, this.cancelOperation, this.fetchResultOperation));
                }),
            //
            // No cleaning needed, Polling Context will be GC-ed
            cxt -> {
            });
    }

    /**
     * Get the duration to wait before making next poll attempt.
     *
     * @param pollResponse the poll response to retrieve delay duration from
     * @return the delay
     */
    private Duration getDelay(PollResponse<T> pollResponse) {
        Duration retryAfter = pollResponse.getRetryAfter();
        return (retryAfter == null || retryAfter.isNegative() || retryAfter.isZero()) ? this.pollInterval : retryAfter;
    }

    private static <V, R> Mono<Boolean> createActivationMono(PollingContext<V> rootContext,
        Function<PollingContext<V>, Mono<R>> activationFunction,
        Function<R, PollResponse<V>> activationPollResponseMapper) {
        // Share the pending activation even if its first subscriber cancels, so an operation isn't started twice.
        // Cache successful activation indefinitely, but allow a later subscription to retry a failed activation.
        return Mono.defer(() -> activationFunction.apply(rootContext))
            .map(activationPollResponseMapper)
            .switchIfEmpty(Mono.fromSupplier(() -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED, null)))
            .map(activationResponse -> {
                rootContext.setOnetimeActivationResponse(activationResponse);
                return true;
            })
            .cache(ignored -> Duration.ofMillis(Long.MAX_VALUE), ignored -> Duration.ZERO, () -> Duration.ZERO);
    }
}
