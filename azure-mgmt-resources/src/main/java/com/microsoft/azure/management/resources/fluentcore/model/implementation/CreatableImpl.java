/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.fluentcore.model.implementation;

import com.microsoft.rest.ServiceCall;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceResponse;
import rx.Observable;
import rx.functions.Func1;

/**
 * The base class for all creatable resource.
 *
 * @param <FluentModelT> the fluent model type representing the creatable resource
 * @param <InnerModelT> the model inner type that the fluent model type wraps
 * @param <FluentModelImplT> the fluent model implementation type
 */
public abstract class CreatableImpl<FluentModelT, InnerModelT, FluentModelImplT extends IndexableRefreshableWrapperImpl<FluentModelT, InnerModelT>>
        extends IndexableRefreshableWrapperImpl<FluentModelT, InnerModelT> {
    /**
     * The name of the creatable resource.
     */
    private String name;

    protected CreatableImpl(String name, InnerModelT innerObject) {
        super(innerObject);
        this.name = name;
    }

    /**
     * @return the name of the creatable resource.
     */
    public String name() {
        return this.name;
    }

    /**
     * Creates the resource asynchronously and return a observable to track the
     * asynchronous operation.
     *
     * @return an observable stream that emits the resource when it is created
     */
    abstract Observable<FluentModelT> createAsync();

    /**
     * Default implementation to create the resource asynchronously.
     *
     * @param callback the callback to handle success and failure
     * @return a handle to cancel the request
     */
    public ServiceCall<FluentModelT> createAsync(final ServiceCallback<FluentModelT> callback) {
        return observableToFuture(createAsync(), callback);
    }

    /**
     * Default implementation of create().
     *
     * @return the created resource
     */
    @SuppressWarnings("unchecked")
    public FluentModelT create() {
        return createAsync().toBlocking().single();
    }

    @SuppressWarnings("unchecked")
    protected Func1<InnerModelT, FluentModelT> innerToFluentMap(final FluentModelImplT fluentModelImplT) {
        return new Func1<InnerModelT, FluentModelT>() {
            @Override
            public FluentModelT call(InnerModelT innerModelT) {
                fluentModelImplT.setInner(innerModelT);
                return (FluentModelT) fluentModelImplT;
            }
        };
    }

    protected ServiceCall<FluentModelT> observableToFuture(Observable<FluentModelT> observable, final ServiceCallback<FluentModelT> callback) {
        return ServiceCall.create(
                observable.map(new Func1<FluentModelT, ServiceResponse<FluentModelT>>() {
                    @Override
                    public ServiceResponse<FluentModelT> call(FluentModelT fluentModelT) {
                        // TODO: When https://github.com/Azure/azure-sdk-for-java/issues/1029 is done, this map (and this method) can be removed
                        return new ServiceResponse<>(fluentModelT, null);
                    }
                }), callback
        );
    }
}
