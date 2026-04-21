/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.azure.cosmos.implementation;

import com.azure.cosmos.implementation.routing.Range;
import com.azure.cosmos.models.SqlParameter;
import com.azure.cosmos.models.SqlQuerySpec;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ReadManyByPartitionKeyContinuationTokenTest {

    private static final String TEST_COLLECTION_RID = "dbs/testDb/colls/testColl";
    private static final int TEST_QUERY_HASH = 12345;

    @Test(groups = { "unit" })
    public void roundtrip_withBackendContinuation() {
        List<Range<String>> remaining = Arrays.asList(
            new Range<>("05C1E0", "0BF333", true, false),
            new Range<>("0BF333", "FF", true, false));
        Range<String> current = new Range<>("", "05C1E0", true, false);
        String backendCont = "eyJDb21wb3NpdGVUb2tlbg==";

        ReadManyByPartitionKeyContinuationToken token =
            new ReadManyByPartitionKeyContinuationToken(
                remaining, current, backendCont, TEST_COLLECTION_RID, TEST_QUERY_HASH);

        String serialized = token.serialize();
        assertThat(serialized).isNotNull().isNotEmpty();

        ReadManyByPartitionKeyContinuationToken deserialized =
            ReadManyByPartitionKeyContinuationToken.deserialize(serialized);

        assertThat(deserialized.getBackendContinuation()).isEqualTo(backendCont);
        assertThat(deserialized.getCollectionRid()).isEqualTo(TEST_COLLECTION_RID);
        assertThat(deserialized.getQueryHash()).isEqualTo(TEST_QUERY_HASH);

        ReadManyByPartitionKeyContinuationToken.BatchDefinition currentBatch = deserialized.getCurrentBatch();
        assertThat(currentBatch.getPartitionScope().getMin()).isEqualTo("");
        assertThat(currentBatch.getPartitionScope().getMax()).isEqualTo("05C1E0");

        List<ReadManyByPartitionKeyContinuationToken.BatchDefinition> remainingBatches =
            deserialized.getRemainingBatches();
        assertThat(remainingBatches).hasSize(2);
        assertThat(remainingBatches.get(0).getPartitionScope().getMin()).isEqualTo("05C1E0");
        assertThat(remainingBatches.get(0).getPartitionScope().getMax()).isEqualTo("0BF333");
        assertThat(remainingBatches.get(1).getPartitionScope().getMin()).isEqualTo("0BF333");
        assertThat(remainingBatches.get(1).getPartitionScope().getMax()).isEqualTo("FF");
    }

    @Test(groups = { "unit" })
    public void roundtrip_withNullBackendContinuation() {
        List<Range<String>> remaining = Collections.singletonList(
            new Range<>("0BF333", "FF", true, false));
        Range<String> current = new Range<>("05C1E0", "0BF333", true, false);

        ReadManyByPartitionKeyContinuationToken token =
            new ReadManyByPartitionKeyContinuationToken(
                remaining, current, null, TEST_COLLECTION_RID, TEST_QUERY_HASH);

        String serialized = token.serialize();
        ReadManyByPartitionKeyContinuationToken deserialized =
            ReadManyByPartitionKeyContinuationToken.deserialize(serialized);

        assertThat(deserialized.getBackendContinuation()).isNull();
        assertThat(deserialized.getCurrentBatch().getPartitionScope().getMin()).isEqualTo("05C1E0");
        assertThat(deserialized.getCurrentBatch().getPartitionScope().getMax()).isEqualTo("0BF333");
        assertThat(deserialized.getRemainingBatches()).hasSize(1);
    }

    @Test(groups = { "unit" })
    public void roundtrip_emptyRemainingBatches() {
        Range<String> current = new Range<>("0BF333", "FF", true, false);

        ReadManyByPartitionKeyContinuationToken token =
            new ReadManyByPartitionKeyContinuationToken(
                Collections.emptyList(), current, "someCont", TEST_COLLECTION_RID, TEST_QUERY_HASH);

        String serialized = token.serialize();
        ReadManyByPartitionKeyContinuationToken deserialized =
            ReadManyByPartitionKeyContinuationToken.deserialize(serialized);

        assertThat(deserialized.getRemainingBatches()).isEmpty();
        assertThat(deserialized.getCurrentBatch().getPartitionScope().getMin()).isEqualTo("0BF333");
        assertThat(deserialized.getBackendContinuation()).isEqualTo("someCont");
    }

    @Test(groups = { "unit" })
    public void roundtrip_lastBatchNoContinuation() {
        Range<String> current = new Range<>("0BF333", "FF", true, false);

        ReadManyByPartitionKeyContinuationToken token =
            new ReadManyByPartitionKeyContinuationToken(
                Collections.emptyList(), current, null, TEST_COLLECTION_RID, TEST_QUERY_HASH);

        String serialized = token.serialize();
        ReadManyByPartitionKeyContinuationToken deserialized =
            ReadManyByPartitionKeyContinuationToken.deserialize(serialized);

        assertThat(deserialized.getRemainingBatches()).isEmpty();
        assertThat(deserialized.getBackendContinuation()).isNull();
    }

    @Test(groups = { "unit" })
    public void deserialize_malformedInput_throws() {
        assertThatThrownBy(() ->
            ReadManyByPartitionKeyContinuationToken.deserialize("not-valid-base64!!!")
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessageContaining("Failed to deserialize");
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
    public void serialized_isBase64() {
        Range<String> current = new Range<>("", "FF", true, false);
        ReadManyByPartitionKeyContinuationToken token =
            new ReadManyByPartitionKeyContinuationToken(
                Collections.emptyList(), current, null, TEST_COLLECTION_RID, 0);

        String serialized = token.serialize();

        // Should be valid Base64 (no whitespace, no special chars except +/=)
        assertThat(serialized).matches("[A-Za-z0-9+/=]+");

        // Decoding should produce valid JSON
        String json = new String(
            java.util.Base64.getDecoder().decode(serialized),
            java.nio.charset.StandardCharsets.UTF_8);
        assertThat(json).startsWith("{");
        assertThat(json).endsWith("}");
    }

    @Test(groups = { "unit" })
    public void rangesPreserveMinMaxInclusive() {
        Range<String> current = new Range<>("AB", "CD", true, false);
        ReadManyByPartitionKeyContinuationToken token =
            new ReadManyByPartitionKeyContinuationToken(
                Collections.singletonList(new Range<>("CD", "EF", true, false)),
                current, null, TEST_COLLECTION_RID, TEST_QUERY_HASH);

        String serialized = token.serialize();
        ReadManyByPartitionKeyContinuationToken deserialized =
            ReadManyByPartitionKeyContinuationToken.deserialize(serialized);

        // Verify that deserialized ranges have the canonical min-inclusive, max-exclusive form
        Range<String> currentScope = deserialized.getCurrentBatch().getPartitionScope();
        assertThat(currentScope.isMinInclusive()).isTrue();
        assertThat(currentScope.isMaxInclusive()).isFalse();
        Range<String> remainingScope = deserialized.getRemainingBatches().get(0).getPartitionScope();
        assertThat(remainingScope.isMinInclusive()).isTrue();
        assertThat(remainingScope.isMaxInclusive()).isFalse();
    }

    @Test(groups = { "unit" })
    public void collectionRidAndQueryHash_roundtrip() {
        Range<String> current = new Range<>("", "FF", true, false);
        String rid = "dbs/myDb/colls/myColl";
        int hash = 98765;

        ReadManyByPartitionKeyContinuationToken token =
            new ReadManyByPartitionKeyContinuationToken(
                Collections.emptyList(), current, null, rid, hash);

        String serialized = token.serialize();
        ReadManyByPartitionKeyContinuationToken deserialized =
            ReadManyByPartitionKeyContinuationToken.deserialize(serialized);

        assertThat(deserialized.getCollectionRid()).isEqualTo(rid);
        assertThat(deserialized.getQueryHash()).isEqualTo(hash);
    }

    @Test(groups = { "unit" })
    public void computeQueryHash_nullSpec_returnsZero() {
        assertThat(ReadManyByPartitionKeyContinuationToken.computeQueryHash(null)).isEqualTo(0);
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
        int hash1 = ReadManyByPartitionKeyContinuationToken.computeQueryHash(spec);
        int hash2 = ReadManyByPartitionKeyContinuationToken.computeQueryHash(spec);
        assertThat(hash1).isEqualTo(hash2);
    }

    @Test(groups = { "unit" })
    public void batchDefinition_roundtrip() {
        Range<String> partitionScope = new Range<>("", "05C1E0", true, false);
        Range<String> batchFilter = new Range<>("01", "03", true, false);

        ReadManyByPartitionKeyContinuationToken.BatchDefinition bd =
            new ReadManyByPartitionKeyContinuationToken.BatchDefinition(partitionScope, batchFilter);

        List<ReadManyByPartitionKeyContinuationToken.BatchDefinition> remaining = Collections.singletonList(
            new ReadManyByPartitionKeyContinuationToken.BatchDefinition(
                new Range<>("05C1E0", "FF", true, false),
                new Range<>("05C1E0", "0A", true, false)));

        ReadManyByPartitionKeyContinuationToken token =
            new ReadManyByPartitionKeyContinuationToken(
                remaining, bd, "cont", TEST_COLLECTION_RID, TEST_QUERY_HASH);

        String serialized = token.serialize();
        ReadManyByPartitionKeyContinuationToken deserialized =
            ReadManyByPartitionKeyContinuationToken.deserialize(serialized);

        ReadManyByPartitionKeyContinuationToken.BatchDefinition currentBatch = deserialized.getCurrentBatch();
        assertThat(currentBatch.getPartitionScope().getMin()).isEqualTo("");
        assertThat(currentBatch.getPartitionScope().getMax()).isEqualTo("05C1E0");
        assertThat(currentBatch.getBatchFilter().getMin()).isEqualTo("01");
        assertThat(currentBatch.getBatchFilter().getMax()).isEqualTo("03");

        ReadManyByPartitionKeyContinuationToken.BatchDefinition remainingBatch =
            deserialized.getRemainingBatches().get(0);
        assertThat(remainingBatch.getPartitionScope().getMin()).isEqualTo("05C1E0");
        assertThat(remainingBatch.getBatchFilter().getMin()).isEqualTo("05C1E0");
        assertThat(remainingBatch.getBatchFilter().getMax()).isEqualTo("0A");
    }
}
