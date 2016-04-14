package com.microsoft.azure.management.resources.models;

import com.microsoft.azure.management.resources.fluentcore.model.Indexable;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;
import com.microsoft.azure.management.resources.models.implementation.api.TenantIdDescriptionInner;

public interface Tenant extends
        Indexable,
        Wrapper<TenantIdDescriptionInner>{

    /***********************************************************
     * Getters
     ***********************************************************/

    String tenantId();
}
