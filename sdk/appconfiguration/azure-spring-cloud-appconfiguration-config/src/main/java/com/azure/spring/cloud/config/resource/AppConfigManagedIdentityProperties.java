// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config.resource;

import org.springframework.lang.Nullable;

/**
 * Managed Identity information for connecting to Azure App Configuration Stores.
 */
public final class AppConfigManagedIdentityProperties {

    @Nullable
    private String clientId; // Optional: client_id of the managed identity

    @Nullable
    public String getClientId() {
        return clientId;
    }

    public void setClientId(@Nullable String clientId) {
        this.clientId = clientId;
    }
}
