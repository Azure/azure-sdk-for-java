// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.util;

import com.azure.core.util.paging.ContinuablePagedIterable;
import com.azure.cosmos.models.FeedResponse;

import java.util.function.Consumer;

/**
 * Cosmos implementation of {@link com.azure.core.util.paging.ContinuablePagedIterable}.
 * <p>
 * This type is a {@link com.azure.core.util.IterableStream} that provides the ability to operate on pages of type
 * {@link FeedResponse} and individual items in such pages. This type supports {@link String} type continuation tokens,
 * allowing for restarting from a previously-retrieved continuation token.
 * <p>
 * For more information on the base type, refer {@link com.azure.core.util.paging.ContinuablePagedIterable}
 *
 * @param <T> The type of elements in a {@link com.azure.core.util.paging.ContinuablePage}
 * @see com.azure.core.util.paging.ContinuablePage
 * @see FeedResponse
 */
public final class CosmosPagedIterable<T> extends ContinuablePagedIterable<String, T, FeedResponse<T>> {
    private static final int SMALLEST_POSSIBLE_QUEUE_SIZE_LARGER_THAN_ONE = 8;
    private final CosmosPagedFlux<T> cosmosPagedFlux;

    /**
     * Creates instance given {@link CosmosPagedFlux}.
     *
     * @param cosmosPagedFlux the paged flux use as iterable
     */
    public CosmosPagedIterable(CosmosPagedFlux<T> cosmosPagedFlux) {
        super(cosmosPagedFlux);
        this.cosmosPagedFlux = cosmosPagedFlux;
    }

    /**
     * Creates instance given {@link CosmosPagedFlux}.
     *
     * @param cosmosPagedFlux the paged flux use as iterable
     * @param pageSize the preferred pageSize to be used when pulling data from the service
     */
    public CosmosPagedIterable(CosmosPagedFlux<T> cosmosPagedFlux, int pageSize) {
        this(null, cosmosPagedFlux, pageSize, 1);
    }

    /**
     * Creates instance given {@link CosmosPagedFlux}.
     *
     * @param cosmosPagedFlux the paged flux use as iterable
     * @param pageSize the preferred pageSize to be used when pulling data from the service
     * @param pagePrefetchCount the number of pages prefetched from the paged flux - note that this might be interpolated
     * by Reactor - for example all numbers &gt; 1 but &lt; 8 will result in at least prefetching 8 pages. See
     * `reactor.util.concurrent.Queues.get(int)` for more details
     */
    public CosmosPagedIterable(CosmosPagedFlux<T> cosmosPagedFlux, int pageSize, int pagePrefetchCount) {
        this(null, cosmosPagedFlux, pageSize, pagePrefetchCount);
    }

    private CosmosPagedIterable(
        @SuppressWarnings("unused") Object dummy,
        CosmosPagedFlux<T> cosmosPagedFlux,
        int pageSize,
        int pagePrefetchCount) {

        super(cosmosPagedFlux.withDefaultPageSize(pageSize), pagePrefetchCount);
        this.cosmosPagedFlux = cosmosPagedFlux;
    }

    /**
     * Handle for invoking "side-effects" on each FeedResponse returned by CosmosPagedIterable
     *
     * @param feedResponseConsumer handler
     * @return CosmosPagedIterable instance with attached handler
     */
    public CosmosPagedIterable<T> handle(Consumer<FeedResponse<T>> feedResponseConsumer) {
        CosmosPagedFlux<T> cosmosPagedFlux = this.cosmosPagedFlux.handle(feedResponseConsumer);
        return new CosmosPagedIterable<>(cosmosPagedFlux);
    }
}
