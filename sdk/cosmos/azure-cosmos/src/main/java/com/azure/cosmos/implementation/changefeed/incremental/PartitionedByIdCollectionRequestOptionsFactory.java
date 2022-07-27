// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.changefeed.incremental;

import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.implementation.changefeed.Lease;
import com.azure.cosmos.implementation.changefeed.RequestOptionsFactory;

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
