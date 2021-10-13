// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.context.core;

import com.azure.spring.core.util.Memoizer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Function;
import reactor.util.function.Tuple2;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MemoizerTest {

    private static final String INPUT = "input";
    private static final String INPUT2 = "input2";
    private static final String OUTPUT = "output";

    @Test
    public void memoizeFunc() {
        ExpensiveOperation expensiveOperation = mock(ExpensiveOperation.class);
        when(expensiveOperation.compute(INPUT)).thenReturn(OUTPUT);
        Function<String, String> memoized = Memoizer.memoize(expensiveOperation::compute);
        Assertions.assertEquals(memoized.apply(INPUT), OUTPUT);
        Assertions.assertEquals(memoized.apply(INPUT), OUTPUT);
        verify(expensiveOperation, times(1)).compute(INPUT);
    }

    @Test
    public void memoizeBiFunc() {
        ExpensiveBiOperation expensiveOperation = mock(ExpensiveBiOperation.class);
        when(expensiveOperation.compute(INPUT, INPUT2)).thenReturn(OUTPUT);
        BiFunction<String, String, String> memoized = Memoizer.memoize(expensiveOperation::compute);
        Assertions.assertEquals(memoized.apply(INPUT, INPUT2), OUTPUT);
        Assertions.assertEquals(memoized.apply(INPUT, INPUT2), OUTPUT);
        verify(expensiveOperation, times(1)).compute(INPUT, INPUT2);
    }

    @Test
    public void memoizeFuncWithMap() {
        Map<String, String> map = new ConcurrentHashMap<>();
        ExpensiveOperation expensiveOperation = mock(ExpensiveOperation.class);
        when(expensiveOperation.compute(INPUT)).thenReturn(OUTPUT);
        Function<String, String> memoized = Memoizer.memoize(map, expensiveOperation::compute);
        Assertions.assertEquals(memoized.apply(INPUT), OUTPUT);
        Assertions.assertEquals(memoized.apply(INPUT), OUTPUT);
        verify(expensiveOperation, times(1)).compute(INPUT);
        Assertions.assertTrue(map.size() == 1);
    }

    @Test
    public void memoizeBiFuncWithMap() {
        Map<Tuple2<String, String>, String> map = new ConcurrentHashMap<>();
        ExpensiveBiOperation expensiveOperation = mock(ExpensiveBiOperation.class);
        when(expensiveOperation.compute(INPUT, INPUT2)).thenReturn(OUTPUT);
        BiFunction<String, String, String> memoized = Memoizer.memoize(map, expensiveOperation::compute);
        Assertions.assertEquals(memoized.apply(INPUT, INPUT2), OUTPUT);
        Assertions.assertEquals(memoized.apply(INPUT, INPUT2), OUTPUT);
        verify(expensiveOperation, times(1)).compute(INPUT, INPUT2);
        Assertions.assertTrue(map.size() == 1);
    }

    interface ExpensiveOperation {

        String compute(String input);
    }

    interface ExpensiveBiOperation {

        String compute(String input, String input2);
    }
}
