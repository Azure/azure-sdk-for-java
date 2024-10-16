// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.query;

import com.azure.cosmos.implementation.Constants;
import com.azure.cosmos.implementation.JsonSerializable;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Hybrid Search Query Info {@link QueryInfo}
 */
public class HybridSearchQueryInfo {
    @JsonProperty(Constants.Properties.GLOBAL_STATISTICS_QUERY_INFO)
    private QueryInfo globalStatisticsQueryInfo;
    @JsonProperty(Constants.Properties.COMPONENT_QUERY_INFOS)
    private List<QueryInfo> componentQueryInfoList;
    @JsonProperty(Constants.Properties.PROJECTION_QUERY_INFO)
    private QueryInfo projectionQueryInfo;
    @JsonProperty(Constants.Properties.SKIP)
    private int skip;
    @JsonProperty(Constants.Properties.TAKE)
    private int take;
    private JsonSerializable jsonSerializable;

    /**
     * Constructor
     */
    public HybridSearchQueryInfo() {
        this.jsonSerializable = new JsonSerializable();
    }

    /**
     * Gets the globalStatisticsQueryInfo for hybrid search
     *
     * @return globalStatisticsQueryInfo
     */
    public QueryInfo getGlobalStatisticsQueryInfo() {
        return globalStatisticsQueryInfo;
    }

    /**
     * Sets the globalStatisticsQueryInfo for hybrid search
     *
     * @param globalStatisticsQueryInfo the globalStatisticsQueryInfo for hybrid search
     * @return HybridSearchQueryInfo
     */
    public HybridSearchQueryInfo setGlobalStatisticsQueryInfo(QueryInfo globalStatisticsQueryInfo) {
        if (globalStatisticsQueryInfo == null) {
            throw new NullPointerException("globalStatisticsQueryInfo cannot be null");
        }
        this.globalStatisticsQueryInfo = globalStatisticsQueryInfo;
        return this;
    }

    /**
     * Gets the list of componentQueryInfo for hybrid search
     *
     * @return componentQueryInfoList
     */
    public List<QueryInfo> getComponentQueryInfoList() {

        return componentQueryInfoList;
    }

    /**
     * Sets the list of componentQueryInfo for hybrid search
     *
     * @param componentQueryInfoList the list of componentQueryInfo for hybrid search
     * @return HybridSearchQueryInfo
     */
    public HybridSearchQueryInfo setComponentQueryInfoList(List<QueryInfo> componentQueryInfoList) {
        if (componentQueryInfoList.isEmpty()) {
            throw new NullPointerException("componentQueryInfoList cannot be empty");
        }
        this.componentQueryInfoList = componentQueryInfoList;
        return this;
    }

    /**
     * Gets the projectionQueryInfo for hybrid search
     *
     * @return projectionQueryInfo
     */
    public QueryInfo getProjectionQueryInfo() {
        return projectionQueryInfo;
    }

    /**
     * Sets the projectionQueryInfo for hybrid search
     *
     * @param projectionQueryInfo the projectionQueryInfo for hybrid search
     * @return HybridSearchQueryInfo
     */
    public HybridSearchQueryInfo setProjectionQueryInfo(QueryInfo projectionQueryInfo) {
        if (projectionQueryInfo == null) {
            throw new NullPointerException("projectionQueryInfo cannot be null");
        }
        this.projectionQueryInfo = projectionQueryInfo;
        return this;
    }

    /**
     * Gets the number of documents to skip for hybrid search
     *
     * @return skip
     */
    public int getSkip() {
        return skip;
    }

    /**
     * Sets the number of documents to skip for hybrid search
     *
     * @param skip the number of documents to skip for hybrid search
     * @return HybridSearchQueryInfo
     */
    public HybridSearchQueryInfo setSkip(int skip) {
        this.skip = skip;
        return this;
    }

    /**
     * Gets the number of documents to take for hybrid search
     *
     * @return take
     */
    public int getTake() {
        return take;
    }

    /**
     * Sets the number of documents to take for hybrid search
     *
     * @param take the number of documents to take for hybrid search
     * @return HybridSearchQueryInfo
     */
    public HybridSearchQueryInfo setTake(int take) {
        this.take = take;
        return this;
    }
}
