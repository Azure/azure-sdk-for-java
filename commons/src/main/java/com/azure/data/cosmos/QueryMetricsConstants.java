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
package com.azure.data.cosmos;

public final class QueryMetricsConstants {
    // QueryMetrics
    public static final String RetrievedDocumentCount = "retrievedDocumentCount";
    public static final String RetrievedDocumentSize = "retrievedDocumentSize";
    public static final String OutputDocumentCount = "outputDocumentCount";
    public static final String OutputDocumentSize = "outputDocumentSize";
    public static final String IndexHitRatio = "indexUtilizationRatio";
    public static final String IndexHitDocumentCount = "indexHitDocumentCount";
    public static final String TotalQueryExecutionTimeInMs = "totalExecutionTimeInMs";

    // QueryPreparationTimes
    public static final String QueryCompileTimeInMs = "queryCompileTimeInMs";
    public static final String LogicalPlanBuildTimeInMs = "queryLogicalPlanBuildTimeInMs";
    public static final String PhysicalPlanBuildTimeInMs = "queryPhysicalPlanBuildTimeInMs";
    public static final String QueryOptimizationTimeInMs = "queryOptimizationTimeInMs";

    // QueryTimes
    public static final String IndexLookupTimeInMs = "indexLookupTimeInMs";
    public static final String DocumentLoadTimeInMs = "documentLoadTimeInMs";
    public static final String VMExecutionTimeInMs = "VMExecutionTimeInMs";
    public static final String DocumentWriteTimeInMs = "writeOutputTimeInMs";

    // RuntimeExecutionTimes
    public static final String QueryEngineTimes = "queryEngineTimes";
    public static final String SystemFunctionExecuteTimeInMs = "systemFunctionExecuteTimeInMs";
    public static final String UserDefinedFunctionExecutionTimeInMs = "userFunctionExecuteTimeInMs";

    // ClientSideMetrics
    public static final String Retries = "retries";
    public static final String RequestCharge = "requestCharge";

    // QueryMetrics Text
    public static final String ActivityIds = "Activity Ids";
    public static final String RetrievedDocumentCountText = "Retrieved Document Count";
    public static final String RetrievedDocumentSizeText = "Retrieved Document Size";
    public static final String OutputDocumentCountText = "Output Document Count";
    public static final String OutputDocumentSizeText = "Output Document Size";
    public static final String IndexUtilizationText = "Index Utilization";
    public static final String TotalQueryExecutionTimeText = "Total Query Execution Time";

    // QueryPreparationTimes Text
    public static final String QueryPreparationTimesText = "Query Preparation Times";
    public static final String QueryCompileTimeText = "Query Compilation Time";
    public static final String LogicalPlanBuildTimeText = "Logical Plan Build Time";
    public static final String PhysicalPlanBuildTimeText = "Physical Plan Build Time";
    public static final String QueryOptimizationTimeText = "Query Optimization Time";

    // QueryTimes Text
    public static final String QueryEngineTimesText = "Query Engine Times";
    public static final String IndexLookupTimeText = "Index Lookup Time";
    public static final String DocumentLoadTimeText = "Document Load Time";
    public static final String WriteOutputTimeText = "Document Write Time";

    // RuntimeExecutionTimes Text
    public static final String RuntimeExecutionTimesText = "Runtime Execution Times";
    public static final String TotalExecutionTimeText = "Query Engine Execution Time";
    public static final String SystemFunctionExecuteTimeText = "System Function Execution Time";
    public static final String UserDefinedFunctionExecutionTimeText = "User-defined Function Execution Time";

    // ClientSideQueryMetrics Text
    public static final String ClientSideQueryMetricsText = "Client Side Metrics";
    public static final String RetriesText = "Retry Count";
    public static final String RequestChargeText = "Request Charge";
    public static final String FetchExecutionRangesText = "Partition Execution Timeline";
    public static final String SchedulingMetricsText = "Scheduling Metrics";
}

