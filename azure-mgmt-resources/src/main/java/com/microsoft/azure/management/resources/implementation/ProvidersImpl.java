/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.implementation;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.resources.Provider;
import com.microsoft.azure.management.resources.Providers;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.ReadableWrappersImpl;

import java.io.IOException;

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
    public PagedList<Provider> list() throws CloudException, IOException {
        return wrapList(client.list());
    }

    @Override
    public Provider unregister(String resourceProviderNamespace) throws CloudException, IOException {
        return wrapModel(client.unregister(resourceProviderNamespace));
    }

    @Override
    public Provider register(String resourceProviderNamespace) throws CloudException, IOException {
        return wrapModel(client.register(resourceProviderNamespace));
    }

    @Override
    public Provider getByName(String resourceProviderNamespace) throws CloudException, IOException {
        return wrapModel(client.get(resourceProviderNamespace));
    }

    @Override
    protected ProviderImpl wrapModel(ProviderInner inner) {
        return new ProviderImpl(inner);
    }
}
