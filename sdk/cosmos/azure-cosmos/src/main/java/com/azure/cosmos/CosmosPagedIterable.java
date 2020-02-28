package com.azure.cosmos;

import com.azure.core.util.paging.ContinuablePagedFlux;

/**
 * Cosmos implementation of {@link ContinuablePagedIterable}.
 * <p>
 * This type is a {@link com.azure.core.util.IterableStream} that provides the ability to operate on pages of type {@link FeedResponse}
 * and individual items in such pages. This type supports {@link String} type continuation tokens,
 * allowing for restarting from a previously-retrieved continuation token.
 * <p>
 * For more information on the base type, refer {@link ContinuablePagedIterable}
 *
 * @param <T> The type of elements in a {@link com.azure.core.util.paging.ContinuablePage}
 * @see com.azure.core.util.paging.ContinuablePage
 * @see FeedResponse
 */
public class CosmosPagedIterable<T> extends ContinuablePagedIterable<String, T, FeedResponse<T>> {

    /**
     * Creates instance given {@link ContinuablePagedFlux}.
     *
     * @param pagedFlux the paged flux use as iterable
     */
    CosmosPagedIterable(ContinuablePagedFlux<String, T, FeedResponse<T>> pagedFlux) {
        super(pagedFlux);
    }
}
