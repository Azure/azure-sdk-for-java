// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.rest.PagedResponseBase;
import reactor.core.publisher.Flux;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.azure.core.CoreTestUtils.createUrl;

/**
 * Code snippets for {@link IterableStream}
 */
public class IterableStreamJavaDocCodeSnippets {
    private static final HttpHeaderName HEADER1 = HttpHeaderName.fromString("header1");
    private static final HttpHeaderName HEADER2 = HttpHeaderName.fromString("header2");

    /**
     * Iterate over {@link java.util.stream.Stream}
     *
     * @throws MalformedURLException if can not create URL object.
     */
    public void streamSnippet() throws MalformedURLException {
        HttpHeaders httpHeaders = new HttpHeaders()
            .set(HEADER1, "value1")
            .set(HEADER2, "value2");
        HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, createUrl("http://localhost"));

        String deserializedHeaders = "header1,value1,header2,value2";

        IterableStream<PagedResponseBase<String, Integer>> myIterableStream =
            new IterableStream<>(Flux.just(createPagedResponse(httpRequest, httpHeaders, deserializedHeaders, 1, 3)));

        // BEGIN: com.azure.core.util.iterableStream.stream
        // process the stream
        myIterableStream.stream().forEach(resp -> {
            if (resp.getStatusCode() == HttpURLConnection.HTTP_OK) {
                System.out.printf("Response headers are %s. Url %s%n", resp.getDeserializedHeaders(),
                    resp.getRequest().getUrl());
                resp.getElements().forEach(value -> System.out.printf("Response value is %d%n", value));
            }
        });
        // END: com.azure.core.util.iterableStream.stream
    }

    /**
     * Iterate with {@link Iterator} interface.
     *
     * @throws MalformedURLException if can not create URL object.
     */
    public void iteratorwhileSnippet() throws MalformedURLException {
        HttpHeaders httpHeaders = new HttpHeaders()
            .set(HEADER1, "value1")
            .set(HEADER2, "value2");
        HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, createUrl("http://localhost"));

        String deserializedHeaders = "header1,value1,header2,value2";

        IterableStream<PagedResponseBase<String, Integer>> myIterableStream =
            new IterableStream<>(Flux.just(createPagedResponse(httpRequest, httpHeaders, deserializedHeaders, 1, 3)));

        // BEGIN: com.azure.core.util.iterableStream.iterator.while
        // Iterate over iterator
        for (PagedResponseBase<String, Integer> resp : myIterableStream) {
            if (resp.getStatusCode() == HttpURLConnection.HTTP_OK) {
                System.out.printf("Response headers are %s. Url %s%n", resp.getDeserializedHeaders(),
                    resp.getRequest().getUrl());
                resp.getElements().forEach(value -> System.out.printf("Response value is %d%n", value));
            }
        }
        // END: com.azure.core.util.iterableStream.iterator.while
    }

    /**
     * Iterate over {@link java.util.stream.Stream}
     *
     * @throws MalformedURLException if can not create URL object.
     */
    public void iteratorStreamFilterSnippet() throws MalformedURLException {
        HttpHeaders httpHeaders = new HttpHeaders()
            .set(HEADER1, "value1")
            .set(HEADER2, "value2");
        HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, createUrl("http://localhost"));

        String deserializedHeaders = "header1,value1,header2,value2";

        IterableStream<PagedResponseBase<String, Integer>> myIterableStream =
            new IterableStream<>(Flux.just(createPagedResponse(httpRequest, httpHeaders, deserializedHeaders, 1, 3)));

        // BEGIN: com.azure.core.util.iterableStream.stream.filter
        // process the stream
        myIterableStream.stream().filter(resp -> resp.getStatusCode() == HttpURLConnection.HTTP_OK)
            .limit(10)
            .forEach(resp -> {
                System.out.printf("Response headers are %s. Url %s%n", resp.getDeserializedHeaders(),
                    resp.getRequest().getUrl());
                resp.getElements().forEach(value -> System.out.printf("Response value is %d%n", value));
            });
        // END: com.azure.core.util.iterableStream.stream.filter
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
