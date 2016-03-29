/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.auth;

public abstract class BatchCredentials {
    private String baseUrl;

    public String getBaseUrl() {
        return baseUrl;
    }

    protected void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
}
