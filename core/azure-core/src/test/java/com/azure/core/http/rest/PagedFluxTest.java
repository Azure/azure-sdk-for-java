// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.rest;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.implementation.http.PagedResponseBase;
import io.netty.handler.codec.http.HttpResponseStatus;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * Unit tests for {@link PagedFlux}
 */
public class PagedFluxTest {
    private List<PagedResponse<Integer>> pagedResponses;

    @Test
    public void testEmptyFirstPage() {
        PagedFlux<Integer> pagedFlux = new PagedFlux<>(Mono.empty(),
            pageLink -> getNextPage(pageLink, pagedResponses));
        StepVerifier.create(pagedFlux).verifyComplete();
    }

    @Test
    public void testPagedFluxSubscribeToItems() throws MalformedURLException {
        PagedFlux<Integer> pagedFlux = getIntegerPagedFlux();
        StepVerifier.create(pagedFlux)
            .expectNext(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14)
            .verifyComplete();
    }

    @Test
    public void testPagedFluxSubscribeToPages() throws MalformedURLException {
        PagedFlux<Integer> pagedFlux = getIntegerPagedFlux();
        StepVerifier.create(pagedFlux.byPage())
            .expectNext(pagedResponses.get(0), pagedResponses.get(1), pagedResponses.get(2),
                pagedResponses.get(3), pagedResponses.get(4))
            .verifyComplete();
    }

    @Test
    public void testPagedFluxSubscribeToPageWithContinuationToken() throws MalformedURLException {
        PagedFlux<Integer> pagedFlux = getIntegerPagedFlux();
        StepVerifier.create(pagedFlux.byPage("3"))
            .expectNext(pagedResponses.get(3), pagedResponses.get(4))
            .verifyComplete();
    }

    private PagedFlux<Integer> getIntegerPagedFlux() throws MalformedURLException {
        HttpHeaders httpHeaders = new HttpHeaders().put("header1", "value1")
            .put("header2", "value2");
        HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, new URL("http://localhost"));

        String deserializedHeaders = "header1,value1,header2,value2";
        pagedResponses = IntStream.range(0, 5)
            .boxed()
            .map(i -> createPagedResponse(httpRequest, httpHeaders, deserializedHeaders, i))
            .collect(Collectors.toList());
        Mono<PagedResponse<Integer>> firstPage = Mono.just(pagedResponses.get(0));
        return new PagedFlux<>(firstPage,
            pageLink -> getNextPage(pageLink, pagedResponses));
    }

    private PagedResponseBase<String, Integer> createPagedResponse(HttpRequest httpRequest,
        HttpHeaders httpHeaders, String deserializedHeaders, Integer i) {
        return new PagedResponseBase<>(httpRequest, HttpResponseStatus.OK.code(),
            httpHeaders,
            getItems(i),
            i < 4 ? String.valueOf(i + 1) : null,
            deserializedHeaders);
    }

    private Mono<PagedResponse<Integer>> getNextPage(String pageLink,
        List<PagedResponse<Integer>> pagedResponses) {
        if (pageLink == null || pageLink.isEmpty()) {
            return Mono.empty();
        }
        return Mono.just(pagedResponses.get(Integer.valueOf(pageLink)));
    }

    private List<Integer> getItems(Integer i) {
        return IntStream.range(i * 3, i * 3 + 3).boxed().collect(Collectors.toList());
    }

}
