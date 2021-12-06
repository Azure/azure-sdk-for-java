// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.aad.b2c.implementation;

/**
 * Profile of Azure cloud environment.
 */
public class AADB2CProfileProperties {
    /**
     * Azure Tenant ID.
     */
    private String tenantId; // tenantId can not set to "common" here, otherwise we can not know whether it's set by customer or it is the default value.

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getTenantId() {
        return tenantId;
    }
}
