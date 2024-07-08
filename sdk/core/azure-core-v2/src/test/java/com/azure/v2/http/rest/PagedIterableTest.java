// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.rest;

import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import com.azure.core.v2.util.paging.ContinuablePage;
import com.azure.core.v2.util.paging.ContinuablePagedFlux;
import com.azure.core.v2.util.paging.ContinuablePagedIterable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Flux;

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

    private final HttpHeaders httpHeaders = new HttpHeaders().set(HttpHeaderName.fromString("header1"), "value1")
        .set(HttpHeaderName.fromString("header2"), "value2");
    private final HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, "http://localhost");
    private final String deserializedHeaders = "header1,value1,header2,value2";

    private List<PagedResponse<Integer>> pagedResponses;
    private List<PagedResponse<String>> pagedStringResponses;

    @ParameterizedTest
    @ValueSource(ints = { 0, 5 })
    public void streamByPage(int numberOfPages) {
        PagedFlux<Integer> pagedFlux = getIntegerPagedFlux(numberOfPages);
        PagedIterable<Integer> pagedIterable = new PagedIterable<>(pagedFlux);
        List<PagedResponse<Integer>> pages = pagedIterable.streamByPage().collect(Collectors.toList());

        assertEquals(numberOfPages, pages.size());
        assertEquals(pagedResponses, pages);
    }

    @ParameterizedTest
    @ValueSource(ints = { 5 })
    public void streamByPagePagedIterable(int numberOfPages) {
        PagedIterable<Integer> pagedIterable = getIntegerPagedIterable(numberOfPages);
        List<PagedResponse<Integer>> pages = pagedIterable.streamByPage().collect(Collectors.toList());

        assertEquals(numberOfPages, pages.size());
        assertEquals(pagedResponses, pages);
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 5 })
    public void iterateByPage(int numberOfPages) {
        PagedFlux<Integer> pagedFlux = getIntegerPagedFlux(numberOfPages);
        PagedIterable<Integer> pagedIterable = new PagedIterable<>(pagedFlux);
        List<PagedResponse<Integer>> pages = new ArrayList<>();
        pagedIterable.iterableByPage().iterator().forEachRemaining(pages::add);

        assertEquals(numberOfPages, pages.size());
        assertEquals(pagedResponses, pages);
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 5 })
    public void iterateByPagePagedIterable(int numberOfPages) {
        PagedIterable<Integer> pagedIterable = getIntegerPagedIterable(numberOfPages);
        List<PagedResponse<Integer>> pages = new ArrayList<>();
        pagedIterable.iterableByPage().iterator().forEachRemaining(pages::add);

        assertEquals(numberOfPages, pages.size());
        assertEquals(pagedResponses, pages);
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 5 })
    public void streamByT(int numberOfPages) {
        PagedFlux<Integer> pagedFlux = getIntegerPagedFlux(numberOfPages);
        PagedIterable<Integer> pagedIterable = new PagedIterable<>(pagedFlux);
        List<Integer> values = pagedIterable.stream().collect(Collectors.toList());

        assertEquals(numberOfPages * 3, values.size());
        assertEquals(Stream.iterate(0, i -> i + 1).limit(numberOfPages * 3L).collect(Collectors.toList()), values);
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 5 })
    public void streamByTPagedIterable(int numberOfPages) {
        PagedIterable<Integer> pagedIterable = getIntegerPagedIterable(numberOfPages);
        List<Integer> values = pagedIterable.stream().collect(Collectors.toList());

        assertEquals(numberOfPages * 3, values.size());
        assertEquals(Stream.iterate(0, i -> i + 1).limit(numberOfPages * 3L).collect(Collectors.toList()), values);
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 5 })
    public void iterateByT(int numberOfPages) {
        PagedFlux<Integer> pagedFlux = getIntegerPagedFlux(numberOfPages);
        PagedIterable<Integer> pagedIterable = new PagedIterable<>(pagedFlux);
        List<Integer> values = new ArrayList<>();
        pagedIterable.iterator().forEachRemaining(values::add);

        assertEquals(numberOfPages * 3, values.size());
        assertEquals(Stream.iterate(0, i -> i + 1).limit(numberOfPages * 3L).collect(Collectors.toList()), values);
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 5 })
    public void iterateByTPagedIterable(int numberOfPages) {
        PagedIterable<Integer> pagedIterable = getIntegerPagedIterable(numberOfPages);
        List<Integer> values = new ArrayList<>();
        pagedIterable.iterator().forEachRemaining(values::add);

        assertEquals(numberOfPages * 3, values.size());
        assertEquals(Stream.iterate(0, i -> i + 1).limit(numberOfPages * 3L).collect(Collectors.toList()), values);
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 5 })
    public void streamByPageMap(int numberOfPages) {
        PagedFlux<Integer> pagedFlux = getIntegerPagedFlux(numberOfPages);
        PagedIterable<Integer> pagedIterable = new PagedIterable<>(pagedFlux);
        List<PagedResponse<String>> pages
            = pagedIterable.mapPage(String::valueOf).streamByPage().collect(Collectors.toList());

        assertEquals(numberOfPages, pages.size());
        for (int i = 0; i < numberOfPages; i++) {
            assertEquals(pagedStringResponses.get(i).getValue(), pages.get(i).getValue());
        }
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 1, 2, 3, 4, 5 })
    public void streamByPageMapSync(int numberOfPages) {
        PagedIterable<Integer> integerPagedIterable = getIntegerPagedIterable(numberOfPages);
        List<PagedResponse<String>> pages
            = integerPagedIterable.mapPage(String::valueOf).streamByPage().collect(Collectors.toList());

        assertEquals(numberOfPages, pages.size());
        for (int i = 0; i < numberOfPages; i++) {
            assertEquals(pagedStringResponses.get(i).getValue(), pages.get(i).getValue());
        }
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 5 })
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
    @ValueSource(ints = { 0, 1, 2, 3, 4, 5 })
    public void iterateByPageMapSync(int numberOfPages) {
        PagedIterable<Integer> integerPagedIterable = getIntegerPagedIterable(numberOfPages);

        List<PagedResponse<String>> pages = new ArrayList<>();
        integerPagedIterable.mapPage(String::valueOf).iterableByPage().iterator().forEachRemaining(pages::add);

        assertEquals(numberOfPages, pages.size());
        for (int i = 0; i < numberOfPages; i++) {
            assertEquals(pagedStringResponses.get(i).getValue(), pages.get(i).getValue());
        }
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 5 })
    public void streamByTMap(int numberOfPages) {
        PagedFlux<Integer> pagedFlux = getIntegerPagedFlux(numberOfPages);
        PagedIterable<Integer> pagedIterable = new PagedIterable<>(pagedFlux);
        List<String> values = pagedIterable.mapPage(String::valueOf).stream().collect(Collectors.toList());

        assertEquals(numberOfPages * 3, values.size());
        assertEquals(
            Stream.iterate(0, i -> i + 1).limit(numberOfPages * 3L).map(String::valueOf).collect(Collectors.toList()),
            values);
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 1, 2, 3, 4, 5 })
    public void streamByTMapSync(int numberOfPages) {
        PagedIterable<Integer> pagedIterable = getIntegerPagedIterable(numberOfPages);
        List<String> values = pagedIterable.mapPage(String::valueOf).stream().collect(Collectors.toList());

        assertEquals(numberOfPages * 3, values.size());
        assertEquals(
            Stream.iterate(0, i -> i + 1).limit(numberOfPages * 3L).map(String::valueOf).collect(Collectors.toList()),
            values);
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 5 })
    public void iterateByTMap(int numberOfPages) {
        PagedFlux<Integer> pagedFlux = getIntegerPagedFlux(numberOfPages);
        PagedIterable<Integer> pagedIterable = new PagedIterable<>(pagedFlux);
        List<String> values = new ArrayList<>();
        pagedIterable.mapPage(String::valueOf).iterator().forEachRemaining(values::add);

        assertEquals(numberOfPages * 3, values.size());
        assertEquals(
            Stream.iterate(0, i -> i + 1).limit(numberOfPages * 3L).map(String::valueOf).collect(Collectors.toList()),
            values);
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 5 })
    public void iterateByTMapSync(int numberOfPages) {
        PagedIterable<Integer> pagedIterable = getIntegerPagedIterable(numberOfPages);
        List<String> values = new ArrayList<>();
        pagedIterable.mapPage(String::valueOf).iterator().forEachRemaining(values::add);

        assertEquals(numberOfPages * 3, values.size());
        assertEquals(
            Stream.iterate(0, i -> i + 1).limit(numberOfPages * 3L).map(String::valueOf).collect(Collectors.toList()),
            values);
    }

    @Test
    public void streamFirstPage() {
        TestPagedFlux<Integer> pagedFlux = getTestPagedFlux();
        PagedIterable<Integer> pagedIterable = new PagedIterable<>(pagedFlux);

        assertEquals(pagedResponses.get(0), pagedIterable.streamByPage().limit(1).collect(Collectors.toList()).get(0));

        // The goal for this test is that 0 next page retrieval calls are made.
        assertEquals(0, pagedFlux.getNextPageRetrievals());
    }

    @Test
    public void streamFirstPagePagedIterable() {
        TestPagedIterable<Integer> pagedIterable = getTestPagedIterable();

        assertEquals(pagedResponses.get(0), pagedIterable.streamByPage().limit(1).collect(Collectors.toList()).get(0));

        // The goal for this test is that 0 next page retrieval calls are made.
        assertEquals(0, pagedIterable.getNextPageRetrievals());
    }

    @Test
    public void iterateFirstPage() {
        TestPagedFlux<Integer> pagedFlux = getTestPagedFlux();
        PagedIterable<Integer> pagedIterable = new PagedIterable<>(pagedFlux);

        assertEquals(pagedResponses.get(0), pagedIterable.iterableByPage().iterator().next());

        // The goal for this test is that 0 next page retrieval calls are made.
        assertEquals(0, pagedFlux.getNextPageRetrievals());
    }

    @Test
    public void iterateFirstPageIterable() {
        TestPagedIterable<Integer> pagedIterable = getTestPagedIterable();

        assertEquals(pagedResponses.get(0), pagedIterable.iterableByPage().iterator().next());

        // The goal for this test is that 0 next page retrieval calls are made.
        assertEquals(0, pagedIterable.getNextPageRetrievals());
    }

    @Test
    public void streamFirstValue() {
        TestPagedFlux<Integer> pagedFlux = getTestPagedFlux();
        PagedIterable<Integer> pagedIterable = new PagedIterable<>(pagedFlux);

        Integer firstValue = pagedResponses.get(0).getValue().get(0);
        assertEquals(firstValue, pagedIterable.stream().limit(1).collect(Collectors.toList()).get(0));
    }

    @Test
    public void streamFirstValuePagedIterable() {
        TestPagedIterable<Integer> pagedIterable = getTestPagedIterable();

        Integer firstValue = pagedResponses.get(0).getValue().get(0);
        assertEquals(firstValue, pagedIterable.stream().limit(1).collect(Collectors.toList()).get(0));
    }

    @Test
    public void iterateFirstValue() {
        TestPagedFlux<Integer> pagedFlux = getTestPagedFlux();
        PagedIterable<Integer> pagedIterable = new PagedIterable<>(pagedFlux);

        Integer firstValue = pagedResponses.get(0).getValue().get(0);
        assertEquals(firstValue, pagedIterable.iterator().next());
        assertEquals(0, pagedFlux.getNextPageRetrievals());
    }

    @Test
    public void iterateFirstValuePagedIterable() {
        TestPagedIterable<Integer> pagedIterable = getTestPagedIterable();

        Integer firstValue = pagedResponses.get(0).getValue().get(0);
        assertEquals(firstValue, pagedIterable.iterator().next());
        assertEquals(0, pagedIterable.getNextPageRetrievals());
    }

    @Test
    public void pagedIterableWithPageSize() {
        final int expectedPageSize = 5;

        HttpHeaders headers = new HttpHeaders();
        HttpRequest request = new HttpRequest(HttpMethod.GET, "http://localhost");
        final Function<String, PagedResponse<Integer>> pagedResponseSupplier
            = continuationToken -> new PagedResponseBase<>(request, 200, headers, Collections.emptyList(),
                continuationToken, null);

        PagedFlux<Integer> singlePageFlux = new PagedFlux<>(pageSize -> {
            assertEquals(expectedPageSize, pageSize);
            return pagedResponseSupplier.apply(null));
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
            return pagedResponseSupplier.apply(expectedContinuationToken));
        }, (continuationToken, pageSize) -> {
            assertEquals(expectedPageSize, pageSize);
            assertEquals(expectedContinuationToken, continuationToken);
            return pagedResponseSupplier.apply(null));
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

    @Test
    public void pageRetrieverPagedIterableWithPageSize() {
        final int expectedPageSize = 5;

        HttpHeaders headers = new HttpHeaders();
        HttpRequest request = new HttpRequest(HttpMethod.GET, "http://localhost");
        final Function<String, PagedResponse<Integer>> pagedResponseSupplier
            = continuationToken -> new PagedResponseBase<>(request, 200, headers, Collections.emptyList(),
                continuationToken, null);

        PagedIterable<Integer> singlePageIterable = new PagedIterable<>(pageSize -> {
            assertEquals(expectedPageSize, pageSize);
            return pagedResponseSupplier.apply(null);
        });

        Iterator<PagedResponse<Integer>> pageIterator = singlePageIterable.iterableByPage(expectedPageSize).iterator();
        assertTrue(pageIterator.hasNext());
        pageIterator.next();
        assertFalse(pageIterator.hasNext());

        assertEquals(1L, singlePageIterable.streamByPage(expectedPageSize).count());

        final String expectedContinuationToken = "0";
        PagedIterable<Integer> multiPageIterable = new PagedIterable<>(pageSize -> {
            assertEquals(expectedPageSize, pageSize);
            return pagedResponseSupplier.apply(expectedContinuationToken);
        }, (continuationToken, pageSize) -> {
            assertEquals(expectedPageSize, pageSize);
            assertEquals(expectedContinuationToken, continuationToken);
            return pagedResponseSupplier.apply(null);
        });

        pageIterator = multiPageIterable.iterableByPage(expectedPageSize).iterator();
        assertTrue(pageIterator.hasNext());
        pageIterator.next();
        assertTrue(pageIterator.hasNext());
        pageIterator.next();
        assertFalse(pageIterator.hasNext());

        assertEquals(2L, multiPageIterable.streamByPage(expectedPageSize).count());
    }

    private PagedFlux<Integer> getIntegerPagedFlux(int numberOfPages) {
        createPagedResponse(numberOfPages);

        return new PagedFlux<>(() -> pagedResponses.isEmpty() ? Mono.empty() : pagedResponses.get(0)),
            continuationToken -> getNextPage(continuationToken, pagedResponses));
    }

    private PagedIterable<Integer> getIntegerPagedIterable(int numberOfPages) {
        createPagedResponse(numberOfPages);

        return new PagedIterable<>(() -> pagedResponses.isEmpty() ? null : pagedResponses.get(0),
            continuationToken -> getNextPageSync(continuationToken, pagedResponses));
    }

    private TestPagedFlux<Integer> getTestPagedFlux() {
        createPagedResponse(5);

        return new TestPagedFlux<>(() -> pagedResponses.isEmpty() ? Mono.empty() : pagedResponses.get(0)),
            continuationToken -> getNextPage(continuationToken, pagedResponses));
    }

    private TestPagedIterable<Integer> getTestPagedIterable() {
        createPagedResponse(5);

        return new TestPagedIterable<>(() -> pagedResponses.isEmpty() ? null : pagedResponses.get(0),
            continuationToken -> getNextPageSync(continuationToken, pagedResponses));
    }

    private void createPagedResponse(int numberOfPages) {
        pagedResponses = IntStream.range(0, numberOfPages)
            .boxed()
            .map(i -> createPagedResponse(httpRequest, httpHeaders, deserializedHeaders, numberOfPages, this::getItems,
                i))
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
            (i < numberOfPages - 1) ? String.valueOf(i + 1) : null, deserializedHeaders);
    }

    private PagedResponse<Integer>> getNextPage(String continuationToken,
        List<PagedResponse<Integer>> pagedResponses) {

        if (continuationToken == null || continuationToken.isEmpty()) {
            return null;
        }

        int parsedToken = Integer.parseInt(continuationToken);
        if (parsedToken >= pagedResponses.size()) {
            return null;
        }

        return pagedResponses.get(parsedToken));
    }

    private PagedResponse<Integer> getNextPageSync(String continuationToken,
        List<PagedResponse<Integer>> pagedResponses) {

        if (continuationToken == null || continuationToken.isEmpty()) {
            return null;
        }

        int parsedToken = Integer.parseInt(continuationToken);
        if (parsedToken >= pagedResponses.size()) {
            return null;
        }

        return pagedResponses.get(parsedToken);
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

        TestPagedFlux(Supplier<PagedResponse<T>>> firstPageRetriever,
            Function<String, PagedResponse<T>>> nextPageRetriever) {
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

    /*
     * Test class used to verify that paged iterable will lazily request next pages.
     */
    private static class TestPagedIterable<T> extends PagedIterable<T> {
        private int nextPageRetrievals = 0;

        TestPagedIterable(Supplier<PagedResponse<T>> firstPageRetriever,
            Function<String, PagedResponse<T>> nextPageRetriever) {
            super(firstPageRetriever, nextPageRetriever);
        }

        @Override
        public Stream<PagedResponse<T>> streamByPage(String continuationToken) {
            nextPageRetrievals++;
            return super.streamByPage(continuationToken);
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
    public void streamFindFirstOnlyRetrievesOnePageSync() {
        OnlyOnePageRetrieverSync pageRetrieverSync = new OnlyOnePageRetrieverSync(DEFAULT_PAGE_COUNT);
        OnlyOnePagedIterable pagedIterable = new OnlyOnePagedIterable(() -> pageRetrieverSync, null, null);

        // Validation that there is more than one paged in the full return.
        pagedIterable.stream().count();
        assertEquals(DEFAULT_PAGE_COUNT, pageRetrieverSync.getGetCount());

        Integer next = pagedIterable.stream().findFirst().orElse(0);

        /*
         * Given that each page contains more than one element we are able to only retrieve a single page.
         */
        assertEquals(1, pageRetrieverSync.getGetCount() - DEFAULT_PAGE_COUNT);
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
    public void streamParallelDoesNotRetrieveMorePagesThanExpectedSync() {
        /*
         * The test doesn't make any service calls so use a high page count to give the test more opportunities for
         * failure.
         */
        int pageCount = 10000;
        OnlyOnePageRetrieverSync pageRetriever = new OnlyOnePageRetrieverSync(pageCount);
        OnlyOnePagedIterable pagedIterable = new OnlyOnePagedIterable(() -> pageRetriever, null, null);

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
    public void iterateNextOnlyRetrievesOnePageSync() {
        OnlyOnePageRetrieverSync pageRetriever = new OnlyOnePageRetrieverSync(DEFAULT_PAGE_COUNT);
        OnlyOnePagedIterable pagedIterable = new OnlyOnePagedIterable(() -> pageRetriever, null, null);

        // Validation that there is more than one paged in the full return.
        pagedIterable.iterator().forEachRemaining(ignored -> {
        });
        assertEquals(DEFAULT_PAGE_COUNT, pageRetriever.getGetCount());

        Integer next = pagedIterable.iterator().next();

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
    public void streamByPageFindFirstOnlyRetrievesOnePageSync() {
        OnlyOnePageRetrieverSync pageRetriever = new OnlyOnePageRetrieverSync(DEFAULT_PAGE_COUNT);
        OnlyOnePagedIterable pagedIterable = new OnlyOnePagedIterable(() -> pageRetriever, null, null);

        // Validation that there is more than one paged in the full return.
        pagedIterable.streamByPage().count();
        assertEquals(DEFAULT_PAGE_COUNT, pageRetriever.getGetCount());

        OnlyOneContinuablePage page = pagedIterable.streamByPage().findFirst().get();

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
    public void streamParallelByPageDoesNotRetrieveMorePagesThanExpectedSync() {
        int pageCount = 10000;
        OnlyOnePageRetrieverSync pageRetriever = new OnlyOnePageRetrieverSync(pageCount);
        OnlyOnePagedIterable pagedIterable = new OnlyOnePagedIterable(() -> pageRetriever, null, null);

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

    @Test
    public void iterateByPageNextOnlyRetrievesOnePageSync() {
        OnlyOnePageRetrieverSync pageRetriever = new OnlyOnePageRetrieverSync(DEFAULT_PAGE_COUNT);
        OnlyOnePagedIterable pagedIterable = new OnlyOnePagedIterable(() -> pageRetriever, null, null);

        // Validation that there is more than one paged in the full return.
        pagedIterable.iterableByPage().iterator().forEachRemaining(ignored -> {
        });
        assertEquals(DEFAULT_PAGE_COUNT, pageRetriever.getGetCount());

        OnlyOneContinuablePage page = pagedIterable.iterableByPage().iterator().next();

        assertEquals(1, pageRetriever.getGetCount() - DEFAULT_PAGE_COUNT);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.http.rest.PagedFluxTest#pagingTerminatesOnSupplier")
    public <C, T, P extends ContinuablePage<C, T>> void streamingTerminatesOn(ContinuablePagedFlux<C, T, P> pagedFlux,
        List<T> expectedItems) {
        List<T> actualItems = new ContinuablePagedIterable<>(pagedFlux).stream().collect(Collectors.toList());
        assertEquals(expectedItems.size(), actualItems.size());
        for (int i = 0; i < expectedItems.size(); i++) {
            assertEquals(expectedItems.get(i), actualItems.get(i));
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.http.rest.PagedFluxTest#pagingTerminatesOnSupplier")
    public <C, T, P extends ContinuablePage<C, T>> void iteratingTerminatesOn(ContinuablePagedFlux<C, T, P> pagedFlux,
        List<T> expectedItems) {
        List<T> actualItems = new ArrayList<>();
        new ContinuablePagedIterable<>(pagedFlux).iterator().forEachRemaining(actualItems::add);
        assertEquals(expectedItems.size(), actualItems.size());
        for (int i = 0; i < expectedItems.size(); i++) {
            assertEquals(expectedItems.get(i), actualItems.get(i));
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.http.rest.PagedFluxTest#pagingTerminatesOnSupplier")
    public <C, T, P extends ContinuablePage<C, T>> void
        streamingByPageTerminatesOn(ContinuablePagedFlux<C, T, P> pagedFlux, List<T> expectedItems) {
        List<T> actualItems = new ArrayList<>();
        new ContinuablePagedIterable<>(pagedFlux).streamByPage()
            .map(page -> page.getElements())
            .forEach(iterableStream -> iterableStream.forEach(actualItems::add));
        assertEquals(expectedItems.size(), actualItems.size());
        for (int i = 0; i < expectedItems.size(); i++) {
            assertEquals(expectedItems.get(i), actualItems.get(i));
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.http.rest.PagedFluxTest#pagingTerminatesOnSupplier")
    public <C, T, P extends ContinuablePage<C, T>> void
        iteratingByPageTerminatesOn(ContinuablePagedFlux<C, T, P> pagedFlux, List<T> expectedItems) {
        List<T> actualItems = new ArrayList<>();
        new ContinuablePagedIterable<>(pagedFlux).iterableByPage()
            .iterator()
            .forEachRemaining(page -> page.getElements().forEach(actualItems::add));
        assertEquals(expectedItems.size(), actualItems.size());
        for (int i = 0; i < expectedItems.size(); i++) {
            assertEquals(expectedItems.get(i), actualItems.get(i));
        }
    }

    private static void sleep() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }
    }
}
