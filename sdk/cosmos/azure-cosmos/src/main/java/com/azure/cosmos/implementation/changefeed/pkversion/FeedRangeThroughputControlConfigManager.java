// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.changefeed.pkversion;

import com.azure.cosmos.ThroughputControlGroupConfig;
import com.azure.cosmos.ThroughputControlGroupConfigBuilder;
import com.azure.cosmos.implementation.changefeed.ChangeFeedContextClient;
import com.azure.cosmos.implementation.changefeed.Lease;
import com.azure.cosmos.models.FeedRange;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

// Only used in CFP when customer configure throughput control config
// The main purpose of this class is to create corresponding throughput control group for each feed range
public class FeedRangeThroughputControlConfigManager {

    private final ThroughputControlGroupConfig throughputControlGroupConfig;
    private final ChangeFeedContextClient documentClient;

    public FeedRangeThroughputControlConfigManager(
        ThroughputControlGroupConfig throughputControlGroupConfig,
        ChangeFeedContextClient documentClient) {

        checkNotNull(throughputControlGroupConfig, "Argument 'throughputControlGroupConfig' can not be null");
        checkNotNull(documentClient, "Argument 'documentClient' can not be null");

        this.throughputControlGroupConfig = throughputControlGroupConfig;
        this.documentClient = documentClient;
    }

    public ThroughputControlGroupConfig getThroughputControlConfigForLeaseFeedRange(Lease lease) {
        checkNotNull(lease, "Argument 'lease' can not be null");

        // for pkRange leases, it has only been used to support for split
        // the lease feed range and partition key range is always a 1:1 mapping
        // based on the thought that usually each CFP instance will only process a unique subset of partition key ranges
        // we create a local throughput control group for each partition key range
        return this.getThroughputControlGroupConfigInternal(lease.getFeedRange());
    }

    private ThroughputControlGroupConfig getThroughputControlGroupConfigInternal(FeedRange feedRange) {
        ThroughputControlGroupConfigBuilder throughputControlGroupConfigForFeedRangeBuilder =
            new ThroughputControlGroupConfigBuilder()
                .groupName(this.throughputControlGroupConfig.getGroupName() + "-" + feedRange.toString())
                .continueOnInitError(this.throughputControlGroupConfig.continueOnInitError());

        if (this.throughputControlGroupConfig.getTargetThroughput() != null) {
            throughputControlGroupConfigForFeedRangeBuilder.targetThroughput(this.throughputControlGroupConfig.getTargetThroughput());
        }
        if (this.throughputControlGroupConfig.getTargetThroughputThreshold() != null) {
            throughputControlGroupConfigForFeedRangeBuilder.targetThroughputThreshold(this.throughputControlGroupConfig.getTargetThroughputThreshold());
        }
        if (this.throughputControlGroupConfig.getPriorityLevel() != null) {
            throughputControlGroupConfigForFeedRangeBuilder.priorityLevel(this.throughputControlGroupConfig.getPriorityLevel());
        }

        ThroughputControlGroupConfig throughputControlGroupConfigForFeedRange = throughputControlGroupConfigForFeedRangeBuilder.build();
        this.documentClient.getContainerClient().enableLocalThroughputControlGroup(throughputControlGroupConfigForFeedRange);

        return throughputControlGroupConfigForFeedRange;
    }
}
