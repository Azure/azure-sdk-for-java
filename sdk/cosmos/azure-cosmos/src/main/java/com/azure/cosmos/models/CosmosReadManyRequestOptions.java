// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

/**
 * Specifies the options associated with read many operation
 * in the Azure Cosmos DB database service.
 */
public final class CosmosReadManyRequestOptions extends CosmosQueryRequestOptionsBase<CosmosReadManyRequestOptions> {
    /**
     * Instantiates a new read many request options.
     */
    public CosmosReadManyRequestOptions() {
        super();
    }

    /**
     * Instantiates a new read many request options.
     * @param options The request options to-be cloned
     */
    public CosmosReadManyRequestOptions(CosmosReadManyRequestOptions options) {
        super(options);
    }
}
