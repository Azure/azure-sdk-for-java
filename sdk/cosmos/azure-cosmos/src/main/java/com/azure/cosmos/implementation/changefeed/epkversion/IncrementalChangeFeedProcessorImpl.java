// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.changefeed.epkversion;

import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.implementation.changefeed.common.ChangeFeedMode;
import com.azure.cosmos.models.ChangeFeedProcessorItem;
import com.azure.cosmos.models.ChangeFeedProcessorOptions;
import com.azure.cosmos.models.CosmosChangeFeedRequestOptions;
import com.azure.cosmos.models.FeedRange;

import java.util.List;
import java.util.function.Consumer;

public class IncrementalChangeFeedProcessorImpl extends ChangeFeedProcessorImplBase<ChangeFeedProcessorItem> {

    public IncrementalChangeFeedProcessorImpl(
            String hostName,
            CosmosAsyncContainer feedContainer,
            CosmosAsyncContainer leaseContainer,
            Consumer<List<ChangeFeedProcessorItem>> consumer,
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
    Class<ChangeFeedProcessorItem> getPartitionProcessorItemType() {
        return ChangeFeedProcessorItem.class;
    }

    @Override
    boolean canBootstrapFromPkRangeIdVersionLeaseStore() {
        return true;
    }
}
