// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.util.paging;

import com.typespec.core.util.IterableStream;

/**
 * Represents a page returned, this page may contain a reference to additional pages known as a continuation token.
 *
 * @param <C> Type of the continuation token.
 * @param <T> Type of the elements in the page.
 * @see ContinuablePagedFlux
 */
public interface ContinuablePage<C, T> {
    /**
     * Gets an {@link IterableStream} of elements in the page.
     *
     * @return An {@link IterableStream} containing the elements in the page.
     */
    IterableStream<T> getElements();

    /**
     * Gets the reference to the next page.
     *
     * @return The next page reference or {@code null} if there isn't a next page.
     */
    C getContinuationToken();
}
