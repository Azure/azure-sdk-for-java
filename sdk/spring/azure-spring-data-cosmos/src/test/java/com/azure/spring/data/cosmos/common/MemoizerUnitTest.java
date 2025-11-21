// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.common;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.*;

public class MemoizerUnitTest {

    private static final String KEY = "key_1";
    private static final Map<String, AtomicInteger> COUNT_MAP = new HashMap<>();
    private static final Function<String, Integer> MEMOIZED_FUNCTION =
        Memoizer.memoize(MemoizerUnitTest::incrCount);

    @BeforeEach
    public void setUp() {
        COUNT_MAP.put(KEY, new AtomicInteger(0));
    }

    @Test
    public void testMemoizedFunctionShouldBeCalledOnlyOnce() {
        IntStream
            .range(0, 10)
            .forEach(number -> MEMOIZED_FUNCTION.apply(KEY));

        assertEquals(1, COUNT_MAP.get(KEY).get());
    }

    @Test
    public void testDifferentMemoizersShouldNotShareTheSameCache() {
        IntStream
            .range(0, 10)
            .forEach(number -> Memoizer.memoize(MemoizerUnitTest::incrCount).apply(KEY));

        assertEquals(10, COUNT_MAP.get(KEY).get());
    }

    private static int incrCount(String key) {
        return COUNT_MAP.get(key).incrementAndGet();
    }

}
