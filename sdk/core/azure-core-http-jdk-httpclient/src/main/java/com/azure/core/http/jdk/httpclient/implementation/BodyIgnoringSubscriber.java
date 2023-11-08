// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.jdk.httpclient.implementation;

import com.azure.core.http.HttpClient;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.logging.LogLevel;

import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Flow;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Implementation of a {@link HttpResponse.BodySubscriber} that ignores the response body.
 * <p>
 * This is used when the {@link HttpClient} is told to ignore the response body, used when the returned body value is
 * {@code void} or {@code Void}.
 * <p>
 * This will log a warning message if a response body is received to indicate that there was a bug either in determining
 * that the response body should be ignored, the Swagger indicated no response body would be received but was, or that
 * the server sent a response body when it shouldn't.
 */
public final class BodyIgnoringSubscriber implements HttpResponse.BodySubscriber<Void> {
    private final CompletableFuture<Void> completableFuture;
    private final ClientLogger logger;
    private final AtomicBoolean subscribed = new AtomicBoolean();

    public BodyIgnoringSubscriber(ClientLogger logger) {
        this.completableFuture = new CompletableFuture<>();
        this.logger = logger;
    }

    @Override
    public CompletionStage<Void> getBody() {
        return completableFuture;
    }

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        if (!subscribed.compareAndSet(false, true)) {
            // Only can have one subscription.
            subscription.cancel();
        } else {
            subscription.request(Long.MAX_VALUE);
        }
    }

    @Override
    public void onNext(List<ByteBuffer> item) {
        logger.log(LogLevel.WARNING, () -> "Received HTTP response body when one wasn't expected. "
            + "Response body will be ignored as directed.");
    }

    @Override
    public void onError(Throwable throwable) {
        completableFuture.completeExceptionally(throwable);
    }

    @Override
    public void onComplete() {
        completableFuture.complete(null);
    }
}
