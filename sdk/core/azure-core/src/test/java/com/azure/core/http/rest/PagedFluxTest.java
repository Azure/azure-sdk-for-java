// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.rest;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.implementation.http.PagedResponseBase;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * Unit tests for {@link PagedFlux}
 */
public class PagedFluxTest {
    private List<PagedResponse<Integer>> pagedResponses;

    @Rule
    public TestName testName = new TestName();

    @Before
    public void setup() {
        System.out.println("-------------- Running " + testName.getMethodName() + " -----------------------------");
    }

    @Test
    public void testEmptyResults() throws MalformedURLException {
        PagedFlux<Integer> pagedFlux = getIntegerPagedFlux(0);
        StepVerifier.create(pagedFlux.log()).verifyComplete();
        StepVerifier.create(pagedFlux.byPage().log()).verifyComplete();
        StepVerifier.create(pagedFlux.byPage(null).log()).verifyComplete();
    }

    @Test
    public void testPagedFluxSubscribeToItems() throws MalformedURLException {
        PagedFlux<Integer> pagedFlux = getIntegerPagedFlux(5);
        StepVerifier.create(pagedFlux.log())
            .expectNext(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14)
            .verifyComplete();
    }

    @Test
    public void testPagedFluxSubscribeToPagesFromStart() throws MalformedURLException {
        PagedFlux<Integer> pagedFlux = getIntegerPagedFlux(5);
        StepVerifier.create(pagedFlux.byPage().log())
            .expectNext(pagedResponses.get(0), pagedResponses.get(1), pagedResponses.get(2),
                pagedResponses.get(3), pagedResponses.get(4))
            .verifyComplete();
    }

    @Test
    public void testPagedFluxSubscribeToPagesFromContinuationToken() throws MalformedURLException {
        PagedFlux<Integer> pagedFlux = getIntegerPagedFlux(5);
        StepVerifier.create(pagedFlux.byPage("3").log())
            .expectNext(pagedResponses.get(3), pagedResponses.get(4))
            .verifyComplete();
    }

    @Test
    public void testPagedFluxSubscribeToPagesWithSinglePageResult() throws MalformedURLException {
        PagedFlux<Integer> pagedFlux = getIntegerPagedFlux(1);
        StepVerifier.create(pagedFlux.byPage().log())
            .expectNext(pagedResponses.get(0))
            .verifyComplete();

        pagedFlux = getIntegerPagedFlux(1);
        StepVerifier.create(pagedFlux.byPage(null).log())
            .verifyComplete();

        pagedFlux = getIntegerPagedFlux(1);
        StepVerifier.create(pagedFlux.log())
            .expectNext(0, 1, 2)
            .verifyComplete();
    }

    @Test
    public void testPagedFluxSubscribeToPagesWithSinglePageResultWithoutNextPageRetriever() throws MalformedURLException {
        PagedFlux<Integer> pagedFlux = getIntegerPagedFluxSinglePage();
        StepVerifier.create(pagedFlux.byPage().log())
            .expectNext(pagedResponses.get(0))
            .verifyComplete();

        pagedFlux = getIntegerPagedFluxSinglePage();
        StepVerifier.create(pagedFlux.byPage(null).log())
            .verifyComplete();

        pagedFlux = getIntegerPagedFluxSinglePage();
        StepVerifier.create(pagedFlux.log())
            .expectNext(0, 1, 2)
            .verifyComplete();
    }

    @Test
    public void testPagedFluxSubscribeToPagesWithTwoPages() throws MalformedURLException {
        PagedFlux<Integer> pagedFlux = getIntegerPagedFlux(2);
        StepVerifier.create(pagedFlux.byPage().log())
            .expectNext(pagedResponses.get(0), pagedResponses.get(1))
            .verifyComplete();

        pagedFlux = getIntegerPagedFlux(2);
        StepVerifier.create(pagedFlux.byPage("1").log())
            .expectNext(pagedResponses.get(1))
            .verifyComplete();

        pagedFlux = getIntegerPagedFlux(2);
        StepVerifier.create(pagedFlux.log())
            .expectNext(0, 1, 2, 3, 4, 5)
            .verifyComplete();
    }

    @Test
    public void testPagedFluxSubscribeToPagesFromNullContinuationToken() throws MalformedURLException {
        PagedFlux<Integer> pagedFlux = getIntegerPagedFlux(5);
        StepVerifier.create(pagedFlux.byPage(null).log())
            .verifyComplete();
    }

    private PagedFlux<Integer> getIntegerPagedFlux(int noOfPages) throws MalformedURLException {
        HttpHeaders httpHeaders = new HttpHeaders().put("header1", "value1")
            .put("header2", "value2");
        HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, new URL("http://localhost"));

        String deserializedHeaders = "header1,value1,header2,value2";
        pagedResponses = IntStream.range(0, noOfPages)
            .boxed()
            .map(i -> createPagedResponse(httpRequest, httpHeaders, deserializedHeaders, i, noOfPages))
            .collect(Collectors.toList());

        return new PagedFlux<>(() -> pagedResponses.isEmpty() ? Mono.empty() : Mono.just(pagedResponses.get(0)),
            continuationToken -> getNextPage(continuationToken, pagedResponses));
    }

    private PagedFlux<Integer> getIntegerPagedFluxSinglePage() throws MalformedURLException {
        HttpHeaders httpHeaders = new HttpHeaders().put("header1", "value1")
            .put("header2", "value2");
        HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, new URL("http://localhost"));

        String deserializedHeaders = "header1,value1,header2,value2";
        pagedResponses = IntStream.range(0, 1)
            .boxed()
            .map(i -> createPagedResponse(httpRequest, httpHeaders, deserializedHeaders, i, 1))
            .collect(Collectors.toList());

        return new PagedFlux<>(() -> pagedResponses.isEmpty() ? Mono.empty() : Mono.just(pagedResponses.get(0)));
    }

    private PagedResponseBase<String, Integer> createPagedResponse(HttpRequest httpRequest,
        HttpHeaders httpHeaders, String deserializedHeaders, int i, int noOfPages) {
        return new PagedResponseBase<>(httpRequest, 200,
            httpHeaders,
            getItems(i),
            i < noOfPages - 1 ? String.valueOf(i + 1) : null,
            deserializedHeaders);
    }

    private Mono<PagedResponse<Integer>> getNextPage(String continuationToken,
        List<PagedResponse<Integer>> pagedResponses) {

        if (continuationToken == null || continuationToken.isEmpty()) {
            return Mono.empty();
        }

        return Mono.just(pagedResponses.get(Integer.valueOf(continuationToken)));
    }

    private List<Integer> getItems(Integer i) {
        return IntStream.range(i * 3, i * 3 + 3).boxed().collect(Collectors.toList());
    }

}
