// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.internal.caches;

import org.testng.annotations.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class AsyncCacheTest {

    private static final int TIMEOUT = 2000;

    @Test(groups = { "unit" }, timeOut = TIMEOUT)
    public void getAsync() {
        AtomicInteger numberOfCacheRefreshes = new AtomicInteger(0);
        final Function<Integer, Mono<Integer>> refreshFunc = key -> {
            numberOfCacheRefreshes.incrementAndGet();
            return Mono.just(key*2);
        };

        AsyncCache<Integer, Integer> cache = new AsyncCache<>();

        List<Mono<Integer>> tasks = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                int key = j;
                tasks.add(cache.getAsync(key, -1, () -> refreshFunc.apply(key)));
            }
        }

        Flux<Integer> o = Flux.merge(tasks.stream().map(Mono::flux).collect(Collectors.toList()));
        o.collectList().single().block();

        assertThat(numberOfCacheRefreshes.get()).isEqualTo(10);
        assertThat(cache.getAsync(2, -1, () -> refreshFunc.apply(2)).block()).isEqualTo(4);

        Function<Integer, Mono<Integer>> refreshFunc1 = key -> {
            numberOfCacheRefreshes.incrementAndGet();
            return Mono.just(key * 2 + 1);
        };

        List<Mono<Integer>> tasks1 = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                int key = j;
                tasks1.add(cache.getAsync(key, key * 2, () -> refreshFunc1.apply(key)));
            }

            for (int j = 0; j < 10; j++) {
                int key = j;
                tasks1.add(cache.getAsync(key, key * 2 , () -> refreshFunc1.apply(key)));
            }
        }

        Flux<Integer> o1 = Flux.merge(tasks1.stream().map(Mono::flux).collect(Collectors.toList()));
        o1.collectList().single().block();

        assertThat(numberOfCacheRefreshes.get()).isEqualTo(20);
        assertThat(cache.getAsync(2, -1, () -> refreshFunc.apply(2)).block()).isEqualTo(5);
    }
}
