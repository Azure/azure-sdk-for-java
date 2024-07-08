// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.rest;

import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Code snippets for {@link PagedIterable}
 */
public class PagedIterableJavaDocCodeSnippets {

    public void createASinglePageInstanceWithPageSizeSupport() {
        // BEGIN: com.azure.core.http.rest.PagedIterable.singlepage.instantiationWithPageSize
        // A function that fetches the single page of data from a source/service.
        Function<Integer, PagedResponse<Integer>> singlePageRetriever = pageSize ->
            getPage(pageSize);

        PagedIterable<Integer> singlePageIterableWithPageSize = new PagedIterable<Integer>(singlePageRetriever);
        // END: com.azure.core.http.rest.PagedIterable.singlepage.instantiationWithPageSize
    }

    public void createAnInstanceWithPageSizeSupport() {
        // BEGIN: com.azure.core.http.rest.PagedIterable.instantiationWithPageSize
        // A function that fetches the first page of data from a source/service.
        Function<Integer, PagedResponse<Integer>> firstPageRetriever = pageSize -> getPage(pageSize);

        // A function that fetches subsequent pages of data from a source/service given a continuation token.
        BiFunction<String, Integer, PagedResponse<Integer>> nextPageRetriever = (continuationToken, pageSize) ->
            getPage(continuationToken, pageSize);

        PagedIterable<Integer> pagedIterableWithPageSize = new PagedIterable<>(firstPageRetriever, nextPageRetriever);
        // END: com.azure.core.http.rest.PagedIterable.instantiationWithPageSize
    }

    /**
     * Provides an example for iterate over each response using streamByPage function.
     **/
    public void streamByPageSnippet() {
        PagedFlux<Integer> pagedFlux = createAnInstance();
        PagedIterable<Integer> pagedIterableResponse = new PagedIterable<>(pagedFlux);

        // BEGIN: com.azure.core.http.rest.PagedIterable.streamByPage
        // process the streamByPage
        pagedIterableResponse.streamByPage().forEach(resp -> {
            System.out.printf("Response headers are %s. Url %s  and status code %d %n", resp.getHeaders(),
                resp.getRequest().getUrl(), resp.getStatusCode());
            resp.getElements().forEach(value -> System.out.printf("Response value is %d %n", value));
        });

        // END: com.azure.core.http.rest.PagedIterable.streamByPage
    }

    /**
     * Provides an example for iterate over each response using iterableByPage function.
     **/
    public void iterateByPageSnippet() {

        PagedFlux<Integer> pagedFlux = createAnInstance();
        PagedIterable<Integer> pagedIterableResponse = new PagedIterable<>(pagedFlux);

        // BEGIN: com.azure.core.http.rest.PagedIterable.iterableByPage
        // process the iterableByPage
        pagedIterableResponse.iterableByPage().forEach(resp -> {
            System.out.printf("Response headers are %s. Url %s  and status code %d %n", resp.getHeaders(),
                resp.getRequest().getUrl(), resp.getStatusCode());
            resp.getElements().forEach(value -> System.out.printf("Response value is %d %n", value));
        });
        // END: com.azure.core.http.rest.PagedIterable.iterableByPage
    }

    /**
     * Provides an example for iterate over each response using iterableByPage function and while loop.
     **/
    public void iterableByPageWhileSnippet() {

        PagedFlux<Integer> pagedFlux = createAnInstance();
        PagedIterable<Integer> pagedIterableResponse = new PagedIterable<>(pagedFlux);

        // BEGIN: com.azure.core.http.rest.PagedIterable.iterableByPage.while
        // iterate over each page
        for (PagedResponse<Integer> resp : pagedIterableResponse.iterableByPage()) {
            System.out.printf("Response headers are %s. Url %s  and status code %d %n", resp.getHeaders(),
                resp.getRequest().getUrl(), resp.getStatusCode());
            resp.getElements().forEach(value -> System.out.printf("Response value is %d %n", value));
        }
        // END: com.azure.core.http.rest.PagedIterable.iterableByPage.while
    }

    /**
     * Code snippets for showing usage of {@link PagedIterable} in class docs
     */
    public void iterateByPageContinuationToken() {
        PagedIterable<Integer> pagedIterable = createPagedIterableInstance();

        // BEGIN: com.azure.core.http.rest.PagedIterable.pagesWithContinuationToken
        String continuationToken = getContinuationToken();
        pagedIterable
            .iterableByPage(continuationToken)
            .forEach(page -> System.out.printf("Processing page containing item values: %s%n",
                page.getElements().stream().map(String::valueOf).collect(Collectors.joining(", "))));
        // END: com.azure.core.http.rest.PagedIterable.pagesWithContinuationToken
    }

    /**
     * Code snippets for creating an instance of {@link PagedFlux}
     *
     * @return An instance of {@link PagedFlux}
     */
    public PagedIterable<Integer> createPagedIterableInstance() {
        // BEGIN: com.azure.core.http.rest.PagedIterable.instantiation
        // A supplier that fetches the first page of data from source/service
        Supplier<PagedResponse<Integer>> firstPageRetriever = () -> getFirstPage();

        // A function that fetches subsequent pages of data from source/service given a continuation token
        Function<String, PagedResponse<Integer>> nextPageRetriever =
            continuationToken -> getNextPage(continuationToken);

        PagedIterable<Integer> pagedIterable = new PagedIterable<>(firstPageRetriever,
            nextPageRetriever);
        // END: com.azure.core.http.rest.PagedIterable.instantiation

        // BEGIN: com.azure.core.http.rest.PagedIterable.singlepage.instantiation
        // A supplier that fetches the first page of data from source/service
        Supplier<PagedResponse<Integer>> firstPageRetrieverFunction = () -> getFirstPage();

        PagedIterable<Integer> pagedIterableInstance = new PagedIterable<>(firstPageRetrieverFunction,
            nextPageRetriever);
        // END: com.azure.core.http.rest.PagedIterable.singlepage.instantiation
        return pagedIterableInstance;
    }

    /**
     * Code snippets for creating an instance of {@link PagedFlux}
     *
     * @return An instance of {@link PagedFlux}
     */
    private PagedFlux<Integer> createAnInstance() {

        // A supplier that fetches the first page of data from source/service
        Supplier<Mono<PagedResponse<Integer>>> firstPageRetriever = () -> Mono.just(getFirstPage());

        // A function that fetches subsequent pages of data from source/service given a continuation token
        Function<String, Mono<PagedResponse<Integer>>> nextPageRetriever =
            continuationToken -> Mono.just(getNextPage(continuationToken));

        return new PagedFlux<>(firstPageRetriever, nextPageRetriever);
    }

    /**
     * Retrieves the next page from a paged API.
     *
     * @param continuationToken Token to fetch the next page
     * @return A {@link Mono} of {@link PagedResponse} containing items of type {@code Integer}
     */
    private PagedResponse<Integer> getNextPage(String continuationToken) {
        return getPage(continuationToken, null);
    }

    /**
     * Retrieves the initial page from a paged API.
     *
     * @return A {@link Mono} of {@link PagedResponse} containing items of type {@code Integer}
     */
    private PagedResponse<Integer> getFirstPage() {
        return getPage(null, null);
    }

    private PagedResponse<Integer> getPage(Integer pageSize) {
        return getPage(null, pageSize);
    }

    /**
     * Retrieves a page from a paged API.
     *
     * @param continuationToken Token to fetch the next page, if {@code null} the first page is retrieved.
     * @return A {@link Mono} of {@link PagedResponse} containing items of type {@code Integer}
     */
    private PagedResponse<Integer> getPage(String continuationToken, Integer pageSize) {
        // Given this isn't calling an actual API we will arbitrarily generate a continuation token or end paging.
        boolean lastPage = Math.random() > 0.5;

        // If it is the last page there should be no additional continuation tokens returned.
        String nextContinuationToken = lastPage ? null : continuationToken;

        // Arbitrarily begin the next page of integers.
        int elementCount = (pageSize == null) ? (int) Math.ceil(Math.random() * 15) : pageSize;
        List<Integer> elements = IntStream.range(elementCount, elementCount + elementCount)
            .map(val -> (int) (Math.random() * val))
            .boxed()
            .collect(Collectors.toList());

        // This is a rough approximation of a service response.
        return new PagedResponseBase<Void, Integer>(new HttpRequest(HttpMethod.GET, "https://requestUrl.com"),
            200, new HttpHeaders(), elements, nextContinuationToken, null);
    }

    /**
     * Implementation not provided
     *
     * @return A continuation token
     */
    private String getContinuationToken() {
        return UUID.randomUUID().toString();
    }
}
