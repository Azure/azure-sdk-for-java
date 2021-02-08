// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.rest;

import com.azure.core.util.IterableStream;
import reactor.core.publisher.Mono;

import java.util.Iterator;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class provides utility to iterate over {@link PagedResponse} using {@link Stream} and {@link Iterable}
 * interfaces.
 *
 * <p><strong>Code sample using {@link Stream} by page</strong></p>
 *
 * {@codesnippet com.azure.core.http.rest.pagedIterable.streamByPage}
 *
 * <p><strong>Code sample using {@link Iterable} by page</strong></p>
 *
 * {@codesnippet com.azure.core.http.rest.pagedIterable.iterableByPage}
 *
 * <p><strong>Code sample using {@link Iterable} by page and while loop</strong></p>
 *
 * {@codesnippet com.azure.core.http.rest.pagedIterable.iterableByPage.while}
 *
 * @param <T> The type of value contained in this {@link IterableStream}.
 * @see PagedResponse
 * @see IterableStream
 */
public class PagedIterable<T> extends PagedIterableBase<T, PagedResponse<T>> {
    //private final PagedFlux<T> pagedFlux;

    /**
     * Creates instance given {@link PagedFlux}.
     * @param pagedFlux to use as iterable
     */
    public PagedIterable(PagedFlux<T> pagedFlux) {
        super(pagedFlux);
        //this.pagedFlux = pagedFlux;
    }

    /**
     * Maps this PagedIterable instance of T to a PagedIterable instance of type S as per the provided mapper function.
     *
     * @param mapper The mapper function to convert from type T to type S.
     * @param <S> The mapped type.
     * @return A PagedIterable of type S.
     */
    @SuppressWarnings("deprecation")
    public <S> PagedIterable<S> mapPage(Function<T, S> mapper) {
        //return new PagedIterable<>(pagedFlux.mapPage(mapper));
        return new PagedIterableImpl<T, S>(this, mapper);
    }

    private static final class IteratorImpl<T, S> implements Iterator<S> {

        private final Iterator<T> source;
        private final Function<T, S> mapper;

        private IteratorImpl(Iterator<T> source, Function<T, S> mapper) {
            this.source = source;
            this.mapper = mapper;
        }

        @Override
        public boolean hasNext() {
            return source.hasNext();
        }

        @Override
        public S next() {
            return mapper.apply(source.next());
        }

        @Override
        public void remove() {
            source.remove();
        }
    }

    private static final class IterableImpl<T, S> implements Iterable<S> {

        private final Iterable<T> source;
        private final Function<T, S> mapper;

        private IterableImpl(Iterable<T> source, Function<T, S> mapper) {
            this.source = source;
            this.mapper = mapper;
        }

        @Override
        public Iterator<S> iterator() {
            return new IteratorImpl<T, S>(source.iterator(), mapper);
        }
    }

    private static final class PagedIterableImpl<T, S> extends PagedIterable<S> {

        private final PagedIterable<T> source;
        private final Function<T, S> mapper;
        private final Function<PagedResponse<T>, PagedResponse<S>> pageMapper;

        private PagedIterableImpl(PagedIterable<T> sourcePageIterable, Function<T, S> mapper) {
            super(new PagedFlux<S>(Mono::empty));
            this.source = sourcePageIterable;
            this.mapper = mapper;
            this.pageMapper = page -> new PagedResponseBase<Void, S>(
                page.getRequest(),
                page.getStatusCode(),
                page.getHeaders(),
                page.getElements().stream().map(mapper).collect(Collectors.toList()),
                page.getContinuationToken(),
                null);
        }

        @Override
        public Stream<S> stream() {
            return source.stream().map(mapper);
        }

        @Override
        public Stream<PagedResponse<S>> streamByPage() {
            return source.streamByPage().map(pageMapper);
        }

        @Override
        public Stream<PagedResponse<S>> streamByPage(String continuationToken) {
            return source.streamByPage(continuationToken).map(pageMapper);
        }

        @Override
        public Stream<PagedResponse<S>> streamByPage(int preferredPageSize) {
            return source.streamByPage(preferredPageSize).map(pageMapper);
        }

        @Override
        public Stream<PagedResponse<S>> streamByPage(String continuationToken, int preferredPageSize) {
            return source.streamByPage(continuationToken, preferredPageSize).map(pageMapper);
        }

        @Override
        public Iterator<S> iterator() {
            return new IteratorImpl<T, S>(source.iterator(), mapper);
        }

        @Override
        public Iterable<PagedResponse<S>> iterableByPage() {
            return new IterableImpl<PagedResponse<T>, PagedResponse<S>>(
                source.iterableByPage(), pageMapper);
        }

        @Override
        public Iterable<PagedResponse<S>> iterableByPage(String continuationToken) {
            return new IterableImpl<PagedResponse<T>, PagedResponse<S>>(
                source.iterableByPage(continuationToken), pageMapper);
        }

        @Override
        public Iterable<PagedResponse<S>> iterableByPage(int preferredPageSize) {
            return new IterableImpl<PagedResponse<T>, PagedResponse<S>>(
                source.iterableByPage(preferredPageSize), pageMapper);
        }

        @Override
        public Iterable<PagedResponse<S>> iterableByPage(String continuationToken, int preferredPageSize) {
            return new IterableImpl<PagedResponse<T>, PagedResponse<S>>(
                source.iterableByPage(continuationToken, preferredPageSize), pageMapper);
        }
    }
}
