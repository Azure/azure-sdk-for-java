// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.implementation.query.QueryInfo;
import com.azure.cosmos.implementation.query.metrics.QueryMetricsTextWriter;

import java.time.Duration;
import java.util.Map;

/**
 * The type Feed response diagnostics.
 */
public final class FeedResponseDiagnostics {

    private final static String EQUALS = "=";
    private final static String QUERY_PLAN = "QueryPlan";
    private final static String SPACE = " ";
    private Map<String, QueryMetrics> queryMetricsMap;
    private QueryInfo.QueryPlanDiagnosticsContext diagnosticsContext;

    public FeedResponseDiagnostics(Map<String, QueryMetrics> queryMetricsMap) {
        this.queryMetricsMap = queryMetricsMap;
    }

    Map<String, QueryMetrics> getQueryMetricsMap() {
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
            }
        }

        if (queryMetricsMap != null && !queryMetricsMap.isEmpty()) {
            queryMetricsMap.forEach((key, value) -> stringBuilder.append(key)
                .append(EQUALS)
                .append(value.toString())
                .append(System.lineSeparator()));
        }
        return stringBuilder.toString();
    }

    public void setDiagnosticsContext(QueryInfo.QueryPlanDiagnosticsContext diagnosticsContext) {
        this.diagnosticsContext = diagnosticsContext;
    }
}
