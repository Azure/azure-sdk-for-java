// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal.query.metrics;

import com.azure.data.cosmos.BridgeInternal;
import com.azure.data.cosmos.internal.QueryMetrics;
import com.azure.data.cosmos.internal.QueryPreparationTimes;
import com.azure.data.cosmos.internal.RuntimeExecutionTimes;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

abstract class QueryMetricsWriter {

    public void writeQueryMetrics(QueryMetrics queryMetrics) {
        this.writeBeforeQueryMetrics();

        // Top Level Properties
        this.writeRetrievedDocumentCount(queryMetrics.getRetrievedDocumentCount());
        this.writeRetrievedDocumentSize(queryMetrics.getRetrievedDocumentSize());
        this.writeOutputDocumentCount(queryMetrics.getOutputDocumentCount());
        this.writeOutputDocumentSize(queryMetrics.getOutputDocumentSize());
        this.writeIndexHitRatio(queryMetrics.getIndexHitRatio());
        this.writeTotalQueryExecutionTime(queryMetrics.getTotalQueryExecutionTime());

        // QueryPreparationTimes
        this.writeQueryPreparationTimes(queryMetrics.getQueryPreparationTimes());

        this.writeIndexLookupTime(queryMetrics.getIndexLookupTime());
        this.writeDocumentLoadTime(queryMetrics.getDocumentLoadTime());
        this.writeVMExecutionTime(queryMetrics.getVMExecutionTime());

        // RuntimesExecutionTimes
        this.writeRuntimesExecutionTimes(queryMetrics.getRuntimeExecutionTimes());

        this.writeDocumentWriteTime(queryMetrics.getDocumentWriteTime());

        // ClientSideMetrics
        this.writeClientSideMetrics(BridgeInternal.getClientSideMetrics(queryMetrics));

        this.writeAfterQueryMetrics();
    }

    protected abstract void writeBeforeQueryMetrics();

    protected abstract void writeRetrievedDocumentCount(long retrievedDocumentCount);

    protected abstract void writeRetrievedDocumentSize(long retrievedDocumentSize);

    protected abstract void writeOutputDocumentCount(long outputDocumentCount);

    protected abstract void writeOutputDocumentSize(long outputDocumentSize);

    protected abstract void writeIndexHitRatio(double indexHitRatio);

    protected abstract void writeTotalQueryExecutionTime(Duration totalQueryExecutionTime);

    //QueryPreparationTimes
    private void writeQueryPreparationTimes(QueryPreparationTimes queryPreparationTimes) {
        this.writeBeforeQueryPreparationTimes();

        this.writeQueryCompilationTime(queryPreparationTimes.getQueryCompilationTime());
        this.writeLogicalPlanBuildTime(queryPreparationTimes.getLogicalPlanBuildTime());
        this.writePhysicalPlanBuildTime(queryPreparationTimes.getPhysicalPlanBuildTime());
        this.writeQueryOptimizationTime(queryPreparationTimes.getQueryOptimizationTime());

        this.writeAfterQueryPreparationTimes();
    }

    protected abstract void writeBeforeQueryPreparationTimes();

    protected abstract void writeQueryCompilationTime(Duration queryCompilationTime);

    protected abstract void writeLogicalPlanBuildTime(Duration logicalPlanBuildTime);

    protected abstract void writePhysicalPlanBuildTime(Duration physicalPlanBuildTime);

    protected abstract void writeQueryOptimizationTime(Duration queryOptimizationTime);

    protected abstract void writeAfterQueryPreparationTimes();

    protected abstract void writeIndexLookupTime(Duration indexLookupTime);

    protected abstract void writeDocumentLoadTime(Duration documentLoadTime);

    protected abstract void writeVMExecutionTime(Duration vMExecutionTime);

    // RuntimeExecutionTimes
    private void writeRuntimesExecutionTimes(RuntimeExecutionTimes runtimeExecutionTimes) {
        this.writeBeforeRuntimeExecutionTimes();

        this.writeQueryEngineExecutionTime(runtimeExecutionTimes.getQueryEngineExecutionTime());
        this.writeSystemFunctionExecutionTime(runtimeExecutionTimes.getSystemFunctionExecutionTime());
        this.writeUserDefinedFunctionExecutionTime(runtimeExecutionTimes.getUserDefinedFunctionExecutionTime());

        this.writeAfterRuntimeExecutionTimes();
    }


    protected abstract void writeBeforeRuntimeExecutionTimes();

    protected abstract void writeQueryEngineExecutionTime(Duration queryEngineExecutionTime);

    protected abstract void writeSystemFunctionExecutionTime(Duration systemFunctionExecutionTime);

    protected abstract void writeUserDefinedFunctionExecutionTime(Duration userDefinedFunctionExecutionTime);

    protected abstract void writeAfterRuntimeExecutionTimes();

    protected abstract void writeDocumentWriteTime(Duration documentWriteTime);

    // ClientSideMetrics
    private void writeClientSideMetrics(ClientSideMetrics clientSideMetrics) {
        this.writeBeforeClientSideMetrics();

        this.writeRetries(clientSideMetrics.getRetries());
        this.writeRequestCharge(clientSideMetrics.getRequestCharge());
        this.writePartitionExecutionTimeline(clientSideMetrics);
        this.writeSchedulingMetrics(clientSideMetrics);

        this.writeAfterClientSideMetrics();
    }

    protected abstract void writeBeforeClientSideMetrics();

    protected abstract void writeRetries(long retries);

    protected abstract void writeRequestCharge(double requestCharge);

    private void writePartitionExecutionTimeline(ClientSideMetrics clientSideMetrics) {
        this.writeBeforePartitionExecutionTimeline();
        List<FetchExecutionRange> fetchExecutionRanges = clientSideMetrics.getFetchExecutionRanges();
        fetchExecutionRanges.sort((f1, f2) -> f2.getStartTime().compareTo(f1.getStartTime()));
        for (FetchExecutionRange fetchExecutionRange : clientSideMetrics.getFetchExecutionRanges()) {
            this.writeFetchExecutionRange(fetchExecutionRange);
        }
        this.writeAfterPartitionExecutionTimeline();
    }

    protected abstract void writeBeforePartitionExecutionTimeline();

    private void writeFetchExecutionRange(FetchExecutionRange fetchExecutionRange) {
        this.writeBeforeFetchExecutionRange();

        this.writeFetchPartitionKeyRangeId(fetchExecutionRange.getPartitionId());
        this.writeActivityId(fetchExecutionRange.getActivityId());
        this.writeStartTime(fetchExecutionRange.getStartTime());
        this.writeEndTime(fetchExecutionRange.getEndTime());
        this.writeFetchDocumentCount(fetchExecutionRange.getNumberOfDocuments());
        this.writeFetchRetryCount(fetchExecutionRange.getRetryCount());

        this.writeAfterFetchExecutionRange();
    }

    protected abstract void writeBeforeFetchExecutionRange();

    protected abstract void writeFetchPartitionKeyRangeId(String partitionId);

    protected abstract void writeActivityId(String activityId);

    protected abstract void writeStartTime(Instant startTime);

    protected abstract void writeEndTime(Instant endTime);

    protected abstract void writeFetchDocumentCount(long numberOfDocuments);

    protected abstract void writeFetchRetryCount(long retryCount);

    protected abstract void writeAfterFetchExecutionRange();

    protected abstract void writeAfterPartitionExecutionTimeline();

    private void writeSchedulingMetrics(ClientSideMetrics clientSideMetrics) {
        this.writeBeforeSchedulingMetrics();
        List<ImmutablePair<String, SchedulingTimeSpan>> partitionSchedulingTimeSpans = clientSideMetrics.getPartitionSchedulingTimeSpans();
        partitionSchedulingTimeSpans.sort((o1, o2) -> (int) (o2.right.getResponseTime() - o1.right.getResponseTime()));
        for (ImmutablePair<String, SchedulingTimeSpan> partitionSchedulingDuration :
                partitionSchedulingTimeSpans) {
            String partitionId = partitionSchedulingDuration.getLeft();
            SchedulingTimeSpan schedulingDuration = partitionSchedulingDuration.getRight();

            this.writePartitionSchedulingDuration(partitionId, schedulingDuration);
        }

        this.writeAfterSchedulingMetrics();
    }

    protected abstract void writeBeforeSchedulingMetrics();

    private void writePartitionSchedulingDuration(String partitionId, SchedulingTimeSpan schedulingDuration) {
        this.writeBeforePartitionSchedulingDuration();

        this.writePartitionSchedulingDurationId(partitionId);
        this.writeResponseTime(schedulingDuration.getResponseTime());
        this.writeRunTime(schedulingDuration.getRunTime());
        this.writeWaitTime(schedulingDuration.getWaitTime());
        this.writeTurnaroundTime(schedulingDuration.getTurnaroundTime());
        this.writeNumberOfPreemptions(schedulingDuration.getNumPreemptions());

        this.writeAfterPartitionSchedulingDuration();
    }

    protected abstract void writeBeforePartitionSchedulingDuration();

    protected abstract void writePartitionSchedulingDurationId(String partitionId);

    protected abstract void writeResponseTime(long responseTime);

    protected abstract void writeRunTime(long runTime);

    protected abstract void writeWaitTime(long waitTime);

    protected abstract void writeTurnaroundTime(long turnaroundTime);

    protected abstract void writeNumberOfPreemptions(long numPreemptions);

    protected abstract void writeAfterPartitionSchedulingDuration();

    protected abstract void writeAfterSchedulingMetrics();

    protected abstract void writeAfterClientSideMetrics();

    protected abstract void writeAfterQueryMetrics();

}
