// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.util;

import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.paging.ContinuablePagedFlux;
import com.azure.cosmos.CosmosDiagnosticsContext;
import com.azure.cosmos.implementation.CosmosPagedFluxOptions;
import com.azure.cosmos.implementation.DiagnosticsProvider;
import com.azure.cosmos.implementation.FeedOperationState;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.models.FeedResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
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
final class CosmosPagedFluxDefaultImpl<T> extends CosmosPagedFlux<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(CosmosPagedFluxStaticListImpl.class);
    private static final ImplementationBridgeHelpers.CosmosDiagnosticsContextHelper.CosmosDiagnosticsContextAccessor ctxAccessor =
        ImplementationBridgeHelpers.CosmosDiagnosticsContextHelper.getCosmosDiagnosticsContextAccessor();

    private final Function<CosmosPagedFluxOptions, Flux<FeedResponse<T>>> optionsFluxFunction;
    private final AtomicReference<Consumer<FeedResponse<T>>> feedResponseConsumer;
    private final AtomicInteger defaultPageSize;

    CosmosPagedFluxDefaultImpl(Function<CosmosPagedFluxOptions, Flux<FeedResponse<T>>> optionsFluxFunction) {
        this(optionsFluxFunction, null, -1);
    }

    CosmosPagedFluxDefaultImpl(Function<CosmosPagedFluxOptions, Flux<FeedResponse<T>>> optionsFluxFunction,
                    Consumer<FeedResponse<T>> feedResponseConsumer) {
        this(optionsFluxFunction, feedResponseConsumer, -1);
    }

    CosmosPagedFluxDefaultImpl(Function<CosmosPagedFluxOptions, Flux<FeedResponse<T>>> optionsFluxFunction,
                    Consumer<FeedResponse<T>> feedResponseConsumer,
                    int defaultPageSize) {
        super();
        this.optionsFluxFunction = optionsFluxFunction;
        this.feedResponseConsumer = new AtomicReference<>(feedResponseConsumer);
        this.defaultPageSize = new AtomicInteger(defaultPageSize);
    }

    /**
     * Handle for invoking "side-effects" on each FeedResponse returned by CosmosPagedFlux
     *
     * @param newFeedResponseConsumer handler
     * @return CosmosPagedFlux instance with attached handler
     */
    public CosmosPagedFlux<T> handle(Consumer<FeedResponse<T>> newFeedResponseConsumer) {
        int i = 0;
        while (true) {
            Consumer<FeedResponse<T>> feedResponseConsumerSnapshot = this.feedResponseConsumer.get();
            i++;
            if (feedResponseConsumerSnapshot != null) {

                if (this.feedResponseConsumer.compareAndSet(
                    feedResponseConsumerSnapshot, feedResponseConsumerSnapshot.andThen(newFeedResponseConsumer))) {

                    break;
                }
            } else {
                if (this.feedResponseConsumer.compareAndSet(
                    null,
                    newFeedResponseConsumer)) {

                    break;
                }
            }

            if (i > 10) {
                LOGGER.warn("Highly concurrent calls to CosmosPagedFlux.handle "
                    + "are not expected and can result in perf regressions. Avoid this by reducing concurrency.");
            }
        }

        return this;
    }

    @Override
    CosmosPagedFlux<T> withDefaultPageSize(int pageSize) {
        this.defaultPageSize.set(pageSize);
        return this;
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

    private CosmosPagedFluxOptions createCosmosPagedFluxOptions() {
        CosmosPagedFluxOptions cosmosPagedFluxOptions = new CosmosPagedFluxOptions();

        int defaultPageSizeSnapshot = this.defaultPageSize.get();
        if (defaultPageSizeSnapshot > 0) {
            cosmosPagedFluxOptions.setMaxItemCount(defaultPageSizeSnapshot);
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
                                    feedResponseConsumer.get(),
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
                                    feedResponseConsumer.get(),
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
                                feedResponseConsumer.get(),
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
            (ImplementationBridgeHelpers.CosmosPageFluxHelper.CosmosPageFluxAccessor) CosmosPagedFluxDefaultImpl::new);
    }

    static { initialize(); }
}
