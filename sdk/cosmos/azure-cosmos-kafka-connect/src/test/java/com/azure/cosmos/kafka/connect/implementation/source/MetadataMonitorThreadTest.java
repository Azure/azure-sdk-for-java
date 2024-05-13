// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation.source;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.kafka.connect.InMemoryStorageReader;
import com.azure.cosmos.kafka.connect.KafkaCosmosTestSuiteBase;
import com.azure.cosmos.kafka.connect.implementation.CosmosAccountConfig;
import com.azure.cosmos.kafka.connect.implementation.CosmosClientStore;
import com.azure.cosmos.kafka.connect.implementation.CosmosMasterKeyAuthConfig;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.FeedRange;
import org.apache.kafka.connect.source.SourceConnectorContext;
import org.mockito.Mockito;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.fail;

public class MetadataMonitorThreadTest extends KafkaCosmosTestSuiteBase {
    private CosmosAsyncClient client;

    @DataProvider(name = "metadataStorageTypeParameterProvider")
    public Object[][] metadataStorageTypeParameterProvider() {
        return new Object[][]{
            { CosmosMetadataStorageType.KAFKA },
            { CosmosMetadataStorageType.COSMOS }
        };
    }

    @BeforeClass(groups = { "kafka", "kafka-emulator" }, timeOut = TIMEOUT)
    public void before_MetadataMonitorThreadTest() {
        CosmosAccountConfig accountConfig = new CosmosAccountConfig(
            TestConfigurations.HOST,
            new CosmosMasterKeyAuthConfig(TestConfigurations.MASTER_KEY),
            "requestTaskReconfigurationTest",
            false,
            new ArrayList<String>());
        this.client = CosmosClientStore.getCosmosClient(accountConfig, "testKafkaConnector");
    }

    @AfterClass(groups = { "kafka", "kafka-emulator" }, timeOut = TIMEOUT)
    public void after_MetadataMonitorThreadTest() {
        if (this.client != null) {
            this.client.close();
        }
    }

    @Test(groups = { "kafka", "kafka-emulator" }, dataProvider = "metadataStorageTypeParameterProvider", timeOut = TIMEOUT)
    public void requestTaskReconfigurationOnContainersChange(CosmosMetadataStorageType metadataStorageType) throws InterruptedException {
        String metadataStorageName = "_cosmos.metadata.topic";
        String connectorName = "requestTaskReconfigurationOnContainersChange";
        try {
            CosmosSourceContainersConfig cosmosSourceContainersConfig =
                new CosmosSourceContainersConfig(
                    databaseName,
                    true,
                    new ArrayList<String>(),
                    new ArrayList<String>());

            CosmosMetadataConfig metadataConfig =
                new CosmosMetadataConfig(500, metadataStorageType, metadataStorageName);
            SourceConnectorContext sourceConnectorContext = Mockito.mock(SourceConnectorContext.class);

            IMetadataReader metadataReader = getMetadataReader(metadataStorageType, metadataConfig);
            MetadataMonitorThread monitorThread =
                new MetadataMonitorThread(
                    connectorName,
                    cosmosSourceContainersConfig,
                    metadataConfig,
                    sourceConnectorContext,
                    metadataReader,
                    this.client);

            monitorThread.run();

            Thread.sleep(1000);
            // Since there is no offset yet, no requestTaskReconfiguration will happen
            Mockito.verify(sourceConnectorContext, Mockito.never()).requestTaskReconfiguration();

            // now populate containers metadata offset
            CosmosContainerProperties singlePartitionContainer = getSinglePartitionContainer(this.client);
            ContainersMetadataTopicPartition containersMetadataTopicPartition =
                new ContainersMetadataTopicPartition(databaseName, connectorName);
            ContainersMetadataTopicOffset containersMetadataTopicOffset =
                new ContainersMetadataTopicOffset(Arrays.asList(singlePartitionContainer.getResourceId()));

            this.populateContainersMetadata(metadataStorageType, metadataReader, containersMetadataTopicPartition, containersMetadataTopicOffset);

            Thread.sleep(5000); // give enough time to do the containers query request
            monitorThread.close();

            Mockito.verify(sourceConnectorContext, Mockito.atLeastOnce()).requestTaskReconfiguration();
        } finally {
            if (metadataStorageType == CosmosMetadataStorageType.COSMOS) {
                this.client.getDatabase(databaseName).getContainer(metadataStorageName).delete();
            }
        }
    }

    @Test(groups = { "kafka", "kafka-emulator" }, dataProvider = "metadataStorageTypeParameterProvider", timeOut = TIMEOUT)
    public void requestTaskReconfigurationOnSplit(CosmosMetadataStorageType metadataStorageType) throws InterruptedException {

        String metadataStorageName = "_cosmos.metadata.topic";
        String connectorName = "requestTaskReconfigurationOnSplit";
        try {
            CosmosSourceContainersConfig cosmosSourceContainersConfig =
                new CosmosSourceContainersConfig(
                    databaseName,
                    false,
                    Arrays.asList(multiPartitionContainerName),
                    new ArrayList<String>());
            CosmosMetadataConfig metadataConfig =
                new CosmosMetadataConfig(500, metadataStorageType, metadataStorageName);
            SourceConnectorContext sourceConnectorContext = Mockito.mock(SourceConnectorContext.class);

            IMetadataReader metadataReader = getMetadataReader(metadataStorageType, metadataConfig);

            //populate containers metadata offset
            CosmosContainerProperties multiPartitionContainer = getMultiPartitionContainer(this.client);
            ContainersMetadataTopicPartition containersMetadataTopicPartition =
                new ContainersMetadataTopicPartition(databaseName, connectorName);
            ContainersMetadataTopicOffset containersMetadataTopicOffset =
                new ContainersMetadataTopicOffset(Arrays.asList(multiPartitionContainer.getResourceId()));

            this.populateContainersMetadata(metadataStorageType, metadataReader, containersMetadataTopicPartition, containersMetadataTopicOffset);

            MetadataMonitorThread monitorThread =
                new MetadataMonitorThread(
                    "requestTaskReconfigurationOnSplit",
                    cosmosSourceContainersConfig,
                    metadataConfig,
                    sourceConnectorContext,
                    metadataReader,
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
                new FeedRangesMetadataTopicPartition(databaseName, multiPartitionContainer.getResourceId(), connectorName);
            FeedRangesMetadataTopicOffset feedRangesMetadataTopicOffset =
                new FeedRangesMetadataTopicOffset(Arrays.asList(FeedRange.forFullRange()));

            this.populateFeedRangesMetadata(metadataStorageType, metadataReader, feedRangesMetadataTopicPartition, feedRangesMetadataTopicOffset);

            Thread.sleep(5000); // give enough time for the containers query and feedRanges request
            monitorThread.close();

            // for merge, no task reconfiguration is needed
            Mockito.verify(sourceConnectorContext, Mockito.atLeastOnce()).requestTaskReconfiguration();
        } finally {
            if (metadataStorageType == CosmosMetadataStorageType.COSMOS) {
                this.client.getDatabase(databaseName).getContainer(metadataStorageName).delete();
            }
        }
    }

    @Test(groups = { "kafka", "kafka-emulator" }, dataProvider = "metadataStorageTypeParameterProvider", timeOut = TIMEOUT)
    public void requestTaskReconfigurationOnMerge(CosmosMetadataStorageType metadataStorageType) throws InterruptedException {
        String metadataStorageName = "_cosmos.metadata.topic";
        String connectorName = "requestTaskReconfigurationOnMerge";
        try {
            CosmosSourceContainersConfig cosmosSourceContainersConfig =
                new CosmosSourceContainersConfig(
                    databaseName,
                    false,
                    Arrays.asList(singlePartitionContainerName),
                    new ArrayList<String>());
            CosmosMetadataConfig metadataConfig =
                new CosmosMetadataConfig(500, metadataStorageType, metadataStorageName);
            SourceConnectorContext sourceConnectorContext = Mockito.mock(SourceConnectorContext.class);

            IMetadataReader metadataReader = this.getMetadataReader(metadataStorageType, metadataConfig);

            //populate containers metadata offset
            CosmosContainerProperties singlePartitionContainer = getSinglePartitionContainer(this.client);
            ContainersMetadataTopicPartition containersMetadataTopicPartition =
                new ContainersMetadataTopicPartition(connectorName, "requestTaskReconfigurationOnMerged");
            ContainersMetadataTopicOffset containersMetadataTopicOffset =
                new ContainersMetadataTopicOffset(Arrays.asList(singlePartitionContainer.getResourceId()));

            this.populateContainersMetadata(metadataStorageType, metadataReader, containersMetadataTopicPartition, containersMetadataTopicOffset);

            MetadataMonitorThread monitorThread =
                new MetadataMonitorThread(
                    connectorName,
                    cosmosSourceContainersConfig,
                    metadataConfig,
                    sourceConnectorContext,
                    metadataReader,
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

            List<FeedRange> childRanges =
                ImplementationBridgeHelpers
                    .CosmosAsyncContainerHelper
                    .getCosmosAsyncContainerAccessor()
                    .trySplitFeedRange(
                        this.client.getDatabase(databaseName).getContainer(singlePartitionContainer.getId()),
                        FeedRange.forFullRange(),
                        2)
                    .block();

            FeedRangesMetadataTopicPartition feedRangesMetadataTopicPartition =
                new FeedRangesMetadataTopicPartition(databaseName, singlePartitionContainer.getResourceId(), connectorName);
            FeedRangesMetadataTopicOffset feedRangesMetadataTopicOffset =
                new FeedRangesMetadataTopicOffset(
                    childRanges
                        .stream()
                        .collect(Collectors.toList()));

            this.populateFeedRangesMetadata(metadataStorageType, metadataReader, feedRangesMetadataTopicPartition, feedRangesMetadataTopicOffset);

            Thread.sleep(5000); // give enough time for the containers query and feedRanges request
            monitorThread.close();

            // for merge, no task reconfiguration is needed
            Mockito.verify(sourceConnectorContext, Mockito.never()).requestTaskReconfiguration();
        } finally {
            if (metadataStorageType == CosmosMetadataStorageType.COSMOS) {
                this.client.getDatabase(databaseName).getContainer(metadataStorageName).delete();
            }
        }
    }

    private IMetadataReader getMetadataReader(CosmosMetadataStorageType metadataStorageType, CosmosMetadataConfig metadataConfig) {
        IMetadataReader metadataReader = null;
        switch (metadataStorageType) {
            case KAFKA:
                InMemoryStorageReader inMemoryStorageReader = new InMemoryStorageReader();
                metadataReader = new MetadataKafkaStorageManager(inMemoryStorageReader);
                break;
            case COSMOS:
                // pre-create metadata container
                this.client.getDatabase(databaseName)
                    .createContainerIfNotExists(metadataConfig.getStorageName(), "/id")
                    .block();
                CosmosAsyncContainer metadataContainer = this.client.getDatabase(databaseName).getContainer(metadataConfig.getStorageName());
                metadataReader = new MetadataCosmosStorageManager(metadataContainer);
                break;
            default:
                fail("Cosmos metadata storage type " + metadataStorageType + " is not supported");
        }

        return metadataReader;
    }

    private void populateContainersMetadata(
        CosmosMetadataStorageType metadataStorageType,
        IMetadataReader metadataReader,
        ContainersMetadataTopicPartition containersMetadataTopicPartition,
        ContainersMetadataTopicOffset containersMetadataTopicOffset) {
        switch (metadataStorageType) {
            case KAFKA:
                Map<Map<String, Object>, Map<String, Object>> offsetMap = new HashMap<>();
                offsetMap.put(
                    ContainersMetadataTopicPartition.toMap(containersMetadataTopicPartition),
                    ContainersMetadataTopicOffset.toMap(containersMetadataTopicOffset));

                MetadataKafkaStorageManager kafkaStorageManager = (MetadataKafkaStorageManager) metadataReader;
                ((InMemoryStorageReader)kafkaStorageManager.getOffsetStorageReader()).populateOffset(offsetMap);
                break;
            case COSMOS:
                MetadataCosmosStorageManager cosmosStorageManager = (MetadataCosmosStorageManager) metadataReader;
                cosmosStorageManager.createContainersMetadataItem(
                    containersMetadataTopicPartition,
                    containersMetadataTopicOffset);
                break;
            default:
                fail("Cosmos metadata storage type " + metadataStorageType + " is not supported");
        }
    }

    private void populateFeedRangesMetadata(
        CosmosMetadataStorageType metadataStorageType,
        IMetadataReader metadataReader,
        FeedRangesMetadataTopicPartition feedRangesMetadataTopicPartition,
        FeedRangesMetadataTopicOffset feedRangesMetadataTopicOffset) {
        switch (metadataStorageType) {
            case KAFKA:
                Map<Map<String, Object>, Map<String, Object>> feedRangesOffSetMap = new HashMap<>();
                feedRangesOffSetMap.put(
                    FeedRangesMetadataTopicPartition.toMap(feedRangesMetadataTopicPartition),
                    FeedRangesMetadataTopicOffset.toMap(feedRangesMetadataTopicOffset));

                MetadataKafkaStorageManager kafkaStorageManager = (MetadataKafkaStorageManager) metadataReader;
                ((InMemoryStorageReader)kafkaStorageManager.getOffsetStorageReader()).populateOffset(feedRangesOffSetMap);
                break;
            case COSMOS:
                MetadataCosmosStorageManager cosmosStorageManager = (MetadataCosmosStorageManager) metadataReader;
                cosmosStorageManager.createFeedRangesMetadataItem(
                    feedRangesMetadataTopicPartition,
                    feedRangesMetadataTopicOffset);
                break;
            default:
                fail("Cosmos metadata storage type " + metadataStorageType + " is not supported");
        }
    }
}
