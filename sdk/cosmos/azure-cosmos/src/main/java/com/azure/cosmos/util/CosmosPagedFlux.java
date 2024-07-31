// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.util;

import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.IterableStream;
import com.azure.core.util.paging.ContinuablePagedFlux;
import com.azure.cosmos.CosmosDiagnosticsContext;
import com.azure.cosmos.implementation.CosmosPagedFluxOptions;
import com.azure.cosmos.implementation.DiagnosticsProvider;
import com.azure.cosmos.implementation.FeedOperationState;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.models.FeedResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final ImplementationBridgeHelpers.CosmosDiagnosticsContextHelper.CosmosDiagnosticsContextAccessor ctxAccessor =
        ImplementationBridgeHelpers.CosmosDiagnosticsContextHelper.getCosmosDiagnosticsContextAccessor();

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

    private Flux<FeedResponse<T>>  wrapWithTracingIfEnabled(CosmosPagedFluxOptions pagedFluxOptions,
                                                             Flux<FeedResponse<T>> publisher,
                                                             AtomicLong feedResponseConsumerLatencyInNanos,
                                                             Context context) {
        FeedOperationState state = pagedFluxOptions.getFeedOperationState();
        DiagnosticsProvider tracerProvider = state != null ? state.getDiagnosticsProvider() : null;
        Object lockHolder = new Object();
        if (tracerProvider == null) {

            return publisher
                .doOnEach(signal -> {
                    FeedResponse<T> response = signal.get();
                    synchronized (lockHolder) {
                        switch (signal.getType()) {
                            case ON_COMPLETE:
                            case ON_NEXT:
                                DiagnosticsProvider.recordFeedResponse(
                                    feedResponseConsumer,
                                    pagedFluxOptions.getFeedOperationState(),
                                    () ->pagedFluxOptions.getSamplingRateSnapshot(),
                                    tracerProvider,
                                    response,
                                    feedResponseConsumerLatencyInNanos);
                                break;
                            default:
                                break;
                        }
                    }
                });
        }

        if (!tracerProvider.isEnabled()) {
            pagedFluxOptions.setSamplingRateSnapshot(0, true);
        }

        final boolean isSampledOut = tracerProvider.shouldSampleOutOperation(pagedFluxOptions);
        final double samplingRateSnapshot = pagedFluxOptions.getSamplingRateSnapshot();

        Flux<FeedResponse<T>> result = tracerProvider
            .runUnderSpanInContext(publisher)
            .doOnEach(signal -> {
                FeedResponse<T> response = signal.get();
                Context traceCtx = DiagnosticsProvider.getContextFromReactorOrNull(signal.getContextView());

                synchronized (lockHolder) {
                    switch (signal.getType()) {
                        case ON_COMPLETE:
                            if (response != null) {
                                DiagnosticsProvider.recordFeedResponse(
                                    feedResponseConsumer,
                                    pagedFluxOptions.getFeedOperationState(),
                                    () ->pagedFluxOptions.getSamplingRateSnapshot(),
                                    tracerProvider,
                                    response,
                                    feedResponseConsumerLatencyInNanos);
                            }
                            state.mergeDiagnosticsContext();

                            CosmosDiagnosticsContext ctxSnapshot = state.getDiagnosticsContextSnapshot();

                            ctxAccessor
                                .setSamplingRateSnapshot(ctxSnapshot, samplingRateSnapshot, isSampledOut);

                            tracerProvider.recordFeedResponseConsumerLatency(
                                signal,
                                ctxSnapshot,
                                Duration.ofNanos(feedResponseConsumerLatencyInNanos.get()));

                            tracerProvider.endSpan(ctxSnapshot, traceCtx, ctxAccessor.isEmptyCompletion(ctxSnapshot), isSampledOut);

                            break;
                        case ON_NEXT:
                            DiagnosticsProvider.recordFeedResponse(
                                feedResponseConsumer,
                                pagedFluxOptions.getFeedOperationState(),
                                () ->pagedFluxOptions.getSamplingRateSnapshot(),
                                tracerProvider,
                                response,
                                feedResponseConsumerLatencyInNanos);
                            state.mergeDiagnosticsContext();
                            CosmosDiagnosticsContext ctxSnapshotOnNext = state.getDiagnosticsContextSnapshot();
                            ctxAccessor
                                .setSamplingRateSnapshot(ctxSnapshotOnNext, samplingRateSnapshot, isSampledOut);
                            tracerProvider.endSpan(ctxSnapshotOnNext, traceCtx, false, isSampledOut);
                            state.resetDiagnosticsContext();

                            DiagnosticsProvider.setContextInReactor(tracerProvider.startSpan(
                                state.getSpanName(),
                                state.getDiagnosticsContextSnapshot(),
                                traceCtx,
                                isSampledOut));

                            break;

                        case ON_ERROR:
                            state.mergeDiagnosticsContext();
                            CosmosDiagnosticsContext ctxSnapshotOnError = state.getDiagnosticsContextSnapshot();
                            ctxAccessor
                                .setSamplingRateSnapshot(ctxSnapshotOnError, samplingRateSnapshot, isSampledOut);
                            tracerProvider.recordFeedResponseConsumerLatency(
                                signal,
                                ctxSnapshotOnError,
                                Duration.ofNanos(feedResponseConsumerLatencyInNanos.get()));

                            // all info is extracted from CosmosException when applicable
                            tracerProvider.endSpan(
                                state.getDiagnosticsContextSnapshot(),
                                traceCtx,
                                signal.getThrowable(),
                                isSampledOut
                            );

                            break;

                        default:
                            break;
                    }
                }
            });

        return Flux
            .deferContextual(reactorCtx -> result
                .doOnCancel(() -> {
                    Context traceCtx = DiagnosticsProvider.getContextFromReactorOrNull(reactorCtx);
                    synchronized (lockHolder) {
                        state.mergeDiagnosticsContext();
                        CosmosDiagnosticsContext ctxSnapshot = state.getDiagnosticsContextSnapshot();

                        ctxAccessor
                            .setSamplingRateSnapshot(ctxSnapshot, samplingRateSnapshot, isSampledOut);

                        tracerProvider.endSpan(ctxSnapshot, traceCtx, false, isSampledOut);
                    }
                })
                .doOnComplete(() -> {
                    Context traceCtx = DiagnosticsProvider.getContextFromReactorOrNull(reactorCtx);
                    synchronized(lockHolder) {
                        state.mergeDiagnosticsContext();

                        CosmosDiagnosticsContext ctxSnapshot = state.getDiagnosticsContextSnapshot();
                        ctxAccessor
                            .setSamplingRateSnapshot(ctxSnapshot, samplingRateSnapshot, isSampledOut);
                        tracerProvider.endSpan(ctxSnapshot, traceCtx, ctxAccessor.isEmptyCompletion(ctxSnapshot), isSampledOut);
                    }
                }))
            .contextWrite(DiagnosticsProvider.setContextInReactor(
                tracerProvider.startSpan(
                    state.getSpanName(),
                    state.getDiagnosticsContextSnapshot(),
                    context,
                    isSampledOut)
            ));
    }



    private Flux<FeedResponse<T>> byPage(CosmosPagedFluxOptions pagedFluxOptions, Context context) {
        AtomicReference<Instant> startTime = new AtomicReference<>();
        AtomicLong feedResponseConsumerLatencyInNanos = new AtomicLong(0);

        Flux<FeedResponse<T>> result =
            wrapWithTracingIfEnabled(
                pagedFluxOptions,
                this.optionsFluxFunction.apply(pagedFluxOptions),
                feedResponseConsumerLatencyInNanos,
                context)
                .doOnSubscribe(ignoredValue -> {
                    startTime.set(Instant.now());
                    feedResponseConsumerLatencyInNanos.set(0);
                });

        return result;
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
