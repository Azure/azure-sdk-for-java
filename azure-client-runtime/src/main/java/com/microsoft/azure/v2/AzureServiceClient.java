/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.v2;

import com.microsoft.rest.v2.ServiceClient;
import com.microsoft.rest.v2.http.HttpPipeline;
import com.microsoft.rest.v2.protocol.SerializerAdapter;

/**
 * The base class for generated Azure service clients.
 */
public abstract class AzureServiceClient extends ServiceClient {
    /**
     * Initializes a new instance of the AzureServiceClient class.
     *
     * @param httpPipeline The HTTP pipeline to send requests through
     */
    protected AzureServiceClient(HttpPipeline httpPipeline) {
        super(httpPipeline);
    }

    @Override
    protected SerializerAdapter<?> createSerializerAdapter() {
        return AzureProxy.createDefaultSerializer();
    }
}
