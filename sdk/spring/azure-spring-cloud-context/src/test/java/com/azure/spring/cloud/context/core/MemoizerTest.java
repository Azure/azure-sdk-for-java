// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.context.core;

import static org.mockito.Mockito.*;

import com.azure.spring.cloud.context.core.util.Memoizer;
import com.azure.spring.cloud.context.core.util.Tuple;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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
        Map<Tuple<String, String>, String> map = new ConcurrentHashMap<>();
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
