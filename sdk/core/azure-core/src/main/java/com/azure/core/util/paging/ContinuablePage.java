// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.paging;

import com.azure.core.util.IterableStream;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents Page from service that has reference (a.k.a continuation token) to next set
 * of one or more pages.
 *
 * @param <C> Type of the continuation token
 * @param <T> Type of the items in the page
 *
 * @see ContinuablePagedFlux
 */
public interface ContinuablePage<C, T> {
    /**
     * @return a iterable stream of elements in the page.
     */
    IterableStream<T> getElements();

    /**
     * @return list of elements in the page.
     *
     * @deprecated use {@link this#getElements()}.
     */
    @Deprecated
    default List<T> getItems() {
        return this.getElements().stream().collect(Collectors.toList());
    }

    /**
     * @return A reference to the next page, or {@code null} if there are no more pages.
     */
    C getContinuationToken();
}
