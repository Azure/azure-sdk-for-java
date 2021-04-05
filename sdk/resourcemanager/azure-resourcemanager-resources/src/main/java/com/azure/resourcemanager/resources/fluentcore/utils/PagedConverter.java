// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.utils;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.core.http.rest.Response;
import com.azure.core.util.paging.PageRetriever;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utility class for conversion of PagedResponse.
 */
public final class PagedConverter {

    private PagedConverter() {
    }

    /**
     * Applies map transform to elements of PagedIterable.
     *
     * @param pageIterable the input of PagedIterable.
     * @param mapper the map transform of element T to element S.
     * @param <T> input type of PagedFlux.
     * @param <S> return type of PagedFlux.
     * @return the PagedFlux with elements in PagedResponse transformed.
     */
    public static <T, S> PagedIterable<S> mapPage(PagedIterable<T> pageIterable, Function<T, S> mapper) {
        return new PagedIterableImpl<T, S>(pageIterable, mapper);
    }

    /**
     * Applies map transform to elements of PagedFlux.
     *
     * @param pagedFlux the input of PagedFlux.
     * @param mapper the map transform of element T to element S.
     * @param <T> input type of PagedFlux.
     * @param <S> return type of PagedFlux.
     * @return the PagedFlux with elements in PagedResponse transformed.
     */
    public static <T, S> PagedFlux<S> mapPage(PagedFlux<T> pagedFlux, Function<T, S> mapper) {
        Supplier<PageRetriever<String, PagedResponse<S>>> provider = () -> (continuationToken, pageSize) -> {
            Flux<PagedResponse<T>> flux = (continuationToken == null)
                ? pagedFlux.byPage().take(1)
                : pagedFlux.byPage(continuationToken).take(1);
            return flux.map(mapPagedResponse(mapper));
        };
        return PagedFlux.create(provider);
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
            // retrieve single page
            Flux<PagedResponse<T>> flux = (continuationToken == null)
                    ? pagedFlux.byPage().take(1)
                    : pagedFlux.byPage(continuationToken).take(1);
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
            // here retrieve all pages, as the continuationToken in mergePagedFluxPagedResponse would confuse this outer paging
            Flux<PagedResponse<T>> flux = (continuationToken == null)
                ? pagedFlux.byPage()
                : pagedFlux.byPage(continuationToken);
            return flux.concatMap(PagedConverter.mergePagedFluxPagedResponse(transformer));
        };
        return PagedFlux.create(provider);
    }

    private static <T, S> Function<PagedResponse<T>, PagedResponse<S>> mapPagedResponse(Function<T, S> mapper) {
        return pagedResponse -> new PagedResponseBase<Void, S>(pagedResponse.getRequest(),
            pagedResponse.getStatusCode(),
            pagedResponse.getHeaders(),
            pagedResponse.getValue().stream().map(mapper).collect(Collectors.toList()),
            pagedResponse.getContinuationToken(),
            null);
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
                        .map(values -> new PagedResponseBase<Void, S>(pagedResponse.getRequest(),
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

    private static final class PagedIterableImpl<T, S> extends PagedIterable<S> {

        private final PagedIterable<T> pagedIterable;
        private final Function<T, S> mapper;
        private final Function<PagedResponse<T>, PagedResponse<S>> pageMapper;

        private PagedIterableImpl(PagedIterable<T> pagedIterable, Function<T, S> mapper) {
            super(PagedFlux.create(() -> (continuationToken, pageSize)
                -> Flux.fromStream(pagedIterable.streamByPage().map(getPageMapper(mapper)))));
            this.pagedIterable = pagedIterable;
            this.mapper = mapper;
            this.pageMapper = getPageMapper(mapper);
        }

        private static <T, S> Function<PagedResponse<T>, PagedResponse<S>> getPageMapper(Function<T, S> mapper) {
            return page -> new PagedResponseBase<Void, S>(
                page.getRequest(),
                page.getStatusCode(),
                page.getHeaders(),
                page.getElements().stream().map(mapper).collect(Collectors.toList()),
                page.getContinuationToken(),
                null);
        }

        @Override
        public Stream<S> stream() {
            return pagedIterable.stream().map(mapper);
        }

        @Override
        public Stream<PagedResponse<S>> streamByPage() {
            return pagedIterable.streamByPage().map(pageMapper);
        }

        @Override
        public Stream<PagedResponse<S>> streamByPage(String continuationToken) {
            return pagedIterable.streamByPage(continuationToken).map(pageMapper);
        }

        @Override
        public Stream<PagedResponse<S>> streamByPage(int preferredPageSize) {
            return pagedIterable.streamByPage(preferredPageSize).map(pageMapper);
        }

        @Override
        public Stream<PagedResponse<S>> streamByPage(String continuationToken, int preferredPageSize) {
            return pagedIterable.streamByPage(continuationToken, preferredPageSize).map(pageMapper);
        }

        @Override
        public Iterator<S> iterator() {
            return new IteratorImpl<T, S>(pagedIterable.iterator(), mapper);
        }

        @Override
        public Iterable<PagedResponse<S>> iterableByPage() {
            return new IterableImpl<PagedResponse<T>, PagedResponse<S>>(
                pagedIterable.iterableByPage(), pageMapper);
        }

        @Override
        public Iterable<PagedResponse<S>> iterableByPage(String continuationToken) {
            return new IterableImpl<PagedResponse<T>, PagedResponse<S>>(
                pagedIterable.iterableByPage(continuationToken), pageMapper);
        }

        @Override
        public Iterable<PagedResponse<S>> iterableByPage(int preferredPageSize) {
            return new IterableImpl<PagedResponse<T>, PagedResponse<S>>(
                pagedIterable.iterableByPage(preferredPageSize), pageMapper);
        }

        @Override
        public Iterable<PagedResponse<S>> iterableByPage(String continuationToken, int preferredPageSize) {
            return new IterableImpl<PagedResponse<T>, PagedResponse<S>>(
                pagedIterable.iterableByPage(continuationToken, preferredPageSize), pageMapper);
        }
    }

    private static final class IteratorImpl<T, S> implements Iterator<S> {

        private final Iterator<T> iterator;
        private final Function<T, S> mapper;

        private IteratorImpl(Iterator<T> iterator, Function<T, S> mapper) {
            this.iterator = iterator;
            this.mapper = mapper;
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public S next() {
            return mapper.apply(iterator.next());
        }

        @Override
        public void remove() {
            iterator.remove();
        }
    }

    private static final class IterableImpl<T, S> implements Iterable<S> {

        private final Iterable<T> iterable;
        private final Function<T, S> mapper;

        private IterableImpl(Iterable<T> iterable, Function<T, S> mapper) {
            this.iterable = iterable;
            this.mapper = mapper;
        }

        @Override
        public Iterator<S> iterator() {
            return new IteratorImpl<T, S>(iterable.iterator(), mapper);
        }
    }
}
