// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.network.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.core.util.paging.PageRetriever;
import reactor.core.publisher.Flux;

import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Utility class for conversion of PagedResponse.
 */
final class PagedConverterForMergedPagedFlux {

    private PagedConverterForMergedPagedFlux() {
    }

    /**
     * Applies map transform to elements of PagedFlux.
     * <p>
     * Use this mapPage (not the one in PagedConverter), when the PagedFlux is produced by PagedConverter.mergePagedFlux
     *
     * @param pagedFlux the input of PagedFlux.
     * @param mapper the map transform of element T to element S.
     * @param <T> input type of PagedFlux.
     * @param <S> return type of PagedFlux.
     * @return the PagedFlux with elements in PagedResponse transformed.
     */
    static <T, S> PagedFlux<S> mapPage(PagedFlux<T> pagedFlux, Function<T, S> mapper) {
        Supplier<PageRetriever<String, PagedResponse<S>>> provider = () -> (continuationToken, pageSize) -> {
            // take all the pages, do not use .take(1)
            Flux<PagedResponse<T>> flux = (continuationToken == null)
                ? pagedFlux.byPage()
                : pagedFlux.byPage(continuationToken);
            return flux.map(mapPagedResponse(mapper));
        };
        return PagedFlux.create(provider);
    }

    private static <T, S> Function<PagedResponse<T>, PagedResponse<S>> mapPagedResponse(Function<T, S> mapper) {
        return pagedResponse -> new PagedResponseBase<Void, S>(pagedResponse.getRequest(),
            pagedResponse.getStatusCode(), pagedResponse.getHeaders(),
            pagedResponse.getValue().stream().map(mapper).collect(Collectors.toList()),
            pagedResponse.getContinuationToken(), null);
    }
}
