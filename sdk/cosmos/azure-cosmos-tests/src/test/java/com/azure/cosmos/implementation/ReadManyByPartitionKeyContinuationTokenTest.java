/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.azure.cosmos.implementation;

import com.azure.cosmos.implementation.routing.Range;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.models.SqlParameter;
import com.azure.cosmos.models.SqlQuerySpec;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ReadManyByPartitionKeyContinuationTokenTest {

    private static final String TEST_COLLECTION_RID = "dbs/testDb/colls/testColl";
    private static final String TEST_QUERY_HASH = "12345";
    private static final String TEST_PARTITION_KEY_SET_HASH = "67890";

    /**
     * Builds a BatchDefinition whose batchFilter is the half-open EPK range [min, max).
     */
    private static ReadManyByPartitionKeyContinuationToken.BatchDefinition bd(String min, String max) {
        return new ReadManyByPartitionKeyContinuationToken.BatchDefinition(
            new Range<>(min, max, true, false));
    }

    private static List<ReadManyByPartitionKeyContinuationToken.BatchDefinition> bds(
        ReadManyByPartitionKeyContinuationToken.BatchDefinition... defs) {
        return new ArrayList<>(Arrays.asList(defs));
    }

    @Test(groups = { "unit" })
    public void roundtrip_withBackendContinuation() {
        List<ReadManyByPartitionKeyContinuationToken.BatchDefinition> remaining = bds(
            bd("05C1E0", "0BF333"),
            bd("0BF333", "FF"));
        ReadManyByPartitionKeyContinuationToken.BatchDefinition current = bd("", "05C1E0");
        String backendCont = "eyJDb21wb3NpdGVUb2tlbg==";

        ReadManyByPartitionKeyContinuationToken token =
            new ReadManyByPartitionKeyContinuationToken(
                remaining, current, backendCont, TEST_COLLECTION_RID, TEST_QUERY_HASH, TEST_PARTITION_KEY_SET_HASH);

        String serialized = token.serialize();
        assertThat(serialized).isNotNull().isNotEmpty();

        ReadManyByPartitionKeyContinuationToken deserialized =
            ReadManyByPartitionKeyContinuationToken.deserialize(serialized);

        assertThat(deserialized.getBackendContinuation()).isEqualTo(backendCont);
        assertThat(deserialized.getCollectionRid()).isEqualTo(TEST_COLLECTION_RID);
        assertThat(deserialized.getQueryHash()).isEqualTo(TEST_QUERY_HASH);
        assertThat(deserialized.getPartitionKeySetHash()).isEqualTo(TEST_PARTITION_KEY_SET_HASH);

        ReadManyByPartitionKeyContinuationToken.BatchDefinition currentBatch = deserialized.getCurrentBatch();
        assertThat(currentBatch.getBatchFilter().getMin()).isEqualTo("");
        assertThat(currentBatch.getBatchFilter().getMax()).isEqualTo("05C1E0");

        List<ReadManyByPartitionKeyContinuationToken.BatchDefinition> remainingBatches =
            deserialized.getRemainingBatches();
        assertThat(remainingBatches).hasSize(2);
        assertThat(remainingBatches.get(0).getBatchFilter().getMin()).isEqualTo("05C1E0");
        assertThat(remainingBatches.get(0).getBatchFilter().getMax()).isEqualTo("0BF333");
        assertThat(remainingBatches.get(1).getBatchFilter().getMin()).isEqualTo("0BF333");
        assertThat(remainingBatches.get(1).getBatchFilter().getMax()).isEqualTo("FF");
    }

    @Test(groups = { "unit" })
    public void roundtrip_withNullBackendContinuation() {
        ReadManyByPartitionKeyContinuationToken token =
            new ReadManyByPartitionKeyContinuationToken(
                bds(bd("0BF333", "FF")), bd("05C1E0", "0BF333"),
                null, TEST_COLLECTION_RID, TEST_QUERY_HASH, TEST_PARTITION_KEY_SET_HASH);

        String serialized = token.serialize();
        ReadManyByPartitionKeyContinuationToken deserialized =
            ReadManyByPartitionKeyContinuationToken.deserialize(serialized);

        assertThat(deserialized.getBackendContinuation()).isNull();
        assertThat(deserialized.getCurrentBatch().getBatchFilter().getMin()).isEqualTo("05C1E0");
        assertThat(deserialized.getCurrentBatch().getBatchFilter().getMax()).isEqualTo("0BF333");
        assertThat(deserialized.getRemainingBatches()).hasSize(1);
    }

    @Test(groups = { "unit" })
    public void roundtrip_emptyRemainingBatches() {
        ReadManyByPartitionKeyContinuationToken token =
            new ReadManyByPartitionKeyContinuationToken(
                Collections.emptyList(), bd("0BF333", "FF"),
                "someCont", TEST_COLLECTION_RID, TEST_QUERY_HASH, TEST_PARTITION_KEY_SET_HASH);

        String serialized = token.serialize();
        ReadManyByPartitionKeyContinuationToken deserialized =
            ReadManyByPartitionKeyContinuationToken.deserialize(serialized);

        assertThat(deserialized.getRemainingBatches()).isEmpty();
        assertThat(deserialized.getCurrentBatch().getBatchFilter().getMin()).isEqualTo("0BF333");
        assertThat(deserialized.getBackendContinuation()).isEqualTo("someCont");
    }

    @Test(groups = { "unit" })
    public void roundtrip_lastBatchNoContinuation() {
        ReadManyByPartitionKeyContinuationToken token =
            new ReadManyByPartitionKeyContinuationToken(
                Collections.emptyList(), bd("0BF333", "FF"),
                null, TEST_COLLECTION_RID, TEST_QUERY_HASH, TEST_PARTITION_KEY_SET_HASH);

        String serialized = token.serialize();
        ReadManyByPartitionKeyContinuationToken deserialized =
            ReadManyByPartitionKeyContinuationToken.deserialize(serialized);

        assertThat(deserialized.getRemainingBatches()).isEmpty();
        assertThat(deserialized.getBackendContinuation()).isNull();
    }

    @Test(groups = { "unit" })
    public void setFeedResponseContinuationToken_handlesEmptyHeadersWithoutCopyingNormalCase() {
        Map<String, String> immutableEmptyHeaders = Collections.emptyMap();
        FeedResponse<String> emptyResponse = ModelBridgeInternal.createFeedResponse(
            Collections.emptyList(),
            immutableEmptyHeaders);

        ModelBridgeInternal.setFeedResponseContinuationToken(null, emptyResponse);

        assertThat(emptyResponse.getContinuationToken()).isNull();
        assertThat(emptyResponse.getResponseHeaders()).isSameAs(immutableEmptyHeaders);
        assertThat(emptyResponse.getResponseHeaders()).isEmpty();

        Map<String, String> normalHeaders = new HashMap<>();
        normalHeaders.put(HttpConstants.HttpHeaders.ACTIVITY_ID, "test-activity-id");
        FeedResponse<String> normalResponse = ModelBridgeInternal.createFeedResponse(
            Collections.emptyList(),
            normalHeaders);

        ModelBridgeInternal.setFeedResponseContinuationToken("token", normalResponse);

        assertThat(normalResponse.getContinuationToken()).isEqualTo("token");
        assertThat(normalResponse.getResponseHeaders()).isSameAs(normalHeaders);
    }

    @Test(groups = { "unit" })
    public void deserialize_malformedInput_throws() {
        // Either the base64 decoder or the JSON parsing layer rejects garbage; both raise
        // IllegalArgumentException, which is the contract callers depend on.
        assertThatThrownBy(() ->
            ReadManyByPartitionKeyContinuationToken.deserialize("not-valid-base64!!!")
        ).isInstanceOf(IllegalArgumentException.class);
    }

    @Test(groups = { "unit" })
    public void deserialize_emptyString_throws() {
        assertThatThrownBy(() ->
            ReadManyByPartitionKeyContinuationToken.deserialize("")
        ).isInstanceOf(IllegalArgumentException.class);
    }

    @Test(groups = { "unit" })
    public void deserialize_null_throws() {
        assertThatThrownBy(() ->
            ReadManyByPartitionKeyContinuationToken.deserialize(null)
        ).isInstanceOf(NullPointerException.class);
    }

    @Test(groups = { "unit" })
    public void deserialize_nullRemainingBatches_throws() {
        // Hand-craft a token with "rb":null - a malformed or tampered token.
        String json = "{\"v\":1,\"rb\":null,\"cb\":{\"bf\":{\"min\":\"\",\"max\":\"FF\"}},"
            + "\"bc\":null,\"cr\":\"" + TEST_COLLECTION_RID + "\",\"qh\":\"" + TEST_QUERY_HASH + "\",\"ph\":\"" + TEST_PARTITION_KEY_SET_HASH + "\"}";
        String serialized = java.util.Base64.getEncoder().encodeToString(json.getBytes(java.nio.charset.StandardCharsets.UTF_8));

        assertThatThrownBy(() -> ReadManyByPartitionKeyContinuationToken.deserialize(serialized))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("remainingBatches");
    }

    @Test(groups = { "unit" })
    public void deserialize_nullCurrentBatch_throws() {
        // Hand-craft a token with "cb":null - a malformed or tampered token.
        String json = "{\"v\":1,\"rb\":[],\"cb\":null,"
            + "\"bc\":null,\"cr\":\"" + TEST_COLLECTION_RID + "\",\"qh\":\"" + TEST_QUERY_HASH + "\",\"ph\":\"" + TEST_PARTITION_KEY_SET_HASH + "\"}";
        String serialized = java.util.Base64.getEncoder().encodeToString(json.getBytes(java.nio.charset.StandardCharsets.UTF_8));

        assertThatThrownBy(() -> ReadManyByPartitionKeyContinuationToken.deserialize(serialized))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("currentBatch");
    }

    @Test(groups = { "unit" })
    public void deserialize_unsupportedVersion_throws() {
        // Hand-craft a token JSON with version=999 (a future format) and ensure it is rejected.
        String json = "{\"v\":999,\"rb\":[],\"cb\":{\"bf\":{\"min\":\"\",\"max\":\"FF\"}},"
            + "\"bc\":null,\"cr\":\"" + TEST_COLLECTION_RID + "\",\"qh\":\"" + TEST_QUERY_HASH + "\",\"ph\":\"" + TEST_PARTITION_KEY_SET_HASH + "\"}";
        String serialized = java.util.Base64.getEncoder().encodeToString(json.getBytes(java.nio.charset.StandardCharsets.UTF_8));

        // The root cause carries the precise "Unsupported version" message; the outer wrapper
        // gives a generic "Failed to deserialize" hint. Match against the full chain.
        assertThatThrownBy(() -> ReadManyByPartitionKeyContinuationToken.deserialize(serialized))
            .isInstanceOf(IllegalArgumentException.class)
            .hasStackTraceContaining("Unsupported readManyByPartitionKeys continuation token version");
    }

    @Test(groups = { "unit" })
    public void serialized_includesVersionField_andIsBase64() {
        ReadManyByPartitionKeyContinuationToken token =
            new ReadManyByPartitionKeyContinuationToken(
                Collections.emptyList(), bd("", "FF"), null,
                TEST_COLLECTION_RID, TEST_QUERY_HASH, TEST_PARTITION_KEY_SET_HASH);

        String serialized = token.serialize();
        assertThat(serialized).matches("[A-Za-z0-9+/=]+");

        String json = new String(
            java.util.Base64.getDecoder().decode(serialized),
            java.nio.charset.StandardCharsets.UTF_8);
        assertThat(json).startsWith("{");
        assertThat(json).endsWith("}");
        // The wire format must include the version field so future SDKs can detect/reject
        // tokens from incompatible versions.
        assertThat(json).contains("\"v\":1");
    }

    @Test(groups = { "unit" })
    public void serialized_doesNotIncludePartitionScope() {
        // The partition routing scope is intentionally NOT persisted in the continuation token.
        // It is rederived at execution time from the live PartitionKeyRange cache so partition
        // splits never cause stale routing information to be embedded in a token.
        ReadManyByPartitionKeyContinuationToken token =
            new ReadManyByPartitionKeyContinuationToken(
                bds(bd("AA", "BB"), bd("BB", "CC")),
                bd("", "AA"), null,
                TEST_COLLECTION_RID, TEST_QUERY_HASH, TEST_PARTITION_KEY_SET_HASH);

        String json = new String(
            java.util.Base64.getDecoder().decode(token.serialize()),
            java.nio.charset.StandardCharsets.UTF_8);

        assertThat(json).doesNotContain("\"ps\"");
        assertThat(json).contains("\"bf\"");
    }

    @Test(groups = { "unit" })
    public void version_roundtripsAsCurrent() {
        ReadManyByPartitionKeyContinuationToken token =
            new ReadManyByPartitionKeyContinuationToken(
                Collections.emptyList(), bd("", "FF"), null,
                TEST_COLLECTION_RID, TEST_QUERY_HASH, TEST_PARTITION_KEY_SET_HASH);

        ReadManyByPartitionKeyContinuationToken deserialized =
            ReadManyByPartitionKeyContinuationToken.deserialize(token.serialize());

        assertThat(deserialized.getVersion()).isEqualTo(ReadManyByPartitionKeyContinuationToken.CURRENT_VERSION);
        assertThat(ReadManyByPartitionKeyContinuationToken.CURRENT_VERSION).isEqualTo(1);
    }

    @Test(groups = { "unit" })
    public void rangesPreserveMinMaxInclusive() {
        ReadManyByPartitionKeyContinuationToken token =
            new ReadManyByPartitionKeyContinuationToken(
                bds(bd("CD", "EF")), bd("AB", "CD"), null,
                TEST_COLLECTION_RID, TEST_QUERY_HASH, TEST_PARTITION_KEY_SET_HASH);

        String serialized = token.serialize();
        ReadManyByPartitionKeyContinuationToken deserialized =
            ReadManyByPartitionKeyContinuationToken.deserialize(serialized);

        Range<String> currentFilter = deserialized.getCurrentBatch().getBatchFilter();
        assertThat(currentFilter.isMinInclusive()).isTrue();
        assertThat(currentFilter.isMaxInclusive()).isFalse();
        Range<String> remainingFilter = deserialized.getRemainingBatches().get(0).getBatchFilter();
        assertThat(remainingFilter.isMinInclusive()).isTrue();
        assertThat(remainingFilter.isMaxInclusive()).isFalse();
    }

    @Test(groups = { "unit" })
    public void collectionRidAndQueryHash_roundtrip() {
        String rid = "dbs/myDb/colls/myColl";
        String hash = "98765";

        ReadManyByPartitionKeyContinuationToken token =
            new ReadManyByPartitionKeyContinuationToken(
                Collections.emptyList(), bd("", "FF"), null, rid, hash, TEST_PARTITION_KEY_SET_HASH);

        String serialized = token.serialize();
        ReadManyByPartitionKeyContinuationToken deserialized =
            ReadManyByPartitionKeyContinuationToken.deserialize(serialized);

        assertThat(deserialized.getCollectionRid()).isEqualTo(rid);
        assertThat(deserialized.getQueryHash()).isEqualTo(hash);
    }

    @Test(groups = { "unit" })
    public void partitionKeySetHash_roundtrip() {
        ReadManyByPartitionKeyContinuationToken token =
            new ReadManyByPartitionKeyContinuationToken(
                Collections.emptyList(), bd("", "FF"), null,
                TEST_COLLECTION_RID, TEST_QUERY_HASH, TEST_PARTITION_KEY_SET_HASH);

        String serialized = token.serialize();
        ReadManyByPartitionKeyContinuationToken deserialized =
            ReadManyByPartitionKeyContinuationToken.deserialize(serialized);

        assertThat(deserialized.getPartitionKeySetHash()).isEqualTo(TEST_PARTITION_KEY_SET_HASH);
    }

    @Test(groups = { "unit" })
    public void computePartitionKeySetHash_isStableAcrossDuplicateAndReorderedEpks() {
        List<String> epks1 = Arrays.asList("BB", "AA", "BB", "CC");
        List<String> epks2 = Arrays.asList("CC", "AA", "BB");

        assertThat(ReadManyByPartitionKeyContinuationToken.computePartitionKeySetHash(epks1))
            .isEqualTo(ReadManyByPartitionKeyContinuationToken.computePartitionKeySetHash(epks2));
    }

    @Test(groups = { "unit" })
    public void computePartitionKeySetHash_returnsStableHexDigest() {
        String hash = ReadManyByPartitionKeyContinuationToken.computePartitionKeySetHash(
            Arrays.asList("BB", "AA", "BB", "CC"));

        assertThat(hash).matches("[0-9a-f]{32}");
    }

    @Test(groups = { "unit" })
    public void computeQueryHash_nullSpec_returnsZero() {
        assertThat(ReadManyByPartitionKeyContinuationToken.computeQueryHash(null)).isEqualTo("0");
    }

    @Test(groups = { "unit" })
    public void computeQueryHash_sameQueryText_sameHash() {
        SqlQuerySpec spec1 = new SqlQuerySpec("SELECT * FROM c WHERE c.pk = @pk",
            Collections.singletonList(new SqlParameter("@pk", "value1")));
        SqlQuerySpec spec2 = new SqlQuerySpec("SELECT * FROM c WHERE c.pk = @pk",
            Collections.singletonList(new SqlParameter("@pk", "value1")));

        assertThat(ReadManyByPartitionKeyContinuationToken.computeQueryHash(spec1))
            .isEqualTo(ReadManyByPartitionKeyContinuationToken.computeQueryHash(spec2));
    }

    @Test(groups = { "unit" })
    public void computeQueryHash_differentParams_differentHash() {
        SqlQuerySpec spec1 = new SqlQuerySpec("SELECT * FROM c WHERE c.pk = @pk",
            Collections.singletonList(new SqlParameter("@pk", "value1")));
        SqlQuerySpec spec2 = new SqlQuerySpec("SELECT * FROM c WHERE c.pk = @pk",
            Collections.singletonList(new SqlParameter("@pk", "value2")));

        assertThat(ReadManyByPartitionKeyContinuationToken.computeQueryHash(spec1))
            .isNotEqualTo(ReadManyByPartitionKeyContinuationToken.computeQueryHash(spec2));
    }

    @Test(groups = { "unit" })
    public void computeQueryHash_differentQueryText_differentHash() {
        SqlQuerySpec spec1 = new SqlQuerySpec("SELECT * FROM c");
        SqlQuerySpec spec2 = new SqlQuerySpec("SELECT c.id FROM c");

        assertThat(ReadManyByPartitionKeyContinuationToken.computeQueryHash(spec1))
            .isNotEqualTo(ReadManyByPartitionKeyContinuationToken.computeQueryHash(spec2));
    }

    @Test(groups = { "unit" })
    public void computeQueryHash_noParams_stableHash() {
        SqlQuerySpec spec = new SqlQuerySpec("SELECT * FROM c");
        String hash1 = ReadManyByPartitionKeyContinuationToken.computeQueryHash(spec);
        String hash2 = ReadManyByPartitionKeyContinuationToken.computeQueryHash(spec);
        assertThat(hash1).isEqualTo(hash2);
    }

    @Test(groups = { "unit" })
    public void batchDefinition_roundtrip() {
        ReadManyByPartitionKeyContinuationToken.BatchDefinition currentBd = bd("01", "03");
        List<ReadManyByPartitionKeyContinuationToken.BatchDefinition> remaining =
            Collections.singletonList(bd("05C1E0", "0A"));

        ReadManyByPartitionKeyContinuationToken token =
            new ReadManyByPartitionKeyContinuationToken(
                remaining, currentBd, "cont", TEST_COLLECTION_RID, TEST_QUERY_HASH, TEST_PARTITION_KEY_SET_HASH);

        String serialized = token.serialize();
        ReadManyByPartitionKeyContinuationToken deserialized =
            ReadManyByPartitionKeyContinuationToken.deserialize(serialized);

        ReadManyByPartitionKeyContinuationToken.BatchDefinition currentBatch = deserialized.getCurrentBatch();
        assertThat(currentBatch.getBatchFilter().getMin()).isEqualTo("01");
        assertThat(currentBatch.getBatchFilter().getMax()).isEqualTo("03");

        ReadManyByPartitionKeyContinuationToken.BatchDefinition remainingBatch =
            deserialized.getRemainingBatches().get(0);
        assertThat(remainingBatch.getBatchFilter().getMin()).isEqualTo("05C1E0");
        assertThat(remainingBatch.getBatchFilter().getMax()).isEqualTo("0A");
    }
}
