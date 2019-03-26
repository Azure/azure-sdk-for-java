/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.common.mgmt;

import com.azure.common.AzureEnvironment;
import com.azure.common.ServiceClient;
import com.azure.common.http.HttpPipeline;
import com.azure.common.implementation.serializer.SerializerAdapter;

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
    protected SerializerAdapter createSerializerAdapter() {
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
