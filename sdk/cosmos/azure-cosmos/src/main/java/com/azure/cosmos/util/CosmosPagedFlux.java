// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.util;

import com.azure.core.util.IterableStream;
import com.azure.core.util.paging.ContinuablePagedFlux;
import com.azure.cosmos.implementation.CosmosPagedFluxOptions;
import com.azure.cosmos.models.FeedResponse;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.function.Consumer;

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
public class CosmosPagedFlux<T> extends ContinuablePagedFlux<String, T, FeedResponse<T>> {
    // Ensure there can only be package-internal implementations
    CosmosPagedFlux() {}

    @Override
    public Flux<FeedResponse<T>> byPage() {
        throw new UnsupportedOperationException("Has to be overridden in child classes.");
    }

    @Override
    public Flux<FeedResponse<T>> byPage(String s) {
        throw new UnsupportedOperationException("Has to be overridden in child classes.");
    }

    @Override
    public Flux<FeedResponse<T>> byPage(int i) {
        throw new UnsupportedOperationException("Has to be overridden in child classes.");
    }

    @Override
    public Flux<FeedResponse<T>> byPage(String s, int i) {
        throw new UnsupportedOperationException("Has to be overridden in child classes.");
    }

    public CosmosPagedFlux<T> handle(Consumer<FeedResponse<T>> newFeedResponseConsumer) {
        throw new UnsupportedOperationException("Has to be overridden in child classes.");
    }

    CosmosPagedFlux<T> withDefaultPageSize(int pageSize) {
        throw new UnsupportedOperationException("Has to be overridden in child classes.");
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

    /**
     * Creates an instance of a CosmosPagedFlux for mocking purposes or when injecting CosmosPagedFlux
     * instances from a different data source
     * @param items - the list of items to be returned
     * @param isChangeFeed - a flag incidcating whether the CosmsoPagedFluy will be returned from a change feed API
     * @return an instance of CosmosPagedFlux
     * @param <T> The type of the items
     */
    public static <T> CosmosPagedFlux<T> createFromList(List<T> items, boolean isChangeFeed) {
        return new CosmosPagedFluxStaticListImpl<>(
            items,
            isChangeFeed
        );
    }
}
