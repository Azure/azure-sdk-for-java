// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.rest;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *  This class provides utility to iterate over {@link PagedResponse}.
 * @param  <T> value
 */
public class PagedIterable<T> {
    private final PagedFlux<T> pagedFlux;

    /**
     * Creates instance given {@link Flux}.
     * @param flux  the flux value
     */
    public PagedIterable(Flux<T> flux) {
        Supplier<Mono<PagedResponse<T>>> supp = getSupplier(flux);
        this.pagedFlux = new PagedFlux<>(supp);
    }

    private Supplier<Mono<PagedResponse<T>>> getSupplier(Flux<T> flux) {
        return () -> Mono.just(new SinglePagedResponse<>(flux));
    }
    /**
     * Creates instance given {@link PagedFlux}.
     * @param pagedFlux to use as iterable
     */
    public PagedIterable(PagedFlux<T> pagedFlux) {
        this.pagedFlux = pagedFlux;
    }

    /**
     * Retrieve the {@link Stream}, one page at a time.
     * @return {@link Stream} of {@link PagedResponse}
     */
    public Stream<PagedResponse<T>> streamByPage() {
        return pagedFlux.byPage().toStream();
    }

    /**
     * Provides iterable API for{@link PagedResponse}.
     * @return iterable interface
     */
    public Iterable<PagedResponse<T>> iterableByPage() {
        return pagedFlux.byPage().toIterable();
    }

    /**
     *
     * @param <T> value
     */
    public class SinglePagedResponse<T> implements PagedResponse<T> {

        private  Flux<T> flux;

        /**
         * Crete an instance.
         * @param flux value
         */
        public SinglePagedResponse(Flux<T> flux) {
            this.flux = flux;
        }

        @Override
        public List<T> items() {
            return flux.toStream().collect(Collectors.toList());
        }

        @Override
        public String nextLink() {
            return null;
        }

        @Override
        public int statusCode() {
            return -1;
        }

        @Override
        public HttpHeaders headers() {
            return null;
        }

        @Override
        public HttpRequest request() {
            return null;
        }

        @Override
        public void close() {
        }
    };
}
