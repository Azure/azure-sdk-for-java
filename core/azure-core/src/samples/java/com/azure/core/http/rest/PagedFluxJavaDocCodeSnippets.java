// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.rest;

import java.util.function.Function;
import java.util.function.Supplier;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Mono;

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
            .doOnSubscribe(
                ignoredVal -> System.out.println("Subscribed to paged flux processing items"))
            .doOnNext(item -> System.out.println("Processing item " + item))
            .doOnComplete(() -> System.out.println("Completed processing"))
            .subscribe();
        // END: com.azure.core.http.rest.pagedflux.items

        // BEGIN: com.azure.core.http.rest.pagedflux.pages
        // Subscribe to process one page at a time from the beginning
        pagedFlux
            .byPage()
            .log()
            .doOnSubscribe(ignoredVal -> System.out
                .println("Subscribed to paged flux processing pages starting from first page"))
            .doOnNext(page -> System.out.println("Processing page containing " + page.items()))
            .doOnComplete(() -> System.out.println("Completed processing"))
            .subscribe();
        // END: com.azure.core.http.rest.pagedflux.pages

        // BEGIN: com.azure.core.http.rest.pagedflux.pagesWithContinuationToken
        // Subscribe to process one page at a time starting from a page associated with
        // a continuation token
        String continuationToken = getContinuationToken();
        pagedFlux
            .byPage(continuationToken)
            .log()
            .doOnSubscribe(ignoredVal -> System.out
                .println("Subscribed to paged flux processing pages starting from first page"))
            .doOnNext(page -> System.out.println("Processing page containing " + page.items()))
            .doOnComplete(() -> System.out.println("Completed processing"))
            .subscribe();
        // END: com.azure.core.http.rest.pagedflux.pagesWithContinuationToken
    }
    /**
     * Code snippets for creating an instance of {@link PagedFlux}
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
        return pagedFlux;
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
            .doOnSubscribe(ignoredVal -> System.out
                .println("Subscribed to paged flux processing pages starting from first page"))
            .doOnNext(page -> System.out.println("Processing page containing " + page.items()))
            .doOnComplete(() -> System.out.println("Completed processing"))
            .subscribe();
        // END: com.azure.core.http.rest.pagedflux.bypage

        // BEGIN: com.azure.core.http.rest.pagedflux.bypage#String
        // Start processing the results from a page associated with the continuation token
        String continuationToken = getContinuationToken();
        pagedFlux.byPage(continuationToken)
            .log()
            .doOnSubscribe(ignoredVal -> System.out.println(
                "Subscribed to paged flux processing page starting from " + continuationToken))
            .doOnNext(page -> System.out.println("Processing page containing " + page.items()))
            .doOnComplete(() -> System.out.println("Completed processing"))
            .subscribe();
        // END: com.azure.core.http.rest.pagedflux.bypage#String
    }

    /**
     * Code snippets for using {@link PagedFlux#subscribe(CoreSubscriber)}
     */
    public void byTSnippet() {
        PagedFlux<Integer> pagedFlux = createAnInstance();

        // BEGIN: com.azure.core.http.rest.pagedflux.subscribe
        pagedFlux.log()
            .doOnSubscribe(ignoredVal -> System.out.println("Subscribed to paged flux processing items"))
            .doOnNext(item -> System.out.println("Processing item " + item))
            .doOnComplete(() -> System.out.println("Completed processing"))
            .subscribe();
        // END: com.azure.core.http.rest.pagedflux.subscribe
    }

    /**
     * Implementation not provided
     *
     * @return A continuation token
     */
    private String getContinuationToken() {
        return null;
    }

    /**
     * Implementation not provided
     *
     * @param continuationToken Token to fetch the next page
     * @return A {@link Mono} of {@link PagedResponse} containing items of type {@code Integer}
     */
    private Mono<PagedResponse<Integer>> getNextPage(String continuationToken) {
        return null;
    }

    /**
     * Implementation not provided
     *
     * @return A {@link Mono} of {@link PagedResponse} containing items of type {@code Integer}
     */
    private Mono<PagedResponse<Integer>> getFirstPage() {
        return null;
    }
}
