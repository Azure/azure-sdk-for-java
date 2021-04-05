// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.utils;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.core.util.logging.ClientLogger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.IntStream;

public class PagedConverterTests {

    private final ClientLogger logger = new ClientLogger(this.getClass());

    @Test
    public void testMapPage() {
        PagedFlux<String> pagedFlux = mockPagedFlux("base", 0, 10, 4);
        PagedIterable<String> pagedIterable = new PagedIterable<>(pagedFlux);

        PagedIterable<String> convertedPagedIterable = PagedConverter.mapPage(pagedIterable, item -> item + "#");

        Iterator<PagedResponse<String>> iteratorByPage = convertedPagedIterable.iterableByPage().iterator();
        Assertions.assertTrue(iteratorByPage.hasNext());
        PagedResponse<String> page1 = iteratorByPage.next();
        Assertions.assertEquals(4, page1.getValue().size());
        Assertions.assertEquals("base0#", page1.getValue().get(0));
        Assertions.assertEquals("base3#", page1.getValue().get(3));
        Assertions.assertTrue(iteratorByPage.hasNext());
        Assertions.assertEquals(4, iteratorByPage.next().getValue().size());
        Assertions.assertTrue(iteratorByPage.hasNext());
        Assertions.assertEquals(2, iteratorByPage.next().getValue().size());
        Assertions.assertFalse(iteratorByPage.hasNext());

        Iterator<String> iterator = convertedPagedIterable.iterator();
        Assertions.assertTrue(iterator.hasNext());
        Assertions.assertEquals("base0#", iterator.next());
        for (int i = 1; i < 10; ++i) {
            Assertions.assertTrue(iterator.hasNext());
            Assertions.assertEquals("base" + i + "#", iterator.next());
        }
        Assertions.assertFalse(iterator.hasNext());
    }

    @Test
    public void testMapPageWithPagedIterable() {
        PagedFlux<String> pagedFlux = mockPagedFlux("base", 0, 10, 4);
        PagedIterable<String> pagedIterable = new PagedIterable<>(pagedFlux);

        PagedIterable<String> convertedPagedIterable = PagedConverter.mapPage(pagedIterable, item -> item + "#");

        PagedIterable<String> afterMapPage = convertedPagedIterable.mapPage(item -> item + "#");
        Assertions.assertEquals(10, afterMapPage.stream().count());
    }

    @Test
    public void testMapPageIterator() {
        PagedFlux<String> pagedFlux = mockPagedFlux("base", 0, 10, 4);
        PagedFlux<String> convertedPagedFlux = PagedConverter.mapPage(pagedFlux, item -> item + "#");

        StepVerifier.create(convertedPagedFlux.byPage())
            .expectSubscription()
            .expectNextMatches(p -> p.getValue().size() == 4
                && p.getValue().get(0).equals("base0#")
                && p.getValue().get(p.getValue().size() - 1).equals("base3#"))
            .expectNextMatches(p -> p.getValue().size() == 4)
            .expectNextMatches(p -> p.getValue().size() == 2)
            .expectComplete()
            .verify();
    }

    @Test
    public void testFlatMapPage() {
        PagedFlux<String> pagedFlux = mockPagedFlux("base", 0, 10, 4);
        PagedFlux<String> convertedPagedFlux = PagedConverter.flatMapPage(pagedFlux, item -> Flux.just(item, item + "#"));
        StepVerifier.create(convertedPagedFlux.byPage())
            .expectSubscription()
            .expectNextMatches(p -> p.getValue().size() == 8
                && p.getValue().get(0).equals("base0")
                && p.getValue().get(p.getValue().size() - 1).equals("base3#"))
            .expectNextMatches(p -> p.getValue().size() == 8)
            .expectNextMatches(p -> p.getValue().size() == 4)
            .expectComplete()
            .verify();

        Assertions.assertEquals(10 * 2, new PagedIterable<>(convertedPagedFlux).stream().count());
    }

    @Test
    public void testMergePagedFlux() {
        PagedFlux<String> pagedFlux = mockPagedFlux("base", 0, 3, 2);
        PagedFlux<String> mergedPagedFlux = PagedConverter.mergePagedFlux(pagedFlux, item -> mockPagedFlux(item + "sub", 0, 10, 4));
        StepVerifier.create(mergedPagedFlux.byPage())
            .expectSubscription()
            .expectNextMatches(p -> p.getValue().size() == 4
                && p.getValue().get(0).equals("base0sub0")
                && p.getValue().get(p.getValue().size() - 1).equals("base0sub3"))
            .expectNextMatches(p -> p.getValue().size() == 4)
            .expectNextMatches(p -> p.getValue().size() == 2)
            .expectNextCount(3 * 2)
            .expectComplete()
            .verify();

        Assertions.assertEquals(3 * 10, new PagedIterable<>(mergedPagedFlux).stream().count());
    }

    @Test
    public void testMergePagedFluxContainsEmptyPage() {
        PagedFlux<String> pagedFlux = mockPagedFlux("base", 0, 3, 2);
        PagedFlux<String> mergedPagedFlux = PagedConverter.mergePagedFlux(pagedFlux, item -> {
            if (item.equals("base1")) {
                return mockEmptyPagedFlux();
            } else {
                return mockPagedFlux(item + "sub", 0, 10, 4);
            }
        });
        StepVerifier.create(mergedPagedFlux.byPage())
            .expectSubscription()
            .expectNextMatches(p -> p.getValue().size() == 4
                && p.getValue().get(0).equals("base0sub0")
                && p.getValue().get(p.getValue().size() - 1).equals("base0sub3"))
            .expectNextMatches(p -> p.getValue().size() == 4)
            .expectNextMatches(p -> p.getValue().size() == 2)
            .expectNextCount(3)
            .expectComplete()
            .verify();

        Assertions.assertEquals(2 * 10, new PagedIterable<>(mergedPagedFlux).stream().count());
    }

    @Test
    public void testMergePagedFluxContainsEmptyPage2() {
        PagedFlux<String> pagedFlux = mockPagedFlux("base", 0, 3, 2);
        PagedFlux<String> mergedPagedFlux = PagedConverter.mergePagedFlux(pagedFlux, item -> {
            if (item.equals("base0") || item.equals("base1")) {
                return mockEmptyPagedFlux();
            } else {
                return mockPagedFlux(item + "sub", 0, 10, 4);
            }
        });
        StepVerifier.create(mergedPagedFlux.byPage())
            .expectSubscription()
            .expectNextMatches(p -> p.getValue().size() == 4
                && p.getValue().get(0).equals("base2sub0")
                && p.getValue().get(p.getValue().size() - 1).equals("base2sub3"))
            .expectNextMatches(p -> p.getValue().size() == 4)
            .expectNextMatches(p -> p.getValue().size() == 2)
            .expectComplete()
            .verify();

        Assertions.assertEquals(10, new PagedIterable<>(mergedPagedFlux).stream().count());
    }

    @Test
    public void testMapPageOnePage() {
        AtomicInteger pageCount = new AtomicInteger(0);
        PagedFlux<String> pagedFlux = mockPagedFlux("base", 0, 10, 4, pageCount);
        PagedFlux<String> convertedPagedFlux = PagedConverter.mapPage(pagedFlux, item -> item + "#");
        PagedIterable<String> pagedIterable = new PagedIterable<>(convertedPagedFlux);

        pagedIterable.stream().findFirst().get();

        Assertions.assertEquals(1, pageCount.get());
    }

    @Test
    public void testFlatMapPageOnePage() {
        AtomicInteger pageCount = new AtomicInteger(0);
        PagedFlux<String> pagedFlux = mockPagedFlux("base", 0, 10, 4, pageCount);
        PagedFlux<String> convertedPagedFlux = PagedConverter.flatMapPage(pagedFlux, item -> Flux.just(item, item + "#"));
        PagedIterable<String> pagedIterable = new PagedIterable<>(convertedPagedFlux);

        pagedIterable.stream().findFirst().get();

        Assertions.assertEquals(1, pageCount.get());
    }

    @Test
    @Disabled("not working as expected")
    public void testMergePagedFluxOnePage() {
        AtomicInteger pageCountRoot = new AtomicInteger(0);
        AtomicInteger pageCount = new AtomicInteger(0);
        PagedFlux<String> pagedFlux = mockPagedFlux("base", 0, 3, 2, pageCountRoot);
        PagedFlux<String> mergedPagedFlux = PagedConverter.mergePagedFlux(pagedFlux, item -> mockPagedFlux(item + "sub", 0, 10, 4, pageCount));
        PagedIterable<String> pagedIterable = new PagedIterable<>(mergedPagedFlux);

        pagedIterable.stream().findFirst().get();

        Assertions.assertEquals(1, pageCountRoot.get());
        Assertions.assertEquals(1, pageCount.get());
    }

    private static PagedFlux<String> mockEmptyPagedFlux() {
        PagedResponseBase<Void, String> emptyPage = new PagedResponseBase<>(null, 200, null,
            Collections.emptyList(), null, null);
        return new PagedFlux<>(() -> Mono.just(emptyPage),
            continuationToken -> Mono.empty());
    }

    private static PagedFlux<String> mockPagedFlux(String prefix, int startInclusive, int stopExclusive, int pageSize) {
        return mockPagedFlux(prefix, startInclusive, stopExclusive, pageSize, new AtomicInteger(0));
    }

    private static PagedFlux<String> mockPagedFlux(String prefix, int startInclusive, int stopExclusive, int pageSize, AtomicInteger pageCount) {
        Iterator<Integer> iterator = IntStream.range(startInclusive, stopExclusive).iterator();
        Map<String, PagedResponse<String>> pages = new HashMap<>();
        String currentContinuationToken = prefix;
        while (iterator.hasNext()) {
            List<String> items = new ArrayList<>();
            Integer possibleNext = null;
            for (int i = 0; i < pageSize; ++i) {
                if (!iterator.hasNext()) {
                    break;
                }
                int item = iterator.next();
                items.add(prefix + item);
                possibleNext = item + 1;
            }

            String newContinuationToken = iterator.hasNext() ? prefix + possibleNext : null;
            PagedResponse<String> page = new PagedResponseBase<>(null, 200, null,
                items, newContinuationToken, null);
            pages.put(currentContinuationToken, page);
            currentContinuationToken = newContinuationToken;
        }

        Function<String, PagedResponse<String>> nextPage = continuationToken -> {
            if (continuationToken == null) {
                throw new IllegalArgumentException();
            }

            pageCount.getAndIncrement();

            return pages.get(continuationToken);
        };
        return new PagedFlux<>(() -> Mono.just(nextPage.apply(prefix)),
            continuationToken -> Mono.just(nextPage.apply(continuationToken)));
    }
}
