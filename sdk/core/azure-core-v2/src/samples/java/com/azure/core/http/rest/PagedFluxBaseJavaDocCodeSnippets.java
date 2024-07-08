// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.rest;

import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Code snippets for {@link PagedFluxBase}
 */
@SuppressWarnings("deprecation")
public final class PagedFluxBaseJavaDocCodeSnippets {

    /**
     * Code snippets for showing usage of {@link PagedFluxBase} in class docs
     */
    public void classDocSnippet() {
        PagedFluxBase<Integer, PagedResponse<Integer>> pagedFluxBase = createAnInstance();
        // BEGIN: com.azure.core.http.rest.pagedfluxbase.items
        pagedFluxBase
            .log()
            .subscribe(item -> System.out.println("Processing item with value: " + item),
                error -> System.err.println("An error occurred: " + error),
                () -> System.out.println("Processing complete."));
        // END: com.azure.core.http.rest.pagedfluxbase.items

        // BEGIN: com.azure.core.http.rest.pagedfluxbase.pages
        pagedFluxBase
            .byPage()
            .log()
            .subscribe(page -> System.out.printf("Processing page containing item values: %s%n",
                page.getElements().stream().map(String::valueOf).collect(Collectors.joining(", "))),
                error -> System.err.println("An error occurred: " + error),
                () -> System.out.println("Processing complete."));
        // END: com.azure.core.http.rest.pagedfluxbase.pages

        // BEGIN: com.azure.core.http.rest.pagedfluxbase.pagesWithContinuationToken
        String continuationToken = getContinuationToken();
        pagedFluxBase
            .byPage(continuationToken)
            .log()
            .doOnSubscribe(ignored -> System.out.println(
                "Subscribed to paged flux processing pages starting from: " + continuationToken))
            .subscribe(page -> System.out.printf("Processing page containing item values: %s%n",
                page.getElements().stream().map(String::valueOf).collect(Collectors.joining(", "))),
                error -> System.err.println("An error occurred: " + error),
                () -> System.out.println("Processing complete."));
        // END: com.azure.core.http.rest.pagedfluxbase.pagesWithContinuationToken
    }

    /**
     * Code snippets for creating an instance of {@link PagedFluxBase}
     *
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

        PagedFluxBase<Integer, PagedResponse<Integer>> pagedFluxBaseInstance =
            new PagedFluxBase<>(firstPageRetrieverFunction,
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
            .subscribe(page -> System.out.printf("Processing page containing item values: %s%n",
                page.getElements().stream().map(String::valueOf).collect(Collectors.joining(", "))),
                error -> System.err.println("An error occurred: " + error),
                () -> System.out.println("Processing complete."));
        // END: com.azure.core.http.rest.pagedfluxbase.bypage

        // BEGIN: com.azure.core.http.rest.pagedfluxbase.bypage#String
        // Start processing the results from a page associated with the continuation token
        String continuationToken = getContinuationToken();
        pagedFluxBase.byPage(continuationToken)
            .log()
            .doOnSubscribe(ignoredVal -> System.out.println(
                "Subscribed to paged flux processing page starting from " + continuationToken))
            .subscribe(page -> System.out.printf("Processing page containing item values: %s%n",
                page.getElements().stream().map(String::valueOf).collect(Collectors.joining(", "))),
                error -> System.err.println("An error occurred: " + error),
                () -> System.out.println("Processing complete."));
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
                System.out.println("Processing item with value: " + value);
            }

            @Override
            protected void hookOnComplete() {
                System.out.println("Processing complete.");
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
        return UUID.randomUUID().toString();
    }

    /**
     * Retrieves the next page from a paged API.
     *
     * @param continuationToken Token to fetch the next page
     * @return A {@link Mono} of {@link PagedResponse} containing items of type {@code Integer}
     */
    private Mono<PagedResponse<Integer>> getNextPage(String continuationToken) {
        return getPage(continuationToken);
    }

    /**
     * Retrieves the initial page from a paged API.
     *
     * @return A {@link Mono} of {@link PagedResponse} containing items of type {@code Integer}
     */
    private Mono<PagedResponse<Integer>> getFirstPage() {
        return getPage(null);
    }

    /**
     * Retrieves a page from a paged API.
     *
     * @param continuationToken Token to fetch the next page, if {@code null} the first page is retrieved.
     * @return A {@link Mono} of {@link PagedResponse} containing items of type {@code Integer}
     */
    private Mono<PagedResponse<Integer>> getPage(String continuationToken) {
        // Given this isn't calling an actual API we will arbitrarily generate a continuation token or end paging.
        boolean lastPage = Math.random() > 0.5;

        // If it is the last page there should be no additional continuation tokens returned.
        String nextContinuationToken = lastPage ? null : UUID.randomUUID().toString();

        // Arbitrarily begin the next page of integers.
        int elementCount = (int) Math.ceil(Math.random() * 15);
        List<Integer> elements = IntStream.range(elementCount, elementCount + elementCount)
            .map(val -> (int) (Math.random() * val))
            .boxed()
            .collect(Collectors.toList());

        // This is a rough approximation of a service response.
        return Mono.just(new PagedResponseBase<Void, Integer>(new HttpRequest(HttpMethod.GET, "https://requestUrl.com"),
            200, new HttpHeaders(), elements, nextContinuationToken, null));
    }
}
