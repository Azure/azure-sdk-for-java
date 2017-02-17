/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.implementation;

import com.microsoft.azure.Page;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.resources.Tenant;
import com.microsoft.azure.management.resources.Tenants;
import com.microsoft.azure.management.resources.fluentcore.utils.PagedListConverter;
import rx.Observable;
import rx.functions.Func1;

import java.util.List;

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
    public PagedList<Tenant> list() {
        PagedListConverter<TenantIdDescriptionInner, Tenant> converter = new PagedListConverter<TenantIdDescriptionInner, Tenant>() {
            @Override
            public Tenant typeConvert(TenantIdDescriptionInner tenantInner) {
                return wrapModel(tenantInner);
            }
        };
        return converter.convert(client.list());
    }

    @Override
    public Observable<Tenant> listAsync() {
        return client.listAsync().map(new Func1<Page<TenantIdDescriptionInner>, List<TenantIdDescriptionInner>>() {
            @Override
            public List<TenantIdDescriptionInner> call(Page<TenantIdDescriptionInner> tenantIdDescriptionInnerPage) {
                return tenantIdDescriptionInnerPage.items();
            }
        }).flatMap(new Func1<List<TenantIdDescriptionInner>, Observable<TenantIdDescriptionInner>>() {
            @Override
            public Observable<TenantIdDescriptionInner> call(List<TenantIdDescriptionInner> tenantIdDescriptionInners) {
                return Observable.from(tenantIdDescriptionInners);
            }
        }).map(new Func1<TenantIdDescriptionInner, Tenant>() {
            @Override
            public Tenant call(TenantIdDescriptionInner tenantIdDescriptionInner) {
                return wrapModel(tenantIdDescriptionInner);
            }
        });
    }

    private Tenant wrapModel(TenantIdDescriptionInner tenantInner) {
        return new TenantImpl(tenantInner);
    }
}
