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
 * @param <T> the type of elements in the page
 * @param <P> the type of page
 *
 * @see Flux
 * @see ContinuablePage
 */
public abstract class ContinuablePagedFlux<C, T, P extends ContinuablePage<C, T>> extends Flux<T> {
    /**
     * @return a Flux of {@link ContinuablePage} in this Paged Flux.
     */
    public abstract Flux<P> byPage();
    /**
     * Get a Flux {@link ContinuablePage} identified by the given continuation token.
     *
     * @param continuationToken the continuation token
     * @return a Flux of {@link ContinuablePage}
     */
    public abstract Flux<P> byPage(C continuationToken);
    /**
     * Get a Flux of {@link ContinuablePage} in this Paged Flux, with each page containing
     * number of elements equal to the preferred page size.
     *
     * @param preferredPageSize the preferred page size, service may or may not honor the page
     *                          size preference hence client MUST be prepared to handle pages
     *                          with different page size.
     * @return a Flux of {@link ContinuablePage}
     */
    public abstract Flux<P> byPage(int preferredPageSize);
    /**
     * Get a Flux of {@link ContinuablePage} identified by the given continuation token, with each
     * page containing number of elements equal to the preferred page size.
     *
     * @param continuationToken the continuation token
     * @param preferredPageSize the preferred page size, service may or may not honor the page
     *                          size preference hence client MUST be prepared to handle pages
     *                          with different page size.
     * @return a Flux of {@link ContinuablePage}
     */
    public abstract Flux<P> byPage(C continuationToken, int preferredPageSize);
}
