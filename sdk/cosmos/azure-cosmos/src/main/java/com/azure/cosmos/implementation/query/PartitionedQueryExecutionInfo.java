// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.query;

import com.azure.cosmos.implementation.RequestTimeline;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.query.hybridsearch.HybridSearchQueryInfo;
import com.azure.cosmos.implementation.routing.PartitionKeyInternal;
import com.azure.cosmos.implementation.routing.PartitionKeyInternalHelper;
import com.azure.cosmos.implementation.routing.Range;
import com.azure.cosmos.implementation.JsonSerializable;
import com.azure.cosmos.implementation.Constants;
import com.azure.cosmos.models.PartitionKeyDefinition;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
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
    private final boolean useThinClientMode;
    private final PartitionKeyDefinition partitionKeyDefinition;

    PartitionedQueryExecutionInfo(QueryInfo queryInfo, List<Range<String>> queryRanges) {
        this.queryInfo = queryInfo;
        this.queryRanges = queryRanges;
        this.useThinClientMode = false;
        this.partitionKeyDefinition = null;

        this.set(
            PartitionedQueryExecutionInfoInternal.PARTITIONED_QUERY_EXECUTION_INFO_VERSION_PROPERTY,
            Constants.PartitionedQueryExecutionInfo.VERSION_1
        );
    }

    public PartitionedQueryExecutionInfo(ObjectNode content, RequestTimeline queryPlanRequestTimeline) {
        super(content);
        this.queryPlanRequestTimeline = queryPlanRequestTimeline;
        this.useThinClientMode = false;
        this.partitionKeyDefinition = null;
    }

    public PartitionedQueryExecutionInfo(ObjectNode content, RequestTimeline queryPlanRequestTimeline, boolean useThinClientMode, PartitionKeyDefinition partitionKeyDefinition) {
        super(content);
        this.queryPlanRequestTimeline = queryPlanRequestTimeline;
        this.useThinClientMode = useThinClientMode;
        this.partitionKeyDefinition = partitionKeyDefinition;
    }

    public PartitionedQueryExecutionInfo(String jsonString) {
        super(jsonString);
        this.useThinClientMode = false;
        this.partitionKeyDefinition = null;
    }

    public int getVersion() {
        return super.getInt(PartitionedQueryExecutionInfoInternal.PARTITIONED_QUERY_EXECUTION_INFO_VERSION_PROPERTY);
    }

    public QueryInfo getQueryInfo() {
        return this.queryInfo != null ? this.queryInfo
                : (this.queryInfo = super.getObject(
                        PartitionedQueryExecutionInfoInternal.QUERY_INFO_PROPERTY, QueryInfo.class));
    }

    public List<Range<String>> getQueryRanges() {
        if (this.queryRanges != null) {
            return this.queryRanges;
        }

        if (this.useThinClientMode) {
            // In thin client mode, the proxy returns queryRanges in PartitionKeyInternal array format
            // (e.g., {"min": [[""]], "max": [["Infinity"]]}) which needs to be converted to EPK hex strings.
            // We need to manually parse this since the generic Range<T> deserialization doesn't handle
            // PartitionKeyInternal properly (it keeps the raw ArrayNode).
            this.queryRanges = parseQueryRangesForThinClient();
        } else {
            // In non-thin client mode, the Gateway returns queryRanges directly as EPK hex strings
            this.queryRanges = super.getList(
                PartitionedQueryExecutionInfoInternal.QUERY_RANGES_PROPERTY, QUERY_RANGES_CLASS);
        }

        return this.queryRanges;
    }

    /**
     * Parses the queryRanges JSON array for thin client mode.
     * The thin client proxy returns ranges in the format:
     * [{"min": [[""]], "max": [["Infinity"]], "isMinInclusive": true, "isMaxInclusive": false}]
     * where min/max are PartitionKeyInternal JSON representations that need to be converted to EPK strings.
     *
     * @return List of ranges with EPK hex string min/max values
     */
    private List<Range<String>> parseQueryRangesForThinClient() {
        ObjectNode propertyBag = this.getPropertyBag();
        JsonNode queryRangesNode = propertyBag.get(PartitionedQueryExecutionInfoInternal.QUERY_RANGES_PROPERTY);

        if (queryRangesNode == null || !queryRangesNode.isArray()) {
            return null;
        }

        ArrayNode rangesArray = (ArrayNode) queryRangesNode;
        List<Range<String>> epkRanges = new ArrayList<>(rangesArray.size());

        for (JsonNode rangeNode : rangesArray) {
            if (!rangeNode.isObject()) {
                continue;
            }

            ObjectNode rangeObject = (ObjectNode) rangeNode;

            // Parse min and max as PartitionKeyInternal
            JsonNode minNode = rangeObject.get("min");
            JsonNode maxNode = rangeObject.get("max");

            PartitionKeyInternal minPk = parsePartitionKeyInternal(minNode);
            PartitionKeyInternal maxPk = parsePartitionKeyInternal(maxNode);

            // Convert to EPK strings
            String minEpk = PartitionKeyInternalHelper.getEffectivePartitionKeyString(minPk, this.partitionKeyDefinition);
            String maxEpk = PartitionKeyInternalHelper.getEffectivePartitionKeyString(maxPk, this.partitionKeyDefinition);

            // Parse isMinInclusive and isMaxInclusive (defaults: min=true, max=false)
            boolean isMinInclusive = !rangeObject.has("isMinInclusive") || rangeObject.get("isMinInclusive").asBoolean(true);
            boolean isMaxInclusive = rangeObject.has("isMaxInclusive") && rangeObject.get("isMaxInclusive").asBoolean(false);

            epkRanges.add(new Range<>(minEpk, maxEpk, isMinInclusive, isMaxInclusive));
        }

        return epkRanges;
    }

    /**
     * Parses a JSON node representing a PartitionKeyInternal.
     * Handles formats like [[""]] (empty), [["Infinity"]] (infinity), or actual partition key values.
     *
     * @param node The JSON node to parse
     * @return The parsed PartitionKeyInternal
     */
    private PartitionKeyInternal parsePartitionKeyInternal(JsonNode node) {
        if (node == null || node.isNull()) {
            return PartitionKeyInternal.EmptyPartitionKey;
        }

        try {
            // Use Jackson to deserialize using PartitionKeyInternal's custom deserializer
            return Utils.getSimpleObjectMapper().treeToValue(node, PartitionKeyInternal.class);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse PartitionKeyInternal from JSON: " + node, e);
        }
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
