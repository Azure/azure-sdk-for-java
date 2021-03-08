// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.implementation;

import com.azure.resourcemanager.resources.fluent.models.TenantIdDescriptionInner;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.IndexableWrapperImpl;
import com.azure.resourcemanager.resources.models.Tenant;

/**
 * The implementation of {@link Tenant}.
 */
final class TenantImpl extends IndexableWrapperImpl<TenantIdDescriptionInner> implements Tenant {
    TenantImpl(TenantIdDescriptionInner innerModel) {
        super(innerModel);
    }

    @Override
    public String tenantId() {
        return innerModel().tenantId();
    }
}
