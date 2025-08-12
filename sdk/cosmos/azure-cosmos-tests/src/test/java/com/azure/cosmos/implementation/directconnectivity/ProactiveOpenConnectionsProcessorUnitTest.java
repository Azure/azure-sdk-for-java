// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.directconnectivity;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;


public class ProactiveOpenConnectionsProcessorUnitTest {

    @DataProvider(name = "sinkEmissionHandlingParams")
    public Object[][] sinkEmissionHandlingParams() {
        return new Object[][] {
            { 5, 500, 8, Duration.ofNanos(10), 500 },
            { 50, 500, 8, Duration.ofNanos(10), 500 },
            { 500, 500, 8, Duration.ofNanos(10), 500 },
            { 1000, 500, 8, Duration.ofNanos(10), 500 }
        };
    }

    // this test essentially tests the sinks ability to handle overflow failures
    // so we have a slow consumer configured with some delay and an aggressive producer
    // configure with a high-enough concurrency to push elements to the consumer
    // the slow consumer / sink has a low enough buffer size but should be able to retry
    // on overflow failures w/o much signal loss and no failures
    // NOTE: even if the consumer is too slow, the only repercussion is elements from
    // the producer will be lost but the consumer will not be terminated
    @Test(groups = "unit", dataProvider = "sinkEmissionHandlingParams")
    public void handleOverflowTest(int sinkBufferSize, int elementsSize, int elementsEmissionConcurrency,
                                   Duration backpressureSimulationDelay, int threadSleepTimeInMs)
        throws InterruptedException {

        List<Integer> elements = new ArrayList<>();
        Sinks.Many<Integer> intSink = Sinks.many().multicast().onBackpressureBuffer(sinkBufferSize);
        AtomicInteger recordedSignalsCount = new AtomicInteger(0);

        for (int i = 0; i < elementsSize; i++) {
            elements.add(i);
        }

        intSink
            .asFlux()
            .publishOn(Schedulers.parallel())
            .delayElements(backpressureSimulationDelay)
            .doOnNext(integer -> recordedSignalsCount.incrementAndGet())
            .subscribe();

        Flux
            .fromIterable(elements)
            .publishOn(Schedulers.parallel())
            .parallel(elementsEmissionConcurrency)
            .flatMap(integer -> {
                intSink.emitNext(integer, (signalType, emitResult) -> {
                    if (emitResult.equals(Sinks.EmitResult.FAIL_OVERFLOW)) {
                        return true;
                    }
                    return false;
                });
                return Mono.just(integer);
            })
            .doOnComplete(intSink::tryEmitComplete)
            .subscribe();

        Thread.sleep(threadSleepTimeInMs);

        assertThat(recordedSignalsCount.get()).isEqualTo(elementsSize);
    }
}
