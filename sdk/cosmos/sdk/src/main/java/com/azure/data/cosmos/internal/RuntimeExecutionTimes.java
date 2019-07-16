/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.azure.data.cosmos.internal;

import java.time.Duration;
import java.util.Collection;
import java.util.HashMap;

/**
 * Query runtime execution times in the Azure Cosmos DB service.
 */
public final class RuntimeExecutionTimes {

    static final RuntimeExecutionTimes ZERO = new RuntimeExecutionTimes(Duration.ZERO, Duration.ZERO, Duration.ZERO);

    private final Duration queryEngineExecutionTime;
    private final Duration systemFunctionExecutionTime;
    private final Duration userDefinedFunctionExecutionTime;

    /**
     * @param queryEngineExecutionTime
     * @param systemFunctionExecutionTime
     * @param userDefinedFunctionExecutionTime
     */
    RuntimeExecutionTimes(Duration queryEngineExecutionTime, Duration systemFunctionExecutionTime,
                          Duration userDefinedFunctionExecutionTime) {
        super();

        if (queryEngineExecutionTime == null) {
            throw new NullPointerException("queryEngineExecutionTime");
        }

        if (systemFunctionExecutionTime == null) {
            throw new NullPointerException("systemFunctionExecutionTime");
        }

        if (userDefinedFunctionExecutionTime == null) {
            throw new NullPointerException("userDefinedFunctionExecutionTime");
        }

        this.queryEngineExecutionTime = queryEngineExecutionTime;
        this.systemFunctionExecutionTime = systemFunctionExecutionTime;
        this.userDefinedFunctionExecutionTime = userDefinedFunctionExecutionTime;
    }

    /**
     * @return the queryEngineExecutionTime
     */
    public Duration getQueryEngineExecutionTime() {
        return queryEngineExecutionTime;
    }

    /**
     * @return the systemFunctionExecutionTime
     */
    public Duration getSystemFunctionExecutionTime() {
        return systemFunctionExecutionTime;
    }

    /**
     * @return the userDefinedFunctionExecutionTime
     */
    public Duration getUserDefinedFunctionExecutionTime() {
        return userDefinedFunctionExecutionTime;
    }

    static RuntimeExecutionTimes createFromCollection(
            Collection<RuntimeExecutionTimes> runtimeExecutionTimesCollection) {
        if (runtimeExecutionTimesCollection == null) {
            throw new NullPointerException("runtimeExecutionTimesCollection");
        }

        Duration queryEngineExecutionTime = Duration.ZERO;
        Duration systemFunctionExecutionTime = Duration.ZERO;
        Duration userDefinedFunctionExecutionTime = Duration.ZERO;

        for (RuntimeExecutionTimes runtimeExecutionTime : runtimeExecutionTimesCollection) {
            queryEngineExecutionTime = queryEngineExecutionTime.plus(runtimeExecutionTime.queryEngineExecutionTime);
            systemFunctionExecutionTime = systemFunctionExecutionTime.plus(runtimeExecutionTime.systemFunctionExecutionTime);
            userDefinedFunctionExecutionTime = userDefinedFunctionExecutionTime.plus(runtimeExecutionTime.userDefinedFunctionExecutionTime);
        }

        return new RuntimeExecutionTimes(
                queryEngineExecutionTime,
                systemFunctionExecutionTime,
                userDefinedFunctionExecutionTime);
    }

    static RuntimeExecutionTimes createFromDelimitedString(String delimitedString) {
        HashMap<String, Double> metrics = QueryMetricsUtils.parseDelimitedString(delimitedString);

        Duration vmExecutionTime = QueryMetricsUtils.durationFromMetrics(metrics, QueryMetricsConstants.VMExecutionTimeInMs);
        Duration indexLookupTime = QueryMetricsUtils.durationFromMetrics(metrics, QueryMetricsConstants.IndexLookupTimeInMs);
        Duration documentLoadTime = QueryMetricsUtils.durationFromMetrics(metrics, QueryMetricsConstants.DocumentLoadTimeInMs);
        Duration documentWriteTime = QueryMetricsUtils.durationFromMetrics(metrics, QueryMetricsConstants.DocumentWriteTimeInMs);

        return new RuntimeExecutionTimes(
                vmExecutionTime.minus(indexLookupTime).minus(documentLoadTime).minus(documentWriteTime),
                QueryMetricsUtils.durationFromMetrics(metrics, QueryMetricsConstants.SystemFunctionExecuteTimeInMs),
                QueryMetricsUtils.durationFromMetrics(metrics, QueryMetricsConstants.UserDefinedFunctionExecutionTimeInMs));
    }

    String toDelimitedString() {
        String formatString = "%s=%2f;%s=%2f";

        // queryEngineExecutionTime is not emitted, since it is calculated as
        // vmExecutionTime - indexLookupTime - documentLoadTime - documentWriteTime
        return String.format(
                formatString,
                QueryMetricsConstants.SystemFunctionExecuteTimeInMs,
                this.systemFunctionExecutionTime.toMillis(),
                QueryMetricsConstants.UserDefinedFunctionExecutionTimeInMs,
                this.userDefinedFunctionExecutionTime.toMillis());
    }

    String toTextString(int indentLevel) {
        if (indentLevel == Integer.MAX_VALUE) {
            throw new NumberFormatException("indentLevel input must be less than Int32.MaxValue");
        }
        StringBuilder stringBuilder = new StringBuilder();

        QueryMetricsUtils.appendHeaderToStringBuilder(stringBuilder, QueryMetricsConstants.RuntimeExecutionTimesText,
                indentLevel);

        QueryMetricsUtils.appendNanosecondsToStringBuilder(stringBuilder,
                QueryMetricsConstants.TotalExecutionTimeText, this.queryEngineExecutionTime.toNanos(),
                indentLevel + 1);

        QueryMetricsUtils.appendNanosecondsToStringBuilder(stringBuilder,
                QueryMetricsConstants.SystemFunctionExecuteTimeText,
                this.systemFunctionExecutionTime.toNanos(), indentLevel + 1);

        QueryMetricsUtils.appendNanosecondsToStringBuilder(stringBuilder,
                QueryMetricsConstants.UserDefinedFunctionExecutionTimeText,
                this.userDefinedFunctionExecutionTime.toNanos(), indentLevel + 1);

        return stringBuilder.toString();
    }
}