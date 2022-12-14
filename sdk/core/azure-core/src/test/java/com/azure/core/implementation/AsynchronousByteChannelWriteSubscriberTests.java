// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation;

import com.azure.core.util.io.IOUtils;
import com.azure.core.util.mocking.MockAsynchronousFileChannel;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscription;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests {@link AsynchronousByteChannelWriteSubscriber}.
 */
public class AsynchronousByteChannelWriteSubscriberTests {
    @SuppressWarnings("unchecked")
    @Test
    public void multipleSubscriptionsCancelsLaterSubscriptions() {
        AsynchronousFileChannel channel = new MockAsynchronousFileChannel() {
            @Override
            public <A> void write(ByteBuffer src,
                                  long position,
                                  A attachment,
                                  CompletionHandler<Integer, ? super A> handler) {
                int remaining = src.remaining();
                src.position(src.position() + remaining);
                handler.completed(remaining, attachment);
            }
        };

        AsynchronousByteChannelWriteSubscriber fileWriteSubscriber = new AsynchronousByteChannelWriteSubscriber(IOUtils
            .toAsynchronousByteChannel(channel, 0), null);

        CallTrackingSubscription subscription1 = new CallTrackingSubscription();
        CallTrackingSubscription subscription2 = new CallTrackingSubscription();

        fileWriteSubscriber.onSubscribe(subscription1);
        fileWriteSubscriber.onSubscribe(subscription2);

        assertEquals(1, subscription1.requestOneCalls.get());
        assertEquals(0, subscription1.cancelCalls.get());

        assertEquals(0, subscription2.requestOneCalls.get());
        assertEquals(1, subscription2.cancelCalls.get());
    }

    private static final class CallTrackingSubscription implements Subscription {
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
