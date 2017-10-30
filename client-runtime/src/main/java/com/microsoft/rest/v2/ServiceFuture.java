/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2;

import com.google.common.util.concurrent.AbstractFuture;
import rx.Single;
import rx.Completable;
import rx.Subscription;
import rx.functions.Action0;
import rx.functions.Action1;

/**
 * An instance of this class provides access to the underlying REST call invocation.
 * This class wraps around the Retrofit Call object and allows updates to it in the
 * progress of a long running operation or a paging operation.
 *
 * @param <T> the type of the returning object
 */
public class ServiceFuture<T> extends AbstractFuture<T> {
    /**
     * The Retrofit method invocation.
     */
    private Subscription subscription;

    protected ServiceFuture() {
    }

    /**
     * Creates a ServiceCall from a Single object and a callback.
     *
     * @param single the single to create from
     * @param callback the callback to call when events happen
     * @param <T> the type of the response
     * @return the created ServiceCall
     */
    public static <T> ServiceFuture<T> fromBody(final Single<T> single, final ServiceCallback<T> callback) {
        final ServiceFuture<T> serviceFuture = new ServiceFuture<>();
        serviceFuture.subscription = single
                .subscribe(new Action1<T>() {
                    @Override
                    public void call(T t) {
                        if (callback != null) {
                            callback.success(t);
                        }
                        serviceFuture.set(t);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        if (callback != null) {
                            callback.failure(throwable);
                        }
                        serviceFuture.setException(throwable);
                    }
                });
        return serviceFuture;
    }

    /**
     * Creates a ServiceFuture from an Completable object and a callback.
     *
     * @param completable the completable to create from
     * @param callback the callback to call when event happen
     * @return the created ServiceFuture
     */
    public static ServiceFuture<Void> fromBody(final Completable completable, final ServiceCallback<Void> callback) {
        final ServiceFuture<Void> serviceFuture = new ServiceFuture<>();
        completable.subscribe(new Action0() {
            Void value = null;
            @Override
            public void call() {
                if (callback != null) {
                    callback.success(value);
                }
                serviceFuture.set(value);
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                if (callback != null) {
                    callback.failure(throwable);
                }
                serviceFuture.setException(throwable);
            }
        });
        return serviceFuture;
    };
    
    /**
     * @return the current Rx subscription associated with the ServiceCall.
     */
    public Subscription getSubscription() {
        return subscription;
    }

    protected void setSubscription(Subscription subscription) {
        this.subscription = subscription;
    }

    /**
     * Invoke this method to report completed, allowing
     * {@link AbstractFuture#get()} to be unblocked.
     *
     * @param result the service response returned.
     * @return true if successfully reported; false otherwise.
     */
    public boolean success(T result) {
        return set(result);
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        subscription.unsubscribe();
        return super.cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean isCancelled() {
        return subscription.isUnsubscribed();
    }
}
