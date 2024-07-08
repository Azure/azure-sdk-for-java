// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.implementation;

import io.clientcore.core.util.ClientLogger;
import com.azure.core.v2.util.mocking.MockMonoSink;
import com.azure.core.v2.util.mocking.MockOutputStream;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscription;
import reactor.core.publisher.MonoSink;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests {@link OutputStreamWriteSubscriber}.
 */
public class OutputStreamWriteSubscriberTests {
    private static final ClientLogger LOGGER = new ClientLogger(OutputStreamWriteSubscriberTests.class);

    @Test
    public void multipleSubscriptionsCancelsLaterSubscriptions() {
        OutputStream stream = new MockOutputStream() {
            @Override
            public void write(byte[] b, int off, int len) {
            }
        };

        OutputStreamWriteSubscriber subscriber = new OutputStreamWriteSubscriber(null, stream, LOGGER);
        CountingSubscription subscription1 = new CountingSubscription();
        CountingSubscription subscription2 = new CountingSubscription();

        subscriber.onSubscribe(subscription1);
        subscriber.onSubscribe(subscription2);

        assertEquals(1, subscription1.requestOneCalls.get());
        assertEquals(0, subscription1.cancelCalls.get());

        assertEquals(0, subscription2.requestOneCalls.get());
        assertEquals(1, subscription2.cancelCalls.get());
    }

    @Test
    public void errorDuringWritingCancelsSubscription() {
        OutputStream stream = new MockOutputStream();
        CountingSubscription subscription = new CountingSubscription();

        AtomicReference<Throwable> error = new AtomicReference<>();
        MonoSink<Void> sink = new MockMonoSink<Void>() {
            @Override
            public void error(Throwable e) {
                error.set(e);
            }
        };

        OutputStreamWriteSubscriber subscriber = new OutputStreamWriteSubscriber(sink, stream, LOGGER);
        subscriber.onSubscribe(subscription);

        IOException ioException = new IOException();
        subscriber.onError(ioException);

        assertEquals(1, subscription.requestOneCalls.get());
        assertEquals(1, subscription.cancelCalls.get());
        assertEquals(ioException, error.get());
    }

    private static final class CountingSubscription implements Subscription {
        private final AtomicInteger requestOneCalls = new AtomicInteger();
        private final AtomicInteger cancelCalls = new AtomicInteger();

        @Override
        public void request(long n) {
            if (n == 1) {
                requestOneCalls.incrementAndGet();
            }
        }

        @Override
        public void cancel() {
            cancelCalls.incrementAndGet();
        }
    }
}
