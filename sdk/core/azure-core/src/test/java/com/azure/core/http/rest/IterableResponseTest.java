// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.rest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import reactor.core.publisher.Flux;


public class IterableResponseTest {

    @Rule
    public TestName testName = new TestName();

    @Before
    public void setup() {
        System.out.println("-------------- Running " + testName.getMethodName() + " -----------------------------");
    }

    /*Ensure that if we call stream multiple times, it always returns same values and they are same as original list of values.*/
    @Test
    public void testIterableResponseStreamFromStart()  {
        IterableResponse<Integer> iterableResponse = getIntegerIterableResponse(2, 5);
        Assert.assertEquals(iterableResponse.stream().collect(Collectors.toList()).size(), iterableResponse.stream().collect(Collectors.toList()).size());

        // ensure original list of values are same after calling iterator()
        List<Integer> originalIntegerList =  Arrays.asList(2, 3, 4, 5, 6);
        iterableResponse.stream().forEach(number -> Assert.assertTrue(originalIntegerList.contains(number)));
    }

    /*Ensure that if we call iterator multiple times, it always returns same values and they are same as original list of values.*/
    @Test
    public void testIterableResponseIteratorFromStart()  {
        IterableResponse<Integer> iterableResponse = getIntegerIterableResponse(2, 5);
        List<Integer> actualNumberValues1 = new ArrayList<>();
        List<Integer> actualNumberValues2 = new ArrayList<>();
        iterableResponse.iterator().forEachRemaining(number -> actualNumberValues1.add(number));
        iterableResponse.iterator().forEachRemaining(number -> actualNumberValues2.add(number));
        Assert.assertArrayEquals(actualNumberValues1.toArray(), actualNumberValues2.toArray());

        // ensure original list of values are same after calling iterator()
        List<Integer> originalIntegerList =  Arrays.asList(2, 3, 4, 5, 6);
        iterableResponse.iterator().forEachRemaining(number -> Assert.assertTrue(originalIntegerList.contains(number)));
    }

    private IterableResponse<Integer> getIntegerIterableResponse(int startNumber, int noOfValues) {
        Flux<Integer> integerFlux = Flux.range(startNumber, noOfValues);
        return new IterableResponse<>(integerFlux);
    }
}
