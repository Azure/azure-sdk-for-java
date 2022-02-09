// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation;

import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscription;
import reactor.core.publisher.MonoSink;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Tests {@link FileWriteSubscriber}.
 */
public class FileWriteSubscriberTests {
    @SuppressWarnings("unchecked")
    @Test
    public void multipleSubscriptionsCancelsLaterSubscriptions() {
        AsynchronousFileChannel channel = mock(AsynchronousFileChannel.class);
        doAnswer(invocation -> {
            ByteBuffer stream = invocation.getArgument(0);
            CompletionHandler<Integer, ByteBuffer> handler = invocation.getArgument(3);

            int remaining = stream.remaining();
            stream.position(stream.limit());
            handler.completed(remaining, stream);

            return null;
        }).when(channel).write(any(), anyLong(), any(), any());

        MonoSink<Void> sink = (MonoSink<Void>) mock(MonoSink.class);

        FileWriteSubscriber fileWriteSubscriber = new FileWriteSubscriber(channel, 0, sink);
        Subscription subscription1 = mock(Subscription.class);
        Subscription subscription2 = mock(Subscription.class);

        fileWriteSubscriber.onSubscribe(subscription1);
        fileWriteSubscriber.onSubscribe(subscription2);

        verify(subscription1, times(1)).request(1);
        verify(subscription1, never()).cancel();

        verify(subscription2, never()).request(1);
        verify(subscription2, times(1)).cancel();
    }
}
