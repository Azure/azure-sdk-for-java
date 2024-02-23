// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.changefeed.pkversion;

import com.azure.cosmos.ThroughputControlGroupConfig;
import com.azure.cosmos.implementation.changefeed.ChangeFeedContextClient;
import com.azure.cosmos.models.FeedRange;

import java.util.concurrent.atomic.AtomicBoolean;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

// Only used in CFP when customer configure throughput control config
// The main purpose of this class is to create corresponding throughput control group for each feed range
public class FeedRangeThroughputControlConfigManager {

    private final ThroughputControlGroupConfig throughputControlGroupConfig;
    private final ChangeFeedContextClient documentClient;
    private final AtomicBoolean throughputControlGroupEnabled;

    public FeedRangeThroughputControlConfigManager(
        ThroughputControlGroupConfig throughputControlGroupConfig,
        ChangeFeedContextClient documentClient) {

        checkNotNull(throughputControlGroupConfig, "Argument 'throughputControlGroupConfig' can not be null");
        checkNotNull(documentClient, "Argument 'documentClient' can not be null");

        this.throughputControlGroupConfig = throughputControlGroupConfig;
        this.documentClient = documentClient;
        this.throughputControlGroupEnabled = new AtomicBoolean(false);
    }

    public ThroughputControlGroupConfig getThroughputControlConfigForFeedRange(FeedRange feedRange) {
        checkNotNull(feedRange, "Argument 'feedRange' can not be null");

        // for pkRange leases, it has only been used to support for split
        // the lease feed range and partition key range is always a 1:1 mapping
        // throughput control internally will divide the target RU across all pk ranges
        // so all pk ranges can use the same local throughput control group
        // Note: if global throughput control be added in future, then we will need to create one group per pkRange

        if (this.throughputControlGroupEnabled.compareAndSet(false, true)) {
            this.documentClient.getContainerClient().enableLocalThroughputControlGroup(this.throughputControlGroupConfig);
        }

        return this.throughputControlGroupConfig;
    }
}
