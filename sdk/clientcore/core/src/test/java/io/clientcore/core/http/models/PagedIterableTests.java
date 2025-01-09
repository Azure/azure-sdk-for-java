// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http.models;

import io.clientcore.core.util.binarydata.BinaryData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.opentest4j.AssertionFailedError;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PagedIterableTests {

    private final HttpHeaders httpHeaders = new HttpHeaders();
    private final HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, "http://localhost");
    private final BinaryData responseBody = BinaryData.empty();

    // tests with mocked PagedResponse
    private List<PagedResponse<Integer>> pagedResponses;

    @ParameterizedTest
    @ValueSource(ints = { 0, 5 })
    public void streamByPage(int numberOfPages) {
        PagedIterable<Integer> pagedIterable = getIntegerPagedIterable(numberOfPages);
        List<PagedResponse<Integer>> pages = pagedIterable.streamByPage().collect(Collectors.toList());

        assertEquals(numberOfPages, pages.size());
        assertEquals(pagedResponses, pages);
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 5 })
    public void iterateByPage(int numberOfPages) {
        PagedIterable<Integer> pagedIterable = getIntegerPagedIterable(numberOfPages);
        List<PagedResponse<Integer>> pages = new ArrayList<>();
        pagedIterable.iterableByPage().iterator().forEachRemaining(pages::add);

        assertEquals(numberOfPages, pages.size());
        assertEquals(pagedResponses, pages);
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 5 })
    public void streamByT(int numberOfPages) {
        PagedIterable<Integer> pagedIterable = getIntegerPagedIterable(numberOfPages);
        List<Integer> values = pagedIterable.stream().collect(Collectors.toList());

        assertEquals(numberOfPages * 3, values.size());
        assertEquals(Stream.iterate(0, i -> i + 1).limit(numberOfPages * 3L).collect(Collectors.toList()), values);
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 5 })
    public void iterateByT(int numberOfPages) {
        PagedIterable<Integer> pagedIterable = getIntegerPagedIterable(numberOfPages);
        List<Integer> values = new ArrayList<>();
        pagedIterable.iterator().forEachRemaining(values::add);

        assertEquals(numberOfPages * 3, values.size());
        assertEquals(Stream.iterate(0, i -> i + 1).limit(numberOfPages * 3L).collect(Collectors.toList()), values);
    }

    private PagedIterable<Integer> getIntegerPagedIterable(int numberOfPages) {
        createPagedResponse(numberOfPages);

        return new PagedIterable<>(pagingOptions -> pagedResponses.isEmpty() ? null : pagedResponses.get(0),
            (pagingOptions, nextLink) -> getNextPageSync(nextLink, pagedResponses));
    }

    private void createPagedResponse(int numberOfPages) {
        pagedResponses = IntStream.range(0, numberOfPages)
            .boxed()
            .map(i -> createPagedResponse(httpRequest, httpHeaders, numberOfPages, this::getItems, i))
            .collect(Collectors.toList());
    }

    private <T> PagedResponse<T> createPagedResponse(HttpRequest httpRequest, HttpHeaders headers, int numberOfPages,
        Function<Integer, List<T>> valueSupplier, int i) {
        return new PagedResponse<>(httpRequest, 200, headers, responseBody, valueSupplier.apply(i), null,
            (i < numberOfPages - 1) ? String.valueOf(i + 1) : null, null, null, null);
    }

    private PagedResponse<Integer> getNextPageSync(String nextLink, List<PagedResponse<Integer>> pagedResponses) {

        if (nextLink == null || nextLink.isEmpty()) {
            return null;
        }

        int parsedToken = Integer.parseInt(nextLink);
        if (parsedToken >= pagedResponses.size()) {
            return null;
        }

        return pagedResponses.get(parsedToken);
    }

    private List<Integer> getItems(int i) {
        return IntStream.range(i * 3, i * 3 + 3).boxed().collect(Collectors.toList());
    }

    // tests with mocked HttpResponse
    private NextPageMode nextPageMode;

    @ParameterizedTest
    @EnumSource(NextPageMode.class)
    public void testPagedIterable(NextPageMode nextPageMode) {
        this.nextPageMode = nextPageMode;

        PagedIterable<TodoItem> pagedIterable = this.list();
        verifyIteratorSize(pagedIterable.iterableByPage().iterator(), 2);
        verifyIteratorSize(pagedIterable.iterator(), 3);
    }

    @Test
    public void testPagedIterableContinuationToken() {
        this.nextPageMode = NextPageMode.CONTINUATION_TOKEN;

        PagingOptions pagingOptions = new PagingOptions().setContinuationToken("page1");

        PagedIterable<TodoItem> pagedIterable = this.list();
        verifyIteratorSize(pagedIterable.iterableByPage(pagingOptions).iterator(), 1);
        verifyIteratorSize(pagedIterable.streamByPage(pagingOptions).iterator(), 1);
    }

    private static <T> void verifyIteratorSize(Iterator<T> iterator, long size) {
        long iteratorSize = 0;
        while (iterator.hasNext()) {
            ++iteratorSize;
            iterator.next();
        }
        Assertions.assertEquals(size, iteratorSize);
    }

    // mock class and API for pageable operation
    public enum NextPageMode {
        CONTINUATION_TOKEN, NEXT_LINK
    }

    private static final class TodoItem {
    }

    private static final class TodoPage {
        private final List<TodoItem> items;
        private final String continuationToken;
        private final String nextLink;

        private TodoPage(List<TodoItem> items, String continuationToken, String nextLink) {
            this.items = items;
            this.continuationToken = continuationToken;
            this.nextLink = nextLink;
        }

        public List<TodoItem> getItems() {
            return items;
        }

        public String getContinuationToken() {
            return continuationToken;
        }

        public String getNextLink() {
            return nextLink;
        }
    }

    private PagedIterable<TodoItem> list() {
        return new PagedIterable<>((pagingOptions) -> listSinglePage(pagingOptions),
            (pagingOptions, nextLink) -> listNextSinglePage(pagingOptions, nextLink));
    }

    private PagedResponse<TodoItem> listSinglePage(PagingOptions pagingOptions) {
        Response<TodoPage> res = listSync(pagingOptions);
        return new PagedResponse<>(res.getRequest(), res.getStatusCode(), res.getHeaders(), res.getBody(),
            res.getValue().getItems(), res.getValue().getContinuationToken(), res.getValue().getNextLink(), null, null,
            null);
    }

    private PagedResponse<TodoItem> listNextSinglePage(PagingOptions pagingOptions, String nextLink) {
        Response<TodoPage> res = (nextLink == null) ? listSync(pagingOptions) : listNextSync(nextLink);
        return new PagedResponse<>(res.getRequest(), res.getStatusCode(), res.getHeaders(), res.getBody(),
            res.getValue().getItems(), res.getValue().getContinuationToken(), res.getValue().getNextLink(), null, null,
            null);
    }

    private Response<TodoPage> listSync(PagingOptions pagingOptions) {
        // mock request on first page
        if (nextPageMode == NextPageMode.NEXT_LINK) {
            return new HttpResponse<>(httpRequest, 200, httpHeaders,
                new TodoPage(List.of(new TodoItem(), new TodoItem()), null, "https://nextLink"));
        } else if (nextPageMode == NextPageMode.CONTINUATION_TOKEN) {
            if (pagingOptions.getContinuationToken() == null) {
                // first page
                return new HttpResponse<>(httpRequest, 200, httpHeaders,
                    new TodoPage(List.of(new TodoItem(), new TodoItem()), "page1", null));
            } else if ("page1".equals(pagingOptions.getContinuationToken())) {
                // second page
                return new HttpResponse<>(httpRequest, 200, httpHeaders,
                    new TodoPage(List.of(new TodoItem()), null, null));
            } else {
                throw new AssertionFailedError();
            }
        } else {
            throw new AssertionFailedError();
        }
    }

    private Response<TodoPage> listNextSync(String nextLink) {
        // mock request on next page
        Assertions.assertEquals("https://nextLink", nextLink);
        return new HttpResponse<>(httpRequest, 200, httpHeaders, new TodoPage(List.of(new TodoItem()), null, null));
    }
}
