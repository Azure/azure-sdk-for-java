// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.implementation.query.QueryInfo;
import com.azure.cosmos.implementation.query.metrics.QueryMetricsTextWriter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * The type Feed response diagnostics.
 */
public class FeedResponseDiagnostics {

    private final static String EQUALS = "=";
    private final static String QUERY_PLAN = "QueryPlan";
    private final static String SPACE = " ";
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Logger LOGGER = LoggerFactory.getLogger(FeedResponseDiagnostics.class);
    private Map<String, QueryMetrics> queryMetricsMap;
    private QueryInfo.QueryPlanDiagnosticsContext diagnosticsContext;
    private List<ClientSideRequestStatistics> clientSideRequestStatisticsList;

    public FeedResponseDiagnostics(Map<String, QueryMetrics> queryMetricsMap) {
        this.queryMetricsMap = queryMetricsMap;
        this.clientSideRequestStatisticsList = Collections.synchronizedList(new ArrayList<>());
    }

    public Map<String, QueryMetrics> getQueryMetricsMap() {
        return queryMetricsMap;
    }

    FeedResponseDiagnostics setQueryMetricsMap(Map<String, QueryMetrics> queryMetricsMap) {
        this.queryMetricsMap = queryMetricsMap;
        return this;
    }

    /**
     * Returns the textual representation of feed response metrics
     * End users are not advised to parse return value and take dependency on parsed object.
     * Since feed response metrics contain some internal metrics, they may change across different versions.
     * @return Textual representation of feed response metrics
     */
    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        if (diagnosticsContext != null) {
            stringBuilder.append(QUERY_PLAN + SPACE + QueryMetricsTextWriter.START_TIME_HEADER)
                .append(EQUALS)
                .append(QueryMetricsTextWriter.DATE_TIME_FORMATTER.format(diagnosticsContext.getStartTimeUTC()))
                .append(System.lineSeparator());
            stringBuilder.append(QUERY_PLAN + SPACE + QueryMetricsTextWriter.END_TIME_HEADER)
                .append(EQUALS)
                .append(QueryMetricsTextWriter.DATE_TIME_FORMATTER.format(diagnosticsContext.getEndTimeUTC()))
                .append(System.lineSeparator());
            if (diagnosticsContext.getStartTimeUTC() != null && diagnosticsContext.getEndTimeUTC() != null) {
                stringBuilder.append(QUERY_PLAN + SPACE + QueryMetricsTextWriter.DURATION_HEADER)
                    .append(EQUALS)
                    .append(Duration.between(diagnosticsContext.getStartTimeUTC(),
                        diagnosticsContext.getEndTimeUTC()).toMillis()).append(System.lineSeparator());
                if (diagnosticsContext.getRequestTimeline() != null) {
                    try {
                        stringBuilder.append(QUERY_PLAN + SPACE + "RequestTimeline ")
                            .append(EQUALS)
                            .append(mapper.writeValueAsString(diagnosticsContext.getRequestTimeline()))
                            .append(System.lineSeparator())
                            .append(System.lineSeparator());
                    } catch (JsonProcessingException e) {
                        LOGGER.error("Error while parsing diagnostics ", e);
                    }
                }
            }
        }

        if (queryMetricsMap != null && !queryMetricsMap.isEmpty()) {
            queryMetricsMap.forEach((key, value) -> stringBuilder.append(key)
                .append(EQUALS)
                .append(value.toString())
                .append(System.lineSeparator()));
        }
        try {
            stringBuilder
                .append(mapper.writeValueAsString(clientSideRequestStatisticsList));
        } catch (JsonProcessingException e) {
            LOGGER.error("Error while parsing diagnostics ", e);
        }

        return stringBuilder.toString();
    }

    public void setDiagnosticsContext(QueryInfo.QueryPlanDiagnosticsContext diagnosticsContext) {
        this.diagnosticsContext = diagnosticsContext;
    }

    public QueryInfo.QueryPlanDiagnosticsContext getQueryPlanDiagnosticsContext() {
        return diagnosticsContext;
    }

    /**
     * Getter for property 'clientSideRequestStatisticsList'.
     *
     * @return Value for property 'clientSideRequestStatisticsList'.
     */
    public List<ClientSideRequestStatistics> getClientSideRequestStatisticsList() {
        return clientSideRequestStatisticsList;
    }

    public void addClientSideRequestStatistics(List<ClientSideRequestStatistics> requestStatistics) {
        clientSideRequestStatisticsList.addAll(requestStatistics);
    }

}
