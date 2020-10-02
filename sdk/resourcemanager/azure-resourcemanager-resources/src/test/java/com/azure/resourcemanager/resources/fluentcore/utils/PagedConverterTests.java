// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.utils;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.core.util.logging.ClientLogger;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;

public class PagedConverterTests {

    private final ClientLogger logger = new ClientLogger(this.getClass());

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
    }

    private static PagedFlux<String> mockPagedFlux(String prefix, int startInclusive, int stopExclusive, int pageSize) {
        Iterator<Integer> iterator = IntStream.range(startInclusive, stopExclusive).iterator();
        Function<String, PagedResponseBase<Void, String>> nextPage = continuationToken -> {
            if (continuationToken == null) {
                throw new IllegalArgumentException();
            }

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

            if (!continuationToken.equals(prefix) && !items.isEmpty()) {
                assert continuationToken.equals(items.iterator().next());
            }

            return new PagedResponseBase<>(null, 200, null,
                items, iterator.hasNext() ? prefix + possibleNext : null,
                null);
        };
        return new PagedFlux<>(() -> Mono.just(nextPage.apply(prefix)),
            continuationToken -> Mono.just(nextPage.apply(continuationToken)));
    }
}
