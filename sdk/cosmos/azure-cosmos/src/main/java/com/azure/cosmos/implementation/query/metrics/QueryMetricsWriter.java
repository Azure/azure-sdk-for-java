// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.query.metrics;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.implementation.QueryMetrics;
import com.azure.cosmos.implementation.QueryPreparationTimes;
import com.azure.cosmos.implementation.RuntimeExecutionTimes;
import com.azure.cosmos.implementation.IndexUtilizationInfo;
import com.azure.cosmos.implementation.SingleIndexUtilizationEntity;
import com.azure.cosmos.implementation.CompositeIndexUtilizationEntity;
import com.azure.cosmos.implementation.apachecommons.lang.tuple.ImmutablePair;

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

        // IndexUtilizationInfo
        this.writeIndexUtilizationInfoMetrics(queryMetrics.getIndexUtilizationInfo());

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


    //Index Utilization Info
    private void writeIndexUtilizationInfoMetrics(IndexUtilizationInfo indexUtilizationInfo) {
        this.writeBeforeIndexUtilizationInfoMetrics();

        this.writeUtilizedSingleIndexesMetrics(indexUtilizationInfo);
        this.writePotentialSingleIndexesMetrics(indexUtilizationInfo);
        this.writeUtilizedCompositeIndexesMetrics(indexUtilizationInfo);
        this.writePotentialCompositeIndexesMetrics(indexUtilizationInfo);

        this.writeAfterIndexUtilizationInfoMetrics();
    }

    protected abstract void writeBeforeIndexUtilizationInfoMetrics();

    // utilizedSingleIndexes
    private void writeUtilizedSingleIndexesMetrics(IndexUtilizationInfo indexUtilizationInfo) {
        this.writeBeforeUtilizedSingleIndexesMetrics();
        for (SingleIndexUtilizationEntity singleIndexUtilizationEntity : indexUtilizationInfo.getUtilizedSingleIndexes()) {
            this.writeUtilizedSingleIndex(singleIndexUtilizationEntity);
        }
        this.writeAfterUtilizedSingleIndexesMetrics();
    }

    protected abstract void writeBeforeUtilizedSingleIndexesMetrics();

    private void writeUtilizedSingleIndex(SingleIndexUtilizationEntity singleIndexUtilizationEntity) {
        this.writeBeforeUtilizedSingleIndex();

        this.writeUtilizedSingleFilterExpression(singleIndexUtilizationEntity.getFilterExpression());
        this.writeUtilizedSingleIndexDocumentExpression(singleIndexUtilizationEntity.getIndexDocumentExpression());
        this.writeUtilizedSingleFilterExpressionPrecision(singleIndexUtilizationEntity.isFilterExpressionPrecision());
        this.writeUtilizedSingleIndexPlanFullFidelity(singleIndexUtilizationEntity.isIndexPlanFullFidelity());
        this.writeUtilizedSingleIndexImpactScore(singleIndexUtilizationEntity.getIndexImpactScore());

        this.writeAfterUtilizedSingleIndex();
    }

    protected abstract void writeBeforeUtilizedSingleIndex();

    protected abstract void writeUtilizedSingleFilterExpression(String filterExpression);

    protected abstract void writeUtilizedSingleIndexDocumentExpression(String indexDocumentExpression);

    protected abstract void writeUtilizedSingleFilterExpressionPrecision(boolean filterExpressionPrecision);

    protected abstract void writeUtilizedSingleIndexPlanFullFidelity(boolean indexPlanFullFidelity);

    protected abstract void writeUtilizedSingleIndexImpactScore(String indexImpactScore);

    protected abstract void writeAfterUtilizedSingleIndex();

    protected abstract void writeAfterUtilizedSingleIndexesMetrics();

    // potentialSingleIndexes
    private void writePotentialSingleIndexesMetrics(IndexUtilizationInfo indexUtilizationInfo) {
        this.writeBeforePotentialSingleIndexesMetrics();
        for (SingleIndexUtilizationEntity singleIndexUtilizationEntity : indexUtilizationInfo.getPotentialSingleIndexes()) {
            this.writePotentialSingleIndex(singleIndexUtilizationEntity);
        }
        this.writeAfterPotentialSingleIndexesMetrics();
    }

    protected abstract void writeBeforePotentialSingleIndexesMetrics();

    private void writePotentialSingleIndex(SingleIndexUtilizationEntity singleIndexUtilizationEntity) {
        this.writeBeforePotentialSingleIndex();

        this.writePotentialSingleFilterExpression(singleIndexUtilizationEntity.getFilterExpression());
        this.writePotentialSingleIndexDocumentExpression(singleIndexUtilizationEntity.getIndexDocumentExpression());
        this.writePotentialSingleFilterExpressionPrecision(singleIndexUtilizationEntity.isFilterExpressionPrecision());
        this.writePotentialSingleIndexPlanFullFidelity(singleIndexUtilizationEntity.isIndexPlanFullFidelity());
        this.writePotentialSingleIndexImpactScore(singleIndexUtilizationEntity.getIndexImpactScore());

        this.writeAfterPotentialSingleIndex();
    }

    protected abstract void writeBeforePotentialSingleIndex();

    protected abstract void writePotentialSingleFilterExpression(String filterExpression);

    protected abstract void writePotentialSingleIndexDocumentExpression(String indexDocumentExpression);

    protected abstract void writePotentialSingleFilterExpressionPrecision(boolean filterExpressionPrecision);

    protected abstract void writePotentialSingleIndexPlanFullFidelity(boolean indexPlanFullFidelity);

    protected abstract void writePotentialSingleIndexImpactScore(String indexImpactScore);

    protected abstract void writeAfterPotentialSingleIndex();

    protected abstract void writeAfterPotentialSingleIndexesMetrics();

    // utilizedCompositeIndexes
    private void writeUtilizedCompositeIndexesMetrics(IndexUtilizationInfo indexUtilizationInfo) {
        this.writeBeforeUtilizedCompositeIndexesMetrics();
        for (CompositeIndexUtilizationEntity compositeIndexUtilizationEntity : indexUtilizationInfo.getUtilizedCompositeIndexes()) {
            this.writeUtilizedCompositeIndex(compositeIndexUtilizationEntity);
        }
        this.writeAfterUtilizedCompositeIndexesMetrics();
    }

    protected abstract void writeBeforeUtilizedCompositeIndexesMetrics();

    private void writeUtilizedCompositeIndex(CompositeIndexUtilizationEntity compositeIndexUtilizationEntity) {
        this.writeBeforeUtilizedCompositeIndex();

        this.writeUtilizedCompositeIndexDocumentExpressions(compositeIndexUtilizationEntity.getIndexDocumentExpressions());
        this.writeUtilizedCompositeIndexPlanFullFidelity(compositeIndexUtilizationEntity.isIndexPlanFullFidelity());
        this.writeUtilizedCompositeIndexImpactScore(compositeIndexUtilizationEntity.getIndexImpactScore());

        this.writeAfterUtilizedCompositeIndex();
    }

    protected abstract void writeBeforeUtilizedCompositeIndex();

    protected abstract void writeUtilizedCompositeIndexDocumentExpressions(List<String> indexDocumentExpressions);

    protected abstract void writeUtilizedCompositeIndexPlanFullFidelity(boolean indexPlanFullFidelity);

    protected abstract void writeUtilizedCompositeIndexImpactScore(String indexImpactScore);

    protected abstract void writeAfterUtilizedCompositeIndex();

    protected abstract void writeAfterUtilizedCompositeIndexesMetrics();

    // potentialCompositeIndexes
    private void writePotentialCompositeIndexesMetrics(IndexUtilizationInfo indexUtilizationInfo) {
        this.writeBeforePotentialCompositeIndexesMetrics();
        for (CompositeIndexUtilizationEntity compositeIndexUtilizationEntity : indexUtilizationInfo.getPotentialCompositeIndexes()) {
            this.writePotentialCompositeIndex(compositeIndexUtilizationEntity);
        }
        this.writeAfterPotentialCompositeIndexesMetrics();
    }

    protected abstract void writeBeforePotentialCompositeIndexesMetrics();

    private void writePotentialCompositeIndex(CompositeIndexUtilizationEntity compositeIndexUtilizationEntity) {
        this.writeBeforePotentialCompositeIndex();

        this.writePotentialCompositeIndexDocumentExpressions(compositeIndexUtilizationEntity.getIndexDocumentExpressions());
        this.writePotentialCompositeIndexPlanFullFidelity(compositeIndexUtilizationEntity.isIndexPlanFullFidelity());
        this.writePotentialCompositeIndexImpactScore(compositeIndexUtilizationEntity.getIndexImpactScore());

        this.writeAfterPotentialCompositeIndex();
    }

    protected abstract void writeBeforePotentialCompositeIndex();

    protected abstract void writePotentialCompositeIndexDocumentExpressions(List<String> indexDocumentExpressions);

    protected abstract void writePotentialCompositeIndexPlanFullFidelity(boolean indexPlanFullFidelity);

    protected abstract void writePotentialCompositeIndexImpactScore(String indexImpactScore);

    protected abstract void writeAfterPotentialCompositeIndex();

    protected abstract void writeAfterPotentialCompositeIndexesMetrics();


    protected abstract void writeAfterIndexUtilizationInfoMetrics();

    protected abstract void writeAfterQueryMetrics();

}
