package com.microsoft.azure.management.resources.implementation;

import com.microsoft.azure.management.resources.fluentcore.model.implementation.IndexableWrapperImpl;
import com.microsoft.azure.management.resources.Tenant;
import com.microsoft.azure.management.resources.implementation.api.TenantIdDescriptionInner;

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
