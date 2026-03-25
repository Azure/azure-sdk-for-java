// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.implementation;

import com.azure.messaging.eventhubs.models.PartitionEvent;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.reactivestreams.Subscription;
import reactor.core.publisher.FluxSink;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SynchronousEventSubscriberTest {
    @Test
    @SuppressWarnings("unchecked")
    void timeoutDisposesSubscriberWithoutDedicatedTimerThread() throws InterruptedException {
        final CountDownLatch completed = new CountDownLatch(1);
        final FluxSink<PartitionEvent> emitter = Mockito.mock(FluxSink.class);
        final Subscription subscription = Mockito.mock(Subscription.class);

        when(emitter.isCancelled()).thenReturn(false);
        doAnswer(invocation -> {
            completed.countDown();
            return null;
        }).when(emitter).complete();

        final SynchronousReceiveWork work = new SynchronousReceiveWork(1L, 1, Duration.ofMillis(25), emitter);
        final SynchronousEventSubscriber subscriber = new SynchronousEventSubscriber(work);

        subscriber.onSubscribe(subscription);

        assertTrue(completed.await(5, TimeUnit.SECONDS), "The timeout should complete the work.");
        verify(subscription, atLeastOnce()).request(1L);
        verify(subscription, atLeastOnce()).cancel();
    }
}
