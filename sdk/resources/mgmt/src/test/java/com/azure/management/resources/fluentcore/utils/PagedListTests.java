// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.management.resources.fluentcore.utils;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.PagedResponseBase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PagedListTests {

    private PagedList<Integer> list;

    @BeforeEach
    public void setupList() {
        final int pageSize = 10;
        final int lastFullPageInt = 50;
        final int lastPageSize = 5;

        final PagedFlux<Integer> mockedPagedFlux = new PagedFlux<>(
            // 1st page 0-9
            () -> Mono.just(new PagedResponseBase<>(null, 200, null,
                IntStream.range(0, pageSize).boxed().collect(Collectors.toList()), Integer.toString(pageSize),
                (Object)null)),
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
}
