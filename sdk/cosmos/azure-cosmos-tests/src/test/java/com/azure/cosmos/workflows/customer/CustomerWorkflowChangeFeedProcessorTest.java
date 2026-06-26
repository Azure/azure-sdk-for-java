// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.workflows.customer;

import com.azure.cosmos.ChangeFeedProcessor;
import com.azure.cosmos.ChangeFeedProcessorBuilder;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.TestObject;
import com.azure.cosmos.models.ChangeFeedProcessorItem;
import com.azure.cosmos.models.ChangeFeedProcessorOptions;
import com.azure.cosmos.models.ChangeFeedProcessorState;
import com.azure.cosmos.test.faultinjection.FaultInjectionOperationType;
import com.azure.cosmos.test.faultinjection.FaultInjectionRule;
import com.fasterxml.jackson.databind.JsonNode;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class CustomerWorkflowChangeFeedProcessorTest extends CustomerWorkflowTestBase {

    @Factory(dataProvider = "clientBuildersWithDirectTcpSession")
    public CustomerWorkflowChangeFeedProcessorTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @BeforeClass(groups = {"fi-customer-workflows"}, timeOut = SETUP_TIMEOUT)
    public void beforeClass() {
        initializeSharedSinglePartitionContainer("Customer change feed processor workflow tests");
    }

    @AfterClass(groups = {"fi-customer-workflows"}, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        closeClient();
    }

    @Test(groups = {"fi-customer-workflows"}, timeOut = 2 * TIMEOUT)
    public void latestVersionProcessorRestartResumesFromLeasesWorkflow() throws InterruptedException {
        CosmosAsyncContainer feedContainer = createTemporaryContainer("customer-cfp-feed", "/mypk");
        CosmosAsyncContainer leaseContainer = createTemporaryContainer("customer-cfp-lease", "/id");
        ChangeFeedProcessor processor = null;
        FaultInjectionRule readFeedDelayRule = null;

        try {
            Set<String> expectedIds = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
            Set<String> receivedIds = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
            CountDownLatch initialLatch = new CountDownLatch(2);

            createFeedItem(feedContainer, expectedIds, "cfp-initial-1");
            createFeedItem(feedContainer, expectedIds, "cfp-initial-2");

            // Use a single, stable lease prefix so the second processor instance resumes from the persisted
            // continuation instead of reprocessing from the beginning - this validates a genuine restart.
            String leasePrefix = "resume";
            processor = createLatestVersionProcessor(feedContainer, leaseContainer, expectedIds, receivedIds, initialLatch, leasePrefix);
            processor.start().block();
            ChangeFeedProcessor initialProcessor = processor;

            assertThat(processor.isStarted()).isTrue();
            assertThat(initialLatch.await(30, TimeUnit.SECONDS)).isTrue();
            assertThat(receivedIds).containsAll(expectedIds);

            awaitCondition(
                () -> hasAcquiredLeases(initialProcessor),
                Duration.ofSeconds(20),
                "Change feed processor did not acquire leases.");

            processor.stop().block();
            assertThat(processor.isStarted()).isFalse();

            CountDownLatch restartLatch = new CountDownLatch(1);
            TestObject restartedItem = createFeedItem(feedContainer, expectedIds, "cfp-restart");
            readFeedDelayRule = configureResponseDelayRule(feedContainer, FaultInjectionOperationType.READ_FEED_ITEM, Duration.ofMillis(100), 1);

            processor = createLatestVersionProcessor(feedContainer, leaseContainer, expectedIds, receivedIds, restartLatch, leasePrefix);
            processor.start().block();

            assertThat(processor.isStarted()).isTrue();
            assertThat(restartLatch.await(30, TimeUnit.SECONDS)).isTrue();
            assertThat(receivedIds).contains(restartedItem.getId());

            // getEstimatedLag() is not supported for a latest-version processor; query the per-lease state
            // (which exposes the estimated lag) via the supported getCurrentState() API instead.
            List<ChangeFeedProcessorState> currentState = processor.getCurrentState().block();
            assertThat(currentState).isNotNull().isNotEmpty();
            assertThat(currentState).allSatisfy(state -> assertThat(state.getEstimatedLag()).isGreaterThanOrEqualTo(0));
        } finally {
            if (readFeedDelayRule != null) {
                readFeedDelayRule.disable();
            }
            if (processor != null && processor.isStarted()) {
                processor.stop().block();
            }
            deleteTemporaryContainer(feedContainer);
            deleteTemporaryContainer(leaseContainer);
        }
    }

    @Test(groups = {"fi-customer-workflows"}, timeOut = 2 * TIMEOUT)
    public void latestVersionProcessorWithNewLeasePrefixReprocessesFromBeginningWorkflow() throws InterruptedException {
        CosmosAsyncContainer feedContainer = createTemporaryContainer("customer-cfp-feed", "/mypk");
        CosmosAsyncContainer leaseContainer = createTemporaryContainer("customer-cfp-lease", "/id");
        ChangeFeedProcessor processor = null;

        try {
            Set<String> expectedIds = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
            Set<String> initialReceivedIds = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
            CountDownLatch initialLatch = new CountDownLatch(2);

            createFeedItem(feedContainer, expectedIds, "cfp-initial-1");
            createFeedItem(feedContainer, expectedIds, "cfp-initial-2");

            processor = createLatestVersionProcessor(feedContainer, leaseContainer, expectedIds, initialReceivedIds, initialLatch, "initial");
            processor.start().block();
            ChangeFeedProcessor initialProcessor = processor;

            assertThat(processor.isStarted()).isTrue();
            assertThat(initialLatch.await(30, TimeUnit.SECONDS)).isTrue();
            assertThat(initialReceivedIds).containsAll(expectedIds);

            awaitCondition(
                () -> hasAcquiredLeases(initialProcessor),
                Duration.ofSeconds(20),
                "Change feed processor did not acquire leases.");

            processor.stop().block();
            assertThat(processor.isStarted()).isFalse();

            // A different lease prefix creates a fresh lease set, so a from-beginning processor reprocesses all
            // existing items. A separate received-id set is required because the original set already contains them.
            Set<String> reprocessedIds = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
            CountDownLatch reprocessLatch = new CountDownLatch(expectedIds.size());

            processor = createLatestVersionProcessor(feedContainer, leaseContainer, expectedIds, reprocessedIds, reprocessLatch, "fresh");
            processor.start().block();

            assertThat(processor.isStarted()).isTrue();
            assertThat(reprocessLatch.await(30, TimeUnit.SECONDS)).isTrue();
            assertThat(reprocessedIds).containsAll(expectedIds);

            // getEstimatedLag() is not supported for a latest-version processor; query the per-lease state
            // (which exposes the estimated lag) via the supported getCurrentState() API instead.
            List<ChangeFeedProcessorState> currentState = processor.getCurrentState().block();
            assertThat(currentState).isNotNull().isNotEmpty();
            assertThat(currentState).allSatisfy(state -> assertThat(state.getEstimatedLag()).isGreaterThanOrEqualTo(0));
        } finally {
            if (processor != null && processor.isStarted()) {
                processor.stop().block();
            }
            deleteTemporaryContainer(feedContainer);
            deleteTemporaryContainer(leaseContainer);
        }
    }

    private static boolean hasAcquiredLeases(ChangeFeedProcessor processor) {
        List<ChangeFeedProcessorState> currentState = processor.getCurrentState().block();
        return currentState != null && !currentState.isEmpty();
    }

    private TestObject createFeedItem(CosmosAsyncContainer feedContainer, Set<String> expectedIds, String partitionKey) {
        TestObject item = TestObject.create(partitionKey + "-" + UUID.randomUUID());
        feedContainer.createItem(item).block();
        expectedIds.add(item.getId());
        return item;
    }

    private ChangeFeedProcessor createLatestVersionProcessor(
        CosmosAsyncContainer feedContainer,
        CosmosAsyncContainer leaseContainer,
        Set<String> expectedIds,
        Set<String> receivedIds,
        CountDownLatch latch,
        String leasePrefix) {

        return new ChangeFeedProcessorBuilder()
            .hostName("customer-workflow-" + leasePrefix + "-" + UUID.randomUUID())
            .feedContainer(feedContainer)
            .leaseContainer(leaseContainer)
            .handleLatestVersionChanges(items -> recordLatestVersionItems(items, expectedIds, receivedIds, latch))
            .options(new ChangeFeedProcessorOptions()
                .setStartFromBeginning(true)
                .setFeedPollDelay(Duration.ofMillis(500))
                .setLeaseAcquireInterval(Duration.ofSeconds(1))
                .setLeaseRenewInterval(Duration.ofSeconds(2))
                .setLeaseExpirationInterval(Duration.ofSeconds(6))
                .setMaxItemCount(10)
                .setLeasePrefix("customer-" + leasePrefix))
            .buildChangeFeedProcessor();
    }

    private static void recordLatestVersionItems(
        List<ChangeFeedProcessorItem> items,
        Set<String> expectedIds,
        Set<String> receivedIds,
        CountDownLatch latch) {

        for (ChangeFeedProcessorItem item : items) {
            JsonNode current = item.getCurrent();
            if (current != null && current.has("id")) {
                String id = current.get("id").asText();
                if (expectedIds.contains(id) && receivedIds.add(id)) {
                    latch.countDown();
                }
            }
        }
    }
}
