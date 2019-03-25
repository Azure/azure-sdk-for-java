/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.common.mgmt;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Supplier;

public class PagedListTests {
    private PagedList<Integer> list;

    @Before
    public void setupList() {
        list = new PagedList<Integer>(new TestPage(0, 21)) {
            @Override
            public Page<Integer> nextPage(String nextPageLink) {
                int pageNum = Integer.parseInt(nextPageLink);
                return new TestPage(pageNum, 21);
            }
        };
    }

    @Test
    public void sizeTest() {
        Assert.assertEquals(20, list.size());
    }

    @Test
    public void getTest() {
        Assert.assertEquals(15, (int) list.get(15));
    }

    @Test
    public void iterateTest() {
        int j = 0;
        for (int i : list) {
            Assert.assertEquals(i, j++);
        }
    }

    @Test
    public void removeTest() {
        Integer i = list.get(10);
        list.remove(10);
        Assert.assertEquals(19, list.size());
        Assert.assertEquals(19, (int) list.get(18));
    }

    @Test
    public void addTest() {
        Integer i = list.get(10);
        list.add(100);
        Assert.assertEquals(21, list.size());
        Assert.assertEquals(100, (int) list.get(11));
        Assert.assertEquals(19, (int) list.get(20));
    }

    @Test
    public void containsTest() {
        Assert.assertTrue(list.contains(0));
        Assert.assertTrue(list.contains(3));
        Assert.assertTrue(list.contains(19));
        Assert.assertFalse(list.contains(20));
    }

    @Test
    public void containsAllTest() {
        List<Integer> subList = new ArrayList<>();
        subList.addAll(Arrays.asList(0, 3, 19));
        Assert.assertTrue(list.containsAll(subList));
        subList.add(20);
        Assert.assertFalse(list.containsAll(subList));
    }

    @Test
    public void subListTest() {
        List<Integer> subList = list.subList(5, 15);
        Assert.assertEquals(10, subList.size());
        Assert.assertTrue(list.containsAll(subList));
        Assert.assertEquals(7, (int) subList.get(2));
    }

    @Test
    public void testIndexOf() {
        Assert.assertEquals(15, list.indexOf(15));
    }

    @Test
    public void testLastIndexOf() {
        Assert.assertEquals(15, list.lastIndexOf(15));
    }


    @Test
    public void testIteratorWithListSizeInvocation() {
        ListIterator<Integer> itr = list.listIterator();
        list.size();
        int j = 0;
        while (itr.hasNext()) {
            Assert.assertEquals(j++, (long) itr.next());
        }
    }

    @Test
    public void testIteratorPartsWithSizeInvocation() {
        ListIterator<Integer> itr = list.listIterator();
        int j = 0;
        while (j < 5) {
            Assert.assertTrue(itr.hasNext());
            Assert.assertEquals(j++, (long) itr.next());
        }
        list.size();
        while (j < 10) {
            Assert.assertTrue(itr.hasNext());
            Assert.assertEquals(j++, (long) itr.next());
        }
    }

    @Test
    public void testIteratorWithLoadNextPageInvocation() {
        ListIterator<Integer> itr = list.listIterator();
        int j = 0;
        while (j < 5) {
            Assert.assertTrue(itr.hasNext());
            Assert.assertEquals(j++, (long) itr.next());
        }
        list.loadNextPage();
        while (j < 10) {
            Assert.assertTrue(itr.hasNext());
            Assert.assertEquals(j++, (long) itr.next());
        }
        list.loadNextPage();
        while (itr.hasNext()) {
            Assert.assertEquals(j++, (long) itr.next());
        }
        Assert.assertEquals(20, j);
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
        Assert.assertNotNull(expectedException);

        ListIterator<Integer> itr2 = list.listIterator();
        Assert.assertTrue(itr2.hasNext());
        Assert.assertEquals(0, (long) itr2.next());
        itr2.remove();
        Assert.assertTrue(itr2.hasNext());
        Assert.assertEquals(1, (long) itr2.next());

        itr2.set(100);
        Assert.assertTrue(itr2.hasPrevious());
        Assert.assertEquals(100, (long) itr2.previous());
        Assert.assertTrue(itr2.hasNext());
        Assert.assertEquals(100, (long) itr2.next());
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
        Assert.assertEquals(30, list.size());
    }

    @Test
    public void testRemoveViaIteratorWhileIterating() {
        ListIterator<Integer> itr1 = list.listIterator();
        while (itr1.hasNext()) {
            itr1.next();
            itr1.remove();
        }
        Assert.assertEquals(0, list.size());
    }

    @Test
    public void canHandleIntermediateEmptyPage() {
        List<Integer> pagedList = new PagedList<Integer>(new Page<Integer>() {
            @Override
            public String nextPageLink() {
                return "A";
            }

            @Override
            public List<Integer> items() {
                List<Integer> list = new ArrayList<>();
                list.add(1);
                list.add(2);
                return list;
            }
        }) {
            @Override
            public Page<Integer> nextPage(String nextPageLink) {
                if (nextPageLink == "A") {
                    return new Page<Integer>() {
                        @Override
                        public String nextPageLink() {
                            return "B";
                        }

                        @Override
                        public List<Integer> items() {
                            return new ArrayList<>(); // EMPTY PAGE
                        }
                    };
                } else if (nextPageLink == "B") {
                    return new Page<Integer>() {
                        @Override
                        public String nextPageLink() {
                            return "C";
                        }

                        @Override
                        public List<Integer> items() {
                            List<Integer> list = new ArrayList<>();
                            list.add(3);
                            list.add(4);
                            return list;
                        }
                    };
                } else if (nextPageLink == "C") {
                    return new Page<Integer>() {
                        @Override
                        public String nextPageLink() {
                            return null;
                        }

                        @Override
                        public List<Integer> items() {
                            List<Integer> list = new ArrayList<>();
                            list.add(5);
                            list.add(6);
                            return list;
                        }
                    };
                }
                throw new RuntimeException("nextPage should not be called after a page with next link as null");
            }
        };
        ListIterator<Integer> itr = pagedList.listIterator();
        int c = 1;
        while (itr.hasNext()) {
            Assert.assertEquals(c, (int) itr.next());
            c++;
        }
        Assert.assertEquals(7, c);
    }

    @Test
    public void canCreateFluxFromPagedList() {
        // Test lazy flux can be created by ensuring loadNextPage invoked lazily
        //
        class FluxFromPagedList {
            int loadNextPageCallCount;

            Flux<Integer> toFlux() {
                return firstFlux().concatWith(nextFlux());
            }

            Flux<Integer> firstFlux() {
                return Flux.defer((Supplier<Flux<Integer>>) () -> Flux.fromIterable(list.currentPage().items()));
            }

            Flux<Integer> nextFlux() {
                return Flux.defer((Supplier<Flux<Integer>>) () -> {
                    if (list.hasNextPage()) {
                        list.loadNextPage();
                        loadNextPageCallCount++;
                        return Flux.fromIterable(list.currentPage().items()).concatWith(Flux.defer(new Supplier<Flux<Integer>>() {
                            @Override
                            public Flux<Integer> get() {
                                return nextFlux();
                            }
                        }));
                    } else {
                        return Flux.empty();
                    }
                });
            }
        }

        FluxFromPagedList obpl = new FluxFromPagedList();

        final Integer[] cnt = new Integer[] { 0 };
        obpl.toFlux().subscribe(integer -> {
            Assert.assertEquals(cnt[0], integer);
            cnt[0]++;
        });
        Assert.assertEquals(20, (long) cnt[0]);
        Assert.assertEquals(19, obpl.loadNextPageCallCount);
    }


    public static class TestPage implements Page<Integer> {
        private int page;
        private int max;

        public TestPage(int page, int max) {
            this.page = page;
            this.max = max;
        }

        @Override
        public String nextPageLink() {
            if (page + 1 == max) {
                return null;
            }
            return Integer.toString(page + 1);
        }

        @Override
        public List<Integer> items() {
            if (page + 1 != max) {
                List<Integer> items = new ArrayList<>();
                items.add(page);
                return items;
            } else {
                return new ArrayList<>();
            }
        }
    }
}
