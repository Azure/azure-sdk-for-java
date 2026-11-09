// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.implementation.query.QueryInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The type Feed response diagnostics.
 */
public class FeedResponseDiagnostics {
    private static final Logger LOGGER = LoggerFactory.getLogger(FeedResponseDiagnostics.class);
    private Map<String, QueryMetrics> queryMetricsMap;
    private QueryInfo.QueryPlanDiagnosticsContext diagnosticsContext;
    private final Collection<ClientSideRequestStatistics> clientSideRequestStatistics;

    public FeedResponseDiagnostics(Map<String, QueryMetrics> queryMetricsMap, Collection<ClientSideRequestStatistics> clientSideRequestStatistics) {
        this.queryMetricsMap = queryMetricsMap;
        this.clientSideRequestStatistics = new DistinctClientSideRequestStatisticsCollection();
        if (clientSideRequestStatistics != null) {
            this.clientSideRequestStatistics.addAll(clientSideRequestStatistics);
        }
    }

    public FeedResponseDiagnostics(FeedResponseDiagnostics toBeCloned) {
        if (toBeCloned.queryMetricsMap != null) {
            this.queryMetricsMap = new ConcurrentHashMap<>(toBeCloned.queryMetricsMap);
        }

        this.clientSideRequestStatistics = new DistinctClientSideRequestStatisticsCollection();
        this.clientSideRequestStatistics.addAll(toBeCloned.clientSideRequestStatistics);

        if (diagnosticsContext != null) {
            this.diagnosticsContext = new QueryInfo.QueryPlanDiagnosticsContext(
                toBeCloned.diagnosticsContext.getStartTimeUTC(),
                toBeCloned.diagnosticsContext.getEndTimeUTC(),
                toBeCloned.diagnosticsContext.getRequestTimeline()
            );
        }
    }

    public Map<String, QueryMetrics> getQueryMetricsMap() {
        return queryMetricsMap;
    }

    /**
     * Returns the textual representation of feed response metrics
     * End users are not advised to parse return value and take dependency on parsed object.
     * Since feed response metrics contain some internal metrics, they may change across different versions.
     * @return Textual representation of feed response metrics
     */
    @Override
    public String toString() {
        try {
            return Utils.getDurationEnabledObjectMapper().writeValueAsString(this);
        } catch (final JsonProcessingException error) {
            LOGGER.debug("could not convert {} value to JSON due to:", this.getClass(), error);
            try {
                return String.format("{\"error\":%s}", Utils.getDurationEnabledObjectMapper().writeValueAsString(error.toString()));
            } catch (final JsonProcessingException exception) {
                return "null";
            }
        }
    }

    public void setDiagnosticsContext(QueryInfo.QueryPlanDiagnosticsContext diagnosticsContext) {
        this.diagnosticsContext = diagnosticsContext;
    }

    public QueryInfo.QueryPlanDiagnosticsContext getQueryPlanDiagnosticsContext() {
        return diagnosticsContext;
    }

    /**
     * Getter for property 'clientSideRequestStatistics'.
     *
     * @return Value for property 'clientSideRequestStatistics'.
     */
    public Collection<ClientSideRequestStatistics> getClientSideRequestStatistics() {
        return clientSideRequestStatistics;
    }

    public void addClientSideRequestStatistics(Collection<ClientSideRequestStatistics> requestStatistics) {
        clientSideRequestStatistics.addAll(requestStatistics);
    }

    public String getUserAgent() {
        if (this.clientSideRequestStatistics != null && !this.clientSideRequestStatistics.isEmpty()) {
            return this.clientSideRequestStatistics.stream().findFirst().get().getUserAgent();
        }

        // return default one
        return Utils.getUserAgent();
    }

    public FeedResponseDiagnostics setSamplingRateSnapshot(double samplingRateSnapshot) {
        for (ClientSideRequestStatistics c: this.clientSideRequestStatistics) {
            c.setSamplingRateSnapshot(samplingRateSnapshot);
        }

        return this;
    }
}
