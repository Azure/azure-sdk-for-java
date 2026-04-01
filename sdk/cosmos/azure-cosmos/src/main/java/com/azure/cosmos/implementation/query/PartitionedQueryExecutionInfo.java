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

    private QueryInfo queryInfo;
    private List<Range<String>> queryRanges;
    private RequestTimeline queryPlanRequestTimeline;
    private HybridSearchQueryInfo hybridSearchQueryInfo;
    private final PartitionKeyDefinition partitionKeyDefinition;

    /**
     * Constructs from a standard gateway query plan response where queryRanges
     * are in EPK hex string format.
     */
    PartitionedQueryExecutionInfo(ObjectNode content, RequestTimeline queryPlanRequestTimeline) {
        super(content);
        this.queryPlanRequestTimeline = queryPlanRequestTimeline;
        this.partitionKeyDefinition = null;
    }

    /**
     * Constructs from a thin client proxy query plan response where queryRanges
     * are in PartitionKeyInternal JSON array format. The partitionKeyDefinition is
     * retained for lazy conversion to EPK hex strings in {@link #getQueryRanges()}.
     */
    PartitionedQueryExecutionInfo(ObjectNode content, RequestTimeline queryPlanRequestTimeline, PartitionKeyDefinition partitionKeyDefinition) {
        super(content);
        this.queryPlanRequestTimeline = queryPlanRequestTimeline;
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
     * The deserialization strategy is determined by inspecting the format of the first
     * range element in the backing JSON:
     * <ul>
     *   <li>If {@code min} is a string → standard gateway format (EPK hex), deserialize directly.</li>
     *   <li>If {@code min} is an array → thin client proxy format (PartitionKeyInternal),
     *       convert to EPK hex via {@link PartitionKeyInternalHelper#convertToSortedEpkRanges}.</li>
     * </ul>
     */
    public List<Range<String>> getQueryRanges() {
        if (this.queryRanges != null) {
            return this.queryRanges;
        }

        if (this.partitionKeyDefinition != null && this.has(PartitionedQueryExecutionInfoInternal.QUERY_RANGES_PROPERTY)) {
            // Thin client path — partitionKeyDefinition is only set for thin client responses.
            // Inspect the first range to confirm it's PK-internal format before converting.
            Collection<ObjectNode> ranges = super.getCollection(
                PartitionedQueryExecutionInfoInternal.QUERY_RANGES_PROPERTY, ObjectNode.class);

            if (ranges != null && !ranges.isEmpty()) {
                ObjectNode firstRange = ranges.iterator().next();
                JsonNode minNode = firstRange.get("min");
                if (minNode != null && minNode.isArray()) {
                    this.queryRanges = PartitionKeyInternalHelper.convertToSortedEpkRanges(
                        PartitionedQueryExecutionInfoInternal.QUERY_RANGES_PROPERTY,
                        this.getPropertyBag(),
                        this.partitionKeyDefinition);
                    return this.queryRanges;
                }
            }
        }

        // Standard gateway format: min/max are EPK hex strings
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
