// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import com.azure.cosmos.implementation.routing.Range;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.testng.annotations.Test;

import java.util.Arrays;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * Tests for {@link PartitionKeyRange}, focused on the memory-saving "strip unused fields" behavior
 * applied when constructing from a Jackson {@link ObjectNode}.
 *
 * <p>The strip set is intentionally kept aligned with
 * <a href="https://github.com/Azure/azure-sdk-for-python/pull/46297">azure-sdk-for-python#46297</a>.
 * These tests pin that contract.</p>
 */
public class PartitionKeyRangeTest {

    private static final ObjectMapper MAPPER = Utils.getSimpleObjectMapper();

    /** Mirrors the JSON shape the Cosmos DB service returns for a partition key range. */
    private static final String FULL_PK_RANGE_JSON =
        "{"
            + "\"_rid\":\"90t-ALzvP44CAAAAAAAAUA==\","
            + "\"id\":\"0\","
            + "\"_etag\":\"\\\"00001e02-0000-0800-0000-6a2c41690000\\\"\","
            + "\"minInclusive\":\"\","
            + "\"maxExclusive\":\"FF\","
            + "\"ridPrefix\":0,"
            + "\"_self\":\"dbs/90t-AA==/colls/90t-ALzvP44=/pkranges/90t-ALzvP44CAAAAAAAAUA==/\","
            + "\"throughputFraction\":1,"
            + "\"status\":\"online\","
            + "\"parents\":[],"
            + "\"ownedArchivalPKRangeIds\":[],"
            + "\"_ts\":1781285225,"
            + "\"lsn\":87,"
            + "\"_lsn\":87"
            + "}";

    private static ObjectNode fullPkRangeNode() throws Exception {
        return (ObjectNode) MAPPER.readTree(FULL_PK_RANGE_JSON);
    }

    @Test(groups = "unit")
    public void objectNodeConstructor_stripsPythonAlignedFieldSet() throws Exception {
        // Pin the deny-list to the exact set agreed across SDKs (Python PR 46297).
        ObjectNode node = fullPkRangeNode();
        PartitionKeyRange range = new PartitionKeyRange(node);

        // Fields that MUST be dropped.
        assertEquals(range.has("_rid"), false, "_rid must be stripped");
        assertEquals(range.has("_etag"), false, "_etag must be stripped");
        assertEquals(range.has("ridPrefix"), false, "ridPrefix must be stripped");
        assertEquals(range.has("_self"), false, "_self must be stripped");
        assertEquals(range.has("ownedArchivalPKRangeIds"), false, "ownedArchivalPKRangeIds must be stripped");
        assertEquals(range.has("_ts"), false, "_ts must be stripped");
        assertEquals(range.has("lsn"), false, "lsn must be stripped");
    }

    @Test(groups = "unit")
    public void objectNodeConstructor_preservesFieldsNotOnDropList() throws Exception {
        // Forward-compat: fields not on the deny-list pass through even if the Java SDK
        // does not currently consume them. This matches Python's choice and protects
        // future usage.
        ObjectNode node = fullPkRangeNode();
        PartitionKeyRange range = new PartitionKeyRange(node);

        // Routing-map essentials.
        assertEquals(range.getId(), "0");
        assertEquals(range.getMinInclusive(), "");
        assertEquals(range.getMaxExclusive(), "FF");
        assertNotNull(range.getParents());
        assertEquals(range.getParents().size(), 0);

        // Not currently consumed but kept (mirrors Python).
        assertTrue(range.has("throughputFraction"), "throughputFraction should be preserved");
        assertTrue(range.has("status"), "status should be preserved");
        assertTrue(range.has("_lsn"), "_lsn should be preserved");
        assertTrue(range.has("parents"), "parents should be preserved");
    }

    @Test(groups = "unit")
    public void objectNodeConstructor_passesThroughUnknownFutureField() throws Exception {
        // Deny-list (not allow-list) semantics: a new server-side field tomorrow is preserved
        // automatically with zero SDK change.
        String json = "{"
            + "\"id\":\"0\",\"minInclusive\":\"\",\"maxExclusive\":\"FF\","
            + "\"_rid\":\"X==\","                       // dropped
            + "\"futureFieldA\":\"hello\","             // not on drop list -> kept
            + "\"futureFieldB\":{\"nested\":42}"        // not on drop list -> kept
            + "}";
        ObjectNode node = (ObjectNode) MAPPER.readTree(json);
        PartitionKeyRange range = new PartitionKeyRange(node);

        assertEquals(range.has("_rid"), false);
        assertTrue(range.has("futureFieldA"), "unknown future field must pass through");
        assertTrue(range.has("futureFieldB"), "unknown future nested field must pass through");
    }

    @Test(groups = "unit")
    public void objectNodeConstructor_preservesNonEmptyParents() throws Exception {
        // Split-merge bookkeeping uses parents; verify it survives the strip.
        String json = "{"
            + "\"_rid\":\"X==\",\"id\":\"1\",\"_etag\":\"\\\"e\\\"\","
            + "\"minInclusive\":\"\",\"maxExclusive\":\"FF\","
            + "\"_self\":\"x/\",\"parents\":[\"0\"],\"_ts\":1,\"lsn\":1"
            + "}";
        ObjectNode node = (ObjectNode) MAPPER.readTree(json);
        PartitionKeyRange range = new PartitionKeyRange(node);

        assertNotNull(range.getParents());
        assertEquals(range.getParents().size(), 1);
        assertEquals(range.getParents().get(0), "0");
        // Dropped fields still gone.
        assertEquals(range.has("_rid"), false);
        assertEquals(range.has("_self"), false);
        assertEquals(range.has("lsn"), false);
    }

    @Test(groups = "unit")
    public void objectNodeConstructor_equalsAndHashCodeUnchanged() throws Exception {
        // PartitionKeyRange#equals / #hashCode use id/min/max only -- the slim instance must
        // remain value-equal to a manually-constructed instance with the same identity fields.
        ObjectNode node = fullPkRangeNode();
        PartitionKeyRange slim = new PartitionKeyRange(node);
        PartitionKeyRange handBuilt = new PartitionKeyRange("0", "", "FF");

        assertEquals(slim, handBuilt);
        assertEquals(slim.hashCode(), handBuilt.hashCode());
        assertEquals(slim.toRange(), new Range<>("", "FF", true, false));
    }

    @Test(groups = "unit")
    public void objectNodeConstructor_handlesNull() {
        // Defensive: a null ObjectNode argument must not throw; super(null) is the existing
        // pre-PR contract and must be preserved.
        new PartitionKeyRange((ObjectNode) null);
    }

    @Test(groups = "unit")
    public void stringConstructor_isUnaffectedByStrip() {
        // The (String) ctor goes through a different superclass path (JsonSerializable(String))
        // and is used by tests/samples that may want full fidelity for round-trip JSON. It is
        // intentionally NOT touched by the strip optimization; this test pins that.
        PartitionKeyRange range = new PartitionKeyRange(FULL_PK_RANGE_JSON);
        assertEquals(range.has("_rid"), true, "string-ctor PKR retains full fidelity");
        assertEquals(range.has("_self"), true, "string-ctor PKR retains full fidelity");
    }

    @Test(groups = "unit")
    public void deserializationFunnelStripsForFeedResponsePath() throws Exception {
        // JsonSerializable.instantiateFromObjectNodeAndType is the funnel every FeedResponse
        // page uses when deserializing pkranges. Confirm it routes through the
        // PartitionKeyRange(ObjectNode) ctor and therefore inherits the strip.
        ObjectNode node = fullPkRangeNode();
        Object result =
            JsonSerializable.instantiateFromObjectNodeAndType(node, PartitionKeyRange.class);

        assertTrue(result instanceof PartitionKeyRange);
        PartitionKeyRange range = (PartitionKeyRange) result;
        for (String dropped : Arrays.asList(
            "_rid", "_etag", "ridPrefix", "_self", "ownedArchivalPKRangeIds", "_ts", "lsn")) {
            assertEquals(range.has(dropped), false, dropped + " must be stripped via FeedResponse funnel");
        }
        assertEquals(range.getId(), "0");
    }
}
