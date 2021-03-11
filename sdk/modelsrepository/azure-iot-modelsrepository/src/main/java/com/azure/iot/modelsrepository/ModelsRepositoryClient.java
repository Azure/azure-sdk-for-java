// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.iot.modelsrepository;

import com.azure.core.annotation.ServiceClient;

/**
 * This class provides a client for interacting synchronously with a ModelsRepository instance.
 * This client is instantiated through {@link ModelsRepositoryClientBuilder}.
 *
 * <p>
 * This client allows for TODO: azabbasi
 * </p>
 */
@ServiceClient(builder = ModelsRepositoryClientBuilder.class)
public final class ModelsRepositoryClient {
    private final ModelsRepositoryAsyncClient modelsRepositoryAsyncClient;

    ModelsRepositoryClient(ModelsRepositoryAsyncClient modelsRepositoryAsyncClient) {
        this.modelsRepositoryAsyncClient = modelsRepositoryAsyncClient;
    }

    /**
     * Gets the Azure Models Repository service API version that this client is configured to use for all service requests.
     * Unless configured while building this client through {@link ModelsRepositoryClientBuilder#serviceVersion(ModelsRepositoryServiceVersion)},
     * this value will be equal to the latest service API version supported by this client.
     *
     * @return The Azure Models Repository service API version.
     */
    public ModelsRepositoryServiceVersion getServiceVersion() {
        return this.modelsRepositoryAsyncClient.getServiceVersion();
    }
}
