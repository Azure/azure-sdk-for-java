/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.v3;

import com.microsoft.rest.v3.ServiceClient;
import com.microsoft.rest.v3.http.HttpPipeline;
import com.microsoft.rest.v3.protocol.SerializerAdapter;

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

    @Override
    protected SerializerAdapter<?> createSerializerAdapter() {
        return AzureProxy.createDefaultSerializer();
    }

    /**
     * Get the environment that this AzureServiceClient targets.
     * @return the environment that this AzureServiceClient targets.
     */
    public AzureEnvironment azureEnvironment() {
        return azureEnvironment;
    }
}
