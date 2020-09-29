// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.caches;

import org.testng.annotations.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class AsyncCacheTest {

    private static final int TIMEOUT = 5000;

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


    @Test(groups = { "unit" }, timeOut = TIMEOUT)
    public void noCacheEntry_Failure() {
        AsyncCache<Integer, Integer> cache = new AsyncCache<>();

        final Function<Void, Mono<Integer>> failureFunction = (dummy) ->
            Mono.<Integer>error(new RuntimeException("fetch function failure")).delayElement(Duration.ofSeconds(1));

        try {
            cache.getAsync(0, -1, () -> failureFunction.apply(null)).block();
            fail("should fail");
        } catch (Exception e) {
            assertThat(e.getMessage()).isEqualTo("fetch function failure");
        }
    }


    @Test(groups = { "unit" }, timeOut = TIMEOUT)
    public void failure_retainOldValue() {
        AsyncCache<Integer, Integer> cache = new AsyncCache<>();

        final Function<Integer, Mono<Integer>> refreshFunc = key ->
            Mono.just(2 * key).delayElement(Duration.ofSeconds(1));

        final Function<Void, Mono<Integer>> failureFunction = (dummy) ->
            Mono.<Integer>error(new RuntimeException("fetch function failure")).delayElement(Duration.ofSeconds(1));

        Mono<Integer> resultAsync = cache.getAsync(0, -1, () -> refreshFunc.apply(2));
        int value = resultAsync.block();
        assertThat(value).isEqualTo(4);
        int newValue = cache.getAsync(0, value, () -> failureFunction.apply(null)).block();

        assertThat(newValue).isEqualTo(value);

        // remove the entry
        cache.remove(0);

        // try again, this time failure should result in failure
        try {
            cache.getAsync(0, value, () -> failureFunction.apply(null)).block();
            fail("should fail");
        } catch (Exception e) {
            assertThat(e.getMessage()).isEqualTo("fetch function failure");
        }
    }

    @Test(groups = { "unit" }, timeOut = TIMEOUT)
    public void multipleFailures_retainOldValue() {
        AtomicInteger numberOfCacheRefreshes = new AtomicInteger(0);

        AsyncCache<Integer, Integer> cache = new AsyncCache<>();

        final Function<Integer, Mono<Integer>> refreshFunc = key ->
            Mono.just(2 * key).delayElement(Duration.ofSeconds(1));

        final Function<Void, Mono<Integer>> failureFunction = (dummy) -> {
            numberOfCacheRefreshes.incrementAndGet();
            return Mono.<Integer>error(new RuntimeException("fetch function failure")).delayElement(Duration.ofSeconds(1));
        };

        Mono<Integer> resultAsync = cache.getAsync(0, -1, () -> refreshFunc.apply(2));
        int value = resultAsync.block();
        assertThat(value).isEqualTo(4);

        int numberOfFailedFetches = 3;
        for (int i = 0; i < numberOfFailedFetches; i++) {
            // value is obsolete, each getAsync will result in invoking the fetch function.
            int newValue = cache.getAsync(0, value, () -> failureFunction.apply(null)).block();
            assertThat(newValue).isEqualTo(value);
            assertThat(numberOfCacheRefreshes.get()).isEqualTo(i + 1);
        }

        // validate
        assertThat(numberOfCacheRefreshes.get()).isEqualTo(numberOfFailedFetches);

        // not invoking the function again because value is not obsolete
        int newValue = cache.getAsync(0, -1, () -> failureFunction.apply(null)).block();

        // no new invocation
        assertThat(numberOfCacheRefreshes.get()).isEqualTo(numberOfFailedFetches);
        assertThat(newValue).isEqualTo(value);


        // remove the entry
        cache.remove(0);

        // try again, this time failure should result in failure
        try {
            cache.getAsync(0, value, () -> failureFunction.apply(null)).block();
            fail("should fail");
        } catch (Exception e) {
            assertThat(e.getMessage()).isEqualTo("fetch function failure");
        }
    }
}
