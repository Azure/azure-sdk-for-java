// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.rest;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.util.IterableStream;
import com.azure.core.util.paging.ContinuablePage;
import com.azure.core.util.paging.ContinuablePagedFlux;
import com.azure.core.util.paging.ContinuablePagedFluxCore;
import com.azure.core.util.paging.ContinuablePagedIterable;
import com.azure.core.util.paging.PageRetriever;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link PagedIterable}.
 */
public class PagedIterableTest {
    private List<PagedResponse<Integer>> pagedResponses;
    private List<PagedResponse<String>> pagedStringResponses;

    private HttpHeaders httpHeaders = new HttpHeaders().put("header1", "value1").put("header2", "value2");
    private HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, "http://localhost");
    private String deserializedHeaders = "header1,value1,header2,value2";

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
        assertEquals(Stream.iterate(0, i -> i + 1).limit(numberOfPages * 3).collect(Collectors.toList()), values);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 5})
    public void iterateByT(int numberOfPages) {
        PagedFlux<Integer> pagedFlux = getIntegerPagedFlux(numberOfPages);
        PagedIterable<Integer> pagedIterable = new PagedIterable<>(pagedFlux);
        List<Integer> values = new ArrayList<>();
        pagedIterable.iterator().forEachRemaining(values::add);

        assertEquals(numberOfPages * 3, values.size());
        assertEquals(Stream.iterate(0, i -> i + 1).limit(numberOfPages * 3).collect(Collectors.toList()), values);
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
        assertEquals(Stream.iterate(0, i -> i + 1).limit(numberOfPages * 3).map(String::valueOf)
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
        assertEquals(Stream.iterate(0, i -> i + 1).limit(numberOfPages * 3).map(String::valueOf)
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

        return Mono.just(pagedResponses.get(Integer.parseInt(continuationToken)));
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
    public void streamFindFirstOnlyRetrievesOnePage() throws InterruptedException {
        OnlyOnePageRetriever pageRetriever = new OnlyOnePageRetriever();
        Integer next = new OnlyOnePagedIterable(new OnlyOnePagedFlux(() -> pageRetriever)).stream().findFirst().get();

        Thread.sleep(2000);

        assertEquals(1, pageRetriever.getGetCount());
    }

    @Test
    public void iterateNextOnlyRetrievesOnePage() throws InterruptedException {
        OnlyOnePageRetriever pageRetriever = new OnlyOnePageRetriever();
        Integer next = new OnlyOnePagedIterable(new OnlyOnePagedFlux(() -> pageRetriever)).iterator().next();

        Thread.sleep(2000);

        assertEquals(1, pageRetriever.getGetCount());
    }

    @Test
    public void streamByPageFindFirstOnlyRetrievesOnePage() throws InterruptedException {
        OnlyOnePageRetriever pageRetriever = new OnlyOnePageRetriever();
        OnlyOneContinuablePage page = new OnlyOnePagedIterable(new OnlyOnePagedFlux(() -> pageRetriever)).streamByPage()
            .findFirst()
            .get();

        Thread.sleep(2000);

        assertEquals(1, pageRetriever.getGetCount());
    }

    @Test
    public void iterateByPageNextOnlyRetrievesOnePage() throws InterruptedException {
        OnlyOnePageRetriever pageRetriever = new OnlyOnePageRetriever();
        OnlyOneContinuablePage page = new OnlyOnePagedIterable(new OnlyOnePagedFlux(() -> pageRetriever))
            .iterableByPage()
            .iterator()
            .next();

        Thread.sleep(2000);

        assertEquals(1, pageRetriever.getGetCount());
    }

    private static final class OnlyOnePagedFlux
        extends ContinuablePagedFluxCore<Integer, Integer, OnlyOneContinuablePage> {
        protected OnlyOnePagedFlux(Supplier<PageRetriever<Integer, OnlyOneContinuablePage>> pageRetrieverProvider) {
            super(pageRetrieverProvider);
        }
    }

    private static final class OnlyOnePagedIterable
        extends ContinuablePagedIterable<Integer, Integer, OnlyOneContinuablePage> {
        public OnlyOnePagedIterable(ContinuablePagedFlux<Integer, Integer, OnlyOneContinuablePage> pagedFlux) {
            super(pagedFlux);
        }
    }

    private static final class OnlyOnePageRetriever implements PageRetriever<Integer, OnlyOneContinuablePage> {
        private final AtomicInteger getCount = new AtomicInteger();

        @Override
        public Flux<OnlyOneContinuablePage> get(Integer continuationToken, Integer pageSize) {
            int value = getCount.getAndIncrement();
            if (value == 3) {
                pageSize = (pageSize == null) ? 10 : pageSize;
                return Flux.just(new OnlyOneContinuablePage(continuationToken, null, pageSize));
            } else {
                continuationToken = (continuationToken == null) ? 0 : continuationToken;
                pageSize = (pageSize == null) ? 10 : pageSize;
                return Flux.just(new OnlyOneContinuablePage(continuationToken, continuationToken + 1, pageSize));
            }
        }

        public int getGetCount() {
            return getCount.get();
        }
    }

    private static final class OnlyOneContinuablePage implements ContinuablePage<Integer, Integer> {
        private final IterableStream<Integer> elements;
        private final Integer nextContinuationToken;

        private OnlyOneContinuablePage(Integer continuationToken, Integer nextContinuationToken, Integer pageSize) {
            elements = IterableStream.of(IntStream.range(continuationToken * 10, (continuationToken * 10) + pageSize)
                .boxed()
                .collect(Collectors.toList()));

            this.nextContinuationToken = nextContinuationToken;
        }

        @Override
        public IterableStream<Integer> getElements() {
            return elements;
        }

        @Override
        public Integer getContinuationToken() {
            return nextContinuationToken;
        }
    }
}
