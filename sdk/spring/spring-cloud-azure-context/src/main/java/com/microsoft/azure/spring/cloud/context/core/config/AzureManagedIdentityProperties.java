/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.context.core.config;

import org.springframework.lang.Nullable;

/**
 * Properties for getting token from Azure Instance Metadata Service (IMDS) endpoint
 * <pre>
 * @see <a href="https://docs.microsoft.com/en-us/azure/active-directory/managed-identities-azure-resources/how-to-use-vm-token#get-a-token-using-http">Get a token using HTTP<a/>
 * </pre>
 */
public class AzureManagedIdentityProperties {
    @Nullable
    private String objectId; // Optional: object_id of the managed identity

    @Nullable
    private String clientId; // Optional: client_id of the managed identity

    @Nullable
    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(@Nullable String objectId) {
        this.objectId = objectId;
    }

    @Nullable
    public String getClientId() {
        return clientId;
    }

    public void setClientId(@Nullable String clientId) {
        this.clientId = clientId;
    }
}
