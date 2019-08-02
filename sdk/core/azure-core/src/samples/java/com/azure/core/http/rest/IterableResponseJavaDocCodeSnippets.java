// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.rest;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.implementation.http.PagedResponseBase;
import reactor.core.publisher.Flux;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Code snippets for {@link IterableResponse}
 */
public class IterableResponseJavaDocCodeSnippets {

    /**
     * Iterate over {@link java.util.stream.Stream}
     * @throws MalformedURLException if can not create URL object.
     */
    public void streamSnippet() throws MalformedURLException {
        HttpHeaders httpHeaders = new HttpHeaders().put("header1", "value1")
            .put("header2", "value2");
        HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, new URL("http://localhost"));

        String deserializedHeaders = "header1,value1,header2,value2";

        IterableResponse<PagedResponseBase<String, Integer>> myIterableResponse =
            new IterableResponse<>(Flux.just(createPagedResponse(httpRequest, httpHeaders, deserializedHeaders, 1, 3)));

        // BEGIN: com.azure.core.http.rest.iterableResponse.stream
        // process the stream
        myIterableResponse.stream().forEach(resp -> {
            if (resp.statusCode() == HttpURLConnection.HTTP_OK) {
                System.out.printf("Response headers are %s. Url %s \n", resp.deserializedHeaders(), resp.request().url());
                resp.items().forEach(value -> {
                    System.out.printf("Response value is %d \n", value);
                });
            }
        });
        // END: com.azure.core.http.rest.iterableResponse.stream
    }

    /**
     *  Iterate with {@link Iterator} interface.
     * @throws MalformedURLException if can not create URL object.
     */
    public void iteratorwhileSnippet() throws MalformedURLException {
        HttpHeaders httpHeaders = new HttpHeaders().put("header1", "value1")
            .put("header2", "value2");
        HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, new URL("http://localhost"));

        String deserializedHeaders = "header1,value1,header2,value2";

        IterableResponse<PagedResponseBase<String, Integer>> myIterableResponse =
            new IterableResponse<>(Flux.just(createPagedResponse(httpRequest, httpHeaders, deserializedHeaders, 1, 3)));

        // BEGIN: com.azure.core.http.rest.iterableResponse.iterator.while
        // Iterate over iterator
        Iterator<PagedResponseBase<String, Integer>> ite = myIterableResponse.iterator();
        while (ite.hasNext()) {
            PagedResponseBase<String, Integer> resp = ite.next();
            if (resp.statusCode() == HttpURLConnection.HTTP_OK) {
                System.out.printf("Response headers are %s. Url %s \n", resp.deserializedHeaders(), resp.request().url());
                resp.items().forEach(value -> {
                    System.out.printf("Response value is %d \n", value);
                });
            }
        }
        // END: com.azure.core.http.rest.iterableResponse.iterator.while
    }

    /**
     * Iterate over {@link java.util.stream.Stream}
     * @throws MalformedURLException if can not create URL object.
     */
    public void iteratorStreamFilterSnippet() throws MalformedURLException {
        HttpHeaders httpHeaders = new HttpHeaders().put("header1", "value1")
            .put("header2", "value2");
        HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, new URL("http://localhost"));

        String deserializedHeaders = "header1,value1,header2,value2";

        IterableResponse<PagedResponseBase<String, Integer>> myIterableResponse =
            new IterableResponse<>(Flux.just(createPagedResponse(httpRequest, httpHeaders, deserializedHeaders, 1, 3)));

        // BEGIN: com.azure.core.http.rest.iterableResponse.stream.filter
        // process the stream
        myIterableResponse.stream().filter(resp -> resp.statusCode() == HttpURLConnection.HTTP_OK)
            .limit(10)
            .forEach(resp -> {
                System.out.printf("Response headers are %s. Url %s \n", resp.deserializedHeaders(), resp.request().url());
                resp.items().forEach(value -> {
                    System.out.printf("Response value is %d \n", value);
                });
            });
        // END: com.azure.core.http.rest.iterableResponse.stream.filter
    }

    private PagedResponseBase<String, Integer> createPagedResponse(HttpRequest httpRequest, HttpHeaders httpHeaders,
                                                                   String deserializedHeaders, int i, int noOfPages) {
        return new PagedResponseBase<>(httpRequest, HttpURLConnection.HTTP_OK,
            httpHeaders,
            getItems(i),
            i < noOfPages - 1 ? String.valueOf(i + 1) : null,
            deserializedHeaders);
    }

    private List<Integer> getItems(Integer i) {
        return IntStream.range(i * 3, i * 3 + 3).boxed().collect(Collectors.toList());
    }
}
