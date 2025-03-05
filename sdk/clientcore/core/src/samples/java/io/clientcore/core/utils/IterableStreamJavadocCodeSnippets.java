// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.core.utils;

import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.paging.PagedResponse;
import io.clientcore.core.models.binarydata.BinaryData;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Code snippets for {@link IterableStream}
 */
public final class IterableStreamJavadocCodeSnippets {
    private static final HttpHeaderName HEADER1 = HttpHeaderName.fromString("header1");
    private static final HttpHeaderName HEADER2 = HttpHeaderName.fromString("header2");

    /**
     * Iterate over {@link java.util.stream.Stream}
     */
    public void streamSnippet() {
        HttpHeaders httpHeaders = new HttpHeaders()
            .set(HEADER1, "value1")
            .set(HEADER2, "value2");
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri("http://localhost");

        IterableStream<PagedResponse<Integer>> myIterableStream =
            new IterableStream<>(Collections.singletonList(createPagedResponse(httpRequest, httpHeaders)));

        // BEGIN: io.clientcore.core.utils.IterableStream.stream
        // process the stream
        myIterableStream.stream().forEach(resp -> {
            if (resp.getStatusCode() == HttpURLConnection.HTTP_OK) {
                System.out.printf("Response headers are %s. URI %s%n", resp.getHeaders(), resp.getRequest().getUri());
                resp.getValue().forEach(value -> System.out.printf("Response value is %d%n", value));
            }
        });
        // END: io.clientcore.core.utils.IterableStream.stream
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
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri("http://localhost");

        IterableStream<PagedResponse<Integer>> myIterableStream =
            new IterableStream<>(Collections.singletonList(createPagedResponse(httpRequest, httpHeaders)));

        // BEGIN: io.clientcore.core.utils.IterableStream.iterator.while
        // Iterate over iterator
        for (PagedResponse<Integer> resp : myIterableStream) {
            if (resp.getStatusCode() == HttpURLConnection.HTTP_OK) {
                System.out.printf("Response headers are %s. URI %s%n", resp.getHeaders(),
                    resp.getRequest().getUri());
                resp.getValue().forEach(value -> System.out.printf("Response value is %d%n", value));
            }
        }
        // END: io.clientcore.core.utils.IterableStream.iterator.while
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
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri("http://localhost");

        IterableStream<PagedResponse<Integer>> myIterableStream =
            new IterableStream<>(Collections.singletonList(createPagedResponse(httpRequest, httpHeaders)));

        // BEGIN: io.clientcore.core.utils.IterableStream.stream.filter
        // process the stream
        myIterableStream.stream().filter(resp -> resp.getStatusCode() == HttpURLConnection.HTTP_OK)
            .limit(10)
            .forEach(resp -> {
                System.out.printf("Response headers are %s. URI %s%n", resp.getHeaders(),
                    resp.getRequest().getUri());
                resp.getValue().forEach(value -> System.out.printf("Response value is %d%n", value));
            });
        // END: io.clientcore.core.utils.IterableStream.stream.filter
    }

    private PagedResponse<Integer> createPagedResponse(HttpRequest httpRequest, HttpHeaders httpHeaders) {
        List<Integer> items = getItems();
        return new PagedResponse<>(httpRequest, HttpURLConnection.HTTP_OK, httpHeaders, BinaryData.fromObject(items),
            getItems(), String.valueOf(1 + 1), null, null, null, null);
    }

    private List<Integer> getItems() {
        return IntStream.range(3, 3 + 3).boxed().collect(Collectors.toList());
    }
}
