// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.resources.Tenants;
import com.azure.resourcemanager.resources.models.TenantIdDescriptionInner;
import com.azure.resourcemanager.resources.models.TenantsInner;

/**
 * Implementation for {@link Tenants}.
 */
final class TenantsImpl
        implements Tenants {
    private final TenantsInner client;

    TenantsImpl(final TenantsInner client) {
        this.client = client;
    }

    @Override
    public PagedIterable<TenantIdDescriptionInner> list() {
        return client.list();
    }

    @Override
    public PagedFlux<TenantIdDescriptionInner> listAsync() {
        return client.listAsync();
    }
}
