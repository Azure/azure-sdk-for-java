// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.paging;

import com.azure.core.util.IterableStream;

/**
 * Represents Page from service that has reference to next set of one or more pages,
 * such a reference is known as continuation token.
 *
 * @param <C> Type of the continuation token
 * @param <T> Type of the elements in the page
 *
 * @see ContinuablePagedFlux
 */
public interface ContinuablePage<C, T> {
    /**
     * Get an iterable stream of elements in the page.
     *
     * @return elements in the page as iterable stream.
     */
    IterableStream<T> getElements();

    /**
     * Get reference to the next page.
     *
     * @return next page reference, or {@code null} if there are no more pages.
     */
    C getContinuationToken();
}
