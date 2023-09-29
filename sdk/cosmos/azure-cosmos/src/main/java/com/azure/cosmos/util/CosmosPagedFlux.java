// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.util;

import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.IterableStream;
import com.azure.core.util.paging.ContinuablePagedFlux;
import com.azure.cosmos.CosmosDiagnostics;
import com.azure.cosmos.implementation.CosmosPagedFluxOptions;
import com.azure.cosmos.implementation.DiagnosticsProvider;
import com.azure.cosmos.implementation.FeedOperationState;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.models.FeedResponse;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Cosmos implementation of {@link ContinuablePagedFlux}.
 * <p>
 * This type is a Flux that provides the ability to operate on pages of type {@link FeedResponse} and individual items
 * in such pages. This type supports {@link String} type continuation tokens, allowing for restarting from a
 * previously-retrieved continuation token.
 * <p>
 * For more information on the base type, refer {@link ContinuablePagedFlux}
 *
 * @param <T> The type of elements in a {@link com.azure.core.util.paging.ContinuablePage}
 * @see com.azure.core.util.paging.ContinuablePage
 * @see CosmosPagedFluxOptions
 * @see FeedResponse
 */
public final class CosmosPagedFlux<T> extends ContinuablePagedFlux<String, T, FeedResponse<T>> {

    private final static ImplementationBridgeHelpers.CosmosDiagnosticsHelper.CosmosDiagnosticsAccessor cosmosDiagnosticsAccessor =
        ImplementationBridgeHelpers.CosmosDiagnosticsHelper.getCosmosDiagnosticsAccessor();

    private final Function<CosmosPagedFluxOptions, Flux<FeedResponse<T>>> optionsFluxFunction;
    private final Consumer<FeedResponse<T>> feedResponseConsumer;
    private final int defaultPageSize;

    CosmosPagedFlux(Function<CosmosPagedFluxOptions, Flux<FeedResponse<T>>> optionsFluxFunction) {
        this(optionsFluxFunction, null, -1);
    }

    CosmosPagedFlux(Function<CosmosPagedFluxOptions, Flux<FeedResponse<T>>> optionsFluxFunction,
                    Consumer<FeedResponse<T>> feedResponseConsumer) {
        this(optionsFluxFunction, feedResponseConsumer, -1);
    }

    CosmosPagedFlux(Function<CosmosPagedFluxOptions, Flux<FeedResponse<T>>> optionsFluxFunction,
                    Consumer<FeedResponse<T>> feedResponseConsumer,
                    int defaultPageSize) {
        this.optionsFluxFunction = optionsFluxFunction;
        this.feedResponseConsumer = feedResponseConsumer;
        this.defaultPageSize = defaultPageSize;
    }

    /**
     * Handle for invoking "side-effects" on each FeedResponse returned by CosmosPagedFlux
     *
     * @param newFeedResponseConsumer handler
     * @return CosmosPagedFlux instance with attached handler
     */
    public CosmosPagedFlux<T> handle(Consumer<FeedResponse<T>> newFeedResponseConsumer) {
        if (this.feedResponseConsumer != null) {
            return new CosmosPagedFlux<>(
                this.optionsFluxFunction,
                this.feedResponseConsumer.andThen(newFeedResponseConsumer));
        } else {
            return new CosmosPagedFlux<>(this.optionsFluxFunction, newFeedResponseConsumer);
        }
    }

    @Override
    public Flux<FeedResponse<T>> byPage() {
        CosmosPagedFluxOptions cosmosPagedFluxOptions = this.createCosmosPagedFluxOptions();
        return FluxUtil.fluxContext(context -> byPage(cosmosPagedFluxOptions, context));
    }

    @Override
    public Flux<FeedResponse<T>> byPage(String continuationToken) {
        CosmosPagedFluxOptions cosmosPagedFluxOptions = this.createCosmosPagedFluxOptions();
        cosmosPagedFluxOptions.setRequestContinuation(continuationToken);
        return FluxUtil.fluxContext(context -> byPage(cosmosPagedFluxOptions, context));
    }

    @Override
    public Flux<FeedResponse<T>> byPage(int preferredPageSize) {
        CosmosPagedFluxOptions cosmosPagedFluxOptions = this.createCosmosPagedFluxOptions();
        cosmosPagedFluxOptions.setMaxItemCount(preferredPageSize);
        return FluxUtil.fluxContext(context -> byPage(cosmosPagedFluxOptions, context));
    }

    @Override
    public Flux<FeedResponse<T>> byPage(String continuationToken, int preferredPageSize) {
        CosmosPagedFluxOptions cosmosPagedFluxOptions = this.createCosmosPagedFluxOptions();
        cosmosPagedFluxOptions.setRequestContinuation(continuationToken);
        cosmosPagedFluxOptions.setMaxItemCount(preferredPageSize);
        return FluxUtil.fluxContext(context -> byPage(cosmosPagedFluxOptions, context));
    }

    /**
     * Subscribe to consume all items of type {@code T} in the sequence respectively. This is recommended for most
     * common scenarios. This will seamlessly fetch next page when required and provide with a {@link Flux} of items.
     *
     * @param coreSubscriber The subscriber for this {@link CosmosPagedFlux}
     */
    @Override
    public void subscribe(@SuppressWarnings("NullableProblems") CoreSubscriber<? super T> coreSubscriber) {
        Flux<FeedResponse<T>> pagedResponse = this.byPage();
        pagedResponse.flatMap(tFeedResponse -> {
            IterableStream<T> elements = tFeedResponse.getElements();
            if (elements == null) {
                return Flux.empty();
            }
            return Flux.fromIterable(elements);
        }).subscribe(coreSubscriber);
    }

    CosmosPagedFlux<T> withDefaultPageSize(int pageSize) {
        return new CosmosPagedFlux<>(this.optionsFluxFunction, this.feedResponseConsumer, pageSize);
    }

    private CosmosPagedFluxOptions createCosmosPagedFluxOptions() {
        CosmosPagedFluxOptions cosmosPagedFluxOptions = new CosmosPagedFluxOptions();

        if (this.defaultPageSize > 0) {
            cosmosPagedFluxOptions.setMaxItemCount(this.defaultPageSize);
        }

        return cosmosPagedFluxOptions;
    }

    private <TOutput> Flux<TOutput> wrapWithTracingIfEnabled(CosmosPagedFluxOptions pagedFluxOptions, Flux<TOutput> publisher) {
        FeedOperationState stateSnapshot = pagedFluxOptions.getFeedOperationState();
        DiagnosticsProvider tracerProvider = stateSnapshot != null ? stateSnapshot.getDiagnosticsProvider() : null;
        if (tracerProvider == null ||
            !tracerProvider.isEnabled()) {

            return publisher;
        }

        return tracerProvider.runUnderSpanInContext(publisher, pagedFluxOptions);
    }

    private void recordFeedResponse(
        CosmosPagedFluxOptions pagedFluxOptions,
        DiagnosticsProvider tracerProvider,
        FeedResponse<T> response,
        AtomicLong feedResponseConsumerLatencyInNanos) {

        CosmosDiagnostics diagnostics = response != null ? response.getCosmosDiagnostics() : null;

        Integer actualItemCount = response != null && response.getResults() != null ?
            response.getResults().size() : null;

        if (diagnostics != null &&
            cosmosDiagnosticsAccessor
                .isDiagnosticsCapturedInPagedFlux(diagnostics)
                .compareAndSet(false, true)) {

            if (pagedFluxOptions.getSamplingRateSnapshot() < 1) {
                cosmosDiagnosticsAccessor
                    .setSamplingRateSnapshot(diagnostics, pagedFluxOptions.getSamplingRateSnapshot());
            }

            if (isTracerEnabled(tracerProvider)) {
                tracerProvider.recordPage(
                    pagedFluxOptions.getFeedOperationState().getDiagnosticsContextSnapshot(),
                    diagnostics,
                    actualItemCount,
                    response.getRequestCharge());
            }

            //  If the user has passed feedResponseConsumer, then call it with each feedResponse
            if (feedResponseConsumer != null) {
                // NOTE this call is happening in a span counted against client telemetry / metric latency
                // So, the latency of the user's callback is accumulated here to correct the latency
                // reported to client telemetry and client metrics
                Instant feedResponseConsumerStart = Instant.now();
                feedResponseConsumer.accept(response);
                feedResponseConsumerLatencyInNanos.addAndGet(
                    Duration.between(Instant.now(), feedResponseConsumerStart).toNanos());
            }
        }
    }

    private Flux<FeedResponse<T>> byPage(CosmosPagedFluxOptions pagedFluxOptions, Context context) {
        AtomicReference<Instant> startTime = new AtomicReference<>();
        AtomicLong feedResponseConsumerLatencyInNanos = new AtomicLong(0);
        Object lockHolder = new Object();

        Flux<FeedResponse<T>> result =
            wrapWithTracingIfEnabled(
                pagedFluxOptions, this.optionsFluxFunction.apply(pagedFluxOptions))
            .doOnSubscribe(ignoredValue -> {
                startTime.set(Instant.now());
                feedResponseConsumerLatencyInNanos.set(0);
            })
            .doOnEach(signal -> {

                FeedResponse<T> response = signal.get();
                Context traceCtx = DiagnosticsProvider.getContextFromReactorOrNull(signal.getContextView());

                FeedOperationState state = pagedFluxOptions.getFeedOperationState();
                DiagnosticsProvider tracerProvider = state != null ? state.getDiagnosticsProvider() : null;

                synchronized (lockHolder) {
                    switch (signal.getType()) {
                        case ON_COMPLETE:
                            this.recordFeedResponse(pagedFluxOptions, tracerProvider, response, feedResponseConsumerLatencyInNanos);

                            if (isTracerEnabled(tracerProvider)) {
                                state.mergeDiagnosticsContext();
                                tracerProvider.recordFeedResponseConsumerLatency(
                                    signal,
                                    state.getDiagnosticsContextSnapshot(),
                                    Duration.ofNanos(feedResponseConsumerLatencyInNanos.get()));

                                tracerProvider.endSpan(state.getDiagnosticsContextSnapshot(), traceCtx);
                            }

                            break;
                        case ON_NEXT:
                            this.recordFeedResponse(pagedFluxOptions, tracerProvider, response, feedResponseConsumerLatencyInNanos);

                            if (isTracerEnabled(tracerProvider)) {
                                state.mergeDiagnosticsContext();
                                tracerProvider.endSpan(state.getDiagnosticsContextSnapshot(), traceCtx);
                                state.resetDiagnosticsContext();

                                DiagnosticsProvider.setContextInReactor(tracerProvider.startSpan(
                                    state.getSpanName(),
                                    state.getDiagnosticsContextSnapshot(),
                                    traceCtx));
                            }

                            break;

                        case ON_ERROR:
                            if (isTracerEnabled(tracerProvider)) {
                                state.mergeDiagnosticsContext();
                                tracerProvider.recordFeedResponseConsumerLatency(
                                    signal,
                                    state.getDiagnosticsContextSnapshot(),
                                    Duration.ofNanos(feedResponseConsumerLatencyInNanos.get()));

                                // all info is extracted from CosmosException when applicable
                                tracerProvider.endSpan(
                                    state.getDiagnosticsContextSnapshot(),
                                    traceCtx,
                                    signal.getThrowable()
                                );
                            }

                            break;

                        default:
                            break;
                    }
                }
            });

        final FeedOperationState state = pagedFluxOptions.getFeedOperationState();
        final DiagnosticsProvider tracerProvider = state != null ? state.getDiagnosticsProvider() : null;
        if (isTracerEnabled(tracerProvider)) {
            return Flux
                .deferContextual(reactorCtx -> result
                    .doOnCancel(() -> {
                        Context traceCtx = DiagnosticsProvider.getContextFromReactorOrNull(reactorCtx);
                        synchronized (lockHolder) {
                            state.mergeDiagnosticsContext();
                            tracerProvider.endSpan(state.getDiagnosticsContextSnapshot(), traceCtx);
                        }
                    })
                    .doOnComplete(() -> {
                        Context traceCtx = DiagnosticsProvider.getContextFromReactorOrNull(reactorCtx);
                        synchronized(lockHolder) {
                            state.mergeDiagnosticsContext();
                            tracerProvider.endSpan(state.getDiagnosticsContextSnapshot(), traceCtx);
                        }
                    }))
                .contextWrite(DiagnosticsProvider.setContextInReactor(
                    tracerProvider.startSpan(
                    state.getSpanName(),
                    state.getDiagnosticsContextSnapshot(),
                    context)
                ));

        }

        return result;
    }

    private boolean isTracerEnabled(DiagnosticsProvider tracerProvider) {
        return tracerProvider != null;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    // the following helper/accessor only helps to access this class outside of this package.//
    ///////////////////////////////////////////////////////////////////////////////////////////
    static void initialize() {
        ImplementationBridgeHelpers.CosmosPageFluxHelper.setCosmosPageFluxAccessor(
            (ImplementationBridgeHelpers.CosmosPageFluxHelper.CosmosPageFluxAccessor) CosmosPagedFlux::new);
    }

    static { initialize(); }
}
