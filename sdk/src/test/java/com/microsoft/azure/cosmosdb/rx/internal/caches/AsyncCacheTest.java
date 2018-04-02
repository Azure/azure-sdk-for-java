/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.microsoft.azure.cosmosdb.rx.internal.caches;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.testng.annotations.Test;

import com.microsoft.azure.cosmosdb.rx.internal.caches.AsyncCache;

import rx.Observable;
import rx.Single;
import rx.functions.Func1;

public class AsyncCacheTest {

    private static final int TIMEOUT = 2000;

    @Test(groups = { "internal" }, timeOut = TIMEOUT)
    public void getAsync() {
        AtomicInteger numberOfCacheRefreshes = new AtomicInteger(0);
        final Func1<Integer, Single<Integer>> refreshFunc = key -> {
            numberOfCacheRefreshes.incrementAndGet();
            return Single.just(key*2);
        };

        AsyncCache<Integer, Integer> cache = new AsyncCache<>();

        List<Single<Integer>> tasks = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                int key = j;
                tasks.add(cache.getAsync(key, -1, () -> refreshFunc.call(key)));
            }
        }

        Observable<Integer> o = Observable.merge(tasks.stream().map(s -> s.toObservable()).collect(Collectors.toList()));
        o.toList().toSingle().toBlocking().value();

        assertThat(numberOfCacheRefreshes.get()).isEqualTo(10);
        assertThat(cache.getAsync(2, -1, () -> refreshFunc.call(2)).toBlocking().value()).isEqualTo(4);

        Func1<Integer, Single<Integer>> refreshFunc1 = key -> {
            numberOfCacheRefreshes.incrementAndGet();
            return Single.just(key * 2 + 1);
        };

        List<Single<Integer>> tasks1 = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                int key = j;
                tasks1.add(cache.getAsync(key, key * 2, () -> refreshFunc1.call(key)));
            }

            for (int j = 0; j < 10; j++) {
                int key = j;
                tasks1.add(cache.getAsync(key, key * 2 , () -> refreshFunc1.call(key)));
            }
        }

        Observable<Integer> o1 = Observable.merge(tasks1.stream().map(s -> s.toObservable()).collect(Collectors.toList()));
        o1.toList().toSingle().toBlocking().value();

        assertThat(numberOfCacheRefreshes.get()).isEqualTo(20);
        assertThat(cache.getAsync(2, -1, () -> refreshFunc.call(2)).toBlocking().value()).isEqualTo(5);
    }
}
