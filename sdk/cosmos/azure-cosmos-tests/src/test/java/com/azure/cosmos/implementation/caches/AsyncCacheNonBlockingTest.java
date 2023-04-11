// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.caches;

import com.azure.cosmos.implementation.guava25.base.Function;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class AsyncCacheNonBlockingTest {
    private static final int TIMEOUT = 20000;

    @Test(groups = {"unit"}, timeOut = TIMEOUT)
    public void getAsync() {
        AtomicInteger numberOfCacheRefreshes = new AtomicInteger(0);
        final Function<Integer, Mono<Integer>> refreshFunc = key -> {
            return Mono.just(key * 2)
                .doOnNext(t -> {
                    numberOfCacheRefreshes.incrementAndGet();
                }).cache();
        };

        AsyncCacheNonBlocking<Integer, Integer> cache = new AsyncCacheNonBlocking<>();

        List<Mono<Integer>> tasks = new ArrayList<>();
        for (int j = 0; j < 10; j++) {
            int key = j;
            tasks.add(cache.getAsync(key, value -> refreshFunc.apply(key), forceRefresh -> false));
        }

        Flux<Integer> o = Flux.merge(tasks.stream().map(Mono::flux).collect(Collectors.toList()));
        o.collectList().single().block();

        assertThat(numberOfCacheRefreshes.get()).isEqualTo(10);
        assertThat(cache.getAsync(2, value -> refreshFunc.apply(2), forceRefresh -> false).block()).isEqualTo(4);

        // New function created to refresh the cache by sending forceRefresh true
        Function<Integer, Mono<Integer>> refreshFunc1 = key -> {
            numberOfCacheRefreshes.incrementAndGet();
            return Mono.just(key * 2 + 1);
        };

        List<Mono<Integer>> tasks1 = new ArrayList<>();
        for (int j = 0; j < 10; j++) {
            int key = j;
            tasks1.add(cache.getAsync(key, value -> refreshFunc1.apply(key), forceRefresh -> true));
        }

        Flux<Integer> o1 = Flux.merge(tasks1.stream().map(Mono::flux).collect(Collectors.toList()));
        o1.collectList().single().block();

        // verify that cache refresh happened
        assertThat(numberOfCacheRefreshes.get()).isEqualTo(20);
        // verify that we have the updated value in the cache now
        assertThat(cache.getAsync(2, value -> refreshFunc1.apply(2), forceRefresh -> false).block()).isEqualTo(5);


        Function<Integer, Mono<Integer>> refreshFunc2 = key -> {
            numberOfCacheRefreshes.incrementAndGet();
            return Mono.just(key * 2 + 3);
        };

        List<Mono<Integer>> tasks2 = new ArrayList<>();
        for (int j = 0; j < 10; j++) {
            int key = j;
            tasks2.add(cache.getAsync(key, value -> refreshFunc2.apply(key), forceRefresh -> false));
        }

        Flux<Integer> o2 = Flux.merge(tasks2.stream().map(Mono::flux).collect(Collectors.toList()));
        o2.collectList().single().block();

        // verify that no cache refresh happened
        assertThat(numberOfCacheRefreshes.get()).isEqualTo(20);
        // verify that we still have the old value in the cache
        assertThat(cache.getAsync(2, value -> refreshFunc2.apply(2), forceRefresh -> false).block()).isEqualTo(5);
    }

    @Test(groups = {"unit"}, timeOut = TIMEOUT)
    public void refreshAsync() throws InterruptedException {
        AtomicInteger numberOfCacheRefreshes = new AtomicInteger(0);
        final Function<Integer, Mono<Integer>> refreshFunc = key -> {
            return Mono.just(key * 2)
                    .doOnNext(t -> {
                        numberOfCacheRefreshes.incrementAndGet();
                    });
        };

        AsyncCacheNonBlocking<Integer, Integer> cache = new AsyncCacheNonBlocking<>();
        // populate the cache
        int cacheKey = 1;
        cache.getAsync(cacheKey, value -> refreshFunc.apply(cacheKey), forceRefresh -> false).block();
        assertThat(numberOfCacheRefreshes.get()).isEqualTo(1);

        // refresh the cache, since there is no refresh in progress, it will start a new one
        Function<Integer, Mono<Integer>> refreshFuncWithDelay = key -> {
            return Mono.just(key * 2)
                    .doOnNext(t -> numberOfCacheRefreshes.incrementAndGet())
                    .delayElement(Duration.ofMinutes(5));
        };

        cache.refresh(cacheKey, refreshFuncWithDelay);
        //since the refresh happens asynchronously, so wait for sometime
        Thread.sleep(500);
        assertThat(numberOfCacheRefreshes.get()).isEqualTo(2);

        // start another refresh, since there is a refresh in progress, so it will not start a new one
        cache.refresh(cacheKey, refreshFunc);
        //since the refresh happens asynchronously, so wait for sometime
        Thread.sleep(500);
        assertThat(numberOfCacheRefreshes.get()).isEqualTo(2);
    }
}
