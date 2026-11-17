// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation.source;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.kafka.connect.InMemoryStorageReader;
import com.azure.cosmos.kafka.connect.KafkaCosmosTestConfigurations;
import com.azure.cosmos.kafka.connect.KafkaCosmosTestSuiteBase;
import com.azure.cosmos.kafka.connect.TestItem;
import com.azure.cosmos.kafka.connect.implementation.CosmosClientCache;
import com.azure.cosmos.kafka.connect.implementation.CosmosClientCacheItem;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.FeedRange;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.models.ThroughputProperties;
import com.azure.cosmos.models.ThroughputResponse;
import com.azure.cosmos.test.faultinjection.CosmosFaultInjectionHelper;
import com.azure.cosmos.test.faultinjection.FaultInjectionConditionBuilder;
import com.azure.cosmos.test.faultinjection.FaultInjectionOperationType;
import com.azure.cosmos.test.faultinjection.FaultInjectionResultBuilders;
import com.azure.cosmos.test.faultinjection.FaultInjectionRule;
import com.azure.cosmos.test.faultinjection.FaultInjectionRuleBuilder;
import com.azure.cosmos.test.faultinjection.FaultInjectionServerErrorType;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.source.SourceTaskContext;
import org.apache.kafka.connect.storage.OffsetStorageReader;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class CosmosSourceTaskTest extends KafkaCosmosTestSuiteBase {
    private final int CONTAINER_THROUGHPUT_FOR_SPLIT = 10100;
    private static final long NO_CHANGES_POLL_MAX_MS = 500;
    private static final long RECORDS_POLL_TIMEOUT_MS = 30_000;

    @Test(groups = "unit")
    public void pollDelayOnlyAppliesWhenChangeFeedHasNoChanges() {
        FeedResponse<?> noChangesResponse =
            ModelBridgeInternal.toFeedResponsePage(Collections.emptyList(), Collections.emptyMap(), true);
        assertThat(CosmosSourceTask.getPollDelayInMs(
            ModelBridgeInternal.getNoChangesFromFeedResponse(noChangesResponse),
            100)).isEqualTo(100);

        FeedResponse<?> changedResponse =
            ModelBridgeInternal.toFeedResponsePage(Collections.emptyList(), Collections.emptyMap(), false);
        assertThat(CosmosSourceTask.getPollDelayInMs(
            ModelBridgeInternal.getNoChangesFromFeedResponse(changedResponse),
            100)).isEqualTo(0);
    }

    @Test(groups = "unit")
    public void pollDelayDefersIdleRangeWithoutBlockingReadyRanges() {
        FeedRangeTaskUnit idleFeedRange = new FeedRangeTaskUnit(
            databaseName,
            multiPartitionContainerName,
            "containerRid",
            FeedRange.forFullRange(),
            null,
            multiPartitionContainerName);
        FeedRangeTaskUnit activeFeedRange = new FeedRangeTaskUnit(
            databaseName,
            multiPartitionContainerName,
            "containerRid",
            FeedRange.forFullRange(),
            null,
            multiPartitionContainerName);

        PriorityQueue<CosmosSourceTask.ScheduledTaskUnit> queue = new PriorityQueue<>();
        queue.add(new CosmosSourceTask.ScheduledTaskUnit(idleFeedRange, 1000));
        queue.add(new CosmosSourceTask.ScheduledTaskUnit(activeFeedRange, 0));
        assertThat(queue.poll().getTaskUnit()).isSameAs(activeFeedRange);
        assertThat(queue.peek().getDelayInMs()).isGreaterThan(0);
    }

    @Test(groups = { "kafka" }, timeOut = 60 * TIMEOUT)
    public void pollDelaysFeedRangeAfterNoChanges() throws Exception {
        int pollDelayInMs = 3000;
        String testContainerName = "KafkaCosmosTestPollDelay-" + UUID.randomUUID();
        Map<String, String> sourceConfigMap = new HashMap<>();
        sourceConfigMap.put("azure.cosmos.account.endpoint", KafkaCosmosTestConfigurations.HOST);
        sourceConfigMap.put("azure.cosmos.account.key", KafkaCosmosTestConfigurations.MASTER_KEY);
        sourceConfigMap.put("azure.cosmos.source.database.name", databaseName);
        sourceConfigMap.put("azure.cosmos.source.containers.includedList", Arrays.asList(testContainerName).toString());
        sourceConfigMap.put("azure.cosmos.source.task.id", UUID.randomUUID().toString());
        sourceConfigMap.put("azure.cosmos.source.changeFeed.poll.delay.ms", Integer.toString(pollDelayInMs));

        CosmosSourceConfig sourceConfig = new CosmosSourceConfig(sourceConfigMap);
        CosmosClientCacheItem clientItem =
            CosmosClientCache.getCosmosClient(sourceConfig.getAccountConfig(), "pollWithDelayAfterNoChanges");
        CosmosContainerProperties testContainer = null;
        CosmosSourceTask sourceTask = null;

        try {
            testContainer = clientItem
                .getClient()
                .getDatabase(databaseName)
                .createContainer(testContainerName, "/id")
                .block()
                .getProperties();

            Map<String, String> taskConfigMap = sourceConfig.originalsStrings();
            FeedRangeTaskUnit feedRangeTaskUnit = new FeedRangeTaskUnit(
                databaseName,
                testContainerName,
                testContainer.getResourceId(),
                FeedRange.forFullRange(),
                null,
                testContainerName);
            taskConfigMap.putAll(
                CosmosSourceTaskConfig.getFeedRangeTaskUnitsConfigMap(Collections.singletonList(feedRangeTaskUnit)));

            sourceTask = new CosmosSourceTask();
            sourceTask.initialize(new TestSourceTaskContext(taskConfigMap));
            sourceTask.start(taskConfigMap);

            TestItem initialItem = TestItem.createNewItem();
            clientItem.getClient().getDatabase(databaseName).getContainer(testContainerName).createItem(initialItem).block();
            validateFeedRangeRecords(pollUntilRecords(sourceTask, TIMEOUT), Collections.singletonList(initialItem));

            long noChangesProcessedAtNanos = pollUntilNoChangesProcessed(sourceTask, TIMEOUT);

            TestItem laterItem = TestItem.createNewItem();
            clientItem.getClient().getDatabase(databaseName).getContainer(testContainerName).createItem(laterItem).block();
            List<SourceRecord> sourceRecords = pollUntilRecords(sourceTask, pollDelayInMs + TIMEOUT);
            long elapsedInMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - noChangesProcessedAtNanos);

            // The delay began within NO_CHANGES_POLL_MAX_MS before the no-change poll returned.
            assertThat(elapsedInMs).isGreaterThanOrEqualTo(pollDelayInMs - NO_CHANGES_POLL_MAX_MS);
            validateFeedRangeRecords(sourceRecords, Collections.singletonList(laterItem));
        } finally {
            if (sourceTask != null) {
                sourceTask.stop();
            }
            if (testContainer != null) {
                clientItem.getClient().getDatabase(databaseName).getContainer(testContainerName).delete().block();
            }
            CosmosClientCache.releaseCosmosClient(clientItem.getClientConfig());
            clientItem.getClient().close();
        }
    }

    @Test(groups = { "kafka" }, timeOut = 60 * TIMEOUT)
    public void pollAndStopStayResponsiveDuringConfiguredDelay() throws Exception {
        int pollDelayInMs = 60000;
        String testContainerName = "KafkaCosmosTestPollStop-" + UUID.randomUUID();
        Map<String, String> sourceConfigMap = new HashMap<>();
        sourceConfigMap.put("azure.cosmos.account.endpoint", KafkaCosmosTestConfigurations.HOST);
        sourceConfigMap.put("azure.cosmos.account.key", KafkaCosmosTestConfigurations.MASTER_KEY);
        sourceConfigMap.put("azure.cosmos.source.database.name", databaseName);
        sourceConfigMap.put("azure.cosmos.source.containers.includedList", Arrays.asList(testContainerName).toString());
        sourceConfigMap.put("azure.cosmos.source.task.id", UUID.randomUUID().toString());
        sourceConfigMap.put("azure.cosmos.source.changeFeed.poll.delay.ms", Integer.toString(pollDelayInMs));

        CosmosSourceConfig sourceConfig = new CosmosSourceConfig(sourceConfigMap);
        CosmosClientCacheItem clientItem =
            CosmosClientCache.getCosmosClient(sourceConfig.getAccountConfig(), "pollStopDuringDelay");
        CosmosContainerProperties testContainer = null;
        CosmosSourceTask sourceTask = null;

        try {
            testContainer = clientItem
                .getClient()
                .getDatabase(databaseName)
                .createContainer(testContainerName, "/id")
                .block()
                .getProperties();

            Map<String, String> taskConfigMap = sourceConfig.originalsStrings();
            FeedRangeTaskUnit feedRangeTaskUnit = new FeedRangeTaskUnit(
                databaseName,
                testContainerName,
                testContainer.getResourceId(),
                FeedRange.forFullRange(),
                null,
                testContainerName);
            taskConfigMap.putAll(
                CosmosSourceTaskConfig.getFeedRangeTaskUnitsConfigMap(Collections.singletonList(feedRangeTaskUnit)));

            sourceTask = new CosmosSourceTask();
            sourceTask.initialize(new TestSourceTaskContext(taskConfigMap));
            sourceTask.start(taskConfigMap);

            TestItem initialItem = TestItem.createNewItem();
            clientItem.getClient().getDatabase(databaseName).getContainer(testContainerName).createItem(initialItem).block();
            validateFeedRangeRecords(pollUntilRecords(sourceTask, TIMEOUT), Collections.singletonList(initialItem));
            assertThat(sourceTask.poll().size()).isEqualTo(0);

            // The next poll is held back by the configured delay but must not block the caller for it.
            long delayedPollStartedAtNanos = System.nanoTime();
            assertThat(sourceTask.poll().size()).isEqualTo(0);
            assertThat(TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - delayedPollStartedAtNanos)).isLessThan(5_000);

            long stopStartedAtNanos = System.nanoTime();
            sourceTask.stop();
            assertThat(TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - stopStartedAtNanos)).isLessThan(5_000);
            assertThat(sourceTask.poll().size()).isEqualTo(0);
        } finally {
            if (sourceTask != null) {
                sourceTask.stop();
            }
            if (testContainer != null) {
                clientItem.getClient().getDatabase(databaseName).getContainer(testContainerName).delete().block();
            }
            CosmosClientCache.releaseCosmosClient(clientItem.getClientConfig());
            clientItem.getClient().close();
        }
    }

    // A poll that returns empty well inside the 1s poll bound means the no-change response was processed rather than timed out.
    private static long pollUntilNoChangesProcessed(CosmosSourceTask sourceTask, long timeoutInMs) {
        long deadlineNanos = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(timeoutInMs);
        while (System.nanoTime() < deadlineNanos) {
            long pollStartedAtNanos = System.nanoTime();
            List<SourceRecord> sourceRecords = sourceTask.poll();
            long returnedAtNanos = System.nanoTime();
            assertThat(sourceRecords.size()).isEqualTo(0);
            if (TimeUnit.NANOSECONDS.toMillis(returnedAtNanos - pollStartedAtNanos) < NO_CHANGES_POLL_MAX_MS) {
                return returnedAtNanos;
            }
        }
        throw new AssertionError("No-change response was not processed within " + timeoutInMs + "ms");
    }

    private static List<SourceRecord> pollUntilRecords(CosmosSourceTask sourceTask, long timeoutInMs) {
        long deadlineNanos = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(timeoutInMs);
        List<SourceRecord> sourceRecords = sourceTask.poll();
        while (sourceRecords.isEmpty() && System.nanoTime() < deadlineNanos) {
            sourceRecords = sourceTask.poll();
        }
        return sourceRecords;
    }

    // poll() returns empty after 1s while the in-flight poll keeps running, so accumulate until all records arrive.
    private static List<SourceRecord> pollUntilRecordCount(CosmosSourceTask sourceTask, int expectedCount, long timeoutInMs) {
        long deadlineNanos = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(timeoutInMs);
        List<SourceRecord> sourceRecords = new ArrayList<>(sourceTask.poll());
        while (sourceRecords.size() < expectedCount && System.nanoTime() < deadlineNanos) {
            sourceRecords.addAll(sourceTask.poll());
        }
        return sourceRecords;
    }

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

        CosmosSourceTask sourceTask = null;
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

            sourceTask = new CosmosSourceTask();
            sourceTask.initialize(new TestSourceTaskContext(taskConfigMap));
            sourceTask.start(taskConfigMap);

            // first creating few items in the container
            List<TestItem> createdItems =
                this.createItems(clientItem.getClient(), databaseName, testContainerName, 10);

            List<SourceRecord> sourceRecords = pollUntilRecords(sourceTask, RECORDS_POLL_TIMEOUT_MS);
            // Since there are metadata task unit being defined, we expected to get the metadata records first.
            validateMetadataRecords(sourceRecords, metadataTaskUnit);

            sourceRecords = pollUntilRecordCount(sourceTask, createdItems.size(), RECORDS_POLL_TIMEOUT_MS);
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
            // the first poll after the split returns 0 records and creates two new feedRange task units
            sourceRecords = pollUntilRecordCount(sourceTask, createdItems.size(), RECORDS_POLL_TIMEOUT_MS);
            validateFeedRangeRecords(sourceRecords, createdItems);
        } finally {
            if (sourceTask != null) {
                sourceTask.stop();
            }
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

        CosmosSourceTask sourceTask = null;
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

            sourceTask = new CosmosSourceTask();
            sourceTask.initialize(new TestSourceTaskContext(taskConfigMap));
            sourceTask.start(taskConfigMap);

            // first creating few items in the container
            List<TestItem> createdItems =
                this.createItems(clientItem.getClient(), databaseName, testContainerName, 10);

            // the first poll detects the split, returns 0 records and creates two new feedRange task units
            List<SourceRecord> sourceRecords =
                pollUntilRecordCount(sourceTask, createdItems.size(), RECORDS_POLL_TIMEOUT_MS);
            validateFeedRangeRecords(sourceRecords, createdItems);
        } finally {
            if (sourceTask != null) {
                sourceTask.stop();
            }
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

        CosmosSourceTask sourceTask = null;
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

            sourceTask = new CosmosSourceTask();
            sourceTask.initialize(new TestSourceTaskContext(taskConfigMap));
            sourceTask.start(taskConfigMap);

            // first creating few items in the container
            this.createItems(clientItem.getClient(), databaseName, multiPartitionContainer.getId(), 10);

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

            List<SourceRecord> sourceRecords =
                pollUntilRecordCount(sourceTask, expectedItems.size(), RECORDS_POLL_TIMEOUT_MS);
            validateFeedRangeRecords(sourceRecords, expectedItems);
        } finally {
            if (sourceTask != null) {
                sourceTask.stop();
            }
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
        CosmosSourceTask sourceTask = null;
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

            sourceTask = new CosmosSourceTask();
            sourceTask.initialize(new TestSourceTaskContext(taskConfigMap));
            sourceTask.start(taskConfigMap);

            // first creating few items in the container
            List<TestItem> createdItems =
                this.createItems(clientCacheItem.getClient(), databaseName, singlePartitionContainer.getId(), 10);

            List<SourceRecord> sourceRecords =
                pollUntilRecordCount(sourceTask, createdItems.size(), RECORDS_POLL_TIMEOUT_MS);
            validateFeedRangeRecords(sourceRecords, createdItems);
        } finally {
            if (sourceTask != null) {
                sourceTask.stop();
            }
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
        CosmosSourceTask sourceTask = null;
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

            sourceTask = new CosmosSourceTask();
            sourceTask.initialize(new TestSourceTaskContext(taskConfigMap));
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
            if (sourceTask != null) {
                sourceTask.stop();
            }
            if (clientCacheItem != null) {
                // clean up containers
                cleanUpContainer(clientCacheItem.getClient(), databaseName, singlePartitionContainer.getId());
                CosmosClientCache.releaseCosmosClient(clientCacheItem.getClientConfig());
                clientCacheItem.getClient().close();
            }
        }
    }

    @Test(groups = { "kafka", "kafka-emulator" }, timeOut = TIMEOUT)
    public void stopCancelsInFlightChangeFeedRequest() throws InterruptedException {
        String taskId = UUID.randomUUID().toString();
        Map<String, String> sourceConfigMap = new HashMap<>();
        sourceConfigMap.put("azure.cosmos.account.endpoint", KafkaCosmosTestConfigurations.HOST);
        sourceConfigMap.put("azure.cosmos.account.key", KafkaCosmosTestConfigurations.MASTER_KEY);
        sourceConfigMap.put("azure.cosmos.source.database.name", databaseName);
        sourceConfigMap.put("azure.cosmos.source.containers.includedList", Arrays.asList(singlePartitionContainerName).toString());
        sourceConfigMap.put("azure.cosmos.source.task.id", taskId);

        CosmosSourceConfig sourceConfig = new CosmosSourceConfig(sourceConfigMap);
        CosmosClientCacheItem clientItem =
            CosmosClientCache.getCosmosClient(
                sourceConfig.getAccountConfig(),
                taskId);
        CosmosContainerProperties containerProperties = getSinglePartitionContainer(clientItem.getClient());
        CosmosAsyncContainer container =
            clientItem.getClient().getDatabase(databaseName).getContainer(containerProperties.getId());
        FaultInjectionRule responseDelayRule =
            new FaultInjectionRuleBuilder("changeFeedResponseDelay-" + UUID.randomUUID())
                .condition(
                    new FaultInjectionConditionBuilder()
                        .operationType(FaultInjectionOperationType.READ_FEED_ITEM)
                        .build())
                .result(
                    FaultInjectionResultBuilders
                        .getResultBuilder(FaultInjectionServerErrorType.RESPONSE_DELAY)
                        .delay(Duration.ofSeconds(30))
                        .suppressServiceRequests(true)
                        .build())
                .hitLimit(1)
                .build();
        CosmosSourceTask sourceTask = null;

        try {
            CosmosFaultInjectionHelper.configureFaultInjectionRules(container, Arrays.asList(responseDelayRule)).block();

            Map<String, String> taskConfigMap = sourceConfig.originalsStrings();
            FeedRangeTaskUnit feedRangeTaskUnit = new FeedRangeTaskUnit(
                databaseName,
                containerProperties.getId(),
                containerProperties.getResourceId(),
                FeedRange.forFullRange(),
                null,
                containerProperties.getId());
            taskConfigMap.putAll(CosmosSourceTaskConfig.getFeedRangeTaskUnitsConfigMap(Arrays.asList(feedRangeTaskUnit)));

            sourceTask = new CosmosSourceTask();
            sourceTask.initialize(new TestSourceTaskContext(taskConfigMap));
            sourceTask.start(taskConfigMap);

            long pollStartTime = System.nanoTime();
            assertThat(sourceTask.poll().isEmpty()).isTrue();
            assertThat(TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - pollStartTime)).isLessThan(5_000);

            long faultDeadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
            while (responseDelayRule.getHitCount() == 0 && System.nanoTime() < faultDeadline) {
                Thread.sleep(100);
            }
            assertThat(responseDelayRule.getHitCount()).isGreaterThan(0);

            long stopStartTime = System.nanoTime();
            sourceTask.stop();
            sourceTask = null;
            assertThat(TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - stopStartTime)).isLessThan(5_000);
        } finally {
            responseDelayRule.disable();
            if (sourceTask != null) {
                sourceTask.stop();
            }
            CosmosClientCache.releaseCosmosClient(clientItem.getClientConfig());
            clientItem.getClient().close();
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
        assertThat(idsReceived.containsAll(expectedIds)).isTrue();
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

    public static class TestSourceTaskContext implements SourceTaskContext {
        private final Map<String, String> map;
        private final OffsetStorageReader offsetStorageReader;

        public TestSourceTaskContext(Map<String, String> map) {
            this.map = map;
            this.offsetStorageReader = new InMemoryStorageReader();
        }

        @Override
        public Map<String, String> configs() {
            return this.map;
        }

        @Override
        public OffsetStorageReader offsetStorageReader() {
            return this.offsetStorageReader;
        }
    }
}
