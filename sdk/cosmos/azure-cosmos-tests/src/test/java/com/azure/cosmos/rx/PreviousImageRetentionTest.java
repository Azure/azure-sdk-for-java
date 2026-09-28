// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.rx;

import com.azure.cosmos.ChangeFeedProcessor;
import com.azure.cosmos.ChangeFeedProcessorBuilder;
import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.models.ChangeFeedMetaData;
import com.azure.cosmos.models.ChangeFeedOperationType;
import com.azure.cosmos.models.ChangeFeedPolicy;
import com.azure.cosmos.models.ChangeFeedProcessorItem;
import com.azure.cosmos.models.ChangeFeedProcessorOptions;
import com.azure.cosmos.models.CosmosChangeFeedPreviousImageRetentionMode;
import com.azure.cosmos.models.CosmosChangeFeedRequestOptions;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosContainerResponse;
import com.azure.cosmos.models.CosmosPatchOperations;
import com.azure.cosmos.models.ExcludedPath;
import com.azure.cosmos.models.FeedRange;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.IncludedPath;
import com.azure.cosmos.models.IndexingPolicy;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.ThroughputProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.testng.ITestContext;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Opt-in live tests for container-level all-versions-and-deletes previous-image retention.
 *
 * <p>Set {@code COSMOS_PREVIOUS_IMAGE_RETENTION_TESTS_ENABLED=true}, {@code ACCOUNT_HOST}, and
 * {@code ACCOUNT_KEY}. The account must already have continuous backup, all-versions-and-deletes
 * change feed, and the container previous-image retention capability enabled. These tests do not
 * configure account capabilities, and are not intended for the emulator. The credential must allow
 * database/container creation and deletion; each container uses 400 RU/s of provisioned throughput.
 * Tests use one gateway/session client and delete their dedicated database after the class.</p>
 *
 * <p>Run only the {@code manual-previous-image-retention} TestNG group and this class. For example, from the
 * repository root, without an emulator/fast test profile:</p>
 * <pre>
 * mvn -B -ntp -f sdk/cosmos/pom.xml -pl azure-cosmos,azure-cosmos-test,azure-cosmos-tests -am \
 *     test-compile failsafe:integration-test failsafe:verify \
 *     -Dit.test=PreviousImageRetentionTest -Dgroups=manual-previous-image-retention \
 *     -Dfailsafe.failIfNoSpecifiedTests=false -DfailIfNoTests=false
 * </pre>
 *
 * <p>Both the explicit group selection and the environment opt-in are required. Otherwise setup
 * skips before creating a client or database. This class deliberately does not extend
 * {@code TestSuiteBase}, so no inherited factory or suite setup can create resources before the guard.
 * Once opted in, unsupported capabilities and missing required previous images fail rather than
 * skip. The retention policy omits {@code includedPaths}, so selected operations must retain the
 * full prior document. No assertion requires previous images to be absent for disabled or
 * nonselected operations: account configuration or another feature can still enable capture.</p>
 */
public class PreviousImageRetentionTest {
    private static final String GROUP = "manual-previous-image-retention";
    private static final String OPT_IN_ENV = "COSMOS_PREVIOUS_IMAGE_RETENTION_TESTS_ENABLED";
    private static final String POLICY_NAME = "previousImageRetentionPolicy";
    private static final String MODE_PATH = "/" + POLICY_NAME + "/supportedFeatures/allVersionsAndDeletes/mode";
    private static final String PARTITION = "previous-image-partition";
    private static final PartitionKey PARTITION_KEY = new PartitionKey(PARTITION);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(30);
    private static final Duration FEED_TIMEOUT = Duration.ofSeconds(60);
    private static final int TEST_TIMEOUT = 180_000;
    private static final ObjectMapper OBJECT_MAPPER = Utils.getSimpleObjectMapper();

    private CosmosAsyncClient client;
    private CosmosAsyncDatabase database;

    // TestNG wraps SkipException as a failure when a configuration method has a timeout.
    @BeforeClass(groups = GROUP)
    public void requireLiveAccountOptIn(ITestContext context) {
        if (!Arrays.asList(context.getIncludedGroups()).contains(GROUP)
            || !Boolean.parseBoolean(System.getenv(OPT_IN_ENV))) {
            throw new SkipException("Live previous-image retention tests require group " + GROUP
                + " and " + OPT_IN_ENV + "=true");
        }
    }

    @BeforeClass(groups = GROUP, dependsOnMethods = "requireLiveAccountOptIn", timeOut = TEST_TIMEOUT)
    public void setUp() {
        assertThat(URI.create(TestConfigurations.HOST).getHost())
            .as("ACCOUNT_HOST must identify the explicitly provisioned live account, not the default emulator")
            .isNotIn("localhost", "127.0.0.1", "[::1]");
        client = new CosmosClientBuilder()
            .endpoint(TestConfigurations.HOST)
            .key(TestConfigurations.MASTER_KEY)
            .consistencyLevel(ConsistencyLevel.SESSION)
            .gatewayMode()
            .buildAsyncClient();
        database = TestSuiteBase.createTestDatabase(client, "previous-image-retention");
    }

    @AfterClass(groups = GROUP, alwaysRun = true, timeOut = TEST_TIMEOUT)
    public void tearDown() {
        if (client == null) {
            return;
        }
        try {
            TestSuiteBase.safeDeleteDatabase(database);
        } finally {
            TestSuiteBase.safeClose(client);
        }
    }

    @DataProvider
    public Object[][] retentionModes() {
        return new Object[][] {
            { CosmosChangeFeedPreviousImageRetentionMode.DISABLED, 0 },
            { CosmosChangeFeedPreviousImageRetentionMode.ENABLED_FOR_REPLACE_OPERATIONS, 1 },
            { CosmosChangeFeedPreviousImageRetentionMode.ENABLED_FOR_DELETE_OPERATIONS, 2 },
            { CosmosChangeFeedPreviousImageRetentionMode.ENABLED_FOR_ALL_OPERATIONS, 3 }
        };
    }

    @Test(groups = GROUP, dataProvider = "retentionModes", timeOut = TEST_TIMEOUT)
    public void createReadAndReplaceRoundTripEveryMode(
        CosmosChangeFeedPreviousImageRetentionMode mode, int wireMode) throws JsonProcessingException {

        CosmosContainerProperties definition = containerDefinition("round-trip");
        definition.setChangeFeedPreviousImageRetentionMode(mode);
        assertPolicy(definition, mode, wireMode);
        assertIncludedPathsOmitted(definition);

        CosmosContainerResponse created = database.createContainer(definition, ThroughputProperties.createManualThroughput(400))
            .block(REQUEST_TIMEOUT);
        assertThat(created).isNotNull();
        assertThat(created.getStatusCode()).isEqualTo(201);
        assertPolicy(created.getProperties(), mode, wireMode);

        CosmosAsyncContainer container = database.getContainer(definition.getId());
        CosmosContainerProperties read = readProperties(container);
        assertPolicy(read, mode, wireMode);
        assertOrdinaryProperties(read, containerJson(created.getProperties()));
        JsonNode ordinaryProperties = containerJson(read);

        // Use a different starting mode, then replace back to the data-provider mode.
        CosmosChangeFeedPreviousImageRetentionMode otherMode = wireMode == 3
            ? CosmosChangeFeedPreviousImageRetentionMode.DISABLED
            : CosmosChangeFeedPreviousImageRetentionMode.ENABLED_FOR_ALL_OPERATIONS;
        read.setChangeFeedPreviousImageRetentionMode(otherMode);
        read = replaceAndRead(container, read, otherMode, wireMode == 3 ? 0 : 3, ordinaryProperties);
        read.setChangeFeedPreviousImageRetentionMode(mode);
        replaceAndRead(container, read, mode, wireMode, ordinaryProperties);
    }

    @Test(groups = GROUP, timeOut = TEST_TIMEOUT)
    public void omittedPolicyAndModeUpdatesPreserveOtherContainerProperties() throws JsonProcessingException {
        CosmosContainerProperties definition = containerDefinition("policy-updates");
        assertPolicyAbsent(definition);
        CosmosContainerResponse created = database.createContainer(definition, ThroughputProperties.createManualThroughput(400))
            .block(REQUEST_TIMEOUT);
        assertThat(created).isNotNull();
        assertThat(created.getStatusCode()).isEqualTo(201);
        assertPolicyAbsent(created.getProperties());

        CosmosAsyncContainer container = database.getContainer(definition.getId());
        CosmosContainerProperties read = readProperties(container);
        assertPolicyAbsent(read);

        // An ordinary read/modify/replace must not introduce a previously omitted policy.
        read.setDefaultTimeToLiveInSeconds(3600);
        JsonNode ordinaryProperties = containerJson(read);
        CosmosContainerResponse replaced = container.replace(read).block(REQUEST_TIMEOUT);
        assertThat(replaced).isNotNull();
        assertThat(replaced.getStatusCode()).isEqualTo(200);
        assertPolicyAbsent(replaced.getProperties());
        assertOrdinaryProperties(replaced.getProperties(), ordinaryProperties);
        read = readProperties(container);
        assertPolicyAbsent(read);
        assertOrdinaryProperties(read, ordinaryProperties);

        CosmosChangeFeedPreviousImageRetentionMode[] modes = {
            CosmosChangeFeedPreviousImageRetentionMode.ENABLED_FOR_DELETE_OPERATIONS,
            CosmosChangeFeedPreviousImageRetentionMode.ENABLED_FOR_ALL_OPERATIONS,
            CosmosChangeFeedPreviousImageRetentionMode.DISABLED
        };
        int[] wireModes = { 2, 3, 0 };
        for (int i = 0; i < modes.length; i++) {
            read.setChangeFeedPreviousImageRetentionMode(modes[i]);
            read = replaceAndRead(container, read, modes[i], wireModes[i], ordinaryProperties);
        }
    }

    @Test(groups = GROUP, dataProvider = "retentionModes", timeOut = TEST_TIMEOUT)
    public void fullPreviousImagesSurviveContinuationAcrossEveryMutation(
        CosmosChangeFeedPreviousImageRetentionMode mode, int wireMode) throws Exception {

        CosmosAsyncContainer container = createFeedContainer("mutations", mode, wireMode);
        String initialContinuation = continuationFromNow(container, FeedRange.forLogicalPartition(PARTITION_KEY));
        ObjectNode original = document();
        assertThat(container.createItem(original).block(REQUEST_TIMEOUT).getStatusCode()).isEqualTo(201);

        ObjectNode replaced = original.deepCopy();
        replaced.put("version", 2);
        replaced.put("description", "replaced");
        replaced.remove("removedOnReplace");
        ((ObjectNode) replaced.get("nested")).put("value", "replacement");
        assertThat(container.replaceItem(replaced, original.get("id").asText(), PARTITION_KEY)
            .block(REQUEST_TIMEOUT).getStatusCode()).isEqualTo(200);

        ChangeBatch firstBatch = readChanges(container, initialContinuation, 2);
        assertThat(firstBatch.continuation).isNotEqualTo(initialContinuation);

        CosmosPatchOperations patch = CosmosPatchOperations.create()
            .replace("/version", 3)
            .replace("/description", "patched")
            .replace("/nested/value", "patch")
            .add("/addedOnPatch", true);
        assertThat(container.patchItem(original.get("id").asText(), PARTITION_KEY, patch, ObjectNode.class)
            .block(REQUEST_TIMEOUT).getStatusCode()).isEqualTo(200);
        ObjectNode patched = replaced.deepCopy();
        patched.put("version", 3);
        patched.put("description", "patched");
        ((ObjectNode) patched.get("nested")).put("value", "patch");
        patched.put("addedOnPatch", true);

        ObjectNode upserted = patched.deepCopy();
        upserted.put("version", 4);
        upserted.put("description", "upserted");
        upserted.putArray("values").add("replacement array").add(99);
        upserted.remove("nullable");
        assertThat(container.upsertItem(upserted).block(REQUEST_TIMEOUT).getStatusCode()).isEqualTo(200);
        assertThat(container.deleteItem(original.get("id").asText(), PARTITION_KEY)
            .block(REQUEST_TIMEOUT).getStatusCode()).isEqualTo(204);

        // Resume with a new request-options instance after the create/replace boundary.
        ChangeBatch resumed = readChanges(container, firstBatch.continuation, 3);
        assertThat(resumed.continuation).isNotEqualTo(firstBatch.continuation);
        List<ChangeFeedProcessorItem> changes = new ArrayList<>(firstBatch.changes);
        changes.addAll(resumed.changes);
        assertThat(changes).hasSize(5);
        List<ObjectNode> versions = Arrays.asList(original, replaced, patched, upserted);
        for (int i = 0; i < versions.size(); i++) {
            assertFullDocument(changes.get(i).getCurrent(), versions.get(i));
            assertMetadata(changes.get(i), i == 0 ? ChangeFeedOperationType.CREATE : ChangeFeedOperationType.REPLACE,
                i == 0 ? null : changes.get(i - 1));
        }
        assertPureCreate(changes.get(0));

        if (mode == CosmosChangeFeedPreviousImageRetentionMode.ENABLED_FOR_REPLACE_OPERATIONS
            || mode == CosmosChangeFeedPreviousImageRetentionMode.ENABLED_FOR_ALL_OPERATIONS) {
            for (int i = 1; i < versions.size(); i++) {
                assertFullPrevious(changes.get(i), changes.get(i - 1));
            }
        }
        ChangeFeedProcessorItem deleted = changes.get(4);
        assertMetadata(deleted, ChangeFeedOperationType.DELETE, changes.get(3));
        assertThat(deleted.getChangeFeedMetaData().isTimeToLiveExpired()).isFalse();
        if (mode == CosmosChangeFeedPreviousImageRetentionMode.ENABLED_FOR_DELETE_OPERATIONS
            || mode == CosmosChangeFeedPreviousImageRetentionMode.ENABLED_FOR_ALL_OPERATIONS) {
            assertFullPrevious(deleted, changes.get(3));
        }
        assertThat(readPage(container, resumed.continuation).getResults())
            .as("Resuming after the final delete must not replay earlier mutations")
            .isEmpty();
    }

    @Test(groups = GROUP, timeOut = TEST_TIMEOUT)
    public void processorReceivesFullPreviousOnReplaceAndDelete() throws Exception {
        CosmosAsyncContainer container = createFeedContainer("processor",
            CosmosChangeFeedPreviousImageRetentionMode.ENABLED_FOR_ALL_OPERATIONS, 3);
        CosmosContainerProperties leaseDefinition = new CosmosContainerProperties("leases-" + UUID.randomUUID(), "/id");
        assertThat(database.createContainer(leaseDefinition, ThroughputProperties.createManualThroughput(400))
            .block(REQUEST_TIMEOUT).getStatusCode()).isEqualTo(201);
        CosmosAsyncContainer leases = database.getContainer(leaseDefinition.getId());

        assertThat(container.getFeedRanges().block(REQUEST_TIMEOUT))
            .as("The processor continuation and LSN ordering require a single physical partition")
            .hasSize(1);
        // Capture a real checkpoint before writing; processor startup cannot advance past these writes.
        String continuation = continuationFromNow(container, FeedRange.forFullRange());
        ObjectNode original = document();
        assertThat(container.createItem(original).block(REQUEST_TIMEOUT).getStatusCode()).isEqualTo(201);
        ObjectNode replaced = original.deepCopy();
        replaced.put("version", 2);
        replaced.remove("removedOnReplace");
        ((ObjectNode) replaced.get("nested")).put("value", "processor replacement");
        assertThat(container.replaceItem(replaced, original.get("id").asText(), PARTITION_KEY)
            .block(REQUEST_TIMEOUT).getStatusCode()).isEqualTo(200);
        assertThat(container.deleteItem(original.get("id").asText(), PARTITION_KEY)
            .block(REQUEST_TIMEOUT).getStatusCode()).isEqualTo(204);

        Map<Long, ChangeFeedProcessorItem> received = Collections.synchronizedMap(new LinkedHashMap<>());
        ChangeFeedProcessor processor = new ChangeFeedProcessorBuilder()
            .hostName("previous-image-" + UUID.randomUUID())
            .feedContainer(container)
            .leaseContainer(leases)
            .options(new ChangeFeedProcessorOptions()
                .setStartContinuation(continuation)
                .setMaxItemCount(1)
                .setFeedPollDelay(Duration.ofMillis(200)))
            .handleAllVersionsAndDeletesChanges(changes -> {
                for (ChangeFeedProcessorItem change : changes) {
                    // Delivery is at least once; one partition makes LSN a stable deduplication key.
                    received.putIfAbsent(change.getChangeFeedMetaData().getLogSequenceNumber(), change);
                }
            })
            .buildChangeFeedProcessor();
        try {
            processor.start().block(REQUEST_TIMEOUT);
            long deadline = System.nanoTime() + FEED_TIMEOUT.toNanos();
            while (received.size() < 3 && System.nanoTime() < deadline) {
                Thread.sleep(100);
            }
            assertThat(received).as("Processor must deliver create, replace, and delete").hasSize(3);
        } finally {
            processor.stop().block(REQUEST_TIMEOUT);
        }

        List<ChangeFeedProcessorItem> changes;
        synchronized (received) {
            changes = new ArrayList<>(received.values());
        }
        assertThat(changes).as("First delivery order must be create, replace, delete without extra events").hasSize(3);
        assertFullDocument(changes.get(0).getCurrent(), original);
        assertMetadata(changes.get(0), ChangeFeedOperationType.CREATE, null);
        assertPureCreate(changes.get(0));
        assertFullDocument(changes.get(1).getCurrent(), replaced);
        assertMetadata(changes.get(1), ChangeFeedOperationType.REPLACE, changes.get(0));
        assertFullPrevious(changes.get(1), changes.get(0));
        assertMetadata(changes.get(2), ChangeFeedOperationType.DELETE, changes.get(1));
        assertThat(changes.get(2).getChangeFeedMetaData().isTimeToLiveExpired()).isFalse();
        assertFullPrevious(changes.get(2), changes.get(1));
    }

    private CosmosContainerProperties containerDefinition(String label) {
        return new CosmosContainerProperties(label + "-" + UUID.randomUUID(), "/pk")
            .setDefaultTimeToLiveInSeconds(-1)
            .setIndexingPolicy(new IndexingPolicy()
                .setIncludedPaths(Collections.singletonList(new IncludedPath("/*")))
                .setExcludedPaths(Collections.singletonList(new ExcludedPath("/excluded/*"))))
            .setChangeFeedPolicy(ChangeFeedPolicy.createAllVersionsAndDeletesPolicy(Duration.ofHours(1)));
    }

    private CosmosAsyncContainer createFeedContainer(
        String label, CosmosChangeFeedPreviousImageRetentionMode mode, int wireMode) throws JsonProcessingException {

        CosmosContainerProperties definition = containerDefinition(label);
        definition.setChangeFeedPreviousImageRetentionMode(mode);
        assertPolicy(definition, mode, wireMode);
        assertIncludedPathsOmitted(definition);
        CosmosContainerResponse response = database.createContainer(definition, ThroughputProperties.createManualThroughput(400))
            .block(REQUEST_TIMEOUT);
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(201);
        assertPolicy(response.getProperties(), mode, wireMode);
        CosmosAsyncContainer container = database.getContainer(definition.getId());
        assertPolicy(readProperties(container), mode, wireMode);
        return container;
    }

    private CosmosContainerProperties readProperties(CosmosAsyncContainer container) {
        CosmosContainerResponse response = container.read().block(REQUEST_TIMEOUT);
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(200);
        return response.getProperties();
    }

    private CosmosContainerProperties replaceAndRead(
        CosmosAsyncContainer container, CosmosContainerProperties properties,
        CosmosChangeFeedPreviousImageRetentionMode mode, int wireMode, JsonNode ordinaryProperties)
        throws JsonProcessingException {

        assertPolicy(properties, mode, wireMode);
        assertOrdinaryProperties(properties, ordinaryProperties);
        CosmosContainerResponse response = container.replace(properties).block(REQUEST_TIMEOUT);
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(200);
        assertPolicy(response.getProperties(), mode, wireMode);
        assertOrdinaryProperties(response.getProperties(), ordinaryProperties);
        CosmosContainerProperties read = readProperties(container);
        assertPolicy(read, mode, wireMode);
        assertOrdinaryProperties(read, ordinaryProperties);
        return read;
    }

    private void assertPolicy(CosmosContainerProperties properties,
        CosmosChangeFeedPreviousImageRetentionMode mode, int wireMode) throws JsonProcessingException {

        assertThat(properties.getChangeFeedPreviousImageRetentionMode()).isEqualTo(mode);
        JsonNode serializedMode = containerJson(properties).at(MODE_PATH);
        assertThat(serializedMode.isIntegralNumber()).as("The service mode must be a JSON integer").isTrue();
        assertThat(serializedMode.intValue()).isEqualTo(wireMode);
    }

    private void assertPolicyAbsent(CosmosContainerProperties properties) throws JsonProcessingException {
        assertThat(properties.getChangeFeedPreviousImageRetentionMode()).isNull();
        assertThat(containerJson(properties).has(POLICY_NAME)).as("An omitted policy must remain omitted").isFalse();
    }

    private void assertIncludedPathsOmitted(CosmosContainerProperties properties) throws JsonProcessingException {
        assertThat(containerJson(properties).path(POLICY_NAME).has("includedPaths"))
            .as("Omitting includedPaths requests full previous documents")
            .isFalse();
    }

    private void assertOrdinaryProperties(CosmosContainerProperties properties, JsonNode expected)
        throws JsonProcessingException {

        JsonNode actual = containerJson(properties);
        for (String field : Arrays.asList("id", "partitionKey", "indexingPolicy", "defaultTtl", "changeFeedPolicy")) {
            assertThat(actual.get(field)).as("Container property %s must survive the policy update", field)
                .isEqualTo(expected.get(field));
        }
    }

    private JsonNode containerJson(CosmosContainerProperties properties) throws JsonProcessingException {
        return OBJECT_MAPPER.readTree(ModelBridgeInternal.getResource(properties).toJson());
    }

    private String continuationFromNow(CosmosAsyncContainer container, FeedRange range) {
        CosmosChangeFeedRequestOptions options = CosmosChangeFeedRequestOptions.createForProcessingFromNow(range)
            .allVersionsAndDeletes();
        FeedResponse<ChangeFeedProcessorItem> page = container.queryChangeFeed(options, ChangeFeedProcessorItem.class)
            .byPage().blockFirst(REQUEST_TIMEOUT);
        assertThat(page).isNotNull();
        assertThat(page.getResults()).isEmpty();
        assertThat(page.getContinuationToken()).isNotEmpty();
        return page.getContinuationToken();
    }

    private FeedResponse<ChangeFeedProcessorItem> readPage(CosmosAsyncContainer container, String continuation) {
        CosmosChangeFeedRequestOptions options = CosmosChangeFeedRequestOptions.createForProcessingFromContinuation(continuation)
            .allVersionsAndDeletes()
            .setMaxItemCount(1);
        FeedResponse<ChangeFeedProcessorItem> page = container.queryChangeFeed(options, ChangeFeedProcessorItem.class)
            .byPage().blockFirst(REQUEST_TIMEOUT);
        assertThat(page).isNotNull();
        assertThat(page.getContinuationToken()).isNotEmpty();
        return page;
    }

    private ChangeBatch readChanges(CosmosAsyncContainer container, String continuation, int count)
        throws InterruptedException {

        List<ChangeFeedProcessorItem> changes = new ArrayList<>();
        long deadline = System.nanoTime() + FEED_TIMEOUT.toNanos();
        while (changes.size() < count && System.nanoTime() < deadline) {
            FeedResponse<ChangeFeedProcessorItem> page = readPage(container, continuation);
            changes.addAll(page.getResults());
            continuation = page.getContinuationToken();
            if (page.getResults().isEmpty()) {
                Thread.sleep(100);
            }
        }
        assertThat(changes).as("Expected exactly %s changes before the feed deadline", count).hasSize(count);
        return new ChangeBatch(changes, continuation);
    }

    private ObjectNode document() {
        ObjectNode document = OBJECT_MAPPER.createObjectNode();
        document.put("id", UUID.randomUUID().toString());
        document.put("pk", PARTITION);
        document.put("version", 1);
        document.put("description", "created");
        document.put("removedOnReplace", "must appear in the first previous image");
        document.putNull("nullable");
        document.putObject("nested").put("value", "original").put("unchanged", true);
        document.putArray("values").add(1).add("two").addObject().put("three", 3);
        document.putObject("excluded").put("payload", "index exclusion must not project the previous image");
        return document;
    }

    private void assertFullDocument(JsonNode actual, ObjectNode expected) {
        assertThat(actual).as("A full document must be present").isNotNull();
        assertThat(actual.isObject()).isTrue();
        ObjectNode userProperties = actual.deepCopy();
        userProperties.remove(Arrays.asList("_rid", "_self", "_etag", "_attachments", "_ts"));
        assertThat(userProperties).as("Every user property, including nested/array/null/excluded fields, is retained")
            .isEqualTo(expected);
    }

    private void assertPureCreate(ChangeFeedProcessorItem created) {
        assertThat(created.getPrevious() == null || created.getPrevious().isNull())
            .as("A pure create has no previous version")
            .isTrue();
    }

    private void assertFullPrevious(ChangeFeedProcessorItem change, ChangeFeedProcessorItem previousChange) {
        assertThat(change.getPrevious()).as("Previous image is required for the explicitly opted-in operation")
            .isNotNull()
            .isEqualTo(previousChange.getCurrent());
    }

    private void assertMetadata(ChangeFeedProcessorItem change, ChangeFeedOperationType operation,
        ChangeFeedProcessorItem previousChange) {

        ChangeFeedMetaData metadata = change.getChangeFeedMetaData();
        assertThat(metadata).isNotNull();
        assertThat(metadata.getOperationType()).isEqualTo(operation);
        assertThat(metadata.getLogSequenceNumber()).isPositive();
        assertThat(metadata.getConflictResolutionTimestamp()).isAfter(Instant.EPOCH);
        if (previousChange != null) {
            ChangeFeedMetaData previous = previousChange.getChangeFeedMetaData();
            assertThat(metadata.getLogSequenceNumber()).isGreaterThan(previous.getLogSequenceNumber());
            assertThat(metadata.getPreviousLogSequenceNumber()).isEqualTo(previous.getLogSequenceNumber());
            assertThat(metadata.getConflictResolutionTimestamp()).isAfterOrEqualTo(previous.getConflictResolutionTimestamp());
        }
    }

    private static final class ChangeBatch {
        private final List<ChangeFeedProcessorItem> changes;
        private final String continuation;

        private ChangeBatch(List<ChangeFeedProcessorItem> changes, String continuation) {
            this.changes = changes;
            this.continuation = continuation;
        }
    }
}
