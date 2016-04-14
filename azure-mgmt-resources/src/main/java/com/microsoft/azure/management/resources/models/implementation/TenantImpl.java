package com.microsoft.azure.management.resources.models.implementation;

import com.microsoft.azure.management.resources.fluentcore.model.implementation.IndexableWrapperImpl;
import com.microsoft.azure.management.resources.models.Tenant;
import com.microsoft.azure.management.resources.models.implementation.api.TenantIdDescriptionInner;

public class TenantImpl extends
        IndexableWrapperImpl<TenantIdDescriptionInner>
        implements
        Tenant {

    public TenantImpl(TenantIdDescriptionInner tenant) {
        super(tenant.id(), tenant);
    }

    /***********************************************************
     * Getters
     ***********************************************************/

    @Override
    public String tenantId() {
        return inner().tenantId();
    }
}
