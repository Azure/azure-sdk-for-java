package com.microsoft.azure.management.resources;

import com.microsoft.azure.management.resources.fluentcore.model.Indexable;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;
import com.microsoft.azure.management.resources.implementation.api.TenantIdDescriptionInner;

/**
 * Defines an interface for accessing a tenant in Azure.
 */
public interface Tenant extends
        Indexable,
        Wrapper<TenantIdDescriptionInner> {

    /**
     * Get the tenant ID.
     *
     * @return the tenant ID.
     */
    String tenantId();
}
