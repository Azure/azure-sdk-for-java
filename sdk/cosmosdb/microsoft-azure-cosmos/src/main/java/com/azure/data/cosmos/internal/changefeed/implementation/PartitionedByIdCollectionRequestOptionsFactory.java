// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.internal.changefeed.implementation;

import com.azure.data.cosmos.CosmosItemRequestOptions;
import com.azure.data.cosmos.FeedOptions;
import com.azure.data.cosmos.PartitionKey;
import com.azure.data.cosmos.internal.changefeed.Lease;
import com.azure.data.cosmos.internal.changefeed.RequestOptionsFactory;

/**
 * Used to create request options for partitioned lease collections, when partition key is defined as /id.
 */
class PartitionedByIdCollectionRequestOptionsFactory implements RequestOptionsFactory {
    @Override
    public CosmosItemRequestOptions createRequestOptions(Lease lease) {
        CosmosItemRequestOptions requestOptions = new CosmosItemRequestOptions();
        requestOptions.partitionKey(new PartitionKey(lease.getId()));

        return requestOptions;
    }

    @Override
    public FeedOptions createFeedOptions() {
        FeedOptions feedOptions = new FeedOptions();
        feedOptions.enableCrossPartitionQuery(true);

        return feedOptions;
    }
}
