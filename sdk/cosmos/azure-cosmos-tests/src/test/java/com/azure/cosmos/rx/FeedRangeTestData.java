// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.rx;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.feedranges.FeedRangeEpkImpl;
import com.azure.cosmos.implementation.routing.PartitionKeyInternal;
import com.azure.cosmos.implementation.routing.Range;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosContainerRequestOptions;
import com.azure.cosmos.models.FeedRange;
import com.azure.cosmos.models.PartitionKeyDefinition;
import com.azure.cosmos.models.PartitionKeyDefinitionVersion;
import com.azure.cosmos.models.PartitionKind;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.math.BigInteger;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public final class FeedRangeTestData implements AutoCloseable {
    public static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(60);
    private final CosmosAsyncDatabase database;
    private final Map<String, String> idsByEpk = new TreeMap<>();
    private final CosmosAsyncContainer container;
    private Range<String> subRange;
    private Range<String> physicalRange;
    private Range<String> crossPartitionRange;

    public FeedRangeTestData(CosmosAsyncClient client, String label) {
        database = TestSuiteBase.createTestDatabase(client, label);
        container = database.getContainer("ranges");
        try {
            PartitionKeyDefinition definition = new PartitionKeyDefinition();
            definition.setPaths(Collections.singletonList("/pk"));
            definition.setVersion(PartitionKeyDefinitionVersion.V2);
            TestSuiteBase.createCollection(database, new CosmosContainerProperties(container.getId(), definition),
                new CosmosContainerRequestOptions(), 12000);
            for (int i = 0; i < 32; i++) {
                String key = "range-" + i;
                ObjectNode document = Utils.getSimpleObjectMapper().createObjectNode().put("id", key).put("pk", key);
                container.createItem(document).block(REQUEST_TIMEOUT);
                PartitionKeyInternal internalKey =
                    PartitionKeyInternal.fromObjectArray(Collections.singletonList(key), true);
                idsByEpk.put(internalKey.getEffectivePartitionKeyString(internalKey, definition), key);
            }

            List<FeedRange> feedRanges = TestSuiteBase.getFeedRangesWithRetry(
                container, "range regression fixture topology");
            assertThat(feedRanges).as("Fixture must span multiple physical partitions").hasSizeGreaterThan(1);
            List<List<String>> partitionPoints = new ArrayList<>();
            for (FeedRange feedRange : feedRanges) {
                Range<String> range = ((FeedRangeEpkImpl) feedRange).getRange();
                List<String> points = idsByEpk.keySet().stream().filter(range::contains).collect(Collectors.toList());
                assertThat(points).as("Every physical partition must contain sentinel documents").isNotEmpty();
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
            assertThat(partitionPoints.stream()
                .filter(partition -> partition.stream().anyMatch(crossPartitionRange::contains)).count())
                .as("Narrowed scope must include data from multiple physical partitions").isGreaterThan(1);
        } catch (RuntimeException | Error failure) {
            try {
                close();
            } catch (RuntimeException cleanupFailure) {
                failure.addSuppressed(cleanupFailure);
            }
            throw failure;
        }
    }

    public CosmosAsyncContainer getContainer() {
        return container;
    }

    public CosmosAsyncContainer createHierarchicalContainer() {
        PartitionKeyDefinition definition = new PartitionKeyDefinition();
        definition.setKind(PartitionKind.MULTI_HASH);
        definition.setVersion(PartitionKeyDefinitionVersion.V2);
        definition.setPaths(Arrays.asList("/tenantId", "/userId"));
        CosmosAsyncContainer hierarchicalContainer = TestSuiteBase.createCollection(
            database, new CosmosContainerProperties("hierarchical", definition),
            new CosmosContainerRequestOptions(), 12000);
        assertThat(TestSuiteBase.getFeedRangesWithRetry(
            hierarchicalContainer, "hierarchical range regression fixture topology"))
            .as("More tenant prefixes than physical partitions guarantees co-located foreign tenants")
            .hasSizeBetween(2, 7);
        for (int tenant = 0; tenant < 8; tenant++) {
            for (int user = 0; user < 4; user++) {
                hierarchicalContainer.createItem(Utils.getSimpleObjectMapper().createObjectNode()
                    .put("id", "tenant-" + tenant + "-user-" + user)
                    .put("tenantId", "tenant-" + tenant).put("userId", "user-" + user)).block(REQUEST_TIMEOUT);
            }
        }
        return hierarchicalContainer;
    }

    public static Object[][] scopes() {
        return new Object[][] {{"subRange"}, {"emptyRange"}, {"physicalRange"}, {"crossPartitionRange"}};
    }

    public Range<String> rangeFor(String scope) {
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

    public List<String> expectedIds(Range<String> range) {
        return idsByEpk.entrySet().stream().filter(entry -> range.contains(entry.getKey()))
            .map(Map.Entry::getValue).collect(Collectors.toList());
    }

    @Override
    public void close() {
        TestSuiteBase.safeDeleteDatabase(database);
    }
}
