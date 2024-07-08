// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.rest;

import com.azure.core.v2.util.IterableStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class IterableResponseTest {
    /**
     * Ensure that if we call stream multiple times, it always returns same values and they are same as original list of
     * values.
     */
    @Test
    public void testIterableResponseStreamFromStart() {
        IterableStream<Integer> iterableResponse = getIntegerIterableResponse(2, 5);
        Assertions.assertEquals(iterableResponse.stream().count(), iterableResponse.stream().count());

        // ensure original list of values are same after calling iterator()
        List<Integer> originalIntegerList = Arrays.asList(2, 3, 4, 5, 6);
        iterableResponse.stream().forEach(number -> assertTrue(originalIntegerList.contains(number)));
    }

    /**
     * Ensure that if we call iterator multiple times, it always returns same values and they are same as original list
     * of values.
     */
    @Test
    public void testIterableResponseIteratorFromStart() {
        IterableStream<Integer> iterableResponse = getIntegerIterableResponse(2, 5);
        List<Integer> actualNumberValues1 = new ArrayList<>();
        List<Integer> actualNumberValues2 = new ArrayList<>();
        iterableResponse.iterator().forEachRemaining(actualNumberValues1::add);
        iterableResponse.iterator().forEachRemaining(actualNumberValues2::add);
        Assertions.assertArrayEquals(actualNumberValues1.toArray(), actualNumberValues2.toArray());

        // ensure original list of values are same after calling iterator()
        List<Integer> originalIntegerList = Arrays.asList(2, 3, 4, 5, 6);
        iterableResponse.iterator().forEachRemaining(number -> assertTrue(originalIntegerList.contains(number)));
    }

    private IterableStream<Integer> getIntegerIterableResponse(int startNumber, int noOfValues) {
        Flux<Integer> integerFlux = Flux.range(startNumber, noOfValues);
        return new IterableStream<>(integerFlux);
    }
}
