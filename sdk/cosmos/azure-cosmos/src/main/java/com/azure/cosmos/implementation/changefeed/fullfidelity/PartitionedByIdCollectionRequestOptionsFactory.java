// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.changefeed.fullfidelity;

import com.azure.cosmos.implementation.changefeed.Lease;
import com.azure.cosmos.implementation.changefeed.RequestOptionsFactory;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosQueryRequestOptions;

/**
 * Used to create request setOptions for partitioned lease collections, when partition getKey is defined as /getId.
 */
class PartitionedByIdCollectionRequestOptionsFactory implements RequestOptionsFactory {
    @Override
    public CosmosItemRequestOptions createItemRequestOptions(Lease lease) {
        return new CosmosItemRequestOptions();
    }

    @Override
    public CosmosQueryRequestOptions createQueryRequestOptions() {
        return new CosmosQueryRequestOptions();
    }
}
