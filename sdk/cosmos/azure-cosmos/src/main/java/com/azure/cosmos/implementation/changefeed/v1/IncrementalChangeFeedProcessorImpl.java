// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.changefeed.v1;

import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedMode;
import com.azure.cosmos.models.ChangeFeedProcessorOptions;
import com.azure.cosmos.models.CosmosChangeFeedRequestOptions;
import com.azure.cosmos.models.FeedRange;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;
import java.util.function.Consumer;

public class IncrementalChangeFeedProcessorImpl extends ChangeFeedProcessorBase<JsonNode> {

    public IncrementalChangeFeedProcessorImpl(
            String hostName,
            CosmosAsyncContainer feedContainer,
            CosmosAsyncContainer leaseContainer,
            Consumer<List<JsonNode>> consumer,
            ChangeFeedProcessorOptions changeFeedProcessorOptions) {
        super(hostName, feedContainer, leaseContainer, changeFeedProcessorOptions, consumer, ChangeFeedMode.INCREMENTAL);
    }

    @Override
    CosmosChangeFeedRequestOptions createRequestOptionsForProcessingFromNow(FeedRange feedRange) {
        return CosmosChangeFeedRequestOptions
                .createForProcessingFromNow(feedRange)
                .setMaxItemCount(1);
    }

    @Override
    Class<JsonNode> getPartitionProcessorItemType() {
        return JsonNode.class;
    }
}
