// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.changefeed.implementation;

import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.FeedOptions;
import com.azure.cosmos.implementation.changefeed.Lease;
import com.azure.cosmos.implementation.changefeed.RequestOptionsFactory;

/**
 * Used to create request setOptions for partitioned lease collections, when partition getKey is defined as /getId.
 */
class PartitionedByIdCollectionRequestOptionsFactory implements RequestOptionsFactory {
    @Override
    public CosmosItemRequestOptions createRequestOptions(Lease lease) {
        CosmosItemRequestOptions requestOptions = new CosmosItemRequestOptions();

        return requestOptions;
    }

    @Override
    public FeedOptions createFeedOptions() {
        FeedOptions feedOptions = new FeedOptions();

        return feedOptions;
    }
}
