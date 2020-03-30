/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.resources.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.management.resources.Tenants;
import com.azure.management.resources.models.TenantIdDescriptionInner;
import com.azure.management.resources.models.TenantsInner;

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
