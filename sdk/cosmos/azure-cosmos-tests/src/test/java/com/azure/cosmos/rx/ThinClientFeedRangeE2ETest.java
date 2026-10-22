// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.rx;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.implementation.directconnectivity.Protocol;
import com.azure.cosmos.implementation.feedranges.FeedRangeEpkImpl;
import com.azure.cosmos.implementation.routing.PartitionKeyInternal;
import com.azure.cosmos.implementation.routing.Range;
import com.azure.cosmos.models.CosmosChangeFeedRequestOptions;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedRange;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.PartitionKeyBuilder;
import com.azure.cosmos.models.PartitionKeyDefinition;
import com.azure.cosmos.models.PartitionKeyDefinitionVersion;
import com.azure.cosmos.models.PartitionKind;
import com.azure.cosmos.models.ThroughputProperties;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.time.Duration;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class ThinClientFeedRangeE2ETest extends TestSuiteBase {
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(60);
    private final Map<String, String> idsByEpk = new TreeMap<>();
    private CosmosAsyncClient directClient;
    private CosmosAsyncClient thinClient;
    private CosmosAsyncDatabase database;
    private CosmosAsyncContainer directContainer;
    private CosmosAsyncContainer thinContainer;
    private CosmosAsyncContainer directHierarchicalContainer;
    private CosmosAsyncContainer thinHierarchicalContainer;
    private Range<String> subRange;
    private Range<String> physicalRange;
    private Range<String> crossPartitionRange;

    @BeforeClass(groups = {"thinclient", "thinclientEndpointProbe"}, timeOut = SETUP_TIMEOUT * 2)
    public void setup() {
        directClient = createDirectRxDocumentClient(ConsistencyLevel.SESSION, Protocol.TCP, false, null, true, true)
            .buildAsyncClient();
        thinClient = createGatewayRxDocumentClient(
            TestConfigurations.HOST, ConsistencyLevel.SESSION, false, null, true, true, true).buildAsyncClient();
        database = createTestDatabase(directClient, "thin-range");

        PartitionKeyDefinition definition = new PartitionKeyDefinition();
        definition.setPaths(Collections.singletonList("/pk"));
        definition.setVersion(PartitionKeyDefinitionVersion.V2);
        database.createContainer(new CosmosContainerProperties("ranges", definition),
            ThroughputProperties.createManualThroughput(12000)).block(REQUEST_TIMEOUT);
        directContainer = database.getContainer("ranges");
        thinContainer = thinClient.getDatabase(database.getId()).getContainer("ranges");
        for (int i = 0; i < 32; i++) {
            String key = "range-" + i;
            ObjectNode document = ThinClientTestBase.OBJECT_MAPPER.createObjectNode().put("id", key).put("pk", key);
            directContainer.createItem(document).block(REQUEST_TIMEOUT);
            PartitionKeyInternal internalKey = PartitionKeyInternal.fromObjectArray(Collections.singletonList(key), true);
            idsByEpk.put(internalKey.getEffectivePartitionKeyString(internalKey, definition), key);
        }

        List<FeedRange> feedRanges = directContainer.getFeedRanges().block(REQUEST_TIMEOUT);
        assertThat(feedRanges).as("Fixture must span multiple physical partitions").hasSizeGreaterThan(1);
        List<List<String>> partitionPoints = new ArrayList<>();
        for (FeedRange feedRange : feedRanges) {
            Range<String> range = ((FeedRangeEpkImpl) feedRange).getRange();
            List<String> points = idsByEpk.keySet().stream().filter(range::contains).collect(Collectors.toList());
            assertThat(points).as("Every physical partition must contain out-of-scope sentinels").isNotEmpty();
            partitionPoints.add(points);
        }
        List<String> points = partitionPoints.stream().max(Comparator.comparingInt(List::size)).get();
        assertThat(points).as("Need included items and excluded items on both sides in the same partition")
            .hasSizeGreaterThanOrEqualTo(4);
        subRange = new Range<>(points.get(1), points.get(points.size() - 1), true, false);
        physicalRange = feedRanges.stream().map(range -> ((FeedRangeEpkImpl) range).getRange())
            .filter(range -> range.contains(points.get(0))).findFirst().get();
        List<String> allPoints = new ArrayList<>(idsByEpk.keySet());
        crossPartitionRange = new Range<>(allPoints.get(1), allPoints.get(allPoints.size() - 1), true, false);
        assertThat(partitionPoints.stream().filter(partition -> partition.stream().anyMatch(crossPartitionRange::contains))
            .count()).as("Narrowed cross-partition scope must include data from multiple partitions").isGreaterThan(1);

        PartitionKeyDefinition hierarchicalDefinition = new PartitionKeyDefinition();
        hierarchicalDefinition.setKind(PartitionKind.MULTI_HASH);
        hierarchicalDefinition.setVersion(PartitionKeyDefinitionVersion.V2);
        hierarchicalDefinition.setPaths(Arrays.asList("/tenantId", "/userId"));
        database.createContainer(new CosmosContainerProperties("hierarchical", hierarchicalDefinition),
            ThroughputProperties.createManualThroughput(12000)).block(REQUEST_TIMEOUT);
        directHierarchicalContainer = database.getContainer("hierarchical");
        thinHierarchicalContainer = thinClient.getDatabase(database.getId()).getContainer("hierarchical");
        assertThat(directHierarchicalContainer.getFeedRanges().block(REQUEST_TIMEOUT))
            .as("More tenant prefixes than physical partitions guarantees co-located foreign tenants")
            .hasSizeBetween(2, 7);
        for (int tenant = 0; tenant < 8; tenant++) {
            for (int user = 0; user < 4; user++) {
                directHierarchicalContainer.createItem(ThinClientTestBase.OBJECT_MAPPER.createObjectNode()
                    .put("id", "tenant-" + tenant + "-user-" + user)
                    .put("tenantId", "tenant-" + tenant).put("userId", "user-" + user)).block(REQUEST_TIMEOUT);
            }
        }
    }

    @AfterClass(groups = {"thinclient", "thinclientEndpointProbe"}, alwaysRun = true, timeOut = SHUTDOWN_TIMEOUT)
    public void cleanup() {
        try {
            if (database != null) {
                safeDeleteDatabase(database);
            }
        } finally {
            try {
                if (thinClient != null) {
                    thinClient.close();
                }
            } finally {
                if (directClient != null) {
                    directClient.close();
                }
            }
        }
    }

    @DataProvider
    public Object[][] scopes() {
        return new Object[][] {{"subRange"}, {"emptyRange"}, {"physicalRange"}, {"crossPartitionRange"}};
    }

    @DataProvider
    public Object[][] queries() {
        List<Object[]> cases = new ArrayList<>();
        for (Object[] scope : scopes()) {
            cases.add(new Object[] {scope[0], false});
            cases.add(new Object[] {scope[0], true});
        }
        return cases.toArray(new Object[0][]);
    }

    @Test(groups = {"thinclient", "thinclientEndpointProbe"}, dataProvider = "queries", timeOut = TIMEOUT)
    public void queryPreservesScopeAcrossContinuation(String scope, boolean ordered) {
        Range<String> range = rangeFor(scope);
        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions().setFeedRange(new FeedRangeEpkImpl(range));
        List<String> expected = expectedIds(range);
        String query = ordered ? "SELECT * FROM c ORDER BY c.id" : "SELECT * FROM c";
        assertThat(drainQuery(directContainer, options, false, query)).containsExactlyInAnyOrderElementsOf(expected);
        assertThat(drainQuery(thinContainer, options, true, query)).containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test(groups = {"thinclient", "thinclientEndpointProbe"}, dataProvider = "scopes", timeOut = TIMEOUT)
    public void changeFeedPreservesScopeAcrossContinuation(String scope) {
        Range<String> range = rangeFor(scope);
        List<String> expected = expectedIds(range);
        assertThat(drainChangeFeed(directContainer, range, false)).containsExactlyInAnyOrderElementsOf(expected);
        assertThat(drainChangeFeed(thinContainer, range, true)).containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test(groups = {"thinclient", "thinclientEndpointProbe"}, timeOut = TIMEOUT)
    public void hierarchicalPrefixesDoNotLeakAcrossContinuation() {
        for (int tenant = 0; tenant < 9; tenant++) {
            CosmosQueryRequestOptions options = new CosmosQueryRequestOptions()
                .setPartitionKey(new PartitionKeyBuilder().add("tenant-" + tenant).build());
            List<String> expected = new ArrayList<>();
            if (tenant < 8) {
                for (int user = 0; user < 4; user++) {
                    expected.add("tenant-" + tenant + "-user-" + user);
                }
            }
            assertThat(drainQuery(directHierarchicalContainer, options, false, "SELECT * FROM c"))
                .containsExactlyInAnyOrderElementsOf(expected);
            assertThat(drainQuery(thinHierarchicalContainer, options, true, "SELECT * FROM c"))
                .containsExactlyInAnyOrderElementsOf(expected);
        }
    }

    private List<String> drainQuery(
        CosmosAsyncContainer container, CosmosQueryRequestOptions options, boolean assertThinClient, String query) {
        List<String> ids = new ArrayList<>();
        String continuation = null;
        for (int pageNumber = 0; pageNumber < 100; pageNumber++) {
            FeedResponse<ObjectNode> page = container.queryItems(query, options, ObjectNode.class)
                .byPage(continuation, 1).blockFirst(REQUEST_TIMEOUT);
            assertThat(page).isNotNull();
            if (assertThinClient) {
                assertThinClientEndpointUsed(page.getCosmosDiagnostics());
            }
            page.getResults().forEach(document -> ids.add(document.get("id").asText()));
            continuation = page.getContinuationToken();
            if (continuation == null) {
                assertThat(new HashSet<>(ids)).as("No duplicate IDs after query resumption").hasSize(ids.size());
                if (ids.size() > 1) {
                    assertThat(pageNumber).as("Must exercise resumed query pages").isPositive();
                }
                return ids;
            }
        }
        throw new AssertionError("Query did not drain in 100 resumed pages");
    }

    private Set<String> drainChangeFeed(CosmosAsyncContainer container, Range<String> range, boolean assertThinClient) {
        Set<String> ids = new HashSet<>();
        String continuation = null;
        int nonEmptyPages = 0;
        for (int pageNumber = 0; pageNumber < 100; pageNumber++) {
            CosmosChangeFeedRequestOptions options = continuation == null
                ? CosmosChangeFeedRequestOptions.createForProcessingFromBeginning(new FeedRangeEpkImpl(range))
                : CosmosChangeFeedRequestOptions.createForProcessingFromContinuation(continuation);
            options.setMaxItemCount(1);
            FeedResponse<ObjectNode> page = container.queryChangeFeed(options, ObjectNode.class)
                .byPage().blockFirst(REQUEST_TIMEOUT);
            assertThat(page).isNotNull();
            if (assertThinClient) {
                assertThinClientEndpointUsed(page.getCosmosDiagnostics());
            }
            for (ObjectNode document : page.getResults()) {
                assertThat(ids.add(document.get("id").asText())).as("No duplicate changes after resumption").isTrue();
            }
            continuation = page.getContinuationToken();
            assertThat(continuation).isNotEmpty();
            if (ImplementationBridgeHelpers.FeedResponseHelper.getFeedResponseAccessor().getNoChanges(page)) {
                if (ids.size() > 1) {
                    assertThat(nonEmptyPages).as("Must exercise resumed change-feed pages").isGreaterThan(1);
                }
                return ids;
            }
            if (!page.getResults().isEmpty()) {
                nonEmptyPages++;
            }
        }
        throw new AssertionError("Change feed did not drain in 100 resumed pages");
    }

    private Range<String> rangeFor(String scope) {
        switch (scope) {
            case "subRange": return subRange;
            case "emptyRange":
                BigInteger nextHash = new BigInteger(subRange.getMin(), 16).add(BigInteger.ONE);
                Range<String> emptyRange = new Range<>(String.format(Locale.ROOT, "%032X", nextHash),
                    String.format(Locale.ROOT, "%032X", nextHash.add(BigInteger.ONE)), true, false);
                assertThat(expectedIds(emptyRange)).as("Empty range must exclude every seeded document").isEmpty();
                return emptyRange;
            case "physicalRange": return physicalRange;
            case "crossPartitionRange": return crossPartitionRange;
            default: throw new AssertionError("Unknown scope: " + scope);
        }
    }

    private List<String> expectedIds(Range<String> range) {
        return idsByEpk.entrySet().stream().filter(entry -> range.contains(entry.getKey()))
            .map(Map.Entry::getValue).collect(Collectors.toList());
    }
}
