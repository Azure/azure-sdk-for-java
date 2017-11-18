/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.implementation;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.resources.Provider;
import com.microsoft.azure.management.resources.Providers;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.ReadableWrappersImpl;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceFuture;
import rx.Observable;
import rx.functions.Func1;

/**
 * The implementation for {@link Providers}.
 */
final class ProvidersImpl
        extends ReadableWrappersImpl<Provider, ProviderImpl, ProviderInner>
        implements Providers {
    private final ProvidersInner client;

    ProvidersImpl(final ProvidersInner client) {
        this.client = client;
    }

    @Override
    public PagedList<Provider> list() {
        return wrapList(client.list());
    }

    @Override
    public Provider unregister(String resourceProviderNamespace) {
        return this.unregisterAsync(resourceProviderNamespace).toBlocking().last();
    }

    @Override
    public Observable<Provider> unregisterAsync(String resourceProviderNamespace) {
        return client.unregisterAsync(resourceProviderNamespace).map(new Func1<ProviderInner, Provider>() {
            @Override
            public Provider call(ProviderInner providerInner) {
                return wrapModel(providerInner);
            }
        });
    }

    @Override
    public ServiceFuture<Provider> unregisterAsync(String resourceProviderNamespace, ServiceCallback<Provider> callback) {
        return ServiceFuture.fromBody(this.unregisterAsync(resourceProviderNamespace), callback);
    }

    @Override
    public Provider register(String resourceProviderNamespace) {
        return this.registerAsync(resourceProviderNamespace).toBlocking().last();
    }

    @Override
    public Observable<Provider> registerAsync(String resourceProviderNamespace) {
        return client.registerAsync(resourceProviderNamespace).map(new Func1<ProviderInner, Provider>() {
            @Override
            public Provider call(ProviderInner providerInner) {
                return wrapModel(providerInner);
            }
        });
    }

    @Override
    public ServiceFuture<Provider> registerAsync(String resourceProviderNamespace, ServiceCallback<Provider> callback) {
        return ServiceFuture.fromBody(this.registerAsync(resourceProviderNamespace), callback);
    }

    @Override
    public Observable<Provider> getByNameAsync(String name) {
        return client.getAsync(name)
                .map(new Func1<ProviderInner, Provider>() {
                    @Override
                    public Provider call(ProviderInner providerInner) {
                        return wrapModel(providerInner);
                    }
                });
    }

    @Override
    public Provider getByName(String resourceProviderNamespace) {
        return wrapModel(client.get(resourceProviderNamespace));
    }

    @Override
    protected ProviderImpl wrapModel(ProviderInner inner) {
        if (inner == null) {
            return null;
        }
        return new ProviderImpl(inner);
    }

    @Override
    public Observable<Provider> listAsync() {
        return wrapPageAsync(this.client.listAsync());
    }
}
