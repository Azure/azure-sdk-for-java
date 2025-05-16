// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.query.hybridsearch;

import com.azure.cosmos.implementation.Constants;
import com.azure.cosmos.implementation.JsonSerializable;
import com.azure.cosmos.implementation.query.QueryInfo;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.List;

/**
 * Hybrid Search Query Info {@link HybridSearchQueryInfo}
 */
public class HybridSearchQueryInfo extends JsonSerializable {
    @JsonProperty(Constants.Properties.GLOBAL_STATISTICS_QUERY)
    private String globalStatisticsQuery;
    @JsonProperty(Constants.Properties.COMPONENT_QUERY_INFOS)
    private List<QueryInfo> componentQueryInfoList;
    @JsonProperty(Constants.Properties.COMPONENT_WEIGHTS)
    private List<Double> componentWeights;
    @JsonProperty(Constants.Properties.PROJECTION_QUERY_INFO)
    private QueryInfo projectionQueryInfo;
    @JsonProperty(Constants.Properties.SKIP)
    private Integer skip;
    @JsonProperty(Constants.Properties.TAKE)
    private Integer take;
    @JsonProperty(Constants.Properties.REQUIRES_GLOBAL_STATISTICS)
    private Boolean requiresGlobalStatistics;

    /**
     * Constructor
     */
    public HybridSearchQueryInfo() {
    }

    public HybridSearchQueryInfo(ObjectNode objectNode) {
        super(objectNode);
    }

    public HybridSearchQueryInfo(String jsonString) {
        super(jsonString);
    }

    /**
     * Gets the globalStatisticsQueryInfo for hybrid search
     *
     * @return globalStatisticsQueryInfo
     */
    public String getGlobalStatisticsQuery() {
        return globalStatisticsQuery != null ? this.globalStatisticsQuery : (this.globalStatisticsQuery = super.getString(Constants.Properties.GLOBAL_STATISTICS_QUERY));
    }

    /**
     * Gets the list of componentQueryInfo for hybrid search
     *
     * @return componentQueryInfoList
     */
    public List<QueryInfo> getComponentQueryInfoList() {

        return componentQueryInfoList != null ? this.componentQueryInfoList : (this.componentQueryInfoList = super.getList(Constants.Properties.COMPONENT_QUERY_INFOS, QueryInfo.class));
    }

    /**
     * Gets the list for componentWeights for hybrid search
     *
     * @return componentWeights
     */
    public List<Double> getComponentWeights() {
        return componentWeights != null ? this.componentWeights : (this.componentWeights = super.getList(Constants.Properties.COMPONENT_WEIGHTS, Double.class));
    }

    /**
     * Gets the projectionQueryInfo for hybrid search
     *
     * @return projectionQueryInfo
     */
    public QueryInfo getProjectionQueryInfo() {

        return projectionQueryInfo != null ? this.projectionQueryInfo : (this.projectionQueryInfo = super.getObject(Constants.Properties.PROJECTION_QUERY_INFO, QueryInfo.class));
    }

    public boolean hasSkip() {
        return this.getSkip() != null;
    }

    public boolean hasTake() {
        return this.getTake() != null;
    }

    /**
     * Gets the number of documents to skip for hybrid search
     *
     * @return skip
     */
    public Integer getSkip() {
        return this.skip != null ? this.skip : (this.skip = super.getInt(Constants.Properties.SKIP));
    }

    /**
     * Gets the number of documents to take for hybrid search
     *
     * @return take
     */
    public Integer getTake() {
        return this.take != null ? this.take : (this.take = super.getInt(Constants.Properties.TAKE));
    }

    public Boolean getRequiresGlobalStatistics() {
        this.requiresGlobalStatistics = Boolean.TRUE.equals(super.getBoolean(Constants.Properties.REQUIRES_GLOBAL_STATISTICS));
        return this.requiresGlobalStatistics;
    }
}
