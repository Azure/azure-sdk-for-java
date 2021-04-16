// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.rest;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link PagedIterable}.
 */
public class PagedIterableTest {
    private static final int DEFAULT_PAGE_COUNT = 4;

    private final HttpHeaders httpHeaders = new HttpHeaders().set("header1", "value1").set("header2", "value2");
    private final HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, "http://localhost");
    private final String deserializedHeaders = "header1,value1,header2,value2";

    private List<PagedResponse<Integer>> pagedResponses;
    private List<PagedResponse<String>> pagedStringResponses;

    @ParameterizedTest
    @ValueSource(ints = {0, 5})
    public void streamByPage(int numberOfPages) {
        PagedFlux<Integer> pagedFlux = getIntegerPagedFlux(numberOfPages);
        PagedIterable<Integer> pagedIterable = new PagedIterable<>(pagedFlux);
        List<PagedResponse<Integer>> pages = pagedIterable.streamByPage().collect(Collectors.toList());

        assertEquals(numberOfPages, pages.size());
        assertEquals(pagedResponses, pages);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 5})
    public void iterateByPage(int numberOfPages) {
        PagedFlux<Integer> pagedFlux = getIntegerPagedFlux(numberOfPages);
        PagedIterable<Integer> pagedIterable = new PagedIterable<>(pagedFlux);
        List<PagedResponse<Integer>> pages = new ArrayList<>();
        pagedIterable.iterableByPage().iterator().forEachRemaining(pages::add);

        assertEquals(numberOfPages, pages.size());
        assertEquals(pagedResponses, pages);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 5})
    public void streamByT(int numberOfPages) {
        PagedFlux<Integer> pagedFlux = getIntegerPagedFlux(numberOfPages);
        PagedIterable<Integer> pagedIterable = new PagedIterable<>(pagedFlux);
        List<Integer> values = pagedIterable.stream().collect(Collectors.toList());

        assertEquals(numberOfPages * 3, values.size());
        assertEquals(Stream.iterate(0, i -> i + 1).limit(numberOfPages * 3L).collect(Collectors.toList()), values);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 5})
    public void iterateByT(int numberOfPages) {
        PagedFlux<Integer> pagedFlux = getIntegerPagedFlux(numberOfPages);
        PagedIterable<Integer> pagedIterable = new PagedIterable<>(pagedFlux);
        List<Integer> values = new ArrayList<>();
        pagedIterable.iterator().forEachRemaining(values::add);

        assertEquals(numberOfPages * 3, values.size());
        assertEquals(Stream.iterate(0, i -> i + 1).limit(numberOfPages * 3L).collect(Collectors.toList()), values);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 5})
    public void streamByPageMap(int numberOfPages) {
        PagedFlux<Integer> pagedFlux = getIntegerPagedFlux(numberOfPages);
        PagedIterable<Integer> pagedIterable = new PagedIterable<>(pagedFlux);
        List<PagedResponse<String>> pages = pagedIterable.mapPage(String::valueOf).streamByPage()
            .collect(Collectors.toList());

        assertEquals(numberOfPages, pages.size());
        for (int i = 0; i < numberOfPages; i++) {
            assertEquals(pagedStringResponses.get(i).getValue(), pages.get(i).getValue());
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 5})
    public void iterateByPageMap(int numberOfPages) {
        PagedFlux<Integer> pagedFlux = getIntegerPagedFlux(numberOfPages);
        PagedIterable<Integer> pagedIterable = new PagedIterable<>(pagedFlux);
        List<PagedResponse<String>> pages = new ArrayList<>();
        pagedIterable.mapPage(String::valueOf).iterableByPage().iterator().forEachRemaining(pages::add);

        assertEquals(numberOfPages, pages.size());
        for (int i = 0; i < numberOfPages; i++) {
            assertEquals(pagedStringResponses.get(i).getValue(), pages.get(i).getValue());
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 5})
    public void streamByTMap(int numberOfPages) {
        PagedFlux<Integer> pagedFlux = getIntegerPagedFlux(numberOfPages);
        PagedIterable<Integer> pagedIterable = new PagedIterable<>(pagedFlux);
        List<String> values = pagedIterable.mapPage(String::valueOf).stream().collect(Collectors.toList());

        assertEquals(numberOfPages * 3, values.size());
        assertEquals(Stream.iterate(0, i -> i + 1).limit(numberOfPages * 3L).map(String::valueOf)
            .collect(Collectors.toList()), values);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 5})
    public void iterateByTMap(int numberOfPages) {
        PagedFlux<Integer> pagedFlux = getIntegerPagedFlux(numberOfPages);
        PagedIterable<Integer> pagedIterable = new PagedIterable<>(pagedFlux);
        List<String> values = new ArrayList<>();
        pagedIterable.mapPage(String::valueOf).iterator().forEachRemaining(values::add);

        assertEquals(numberOfPages * 3, values.size());
        assertEquals(Stream.iterate(0, i -> i + 1).limit(numberOfPages * 3L).map(String::valueOf)
            .collect(Collectors.toList()), values);
    }

    @Test
    public void streamFirstPage() {
        TestPagedFlux<Integer> pagedFlux = getTestPagedFlux(5);
        PagedIterable<Integer> pagedIterable = new PagedIterable<>(pagedFlux);

        assertEquals(pagedResponses.get(0), pagedIterable.streamByPage().limit(1).collect(Collectors.toList()).get(0));

        // The goal for this test is that 0 next page retrieval calls are made.
        assertEquals(0, pagedFlux.getNextPageRetrievals());
    }

    @Test
    public void iterateFirstPage() {
        TestPagedFlux<Integer> pagedFlux = getTestPagedFlux(5);
        PagedIterable<Integer> pagedIterable = new PagedIterable<>(pagedFlux);

        assertEquals(pagedResponses.get(0), pagedIterable.iterableByPage().iterator().next());

        // The goal for this test is that 0 next page retrieval calls are made.
        assertEquals(0, pagedFlux.getNextPageRetrievals());
    }

    @Test
    public void streamFirstValue() {
        TestPagedFlux<Integer> pagedFlux = getTestPagedFlux(5);
        PagedIterable<Integer> pagedIterable = new PagedIterable<>(pagedFlux);

        Integer firstValue = pagedResponses.get(0).getValue().get(0);
        assertEquals(firstValue, pagedIterable.stream().limit(1).collect(Collectors.toList()).get(0));
    }

    @Test
    public void iterateFirstValue() {
        TestPagedFlux<Integer> pagedFlux = getTestPagedFlux(5);
        PagedIterable<Integer> pagedIterable = new PagedIterable<>(pagedFlux);

        Integer firstValue = pagedResponses.get(0).getValue().get(0);
        assertEquals(firstValue, pagedIterable.iterator().next());
        assertEquals(0, pagedFlux.getNextPageRetrievals());
    }

    @Test
    public void pagedIterableWithPageSize() {
        final int expectedPageSize = 5;

        HttpHeaders headers = new HttpHeaders();
        HttpRequest request = new HttpRequest(HttpMethod.GET, "http://localhost");
        final Function<String, PagedResponse<Integer>> pagedResponseSupplier = continuationToken ->
            new PagedResponseBase<>(request, 200, headers, Collections.emptyList(), continuationToken, null);

        PagedFlux<Integer> singlePageFlux = new PagedFlux<>(pageSize -> {
            assertEquals(expectedPageSize, pageSize);
            return Mono.just(pagedResponseSupplier.apply(null));
        });

        PagedIterable<Integer> singlePageIterable = new PagedIterable<>(singlePageFlux);
        Iterator<PagedResponse<Integer>> pageIterator = singlePageIterable.iterableByPage(expectedPageSize).iterator();
        assertTrue(pageIterator.hasNext());
        pageIterator.next();
        assertFalse(pageIterator.hasNext());

        assertEquals(1L, singlePageIterable.streamByPage(expectedPageSize).count());

        final String expectedContinuationToken = "0";
        PagedFlux<Integer> multiPageFlux = new PagedFlux<>(pageSize -> {
            assertEquals(expectedPageSize, pageSize);
            return Mono.just(pagedResponseSupplier.apply(expectedContinuationToken));
        }, (continuationToken, pageSize) -> {
            assertEquals(expectedPageSize, pageSize);
            assertEquals(expectedContinuationToken, continuationToken);
            return Mono.just(pagedResponseSupplier.apply(null));
        });

        PagedIterable<Integer> multiPageIterator = new PagedIterable<>(multiPageFlux);
        pageIterator = multiPageIterator.iterableByPage(expectedPageSize).iterator();
        assertTrue(pageIterator.hasNext());
        pageIterator.next();
        assertTrue(pageIterator.hasNext());
        pageIterator.next();
        assertFalse(pageIterator.hasNext());

        assertEquals(2L, multiPageIterator.streamByPage(expectedPageSize).count());
    }

    private PagedFlux<Integer> getIntegerPagedFlux(int numberOfPages) {
        createPagedResponse(numberOfPages);

        return new PagedFlux<>(() -> pagedResponses.isEmpty() ? Mono.empty() : Mono.just(pagedResponses.get(0)),
            continuationToken -> getNextPage(continuationToken, pagedResponses));
    }

    private TestPagedFlux<Integer> getTestPagedFlux(int numberOfPages) {
        createPagedResponse(numberOfPages);

        return new TestPagedFlux<>(() -> pagedResponses.isEmpty() ? Mono.empty() : Mono.just(pagedResponses.get(0)),
            continuationToken -> getNextPage(continuationToken, pagedResponses));
    }

    private void createPagedResponse(int numberOfPages) {
        pagedResponses = IntStream.range(0, numberOfPages)
            .boxed()
            .map(i ->
                createPagedResponse(httpRequest, httpHeaders, deserializedHeaders, numberOfPages, this::getItems, i))
            .collect(Collectors.toList());

        pagedStringResponses = IntStream.range(0, numberOfPages)
            .boxed()
            .map(i -> createPagedResponse(httpRequest, httpHeaders, deserializedHeaders, numberOfPages,
                this::getStringItems, i))
            .collect(Collectors.toList());
    }

    private <T> PagedResponseBase<String, T> createPagedResponse(HttpRequest httpRequest, HttpHeaders headers,
        String deserializedHeaders, int numberOfPages, Function<Integer, List<T>> valueSupplier, int i) {
        return new PagedResponseBase<>(httpRequest, 200, headers, valueSupplier.apply(i),
            (i < numberOfPages - 1) ? String.valueOf(i + 1) : null,
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

    private List<Integer> getItems(int i) {
        return IntStream.range(i * 3, i * 3 + 3).boxed().collect(Collectors.toList());
    }

    private List<String> getStringItems(int i) {
        return IntStream.range(i * 3, i * 3 + 3).boxed().map(String::valueOf).collect(Collectors.toList());
    }

    /*
     * Test class used to verify that paged iterable will lazily request next pages.
     */
    private static class TestPagedFlux<T> extends PagedFlux<T> {
        private int nextPageRetrievals = 0;

        TestPagedFlux(Supplier<Mono<PagedResponse<T>>> firstPageRetriever,
            Function<String, Mono<PagedResponse<T>>> nextPageRetriever) {
            super(firstPageRetriever, nextPageRetriever);
        }

        @Override
        public Flux<PagedResponse<T>> byPage(String continuationToken) {
            nextPageRetrievals++;
            return super.byPage(continuationToken);
        }

        /*
         * Returns the number of times another page has been retrieved.
         */
        int getNextPageRetrievals() {
            return nextPageRetrievals;
        }
    }

    @Test
    public void streamFindFirstOnlyRetrievesOnePage() {
        OnlyOnePageRetriever pageRetriever = new OnlyOnePageRetriever(DEFAULT_PAGE_COUNT);
        OnlyOnePagedIterable pagedIterable = new OnlyOnePagedIterable(new OnlyOnePagedFlux(() -> pageRetriever));

        // Validation that there is more than one paged in the full return.
        pagedIterable.stream().count();
        assertEquals(DEFAULT_PAGE_COUNT, pageRetriever.getGetCount());

        Integer next = pagedIterable.stream().findFirst().orElse(0);

        sleep();

        /*
         * Given that each page contains more than one element we are able to only retrieve a single page.
         */
        assertEquals(1, pageRetriever.getGetCount() - DEFAULT_PAGE_COUNT);
    }

    @Test
    public void streamParallelDoesNotRetrieveMorePagesThanExpected() {
        /*
         * The test doesn't make any service calls so use a high page count to give the test more opportunities for
         * failure.
         */
        int pageCount = 10000;
        OnlyOnePageRetriever pageRetriever = new OnlyOnePageRetriever(pageCount);
        OnlyOnePagedIterable pagedIterable = new OnlyOnePagedIterable(new OnlyOnePagedFlux(() -> pageRetriever));

        // Validation that there is more than one paged in the full return.
        pagedIterable.stream().parallel().count();
        assertEquals(pageCount, pageRetriever.getGetCount());
    }

    @Test
    public void iterateNextOnlyRetrievesOnePage() {
        OnlyOnePageRetriever pageRetriever = new OnlyOnePageRetriever(DEFAULT_PAGE_COUNT);
        OnlyOnePagedIterable pagedIterable = new OnlyOnePagedIterable(new OnlyOnePagedFlux(() -> pageRetriever));

        // Validation that there is more than one paged in the full return.
        pagedIterable.iterator().forEachRemaining(ignored -> {
        });
        assertEquals(DEFAULT_PAGE_COUNT, pageRetriever.getGetCount());

        Integer next = pagedIterable.iterator().next();

        sleep();

        /*
         * Given that each page contains more than one element we are able to only retrieve a single page.
         */
        assertEquals(1, pageRetriever.getGetCount() - DEFAULT_PAGE_COUNT);
    }

    @Test
    public void streamByPageFindFirstOnlyRetrievesOnePage() {
        OnlyOnePageRetriever pageRetriever = new OnlyOnePageRetriever(DEFAULT_PAGE_COUNT);
        OnlyOnePagedIterable pagedIterable = new OnlyOnePagedIterable(new OnlyOnePagedFlux(() -> pageRetriever));

        // Validation that there is more than one paged in the full return.
        pagedIterable.streamByPage().count();
        assertEquals(DEFAULT_PAGE_COUNT, pageRetriever.getGetCount());

        OnlyOneContinuablePage page = pagedIterable.streamByPage().findFirst().get();

        sleep();

        /*
         * Given that Reactor maintains an internal buffer, and when it empties it will attempt to fill the buffer,
         * the best result we can get is only two pages being retrieved. One to satisfy the findFirst operations and
         * one to refill the buffer.
         */
        assertEquals(1, pageRetriever.getGetCount() - DEFAULT_PAGE_COUNT);
    }

    @Test
    public void streamParallelByPageDoesNotRetrieveMorePagesThanExpected() {
        /*
         * The test doesn't make any service calls so use a high page count to give the test more opportunities for
         * failure.
         */
        int pageCount = 10000;
        OnlyOnePageRetriever pageRetriever = new OnlyOnePageRetriever(pageCount);
        OnlyOnePagedIterable pagedIterable = new OnlyOnePagedIterable(new OnlyOnePagedFlux(() -> pageRetriever));

        // Validation that there is more than one paged in the full return.
        pagedIterable.streamByPage().parallel().count();
        assertEquals(pageCount, pageRetriever.getGetCount());
    }

    @Test
    public void iterateByPageNextOnlyRetrievesOnePage() {
        OnlyOnePageRetriever pageRetriever = new OnlyOnePageRetriever(DEFAULT_PAGE_COUNT);
        OnlyOnePagedIterable pagedIterable = new OnlyOnePagedIterable(new OnlyOnePagedFlux(() -> pageRetriever));

        // Validation that there is more than one paged in the full return.
        pagedIterable.iterableByPage().iterator().forEachRemaining(ignored -> {
        });
        assertEquals(DEFAULT_PAGE_COUNT, pageRetriever.getGetCount());

        OnlyOneContinuablePage page = pagedIterable.iterableByPage().iterator().next();

        sleep();

        /*
         * Given that Reactor maintains an internal buffer, and when it empties it will attempt to fill the buffer,
         * the best result we can get is only two pages being retrieved. One to satisfy the findFirst operations and
         * one to refill the buffer.
         */
        assertEquals(1, pageRetriever.getGetCount() - DEFAULT_PAGE_COUNT);
    }

    private static void sleep() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }
    }
}
