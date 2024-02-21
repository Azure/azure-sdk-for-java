// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.changefeed.epkversion;

import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.ThroughputControlGroupConfig;
import com.azure.cosmos.ThroughputControlGroupConfigBuilder;
import com.azure.cosmos.implementation.PartitionKeyRange;
import com.azure.cosmos.implementation.changefeed.ChangeFeedContextClient;
import com.azure.cosmos.implementation.changefeed.Lease;
import com.azure.cosmos.implementation.feedranges.FeedRangeEpkImpl;
import com.azure.cosmos.implementation.routing.PartitionKeyInternalHelper;
import com.azure.cosmos.implementation.routing.Range;
import com.azure.cosmos.models.PriorityLevel;
import org.mockito.Mockito;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
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
        List<PartitionKeyRange> pkRanges = Arrays.asList(
            new PartitionKeyRange("1", "AA", "DD"));
        Mockito.doReturn(Mono.just(pkRanges)).when(documentClientMock).getOverlappingRanges(PartitionKeyInternalHelper.FullRange, false);

        FeedRangeThroughputControlConfigManager throughputControlConfigManager =
            new FeedRangeThroughputControlConfigManager(throughputControlGroupConfig,documentClientMock);

        // refresh the throughputControlConfigManager
        List<Lease> allLeases = Arrays.asList(
            new ServiceItemLeaseV1().withFeedRange(new FeedRangeEpkImpl(new Range<>("AA", "CC", true, false))),
            new ServiceItemLeaseV1().withFeedRange(new FeedRangeEpkImpl(new Range<>("CC", "DD", true, false))));
        throughputControlConfigManager.refresh(allLeases);

        FeedRangeEpkImpl feedRangeEpk = new FeedRangeEpkImpl(new Range<>("AA", "CC", true, false));
        Mockito.doReturn(Mono.just(pkRanges)).when(documentClientMock).getOverlappingRanges(feedRangeEpk.getRange(), false);

        ThroughputControlGroupConfig pkRangeThroughputControlConfig =
            throughputControlConfigManager.getOrCreateThroughputControlConfigForFeedRange(feedRangeEpk).block();

        assertThat(pkRangeThroughputControlConfig).isNotNull();
        String expectedGroupName = throughputControlGroupConfig.getGroupName() + "-" + feedRangeEpk;
        assertThat(pkRangeThroughputControlConfig.getGroupName()).isEqualTo(expectedGroupName);
        assertThat(pkRangeThroughputControlConfig.getTargetThroughput()).isEqualTo(throughputControlGroupConfig.getTargetThroughput()/allLeases.size());
        assertThat(pkRangeThroughputControlConfig.getTargetThroughputThreshold()).isEqualTo(throughputControlGroupConfig.getTargetThroughputThreshold()/allLeases.size());
        assertThat(pkRangeThroughputControlConfig.getPriorityLevel()).isEqualTo(throughputControlGroupConfig.getPriorityLevel());
    }
}
