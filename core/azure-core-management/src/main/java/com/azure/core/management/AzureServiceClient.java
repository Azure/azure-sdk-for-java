// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.management;

import com.azure.core.AzureEnvironment;
import com.azure.core.ServiceClient;
import com.azure.core.http.HttpPipeline;

/**
 * The base class for generated Azure service clients.
 */
public abstract class AzureServiceClient extends ServiceClient {
    /**
     * The environment that this AzureServiceClient targets.
     */
    private final AzureEnvironment azureEnvironment;

    /**
     * Initializes a new instance of the AzureServiceClient class.
     *
     * @param httpPipeline The HTTP pipeline to send requests through
     * @param azureEnvironment The environment that this AzureServiceClient targets.
     */
    protected AzureServiceClient(HttpPipeline httpPipeline, AzureEnvironment azureEnvironment) {
        super(httpPipeline);

        this.azureEnvironment = azureEnvironment;
    }

    /**
     * Get the environment that this AzureServiceClient targets.
     * @return the environment that this AzureServiceClient targets.
     */
    public AzureEnvironment azureEnvironment() {
        return azureEnvironment;
    }
}
