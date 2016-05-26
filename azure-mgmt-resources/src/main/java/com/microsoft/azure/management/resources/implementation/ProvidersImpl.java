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
import com.microsoft.azure.management.resources.fluentcore.utils.PagedListConverter;
import com.microsoft.azure.management.resources.implementation.api.ProviderInner;
import com.microsoft.azure.management.resources.implementation.api.ProvidersInner;

import java.io.IOException;

/**
 * The implementation for Providers and its parent interfaces.
 */
final class ProvidersImpl
        implements Providers {
    private final ProvidersInner client;

    ProvidersImpl(final ProvidersInner client) {
        this.client = client;
    }

    @Override
    public PagedList<Provider> list() throws CloudException, IOException {
        PagedListConverter<ProviderInner, Provider> converter = new PagedListConverter<ProviderInner, Provider>() {
            @Override
            public Provider typeConvert(ProviderInner providerInner) {
                return new ProviderImpl(providerInner);
            }
        };
        return converter.convert(client.list().getBody());
    }

    @Override
    public Provider unregister(String resourceProviderNamespace) throws CloudException, IOException {
        return new ProviderImpl(client.unregister(resourceProviderNamespace).getBody());
    }

    @Override
    public Provider register(String resourceProviderNamespace) throws CloudException, IOException {
        return new ProviderImpl(client.register(resourceProviderNamespace).getBody());
    }

    @Override
    public Provider get(String resourceProviderNamespace) throws CloudException, IOException {
        return new ProviderImpl(client.get(resourceProviderNamespace).getBody());
    }
}
