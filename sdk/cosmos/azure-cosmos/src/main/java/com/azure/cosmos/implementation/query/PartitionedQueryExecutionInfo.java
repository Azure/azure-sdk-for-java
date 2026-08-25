// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.query;

import com.azure.cosmos.implementation.RequestTimeline;
import com.azure.cosmos.implementation.query.hybridsearch.HybridSearchQueryInfo;
import com.azure.cosmos.implementation.routing.PartitionKeyInternalHelper;
import com.azure.cosmos.implementation.routing.Range;
import com.azure.cosmos.implementation.JsonSerializable;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.azure.cosmos.models.PartitionKeyDefinition;

import java.util.List;

/**
 * Used internally to encapsulates execution information for a query in the Azure Cosmos DB database service.
 */
public final class PartitionedQueryExecutionInfo extends JsonSerializable {
    @SuppressWarnings("unchecked")
    private static final Class<Range<String>> QUERY_RANGES_CLASS = (Class<Range<String>>) Range
            .getEmptyRange((String) null).getClass();

    private QueryInfo queryInfo;
    private List<Range<String>> queryRanges;
    private RequestTimeline queryPlanRequestTimeline;
    private HybridSearchQueryInfo hybridSearchQueryInfo;

    /**
     * Constructs with EPK hex string format expected for queryRanges.
     */
    public PartitionedQueryExecutionInfo(ObjectNode content, RequestTimeline queryPlanRequestTimeline) {
        super(content);
        this.queryPlanRequestTimeline = queryPlanRequestTimeline;
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
        if (partitionKeyDefinition == null) {
            throw new IllegalArgumentException("partitionKeyDefinition must not be null");
        }
        this.queryPlanRequestTimeline = queryPlanRequestTimeline;
        this.queryRanges = PartitionKeyInternalHelper.convertToSortedEpkRanges(
            PartitionedQueryExecutionInfoInternal.QUERY_RANGES_PROPERTY,
            content,
            partitionKeyDefinition);
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
     * Two formats exist:
     * <ul>
     *   <li>Gateway V1: {@code min}/{@code max} are EPK hex strings and are
     *       deserialized directly via {@code getList()}.</li>
     *   <li>Thin client proxy: {@code min}/{@code max} are PartitionKeyInternal
     *       JSON arrays and are converted to EPK hex by the thin-client constructor.</li>
     * </ul>
     */
    public List<Range<String>> getQueryRanges() {
        if (this.queryRanges != null) {
            return this.queryRanges;
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
