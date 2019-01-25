/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v3;

import reactor.core.Disposable;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletableFuture;

/**
 * An instance of this class provides access to the underlying REST call invocation.
 * This class wraps around the Retrofit Call object and allows updates to it in the
 * progress of a long running operation or a paging operation.
 *
 * @param <T> the type of the returning object
 */
public class ServiceFuture<T> extends CompletableFuture<T> {
    /**
     * The Retrofit method invocation.
     */
    private Disposable subscription;

    protected ServiceFuture() {
    }

    /**
     * Creates a ServiceCall from a Mono object and a callback.
     *
     * @param flux the flux to create from
     * @param callback the callback to call when events happen
     * @param <T> the type of the response
     * @return the created ServiceCall
     */
    public static <T> ServiceFuture<T> fromBody(final Mono<T> flux, final ServiceCallback<T> callback) {
        final ServiceFuture<T> serviceFuture = new ServiceFuture<>();
        serviceFuture.subscription = flux.subscribe(result -> {
            if (callback != null) {
                callback.success(result);
            }
            serviceFuture.complete(result);
        }, throwable -> {
            if (callback != null) {
                callback.failure(throwable);
            }
            serviceFuture.completeExceptionally(throwable);
        });
        return serviceFuture;
    }

    /**
     * @return the current Rx subscription associated with the ServiceCall.
     */
    public Disposable getSubscription() {
        return subscription;
    }

    protected void setSubscription(Disposable subscription) {
        this.subscription = subscription;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        subscription.dispose();
        return super.cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean isCancelled() {
        return subscription.isDisposed();
    }
}
