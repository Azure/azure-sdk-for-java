// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.paging;

import reactor.core.publisher.Flux;

/**
 * This class is a {@link Flux} implementation that provides the ability to operate on pages of type
 * {@link ContinuablePage} and individual items in such pages. This type supports user-provided continuation tokens,
 * allowing for restarting from a previously-retrieved continuation token.
 *
 * @param <C> Type of the continuation token.
 * @param <T> Type of the elements in the page.
 * @param <P> Type of the page.
 * @see Flux
 * @see ContinuablePage
 */
public abstract class ContinuablePagedFlux<C, T, P extends ContinuablePage<C, T>> extends Flux<T> {
    /**
     * Gets a {@link Flux} of {@link ContinuablePage} starting at the first page.
     *
     * @return A {@link Flux} of {@link ContinuablePage}.
     */
    public abstract Flux<P> byPage();

    /**
     * Gets a {@link Flux} of {@link ContinuablePage} beginning at the page identified by the given continuation token.
     *
     * @param continuationToken A continuation token identifying the page to select.
     * @return A {@link Flux} of {@link ContinuablePage}.
     */
    public abstract Flux<P> byPage(C continuationToken);

    /**
     * Gets a {@link Flux} of {@link ContinuablePage} starting at the first page requesting each page to contain a
     * number of elements equal to the preferred page size.
     * <p>
     * The service may or may not honor the preferred page size therefore the client <em>MUST</em> be prepared to handle
     * pages with different page sizes.
     *
     * @param preferredPageSize The preferred page size.
     * @return A {@link Flux} of {@link ContinuablePage}.
     */
    public abstract Flux<P> byPage(int preferredPageSize);

    /**
     * Gets a {@link Flux} of {@link ContinuablePage} beginning at the page identified by the given continuation token
     * requesting each page to contain the number of elements equal to the preferred page size.
     * <p>
     * The service may or may not honor the preferred page size therefore the client <em>MUST</em> be prepared to handle
     * pages with different page sizes.
     *
     * @param continuationToken A continuation token identifying the page to select.
     * @param preferredPageSize The preferred page size.
     * @return A {@link Flux} of {@link ContinuablePage}.
     */
    public abstract Flux<P> byPage(C continuationToken, int preferredPageSize);
}
