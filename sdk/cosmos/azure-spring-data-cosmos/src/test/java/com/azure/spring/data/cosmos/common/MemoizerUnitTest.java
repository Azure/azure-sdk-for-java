// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.common;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;

public class MemoizerUnitTest {
    private static final String KEY = "key_1";
    private static final Map<String, AtomicInteger> countMap = new HashMap<>();
    private static final Function<String, Integer> memoizedFunction =
            Memoizer.memoize(MemoizerUnitTest::incrCount);

    @Before
    public void setUp() {
        countMap.put(KEY, new AtomicInteger(0));
    }

    @Test
    public void testMemoizedFunctionShouldBeCalledOnlyOnce() {
        IntStream
        .range(0, 10)
            .forEach(number -> memoizedFunction.apply(KEY));

        assertEquals(1, countMap.get(KEY).get());
    }

    @Test
    public void testDifferentMemoizersShouldNotShareTheSameCache() {
        IntStream
        .range(0, 10)
            .forEach(number -> Memoizer.memoize(MemoizerUnitTest::incrCount).apply(KEY));

        assertEquals(10, countMap.get(KEY).get());
    }

    private static int incrCount(String key) {
        return countMap.get(key).incrementAndGet();
    }

}
