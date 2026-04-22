// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import com.azure.cosmos.implementation.routing.MurmurHash3_128;
import com.azure.cosmos.implementation.routing.Range;
import com.azure.cosmos.implementation.routing.UInt128;
import com.azure.cosmos.models.SqlParameter;
import com.azure.cosmos.models.SqlQuerySpec;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * Composite continuation token for {@code readManyByPartitionKeys} operations.
 * <p>
 * Captures the state needed to resume a readManyByPartitionKeys operation:
 * <ul>
 *   <li>{@code remainingBatches} — batch definitions of batches not yet started</li>
 *   <li>{@code currentBatch} — batch definition of the batch currently being processed</li>
 *   <li>{@code backendContinuation} — backend query continuation within the current batch (nullable)</li>
 * </ul>
 * Each batch definition has two EPK ranges:
 * <ul>
 *   <li>{@code partitionScope} — the physical partition's EPK range at the time the operation
 *       started. Used for routing queries (via FeedRange). Only causes fan-out overhead if a
 *       split has actually occurred.</li>
 *   <li>{@code batchFilter} — the EPK sub-range that identifies which PKs belong to this batch.
 *       Within a physical partition, PKs are sorted by EPK and split into batches; the filter
 *       range spans the EPKs of the PKs in this batch. Used to reconstruct the exact same set
 *       of PKs per batch when resuming from a continuation token.</li>
 * </ul>
 * EPK ranges are used instead of PkRangeIds so the token survives partition splits.
 * <p>
 * Serialized as JSON → Base64 to keep the token opaque.
 */
public final class ReadManyByPartitionKeyContinuationToken {

    private static final String REMAINING_BATCHES_PROPERTY = "rb";
    private static final String CURRENT_BATCH_PROPERTY = "cb";
    private static final String BACKEND_CONTINUATION_PROPERTY = "bc";
    private static final String COLLECTION_RID_PROPERTY = "cr";
    private static final String QUERY_HASH_PROPERTY = "qh";
    private static final String PARTITION_KEY_SET_HASH_PROPERTY = "ph";

    @JsonProperty(REMAINING_BATCHES_PROPERTY)
    private final List<BatchDefinitionDto> remainingBatches;

    @JsonProperty(CURRENT_BATCH_PROPERTY)
    private final BatchDefinitionDto currentBatch;

    @JsonProperty(BACKEND_CONTINUATION_PROPERTY)
    private final String backendContinuation;

    @JsonProperty(COLLECTION_RID_PROPERTY)
    private final String collectionRid;

    @JsonProperty(QUERY_HASH_PROPERTY)
    private final String queryHash;

    @JsonProperty(PARTITION_KEY_SET_HASH_PROPERTY)
    private final String partitionKeySetHash;

    @JsonCreator
    ReadManyByPartitionKeyContinuationToken(
        @JsonProperty(REMAINING_BATCHES_PROPERTY) List<BatchDefinitionDto> remainingBatches,
        @JsonProperty(CURRENT_BATCH_PROPERTY) BatchDefinitionDto currentBatch,
        @JsonProperty(BACKEND_CONTINUATION_PROPERTY) String backendContinuation,
        @JsonProperty(COLLECTION_RID_PROPERTY) String collectionRid,
        @JsonProperty(QUERY_HASH_PROPERTY) String queryHash,
        @JsonProperty(PARTITION_KEY_SET_HASH_PROPERTY) String partitionKeySetHash) {

        this.remainingBatches = remainingBatches;
        this.currentBatch = currentBatch;
        this.backendContinuation = backendContinuation;
        this.collectionRid = collectionRid;
        this.queryHash = queryHash;
        this.partitionKeySetHash = partitionKeySetHash;
    }

    public ReadManyByPartitionKeyContinuationToken(
        List<BatchDefinition> remainingBatches,
        BatchDefinition currentBatch,
        String backendContinuation,
        String collectionRid,
        int queryHash) {

        this(remainingBatches, currentBatch, backendContinuation, collectionRid, String.valueOf(queryHash), "0");
    }

    public ReadManyByPartitionKeyContinuationToken(
        List<BatchDefinition> remainingBatches,
        BatchDefinition currentBatch,
        String backendContinuation,
        String collectionRid,
        int queryHash,
        int partitionKeySetHash) {

        this(remainingBatches, currentBatch, backendContinuation, collectionRid,
            String.valueOf(queryHash), String.valueOf(partitionKeySetHash));
    }

    public ReadManyByPartitionKeyContinuationToken(
        List<BatchDefinition> remainingBatches,
        BatchDefinition currentBatch,
        String backendContinuation,
        String collectionRid,
        String queryHash) {

        this(remainingBatches, currentBatch, backendContinuation, collectionRid, queryHash, "0");
    }

    public ReadManyByPartitionKeyContinuationToken(
        List<BatchDefinition> remainingBatches,
        BatchDefinition currentBatch,
        String backendContinuation,
        String collectionRid,
        String queryHash,
        String partitionKeySetHash) {

        checkNotNull(currentBatch, "Argument 'currentBatch' must not be null.");
        checkNotNull(remainingBatches, "Argument 'remainingBatches' must not be null.");

        this.remainingBatches = new ArrayList<>(remainingBatches.size());
        for (BatchDefinition bd : remainingBatches) {
            this.remainingBatches.add(BatchDefinitionDto.fromBatchDefinition(bd));
        }
        this.currentBatch = BatchDefinitionDto.fromBatchDefinition(currentBatch);
        this.backendContinuation = backendContinuation;
        this.collectionRid = collectionRid;
        this.queryHash = queryHash;
        this.partitionKeySetHash = partitionKeySetHash;
    }

    /**
     * Convenience constructor that accepts EPK ranges directly (without separate partitionScope/batchFilter).
     * Each range is used as both the partitionScope and batchFilter in the BatchDefinition.
     * This is an interim API until the full batch redesign with distinct partitionScope/batchFilter is wired up.
     */
    public ReadManyByPartitionKeyContinuationToken(
        List<Range<String>> remainingBatchRanges,
        Range<String> currentBatchRange,
        String backendContinuation,
        String collectionRid,
        int queryHash) {

        this(remainingBatchRanges, currentBatchRange, backendContinuation, collectionRid, String.valueOf(queryHash), "0");
    }

    public ReadManyByPartitionKeyContinuationToken(
        List<Range<String>> remainingBatchRanges,
        Range<String> currentBatchRange,
        String backendContinuation,
        String collectionRid,
        int queryHash,
        int partitionKeySetHash) {

        this(remainingBatchRanges, currentBatchRange, backendContinuation, collectionRid,
            String.valueOf(queryHash), String.valueOf(partitionKeySetHash));
    }

    public ReadManyByPartitionKeyContinuationToken(
        List<Range<String>> remainingBatchRanges,
        Range<String> currentBatchRange,
        String backendContinuation,
        String collectionRid,
        String queryHash) {

        this(remainingBatchRanges, currentBatchRange, backendContinuation, collectionRid, queryHash, "0");
    }

    public ReadManyByPartitionKeyContinuationToken(
        List<Range<String>> remainingBatchRanges,
        Range<String> currentBatchRange,
        String backendContinuation,
        String collectionRid,
        String queryHash,
        String partitionKeySetHash) {

        checkNotNull(currentBatchRange, "Argument 'currentBatchRange' must not be null.");
        checkNotNull(remainingBatchRanges, "Argument 'remainingBatchRanges' must not be null.");

        this.remainingBatches = new ArrayList<>(remainingBatchRanges.size());
        for (Range<String> range : remainingBatchRanges) {
            BatchDefinition bd = new BatchDefinition(range, range);
            this.remainingBatches.add(BatchDefinitionDto.fromBatchDefinition(bd));
        }
        this.currentBatch = BatchDefinitionDto.fromBatchDefinition(
            new BatchDefinition(currentBatchRange, currentBatchRange));
        this.backendContinuation = backendContinuation;
        this.collectionRid = collectionRid;
        this.queryHash = queryHash;
        this.partitionKeySetHash = partitionKeySetHash;
    }

    @JsonIgnore
    public List<BatchDefinition> getRemainingBatches() {
        List<BatchDefinition> result = new ArrayList<>(remainingBatches.size());
        for (BatchDefinitionDto dto : remainingBatches) {
            result.add(dto.toBatchDefinition());
        }
        return result;
    }

    @JsonIgnore
    public BatchDefinition getCurrentBatch() {
        return currentBatch.toBatchDefinition();
    }

    public String getBackendContinuation() {
        return backendContinuation;
    }

    public String getCollectionRid() {
        return collectionRid;
    }

    public String getQueryHash() {
        return queryHash;
    }

    public String getPartitionKeySetHash() {
        return partitionKeySetHash;
    }

    /**
     * Computes a stable hash for a SqlQuerySpec (or null for the default SELECT * FROM c query).
     * Hashes over both the query text and all parameter names/values to detect when a continuation
     * token is reused with a different query or different parameter values.
     */
    public static String computeQueryHash(SqlQuerySpec querySpec) {
        if (querySpec == null) {
            return "0";
        }

        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            updateHashInput(output, querySpec.getQueryText());

            List<SqlParameter> params = querySpec.getParameters();
            if (params != null) {
                for (SqlParameter param : params) {
                    updateHashInput(output, param.getName());

                    Object value = param.getValue(Object.class);
                    if (value == null) {
                        updateHashInput(output, null);
                    } else {
                        output.write(Utils.getSimpleObjectMapper().writeValueAsBytes(value));
                        output.write(0);
                    }
                }
            }

            return murmurHash128Hex(output.toByteArray());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to compute stable query hash for continuation token.", e);
        }
    }

    static int computeLegacyQueryHash(SqlQuerySpec querySpec) {
        if (querySpec == null) {
            return 0;
        }
        int hash = 17;
        String queryText = querySpec.getQueryText();
        hash = 31 * hash + (queryText != null ? queryText.hashCode() : 0);
        List<SqlParameter> params = querySpec.getParameters();
        if (params != null) {
            for (SqlParameter param : params) {
                hash = 31 * hash + (param.getName() != null ? param.getName().hashCode() : 0);
                Object value = param.getValue(Object.class);
                hash = 31 * hash + (value != null ? value.hashCode() : 0);
            }
        }
        return hash;
    }

    /**
     * Computes a stable hash for the normalized set of partition key EPK values.
     * Duplicate and reordered inputs intentionally produce the same digest.
     */
    public static String computePartitionKeySetHash(List<String> partitionKeyEpks) {
        if (partitionKeyEpks == null || partitionKeyEpks.isEmpty()) {
            return "0";
        }

        List<String> normalizedEpks = new ArrayList<>(partitionKeyEpks.size());
        for (String epk : partitionKeyEpks) {
            if (epk != null) {
                normalizedEpks.add(epk);
            }
        }

        if (normalizedEpks.isEmpty()) {
            return "0";
        }

        Collections.sort(normalizedEpks);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String previous = null;
        for (String epk : normalizedEpks) {
            if (epk.equals(previous)) {
                continue;
            }

            updateHashInput(output, epk);
            previous = epk;
        }

        return murmurHash128Hex(output.toByteArray());
    }

    static int computeLegacyPartitionKeySetHash(List<String> partitionKeyEpks) {
        if (partitionKeyEpks == null || partitionKeyEpks.isEmpty()) {
            return 0;
        }

        List<String> normalizedEpks = new ArrayList<>(partitionKeyEpks.size());
        for (String epk : partitionKeyEpks) {
            if (epk != null) {
                normalizedEpks.add(epk);
            }
        }

        if (normalizedEpks.isEmpty()) {
            return 0;
        }

        Collections.sort(normalizedEpks);

        int hash = 17;
        String previous = null;
        for (String epk : normalizedEpks) {
            if (epk.equals(previous)) {
                continue;
            }

            hash = 31 * hash + epk.hashCode();
            previous = epk;
        }

        return hash;
    }

    private static void updateHashInput(ByteArrayOutputStream output, String value) {
        if (value != null) {
            output.writeBytes(value.getBytes(StandardCharsets.UTF_8));
        }
        output.write(0);
    }

    private static String murmurHash128Hex(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return "0";
        }

        UInt128 hash = MurmurHash3_128.hash128(bytes, bytes.length);
        return toFixedHex(hash.getHigh()) + toFixedHex(hash.getLow());
    }

    private static String toFixedHex(long value) {
        String hex = Long.toHexString(value);
        if (hex.length() >= 16) {
            return hex;
        }

        StringBuilder builder = new StringBuilder(16);
        for (int i = hex.length(); i < 16; i++) {
            builder.append('0');
        }
        builder.append(hex);
        return builder.toString();
    }

    /**
     * Serializes this token to a Base64-encoded JSON string.
     */
    public String serialize() {
        try {
            String json = Utils.getSimpleObjectMapper().writeValueAsString(this);
            return Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize ReadManyByPartitionKeyContinuationToken.", e);
        }
    }

    /**
     * Deserializes a Base64-encoded JSON string into a continuation token.
     *
     * @param serialized the serialized token (Base64 of JSON)
     * @return the deserialized token
     * @throws IllegalArgumentException if the token is malformed
     */
    public static ReadManyByPartitionKeyContinuationToken deserialize(String serialized) {
        checkNotNull(serialized, "Argument 'serialized' must not be null.");
        checkArgument(!serialized.isEmpty(), "Argument 'serialized' must not be empty.");

        try {
            byte[] decoded = Base64.getDecoder().decode(serialized);
            String json = new String(decoded, StandardCharsets.UTF_8);
            return Utils.getSimpleObjectMapper().readValue(json, ReadManyByPartitionKeyContinuationToken.class);
        } catch (Exception e) {
            throw new IllegalArgumentException(
                "Failed to deserialize ReadManyByPartitionKeyContinuationToken. " +
                    "The continuation token may be malformed or from an incompatible version.", e);
        }
    }

    /**
     * Identifies a single batch in a readManyByPartitionKeys operation.
     * <p>
     * Each batch has two EPK ranges:
     * <ul>
     *   <li>{@code partitionScope} — the physical partition's full EPK range. Used for routing
     *       the query to the correct physical partition(s) via FeedRange.</li>
     *   <li>{@code batchFilter} — the EPK sub-range within the partition that identifies which
     *       PKs belong to this batch. PKs whose EPK falls within [filterMin, filterMax) are
     *       part of this batch.</li>
     * </ul>
     */
    public static final class BatchDefinition {
        private final Range<String> partitionScope;
        private final Range<String> batchFilter;

        public BatchDefinition(Range<String> partitionScope, Range<String> batchFilter) {
            this.partitionScope = checkNotNull(partitionScope, "Argument 'partitionScope' must not be null.");
            this.batchFilter = checkNotNull(batchFilter, "Argument 'batchFilter' must not be null.");
        }

        public Range<String> getPartitionScope() {
            return partitionScope;
        }

        public Range<String> getBatchFilter() {
            return batchFilter;
        }
    }

    /**
     * Compact DTO for JSON serialization of a batch definition.
     */
    static final class BatchDefinitionDto {
        private final EpkRangeDto ps;
        private final EpkRangeDto bf;

        @JsonCreator
        BatchDefinitionDto(
            @JsonProperty("ps") EpkRangeDto ps,
            @JsonProperty("bf") EpkRangeDto bf) {
            this.ps = ps;
            this.bf = bf;
        }

        @JsonProperty("ps")
        EpkRangeDto getPs() { return ps; }

        @JsonProperty("bf")
        EpkRangeDto getBf() { return bf; }

        static BatchDefinitionDto fromBatchDefinition(BatchDefinition bd) {
            return new BatchDefinitionDto(
                EpkRangeDto.fromRange(bd.partitionScope),
                EpkRangeDto.fromRange(bd.batchFilter));
        }

        BatchDefinition toBatchDefinition() {
            return new BatchDefinition(ps.toRange(), bf.toRange());
        }
    }

    /**
     * Minimal DTO for EPK range serialization. Uses short field names to keep the
     * serialized token compact.
     */
    static final class EpkRangeDto {
        @JsonProperty("min")
        private final String min;
        @JsonProperty("max")
        private final String max;

        @JsonCreator
        EpkRangeDto(
            @JsonProperty("min") String min,
            @JsonProperty("max") String max) {
            this.min = min;
            this.max = max;
        }

        static EpkRangeDto fromRange(Range<String> range) {
            if (!range.isMinInclusive()) {
                throw new IllegalArgumentException("EPK ranges must be minInclusive.");
            }
            if (range.isMaxInclusive()) {
                throw new IllegalArgumentException("EPK ranges must be maxExclusive.");
            }
            return new EpkRangeDto(range.getMin(), range.getMax());
        }

        Range<String> toRange() {
            return new Range<>(min, max, true, false);
        }
    }
}
