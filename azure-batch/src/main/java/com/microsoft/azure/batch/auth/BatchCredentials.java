/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.auth;

/**
 * Base class for credentials used to authenticate access to an Azure Batch account.
 */
public abstract class BatchCredentials {
    private String baseUrl;

    /**
     * Gets the Batch service endpoint
     *
     * @return The Batch service endpoint
     */
    public String baseUrl() {
        return baseUrl;
    }

    /**
     * Initializes a new instance of the {@link BatchCredentials} class.
     *
     * @param baseUrl The Batch service endpoint
     * @return The new instance of BatchCredentials.
     */
    protected BatchCredentials withBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
        return this;
    }
}
