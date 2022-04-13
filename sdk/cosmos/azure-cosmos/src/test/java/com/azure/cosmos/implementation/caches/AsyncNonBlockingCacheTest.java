// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.caches;

import com.azure.cosmos.implementation.guava25.base.Function;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class AsyncNonBlockingCacheTest {
    private static final int TIMEOUT = 2000000;

    @Test(groups = {"unit"}, timeOut = TIMEOUT)
    public void getAsync() {
        AtomicInteger numberOfCacheRefreshes = new AtomicInteger(0);
        final Function<Integer, Mono<Integer>> refreshFunc = key -> {
            numberOfCacheRefreshes.incrementAndGet();
            return Mono.just(key * 2);
        };

        AsyncCacheNonBlocking<Integer, Integer> cache = new AsyncCacheNonBlocking<>();

        List<Mono<Integer>> tasks = new ArrayList<>();
        for (int j = 0; j < 10; j++) {
            int key = j;
            tasks.add(cache.getAsync(key, value -> refreshFunc.apply(key), false));
        }

        Flux<Integer> o = Flux.merge(tasks.stream().map(Mono::flux).collect(Collectors.toList()));
        o.collectList().single().block();

        assertThat(numberOfCacheRefreshes.get()).isEqualTo(10);
        assertThat(cache.getAsync(2, value -> refreshFunc.apply(2), false).block()).isEqualTo(4);

        Function<Integer, Mono<Integer>> refreshFunc1 = key -> {
            numberOfCacheRefreshes.incrementAndGet();
            return Mono.just(key * 2 + 1);
        };

        List<Mono<Integer>> tasks1 = new ArrayList<>();
        for (int j = 0; j < 10; j++) {
            int key = j;
            tasks1.add(cache.getAsync(key, value -> refreshFunc1.apply(key), true));
            System.out.println(refreshFunc1.apply(key));
        }

        Flux<Integer> o1 = Flux.merge(tasks1.stream().map(Mono::flux).collect(Collectors.toList()));
        o1.collectList().single().block();

        assertThat(numberOfCacheRefreshes.get()).isEqualTo(30);
        assertThat(cache.getAsync(2, value -> refreshFunc1.apply(2), false).block()).isEqualTo(5);
    }
}
