// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation.source;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.kafka.connect.KafkaCosmosTestConfigurations;
import com.azure.cosmos.kafka.connect.KafkaCosmosTestSuiteBase;
import com.azure.cosmos.kafka.connect.TestItem;
import com.azure.cosmos.kafka.connect.implementation.CosmosClientCache;
import com.azure.cosmos.kafka.connect.implementation.CosmosClientCacheItem;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedRange;
import com.azure.cosmos.models.ThroughputProperties;
import com.azure.cosmos.models.ThroughputResponse;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.source.SourceRecord;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class CosmosSourceTaskTest extends KafkaCosmosTestSuiteBase {
    private final int CONTAINER_THROUGHPUT_FOR_SPLIT = 10100;

    @Test(groups = { "kafka" }, timeOut = 60 * TIMEOUT)
    public void poll() throws InterruptedException {
        String testContainerName = "KafkaCosmosTestPoll-" + UUID.randomUUID();
        String connectorName = "kafka-test-poll";
        Map<String, String> sourceConfigMap = new HashMap<>();
        sourceConfigMap.put("name", connectorName);
        sourceConfigMap.put("azure.cosmos.account.endpoint", KafkaCosmosTestConfigurations.HOST);
        sourceConfigMap.put("azure.cosmos.account.key", KafkaCosmosTestConfigurations.MASTER_KEY);
        sourceConfigMap.put("azure.cosmos.source.database.name", databaseName);
        List<String> containersIncludedList = Arrays.asList(testContainerName);
        sourceConfigMap.put("azure.cosmos.source.containers.includedList", containersIncludedList.toString());
        sourceConfigMap.put("azure.cosmos.source.task.id", UUID.randomUUID().toString());

        CosmosSourceConfig sourceConfig = new CosmosSourceConfig(sourceConfigMap);
        CosmosClientCacheItem clientItem =
            CosmosClientCache.getCosmosClient(
                sourceConfig.getAccountConfig(),
                "poll");

        // create a new container as we are going to trigger split as well, isolate the possible impact for other tests
        CosmosContainerProperties testContainer =
            clientItem
                .getClient()
                .getDatabase(databaseName)
                .createContainer(testContainerName, "/id")
                .block()
                .getProperties();

        try {
            Map<String, String> taskConfigMap = sourceConfig.originalsStrings();

            // define metadata task
            List<FeedRange> feedRanges =
                clientItem
                    .getClient()
                    .getDatabase(databaseName)
                    .getContainer(testContainerName)
                    .getFeedRanges()
                    .block();
            assertThat(feedRanges.size()).isEqualTo(1);

            Map<String, List<FeedRange>> containersEffectiveRangesMap = new HashMap<>();
            containersEffectiveRangesMap.put(testContainer.getResourceId(), Arrays.asList(FeedRange.forFullRange()));
            MetadataTaskUnit metadataTaskUnit = new MetadataTaskUnit(
                connectorName,
                databaseName,
                Arrays.asList(testContainer.getResourceId()),
                containersEffectiveRangesMap,
                testContainerName,
                CosmosMetadataStorageType.KAFKA);
            taskConfigMap.putAll(CosmosSourceTaskConfig.getMetadataTaskUnitConfigMap(metadataTaskUnit));

            // define feedRanges task
            FeedRangeTaskUnit feedRangeTaskUnit = new FeedRangeTaskUnit(
                databaseName,
                testContainerName,
                testContainer.getResourceId(),
                FeedRange.forFullRange(),
                null,
                testContainerName);
            taskConfigMap.putAll(CosmosSourceTaskConfig.getFeedRangeTaskUnitsConfigMap(Arrays.asList(feedRangeTaskUnit)));

            CosmosSourceTask sourceTask = new CosmosSourceTask();
            sourceTask.start(taskConfigMap);

            // first creating few items in the container
            List<TestItem> createdItems =
                this.createItems(clientItem.getClient(), databaseName, testContainerName, 10);

            List<SourceRecord> sourceRecords = sourceTask.poll();
            // Since there are metadata task unit being defined, we expected to get the metadata records first.
            validateMetadataRecords(sourceRecords, metadataTaskUnit);

            sourceRecords = sourceTask.poll();
            validateFeedRangeRecords(sourceRecords, createdItems);

            logger.info("Testing split...");
            // trigger split
            ThroughputResponse throughputResponse =
                clientItem
                    .getClient()
                    .getDatabase(databaseName)
                    .getContainer(testContainerName)
                    .replaceThroughput(ThroughputProperties.createManualThroughput(CONTAINER_THROUGHPUT_FOR_SPLIT))
                    .block();

            // Wait for the throughput update to complete so that we get the partition split
            while (true) {
                assert throughputResponse != null;
                if (!throughputResponse.isReplacePending()) {
                    break;
                }
                logger.info("Waiting for split to complete");
                Thread.sleep(10 * 1000);
                throughputResponse =
                    clientItem
                        .getClient()
                        .getDatabase(databaseName)
                        .getContainer(testContainerName)
                        .readThroughput().block();
            }

            createdItems = this.createItems(clientItem.getClient(), databaseName, testContainerName, 10);
            sourceRecords = new ArrayList<>();
            // the first poll will return 0 records as it will be the first time the task detect split happened
            // internally it will create two new feedRange task units
            // so here we will need to poll 3 times to get all newly created items
            for (int i = 0; i < 3; i++) {
                sourceRecords.addAll(sourceTask.poll());
            }
            validateFeedRangeRecords(sourceRecords, createdItems);
        } finally {
            if (clientItem != null) {
                clientItem.getClient().getDatabase(databaseName).getContainer(testContainerName).delete().block();
                CosmosClientCache.releaseCosmosClient(clientItem.getClientConfig());
                clientItem.getClient().close();
            }
        }
    }

    @Test(groups = { "kafka" }, timeOut = 60 * TIMEOUT)
    public void poll_splitWhenStartFeedRangeTask() {
        String testContainerName = "KafkaCosmosTestPoll-" + UUID.randomUUID();
        String connectorName = "kafka-test-poll";
        Map<String, String> sourceConfigMap = new HashMap<>();
        sourceConfigMap.put("name", connectorName);
        sourceConfigMap.put("azure.cosmos.account.endpoint", KafkaCosmosTestConfigurations.HOST);
        sourceConfigMap.put("azure.cosmos.account.key", KafkaCosmosTestConfigurations.MASTER_KEY);
        sourceConfigMap.put("azure.cosmos.source.database.name", databaseName);
        List<String> containersIncludedList = Arrays.asList(testContainerName);
        sourceConfigMap.put("azure.cosmos.source.containers.includedList", containersIncludedList.toString());
        sourceConfigMap.put("azure.cosmos.source.task.id", UUID.randomUUID().toString());

        CosmosSourceConfig sourceConfig = new CosmosSourceConfig(sourceConfigMap);
        CosmosClientCacheItem clientItem =
            CosmosClientCache.getCosmosClient(
                sourceConfig.getAccountConfig(),
                "pollSplitwhenStartup");

        // create a new container with multi-partition
        CosmosContainerProperties testContainer =
            clientItem
                .getClient()
                .getDatabase(databaseName)
                .createContainer(testContainerName, "/id", ThroughputProperties.createManualThroughput(10100))
                .onErrorResume(throwable -> {
                    System.out.println(throwable.getMessage());
                    return Mono.empty();
                })
                .block()
                .getProperties();

        try {
            Map<String, String> taskConfigMap = sourceConfig.originalsStrings();
            // define feedRanges task
            FeedRangeTaskUnit feedRangeTaskUnit = new FeedRangeTaskUnit(
                databaseName,
                testContainerName,
                testContainer.getResourceId(),
                FeedRange.forFullRange(),
                null,
                testContainerName);
            taskConfigMap.putAll(CosmosSourceTaskConfig.getFeedRangeTaskUnitsConfigMap(Arrays.asList(feedRangeTaskUnit)));

            CosmosSourceTask sourceTask = new CosmosSourceTask();
            sourceTask.start(taskConfigMap);

            // first creating few items in the container
            List<TestItem> createdItems =
                this.createItems(clientItem.getClient(), databaseName, testContainerName, 10);

            List<SourceRecord> sourceRecords = new ArrayList<>();
            // the first poll will return 0 records as it will be the first time the task detect split happened
            // internally it will create two new feedRange task units
            // so here we will need to poll 3 times to get all newly created items
            for (int i = 0; i < 3; i++) {
                sourceRecords.addAll(sourceTask.poll());
            }
            validateFeedRangeRecords(sourceRecords, createdItems);
        } finally {
            if (clientItem != null) {
                clientItem.getClient().getDatabase(databaseName).getContainer(testContainerName).delete().block();
                CosmosClientCache.releaseCosmosClient(clientItem.getClientConfig());
                clientItem.getClient().close();
            }
        }
    }
    @Test(groups = { "kafka", "kafka-emulator" }, timeOut = TIMEOUT)
    public void pollWithSpecificFeedRange() {
        // Test only items belong to the feedRange defined in the feedRangeTaskUnit will be returned
        Map<String, String> sourceConfigMap = new HashMap<>();
        sourceConfigMap.put("azure.cosmos.account.endpoint", KafkaCosmosTestConfigurations.HOST);
        sourceConfigMap.put("azure.cosmos.account.key", KafkaCosmosTestConfigurations.MASTER_KEY);
        sourceConfigMap.put("azure.cosmos.source.database.name", databaseName);
        List<String> containersIncludedList = Arrays.asList(multiPartitionContainerName);
        sourceConfigMap.put("azure.cosmos.source.containers.includedList", containersIncludedList.toString());
        sourceConfigMap.put("azure.cosmos.source.task.id", UUID.randomUUID().toString());

        CosmosSourceConfig sourceConfig = new CosmosSourceConfig(sourceConfigMap);
        CosmosClientCacheItem clientItem =
            CosmosClientCache.getCosmosClient(
                sourceConfig.getAccountConfig(),
                "pollWithSpecificFeedRange");

        try {
            Map<String, String> taskConfigMap = sourceConfig.originalsStrings();

            // define metadata task
            List<FeedRange> feedRanges =
                clientItem
                    .getClient()
                    .getDatabase(databaseName)
                    .getContainer(multiPartitionContainerName)
                    .getFeedRanges()
                    .block();
            CosmosContainerProperties multiPartitionContainer = getMultiPartitionContainer(clientItem.getClient());
            assertThat(feedRanges.size()).isGreaterThan(1);

            // define feedRanges task
            FeedRangeTaskUnit feedRangeTaskUnit = new FeedRangeTaskUnit(
                databaseName,
                multiPartitionContainer.getId(),
                multiPartitionContainer.getResourceId(),
                feedRanges.get(0),
                null,
                multiPartitionContainer.getId());
            taskConfigMap.putAll(CosmosSourceTaskConfig.getFeedRangeTaskUnitsConfigMap(Arrays.asList(feedRangeTaskUnit)));

            CosmosSourceTask sourceTask = new CosmosSourceTask();
            sourceTask.start(taskConfigMap);

            // first creating few items in the container
            this.createItems(clientItem.getClient(), databaseName, multiPartitionContainer.getId(), 10);

            List<SourceRecord> sourceRecords = new ArrayList<>();
            for (int i = 0; i < 3; i++) { // poll few times
                sourceRecords.addAll(sourceTask.poll());
            }

            // get all items belong to feed range 0
            CosmosQueryRequestOptions queryRequestOptions = new CosmosQueryRequestOptions();
            queryRequestOptions.setFeedRange(feedRanges.get(0));
            List<TestItem> expectedItems = clientItem
                .getClient()
                .getDatabase(databaseName)
                .getContainer(multiPartitionContainer.getId())
                .queryItems("select * from c", queryRequestOptions, TestItem.class)
                .byPage()
                .flatMapIterable(feedResponse -> feedResponse.getResults())
                .collectList()
                .block();

            validateFeedRangeRecords(sourceRecords, expectedItems);
        } finally {
            if (clientItem != null) {
                // clean up containers
                cleanUpContainer(clientItem.getClient(), databaseName, multiPartitionContainerName);
                CosmosClientCache.releaseCosmosClient(clientItem.getClientConfig());
                clientItem.getClient().close();
            }
        }
    }

    @Test(groups = { "kafka", "kafka-emulator" }, timeOut = TIMEOUT)
    public void pollWithThroughputControl() {
        // Test only items belong to the feedRange defined in the feedRangeTaskUnit will be returned
        String throughputControlContainerName = "throughputControlContainer-" + UUID.randomUUID();

        Map<String, String> sourceConfigMap = new HashMap<>();
        sourceConfigMap.put("azure.cosmos.account.endpoint", KafkaCosmosTestConfigurations.HOST);
        sourceConfigMap.put("azure.cosmos.account.key", KafkaCosmosTestConfigurations.MASTER_KEY);
        sourceConfigMap.put("azure.cosmos.source.database.name", databaseName);
        List<String> containersIncludedList = Arrays.asList(singlePartitionContainerName);
        sourceConfigMap.put("azure.cosmos.source.containers.includedList", containersIncludedList.toString());
        sourceConfigMap.put("azure.cosmos.throughputControl.enabled", "true");
        sourceConfigMap.put("azure.cosmos.throughputControl.group.name", "pollWithThroughputControl-" + UUID.randomUUID());
        sourceConfigMap.put("azure.cosmos.throughputControl.targetThroughput", "100");
        sourceConfigMap.put("azure.cosmos.throughputControl.globalControl.database.name", databaseName);
        sourceConfigMap.put("azure.cosmos.throughputControl.globalControl.container.name", throughputControlContainerName);
        sourceConfigMap.put("azure.cosmos.source.task.id", UUID.randomUUID().toString());

        CosmosSourceConfig sourceConfig = new CosmosSourceConfig(sourceConfigMap);
        CosmosClientCacheItem clientCacheItem =
            CosmosClientCache.getCosmosClient(
                sourceConfig.getAccountConfig(),
                "pollWithThroughputControl");
        CosmosAsyncContainer throughputControlContainer =
            clientCacheItem.getClient().getDatabase(databaseName).getContainer(throughputControlContainerName);
        CosmosContainerProperties singlePartitionContainer = getSinglePartitionContainer(clientCacheItem.getClient());
        try {
            // create throughput control container
            clientCacheItem
                .getClient()
                .getDatabase(databaseName)
                .createContainerIfNotExists(throughputControlContainerName, "/groupId", ThroughputProperties.createManualThroughput(400))
                .block();

            Map<String, String> taskConfigMap = sourceConfig.originalsStrings();

            // define feedRanges task
            FeedRangeTaskUnit feedRangeTaskUnit = new FeedRangeTaskUnit(
                databaseName,
                singlePartitionContainer.getId(),
                singlePartitionContainer.getResourceId(),
                FeedRange.forFullRange(),
                null,
                singlePartitionContainer.getId());
            taskConfigMap.putAll(CosmosSourceTaskConfig.getFeedRangeTaskUnitsConfigMap(Arrays.asList(feedRangeTaskUnit)));

            CosmosSourceTask sourceTask = new CosmosSourceTask();
            sourceTask.start(taskConfigMap);

            // first creating few items in the container
            List<TestItem> createdItems =
                this.createItems(clientCacheItem.getClient(), databaseName, singlePartitionContainer.getId(), 10);

            List<SourceRecord> sourceRecords = new ArrayList<>();
            for (int i = 0; i < 3; i++) { // poll few times
                sourceRecords.addAll(sourceTask.poll());
            }

            validateFeedRangeRecords(sourceRecords, createdItems);
        } finally {
            if (clientCacheItem != null) {
                // delete throughput control containers
                throughputControlContainer
                    .delete()
                    .onErrorResume(throwable -> {
                        logger.warn("Delete throughput control container {} failed", throughputControlContainer.getId(), throwable);
                        return Mono.empty();
                    })
                    .block();

                // clean up containers
                cleanUpContainer(clientCacheItem.getClient(), databaseName, singlePartitionContainer.getId());
                CosmosClientCache.releaseCosmosClient(clientCacheItem.getClientConfig());
                clientCacheItem.getClient().close();
            }
        }
    }

    @Test(groups = { "kafka-emulator" }, timeOut = TIMEOUT)
    public void pollWithAllVersionsAndDeletes() throws InterruptedException {
        Map<String, String> sourceConfigMap = new HashMap<>();
        sourceConfigMap.put("azure.cosmos.account.endpoint", KafkaCosmosTestConfigurations.HOST);
        sourceConfigMap.put("azure.cosmos.account.key", KafkaCosmosTestConfigurations.MASTER_KEY);
        sourceConfigMap.put("azure.cosmos.source.database.name", databaseName);
        List<String> containersIncludedList = Arrays.asList(singlePartitionContainerName);
        sourceConfigMap.put("azure.cosmos.source.containers.includedList", containersIncludedList.toString());
        sourceConfigMap.put("azure.cosmos.source.changeFeed.mode", CosmosChangeFeedMode.ALL_VERSION_AND_DELETES.getName());
        sourceConfigMap.put("azure.cosmos.source.changeFeed.startFrom", "Now");
        sourceConfigMap.put("azure.cosmos.source.task.id", UUID.randomUUID().toString());

        CosmosSourceConfig sourceConfig = new CosmosSourceConfig(sourceConfigMap);
        CosmosClientCacheItem clientCacheItem =
            CosmosClientCache.getCosmosClient(
                sourceConfig.getAccountConfig(),
                "pollWithAllVersionsAndDeletes");

        CosmosContainerProperties singlePartitionContainer = getSinglePartitionContainer(clientCacheItem.getClient());
        try {
            Map<String, String> taskConfigMap = sourceConfig.originalsStrings();

            // define feedRanges task
            FeedRangeTaskUnit feedRangeTaskUnit = new FeedRangeTaskUnit(
                databaseName,
                singlePartitionContainer.getId(),
                singlePartitionContainer.getResourceId(),
                FeedRange.forFullRange(),
                null,
                singlePartitionContainer.getId());
            taskConfigMap.putAll(CosmosSourceTaskConfig.getFeedRangeTaskUnitsConfigMap(Arrays.asList(feedRangeTaskUnit)));

            CosmosSourceTask sourceTask = new CosmosSourceTask();
            sourceTask.start(taskConfigMap);
            sourceTask.poll();

            CosmosAsyncContainer container =
                clientCacheItem
                    .getClient()
                    .getDatabase(databaseName)
                    .getContainer(singlePartitionContainerName);

            // create item
            TestItem testItem = TestItem.createNewItem();
            container.createItem(testItem).block();
            // update item
            testItem.setProp(UUID.randomUUID().toString());
            container.upsertItem(testItem).block();
            // delete item
            container.deleteItem(testItem, new CosmosItemRequestOptions()).block();

            Thread.sleep(500);
            List<SourceRecord> sourceRecords = new ArrayList<>();
            for (int i = 0; i < 10; i++) { // poll few times
                sourceRecords.addAll(sourceTask.poll());
            }

            assertThat(sourceRecords.size()).isEqualTo(3);
        } finally {
            if (clientCacheItem != null) {
                // clean up containers
                cleanUpContainer(clientCacheItem.getClient(), databaseName, singlePartitionContainer.getId());
                CosmosClientCache.releaseCosmosClient(clientCacheItem.getClientConfig());
                clientCacheItem.getClient().close();
            }
        }
    }

    private void validateMetadataRecords(
        List<SourceRecord> sourceRecords,
        MetadataTaskUnit metadataTaskUnit) {
        // one containers metadata
        // one feedRanges metadata record for each container
        assertThat(sourceRecords.size()).isEqualTo(metadataTaskUnit.getContainerRids().size() + 1);

        ContainersMetadataTopicPartition containersMetadataTopicPartition =
            new ContainersMetadataTopicPartition(metadataTaskUnit.getDatabaseName(), metadataTaskUnit.getConnectorName());
        ContainersMetadataTopicOffset containersMetadataTopicOffset =
            new ContainersMetadataTopicOffset(metadataTaskUnit.getContainerRids());
        assertThat(sourceRecords.get(0).sourcePartition()).isEqualTo(ContainersMetadataTopicPartition.toMap(containersMetadataTopicPartition));
        assertThat(sourceRecords.get(0).sourceOffset()).isEqualTo(ContainersMetadataTopicOffset.toMap(containersMetadataTopicOffset));

        for (int i = 0; i < metadataTaskUnit.getContainerRids().size(); i++) {
            String containerRid = metadataTaskUnit.getContainerRids().get(i);
            SourceRecord sourceRecord = sourceRecords.get(i + 1);
            List<FeedRange> containerFeedRanges =
                metadataTaskUnit.getContainersEffectiveRangesMap().get(containerRid);
            assertThat(containerFeedRanges).isNotNull();

            FeedRangesMetadataTopicPartition feedRangesMetadataTopicPartition =
                new FeedRangesMetadataTopicPartition(metadataTaskUnit.getDatabaseName(), containerRid, metadataTaskUnit.getConnectorName());
            FeedRangesMetadataTopicOffset feedRangesMetadataTopicOffset =
                new FeedRangesMetadataTopicOffset(containerFeedRanges);
            assertThat(sourceRecord.sourcePartition()).isEqualTo(FeedRangesMetadataTopicPartition.toMap(feedRangesMetadataTopicPartition));
            assertThat(sourceRecord.sourceOffset()).isEqualTo(FeedRangesMetadataTopicOffset.toMap(feedRangesMetadataTopicOffset));
        }
    }

    private void validateFeedRangeRecords(List<SourceRecord> sourceRecords, List<TestItem> expectedItems) {
        List<String> idsReceived =
            sourceRecords
                .stream()
                .map(sourceRecord -> ((Struct)sourceRecord.value()).get("id").toString())
                .collect(Collectors.toList());
        List<String> expectedIds =
            expectedItems
                .stream()
                .map(testItem -> testItem.getId())
                .collect(Collectors.toList());
        assertThat(idsReceived.size()).isEqualTo(expectedItems.size());
        assertThat(idsReceived.containsAll(expectedIds));
    }

    private List<TestItem> createItems(
        CosmosAsyncClient client,
        String databaseName,
        String containerName,
        int numberOfItems) {

        List<TestItem> testItems = new ArrayList<>();
        CosmosAsyncContainer container = client.getDatabase(databaseName).getContainer(containerName);
        for (int i = 0; i < numberOfItems; i++) {
            TestItem testItem = TestItem.createNewItem();
            container.createItem(testItem).block();
            testItems.add(testItem);
        }

        return testItems;
    }
}
