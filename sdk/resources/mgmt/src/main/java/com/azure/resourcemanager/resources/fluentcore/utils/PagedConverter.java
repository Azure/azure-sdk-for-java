// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.utils;

import com.azure.core.http.HttpRequest;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.core.util.paging.PageRetriever;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Utility class for conversion of PagedResponse.
 */
public final class PagedConverter {

    private PagedConverter() {

    }

    /**
     * Applies flatMap transform to elements of PagedFlux.
     *
     * @param <T> input type of pagedFlux
     * @param <S> return type of pagedFlux
     * @param pagedFlux input
     * @param mapper the flatMap transform of element T to Publisher of S.
     * @return the PagedFlux.
     */
    public static <T, S> PagedFlux<S> flatMapPage(PagedFlux<T> pagedFlux,
            Function<? super T, ? extends Publisher<? extends S>> mapper) {
        Supplier<PageRetriever<String, PagedResponse<S>>> provider = () -> (continuationToken, pageSize) -> {
            Flux<PagedResponse<T>> flux = (continuationToken == null)
                    ? pagedFlux.byPage()
                    : pagedFlux.byPage(continuationToken);
            return flux.flatMap(PagedConverter.flatMapPagedResponse(mapper));
        };
        return PagedFlux.create(provider);
    }

    /**
     * Applies flatMap transform to elements of PagedResponse.
     *
     * @param <T> input type of pagedFlux
     * @param <S> return type of pagedFlux
     * @param mapper the flatMap transform of element T to Publisher of S.
     * @return the lifted transform on PagedResponse.
     */
    private static <T, S> Function<PagedResponse<T>, Mono<PagedResponse<S>>> flatMapPagedResponse(
            Function<? super T, ? extends Publisher<? extends S>> mapper) {
        return pagedResponse ->
                Flux.fromIterable(pagedResponse.getValue())
                        .flatMapSequential(mapper)
                        .collectList()
                        .map(values -> new PagedResponseBase<HttpRequest, S>(pagedResponse.getRequest(),
                                pagedResponse.getStatusCode(),
                                pagedResponse.getHeaders(),
                                values,
                                pagedResponse.getContinuationToken(),
                                null));
    }

    /**
     * Converts list to PagedFlux.
     *
     * @param <T> type of List in Mono.
     * @param list the list to convert.
     * @return the PagedFlux.
     */
    public static <T> PagedFlux<T> convertListToPagedFlux(Mono<List<T>> list) {
        return new PagedFlux<>(() -> list.map(elements -> new PagedResponseBase<HttpRequest, T>(
                null,
                200,
                null,
                elements,
                null,
                null
        )));
    }
}
