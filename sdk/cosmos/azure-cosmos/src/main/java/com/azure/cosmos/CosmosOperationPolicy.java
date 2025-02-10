// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import com.azure.cosmos.models.CosmosOperationDetails;

/**
 * Represents a policy that can be used with {@link CosmosClientBuilder} to customize the request sent to Azure Cosmos DB.
 */
@FunctionalInterface
public interface CosmosOperationPolicy {

    /**
     * Processes the request details and calls the next policy in the chain.
     *
     * @param operationDetails The request details.
     */
    void process(CosmosOperationDetails operationDetails);
}
