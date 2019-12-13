// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.paging;

import reactor.core.publisher.Flux;

/**
 * A contract that represents a Flux that provides the ability to operate on pages of type
 * {@link ContinuablePage} and individual items in such pages. This type supports user-provided
 * continuation tokens, allowing for restarting from a previously-retrieved continuation token.
 *
 * @param <C> the type of continuation token
 * @param <T> the type of items in the page
 * @param <P> the type of page
 *
 * @see Flux
 * @see ContinuablePage
 */
public abstract class ContinuablePagedFlux<C, T, P extends ContinuablePage<C, T>> extends Flux<T> {
    /**
     * @return a Flux that emits stream of {@link ContinuablePage} in this Paged Flux.
     */
    public abstract Flux<P> byPage();
    /**
     * Get a Flux that emits stream of {@link ContinuablePage} identified by the given
     * continuation token.
     *
     * @param continuationToken the continuation token
     * @return a Flux of {@link ContinuablePage}
     */
    public abstract Flux<P> byPage(C continuationToken);
}
