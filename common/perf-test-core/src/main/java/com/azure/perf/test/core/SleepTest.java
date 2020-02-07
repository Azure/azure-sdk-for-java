// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.perf.test.core;

import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;


class SleepTest extends PerfStressTest<PerfStressOptions> {
    private static final ClientLogger LOGGER = new ClientLogger(SleepTest.class);
    private static final AtomicInteger INSTANCE_COUNT = new AtomicInteger();
    private final int secondsPerOperation;


    public SleepTest(PerfStressOptions options) {
        super(options);

        int instanceCount = SleepTest.INSTANCE_COUNT.incrementAndGet();
        secondsPerOperation = pow(2, instanceCount);
    }

    private static int pow(int value, int exponent) {
        int power = 1;
        for (int i = 0; i < exponent; i++) {
            power *= value;
        }
        return power;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(secondsPerOperation * 1000);
        } catch (InterruptedException e) {
            throw LOGGER.logExceptionAsError(new RuntimeException(e));
        }
    }

    @Override
    public Mono<Void> runAsync() {
        return Mono.delay(Duration.ofSeconds(secondsPerOperation)).then();
    }
}
