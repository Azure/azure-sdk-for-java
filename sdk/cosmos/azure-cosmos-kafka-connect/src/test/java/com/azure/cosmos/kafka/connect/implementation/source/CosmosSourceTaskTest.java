// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation.source;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.implementation.feedranges.FeedRangeEpkImpl;
import com.azure.cosmos.implementation.routing.Range;
import com.azure.cosmos.kafka.connect.KafkaCosmosTestSuiteBase;
import com.azure.cosmos.kafka.connect.TestItem;
import com.azure.cosmos.kafka.connect.implementation.CosmosClientStore;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.FeedRange;
import com.azure.cosmos.models.ThroughputProperties;
import com.azure.cosmos.models.ThroughputResponse;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.source.SourceRecord;
import org.testng.annotations.Test;

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

    @Test(groups = {"fast"}, timeOut = 10 * TIMEOUT)
    public void poll() throws InterruptedException {
        String testContainerName = "KafkaCosmosTestPoll-" + UUID.randomUUID();
        Map<String, String> sourceConfigMap = new HashMap<>();
        sourceConfigMap.put("kafka.connect.cosmos.accountEndpoint", TestConfigurations.HOST);
        sourceConfigMap.put("kafka.connect.cosmos.accountKey", TestConfigurations.MASTER_KEY);
        sourceConfigMap.put("kafka.connect.cosmos.source.database.name", databaseName);
        List<String> containersIncludedList = Arrays.asList(testContainerName);
        sourceConfigMap.put("kafka.connect.cosmos.source.containers.includedList", containersIncludedList.toString());

        CosmosSourceConfig sourceConfig = new CosmosSourceConfig(sourceConfigMap);
        CosmosAsyncClient client = CosmosClientStore.getCosmosClient(sourceConfig.getAccountConfig());

        // create a new container as we are going to trigger split as well, isolate the possible impact for other tests
        CosmosContainerProperties testContainer =
            client
                .getDatabase(databaseName)
                .createContainer(testContainerName, "/id")
                .block()
                .getProperties();

        try {
            Map<String, String> taskConfigMap = sourceConfig.originalsStrings();

            // define metadata task
            List<FeedRange> feedRanges =
                client.getDatabase(databaseName).getContainer(testContainerName).getFeedRanges().block();
            assertThat(feedRanges.size()).isEqualTo(1);

            Map<String, List<Range<String>>> containersEffectiveRangesMap = new HashMap<>();
            containersEffectiveRangesMap.put(testContainer.getResourceId(), Arrays.asList(FeedRangeEpkImpl.forFullRange().getRange()));
            MetadataTaskUnit metadataTaskUnit = new MetadataTaskUnit(
                databaseName,
                Arrays.asList(testContainer.getResourceId()),
                containersEffectiveRangesMap,
                testContainerName);
            taskConfigMap.putAll(CosmosSourceTaskConfig.getMetadataTaskUnitConfigMap(metadataTaskUnit));

            // define feedRanges task
            FeedRangeTaskUnit feedRangeTaskUnit = new FeedRangeTaskUnit(
                databaseName,
                testContainerName,
                testContainer.getResourceId(),
                FeedRangeEpkImpl.forFullRange().getRange(),
                null,
                testContainerName);
            taskConfigMap.putAll(CosmosSourceTaskConfig.getFeedRangeTaskUnitsConfigMap(Arrays.asList(feedRangeTaskUnit)));

            CosmosSourceTask sourceTask = new CosmosSourceTask();
            sourceTask.start(taskConfigMap);

            // first creating few items in the container
            List<TestItem> createdItems = this.createItems(client, databaseName, testContainerName, 10);

            List<SourceRecord> sourceRecords = sourceTask.poll();
            // Since there are metadata task unit being defined, we expected to get the metadata records first.
            validateMetadataRecords(sourceRecords, metadataTaskUnit);

            sourceRecords = sourceTask.poll();
            validateFeedRangeRecords(sourceRecords, createdItems);

            logger.info("Testing split...");
            // trigger split
            ThroughputResponse throughputResponse =
                client
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
                throughputResponse = client.getDatabase(databaseName).getContainer(testContainerName).readThroughput().block();
            }

            createdItems = this.createItems(client, databaseName, testContainerName, 10);
            sourceRecords = new ArrayList<>();
            // the first poll will return 0 records as it will be the first time the task detect split happened
            // internally it will create two new feedRange task units
            // so here we will need to poll 3 times to get all newly created items
            for (int i = 0; i < 3; i++) {
                sourceRecords.addAll(sourceTask.poll());
            }
            validateFeedRangeRecords(sourceRecords, createdItems);
        } finally {
            if (client != null) {
                client.getDatabase(databaseName).getContainer(testContainerName).delete().block();
                client.close();
            }
        }
    }

    private void validateMetadataRecords(List<SourceRecord> sourceRecords, MetadataTaskUnit metadataTaskUnit) {
        // one containers metadata
        // one feedRanges metadata record for each container
        assertThat(sourceRecords.size()).isEqualTo(metadataTaskUnit.getContainerRids().size() + 1);

        ContainersMetadataTopicPartition containersMetadataTopicPartition =
            new ContainersMetadataTopicPartition(metadataTaskUnit.getDatabaseName());
        ContainersMetadataTopicOffset containersMetadataTopicOffset =
            new ContainersMetadataTopicOffset(metadataTaskUnit.getContainerRids());
        assertThat(sourceRecords.get(0).sourcePartition()).isEqualTo(ContainersMetadataTopicPartition.toMap(containersMetadataTopicPartition));
        assertThat(sourceRecords.get(0).sourceOffset()).isEqualTo(ContainersMetadataTopicOffset.toMap(containersMetadataTopicOffset));

        for (int i = 0; i < metadataTaskUnit.getContainerRids().size(); i++) {
            String containerRid = metadataTaskUnit.getContainerRids().get(i);
            SourceRecord sourceRecord = sourceRecords.get(i + 1);
            List<Range<String>> containerFeedRanges =
                metadataTaskUnit.getContainersEffectiveRangesMap().get(containerRid);
            assertThat(containerFeedRanges).isNotNull();

            FeedRangesMetadataTopicPartition feedRangesMetadataTopicPartition =
                new FeedRangesMetadataTopicPartition(metadataTaskUnit.getDatabaseName(), containerRid);
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
