// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http.models;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.List;

public class PagedIterableTests {

    @Test
    public void testPagedIterable() {
        PagedIterable<TodoItem> pagedIterable = this.list();
        verifyIteratorSize(pagedIterable.iterableByPage().iterator(), 2);
        verifyIteratorSize(pagedIterable.iterator(), 3);
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
    private static final class TodoItem {
    }

    private static final class TodoPage {
        private final List<TodoItem> items;
        private final String nextLink;

        private TodoPage(List<TodoItem> items, String nextLink) {
            this.items = items;
            this.nextLink = nextLink;
        }

        public List<TodoItem> getItems() {
            return items;
        }

        public String getNextLink() {
            return nextLink;
        }
    }

    private PagedIterable<TodoItem> list() {
        return new PagedIterable<>(() -> listSinglePage(), nextLink -> listNextSinglePage(nextLink));
    }

    private PagedResponse<TodoItem> listSinglePage() {
        Response<TodoPage> res = listSync();
        return new PagedResponse<>(res.getRequest(), res.getStatusCode(), res.getHeaders(), res.getBody(),
            res.getValue().getItems(), res.getValue().getNextLink());
    }

    private PagedResponse<TodoItem> listNextSinglePage(String nextLink) {
        Response<TodoPage> res = listNextSync(nextLink);
        return new PagedResponse<>(res.getRequest(), res.getStatusCode(), res.getHeaders(), res.getBody(),
            res.getValue().getItems(), res.getValue().getNextLink());
    }

    private Response<TodoPage> listSync() {
        // mock request on first page
        return new HttpResponse<>(null, 200, null, new TodoPage(List.of(new TodoItem(), new TodoItem()), "nextLink1"));
    }

    private Response<TodoPage> listNextSync(String nextLink) {
        // mock request on next page
        Assertions.assertEquals("nextLink1", nextLink);
        return new HttpResponse<>(null, 200, null, new TodoPage(List.of(new TodoItem()), null));
    }
}
