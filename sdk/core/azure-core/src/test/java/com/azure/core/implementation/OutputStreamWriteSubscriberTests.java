// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.implementation;

import com.azure.core.util.logging.ClientLogger;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscription;
import reactor.core.publisher.MonoSink;

import java.io.IOException;
import java.io.OutputStream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Tests {@link OutputStreamWriteSubscriber}.
 */
public class OutputStreamWriteSubscriberTests {
    @SuppressWarnings("unchecked")
    @Test
    public void multipleSubscriptionsCancelsLaterSubscriptions() throws IOException {
        ClientLogger clientLogger = mock(ClientLogger.class);
        OutputStream stream = mock(OutputStream.class);
        doAnswer(invocation -> null).when(stream).write(any(), anyInt(), anyInt());

        MonoSink<Void> sink = (MonoSink<Void>) mock(MonoSink.class);

        OutputStreamWriteSubscriber subscriber = new OutputStreamWriteSubscriber(sink, stream, clientLogger);
        Subscription subscription1 = mock(Subscription.class);
        Subscription subscription2 = mock(Subscription.class);

        subscriber.onSubscribe(subscription1);
        subscriber.onSubscribe(subscription2);

        verify(subscription1, times(1)).request(1);
        verify(subscription1, never()).cancel();

        verify(subscription2, never()).request(1);
        verify(subscription2, times(1)).cancel();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void errorDuringWritingCancelsSubscription() throws IOException {
        ClientLogger clientLogger = new ClientLogger(OutputStreamWriteSubscriberTests.class);
        OutputStream stream = mock(OutputStream.class);
        MonoSink<Void> sink = (MonoSink<Void>) mock(MonoSink.class);

        OutputStreamWriteSubscriber subscriber = new OutputStreamWriteSubscriber(sink, stream, clientLogger);

        Subscription subscription = mock(Subscription.class);

        subscriber.onSubscribe(subscription);

        IOException ioException = new IOException();
        subscriber.onError(ioException);

        verify(subscription, times(1)).request(1);
        verify(subscription, times(1)).cancel();

        verify(sink, times(1)).error(ioException);
    }
}
