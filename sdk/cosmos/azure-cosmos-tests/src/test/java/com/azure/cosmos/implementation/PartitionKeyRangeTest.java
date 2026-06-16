// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import com.azure.cosmos.implementation.routing.Range;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * Tests for {@link PartitionKeyRange}, focused on the memory-saving "strip unused fields" behavior
 * applied when constructing from a Jackson {@link ObjectNode}.
 *
 * <p>The retained-field set is intentionally kept aligned with
 * <a href="https://github.com/Azure/azure-sdk-for-python/pull/46297">azure-sdk-for-python#46297</a>
 * (Python's {@code PKRange} namedtuple). These tests pin that contract.</p>
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

    /**
     * Allow-list kept in heap on every deserialized {@link PartitionKeyRange}.
     *
     * <p>Includes Python's {@code PKRange} namedtuple slots
     * ({@code id}, {@code minInclusive}, {@code maxExclusive}, {@code parents},
     * {@code status}, {@code throughputFraction}) plus {@code _rid} — Java-specific because
     * {@code AddressResolver.isSameCollection} reads {@code getResourceId()} on a
     * {@code PartitionKeyRange} during retry target-change detection.</p>
     */
    private static final List<String> KEPT_FIELDS = Arrays.asList(
        "id", "minInclusive", "maxExclusive", "parents", "status", "throughputFraction", "_rid");

    /** All non-kept fields present in the full payload above; everything here must be stripped. */
    private static final List<String> STRIPPED_FIELDS = Arrays.asList(
        "_etag", "ridPrefix", "_self", "ownedArchivalPKRangeIds", "_ts", "lsn", "_lsn");

    private static ObjectNode fullPkRangeNode() throws Exception {
        return (ObjectNode) MAPPER.readTree(FULL_PK_RANGE_JSON);
    }

    @Test(groups = "unit")
    public void objectNodeConstructor_stripsEverythingNotOnAllowList() throws Exception {
        // Pin the allow-list: every field not on Python's PKRange namedtuple must be dropped.
        ObjectNode node = fullPkRangeNode();
        PartitionKeyRange range = new PartitionKeyRange(node);

        for (String dropped : STRIPPED_FIELDS) {
            assertEquals(range.has(dropped), false, dropped + " must be stripped (not on allow-list)");
        }
    }

    @Test(groups = "unit")
    public void objectNodeConstructor_preservesAllowListedFields() throws Exception {
        // Every field on the allow-list must survive the strip.
        ObjectNode node = fullPkRangeNode();
        PartitionKeyRange range = new PartitionKeyRange(node);

        // Routing-map essentials.
        assertEquals(range.getId(), "0");
        assertEquals(range.getMinInclusive(), "");
        assertEquals(range.getMaxExclusive(), "FF");
        assertNotNull(range.getParents());
        assertEquals(range.getParents().size(), 0);

        // _rid is on the allow-list specifically so AddressResolver.isSameCollection
        // can call getResourceId() on a deserialized PartitionKeyRange during retry
        // target-change detection. Without this, ResourceId.parse(null) throws
        // "INVALID resource id null" on the first retry after a 410/Gone.
        assertEquals(range.getResourceId(), "90t-ALzvP44CAAAAAAAAUA==");

        for (String kept : KEPT_FIELDS) {
            assertTrue(range.has(kept), kept + " must be preserved (on allow-list)");
        }
    }

    @Test(groups = "unit")
    public void objectNodeConstructor_dropsUnknownFutureField() throws Exception {
        // Allow-list (not deny-list) semantics: a new server-side field tomorrow is dropped
        // by default so per-instance heap stays bounded against payload growth. Mirrors
        // Python's PKRange namedtuple, which has no slot for unknown fields.
        String json = "{"
            + "\"id\":\"0\",\"minInclusive\":\"\",\"maxExclusive\":\"FF\","
            + "\"futureFieldA\":\"hello\","
            + "\"futureFieldB\":{\"nested\":42}"
            + "}";
        ObjectNode node = (ObjectNode) MAPPER.readTree(json);
        PartitionKeyRange range = new PartitionKeyRange(node);

        assertEquals(range.getId(), "0");
        assertEquals(range.has("futureFieldA"), false, "unknown future field must be dropped by allow-list");
        assertEquals(range.has("futureFieldB"), false, "unknown future nested field must be dropped by allow-list");
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
        // Dropped fields still gone. _rid stays now that it's on the allow-list.
        assertEquals(range.has("_self"), false);
        assertEquals(range.has("lsn"), false);
        assertEquals(range.getResourceId(), "X==");
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
    public void deserializationFunnelStripsForFeedResponsePath() throws Exception {
        // JsonSerializable.instantiateFromObjectNodeAndType is the funnel every FeedResponse
        // page uses when deserializing pkranges. Confirm it routes through the
        // PartitionKeyRange(ObjectNode) ctor and therefore inherits the strip.
        ObjectNode node = fullPkRangeNode();
        Object result =
            JsonSerializable.instantiateFromObjectNodeAndType(node, PartitionKeyRange.class);

        assertTrue(result instanceof PartitionKeyRange);
        PartitionKeyRange range = (PartitionKeyRange) result;
        for (String dropped : STRIPPED_FIELDS) {
            assertEquals(range.has(dropped), false, dropped + " must be stripped via FeedResponse funnel");
        }
        assertEquals(range.getId(), "0");
    }
}
