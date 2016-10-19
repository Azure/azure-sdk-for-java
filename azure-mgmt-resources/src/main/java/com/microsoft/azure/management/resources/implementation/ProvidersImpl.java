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
        return wrapModel(client.unregister(resourceProviderNamespace));
    }

    @Override
    public Provider register(String resourceProviderNamespace) {
        return wrapModel(client.register(resourceProviderNamespace));
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
}
