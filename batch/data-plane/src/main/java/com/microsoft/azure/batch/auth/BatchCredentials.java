// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.batch.auth;

import com.microsoft.rest.credentials.ServiceClientCredentials;

/**
 * Interface for credentials used to authenticate access to an Azure Batch account.
 */
public interface BatchCredentials extends ServiceClientCredentials {
    /**
     * Gets the Batch service endpoint.
     *
     * @return The Batch service endpoint.
     */
    String baseUrl();
}
