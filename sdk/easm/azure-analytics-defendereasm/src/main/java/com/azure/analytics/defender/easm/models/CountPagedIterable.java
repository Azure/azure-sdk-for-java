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

    /*
    * The total number of elements in the full result set
     */
    private Long totalElements;

    public CountPagedIterable(Supplier<CountPagedResponse<T>> firstPageRetriever, Function<String, CountPagedResponse<T>> nextPageRetriever){
        super(() -> (continuationToken, pageSize) ->
            continuationToken == null
                ? firstPageRetriever.get()
                : nextPageRetriever.apply(continuationToken));

        this.totalElements = firstPageRetriever.get().getTotalElements();
    }

    public Long getTotalElements() {
        return totalElements;
    }
}
