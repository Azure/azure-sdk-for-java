// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.internal.changefeed.implementation;

import com.azure.cosmos.CosmosItemRequestOptions;
import com.azure.cosmos.FeedOptions;
import com.azure.cosmos.PartitionKey;
import com.azure.cosmos.internal.changefeed.Lease;
import com.azure.cosmos.internal.changefeed.RequestOptionsFactory;

/**
 * Used to create request setOptions for partitioned lease collections, when partition getKey is defined as /getId.
 */
class PartitionedByIdCollectionRequestOptionsFactory implements RequestOptionsFactory {
    @Override
    public CosmosItemRequestOptions createRequestOptions(Lease lease) {
        CosmosItemRequestOptions requestOptions = new CosmosItemRequestOptions();
        requestOptions.setPartitionKey(new PartitionKey(lease.getId()));

        return requestOptions;
    }

    @Override
    public FeedOptions createFeedOptions() {
        FeedOptions feedOptions = new FeedOptions();
        feedOptions.setEnableCrossPartitionQuery(true);

        return feedOptions;
    }
}
