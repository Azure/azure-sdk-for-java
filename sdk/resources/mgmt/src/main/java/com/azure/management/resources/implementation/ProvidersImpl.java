/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.resources.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.management.resources.Provider;
import com.azure.management.resources.Providers;
import com.azure.management.resources.fluentcore.arm.collection.implementation.ReadableWrappersImpl;
import com.azure.management.resources.models.ProviderInner;
import com.azure.management.resources.models.ProvidersInner;
import reactor.core.publisher.Mono;

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
    public PagedIterable<Provider> list() {
        return wrapList(client.list());
    }

    @Override
    public Provider unregister(String resourceProviderNamespace) {
        return this.unregisterAsync(resourceProviderNamespace).block();
    }

    @Override
    public Mono<Provider> unregisterAsync(String resourceProviderNamespace) {
        return client.unregisterAsync(resourceProviderNamespace).map(providerInner -> wrapModel(providerInner));
    }

    @Override
    public Provider register(String resourceProviderNamespace) {
        return this.registerAsync(resourceProviderNamespace).block();
    }

    @Override
    public Mono<Provider> registerAsync(String resourceProviderNamespace) {
        return client.registerAsync(resourceProviderNamespace).map(providerInner -> wrapModel(providerInner));
    }

    @Override
    public Mono<Provider> getByNameAsync(String name) {
        return client.getAsync(name).map(providerInner -> wrapModel(providerInner));
    }

    @Override
    public Provider getByName(String resourceProviderNamespace) {
        return wrapModel(client.get(resourceProviderNamespace));
    }

    @Override
    public PagedFlux<Provider> listAsync() {
        return this.client.listAsync().mapPage(inner -> wrapModel(inner));
    }

    @Override
    protected ProviderImpl wrapModel(ProviderInner inner) {
        if (inner == null) {
            return null;
        }
        return new ProviderImpl(inner);
    }
}
