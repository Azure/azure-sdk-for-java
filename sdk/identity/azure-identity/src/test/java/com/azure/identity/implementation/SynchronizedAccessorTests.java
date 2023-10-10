// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation;

import org.junit.Assert;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.Random;


public class SynchronizedAccessorTests {

    @Test
    public void testSyncAccess() throws Exception {
        // setup
        int counter = 0;
        Random random = new Random();

        SynchronizedAccessor<Integer> synchronizedAccessor = new SynchronizedAccessor<>(() -> Mono.just(random.nextInt()));

        List<Integer> values = Flux.fromIterable(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10))
                .flatMap(i -> synchronizedAccessor.getValue(), 16)
                .collectList()
                .block();

        //test
        Integer firstVal = values.get(0);
        for (int z = 1; z < values.size(); z++) {
            Assert.assertEquals(firstVal, values.get(z));
        }
    }
}
