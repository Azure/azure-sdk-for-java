// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.rest;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.context.Context;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.azure.core.util.FluxUtil.withContext;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link PagedFlux}
 */
public class PagedFluxTest {
    private static final int DEFAULT_PAGE_COUNT = 4;

    private List<PagedResponse<Integer>> pagedResponses;
    private List<PagedResponse<String>> pagedStringResponses;

    @BeforeEach
    public void init(TestInfo testInfo) {
        System.out.println("-------------- Running " + testInfo.getDisplayName() + " -----------------------------");
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
    public void testPagedFluxConverter() throws MalformedURLException {
        PagedFlux<Integer> pagedFlux = getIntegerPagedFlux(5);

        StepVerifier.create(pagedFlux.mapPage(String::valueOf))
            .expectNext("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14")
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
    public void testPagedFluxSubscribeToPagesFromStartWithConvertedType() throws MalformedURLException {
        PagedFlux<Integer> pagedFlux = getIntegerPagedFlux(5);
        StepVerifier.create(pagedFlux.mapPage(String::valueOf).byPage().log())
            .expectNextCount(5)
            .verifyComplete();

        StepVerifier.create(pagedFlux.mapPage(String::valueOf).byPage().log())
            .expectNextMatches(pagedResponse -> pagedStringResponses.get(0).getValue().equals(pagedResponse.getValue()))
            .expectNextMatches(pagedResponse -> pagedStringResponses.get(1).getValue().equals(pagedResponse.getValue()))
            .expectNextMatches(pagedResponse -> pagedStringResponses.get(2).getValue().equals(pagedResponse.getValue()))
            .expectNextMatches(pagedResponse -> pagedStringResponses.get(3).getValue().equals(pagedResponse.getValue()))
            .expectNextMatches(pagedResponse -> pagedStringResponses.get(4).getValue().equals(pagedResponse.getValue()))
            .verifyComplete();

    }

    @Test
    public void testPagedFluxSinglePageConvertedType() throws MalformedURLException {
        PagedFlux<Integer> pagedFlux = getIntegerPagedFlux(1);
        StepVerifier.create(pagedFlux.mapPage(String::valueOf).byPage().log())
            .expectNextCount(1)
            .verifyComplete();

        StepVerifier.create(pagedFlux.mapPage(String::valueOf).byPage().log())
            .expectNextMatches(pagedResponse -> pagedStringResponses.get(0).getValue().equals(pagedResponse.getValue()))
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
    public void testPagedFluxSubscribeToPagesWithSinglePageResultWithoutNextPageRetriever()
        throws MalformedURLException {
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

    @Test
    public void testPagedFluxWithContext() throws Exception {

        CountDownLatch singlePageLatch = new CountDownLatch(1);
        PagedFlux<Integer> pagedFlux = new PagedFlux<>(() -> withContext(context -> {
            assertNotNull(context);
            assertEquals(1, context.getValues().size());
            assertEquals("context", context.getData("hello").get().toString());
            singlePageLatch.countDown();
            return Mono.empty();
        }));


        CountDownLatch multiPageLatch = new CountDownLatch(2);
        pagedFlux
            .byPage()
            .contextWrite(Context.of("hello", "context"))
            .subscribe(pagedResponse -> assertTrue(pagedResponse instanceof PagedResponse));

        boolean completed = singlePageLatch.await(1, TimeUnit.SECONDS);
        assertTrue(completed);

        HttpHeaders httpHeaders = new HttpHeaders().put("header1", "value1")
            .put("header2", "value2");
        HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, new URL("http://localhost"));
        pagedFlux = new PagedFlux<>(() -> withContext(context -> {
            assertNotNull(context);
            assertEquals(1, context.getValues().size());
            assertEquals("context", context.getData("hello").get().toString());
            multiPageLatch.countDown();
            PagedResponse<Integer> response = new PagedResponseBase<>(httpRequest, 200, httpHeaders,
                Collections.emptyList(),
                "0", null);
            return Mono.just(response);
        }), continuationToken -> withContext(context -> {
            assertNotNull(context);
            assertEquals(1, context.getValues().size());
            assertEquals("context", context.getData("hello").get().toString());
            multiPageLatch.countDown();
            return Mono.empty();
        }));

        pagedFlux
            .byPage()
            .contextWrite(Context.of("hello", "context"))
            .subscribe(pagedResponse -> assertTrue(pagedResponse instanceof PagedResponse));
        completed = multiPageLatch.await(1, TimeUnit.SECONDS);
        assertTrue(completed);
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

        pagedStringResponses = IntStream.range(0, noOfPages)
            .boxed()
            .map(i -> createPagedResponseWithString(httpRequest, httpHeaders, deserializedHeaders, i, noOfPages))
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

        pagedStringResponses = IntStream.range(0, 1)
            .boxed()
            .map(i -> createPagedResponseWithString(httpRequest, httpHeaders, deserializedHeaders, i, 1))
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

    private PagedResponseBase<String, String> createPagedResponseWithString(HttpRequest httpRequest,
        HttpHeaders httpHeaders, String deserializedHeaders, int i, int noOfPages) {
        return new PagedResponseBase<>(httpRequest, 200,
            httpHeaders,
            getStringItems(i),
            i < noOfPages - 1 ? String.valueOf(i + 1) : null,
            deserializedHeaders);
    }

    private Mono<PagedResponse<Integer>> getNextPage(String continuationToken,
        List<PagedResponse<Integer>> pagedResponses) {

        if (continuationToken == null || continuationToken.isEmpty()) {
            return Mono.empty();
        }

        int parsedToken = Integer.parseInt(continuationToken);
        if (parsedToken >= pagedResponses.size()) {
            return Mono.empty();
        }

        return Mono.just(pagedResponses.get(parsedToken));
    }

    private List<Integer> getItems(Integer i) {
        return IntStream.range(i * 3, i * 3 + 3).boxed().collect(Collectors.toList());
    }

    private List<String> getStringItems(Integer i) {
        return IntStream.range(i * 3, i * 3 + 3).boxed().map(String::valueOf).collect(Collectors.toList());
    }

    @Test
    public void fluxByItemOnlyRetrievesOnePage() throws InterruptedException {
        OnlyOnePageRetriever pageRetriever = new OnlyOnePageRetriever(DEFAULT_PAGE_COUNT);
        OnlyOnePagedFlux pagedFlux = new OnlyOnePagedFlux(() -> pageRetriever);

        pagedFlux.ignoreElements().block();
        assertEquals(DEFAULT_PAGE_COUNT, pageRetriever.getGetCount());

        pagedFlux.blockFirst();

        Thread.sleep(2000);

        assertEquals(1, pageRetriever.getGetCount() - DEFAULT_PAGE_COUNT);
    }

    @Test
    public void fluxByPageOnlyRetrievesOnePage() throws InterruptedException {
        OnlyOnePageRetriever pageRetriever = new OnlyOnePageRetriever(DEFAULT_PAGE_COUNT);
        OnlyOnePagedFlux pagedFlux = new OnlyOnePagedFlux(() -> pageRetriever);

        pagedFlux.byPage().ignoreElements().block();
        assertEquals(DEFAULT_PAGE_COUNT, pageRetriever.getGetCount());

        pagedFlux.byPage().blockFirst();

        Thread.sleep(2000);

        assertEquals(1, pageRetriever.getGetCount() - DEFAULT_PAGE_COUNT);
    }
}
