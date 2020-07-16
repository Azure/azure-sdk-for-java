// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.performance.utils;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class FunctionUtils {
    /**
     * Run <code>supplier</code> for <code>times</code>.
     * @param times How many times the <code>supplier</code> will run.
     * @param supplier Supplier to run.
     * @param <T>
     * @return time cost in milli-seconds of running the <code>supplier</code> for <code>times</code>
     */
    public static <T> long getSupplier(int times, Supplier<T> supplier) {
        final long startTime = System.currentTimeMillis();
        for (int i = 0; i < times; i++) {
            supplier.get();
        }
        final long endTime = System.currentTimeMillis();

        return endTime - startTime;
    }


    /**
     * Run function for each element in the <code>inputList</code>
     * @param inputList list of input data to be processed by <code>function</code>
     * @param function @see java.util.function.Function to process data in the <code>inputList</code>
     * @param <T> the type of the input to the function
     * @param <R> the type of the result of the function
     * @return time cost in milli-seconds of processing the whole <code>inputList</code> by <code>function</code>
     */
    public static <T, R> long applyInputListFunc(List<T> inputList, Function<T, R> function) {
        final long startTime = System.currentTimeMillis();
        inputList.forEach(function::apply);
        final long endTime = System.currentTimeMillis();

        return endTime - startTime;
    }

    /**
     *
     * @param times How many times the <code>function</code> will apply
     * @param argument argument for the <code>function</code>
     * @param function @see java.util.function.Function to process the <code>argument</code>
     * @param <T> the type of the input to the function
     * @param <R> the type of the result of the function
     * @return time cost in milli-seconds of running the <code>function</code> for <code>times</code>
     */
    public static <T, R> long runFunctionForTimes(int times, T argument, Function<T, R> function) {
        final long startTime = System.currentTimeMillis();
        for (int i = 0; i < times; i++) {
            function.apply(argument);
        }
        final long endTime = System.currentTimeMillis();

        return endTime - startTime;
    }

    /**
     * Run consumer for each element in the <code>inputList</code>
     * @param inputList list of input data to be processed by <code>consumer</code>
     * @param consumer @see java.util.function.Consumer to process data in the <code>inputList</code>
     * @param <T> the type of the input to the consumer
     * @return time cost in milli-seconds of processing the whole <code>inputList</code> by <code>consumer</code>
     */
    public static <T> long acceptInputListFunc(List<T> inputList, Consumer<T> consumer) {
        final long startTime = System.currentTimeMillis();
        inputList.forEach(consumer::accept);
        final long endTime = System.currentTimeMillis();

        return endTime - startTime;
    }

    /**
     * Run consumer with given argument for given times.
     * @param times How many times the <code>consumer</code> will run
     * @param argument argument for the <code>consumer</code>
     * @param consumer @see java.util.function.Consumer to process the input
     * @param <T> the type of the input to the consumer
     * @return time cost in milli-seconds of running the <code>consumer</code> for <code>times</code>
     */
    public static <T> long runConsumerForTimes(int times, T argument, Consumer<T> consumer) {
        final long startTime = System.currentTimeMillis();
        for (int i = 0; i < times; i++) {
            consumer.accept(argument);
        }
        final long endTime = System.currentTimeMillis();

        return endTime - startTime;
    }
}
