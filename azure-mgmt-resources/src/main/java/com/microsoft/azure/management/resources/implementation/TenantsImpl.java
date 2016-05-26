/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.implementation;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.resources.Tenant;
import com.microsoft.azure.management.resources.Tenants;
import com.microsoft.azure.management.resources.fluentcore.utils.PagedListConverter;
import com.microsoft.azure.management.resources.implementation.api.TenantIdDescriptionInner;
import com.microsoft.azure.management.resources.implementation.api.TenantsInner;

import java.io.IOException;

/**
 * Implementation for Tenants and its nested interfaces.
 */
final class TenantsImpl
        implements Tenants {
    private final TenantsInner client;

    TenantsImpl(final TenantsInner client) {
        this.client = client;
    }

    @Override
    public PagedList<Tenant> list() throws CloudException, IOException {
        PagedListConverter<TenantIdDescriptionInner, Tenant> converter = new PagedListConverter<TenantIdDescriptionInner, Tenant>() {
            @Override
            public Tenant typeConvert(TenantIdDescriptionInner tenantInner) {
                return new TenantImpl(tenantInner);
            }
        };
        return converter.convert(client.list().getBody());
    }
}
