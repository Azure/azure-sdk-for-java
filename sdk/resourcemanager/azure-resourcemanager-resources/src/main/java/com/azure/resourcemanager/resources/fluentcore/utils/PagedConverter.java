// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.utils;

import com.azure.core.http.HttpRequest;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.core.http.rest.Response;
import com.azure.core.util.paging.PageRetriever;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Utility class for conversion of PagedResponse.
 */
public final class PagedConverter {

    private PagedConverter() {
    }

    /**
     * Applies flatMap transform to elements of PagedFlux.
     *
     * @param pagedFlux the input of PagedFlux.
     * @param mapper the flatMap transform of element T to Publisher of S.
     * @param <T> input type of PagedFlux.
     * @param <S> return type of PagedFlux.
     * @return the PagedFlux with elements in PagedResponse transformed.
     */
    public static <T, S> PagedFlux<S> flatMapPage(PagedFlux<T> pagedFlux,
            Function<? super T, ? extends Publisher<? extends S>> mapper) {
        Supplier<PageRetriever<String, PagedResponse<S>>> provider = () -> (continuationToken, pageSize) -> {
            Flux<PagedResponse<T>> flux = (continuationToken == null)
                    ? pagedFlux.byPage()
                    : pagedFlux.byPage(continuationToken);
            return flux.concatMap(PagedConverter.flatMapPagedResponse(mapper));
        };
        return PagedFlux.create(provider);
    }

    /**
     * Merge collection of all PagedFlux transformed from elements of PagedFlux to a single PagedFlux.
     *
     * @param pagedFlux the input of PagedFlux.
     * @param transformer the transform of element T to PagedFlux of S.
     * @param <T> input type of PagedFlux.
     * @param <S> return type of PagedFlux.
     * @return the merged PagedFlux.
     */
    public static <T, S> PagedFlux<S> mergePagedFlux(PagedFlux<T> pagedFlux,
            Function<? super T, PagedFlux<S>> transformer) {
        // one possible issue is that when inner PagedFlux ends, that PagedResponse will have continuationToken == null

        Supplier<PageRetriever<String, PagedResponse<S>>> provider = () -> (continuationToken, pageSize) -> {
            Flux<PagedResponse<T>> flux = (continuationToken == null)
                ? pagedFlux.byPage()
                : pagedFlux.byPage(continuationToken);
            return flux.concatMap(PagedConverter.mergePagedFluxPagedResponse(transformer));
        };
        return PagedFlux.create(provider);
    }

    /**
     * Applies flatMap transform to elements of PagedResponse.
     *
     * @param mapper the flatMap transform of element T to Publisher of S.
     * @param <T> input type of pagedFlux.
     * @param <S> return type of pagedFlux.
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
     * Applies transform of element to PagedFlux, to elements of PagedResponse. Then merge all these PagedFlux.
     *
     * @param transformer the transform of element T to PagedFlux of S.
     * @param <T> input type of pagedFlux.
     * @param <S> return type of pagedFlux.
     * @return the the merged PagedFlux.
     */
    private static <T, S> Function<PagedResponse<T>, Flux<PagedResponse<S>>> mergePagedFluxPagedResponse(
        Function<? super T, PagedFlux<S>> transformer) {
        return pagedResponse -> {
            List<Flux<PagedResponse<S>>> fluxList = pagedResponse.getValue().stream()
                .map(item -> transformer.apply(item).byPage()).collect(Collectors.toList());
            return Flux.concat(fluxList)
                .filter(p -> !p.getValue().isEmpty());
        };
    }

    /**
     * Converts Response of List to PagedFlux.
     *
     * @param <T> type of element.
     * @param responseMono the Response of List to convert.
     * @return the PagedFlux.
     */
    public static <T> PagedFlux<T> convertListToPagedFlux(Mono<Response<List<T>>> responseMono) {
        return new PagedFlux<>(() -> responseMono.map(response -> new PagedResponseBase<Void, T>(
            response.getRequest(),
            response.getStatusCode(),
            response.getHeaders(),
            response.getValue(),
            null,
            null
        )));
    }
}
