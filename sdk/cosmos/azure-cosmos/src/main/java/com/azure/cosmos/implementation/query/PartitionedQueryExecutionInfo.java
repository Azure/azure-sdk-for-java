// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.query;

import com.azure.cosmos.implementation.RequestTimeline;
import com.azure.cosmos.implementation.query.hybridsearch.HybridSearchQueryInfo;
import com.azure.cosmos.implementation.routing.PartitionKeyInternalHelper;
import com.azure.cosmos.implementation.routing.Range;
import com.azure.cosmos.implementation.JsonSerializable;
import com.azure.cosmos.models.PartitionKeyDefinition;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Collection;
import java.util.List;

/**
 * Used internally to encapsulates execution information for a query in the Azure Cosmos DB database service.
 */
public final class PartitionedQueryExecutionInfo extends JsonSerializable {
    @SuppressWarnings("unchecked")
    private static final Class<Range<String>> QUERY_RANGES_CLASS = (Class<Range<String>>) Range
            .getEmptyRange((String) null).getClass();

    /**
     * Describes the expected wire format for the {@code queryRanges} field.
     */
    enum QueryRangesFormat {
        /** Ranges use EPK hex strings for {@code min}/{@code max} (e.g., {@code "24F3B4E0..."}). */
        EPK_HEX_STRING,
        /** Ranges use PartitionKeyInternal JSON arrays for {@code min}/{@code max} (e.g., {@code ["value"]}). */
        PARTITION_KEY_INTERNAL_ARRAY
    }

    private QueryInfo queryInfo;
    private List<Range<String>> queryRanges;
    private RequestTimeline queryPlanRequestTimeline;
    private HybridSearchQueryInfo hybridSearchQueryInfo;
    private final QueryRangesFormat expectedQueryRangesFormat;
    private final PartitionKeyDefinition partitionKeyDefinition;

    /**
     * Constructs with EPK hex string format expected for queryRanges.
     */
    public PartitionedQueryExecutionInfo(ObjectNode content, RequestTimeline queryPlanRequestTimeline) {
        super(content);
        this.queryPlanRequestTimeline = queryPlanRequestTimeline;
        this.expectedQueryRangesFormat = QueryRangesFormat.EPK_HEX_STRING;
        this.partitionKeyDefinition = null;
    }

    /**
     * Constructs with PartitionKeyInternal array format expected for queryRanges.
     * The {@code partitionKeyDefinition} is required for converting PartitionKeyInternal
     * values to EPK hex strings.
     *
     * @param partitionKeyDefinition the container's partition key definition, must not be null.
     */
    PartitionedQueryExecutionInfo(ObjectNode content, RequestTimeline queryPlanRequestTimeline, PartitionKeyDefinition partitionKeyDefinition) {
        super(content);
        this.queryPlanRequestTimeline = queryPlanRequestTimeline;
        this.expectedQueryRangesFormat = QueryRangesFormat.PARTITION_KEY_INTERNAL_ARRAY;
        this.partitionKeyDefinition = partitionKeyDefinition;
    }

    public int getVersion() {
        return super.getInt(PartitionedQueryExecutionInfoInternal.PARTITIONED_QUERY_EXECUTION_INFO_VERSION_PROPERTY);
    }

    public QueryInfo getQueryInfo() {
        return this.queryInfo != null ? this.queryInfo
                : (this.queryInfo = super.getObject(
                        PartitionedQueryExecutionInfoInternal.QUERY_INFO_PROPERTY, QueryInfo.class));
    }

    /**
     * Returns the query ranges as sorted EPK hex string ranges.
     * <p>
     * Uses the {@link #expectedQueryRangesFormat} hint to choose the primary deserialization
     * path, then validates by inspecting the actual JSON structure. If the hint doesn't match
     * the actual format, falls through to the other path.
     * <p>
     * Two formats exist:
     * <ul>
     *   <li>{@link QueryRangesFormat#EPK_HEX_STRING}: {@code min}/{@code max} are hex strings
     *       — deserialized directly via {@code getList()}.</li>
     *   <li>{@link QueryRangesFormat#PARTITION_KEY_INTERNAL_ARRAY}: {@code min}/{@code max} are
     *       JSON arrays — converted to EPK hex via
     *       {@link PartitionKeyInternalHelper#convertToSortedEpkRanges}.</li>
     * </ul>
     */
    public List<Range<String>> getQueryRanges() {
        if (this.queryRanges != null) {
            return this.queryRanges;
        }

        boolean isPartitionKeyInternalFormat = false;

        if (this.has(PartitionedQueryExecutionInfoInternal.QUERY_RANGES_PROPERTY)) {
            Collection<ObjectNode> ranges = super.getCollection(
                PartitionedQueryExecutionInfoInternal.QUERY_RANGES_PROPERTY, ObjectNode.class);

            if (ranges != null && !ranges.isEmpty()) {
                JsonNode minNode = ranges.iterator().next().get("min");
                isPartitionKeyInternalFormat = minNode != null && minNode.isArray();
            }
        }

        // If the hint says PARTITION_KEY_INTERNAL_ARRAY, try that first; otherwise EPK_HEX_STRING.
        // If the actual format doesn't match the hint, fall through to the other path.
        if (this.expectedQueryRangesFormat == QueryRangesFormat.PARTITION_KEY_INTERNAL_ARRAY || isPartitionKeyInternalFormat) {
            if (isPartitionKeyInternalFormat) {
                if (this.partitionKeyDefinition == null) {
                    throw new IllegalStateException(
                        "queryRanges are in PartitionKeyInternal array format but partitionKeyDefinition is null.");
                }
                this.queryRanges = PartitionKeyInternalHelper.convertToSortedEpkRanges(
                    PartitionedQueryExecutionInfoInternal.QUERY_RANGES_PROPERTY,
                    this.getPropertyBag(),
                    this.partitionKeyDefinition);
                return this.queryRanges;
            }
        }

        // EPK hex string format — direct deserialization
        this.queryRanges = super.getList(
            PartitionedQueryExecutionInfoInternal.QUERY_RANGES_PROPERTY, QUERY_RANGES_CLASS);
        return this.queryRanges;
    }

    public RequestTimeline getQueryPlanRequestTimeline() {
        return queryPlanRequestTimeline;
    }

    public boolean hasHybridSearchQueryInfo() {
        getHybridSearchQueryInfo();
        return hybridSearchQueryInfo != null;
    }

    public HybridSearchQueryInfo getHybridSearchQueryInfo() {
        if (hybridSearchQueryInfo == null) {
            hybridSearchQueryInfo = super.getObject(
                PartitionedQueryExecutionInfoInternal.HYBRID_SEARCH_QUERY_INFO_PROPERTY, HybridSearchQueryInfo.class);
        }

        return this.hybridSearchQueryInfo;
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
