// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.rest;

import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Mono;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Code snippets for {@link PagedFluxBase}
 */
public final class PagedFluxBaseJavaDocCodeSnippets {

    /**
     * Code snippets for showing usage of {@link PagedFluxBase} in class docs
     */
    public void classDocSnippet() {
        PagedFluxBase<Integer, PagedResponse<Integer>> pagedFluxBase = createAnInstance();
        // BEGIN: com.azure.core.http.rest.pagedfluxbase.items
        pagedFluxBase
            .log()
            .subscribe(item -> System.out.println("Processing item " + item),
                error -> System.err.println("Error occurred " + error),
                () -> System.out.println("Completed processing."));
        // END: com.azure.core.http.rest.pagedfluxbase.items

        // BEGIN: com.azure.core.http.rest.pagedfluxbase.pages
        pagedFluxBase
            .byPage()
            .log()
            .subscribe(page -> System.out.println("Processing page containing " + page.getItems()),
                error -> System.err.println("Error occurred " + error),
                () -> System.out.println("Completed processing."));
        // END: com.azure.core.http.rest.pagedfluxbase.pages

        // BEGIN: com.azure.core.http.rest.pagedfluxbase.pagesWithContinuationToken
        String continuationToken = getContinuationToken();
        pagedFluxBase
            .byPage(continuationToken)
            .log()
            .doOnSubscribe(ignored -> System.out.println(
                "Subscribed to paged flux processing pages starting from: " + continuationToken))
            .subscribe(page -> System.out.println("Processing page containing " + page.getItems()),
                error -> System.err.println("Error occurred " + error),
                () -> System.out.println("Completed processing."));
        // END: com.azure.core.http.rest.pagedfluxbase.pagesWithContinuationToken
    }

    /**
     * Code snippets for creating an instance of {@link PagedFluxBase}
     * @return An instance of {@link PagedFluxBase}
     */
    private PagedFluxBase<Integer, PagedResponse<Integer>> createAnInstance() {

        // BEGIN: com.azure.core.http.rest.pagedfluxbase.instantiation
        // A supplier that fetches the first page of data from source/service
        Supplier<Mono<PagedResponse<Integer>>> firstPageRetriever = () -> getFirstPage();

        // A function that fetches subsequent pages of data from source/service given a continuation token
        Function<String, Mono<PagedResponse<Integer>>> nextPageRetriever =
            continuationToken -> getNextPage(continuationToken);

        PagedFluxBase<Integer, PagedResponse<Integer>> pagedFluxBase = new PagedFluxBase<>(firstPageRetriever,
            nextPageRetriever);
        // END: com.azure.core.http.rest.pagedfluxbase.instantiation

        // BEGIN: com.azure.core.http.rest.pagedfluxbase.singlepage.instantiation
        // A supplier that fetches the first page of data from source/service
        Supplier<Mono<PagedResponse<Integer>>> firstPageRetrieverFunction = () -> getFirstPage();

        PagedFluxBase<Integer, PagedResponse<Integer>> pagedFluxBaseInstance = new PagedFluxBase<>(firstPageRetrieverFunction,
            nextPageRetriever);
        // END: com.azure.core.http.rest.pagedfluxbase.singlepage.instantiation
        return pagedFluxBase;
    }

    /**
     * Code snippets for using {@link PagedFluxBase#byPage()} and {@link PagedFluxBase#byPage(String)}
     */
    public void byPageSnippet() {
        PagedFluxBase<Integer, PagedResponse<Integer>> pagedFluxBase = createAnInstance();

        // BEGIN: com.azure.core.http.rest.pagedfluxbase.bypage
        // Start processing the results from first page
        pagedFluxBase.byPage()
            .log()
            .doOnSubscribe(ignoredVal -> System.out.println(
                "Subscribed to paged flux processing pages starting from first page"))
            .subscribe(page -> System.out.println("Processing page containing " + page.getItems()),
                error -> System.err.println("Error occurred " + error),
                () -> System.out.println("Completed processing."));
        // END: com.azure.core.http.rest.pagedfluxbase.bypage

        // BEGIN: com.azure.core.http.rest.pagedfluxbase.bypage#String
        // Start processing the results from a page associated with the continuation token
        String continuationToken = getContinuationToken();
        pagedFluxBase.byPage(continuationToken)
            .log()
            .doOnSubscribe(ignoredVal -> System.out.println(
                "Subscribed to paged flux processing page starting from " + continuationToken))
            .subscribe(page -> System.out.println("Processing page containing " + page.getItems()),
                error -> System.err.println("Error occurred " + error),
                () -> System.out.println("Completed processing."));
        // END: com.azure.core.http.rest.pagedfluxbase.bypage#String
    }

    /**
     * Code snippets for using {@link PagedFluxBase#subscribe(CoreSubscriber)}
     */
    public void byTSnippet() {
        PagedFluxBase<Integer, PagedResponse<Integer>> pagedFluxBase = createAnInstance();

        // BEGIN: com.azure.core.http.rest.pagedfluxbase.subscribe
        pagedFluxBase.subscribe(new BaseSubscriber<Integer>() {
                @Override
                protected void hookOnSubscribe(Subscription subscription) {
                    System.out.println("Subscribed to paged flux processing items");
                    super.hookOnSubscribe(subscription);
                }

                @Override
                protected void hookOnNext(Integer value) {
                    System.out.println("Processing item " + value);
                }

                @Override
                protected void hookOnComplete() {
                    System.out.println("Completed processing");
                }
            });
        // END: com.azure.core.http.rest.pagedfluxbase.subscribe
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
