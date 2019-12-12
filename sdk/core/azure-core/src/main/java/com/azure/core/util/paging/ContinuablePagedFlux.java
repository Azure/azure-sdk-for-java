// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.paging;

import reactor.core.publisher.Flux;

/**
 * A contract that represents a Flux that provides the ability to operate on individual items
 * in pages of type {@link ContinuablePage}, also provide ability to operate on individual
 * pages.
 *
 * @param <O> the type of continuation option for byPage
 * @param <C> the type of continuation token
 * @param <T> the type of items in the page
 * @param <P> the type of page
 */
public abstract class ContinuablePagedFlux<O, C, T, P extends ContinuablePage<C, T>> extends Flux<T> {
    /**
     * @return a Flux of {@link ContinuablePage} that this Paged Flux represents.
     */
    public abstract Flux<P> byPage();
    /**
     * @return a Flux of {@link ContinuablePage} identified by the given continuation option.
     */
    public abstract Flux<P> byPage(O continuationOption);
}
