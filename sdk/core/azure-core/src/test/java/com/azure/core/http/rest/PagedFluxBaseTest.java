// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.rest;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.implementation.http.PagedResponseBase;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Unit tests for {@link PagedFlux}
 */
public class PagedFluxBaseTest {
    private List<PagedResponse<Integer>> pagedResponses;

    @Rule
    public TestName testName = new TestName();

    @Before
    public void setup() {
        System.out.println("-------------- Running " + testName.getMethodName() + " -----------------------------");
    }

    @Test
    public void testEmptyResults() throws MalformedURLException {
        PagedFluxBase<Integer, PagedResponse<Integer>> pagedFluxBase = getIntegerPagedFlux(0);
        StepVerifier.create(pagedFluxBase.log()).verifyComplete();
        StepVerifier.create(pagedFluxBase.byPage().log()).verifyComplete();
        StepVerifier.create(pagedFluxBase.byPage(null).log()).verifyComplete();
    }

    @Test
    public void testPagedFluxSubscribeToItems() throws MalformedURLException {
        PagedFluxBase<Integer, PagedResponse<Integer>> pagedFluxBase = getIntegerPagedFlux(5);
        StepVerifier.create(pagedFluxBase.log())
            .expectNext(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14)
            .verifyComplete();
    }

    @Test
    public void testPagedFluxSubscribeToPagesFromStart() throws MalformedURLException {
        PagedFluxBase<Integer, PagedResponse<Integer>> pagedFluxBase = getIntegerPagedFlux(5);
        StepVerifier.create(pagedFluxBase.byPage().log())
            .expectNext(pagedResponses.get(0), pagedResponses.get(1), pagedResponses.get(2),
                pagedResponses.get(3), pagedResponses.get(4))
            .verifyComplete();
    }

    @Test
    public void testPagedFluxSubscribeToPagesFromContinuationToken() throws MalformedURLException {
        PagedFluxBase<Integer, PagedResponse<Integer>> pagedFluxBase = getIntegerPagedFlux(5);
        StepVerifier.create(pagedFluxBase.byPage("3").log())
            .expectNext(pagedResponses.get(3), pagedResponses.get(4))
            .verifyComplete();
    }

    @Test
    public void testPagedFluxSubscribeToPagesWithSinglePageResult() throws MalformedURLException {
        PagedFluxBase<Integer, PagedResponse<Integer>> pagedFluxBase = getIntegerPagedFlux(1);
        StepVerifier.create(pagedFluxBase.byPage().log())
            .expectNext(pagedResponses.get(0))
            .verifyComplete();

        pagedFluxBase = getIntegerPagedFlux(1);
        StepVerifier.create(pagedFluxBase.byPage(null).log())
            .verifyComplete();

        pagedFluxBase = getIntegerPagedFlux(1);
        StepVerifier.create(pagedFluxBase.log())
            .expectNext(0, 1, 2)
            .verifyComplete();
    }

    @Test
    public void testPagedFluxSubscribeToPagesWithSinglePageResultWithoutNextPageRetriever() throws MalformedURLException {
        PagedFluxBase<Integer, PagedResponse<Integer>> pagedFluxBase = getIntegerPagedFluxSinglePage();
        StepVerifier.create(pagedFluxBase.byPage().log())
            .expectNext(pagedResponses.get(0))
            .verifyComplete();

        pagedFluxBase = getIntegerPagedFluxSinglePage();
        StepVerifier.create(pagedFluxBase.byPage(null).log())
            .verifyComplete();

        pagedFluxBase = getIntegerPagedFluxSinglePage();
        StepVerifier.create(pagedFluxBase.log())
            .expectNext(0, 1, 2)
            .verifyComplete();
    }

    @Test
    public void testPagedFluxSubscribeToPagesWithTwoPages() throws MalformedURLException {
        PagedFluxBase<Integer, PagedResponse<Integer>> pagedFluxBase = getIntegerPagedFlux(2);
        StepVerifier.create(pagedFluxBase.byPage().log())
            .expectNext(pagedResponses.get(0), pagedResponses.get(1))
            .verifyComplete();

        pagedFluxBase = getIntegerPagedFlux(2);
        StepVerifier.create(pagedFluxBase.byPage("1").log())
            .expectNext(pagedResponses.get(1))
            .verifyComplete();

        pagedFluxBase = getIntegerPagedFlux(2);
        StepVerifier.create(pagedFluxBase.log())
            .expectNext(0, 1, 2, 3, 4, 5)
            .verifyComplete();
    }

    @Test
    public void testPagedFluxSubscribeToPagesFromNullContinuationToken() throws MalformedURLException {
        PagedFluxBase<Integer, PagedResponse<Integer>> pagedFluxBase = getIntegerPagedFlux(5);
        StepVerifier.create(pagedFluxBase.byPage(null).log())
            .verifyComplete();
    }

    private PagedFluxBase<Integer, PagedResponse<Integer>> getIntegerPagedFlux(int noOfPages) throws MalformedURLException {
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

    private PagedFluxBase<Integer, PagedResponse<Integer>> getIntegerPagedFluxSinglePage() throws MalformedURLException {
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
        return new PagedResponseBase<>(httpRequest, HttpResponseStatus.OK.code(),
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
