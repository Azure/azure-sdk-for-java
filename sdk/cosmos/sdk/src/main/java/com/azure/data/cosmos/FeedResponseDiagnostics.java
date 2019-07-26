// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos;

import com.azure.data.cosmos.internal.QueryMetrics;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

public class FeedResponseDiagnostics {

    private Map<String, QueryMetrics> queryMetricsMap;

    FeedResponseDiagnostics(Map<String, QueryMetrics> queryMetricsMap) {
        this.queryMetricsMap = queryMetricsMap;
    }

    Map<String, QueryMetrics> queryMetricsMap() {
        return queryMetricsMap;
    }

    FeedResponseDiagnostics queryMetricsMap(Map<String, QueryMetrics> queryMetricsMap) {
        this.queryMetricsMap = queryMetricsMap;
        return this;
    }

    /**
     * Returns the textual representation of feed response metrics
     * @return Textual representation of feed response metrics
     */
    @Override
    public String toString() {
        if (queryMetricsMap == null || queryMetricsMap.isEmpty()) {
            return StringUtils.EMPTY;
        }
        StringBuilder stringBuilder = new StringBuilder();
        queryMetricsMap.forEach((key, value) -> stringBuilder.append(key).append("=").append(value.toString()).append("\n"));
        return stringBuilder.toString();
    }
}
