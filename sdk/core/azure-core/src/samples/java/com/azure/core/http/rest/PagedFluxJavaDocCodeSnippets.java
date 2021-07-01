// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.rest;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.util.paging.PageRetriever;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Code snippets for {@link PagedFlux}
 */
public final class PagedFluxJavaDocCodeSnippets {

    /**
     * Code snippets for showing usage of {@link PagedFlux} in class docs
     */
    public void classDocSnippet() {
        PagedFlux<Integer> pagedFlux = createAnInstance();
        // BEGIN: com.azure.core.http.rest.pagedflux.items
        // Subscribe to process one item at a time
        pagedFlux
            .log()
            .subscribe(item -> System.out.println("Processing item with value: " + item),
                error -> System.err.println("An error occurred: " + error),
                () -> System.out.println("Processing complete."));
        // END: com.azure.core.http.rest.pagedflux.items

        // BEGIN: com.azure.core.http.rest.pagedflux.pages
        // Subscribe to process one page at a time from the beginning
        pagedFlux
            .byPage()
            .log()
            .subscribe(page -> System.out.printf("Processing page containing item values: %s%n",
                page.getElements().stream().map(String::valueOf).collect(Collectors.joining(", "))),
                error -> System.err.println("An error occurred: " + error),
                () -> System.out.println("Processing complete."));
        // END: com.azure.core.http.rest.pagedflux.pages

        // BEGIN: com.azure.core.http.rest.pagedflux.pagesWithContinuationToken
        // Subscribe to process one page at a time starting from a page associated with
        // a continuation token
        String continuationToken = getContinuationToken();
        pagedFlux
            .byPage(continuationToken)
            .log()
            .doOnSubscribe(ignored -> System.out.println(
                "Subscribed to paged flux processing pages starting from: " + continuationToken))
            .subscribe(page -> System.out.printf("Processing page containing item values: %s%n",
                page.getElements().stream().map(String::valueOf).collect(Collectors.joining(", "))),
                error -> System.err.println("An error occurred: " + error),
                () -> System.out.println("Processing complete."));
        // END: com.azure.core.http.rest.pagedflux.pagesWithContinuationToken
    }

    /**
     * Code snippets for creating an instance of {@link PagedFlux}
     *
     * @return An instance of {@link PagedFlux}
     */
    public PagedFlux<Integer> createAnInstance() {

        // BEGIN: com.azure.core.http.rest.pagedflux.instantiation
        // A supplier that fetches the first page of data from source/service
        Supplier<Mono<PagedResponse<Integer>>> firstPageRetriever = () -> getFirstPage();

        // A function that fetches subsequent pages of data from source/service given a continuation token
        Function<String, Mono<PagedResponse<Integer>>> nextPageRetriever =
            continuationToken -> getNextPage(continuationToken);

        PagedFlux<Integer> pagedFlux = new PagedFlux<>(firstPageRetriever,
            nextPageRetriever);
        // END: com.azure.core.http.rest.pagedflux.instantiation

        // BEGIN: com.azure.core.http.rest.pagedflux.singlepage.instantiation
        // A supplier that fetches the first page of data from source/service
        Supplier<Mono<PagedResponse<Integer>>> firstPageRetrieverFunction = () -> getFirstPage();

        PagedFlux<Integer> pagedFluxInstance = new PagedFlux<>(firstPageRetrieverFunction,
            nextPageRetriever);
        // END: com.azure.core.http.rest.pagedflux.singlepage.instantiation
        return pagedFlux;
    }

    public void createASinglePageInstanceWithPageSizeSupport() {
        // BEGIN: com.azure.core.http.rest.PagedFlux.singlepage.instantiationWithPageSize
        // A function that fetches the single page of data from a source/service.
        Function<Integer, Mono<PagedResponse<Integer>>> singlePageRetriever = pageSize ->
            getFirstPageWithSize(pageSize);

        PagedFlux<Integer> singlePageFluxWithPageSize = new PagedFlux<Integer>(singlePageRetriever);
        // END: com.azure.core.http.rest.PagedFlux.singlepage.instantiationWithPageSize
    }

    public void createAnInstanceWithPageSizeSupport() {
        // BEGIN: com.azure.core.http.rest.PagedFlux.instantiationWithPageSize
        // A function that fetches the first page of data from a source/service.
        Function<Integer, Mono<PagedResponse<Integer>>> firstPageRetriever = pageSize -> getFirstPageWithSize(pageSize);

        // A function that fetches subsequent pages of data from a source/service given a continuation token.
        BiFunction<String, Integer, Mono<PagedResponse<Integer>>> nextPageRetriever = (continuationToken, pageSize) ->
            getNextPageWithSize(continuationToken, pageSize);

        PagedFlux<Integer> pagedFluxWithPageSize = new PagedFlux<>(firstPageRetriever, nextPageRetriever);
        // END: com.azure.core.http.rest.PagedFlux.instantiationWithPageSize
    }

    /**
     * Code snippets for using {@link PagedFlux#byPage()} and {@link PagedFlux#byPage(String)}
     */
    public void byPageSnippet() {
        PagedFlux<Integer> pagedFlux = createAnInstance();

        // BEGIN: com.azure.core.http.rest.pagedflux.bypage
        // Start processing the results from first page
        pagedFlux.byPage()
            .log()
            .doOnSubscribe(ignoredVal -> System.out.println(
                "Subscribed to paged flux processing pages starting from first page"))
            .subscribe(page -> System.out.printf("Processing page containing item values: %s%n",
                page.getElements().stream().map(String::valueOf).collect(Collectors.joining(", "))),
                error -> System.err.println("An error occurred: " + error),
                () -> System.out.println("Processing complete."));
        // END: com.azure.core.http.rest.pagedflux.bypage

        // BEGIN: com.azure.core.http.rest.pagedflux.bypage#String
        // Start processing the results from a page associated with the continuation token
        String continuationToken = getContinuationToken();
        pagedFlux.byPage(continuationToken)
            .log()
            .doOnSubscribe(ignoredVal -> System.out.println(
                "Subscribed to paged flux processing page starting from " + continuationToken))
            .subscribe(page -> System.out.printf("Processing page containing item values: %s%n",
                page.getElements().stream().map(String::valueOf).collect(Collectors.joining(", "))),
                error -> System.err.println("An error occurred: " + error),
                () -> System.out.println("Processing complete."));
        // END: com.azure.core.http.rest.pagedflux.bypage#String
    }

    /**
     * Code snippets for using {@link PagedFlux#subscribe(CoreSubscriber)}
     */
    public void byTSnippet() {
        PagedFlux<Integer> pagedFlux = createAnInstance();

        // BEGIN: com.azure.core.http.rest.pagedflux.subscribe
        pagedFlux.subscribe(new BaseSubscriber<Integer>() {
            @Override
            protected void hookOnSubscribe(Subscription subscription) {
                System.out.println("Subscribed to paged flux processing items");
                super.hookOnSubscribe(subscription);
            }

            @Override
            protected void hookOnNext(Integer value) {
                System.out.println("Processing item with value: " + value);
            }

            @Override
            protected void hookOnComplete() {
                System.out.println("Processing complete.");
            }
        });
        // END: com.azure.core.http.rest.pagedflux.subscribe
    }

    /**
     * Code snippets for using {@link PagedFlux#create(Supplier)} to create a PagedFlux by applying decoration on
     * another PagedFlux.
     */
    public void pagedFluxFromPagedFlux() {
        // BEGIN: com.azure.core.http.rest.pagedflux.create.decoration

        // Transform a PagedFlux with Integer items to PagedFlux of String items.
        final PagedFlux<Integer> intPagedFlux = createAnInstance();

        // PagedResponse<Integer> to PagedResponse<String> mapper
        final Function<PagedResponse<Integer>, PagedResponse<String>> responseMapper
            = intResponse -> new PagedResponseBase<Void, String>(intResponse.getRequest(),
            intResponse.getStatusCode(),
            intResponse.getHeaders(),
            intResponse.getValue()
                .stream()
                .map(intValue -> Integer.toString(intValue)).collect(Collectors.toList()),
            intResponse.getContinuationToken(),
            null);

        final Supplier<PageRetriever<String, PagedResponse<String>>> provider = () ->
            (continuationToken, pageSize) -> {
                Flux<PagedResponse<Integer>> flux = (continuationToken == null)
                    ? intPagedFlux.byPage()
                    : intPagedFlux.byPage(continuationToken);
                return flux.map(responseMapper);
            };
        PagedFlux<String> strPagedFlux = PagedFlux.create(provider);

        // Create a PagedFlux from a PagedFlux with all exceptions mapped to a specific exception.
        final PagedFlux<Integer> pagedFlux = createAnInstance();
        final Supplier<PageRetriever<String, PagedResponse<Integer>>> eprovider = () ->
            (continuationToken, pageSize) -> {
                Flux<PagedResponse<Integer>> flux = (continuationToken == null)
                    ? pagedFlux.byPage()
                    : pagedFlux.byPage(continuationToken);
                return flux.onErrorMap(PaginationException::new);
            };
        final PagedFlux<Integer> exceptionMappedPagedFlux = PagedFlux.create(eprovider);
        // END: com.azure.core.http.rest.pagedflux.create.decoration
    }

    /**
     * Implementation not provided
     *
     * @return A continuation token
     */
    private String getContinuationToken() {
        return UUID.randomUUID().toString();
    }

    /**
     * Retrieves the initial page from a paged API.
     *
     * @return A {@link Mono} of {@link PagedResponse} containing items of type {@link Integer}
     */
    private Mono<PagedResponse<Integer>> getFirstPage() {
        return getPage(null, null);
    }

    /**
     * Retrieves the next page from a paged API.
     *
     * @param continuationToken Token to fetch the next page
     * @return A {@link Mono} of {@link PagedResponse} containing items of type {@link Integer}
     */
    private Mono<PagedResponse<Integer>> getNextPage(String continuationToken) {
        return getPage(continuationToken, null);
    }

    /**
     * Retrieves the initial page from a paged API with the given number of elements.
     * <p>
     * If {@code pageSize} is null then it will return the default page size used by the paged API.
     *
     * @param pageSize Number of elements to be included in the page.
     * @return A {@link Mono} of {@link PagedResponse} containing items of type {@link Integer}.
     */
    private Mono<PagedResponse<Integer>> getFirstPageWithSize(Integer pageSize) {
        return getPage(null, pageSize);
    }

    /**
     * Retrieves the next page from a paged API with the given number of elements.
     * <p>
     * If {@code pageSize} is null then it will return the default page size used by the paged API.
     *
     * @param continuationToken Token to fetch the next page.
     * @param pageSize Number of elements to be included in the page.
     * @return A {@link Mono} of {@link PagedResponse} containing items of type {@link Integer}.
     */
    private Mono<PagedResponse<Integer>> getNextPageWithSize(String continuationToken, Integer pageSize) {
        return getPage(continuationToken, pageSize);
    }

    /**
     * Retrieves a page from a paged API.
     *
     * @param continuationToken Token to fetch the next page, if {@code null} the first page is retrieved.
     * @param pageSize Number of elements to be returned in each page.
     * @return A {@link Mono} of {@link PagedResponse} containing items of type {@code Integer}
     */
    private Mono<PagedResponse<Integer>> getPage(String continuationToken, Integer pageSize) {
        // Given this isn't calling an actual API we will arbitrarily generate a continuation token or end paging.
        boolean lastPage = Math.random() > 0.5;

        // If it is the last page there should be no additional continuation tokens returned.
        String nextContinuationToken = lastPage ? null : UUID.randomUUID().toString();

        // Arbitrarily begin the next page of integers.
        int elementCount = (pageSize == null) ? (int) Math.ceil(Math.random() * 15) : pageSize;
        List<Integer> elements = IntStream.range(elementCount, elementCount + elementCount)
            .map(val -> (int) (Math.random() * val))
            .boxed()
            .collect(Collectors.toList());

        // This is a rough approximation of a service response.
        return Mono.just(new PagedResponseBase<Void, Integer>(new HttpRequest(HttpMethod.GET, "https://requestUrl.com"),
            200, new HttpHeaders(), elements, nextContinuationToken, null));
    }

    static class PaginationException extends RuntimeException {
        PaginationException(Throwable ex) {
            super(ex);
        }
    }
}
