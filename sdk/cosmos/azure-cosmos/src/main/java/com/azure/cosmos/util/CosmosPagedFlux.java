// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.util;

import com.azure.core.http.HttpHeader;
import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.IterableStream;
import com.azure.core.util.paging.ContinuablePagedFlux;
import com.azure.core.util.tracing.ProcessKind;
import com.azure.cosmos.implementation.CosmosPagedFluxOptions;
import com.azure.cosmos.implementation.TracerProvider;
import com.azure.cosmos.models.FeedResponse;
import io.netty.handler.codec.http.HttpConstants;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Signal;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

/**
 * Cosmos implementation of {@link ContinuablePagedFlux}.
 * <p>
 * This type is a Flux that provides the ability to operate on pages of type {@link FeedResponse}
 * and individual items in such pages. This type supports {@link String} type continuation tokens,
 * allowing for restarting from a previously-retrieved continuation token.
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

    CosmosPagedFlux(Function<CosmosPagedFluxOptions, Flux<FeedResponse<T>>> optionsFluxFunction) {
        this.optionsFluxFunction = optionsFluxFunction;
    }

    @Override
    public Flux<FeedResponse<T>> byPage() {
        CosmosPagedFluxOptions cosmosPagedFluxOptions = new CosmosPagedFluxOptions();
        return FluxUtil.fluxContext(context ->  byPage(cosmosPagedFluxOptions, context)).subscriberContext(reactorContext -> {
            Optional<String> master = reactorContext.getOrEmpty(TracerProvider.MASTER_CALL);
            if (master.isPresent()) {
                reactorContext = reactorContext.put(TracerProvider.NESTED_CALL, true);
            }
            return reactorContext.put(TracerProvider.MASTER_CALL, true);
        });
    }

    @Override
    public Flux<FeedResponse<T>> byPage(String continuationToken) {
        CosmosPagedFluxOptions cosmosPagedFluxOptions = new CosmosPagedFluxOptions();
        cosmosPagedFluxOptions.setRequestContinuation(continuationToken);

        return FluxUtil.fluxContext(context ->  byPage(cosmosPagedFluxOptions, context)).subscriberContext(reactorContext -> {
            Optional<String> master = reactorContext.getOrEmpty(TracerProvider.MASTER_CALL);
            if (master.isPresent()) {
                reactorContext = reactorContext.put(TracerProvider.NESTED_CALL, true);
            }
            return reactorContext.put(TracerProvider.MASTER_CALL, true);
        });
    }

    @Override
    public Flux<FeedResponse<T>> byPage(int preferredPageSize) {
        CosmosPagedFluxOptions cosmosPagedFluxOptions = new CosmosPagedFluxOptions();
        cosmosPagedFluxOptions.setMaxItemCount(preferredPageSize);

        return FluxUtil.fluxContext(context ->  byPage(cosmosPagedFluxOptions, context)).subscriberContext(reactorContext -> {
            Optional<String> master = reactorContext.getOrEmpty(TracerProvider.MASTER_CALL);
            if (master.isPresent()) {
                reactorContext = reactorContext.put(TracerProvider.NESTED_CALL, true);
            }
            return reactorContext.put(TracerProvider.MASTER_CALL, true);
        });
    }

    @Override
    public Flux<FeedResponse<T>> byPage(String continuationToken, int preferredPageSize) {
        CosmosPagedFluxOptions cosmosPagedFluxOptions = new CosmosPagedFluxOptions();
        cosmosPagedFluxOptions.setRequestContinuation(continuationToken);
        cosmosPagedFluxOptions.setMaxItemCount(preferredPageSize);

        return FluxUtil.fluxContext(context ->  byPage(cosmosPagedFluxOptions, context)).subscriberContext(reactorContext -> {
            Optional<String> master = reactorContext.getOrEmpty(TracerProvider.MASTER_CALL);
            if (master.isPresent()) {
                reactorContext = reactorContext.put(TracerProvider.NESTED_CALL, true);
            }
            return reactorContext.put(TracerProvider.MASTER_CALL, true);
        });
    }

    /**
     * Subscribe to consume all items of type {@code T} in the sequence respectively.
     * This is recommended for most common scenarios. This will seamlessly fetch next
     * page when required and provide with a {@link Flux} of items.
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
        final AtomicReference<Context> parentContext =  new AtomicReference<>(Context.NONE);

        return this.optionsFluxFunction.apply(pagedFluxOptions).doOnSubscribe(ignoredValue -> {
            if (pagedFluxOptions.getTracerProvider().isEnabled()) {
                reactor.util.context.Context reactorContext = FluxUtil.toReactorContext(context);
                Objects.requireNonNull(reactorContext.hasKey(TracerProvider.MASTER_CALL));
                Optional<Object> callerFunc = reactorContext.getOrEmpty(TracerProvider.NESTED_CALL);
                if (!callerFunc.isPresent()) {
                    parentContext.set(pagedFluxOptions.getTracerProvider().startSpan(pagedFluxOptions.getTracerSpanName(),
                        context.addData(TracerProvider.ATTRIBUTE_MAP, pagedFluxOptions.getTracingAttributes()), ProcessKind.DATABASE));
                }
            }
        }).doOnEach(responseSignal -> {
            if (pagedFluxOptions.getTracerProvider().isEnabled()) {
                pagedFluxOptions.getTracerProvider().endSpan(parentContext.get(), Signal.complete(), 200);
            }
        }).doOnError(throwable -> {
            if (pagedFluxOptions.getTracerProvider().isEnabled()) {
                pagedFluxOptions.getTracerProvider().endSpan(parentContext.get(), Signal.error(throwable), 0);
            }
        });
    }
}
