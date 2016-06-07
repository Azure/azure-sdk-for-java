/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.implementation.api;


/**
 * Tenant Id information.
 */
public class TenantIdDescriptionInner {
    /**
     * Gets or sets Id.
     */
    private String id;

    /**
     * Gets or sets tenantId.
     */
    private String tenantId;

    /**
     * Get the id value.
     *
     * @return the id value
     */
    public String id() {
        return this.id;
    }

    /**
     * Set the id value.
     *
     * @param id the id value to set
     * @return the TenantIdDescriptionInner object itself.
     */
    public TenantIdDescriptionInner withId(String id) {
        this.id = id;
        return this;
    }

    /**
     * Get the tenantId value.
     *
     * @return the tenantId value
     */
    public String tenantId() {
        return this.tenantId;
    }

    /**
     * Set the tenantId value.
     *
     * @param tenantId the tenantId value to set
     * @return the TenantIdDescriptionInner object itself.
     */
    public TenantIdDescriptionInner withTenantId(String tenantId) {
        this.tenantId = tenantId;
        return this;
    }

}
