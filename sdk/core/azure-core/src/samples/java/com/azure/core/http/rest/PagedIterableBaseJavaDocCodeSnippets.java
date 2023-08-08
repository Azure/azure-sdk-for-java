// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.rest;

import reactor.core.publisher.Mono;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Code snippets for {@link PagedIterableBase}
 */
public class PagedIterableBaseJavaDocCodeSnippets {

    @SuppressWarnings("deprecation")
    static class CustomPagedFlux<String> extends PagedFluxBase<String, PagedResponse<String>> {
        CustomPagedFlux(Supplier<Mono<PagedResponse<String>>> firstPageRetriever) {
            super(firstPageRetriever);
        }

        CustomPagedFlux(Supplier<Mono<PagedResponse<String>>> firstPageRetriever,
            Function<java.lang.String, Mono<PagedResponse<String>>> nextPageRetriever) {
            super(firstPageRetriever, nextPageRetriever);
        }
    }

    /**
     * Provides an example for iterate over each response using streamByPage function.
     **/
    public void streamByPageSnippet() {
        // BEGIN: com.azure.core.http.rest.pagedIterableBase.streamByPage
        // process the streamByPage
        CustomPagedFlux<String> customPagedFlux = createCustomInstance();
        PagedIterableBase<String, PagedResponse<String>> customPagedIterableResponse =
            new PagedIterableBase<>(customPagedFlux);
        customPagedIterableResponse.streamByPage().forEach(resp -> {
            System.out.printf("Response headers are %s. Url %s  and status code %d %n", resp.getHeaders(),
                resp.getRequest().getUrl(), resp.getStatusCode());
            resp.getElements().forEach(value -> System.out.printf("Response value is %s %n", value));
        });
        // END: com.azure.core.http.rest.pagedIterableBase.streamByPage
    }


    /**
     * Provides an example for iterate over each response using iterableByPage function.
     **/
    public void iterateByPageSnippet() {

        CustomPagedFlux<String> customPagedFlux = createCustomInstance();
        PagedIterableBase<String, PagedResponse<String>> customPagedIterableResponse =
            new PagedIterableBase<>(customPagedFlux);

        // BEGIN: com.azure.core.http.rest.pagedIterableBase.iterableByPage
        // process the iterableByPage
        customPagedIterableResponse.iterableByPage().forEach(resp -> {
            System.out.printf("Response headers are %s. Url %s  and status code %d %n", resp.getHeaders(),
                resp.getRequest().getUrl(), resp.getStatusCode());
            resp.getElements().forEach(value -> System.out.printf("Response value is %s %n", value));
        });
        // END: com.azure.core.http.rest.pagedIterableBase.iterableByPage
    }

    /**
     * Provides an example for iterate over each response using iterableByPage function and while loop.
     **/
    public void iterableByPageWhileSnippet() {

        CustomPagedFlux<String> customPagedFlux = createCustomInstance();
        PagedIterableBase<String, PagedResponse<String>> customPagedIterableResponse =
            new PagedIterableBase<>(customPagedFlux);

        // BEGIN: com.azure.core.http.rest.pagedIterableBase.iterableByPage.while
        // iterate over each page
        for (PagedResponse<String> resp : customPagedIterableResponse.iterableByPage()) {
            System.out.printf("Response headers are %s. Url %s  and status code %d %n", resp.getHeaders(),
                resp.getRequest().getUrl(), resp.getStatusCode());
            resp.getElements().forEach(value -> System.out.printf("Response value is %s %n", value));
        }
        // END: com.azure.core.http.rest.pagedIterableBase.iterableByPage.while
    }


    /**
     * Code snippets for creating an instance of {@link PagedFlux}
     *
     * @return An instance of {@link PagedFlux}
     */
    CustomPagedFlux<String> createCustomInstance() {

        // A supplier that fetches the first page of data from source/service
        Supplier<Mono<PagedResponse<String>>> firstPageRetriever = () -> getFirstPage();

        // A function that fetches subsequent pages of data from source/service given a continuation token
        Function<String, Mono<PagedResponse<String>>> nextPageRetriever =
            continuationToken -> getNextPage(continuationToken);

        CustomPagedFlux<String> pagedFlux = new CustomPagedFlux<>(firstPageRetriever,
            nextPageRetriever);
        return pagedFlux;
    }

    /**
     * Implementation not provided
     *
     * @param continuationToken Token to fetch the next page
     * @return A {@link Mono} of {@link PagedResponse} containing items of type {@code Integer}
     */
    private Mono<PagedResponse<String>> getNextPage(String continuationToken) {
        return null;
    }

    /**
     * Implementation not provided
     *
     * @return A {@link Mono} of {@link PagedResponse} containing items of type {@code Integer}
     */
    private Mono<PagedResponse<String>> getFirstPage() {
        return null;
    }
}
