// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.rest;

import reactor.core.publisher.Mono;

import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Code snippets for {@link PagedIterable}
 */
public class PagedIterableJavaDocCodeSnippets {

    static class CustomPagedFlux<String> extends PagedFluxBase<String, PagedResponse<String>> {

        CustomPagedFlux(Supplier<Mono<PagedResponse<String>>> firstPageRetriever) {
            super(firstPageRetriever);
        }

        CustomPagedFlux(Supplier<Mono<PagedResponse<String>>> firstPageRetriever,
                         Function<java.lang.String, Mono<PagedResponse<String>>> nextPageRetriever) {
            super(firstPageRetriever, nextPageRetriever);
        }
    }


    /**Provides an example for iterate over each response using streamByPage function.**/
    public void streamByPageSnippet() {
        PagedFlux<Integer> pagedFlux = createAnInstance();
        CustomPagedFlux<String> customPagedFlux = createCustomInstance();
        PagedIterable<Integer> pagedIterableResponse =  new PagedIterable<>(pagedFlux);
        PagedIterable<String> customPagedIterableResponse =  new PagedIterable<>(customPagedFlux);

        // BEGIN: com.azure.core.http.rest.pagedIterable.streamByPage
        // process the streamByPage
        pagedIterableResponse.streamByPage().forEach(resp -> {
            System.out.printf("Response headers are %s. Url %s  and status code %d %n", resp.getHeaders(),
                resp.getRequest().getUrl(), resp.getStatusCode());
            resp.getItems().forEach(value -> {
                System.out.printf("Response value is %d %n", value);
            });
        });

        customPagedIterableResponse.streamByPage().forEach(resp -> {
            System.out.printf("Response headers are %s. Url %s  and status code %d %n", resp.getHeaders(),
                resp.getRequest().getUrl(), resp.getStatusCode());
            resp.getItems().forEach(value -> {
                System.out.printf("Response value is %s %n", value);
            });
        });
        // END: com.azure.core.http.rest.pagedIterable.streamByPage
    }

    /**Provides an example for iterate over each response using iterableByPage function.**/
    public void iterateByPageSnippet() {

        PagedFlux<Integer> pagedFlux = createAnInstance();
        CustomPagedFlux<String> customPagedFlux = createCustomInstance();
        PagedIterable<Integer> pagedIterableResponse =  new PagedIterable<>(pagedFlux);
        PagedIterable<String> customPagedIterableResponse =  new PagedIterable<>(customPagedFlux);

        // BEGIN: com.azure.core.http.rest.pagedIterable.iterableByPage
        // process the iterableByPage
        pagedIterableResponse.iterableByPage().forEach(resp -> {
            System.out.printf("Response headers are %s. Url %s  and status code %d %n", resp.getHeaders(),
                resp.getRequest().getUrl(), resp.getStatusCode());
            resp.getItems().forEach(value -> {
                System.out.printf("Response value is %d %n", value);
            });
        });

        customPagedIterableResponse.iterableByPage().forEach(resp -> {
            System.out.printf("Response headers are %s. Url %s  and status code %d %n", resp.getHeaders(),
                resp.getRequest().getUrl(), resp.getStatusCode());
            resp.getItems().forEach(value -> {
                System.out.printf("Response value is %s %n", value);
            });
        });
        // END: com.azure.core.http.rest.pagedIterable.iterableByPage
    }

    /**Provides an example for iterate over each response using iterableByPage function and while loop.**/
    public void iterableByPageWhileSnippet() {

        PagedFlux<Integer> pagedFlux = createAnInstance();
        CustomPagedFlux<String> customPagedFlux = createCustomInstance();
        PagedIterable<Integer> pagedIterableResponse =  new PagedIterable<>(pagedFlux);
        PagedIterable<String> customPagedIterableResponse =  new PagedIterable<>(customPagedFlux);

        // BEGIN: com.azure.core.http.rest.pagedIterable.iterableByPage.while
        // iterate over each page
        Iterator<PagedResponse<Integer>> ite = pagedIterableResponse.iterableByPage().iterator();
        while (ite.hasNext()) {
            PagedResponse<Integer> resp = ite.next();
            System.out.printf("Response headers are %s. Url %s  and status code %d %n", resp.getHeaders(),
                resp.getRequest().getUrl(), resp.getStatusCode());
            resp.getItems().forEach(value -> {
                System.out.printf("Response value is %d %n", value);
            });
        }

        Iterator<PagedResponse<String>> iterator = customPagedIterableResponse.iterableByPage().iterator();
        while (iterator.hasNext()) {
            PagedResponse<Integer> resp = ite.next();
            System.out.printf("Response headers are %s. Url %s  and status code %d %n", resp.getHeaders(),
                resp.getRequest().getUrl(), resp.getStatusCode());
            resp.getItems().forEach(value -> {
                System.out.printf("Response value is %s %n", value);
            });
        }
        // END: com.azure.core.http.rest.pagedIterable.iterableByPage.while
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
     * Code snippets for creating an instance of {@link PagedFlux}
     * @return An instance of {@link PagedFlux}
     */
    public CustomPagedFlux<String> createCustomInstance() {

        // BEGIN: com.azure.core.http.rest.custompagedflux.instantiation
        // A supplier that fetches the first page of data from source/service
        Supplier<Mono<PagedResponse<String>>> firstPageRetriever = () -> null;

        // A function that fetches subsequent pages of data from source/service given a continuation token
        Function<String, Mono<PagedResponse<String>>> nextPageRetriever =
            continuationToken -> null;

        CustomPagedFlux<String> pagedFlux = new CustomPagedFlux<>(firstPageRetriever,
            nextPageRetriever);
        // END: com.azure.core.http.rest.custompagedflux.instantiation
        return pagedFlux;
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
