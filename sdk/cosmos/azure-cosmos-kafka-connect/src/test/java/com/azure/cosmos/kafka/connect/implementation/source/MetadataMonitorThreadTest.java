// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation.source;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.implementation.feedranges.FeedRangeEpkImpl;
import com.azure.cosmos.kafka.connect.InMemoryStorageReader;
import com.azure.cosmos.kafka.connect.KafkaCosmosTestSuiteBase;
import com.azure.cosmos.kafka.connect.implementation.CosmosAccountConfig;
import com.azure.cosmos.kafka.connect.implementation.CosmosClientStore;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.FeedRange;
import org.apache.kafka.connect.source.SourceConnectorContext;
import org.mockito.Mockito;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class MetadataMonitorThreadTest extends KafkaCosmosTestSuiteBase {
    private CosmosAsyncClient client;
    @BeforeClass(groups = {"fast"}, timeOut = TIMEOUT)
    public void before_MetadataMonitorThreadTest() {
        CosmosAccountConfig accountConfig = new CosmosAccountConfig(
            TestConfigurations.HOST,
            TestConfigurations.MASTER_KEY,
            "requestTaskReconfigurationTest",
            false,
            new ArrayList<String>());
        this.client = CosmosClientStore.getCosmosClient(accountConfig);
    }

    @AfterClass(groups = {"fast"}, timeOut = TIMEOUT)
    public void after_MetadataMonitorThreadTest() {
        if (this.client != null) {
            this.client.close();
        }
    }

    @Test(groups = "{ fast }", timeOut = TIMEOUT)
    public void requestTaskReconfigurationOnContainersChange() throws InterruptedException {

        CosmosSourceContainersConfig cosmosSourceContainersConfig =
            new CosmosSourceContainersConfig(
                databaseName,
                true,
                new ArrayList<String>(),
                new ArrayList<String>());
        CosmosMetadataConfig metadataConfig =
            new CosmosMetadataConfig(500, "_cosmos.metadata.topic");
        SourceConnectorContext sourceConnectorContext = Mockito.mock(SourceConnectorContext.class);
        InMemoryStorageReader inMemoryStorageReader = new InMemoryStorageReader();
        CosmosSourceOffsetStorageReader sourceOffsetStorageReader = new CosmosSourceOffsetStorageReader(inMemoryStorageReader);

        MetadataMonitorThread monitorThread =
            new MetadataMonitorThread(
                cosmosSourceContainersConfig,
                metadataConfig,
                sourceConnectorContext,
                sourceOffsetStorageReader,
                this.client);

        monitorThread.run();

        Thread.sleep(1000);
        // Since there is no offset yet, no requestTaskReconfiguration will happen
        Mockito.verify(sourceConnectorContext, Mockito.never()).requestTaskReconfiguration();

        // now populate containers metadata offset
        CosmosContainerProperties singlePartitionContainer = getSinglePartitionContainer(this.client);
        ContainersMetadataTopicPartition containersMetadataTopicPartition =
            new ContainersMetadataTopicPartition(databaseName);
        ContainersMetadataTopicOffset containersMetadataTopicOffset =
            new ContainersMetadataTopicOffset(Arrays.asList(singlePartitionContainer.getResourceId()));

        Map<Map<String, Object>, Map<String, Object>> offsetMap = new HashMap<>();
        offsetMap.put(
            ContainersMetadataTopicPartition.toMap(containersMetadataTopicPartition),
            ContainersMetadataTopicOffset.toMap(containersMetadataTopicOffset));

        inMemoryStorageReader.populateOffset(offsetMap);

        Thread.sleep(5000); // give enough time to do the containers query request
        monitorThread.close();

        Mockito.verify(sourceConnectorContext, Mockito.atLeastOnce()).requestTaskReconfiguration();
    }

    @Test(groups = "{ fast }", timeOut = TIMEOUT)
    public void requestTaskReconfigurationOnSplit() throws InterruptedException {

        CosmosSourceContainersConfig cosmosSourceContainersConfig =
            new CosmosSourceContainersConfig(
                databaseName,
                false,
                Arrays.asList(multiPartitionContainerName),
                new ArrayList<String>());
        CosmosMetadataConfig metadataConfig =
            new CosmosMetadataConfig(500, "_cosmos.metadata.topic");
        SourceConnectorContext sourceConnectorContext = Mockito.mock(SourceConnectorContext.class);

        InMemoryStorageReader inMemoryStorageReader = new InMemoryStorageReader();
        CosmosSourceOffsetStorageReader sourceOffsetStorageReader = new CosmosSourceOffsetStorageReader(inMemoryStorageReader);

        //populate containers metadata offset
        CosmosContainerProperties multiPartitionContainer = getMultiPartitionContainer(this.client);
        ContainersMetadataTopicPartition containersMetadataTopicPartition =
            new ContainersMetadataTopicPartition(databaseName);
        ContainersMetadataTopicOffset containersMetadataTopicOffset =
            new ContainersMetadataTopicOffset(Arrays.asList(multiPartitionContainer.getResourceId()));
        Map<Map<String, Object>, Map<String, Object>> offsetMap = new HashMap<>();
        offsetMap.put(
            ContainersMetadataTopicPartition.toMap(containersMetadataTopicPartition),
            ContainersMetadataTopicOffset.toMap(containersMetadataTopicOffset));

        inMemoryStorageReader.populateOffset(offsetMap);

        MetadataMonitorThread monitorThread =
            new MetadataMonitorThread(
                cosmosSourceContainersConfig,
                metadataConfig,
                sourceConnectorContext,
                sourceOffsetStorageReader,
                this.client);

        monitorThread.run();

        Thread.sleep(2000); // give some time for the query containers requests
        // Since there is no offset yet, no requestTaskReconfiguration will happen
        Mockito.verify(sourceConnectorContext, Mockito.never()).requestTaskReconfiguration();

        // now populate container feedRanges metadata
        List<FeedRange> feedRanges =
            this.client
                .getDatabase(databaseName)
                .getContainer(multiPartitionContainer.getId())
                .getFeedRanges()
                .block();
        assertThat(feedRanges.size()).isGreaterThan(1);

        FeedRangesMetadataTopicPartition feedRangesMetadataTopicPartition =
            new FeedRangesMetadataTopicPartition(databaseName, multiPartitionContainer.getResourceId());
        FeedRangesMetadataTopicOffset feedRangesMetadataTopicOffset =
            new FeedRangesMetadataTopicOffset(Arrays.asList(FeedRangeEpkImpl.forFullRange().getRange()));

        Map<Map<String, Object>, Map<String, Object>> feedRangesOffSetMap = new HashMap<>();
        feedRangesOffSetMap.put(
            FeedRangesMetadataTopicPartition.toMap(feedRangesMetadataTopicPartition),
            FeedRangesMetadataTopicOffset.toMap(feedRangesMetadataTopicOffset));

        inMemoryStorageReader.populateOffset(feedRangesOffSetMap);

        Thread.sleep(5000); // give enough time for the containers query and feedRanges request
        monitorThread.close();

        // for merge, no task reconfiguration is needed
        Mockito.verify(sourceConnectorContext, Mockito.atLeastOnce()).requestTaskReconfiguration();
    }

    @Test(groups = "{ fast }", timeOut = TIMEOUT)
    public void requestTaskReconfigurationOnMerge() throws InterruptedException {

        CosmosSourceContainersConfig cosmosSourceContainersConfig =
            new CosmosSourceContainersConfig(
                databaseName,
                false,
                Arrays.asList(singlePartitionContainerName),
                new ArrayList<String>());
        CosmosMetadataConfig metadataConfig =
            new CosmosMetadataConfig(500, "_cosmos.metadata.topic");
        SourceConnectorContext sourceConnectorContext = Mockito.mock(SourceConnectorContext.class);

        InMemoryStorageReader inMemoryStorageReader = new InMemoryStorageReader();
        CosmosSourceOffsetStorageReader sourceOffsetStorageReader = new CosmosSourceOffsetStorageReader(inMemoryStorageReader);

        //populate containers metadata offset
        CosmosContainerProperties singlePartitionContainer = getSinglePartitionContainer(this.client);
        ContainersMetadataTopicPartition containersMetadataTopicPartition =
            new ContainersMetadataTopicPartition(databaseName);
        ContainersMetadataTopicOffset containersMetadataTopicOffset =
            new ContainersMetadataTopicOffset(Arrays.asList(singlePartitionContainer.getResourceId()));
        Map<Map<String, Object>, Map<String, Object>> offsetMap = new HashMap<>();
        offsetMap.put(
            ContainersMetadataTopicPartition.toMap(containersMetadataTopicPartition),
            ContainersMetadataTopicOffset.toMap(containersMetadataTopicOffset));

        inMemoryStorageReader.populateOffset(offsetMap);

        MetadataMonitorThread monitorThread =
            new MetadataMonitorThread(
                cosmosSourceContainersConfig,
                metadataConfig,
                sourceConnectorContext,
                sourceOffsetStorageReader,
                this.client);

        monitorThread.run();

        Thread.sleep(2000); // give some time for the query containers requests
        // Since there is no offset yet, no requestTaskReconfiguration will happen
        Mockito.verify(sourceConnectorContext, Mockito.never()).requestTaskReconfiguration();

        // now populate container feedRanges metadata
        List<FeedRange> feedRanges =
            this.client
                .getDatabase(databaseName)
                .getContainer(singlePartitionContainer.getId())
                .getFeedRanges()
                .block();
        assertThat(feedRanges.size()).isEqualTo(1);

        List<FeedRangeEpkImpl> childRanges =
            ImplementationBridgeHelpers
                .CosmosAsyncContainerHelper
                .getCosmosAsyncContainerAccessor()
                .trySplitFeedRange(
                    this.client.getDatabase(databaseName).getContainer(singlePartitionContainer.getId()),
                    FeedRange.forFullRange(),
                    2)
                .block();

        FeedRangesMetadataTopicPartition feedRangesMetadataTopicPartition =
            new FeedRangesMetadataTopicPartition(databaseName, singlePartitionContainer.getResourceId());
        FeedRangesMetadataTopicOffset feedRangesMetadataTopicOffset =
            new FeedRangesMetadataTopicOffset(
                childRanges
                    .stream()
                    .map(FeedRangeEpkImpl::getRange)
                    .collect(Collectors.toList()));

        Map<Map<String, Object>, Map<String, Object>> feedRangesOffSetMap = new HashMap<>();
        feedRangesOffSetMap.put(
            FeedRangesMetadataTopicPartition.toMap(feedRangesMetadataTopicPartition),
            FeedRangesMetadataTopicOffset.toMap(feedRangesMetadataTopicOffset));

        inMemoryStorageReader.populateOffset(feedRangesOffSetMap);

        Thread.sleep(5000); // give enough time for the containers query and feedRanges request
        monitorThread.close();

        // for merge, no task reconfiguration is needed
        Mockito.verify(sourceConnectorContext, Mockito.never()).requestTaskReconfiguration();
    }
}
