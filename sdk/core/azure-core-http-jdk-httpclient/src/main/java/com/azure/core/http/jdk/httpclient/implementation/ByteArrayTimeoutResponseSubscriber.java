// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.http.jdk.httpclient.implementation;

import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Flow;

/**
 * Implementation of {@link HttpResponse.BodySubscriber} that accumulates the response body into a byte array while
 * tracking a timeout for each value emitted by the subscription.
 * <p>
 * This differs from {@link HttpResponse.BodySubscribers#ofByteArray()} in that it tracks a timeout for each value
 * emitted by the subscription. This is needed to offer better reliability when reading the response in cases where the
 * server is either slow to respond or the connection has dropped without a signal from the server.
 */
public final class ByteArrayTimeoutResponseSubscriber implements HttpResponse.BodySubscriber<byte[]> {
    private final CompletableFuture<byte[]> future = new CompletableFuture<>();
    private final List<ByteBuffer> received = new ArrayList<>();

    private final long readTimeout;
    private TimerTask currentTimeout;

    private volatile Flow.Subscription subscription;

    /**
     * Creates a response body subscriber that accumulates the response body into a byte array while tracking a timeout
     * for each value emitted by the subscription.
     *
     * @param readTimeout The timeout for reading each value emitted by the subscription.
     */
    public ByteArrayTimeoutResponseSubscriber(long readTimeout) {
        this.readTimeout = readTimeout;
    }

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        // Only one subscription is valid.
        if (this.subscription != null) {
            subscription.cancel();
            return;
        }

        this.subscription = subscription;
        currentTimeout = createTimeout();
        subscription.request(1);
    }

    @Override
    public void onNext(List<ByteBuffer> item) {
        // From documentation within ResponseSubscribers it is mentioned that the ByteBuffers passed here won't be used
        // anywhere else. So, it is safe to store them without copying.
        currentTimeout.cancel();
        received.addAll(item);
        currentTimeout = createTimeout();
        subscription.request(1);
    }

    @Override
    public void onError(Throwable throwable) {
        currentTimeout.cancel();
        received.clear();
        future.completeExceptionally(throwable);
    }

    @Override
    public void onComplete() {
        currentTimeout.cancel();
        int size = JdkHttpUtils.getSizeOfBuffers(received);
        byte[] result = new byte[size];
        int offset = 0;
        for (ByteBuffer buffer : received) {
            int length = buffer.remaining();
            buffer.get(result, offset, length);
            offset += length;
        }
        future.complete(result);
    }

    private TimerTask createTimeout() {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                // Complete the future first. Cancelling the subscription causes an error to be emitted about the
                // subscription being cancelled which we don't want to propagate as we are explicitly doing it.
                future.completeExceptionally(new HttpTimeoutException("Timeout reading response body."));
                subscription.cancel();
            }
        };

        JdkHttpUtils.scheduleTimeoutTask(task, readTimeout);
        return task;
    }

    @Override
    public CompletionStage<byte[]> getBody() {
        return future;
    }
}
