package com.microsoft.azure.management.resources.implementation;

import com.microsoft.azure.management.resources.fluentcore.model.implementation.IndexableWrapperImpl;
import com.microsoft.azure.management.resources.Tenant;
import com.microsoft.azure.management.resources.implementation.api.TenantIdDescriptionInner;

/**
 * An instance of this class provides access to a tenant in Azure.
 */
final class TenantImpl extends
        IndexableWrapperImpl<TenantIdDescriptionInner>
        implements
        Tenant {

    TenantImpl(TenantIdDescriptionInner innerModel) {
        super(innerModel.id(), innerModel);
    }

    @Override
    public String tenantId() {
        return inner().tenantId();
    }
}
