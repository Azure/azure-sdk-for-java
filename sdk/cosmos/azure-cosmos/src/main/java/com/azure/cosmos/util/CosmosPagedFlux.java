// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.util;

import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.IterableStream;
import com.azure.core.util.paging.ContinuablePagedFlux;
import com.azure.cosmos.implementation.CosmosPagedFluxOptions;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.TracerProvider;
import com.azure.cosmos.models.FeedResponse;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Signal;

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

    private final Function<CosmosPagedFluxOptions, Flux<FeedResponse<T>>> optionsFluxFunction;

    private final Consumer<FeedResponse<T>> feedResponseConsumer;

    CosmosPagedFlux(Function<CosmosPagedFluxOptions, Flux<FeedResponse<T>>> optionsFluxFunction) {
        this.optionsFluxFunction = optionsFluxFunction;
        this.feedResponseConsumer = null;
    }

    CosmosPagedFlux(Function<CosmosPagedFluxOptions, Flux<FeedResponse<T>>> optionsFluxFunction,
                    Consumer<FeedResponse<T>> feedResponseConsumer) {
        this.optionsFluxFunction = optionsFluxFunction;
        this.feedResponseConsumer = feedResponseConsumer;
    }

    /**
     * Handle for invoking "side-effects" on each FeedResponse returned by CosmosPagedFlux
     *
     * @param feedResponseConsumer handler
     * @return CosmosPagedFlux instance with attached handler
     */
    @Beta(value = Beta.SinceVersion.V4_6_0)
    public CosmosPagedFlux<T> handle(Consumer<FeedResponse<T>> feedResponseConsumer) {
        return new CosmosPagedFlux<T>(this.optionsFluxFunction, feedResponseConsumer);
    }

    @Override
    public Flux<FeedResponse<T>> byPage() {
        CosmosPagedFluxOptions cosmosPagedFluxOptions = new CosmosPagedFluxOptions();
        return FluxUtil.fluxContext(context -> byPage(cosmosPagedFluxOptions, context));
    }

    @Override
    public Flux<FeedResponse<T>> byPage(String continuationToken) {
        CosmosPagedFluxOptions cosmosPagedFluxOptions = new CosmosPagedFluxOptions();
        cosmosPagedFluxOptions.setRequestContinuation(continuationToken);
        return FluxUtil.fluxContext(context -> byPage(cosmosPagedFluxOptions, context));
    }

    @Override
    public Flux<FeedResponse<T>> byPage(int preferredPageSize) {
        CosmosPagedFluxOptions cosmosPagedFluxOptions = new CosmosPagedFluxOptions();
        cosmosPagedFluxOptions.setMaxItemCount(preferredPageSize);
        return FluxUtil.fluxContext(context -> byPage(cosmosPagedFluxOptions, context));
    }

    @Override
    public Flux<FeedResponse<T>> byPage(String continuationToken, int preferredPageSize) {
        CosmosPagedFluxOptions cosmosPagedFluxOptions = new CosmosPagedFluxOptions();
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
    public void subscribe(CoreSubscriber<? super T> coreSubscriber) {
        Flux<FeedResponse<T>> pagedResponse = this.byPage();
        pagedResponse.flatMap(tFeedResponse -> {
            IterableStream<T> elements = tFeedResponse.getElements();
            if (elements == null) {
                return Flux.empty();
            }
            return Flux.fromIterable(elements);
        }).subscribe(coreSubscriber);
    }

    private Flux<FeedResponse<T>> byPage(CosmosPagedFluxOptions pagedFluxOptions, Context context) {
        final AtomicReference<Context> parentContext = new AtomicReference<>(Context.NONE);
        return this.optionsFluxFunction.apply(pagedFluxOptions).doOnSubscribe(ignoredValue -> {
            if (pagedFluxOptions.getTracerProvider().isEnabled()) {
                parentContext.set(pagedFluxOptions.getTracerProvider().startSpan(pagedFluxOptions.getTracerSpanName(),
                    pagedFluxOptions.getDatabaseId(), pagedFluxOptions.getServiceEndpoint(),
                    context));
            }
        }).doOnComplete(() -> {
            if (pagedFluxOptions.getTracerProvider().isEnabled()) {
                pagedFluxOptions.getTracerProvider().endSpan(parentContext.get(), Signal.complete(),
                    HttpConstants.StatusCodes.OK);
            }
        }).doOnError(throwable -> {
            if (pagedFluxOptions.getTracerProvider().isEnabled()) {
                pagedFluxOptions.getTracerProvider().endSpan(parentContext.get(), Signal.error(throwable),
                    TracerProvider.ERROR_CODE);
            }
        }).doOnNext(feedResponse -> {
            //  If the user has passed feedResponseConsumer, then call it with each feedResponse
            if (feedResponseConsumer != null) {
                feedResponseConsumer.accept(feedResponse);
            }
        });
    }
}
