// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.implementation;

import org.junit.jupiter.api.Test;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.LongStream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link ReactorShim}.
 */
public class ReactorShimTest {

    /**
     * Test to validate the {@link ReactorShim#windowTimeout(Flux, int, Duration)} facade honor backpressure.
     * <p>
     * Today the ReactorShim (type local to Event Hubs SDK) is always guaranteed to use Reactor 3.4.19 or above,
     * which has backpressure aware windowTimeout. In the future, if the ReactorShim moves to azure-core, and if
     * we decide to have a CI pipeline for the lower Reactor version (below 3.4.19), then this test needs to be
     * adjusted to execute conditionally (i.e. run only for Reactor version >= 3.4.19 setup).
     * <p>
     * This is the replica, with additional asserts, of windowTimeout test that Azure Event Hubs crew contributed
     * to Reactor while developing the backpressure aware windowTimeout operator, which got integrated into
     * the Reactor test suite. The test is not using a virtual Timer scheduler (May have to check with the Reactor
     * team to understand if it is safe to do so for this time-involved operator, and not invalidate any cases).
     */
    @Test
    public void windowTimeoutShouldHonorBackpressure() throws InterruptedException {
        // -- The Event Producer
        //    The producer emitting requested events to downstream but with a delay of 250ms between each emission.
        //
        final int eventProduceDelayInMillis = 250;
        Flux<String> producer = Flux.<String>create(sink -> {
            sink.onRequest(request -> {
                if (request != Long.MAX_VALUE) {
                    LongStream.range(0, request)
                        .mapToObj(String::valueOf)
                        .forEach(message -> {
                            try {
                                TimeUnit.MILLISECONDS.sleep(eventProduceDelayInMillis);
                            } catch (InterruptedException e) {
                                // Expected if thread was in sleep while disposing the subscription.
                            }
                            sink.next(message);
                        });
                } else {
                    sink.error(new RuntimeException("No_Backpressure unsupported"));
                }
            });
        }).subscribeOn(Schedulers.boundedElastic());

        // -- The Event Consumer
        //    The consumer using windowTimeout that batches maximum 10 events with a max wait time of 1 second.
        //    Given the Event producer produces at most 4 events per second (due to 250 ms delay between events),
        //    the consumer should receive 3-4 events.
        //
        final List<Integer> batchSizes = new ArrayList<>(300);
        final int maxWindowSize = 10;
        final int eventConsumeDelayInMillis = 0;
        final Scheduler scheduler = Schedulers.newBoundedElastic(10, 10000, "queued-tasks");
        final AtomicReference<Throwable> errorReference = new AtomicReference<>(null);
        final Semaphore isCompleted = new Semaphore(1);
        isCompleted.acquire();

        Disposable subscription = ReactorShim.windowTimeout(producer, maxWindowSize, Duration.ofSeconds(1))
            .concatMap(Flux::collectList, 0)
            .publishOn(scheduler)
            .subscribe(eventBatch -> {
                batchSizes.add(eventBatch.size());
                for (String event : eventBatch) {
                    try {
                        TimeUnit.MILLISECONDS.sleep(eventConsumeDelayInMillis);
                    } catch (InterruptedException e) {
                        // Expected if thread was in sleep while disposing the subscription.
                    }
                }
            }, error -> {
                errorReference.set(error);
                isCompleted.release();
            }, () -> {
                isCompleted.release();
            });

        final Duration durationToPublish = Duration.ofSeconds(20);
        try {
            final boolean acquired = isCompleted.tryAcquire(durationToPublish.toMillis(), TimeUnit.MILLISECONDS);
            if (acquired) {
                if (errorReference.get() != null) {
                    assertFalse(true, "'isCompleted' should have been false because the producer"
                        + " should not be terminating, but terminated with an error:" + errorReference.get());
                } else {
                    assertFalse(true, "'isCompleted' should have been false because the producer"
                        + " should not be terminating, but terminated to completion.");
                }
            }
            for (Integer batchSize : batchSizes) {
                assertTrue(batchSize <= maxWindowSize, "Unexpected batch size " + batchSize);
            }

        } finally {
            subscription.dispose();
        }
    }
}
