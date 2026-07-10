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
 *   <li>{@code remainingBatches} - batch definitions of batches not yet started</li>
 *   <li>{@code currentBatch} - batch definition of the batch currently being processed</li>
 *   <li>{@code backendContinuation} - backend query continuation within the current batch (nullable)</li>
 * </ul>
 * Each batch is identified solely by its {@link BatchDefinition#getBatchFilter() batchFilter}
 * EPK range - the half-open range {@code [minInclusive, maxExclusive)} that contains the EPKs
 * of all PKs assigned to the batch. The physical-partition routing range used at execution
 * time (the FeedRange set on {@code CosmosQueryRequestOptions}) is <strong>not persisted</strong>;
 * it is rederived at execution time from the current PartitionKeyRange cache by taking the
 * union of all partition-key-range EPK ranges that overlap the batch filter
 * ({@code [min(minEpk), max(maxEpk))}).
 * <p>
 * This means the token survives partition splits without ever encoding stale partition
 * boundaries: after a split, the rederived routing range exactly matches the new physical
 * partition boundaries (one or more of them), keeping query-RU cost minimal. It also keeps
 * the serialized token small (one EPK range per batch instead of two).
 * <p>
 * Serialized as JSON -> Base64 to keep the token opaque. The serialized form embeds a
 * {@code "v"} version field so future format evolutions can be detected and rejected
 * (or migrated) cleanly without silently misinterpreting an older token.
 */
public final class ReadManyByPartitionKeyContinuationToken {

    /** Wire format version. Bump on any breaking change to the JSON shape. */
    public static final int CURRENT_VERSION = 1;

    private static final String VERSION_PROPERTY = "v";
    private static final String REMAINING_BATCHES_PROPERTY = "rb";
    private static final String CURRENT_BATCH_PROPERTY = "cb";
    private static final String BACKEND_CONTINUATION_PROPERTY = "bc";
    private static final String COLLECTION_RID_PROPERTY = "cr";
    private static final String QUERY_HASH_PROPERTY = "qh";
    private static final String PARTITION_KEY_SET_HASH_PROPERTY = "ph";

    @JsonProperty(VERSION_PROPERTY)
    private final int version;

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

    /**
     * Production constructor used by RxDocumentClientImpl when stamping each FeedResponse
     * with a continuation token. Callers supply just the batch filter for both the current
     * batch and every remaining batch, plus all three identity fingerprints
     * (collectionRid + queryHash + partitionKeySetHash). Routing scopes are not persisted.
     */
    public ReadManyByPartitionKeyContinuationToken(
        List<BatchDefinition> remainingBatches,
        BatchDefinition currentBatch,
        String backendContinuation,
        String collectionRid,
        String queryHash,
        String partitionKeySetHash) {

        checkNotNull(currentBatch, "Argument 'currentBatch' must not be null.");
        checkNotNull(remainingBatches, "Argument 'remainingBatches' must not be null.");

        this.version = CURRENT_VERSION;
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
     * Deserialization constructor invoked by Jackson. Validates the version field so a
     * token from an incompatible future SDK is rejected with a clear error rather than
     * being silently misinterpreted.
     */
    @JsonCreator
    ReadManyByPartitionKeyContinuationToken(
        @JsonProperty(VERSION_PROPERTY) Integer version,
        @JsonProperty(REMAINING_BATCHES_PROPERTY) List<BatchDefinitionDto> remainingBatches,
        @JsonProperty(CURRENT_BATCH_PROPERTY) BatchDefinitionDto currentBatch,
        @JsonProperty(BACKEND_CONTINUATION_PROPERTY) String backendContinuation,
        @JsonProperty(COLLECTION_RID_PROPERTY) String collectionRid,
        @JsonProperty(QUERY_HASH_PROPERTY) String queryHash,
        @JsonProperty(PARTITION_KEY_SET_HASH_PROPERTY) String partitionKeySetHash) {

        // Tokens written before the version field existed will deserialize with version == null.
        // Treat null as version 1 (the format that existed when this field was introduced) to
        // remain forward-compatible with any tokens emitted by an in-flight pre-versioned beta.
        int effectiveVersion = (version == null) ? 1 : version;
        if (effectiveVersion != CURRENT_VERSION) {
            throw new IllegalArgumentException(
                "Unsupported readManyByPartitionKeys continuation token version: " + effectiveVersion
                    + ". This SDK supports version " + CURRENT_VERSION + ".");
        }

        this.version = effectiveVersion;

        if (remainingBatches == null) {
            throw new IllegalArgumentException(
                "Malformed readManyByPartitionKeys continuation token: 'remainingBatches' is required.");
        }
        if (currentBatch == null) {
            throw new IllegalArgumentException(
                "Malformed readManyByPartitionKeys continuation token: 'currentBatch' is required.");
        }
        this.remainingBatches = remainingBatches;
        this.currentBatch = currentBatch;
        this.backendContinuation = backendContinuation;
        this.collectionRid = collectionRid;
        this.queryHash = queryHash;
        this.partitionKeySetHash = partitionKeySetHash;
    }

    @JsonIgnore
    public int getVersion() {
        return version;
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

    /**
     * Computes a stable hash for the normalized set of partition key EPK values.
     * Duplicate and reordered inputs intentionally produce the same digest.
     */
    public static String computePartitionKeySetHash(List<String> partitionKeyEpks) {
        return computePartitionKeySetHash(partitionKeyEpks, false);
    }

    /**
     * Computes a stable hash for the normalized set of partition key EPK values.
     * When {@code alreadySorted} is true, the input is assumed to be
     * already sorted (e.g. from {@code normalizePartitionKeys}),
     * skipping the O(n log n) sort pass.
     */
    public static String computePartitionKeySetHash(List<String> partitionKeyEpks, boolean alreadySorted) {
        if (partitionKeyEpks == null || partitionKeyEpks.isEmpty()) {
            return "0";
        }

        List<String> normalizedEpks = null;
        if (alreadySorted) {
            normalizedEpks = partitionKeyEpks;
        } else {

            normalizedEpks = new ArrayList<>(partitionKeyEpks.size());
            for (String epk : partitionKeyEpks) {
                if (epk != null) {
                    normalizedEpks.add(epk);
                }
            }

            if (normalizedEpks.isEmpty()) {
                return "0";
            }

            Collections.sort(normalizedEpks);
        }

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

    private static void updateHashInput(ByteArrayOutputStream output, String value) {
        if (value != null) {
            byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
            output.write(bytes, 0, bytes.length);
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
     * Serializes this token to a URL-safe Base64-encoded JSON string.
     */
    public String serialize() {
        try {
            String json = Utils.getSimpleObjectMapper().writeValueAsString(this);
            return Base64.getUrlEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize ReadManyByPartitionKeyContinuationToken.", e);
        }
    }

    /**
     * Deserializes a URL-safe Base64-encoded JSON string into a continuation token.
     *
     * @param serialized the serialized token (URL-safe Base64 of JSON)
     * @return the deserialized token
     * @throws IllegalArgumentException if the token is malformed
     */
    public static ReadManyByPartitionKeyContinuationToken deserialize(String serialized) {
        checkNotNull(serialized, "Argument 'serialized' must not be null.");
        checkArgument(!serialized.isEmpty(), "Argument 'serialized' must not be empty.");

        try {
            byte[] decoded = Base64.getUrlDecoder().decode(serialized);
            String json = new String(decoded, StandardCharsets.UTF_8);
            return Utils.getSimpleObjectMapper().readValue(json, ReadManyByPartitionKeyContinuationToken.class);
        } catch (IllegalArgumentException e) {
            // Preserve our own IllegalArgumentException (version mismatch, null fields) without wrapping.
            throw e;
        } catch (Exception e) {
            // Jackson wraps constructor-thrown IllegalArgumentException inside JsonMappingException.
            // Unwrap to preserve the actionable error message from our null checks.
            Throwable cause = e.getCause();
            if (cause instanceof IllegalArgumentException) {
                throw (IllegalArgumentException) cause;
            }
            throw new IllegalArgumentException(
                "Failed to deserialize ReadManyByPartitionKeyContinuationToken. "
                    + "The continuation token may be malformed or from an incompatible version.", e);
        }
    }

    /**
     * Identifies a single batch in a readManyByPartitionKeys operation by its EPK filter range.
     * <p>
     * The {@code batchFilter} is the half-open EPK range {@code [minInclusive, maxExclusive)}
     * containing the EPKs of all PKs assigned to the batch. It is the only piece of routing
     * data persisted in the continuation token; the physical-partition scope used as the
     * query FeedRange is rederived at execution time from the current PartitionKeyRange
     * cache (union of overlapping partition-key-range EPK ranges) so partition splits do not
     * cause stale routing information to be embedded in the token.
     */
    public static final class BatchDefinition {
        private final Range<String> batchFilter;

        public BatchDefinition(Range<String> batchFilter) {
            this.batchFilter = checkNotNull(batchFilter, "Argument 'batchFilter' must not be null.");
        }

        public Range<String> getBatchFilter() {
            return batchFilter;
        }
    }

    /**
     * Compact DTO for JSON serialization of a batch definition.
     * Persists only the batch filter; routing scope is rederived at execution time.
     */
    static final class BatchDefinitionDto {
        private final EpkRangeDto bf;

        @JsonCreator
        BatchDefinitionDto(@JsonProperty("bf") EpkRangeDto bf) {
            this.bf = bf;
        }

        @JsonProperty("bf")
        EpkRangeDto getBf() { return bf; }

        static BatchDefinitionDto fromBatchDefinition(BatchDefinition bd) {
            return new BatchDefinitionDto(EpkRangeDto.fromRange(bd.batchFilter));
        }

        BatchDefinition toBatchDefinition() {
            return new BatchDefinition(bf.toRange());
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
