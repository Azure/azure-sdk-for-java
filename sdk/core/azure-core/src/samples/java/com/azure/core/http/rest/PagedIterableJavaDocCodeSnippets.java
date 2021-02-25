// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.rest;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Code snippets for {@link PagedIterable}
 */
public class PagedIterableJavaDocCodeSnippets {


    /**
     * Provides an example for iterate over each response using streamByPage function.
     **/
    public void streamByPageSnippet() {
        PagedFlux<Integer> pagedFlux = createAnInstance();
        PagedIterable<Integer> pagedIterableResponse = new PagedIterable<>(pagedFlux);

        // BEGIN: com.azure.core.http.rest.pagedIterable.streamByPage
        // process the streamByPage
        pagedIterableResponse.streamByPage().forEach(resp -> {
            System.out.printf("Response headers are %s. Url %s  and status code %d %n", resp.getHeaders(),
                resp.getRequest().getUrl(), resp.getStatusCode());
            resp.getElements().forEach(value -> System.out.printf("Response value is %d %n", value));
        });

        // END: com.azure.core.http.rest.pagedIterable.streamByPage
    }

    /**
     * Provides an example for iterate over each response using iterableByPage function.
     **/
    public void iterateByPageSnippet() {

        PagedFlux<Integer> pagedFlux = createAnInstance();
        PagedIterable<Integer> pagedIterableResponse = new PagedIterable<>(pagedFlux);

        // BEGIN: com.azure.core.http.rest.pagedIterable.iterableByPage
        // process the iterableByPage
        pagedIterableResponse.iterableByPage().forEach(resp -> {
            System.out.printf("Response headers are %s. Url %s  and status code %d %n", resp.getHeaders(),
                resp.getRequest().getUrl(), resp.getStatusCode());
            resp.getElements().forEach(value -> System.out.printf("Response value is %d %n", value));
        });
        // END: com.azure.core.http.rest.pagedIterable.iterableByPage
    }

    /**
     * Provides an example for iterate over each response using iterableByPage function and while loop.
     **/
    public void iterableByPageWhileSnippet() {

        PagedFlux<Integer> pagedFlux = createAnInstance();
        PagedIterable<Integer> pagedIterableResponse = new PagedIterable<>(pagedFlux);

        // BEGIN: com.azure.core.http.rest.pagedIterable.iterableByPage.while
        // iterate over each page
        for (PagedResponse<Integer> resp : pagedIterableResponse.iterableByPage()) {
            System.out.printf("Response headers are %s. Url %s  and status code %d %n", resp.getHeaders(),
                resp.getRequest().getUrl(), resp.getStatusCode());
            resp.getElements().forEach(value -> System.out.printf("Response value is %d %n", value));
        }
        // END: com.azure.core.http.rest.pagedIterable.iterableByPage.while
    }

    /**
     * Code snippets for creating an instance of {@link PagedFlux}
     *
     * @return An instance of {@link PagedFlux}
     */
    public PagedFlux<Integer> createAnInstance() {

        // A supplier that fetches the first page of data from source/service
        Supplier<Mono<PagedResponse<Integer>>> firstPageRetriever = () -> getFirstPage();

        // A function that fetches subsequent pages of data from source/service given a continuation token
        Function<String, Mono<PagedResponse<Integer>>> nextPageRetriever =
            continuationToken -> getNextPage(continuationToken);

        return new PagedFlux<>(firstPageRetriever, nextPageRetriever);
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
