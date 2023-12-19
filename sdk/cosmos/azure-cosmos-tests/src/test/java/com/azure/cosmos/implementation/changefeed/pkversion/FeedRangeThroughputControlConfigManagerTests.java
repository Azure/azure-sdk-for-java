// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.changefeed.pkversion;

import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.ThroughputControlGroupConfig;
import com.azure.cosmos.ThroughputControlGroupConfigBuilder;
import com.azure.cosmos.implementation.changefeed.ChangeFeedContextClient;
import com.azure.cosmos.implementation.feedranges.FeedRangePartitionKeyRangeImpl;
import com.azure.cosmos.models.FeedRange;
import com.azure.cosmos.models.PriorityLevel;
import org.mockito.Mockito;
import org.testng.annotations.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class FeedRangeThroughputControlConfigManagerTests {

    @Test(groups = "unit")
    public void getThroughputControlConfigForFeedRange() {

        ThroughputControlGroupConfig throughputControlGroupConfig =
            new ThroughputControlGroupConfigBuilder()
                .groupName("test-" + UUID.randomUUID())
                .targetThroughput(100)
                .targetThroughputThreshold(0.1)
                .priorityLevel(PriorityLevel.LOW)
                .build();

        ChangeFeedContextClient documentClientMock = Mockito.mock(ChangeFeedContextClient.class);
        CosmosAsyncContainer containerMock = Mockito.mock(CosmosAsyncContainer.class);
        Mockito.doReturn(containerMock).when(documentClientMock).getContainerClient();
        Mockito.doNothing().when(containerMock).enableLocalThroughputControlGroup(Mockito.any());

        FeedRangeThroughputControlConfigManager throughputControlConfigManager =
            new FeedRangeThroughputControlConfigManager(throughputControlGroupConfig,documentClientMock);

        FeedRange feedRange = new FeedRangePartitionKeyRangeImpl("1");
        ThroughputControlGroupConfig pkRangeThroughputControlConfig =
            throughputControlConfigManager.getThroughputControlConfigForFeedRange(feedRange);

        assertThat(pkRangeThroughputControlConfig).isNotNull();
        assertThat(pkRangeThroughputControlConfig.getGroupName()).isEqualTo(throughputControlGroupConfig.getGroupName());
        assertThat(pkRangeThroughputControlConfig.getTargetThroughput()).isEqualTo(throughputControlGroupConfig.getTargetThroughput());
        assertThat(pkRangeThroughputControlConfig.getTargetThroughputThreshold()).isEqualTo(throughputControlGroupConfig.getTargetThroughputThreshold());
        assertThat(pkRangeThroughputControlConfig.getPriorityLevel()).isEqualTo(throughputControlGroupConfig.getPriorityLevel());
    }
}
