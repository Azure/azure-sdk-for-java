// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.rx.changefeed;

import com.azure.cosmos.ChangeFeedProcessor;
import com.azure.cosmos.ChangeFeedProcessorBuilder;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosDiagnosticsContext;
import com.azure.cosmos.implementation.InternalObjectNode;
import com.azure.cosmos.models.ChangeFeedProcessorItem;
import com.azure.cosmos.models.ChangeFeedProcessorOptions;
import com.azure.cosmos.models.CosmosClientTelemetryConfig;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosContainerRequestOptions;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.rx.TestSuiteBase;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Validates that the change feed processor works end to end when lease writes do not return response content - both
 * when the lease container client has content response on write disabled (which used to be rejected) and with the
 * change feed processor now always disabling content response for its own lease writes.
 * <p>
 * Each lease is checkpointed many times sequentially, so any stale If-Match ETag kept on the cached lease after a
 * write (the server's new ETag is only available in the response headers) would surface as 412 responses on the
 * lease container.
 */
public class ChangeFeedProcessorLeaseWithoutContentResponseTest extends TestSuiteBase {
    private static final int DOCUMENTS_PER_WAVE = 30;
    private static final int MAX_ITEM_COUNT = 3;
    private static final int FEED_THROUGHPUT = 400;
    private static final int LEASE_THROUGHPUT = 400;
    private static final Duration WAIT_TIMEOUT = Duration.ofMinutes(2);
    private static final Duration LIFECYCLE_TIMEOUT = Duration.ofSeconds(60);
    private static final long CHECKPOINT_SETTLE_TIME_MS = 3000;
    private static final int TEST_TIMEOUT = 10 * 60 * 1000;

    private enum Mode {
        PK_RANGE_ID_VERSION,
        LATEST_VERSION,
        ALL_VERSIONS_AND_DELETES
    }

    private final Map<String, ConcurrentLinkedQueue<LeaseOperation>> leaseOperationsByContainer = new ConcurrentHashMap<>();

    private CosmosAsyncClient client;
    private CosmosAsyncClient leaseClientWithoutContentResponse;
    private CosmosAsyncDatabase createdDatabase;

    @Factory(dataProvider = "clientBuildersWithSessionConsistency")
    public ChangeFeedProcessorLeaseWithoutContentResponseTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @DataProvider(name = "incrementalModes")
    public static Object[][] incrementalModes() {
        return new Object[][] {
            { Mode.PK_RANGE_ID_VERSION },
            { Mode.LATEST_VERSION }
        };
    }

    @BeforeClass(groups = { "query", "long-emulator" }, timeOut = SETUP_TIMEOUT, alwaysRun = true)
    public void before_ChangeFeedProcessorLeaseWithoutContentResponseTest() {
        client = getClientBuilder().buildAsyncClient();
        createdDatabase = createTestDatabase(client, "cfpLeaseNoContent");

        CosmosClientTelemetryConfig telemetryConfig = new CosmosClientTelemetryConfig()
            .diagnosticsHandler((diagnosticsContext, traceContext) -> recordLeaseOperation(diagnosticsContext));

        leaseClientWithoutContentResponse = copyCosmosClientBuilder(getClientBuilder())
            .contentResponseOnWriteEnabled(false)
            .clientTelemetryConfig(telemetryConfig)
            .buildAsyncClient();
    }

    @AfterClass(groups = { "query", "long-emulator" }, timeOut = SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        safeDeleteDatabase(createdDatabase);
        safeClose(leaseClientWithoutContentResponse);
        safeClose(client);
    }

    @Test(groups = { "query" }, dataProvider = "incrementalModes", timeOut = TEST_TIMEOUT)
    public void incrementalLeaseWritesWithoutContentResponse(Mode mode) throws InterruptedException {
        runScenario(mode);
    }

    @Test(groups = { "long-emulator" }, timeOut = TEST_TIMEOUT)
    public void allVersionsAndDeletesLeaseWritesWithoutContentResponse() throws InterruptedException {
        runScenario(Mode.ALL_VERSIONS_AND_DELETES);
    }

    private void runScenario(Mode mode) throws InterruptedException {
        CosmosContainerProperties feedDefinition = mode == Mode.ALL_VERSIONS_AND_DELETES
            ? getCollectionDefinitionWithFullFidelity()
            : getCollectionDefinition();
        CosmosAsyncContainer feedContainer = createCollection(
            createdDatabase, feedDefinition, new CosmosContainerRequestOptions(), FEED_THROUGHPUT);
        CosmosAsyncContainer leaseContainerWithContent = createCollection(
            createdDatabase,
            new CosmosContainerProperties("leases_" + UUID.randomUUID(), "/id"),
            new CosmosContainerRequestOptions(),
            LEASE_THROUGHPUT);
        CosmosAsyncContainer leaseContainer = leaseClientWithoutContentResponse
            .getDatabase(createdDatabase.getId())
            .getContainer(leaseContainerWithContent.getId());
        ConcurrentLinkedQueue<LeaseOperation> leaseOperations = new ConcurrentLinkedQueue<>();
        leaseOperationsByContainer.put(leaseContainer.getId(), leaseOperations);

        String leasePrefix = "noContent" + mode.ordinal();
        Map<String, AtomicInteger> receivedDocuments = new ConcurrentHashMap<>();
        List<String> createdDocumentIds = new ArrayList<>();
        ChangeFeedProcessor firstProcessor = null;
        ChangeFeedProcessor secondProcessor = null;

        try {
            firstProcessor = buildProcessor(mode, "host-1", leasePrefix, feedContainer, leaseContainer, receivedDocuments);
            start(firstProcessor);
            waitForOwnedLeases(leaseContainerWithContent, "host-1");
            if (mode == Mode.ALL_VERSIONS_AND_DELETES) {
                // AllVersionsAndDeletes always starts from "now" - give the processor time to issue its first read.
                Thread.sleep(5000);
            }

            // Two waves, each producing many sequential checkpoints on the same lease(s).
            createdDocumentIds.addAll(createDocuments(feedContainer, DOCUMENTS_PER_WAVE));
            waitForDocuments(receivedDocuments, createdDocumentIds);
            createdDocumentIds.addAll(createDocuments(feedContainer, DOCUMENTS_PER_WAVE));
            waitForDocuments(receivedDocuments, createdDocumentIds);
            // Change feed processing is at-least-once - let the checkpoint for the last batch complete before stopping.
            Thread.sleep(CHECKPOINT_SETTLE_TIME_MS);

            // Snapshot the lease operations while the processor still owns the leases.
            List<LeaseOperation> operationsWhileRunning = new ArrayList<>(leaseOperations);
            stop(firstProcessor);

            assertThat(operationsWhileRunning.stream().filter(op -> op.statusCode == 412).collect(Collectors.toList()))
                .as("No lease write should fail with 412 - the cached lease must carry the server's latest ETag")
                .isEmpty();
            long successfulReplaces = operationsWhileRunning.stream()
                .filter(op -> "Replace".equalsIgnoreCase(op.operationType) && op.statusCode == 200)
                .count();
            int minimumCheckpoints = (2 * DOCUMENTS_PER_WAVE) / MAX_ITEM_COUNT;
            assertThat(successfulReplaces)
                .as("Each lease should have been checkpointed many times sequentially")
                .isGreaterThanOrEqualTo(minimumCheckpoints);
            assertThat(operationsWhileRunning.stream().anyMatch(op -> "Create".equalsIgnoreCase(op.operationType) && op.statusCode == 201))
                .as("Lease store bootstrap (lock, leases, marker) should have created documents")
                .isTrue();

            assertExactlyOnce(receivedDocuments, createdDocumentIds);
            List<JsonNode> leases = readLeases(leaseContainerWithContent);
            assertThat(leases).isNotEmpty();
            for (JsonNode lease : leases) {
                assertThat(lease.path("ContinuationToken").asText(null))
                    .as("Lease %s should have a persisted continuation token", lease.path("id").asText())
                    .isNotEmpty();
            }

            // A new processor instance must resume from the persisted continuation tokens: nothing is re-delivered
            // and only new documents are processed.
            secondProcessor = buildProcessor(mode, "host-2", leasePrefix, feedContainer, leaseContainer, receivedDocuments);
            start(secondProcessor);
            waitForOwnedLeases(leaseContainerWithContent, "host-2");
            createdDocumentIds.addAll(createDocuments(feedContainer, 5));
            waitForDocuments(receivedDocuments, createdDocumentIds);
            Thread.sleep(CHECKPOINT_SETTLE_TIME_MS);
            stop(secondProcessor);

            assertExactlyOnce(receivedDocuments, createdDocumentIds);
        } finally {
            stop(firstProcessor);
            stop(secondProcessor);
            leaseOperationsByContainer.remove(leaseContainer.getId());
            safeDeleteCollection(feedContainer);
            safeDeleteCollection(leaseContainerWithContent);
        }
    }

    private ChangeFeedProcessor buildProcessor(
        Mode mode,
        String hostName,
        String leasePrefix,
        CosmosAsyncContainer feedContainer,
        CosmosAsyncContainer leaseContainer,
        Map<String, AtomicInteger> receivedDocuments) {

        // Long renew/expiration intervals keep the renewer from racing with checkpoints on the same lease, so the
        // only lease writes during the test are deterministic: bootstrap, acquire, sequential checkpoints, release.
        ChangeFeedProcessorOptions options = new ChangeFeedProcessorOptions()
            .setLeasePrefix(leasePrefix)
            .setMaxItemCount(MAX_ITEM_COUNT)
            .setFeedPollDelay(Duration.ofMillis(500))
            .setLeaseAcquireInterval(Duration.ofSeconds(2))
            .setLeaseRenewInterval(Duration.ofMinutes(5))
            .setLeaseExpirationInterval(Duration.ofMinutes(10));
        if (mode != Mode.ALL_VERSIONS_AND_DELETES) {
            options.setStartFromBeginning(true);
        }

        ChangeFeedProcessorBuilder builder = new ChangeFeedProcessorBuilder()
            .hostName(hostName)
            .feedContainer(feedContainer)
            .leaseContainer(leaseContainer)
            .options(options);

        Consumer<String> onDocument = id -> receivedDocuments.computeIfAbsent(id, key -> new AtomicInteger()).incrementAndGet();
        switch (mode) {
            case PK_RANGE_ID_VERSION:
                builder.handleChanges((List<JsonNode> docs) -> docs.forEach(doc -> onDocument.accept(doc.get("id").asText())));
                break;
            case LATEST_VERSION:
                builder.handleLatestVersionChanges((List<ChangeFeedProcessorItem> items) ->
                    items.forEach(item -> onDocument.accept(item.getCurrent().get("id").asText())));
                break;
            case ALL_VERSIONS_AND_DELETES:
                builder.handleAllVersionsAndDeletesChanges((List<ChangeFeedProcessorItem> items) ->
                    items.forEach(item -> onDocument.accept(item.getCurrent().get("id").asText())));
                break;
            default:
                throw new IllegalArgumentException("Unexpected mode " + mode);
        }

        return builder.buildChangeFeedProcessor();
    }

    private void recordLeaseOperation(CosmosDiagnosticsContext diagnosticsContext) {
        ConcurrentLinkedQueue<LeaseOperation> operations = leaseOperationsByContainer.get(diagnosticsContext.getContainerName());
        if (operations != null && "Document".equalsIgnoreCase(diagnosticsContext.getResourceType())) {
            operations.add(new LeaseOperation(diagnosticsContext.getOperationType(), diagnosticsContext.getStatusCode()));
        }
    }

    private List<String> createDocuments(CosmosAsyncContainer feedContainer, int count) {
        List<InternalObjectNode> documents = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            String id = UUID.randomUUID().toString();
            documents.add(new InternalObjectNode(String.format("{ \"id\": \"%s\", \"mypk\": \"%s\" }", id, id)));
        }

        List<InternalObjectNode> created = insertAllItemsBlocking(feedContainer, documents, false);
        waitIfNeededForReplicasToCatchUp(getClientBuilder());
        return created.stream().map(InternalObjectNode::getId).collect(Collectors.toList());
    }

    private static void waitForDocuments(Map<String, AtomicInteger> receivedDocuments, List<String> expectedIds)
        throws InterruptedException {

        Instant deadline = Instant.now().plus(WAIT_TIMEOUT);
        while (!receivedDocuments.keySet().containsAll(expectedIds) && Instant.now().isBefore(deadline)) {
            Thread.sleep(250);
        }

        assertThat(receivedDocuments.keySet()).containsAll(expectedIds);
    }

    private static void waitForOwnedLeases(CosmosAsyncContainer leaseContainer, String hostName) throws InterruptedException {
        Instant deadline = Instant.now().plus(WAIT_TIMEOUT);
        while (Instant.now().isBefore(deadline)) {
            List<JsonNode> leases = readLeases(leaseContainer);
            if (!leases.isEmpty() && leases.stream().allMatch(lease -> hostName.equals(lease.path("Owner").asText(null)))) {
                return;
            }
            Thread.sleep(500);
        }

        throw new AssertionError("Leases were not acquired by " + hostName + " within " + WAIT_TIMEOUT);
    }

    private static List<JsonNode> readLeases(CosmosAsyncContainer leaseContainer) {
        return leaseContainer
            .queryItems("SELECT * FROM c WHERE IS_DEFINED(c.LeaseToken)", new CosmosQueryRequestOptions(), ObjectNode.class)
            .collectList()
            .block()
            .stream()
            .map(JsonNode.class::cast)
            .collect(Collectors.toList());
    }

    private static void assertExactlyOnce(Map<String, AtomicInteger> receivedDocuments, List<String> expectedIds) {
        assertThat(receivedDocuments.keySet()).containsExactlyInAnyOrderElementsOf(expectedIds);
        for (Map.Entry<String, AtomicInteger> entry : receivedDocuments.entrySet()) {
            assertThat(entry.getValue().get())
                .as("Document %s should be delivered exactly once (no lease loss / continuation regression)", entry.getKey())
                .isEqualTo(1);
        }
    }

    private static void start(ChangeFeedProcessor processor) {
        processor.start().subscribeOn(Schedulers.boundedElastic()).timeout(LIFECYCLE_TIMEOUT).block();
    }

    private static void stop(ChangeFeedProcessor processor) {
        if (processor != null && processor.isStarted()) {
            processor.stop().timeout(LIFECYCLE_TIMEOUT).onErrorResume(throwable -> Mono.empty()).block();
        }
    }

    private static final class LeaseOperation {
        private final String operationType;
        private final int statusCode;

        private LeaseOperation(String operationType, int statusCode) {
            this.operationType = operationType;
            this.statusCode = statusCode;
        }

        @Override
        public String toString() {
            return operationType + ":" + statusCode;
        }
    }
}
