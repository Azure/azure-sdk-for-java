/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure;

import com.microsoft.rest.ServiceFuture;
import com.microsoft.rest.ServiceResponse;
import com.microsoft.rest.ServiceResponseWithHeaders;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;

import java.util.List;

/**
 * An instance of this class provides access to the underlying REST call invocation.
 * This class wraps around the Retrofit Call object and allows updates to it in the
 * progress of a long running operation or a paging operation.
 *
 * @param <T> the type of the returning object
 */
public final class AzureServiceFuture<T> extends ServiceFuture<T> {
    private AzureServiceFuture() {
    }

    /**
     * Creates a ServiceCall from a paging operation.
     *
     * @param first the observable to the first page
     * @param next the observable to poll subsequent pages
     * @param callback the client-side callback
     * @param <E> the element type
     * @return the future based ServiceCall
     */
    public static <E> ServiceFuture<List<E>> fromPageResponse(Observable<ServiceResponse<Page<E>>> first, final Func1<String, Observable<ServiceResponse<Page<E>>>> next, final ListOperationCallback<E> callback) {
        final AzureServiceFuture<List<E>> serviceCall = new AzureServiceFuture<>();
        final PagingSubscriber<E> subscriber = new PagingSubscriber<>(serviceCall, next, callback);
        serviceCall.setSubscription(first
            .single()
            .subscribe(subscriber));
        return serviceCall;
    }

    /**
     * Creates a ServiceCall from a paging operation that returns a header response.
     *
     * @param first the observable to the first page
     * @param next the observable to poll subsequent pages
     * @param callback the client-side callback
     * @param <E> the element type
     * @param <V> the header object type
     * @return the future based ServiceCall
     */
    public static <E, V> ServiceFuture<List<E>> fromHeaderPageResponse(Observable<ServiceResponseWithHeaders<Page<E>, V>> first, final Func1<String, Observable<ServiceResponseWithHeaders<Page<E>, V>>> next, final ListOperationCallback<E> callback) {
        final AzureServiceFuture<List<E>> serviceCall = new AzureServiceFuture<>();
        final PagingSubscriber<E> subscriber = new PagingSubscriber<>(serviceCall, new Func1<String, Observable<ServiceResponse<Page<E>>>>() {
            @Override
            public Observable<ServiceResponse<Page<E>>> call(String s) {
                return next.call(s)
                        .map(new Func1<ServiceResponseWithHeaders<Page<E>, V>, ServiceResponse<Page<E>>>() {
                            @Override
                            public ServiceResponse<Page<E>> call(ServiceResponseWithHeaders<Page<E>, V> pageVServiceResponseWithHeaders) {
                                return pageVServiceResponseWithHeaders;
                            }
                        });
            }
        }, callback);
        serviceCall.setSubscription(first
                .single()
                .subscribe(subscriber));
        return serviceCall;
    }

    /**
     * The subscriber that handles user callback and automatically subscribes to the next page.
     *
     * @param <E> the element type
     */
    private static final class PagingSubscriber<E> extends Subscriber<ServiceResponse<Page<E>>> {
        private AzureServiceFuture<List<E>> serviceCall;
        private Func1<String, Observable<ServiceResponse<Page<E>>>> next;
        private ListOperationCallback<E> callback;
        private ServiceResponse<Page<E>> lastResponse;

        PagingSubscriber(final AzureServiceFuture<List<E>> serviceCall, final Func1<String, Observable<ServiceResponse<Page<E>>>> next, final ListOperationCallback<E> callback) {
            this.serviceCall = serviceCall;
            this.next = next;
            this.callback = callback;
        }

        @Override
        public void onCompleted() {
            // do nothing
        }

        @Override
        public void onError(Throwable e) {
            serviceCall.setException(e);
            if (callback != null) {
                callback.failure(e);
            }
        }

        @Override
        public void onNext(ServiceResponse<Page<E>> serviceResponse) {
            lastResponse = serviceResponse;
            ListOperationCallback.PagingBehavior behavior = ListOperationCallback.PagingBehavior.CONTINUE;
            if (callback != null) {
                behavior = callback.progress(serviceResponse.body().items());
                if (behavior == ListOperationCallback.PagingBehavior.STOP || serviceResponse.body().nextPageLink() == null) {
                    callback.success();
                }
            }
            if (behavior == ListOperationCallback.PagingBehavior.STOP || serviceResponse.body().nextPageLink() == null) {
                serviceCall.set(lastResponse.body().items());
            } else {
                serviceCall.setSubscription(next.call(serviceResponse.body().nextPageLink()).single().subscribe(this));
            }
        }
    }
}
