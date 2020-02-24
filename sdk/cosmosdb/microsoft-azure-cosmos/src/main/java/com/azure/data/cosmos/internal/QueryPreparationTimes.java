// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.internal;

import java.time.Duration;
import java.util.Collection;
import java.util.HashMap;

public final class QueryPreparationTimes {

    static final QueryPreparationTimes ZERO = new QueryPreparationTimes(Duration.ZERO, Duration.ZERO, Duration.ZERO, Duration.ZERO);

    private final Duration queryCompilationTime;
    private final Duration logicalPlanBuildTime;
    private final Duration physicalPlanBuildTime;
    private final Duration queryOptimizationTime;

    /**
     * @param queryCompilationTime
     * @param logicalPlanBuildTime
     * @param physicalPlanBuildTime
     * @param queryOptimizationTime
     */
    QueryPreparationTimes(Duration queryCompilationTime, Duration logicalPlanBuildTime, Duration physicalPlanBuildTime,
                          Duration queryOptimizationTime) {
        super();

        if (queryCompilationTime == null) {
            throw new NullPointerException("queryCompilationTime");
        }

        if (logicalPlanBuildTime == null) {
            throw new NullPointerException("logicalPlanBuildTime");
        }

        if (physicalPlanBuildTime == null) {
            throw new NullPointerException("physicalPlanBuildTime");
        }

        if (queryOptimizationTime == null) {
            throw new NullPointerException("queryOptimizationTime");
        }

        this.queryCompilationTime = queryCompilationTime;
        this.logicalPlanBuildTime = logicalPlanBuildTime;
        this.physicalPlanBuildTime = physicalPlanBuildTime;
        this.queryOptimizationTime = queryOptimizationTime;
    }

    /**
     * @return the queryCompilationTime
     */
    public Duration getQueryCompilationTime() {
        return queryCompilationTime;
    }

    /**
     * @return the logicalPlanBuildTime
     */
    public Duration getLogicalPlanBuildTime() {
        return logicalPlanBuildTime;
    }

    /**
     * @return the physicalPlanBuildTime
     */
    public Duration getPhysicalPlanBuildTime() {
        return physicalPlanBuildTime;
    }

    /**
     * @return the queryOptimizationTime
     */
    public Duration getQueryOptimizationTime() {
        return queryOptimizationTime;
    }

    static QueryPreparationTimes createFromCollection(
            Collection<QueryPreparationTimes> queryPreparationTimesCollection) {
        if (queryPreparationTimesCollection == null) {
            throw new NullPointerException("queryPreparationTimesCollection");
        }

        Duration queryCompilationTime = Duration.ZERO;
        Duration logicalPlanBuildTime = Duration.ZERO;
        Duration physicalPlanBuildTime = Duration.ZERO;
        Duration queryOptimizationTime = Duration.ZERO;

        for (QueryPreparationTimes queryPreparationTimes : queryPreparationTimesCollection) {
            if (queryPreparationTimes == null) {
                throw new NullPointerException("queryPreparationTimesList can not have a null element");
            }

            queryCompilationTime = queryCompilationTime.plus(queryPreparationTimes.queryCompilationTime);
            logicalPlanBuildTime = logicalPlanBuildTime.plus(queryPreparationTimes.logicalPlanBuildTime);
            physicalPlanBuildTime = physicalPlanBuildTime.plus(queryPreparationTimes.physicalPlanBuildTime);
            queryOptimizationTime = queryOptimizationTime.plus(queryPreparationTimes.queryOptimizationTime);
        }

        return new QueryPreparationTimes(
                queryCompilationTime,
                logicalPlanBuildTime,
                physicalPlanBuildTime,
                queryOptimizationTime);
    }

    static QueryPreparationTimes createFromDelimitedString(String delimitedString) {
        HashMap<String, Double> metrics = QueryMetricsUtils.parseDelimitedString(delimitedString);

        return new QueryPreparationTimes(
                QueryMetricsUtils.durationFromMetrics(metrics, QueryMetricsConstants.QueryCompileTimeInMs),
                QueryMetricsUtils.durationFromMetrics(metrics, QueryMetricsConstants.LogicalPlanBuildTimeInMs),
                QueryMetricsUtils.durationFromMetrics(metrics, QueryMetricsConstants.PhysicalPlanBuildTimeInMs),
                QueryMetricsUtils.durationFromMetrics(metrics, QueryMetricsConstants.QueryOptimizationTimeInMs));
    }

    String toDelimitedString() {
        String formatString = "%s=%.2f;%s=%.2f;%s=%.2f;%s=%.2f";
        return String.format(
                formatString,
                QueryMetricsConstants.QueryCompileTimeInMs,
                this.queryCompilationTime.toMillis(),
                QueryMetricsConstants.LogicalPlanBuildTimeInMs,
                this.logicalPlanBuildTime.toMillis(),
                QueryMetricsConstants.PhysicalPlanBuildTimeInMs,
                this.physicalPlanBuildTime.toMillis(),
                QueryMetricsConstants.QueryOptimizationTimeInMs,
                this.queryOptimizationTime.toMillis());
    }

    String toTextString(int indentLevel) {
        if (indentLevel == Integer.MAX_VALUE) {
            throw new NumberFormatException("indentLevel input must be less than Integer.MaxValue");
        }

        StringBuilder stringBuilder = new StringBuilder();

        QueryMetricsUtils.appendHeaderToStringBuilder(stringBuilder, QueryMetricsConstants.QueryPreparationTimesText,
                indentLevel);

        QueryMetricsUtils.appendNanosecondsToStringBuilder(stringBuilder, QueryMetricsConstants.QueryCompileTimeText
                , this.queryCompilationTime.toNanos(), indentLevel + 1);

        QueryMetricsUtils.appendNanosecondsToStringBuilder(stringBuilder,
                QueryMetricsConstants.LogicalPlanBuildTimeText, this.logicalPlanBuildTime.toNanos(),
                indentLevel + 1);

        QueryMetricsUtils.appendNanosecondsToStringBuilder(stringBuilder,
                QueryMetricsConstants.PhysicalPlanBuildTimeText, this.physicalPlanBuildTime.toNanos(),
                indentLevel + 1);

        QueryMetricsUtils.appendNanosecondsToStringBuilder(stringBuilder,
                QueryMetricsConstants.QueryOptimizationTimeText, this.queryOptimizationTime.toNanos(),
                indentLevel + 1);
        return stringBuilder.toString();
    }
}

