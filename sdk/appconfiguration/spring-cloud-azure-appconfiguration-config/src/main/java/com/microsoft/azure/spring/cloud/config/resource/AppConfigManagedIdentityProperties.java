/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config.resource;

import org.springframework.lang.Nullable;

public class AppConfigManagedIdentityProperties {
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
