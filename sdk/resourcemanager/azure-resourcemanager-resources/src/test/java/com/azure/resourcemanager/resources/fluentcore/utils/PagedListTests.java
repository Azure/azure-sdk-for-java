// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.utils;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.PagedResponseBase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PagedListTests {

    private PagedList<Integer> list;

    private static final int LIST_SIZE = 55;

    @BeforeEach
    public void setupList() {
        final int pageSize = 10;
        final int lastFullPageInt = 50;
        final int lastPageSize = 5;

        final PagedFlux<Integer> mockedPagedFlux = new PagedFlux<>(
            // 1st page 0-9
            () -> Mono.just(new PagedResponseBase<>(null, 200, null,
                IntStream.range(0, pageSize).boxed().collect(Collectors.toList()), Integer.toString(pageSize),
                (Object) null)),
            // 2nd page 10-19
            // 3rd page 20-29
            // ...
            // last page 50-55
            (continuationToken) -> {
                int nextInt = Integer.parseInt(continuationToken);
                String nextToken = Integer.toString(nextInt + pageSize);
                if (nextInt < lastFullPageInt) {
                    return Mono.just(new PagedResponseBase<>(null, 200, null,
                        IntStream.range(nextInt, nextInt + pageSize).boxed().collect(Collectors.toList()), nextToken,
                        (Object) null));
                } else {
                    return Mono.just(new PagedResponseBase<>(null, 200, null,
                        IntStream.range(nextInt, nextInt + lastPageSize).boxed().collect(Collectors.toList()), null,
                        (Object) null));
                }
            }
        );
        final PagedIterable<Integer> pagedIterable = new PagedIterable<>(mockedPagedFlux);

        list = new PagedList<>(pagedIterable);
    }

    @Test
    public void sizeTest() {
        Assertions.assertEquals(55, list.size());
    }

    @Test
    public void getTest() {
        Assertions.assertEquals(15, (int) list.get(15));
    }

    @Test
    public void iterateTest() {
        int j = 0;
        for (int i : list) {
            Assertions.assertEquals(i, j++);
        }
    }

    @Test
    public void removeTest() {
        Integer i = list.get(10);
        list.remove(10);
        Assertions.assertEquals(LIST_SIZE - 1, list.size());
        Assertions.assertEquals(19, (int) list.get(18));
    }

    @Test
    public void addTest() {
        Integer i = list.get(10);
        list.add(100);
        Assertions.assertEquals(LIST_SIZE + 1, list.size());
        Assertions.assertEquals(100, (int) list.get(LIST_SIZE));
    }

    @Test
    public void containsTest() {
        Assertions.assertTrue(list.contains(0));
        Assertions.assertTrue(list.contains(3));
        Assertions.assertTrue(list.contains(19));
        Assertions.assertFalse(list.contains(LIST_SIZE + 5));
    }

    @Test
    public void containsAllTest() {
        List<Integer> subList = new ArrayList<>();
        subList.addAll(Arrays.asList(0, 3, 19));
        Assertions.assertTrue(list.containsAll(subList));
        subList.add(LIST_SIZE);
        Assertions.assertFalse(list.containsAll(subList));
    }

    @Test
    public void subListTest() {
        List<Integer> subList = list.subList(5, 15);
        Assertions.assertEquals(10, subList.size());
        Assertions.assertTrue(list.containsAll(subList));
        Assertions.assertEquals(7, (int) subList.get(2));
    }

    @Test
    public void testIndexOf() {
        Assertions.assertEquals(15, list.indexOf(15));
    }

    @Test
    public void testLastIndexOf() {
        Assertions.assertEquals(15, list.lastIndexOf(15));
    }


    @Test
    public void testIteratorWithListSizeInvocation() {
        ListIterator<Integer> itr = list.listIterator();
        list.size();
        int j = 0;
        while (itr.hasNext()) {
            Assertions.assertEquals(j++, (long) itr.next());
        }
    }

    @Test
    public void testIteratorPartsWithSizeInvocation() {
        ListIterator<Integer> itr = list.listIterator();
        int j = 0;
        while (j < 5) {
            Assertions.assertTrue(itr.hasNext());
            Assertions.assertEquals(j++, (long) itr.next());
        }
        list.size();
        while (j < 10) {
            Assertions.assertTrue(itr.hasNext());
            Assertions.assertEquals(j++, (long) itr.next());
        }
    }

    @Test
    public void testIteratorWithLoadNextPageInvocation() {
        ListIterator<Integer> itr = list.listIterator();
        int j = 0;
        while (j < 5) {
            Assertions.assertTrue(itr.hasNext());
            Assertions.assertEquals(j++, (long) itr.next());
        }
        list.loadNextPage();
        while (j < 10) {
            Assertions.assertTrue(itr.hasNext());
            Assertions.assertEquals(j++, (long) itr.next());
        }
        list.loadNextPage();
        while (itr.hasNext()) {
            Assertions.assertEquals(j++, (long) itr.next());
        }
        Assertions.assertEquals(LIST_SIZE, j);
    }

    @Test
    public void testIteratorOperations() {
        ListIterator<Integer> itr1 = list.listIterator();
        IllegalStateException expectedException = null;
        try {
            itr1.remove();
        } catch (IllegalStateException ex) {
            expectedException = ex;
        }
        Assertions.assertNotNull(expectedException);

        ListIterator<Integer> itr2 = list.listIterator();
        Assertions.assertTrue(itr2.hasNext());
        Assertions.assertEquals(0, (long) itr2.next());
        itr2.remove();
        Assertions.assertTrue(itr2.hasNext());
        Assertions.assertEquals(1, (long) itr2.next());

        itr2.set(100);
        Assertions.assertTrue(itr2.hasPrevious());
        Assertions.assertEquals(100, (long) itr2.previous());
        Assertions.assertTrue(itr2.hasNext());
        Assertions.assertEquals(100, (long) itr2.next());
    }

    @Test
    public void testAddViaIteratorWhileIterating() {
        ListIterator<Integer> itr1 = list.listIterator();
        while (itr1.hasNext()) {
            Integer val = itr1.next();
            if (val < 10) {
                itr1.add(99);
            }
        }
        Assertions.assertEquals(LIST_SIZE + 10, list.size());
    }

    @Test
    public void testRemoveViaIteratorWhileIterating() {
        ListIterator<Integer> itr1 = list.listIterator();
        while (itr1.hasNext()) {
            itr1.next();
            itr1.remove();
        }
        Assertions.assertEquals(0, list.size());
    }
}
