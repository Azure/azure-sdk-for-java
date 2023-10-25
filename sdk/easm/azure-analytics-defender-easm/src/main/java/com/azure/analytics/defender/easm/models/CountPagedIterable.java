// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.analytics.defender.easm.models;

import com.azure.core.http.rest.PagedIterableBase;
import com.azure.core.http.rest.PagedResponse;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 Custom paged iterable to be used in list operations with the additional totalElements property
 T: Resource used in the list operation
 */
public class CountPagedIterable<T> extends PagedIterableBase<T, PagedResponse<T>> {

    /**
     * The total number of elements in the entire collection.
     */
    private final Long totalElements;

    /**
     * Constructs a new CountPagedIterable with the provided retrievers for fetching the first page
     * of elements and the subsequent pages of elements.
     * @param firstPageRetriever A Supplier that retrieves the first page of elements in the collection.
     * @param nextPageRetriever A Function that retrieves the next page of elements in the collection.
     */
    public CountPagedIterable(Supplier<CountPagedResponse<T>> firstPageRetriever, Function<String, CountPagedResponse<T>> nextPageRetriever) {
        super(() -> (continuationToken, pageSize) ->
            continuationToken == null
                ? firstPageRetriever.get()
                : nextPageRetriever.apply(continuationToken));

        this.totalElements = firstPageRetriever.get().getTotalElements();
    }

    /**
     * Retrieve the total count of elements available in the entire collection. This count may not
     * represent the actual number of elements retrieved in the current page, but rather the total
     * count of elements in the collection.
     * @return total elements of the full result set
     */
    public Long getTotalElements() {
        return totalElements;
    }
}
