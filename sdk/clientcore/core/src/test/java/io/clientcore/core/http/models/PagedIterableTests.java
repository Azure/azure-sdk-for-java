// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http.models;

import io.clientcore.core.http.paging.PagedIterable;
import io.clientcore.core.http.paging.PagedResponse;
import io.clientcore.core.http.paging.PagingOptions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.opentest4j.AssertionFailedError;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PagedIterableTests {

    private final HttpHeaders httpHeaders = new HttpHeaders();
    private final HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri("http://localhost");

    // tests with mocked PagedResponse
    private List<PagedResponse<Integer, String>> pagedResponses;

    @ParameterizedTest
    @ValueSource(ints = { 0, 5 })
    public void streamByPage(int numberOfPages) {
        PagedIterable<Integer, String> pagedIterable = getIntegerPagedIterable(numberOfPages);
        List<PagedResponse<Integer, String>> pages = pagedIterable.streamByPage().collect(Collectors.toList());

        assertEquals(numberOfPages, pages.size());
        assertEquals(pagedResponses, pages);
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 5 })
    public void iterateByPage(int numberOfPages) {
        PagedIterable<Integer, String> pagedIterable = getIntegerPagedIterable(numberOfPages);
        List<PagedResponse<Integer, String>> pages = new ArrayList<>();
        pagedIterable.iterableByPage().iterator().forEachRemaining(pages::add);

        assertEquals(numberOfPages, pages.size());
        assertEquals(pagedResponses, pages);
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 5 })
    public void streamByT(int numberOfPages) {
        PagedIterable<Integer, String> pagedIterable = getIntegerPagedIterable(numberOfPages);
        List<Integer> values = pagedIterable.stream().collect(Collectors.toList());

        assertEquals(numberOfPages * 3, values.size());
        assertEquals(Stream.iterate(0, i -> i + 1).limit(numberOfPages * 3L).collect(Collectors.toList()), values);
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 5 })
    public void iterateByT(int numberOfPages) {
        PagedIterable<Integer, String> pagedIterable = getIntegerPagedIterable(numberOfPages);
        List<Integer> values = new ArrayList<>();
        pagedIterable.iterator().forEachRemaining(values::add);

        assertEquals(numberOfPages * 3, values.size());
        assertEquals(Stream.iterate(0, i -> i + 1).limit(numberOfPages * 3L).collect(Collectors.toList()), values);
    }

    @Test
    public void iterateResponseContainsEmptyArray() {
        pagedResponses = new ArrayList<>(3);
        // second page is empty but has nextLink
        pagedResponses.add(
            new PagedResponse<>(httpRequest, 200, httpHeaders, Arrays.asList(0, 1, 2), null, "1", null, null, null));
        pagedResponses.add(
            new PagedResponse<>(httpRequest, 200, httpHeaders, Collections.emptyList(), null, "2", null, null, null));
        pagedResponses
            .add(new PagedResponse<>(httpRequest, 200, httpHeaders, Arrays.asList(3, 4), null, null, null, null, null));

        PagedIterable<Integer, String> pagedIterable
            = new PagedIterable<>(pagingOptions -> pagedResponses.isEmpty() ? null : pagedResponses.get(0),
                (pagingOptions, nextLink) -> getNextPageSync(nextLink, pagedResponses));

        verifyIteratorSize(pagedIterable.iterableByPage().iterator(), 3);
        verifyIteratorSize(pagedIterable.iterator(), 5);
    }

    private PagedIterable<Integer, String> getIntegerPagedIterable(int numberOfPages) {
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

    private <T> PagedResponse<T, String> createPagedResponse(HttpRequest httpRequest, HttpHeaders headers,
        int numberOfPages, Function<Integer, List<T>> valueSupplier, int i) {
        return new PagedResponse<>(httpRequest, 200, headers, valueSupplier.apply(i), null,
            (i < numberOfPages - 1) ? String.valueOf(i + 1) : null, null, null, null);
    }

    private PagedResponse<Integer, String> getNextPageSync(String nextLink,
        List<PagedResponse<Integer, String>> pagedResponses) {

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
    @ParameterizedTest
    @ValueSource(ints = { 0, 10000, 100000 })
    public void streamParallelDoesNotRetrieveMorePagesThanExpected(int numberOfPages) {
        /*
         * The test doesn't make any service calls so use a high page count to give the test more opportunities for
         * failure.
         */

        // there is still 1 request (1 page with empty array), when no items
        int expectedNumberOfRetrievals = numberOfPages == 0 ? 1 : numberOfPages;

        nextPageMode = NextPageMode.CONTINUATION_TOKEN;
        pagingStatistics.resetAll();
        pagingStatistics.totalPages = numberOfPages;

        PagedIterable<TodoItem, String> pagedIterable = list();

        long count = pagedIterable.stream().parallel().count();
        assertEquals((long) numberOfPages * pagingStatistics.pageSize, count);
        assertEquals(expectedNumberOfRetrievals, pagingStatistics.numberOfPageRetrievals);
    }

    @ParameterizedTest
    @EnumSource(NextPageMode.class)
    public void testPagedIterable(NextPageMode nextPageMode) {
        this.nextPageMode = nextPageMode;
        pagingStatistics.resetAll();

        PagedIterable<TodoItem, String> pagedIterable = this.list();

        verifyIteratorSize(pagedIterable.iterableByPage().iterator(), pagingStatistics.totalPages);
        verifyIteratorSize(pagedIterable.iterator(), (long) pagingStatistics.totalPages * pagingStatistics.pageSize);

        // case when pagingOptions == null
        verifyIteratorSize(pagedIterable.iterableByPage(null).iterator(), pagingStatistics.totalPages);
        verifyIteratorSize(pagedIterable.streamByPage(null).iterator(), pagingStatistics.totalPages);
    }

    @Test
    public void testPagedIterableContinuationToken() {
        nextPageMode = NextPageMode.CONTINUATION_TOKEN;
        pagingStatistics.resetAll();
        pagingStatistics.totalPages = 5;

        int startPage = 2;

        PagedIterable<TodoItem, String> pagedIterable = this.list();

        // continuationToken provided, start from startPage
        PagingOptions<String> pagingOptions
            = new PagingOptions<String>().setContinuationToken(String.valueOf(startPage));

        verifyIteratorSize(pagedIterable.iterableByPage(pagingOptions).iterator(),
            pagingStatistics.totalPages - startPage);
        verifyIteratorSize(pagedIterable.streamByPage(pagingOptions).iterator(),
            pagingStatistics.totalPages - startPage);
    }

    @Test
    public void supportsNonStringContinuationToken() {
        PagedIterable<Integer, ContinuationToken> pagedIterable = new PagedIterable<>(pagingOptions -> {
            ContinuationToken token = pagingOptions.getContinuationToken();
            int offset = token == null ? 0 : token.offset;
            ContinuationToken next = new ContinuationToken(offset + 2, offset >= 4);
            return new PagedResponse<>(httpRequest, 200, httpHeaders, Arrays.asList(offset, offset + 1), next, null,
                null, null, null);
        }, token -> token != null && !token.terminal);

        PagingOptions<ContinuationToken> options
            = new PagingOptions<ContinuationToken>().setContinuationToken(new ContinuationToken(2, false));
        List<Integer> values = pagedIterable.streamByPage(options)
            .flatMap(page -> page.getValue().stream())
            .collect(Collectors.toList());

        assertEquals(Arrays.asList(2, 3, 4, 5), values);
    }

    @Test
    public void tokenBasedConstructorStopsOnEmptyStringByDefault() {
        AtomicInteger retrievals = new AtomicInteger();
        PagedIterable<Integer, String> pagedIterable = new PagedIterable<>(pagingOptions -> {
            retrievals.incrementAndGet();
            return new PagedResponse<>(httpRequest, 200, httpHeaders, Collections.singletonList(1), "", null, null,
                null, null);
        });

        assertEquals(Collections.singletonList(1), pagedIterable.stream().collect(Collectors.toList()));
        assertEquals(1, retrievals.get());
    }

    @Test
    public void tokenBasedConstructorSupportsSinglePage() {
        AtomicInteger retrievals = new AtomicInteger();
        PagedIterable<Integer, String> pagedIterable = new PagedIterable<>(pagingOptions -> {
            retrievals.incrementAndGet();
            return new PagedResponse<>(httpRequest, 200, httpHeaders, Collections.singletonList(1));
        });

        assertEquals(Collections.singletonList(1), pagedIterable.stream().collect(Collectors.toList()));
        assertEquals(1, retrievals.get());
    }

    @Test
    public void nextLinkTakesPrecedenceOverContinuationPredicate() {
        AtomicInteger retrievals = new AtomicInteger();
        PagedIterable<Integer, String> pagedIterable = new PagedIterable<>(pagingOptions -> {
            retrievals.incrementAndGet();
            return new PagedResponse<>(httpRequest, 200, httpHeaders, Collections.singletonList(1), "terminal", "next",
                null, null, null);
        }, (pagingOptions, nextLink) -> {
            retrievals.incrementAndGet();
            return new PagedResponse<>(httpRequest, 200, httpHeaders, Collections.singletonList(2));
        }, token -> false);

        assertEquals(Arrays.asList(1, 2), pagedIterable.stream().collect(Collectors.toList()));
        assertEquals(2, retrievals.get());
    }

    @Test
    public void emptyNextLinkFallsBackToContinuationToken() {
        AtomicInteger tokenRetrievals = new AtomicInteger();
        AtomicInteger nextLinkRetrievals = new AtomicInteger();
        PagedIterable<Integer, String> pagedIterable = new PagedIterable<>(pagingOptions -> {
            int retrieval = tokenRetrievals.incrementAndGet();
            return retrieval == 1
                ? new PagedResponse<>(httpRequest, 200, httpHeaders, Collections.singletonList(1), "next-token", "",
                    null, null, null)
                : new PagedResponse<>(httpRequest, 200, httpHeaders, Collections.singletonList(2));
        }, (pagingOptions, nextLink) -> {
            nextLinkRetrievals.incrementAndGet();
            return null;
        });

        assertEquals(Arrays.asList(1, 2), pagedIterable.stream().collect(Collectors.toList()));
        assertEquals(2, tokenRetrievals.get());
        assertEquals(0, nextLinkRetrievals.get());
    }

    private static <T> void verifyIteratorSize(Iterator<T> iterator, long size) {
        long iteratorSize = 0;
        while (iterator.hasNext()) {
            ++iteratorSize;
            iterator.next();
        }
        Assertions.assertEquals(size, iteratorSize);
    }

    private NextPageMode nextPageMode;
    private final PagingStatistics pagingStatistics = new PagingStatistics();

    private static final class PagingStatistics {
        private int totalPages = 3;
        private int pageSize = 5;

        private int numberOfPageRetrievals;

        private void resetAll() {
            resetStatistics();
            totalPages = 3;
            pageSize = 5;
        }

        private void resetStatistics() {
            numberOfPageRetrievals = 0;
        }
    }

    // mock class and API for pageable operation
    public enum NextPageMode {
        CONTINUATION_TOKEN, NEXT_LINK
    }

    private static final class TodoItem {
        private final int id;

        private TodoItem(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }
    }

    private static final class ContinuationToken {
        private final int offset;
        private final boolean terminal;

        private ContinuationToken(int offset, boolean terminal) {
            this.offset = offset;
            this.terminal = terminal;
        }
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

    private PagedIterable<TodoItem, String> list() {
        pagingStatistics.resetStatistics();
        return new PagedIterable<>((pagingOptions) -> listSinglePage(pagingOptions),
            (pagingOptions, nextLink) -> listNextSinglePage(pagingOptions, nextLink));
    }

    private PagedResponse<TodoItem, String> listSinglePage(PagingOptions<String> pagingOptions) {
        Response<TodoPage> res = listSync(pagingOptions);
        return new PagedResponse<>(res.getRequest(), res.getStatusCode(), res.getHeaders(), res.getValue().getItems(),
            res.getValue().getContinuationToken(), res.getValue().getNextLink(), null, null, null);
    }

    private PagedResponse<TodoItem, String> listNextSinglePage(PagingOptions<String> pagingOptions, String nextLink) {
        Response<TodoPage> res = (nextLink == null) ? listSync(pagingOptions) : listNextSync(nextLink);
        return new PagedResponse<>(res.getRequest(), res.getStatusCode(), res.getHeaders(), res.getValue().getItems(),
            res.getValue().getContinuationToken(), res.getValue().getNextLink(), null, null, null);
    }

    private Response<TodoPage> listSync(PagingOptions<String> pagingOptions) {
        ++pagingStatistics.numberOfPageRetrievals;
        // mock request on first page
        if (pagingStatistics.totalPages == 0) {
            return new Response<>(httpRequest, 200, httpHeaders, new TodoPage(Collections.emptyList(), null, null));
        } else {
            switch (nextPageMode) {
                case NEXT_LINK: {
                    // first page
                    return new Response<>(httpRequest, 200, httpHeaders,
                        new TodoPage(createTodoItemList(0), null, "1"));
                }

                case CONTINUATION_TOKEN: {
                    if (pagingOptions.getContinuationToken() == null) {
                        // first page
                        return new Response<>(httpRequest, 200, httpHeaders,
                            new TodoPage(createTodoItemList(0), "1", null));
                    } else {
                        int pageIndex = Integer.parseInt(pagingOptions.getContinuationToken());
                        int nextPageIndex = pageIndex + 1;
                        String newContinuationToken
                            = nextPageIndex >= pagingStatistics.totalPages ? null : String.valueOf(nextPageIndex);
                        return new Response<>(httpRequest, 200, httpHeaders,
                            new TodoPage(createTodoItemList(pageIndex), newContinuationToken, null));
                    }
                }

                default:
                    throw new AssertionFailedError();
            }
        }
    }

    private Response<TodoPage> listNextSync(String nextLink) {
        ++pagingStatistics.numberOfPageRetrievals;
        // mock request on next page
        int pageIndex = Integer.parseInt(nextLink);
        int nextPageIndex = pageIndex + 1;
        String newNextLink = nextPageIndex >= pagingStatistics.totalPages ? null : String.valueOf(nextPageIndex);
        return new Response<>(httpRequest, 200, httpHeaders,
            new TodoPage(createTodoItemList(pageIndex), null, newNextLink));
    }

    private List<TodoItem> createTodoItemList(int pageIndex) {
        return IntStream.range(pageIndex * pagingStatistics.pageSize, (pageIndex + 1) * pagingStatistics.pageSize)
            .mapToObj(TodoItem::new)
            .collect(Collectors.toList());
    }
}
