// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.internal;

import com.azure.data.cosmos.internal.query.metrics.ClientSideMetrics;
import com.azure.data.cosmos.internal.query.metrics.FetchExecutionRange;
import com.azure.data.cosmos.internal.query.metrics.QueryMetricsTextWriter;
import com.azure.data.cosmos.internal.query.metrics.SchedulingTimeSpan;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * Query metrics in the Azure Cosmos database service.
 * This metric represents a moving average for a set of queries whose metrics have been aggregated together.
 */
public final class QueryMetrics {
    public static QueryMetrics ZERO = new QueryMetrics(
            new ArrayList<>(), /* */
            0, /* retrievedDocumentCount */
            0, /* retrievedDocumentSize */
            0, /* outputDocumentCount */
            0, /* outputDocumentSize */
            0, /* indexHitCount */
            Duration.ZERO,
            QueryPreparationTimes.ZERO,
            Duration.ZERO,
            Duration.ZERO,
            Duration.ZERO,
            RuntimeExecutionTimes.ZERO,
            Duration.ZERO,
            ClientSideMetrics.ZERO);
    private final long retrievedDocumentCount;
    private final long retrievedDocumentSize;
    private final long outputDocumentCount;
    private final long outputDocumentSize;
    private final long indexHitDocumentCount;
    private final Duration totalQueryExecutionTime;
    private final QueryPreparationTimes queryPreparationTimes;
    private final Duration indexLookupTime;
    private final Duration documentLoadTime;
    private final Duration vmExecutionTime;
    private final RuntimeExecutionTimes runtimeExecutionTimes;
    private final Duration documentWriteTime;
    private final ClientSideMetrics clientSideMetrics;
    private final List<String> activityIds;

    public QueryMetrics(List<String> activities, long retrievedDocumentCount, long retrievedDocumentSize, long outputDocumentCount,
                        long outputDocumentSize, long indexHitCount, Duration totalQueryExecutionTime,
                        QueryPreparationTimes queryPreparationTimes, Duration indexLookupTime, Duration documentLoadTime,
                        Duration vmExecutionTime, RuntimeExecutionTimes runtimeExecutionTimes, Duration documentWriteTime,
                        ClientSideMetrics clientSideMetrics) {
        this.retrievedDocumentCount = retrievedDocumentCount;
        this.retrievedDocumentSize = retrievedDocumentSize;
        this.outputDocumentCount = outputDocumentCount;
        this.outputDocumentSize = outputDocumentSize;
        this.indexHitDocumentCount = indexHitCount;
        this.totalQueryExecutionTime = totalQueryExecutionTime;
        this.queryPreparationTimes = queryPreparationTimes;
        this.indexLookupTime = indexLookupTime;
        this.documentLoadTime = documentLoadTime;
        this.vmExecutionTime = vmExecutionTime;
        this.runtimeExecutionTimes = runtimeExecutionTimes;
        this.documentWriteTime = documentWriteTime;
        this.clientSideMetrics = clientSideMetrics;
        this.activityIds = activities;
    }

    /**
     * @return the retrievedDocumentCount
     */
    public long getRetrievedDocumentCount() {
        return retrievedDocumentCount;
    }

    /**
     * @return the retrievedDocumentSize
     */
    public long getRetrievedDocumentSize() {
        return retrievedDocumentSize;
    }

    /**
     * @return the outputDocumentCount
     */
    public long getOutputDocumentCount() {
        return outputDocumentCount;
    }

    /**
     * @return the outputDocumentSize
     */
    public long getOutputDocumentSize() {
        return outputDocumentSize;
    }

    /**
     * @return the indexHitDocumentCount
     */
    public long getIndexHitDocumentCount() {
        return indexHitDocumentCount;
    }

    /**
     * Gets the index hit ratio by query in the Azure Cosmos database service.
     *
     * @return the IndexHitRatio
     */
    public double getIndexHitRatio() {
        return this.retrievedDocumentCount == 0 ? 1 : (double) this.indexHitDocumentCount / this.retrievedDocumentCount;
    }

    /**
     * @return the totalQueryExecutionTime
     */
    public Duration getTotalQueryExecutionTime() {
        return totalQueryExecutionTime;
    }

    /**
     * @return the queryPreparationTimes
     */
    public QueryPreparationTimes getQueryPreparationTimes() {
        return queryPreparationTimes;
    }

    /**
     * @return the indexLookupTime
     */
    public Duration getIndexLookupTime() {
        return indexLookupTime;
    }

    /**
     * @return the documentLoadTime
     */
    public Duration getDocumentLoadTime() {
        return documentLoadTime;
    }

    /**
     * @return the vmExecutionTime
     */
    public Duration getVMExecutionTime() {
        return vmExecutionTime;
    }

    /**
     * @return the runtimeExecutionTimes
     */
    public RuntimeExecutionTimes getRuntimeExecutionTimes() {
        return runtimeExecutionTimes;
    }

    /**
     * @return the documentWriteTime
     */
    public Duration getDocumentWriteTime() {
        return documentWriteTime;
    }

    /**
     * @return the clientSideMetrics
     */
    public ClientSideMetrics getClientSideMetrics() {
        return clientSideMetrics;
    }

    /**
     * @return number of reties in the Azure Cosmos database service.
     */
    public long getRetries() {
        return this.clientSideMetrics.getRetries();
    }

    public QueryMetrics add(QueryMetrics... queryMetricsArgs) {
        ArrayList<QueryMetrics> queryMetricsList = new ArrayList<QueryMetrics>();
        for (QueryMetrics queryMetrics : queryMetricsArgs) {
            queryMetricsList.add(queryMetrics);
        }

        queryMetricsList.add(this);

        return QueryMetrics.createFromCollection(queryMetricsList);
    }

    private String toTextString() {
        return toTextString(0);
    }

    private String toTextString(int indentLevel) {
        StringBuilder stringBuilder = new StringBuilder();
        QueryMetricsTextWriter queryMetricsTextWriter = new QueryMetricsTextWriter(stringBuilder);
        queryMetricsTextWriter.writeQueryMetrics(this);
        return stringBuilder.toString();
    }

    public static QueryMetrics createFromCollection(Collection<QueryMetrics> queryMetricsCollection) {
        long retrievedDocumentCount = 0;
        long retrievedDocumentSize = 0;
        long outputDocumentCount = 0;
        long outputDocumentSize = 0;
        long indexHitDocumentCount = 0;
        Duration totalQueryExecutionTime = Duration.ZERO;
        Collection<QueryPreparationTimes> queryPreparationTimesCollection = new ArrayList<QueryPreparationTimes>();
        Duration indexLookupTime = Duration.ZERO;
        Duration documentLoadTime = Duration.ZERO;
        Duration vmExecutionTime = Duration.ZERO;
        Collection<RuntimeExecutionTimes> runtimeExecutionTimesCollection = new ArrayList<RuntimeExecutionTimes>();
        Duration documentWriteTime = Duration.ZERO;
        Collection<ClientSideMetrics> clientSideMetricsCollection = new ArrayList<ClientSideMetrics>();
        List<String> activityIds = new ArrayList<>();

        for (QueryMetrics queryMetrics : queryMetricsCollection) {
            if (queryMetrics == null) {
                throw new NullPointerException("queryMetricsList can not have null elements");
            }
            activityIds.addAll(queryMetrics.activityIds);
            retrievedDocumentCount += queryMetrics.retrievedDocumentCount;
            retrievedDocumentSize += queryMetrics.retrievedDocumentSize;
            outputDocumentCount += queryMetrics.outputDocumentCount;
            outputDocumentSize += queryMetrics.outputDocumentSize;
            indexHitDocumentCount += queryMetrics.indexHitDocumentCount;
            totalQueryExecutionTime = totalQueryExecutionTime.plus(queryMetrics.totalQueryExecutionTime);
            queryPreparationTimesCollection.add(queryMetrics.queryPreparationTimes);
            indexLookupTime = indexLookupTime.plus(queryMetrics.indexLookupTime);
            documentLoadTime = documentLoadTime.plus(queryMetrics.documentLoadTime);
            vmExecutionTime = vmExecutionTime.plus(queryMetrics.vmExecutionTime);
            runtimeExecutionTimesCollection.add(queryMetrics.runtimeExecutionTimes);
            documentWriteTime = documentWriteTime.plus(queryMetrics.documentWriteTime);
            clientSideMetricsCollection.add(queryMetrics.clientSideMetrics);
        }

        return new QueryMetrics(activityIds, retrievedDocumentCount, retrievedDocumentSize, outputDocumentCount,
                outputDocumentSize,
                indexHitDocumentCount, totalQueryExecutionTime,
                QueryPreparationTimes.createFromCollection(queryPreparationTimesCollection), indexLookupTime, documentLoadTime,
                vmExecutionTime, RuntimeExecutionTimes.createFromCollection(runtimeExecutionTimesCollection),
                documentWriteTime, ClientSideMetrics.createFromCollection(clientSideMetricsCollection));
    }

    private static double getOrDefault(HashMap<String, Double> metrics, String key) {
        Double doubleReference = metrics.get(key);
        return doubleReference == null ? 0 : doubleReference;
    }

    public static QueryMetrics createFromDelimitedString(String delimitedString) {
        HashMap<String, Double> metrics = QueryMetricsUtils.parseDelimitedString(delimitedString);
        return QueryMetrics.createFromDelimitedStringAndClientSideMetrics(delimitedString,
                new ClientSideMetrics(0, 0, new ArrayList<FetchExecutionRange>(),
                        new ArrayList<ImmutablePair<String, SchedulingTimeSpan>>()), "");
    }

    public static QueryMetrics createFromDelimitedStringAndClientSideMetrics(String delimitedString, ClientSideMetrics clientSideMetrics,
                                                                      String activityId) {
        HashMap<String, Double> metrics = QueryMetricsUtils.parseDelimitedString(delimitedString);
        double indexHitRatio;
        double retrievedDocumentCount;
        indexHitRatio = metrics.get(QueryMetricsConstants.IndexHitRatio);
        retrievedDocumentCount = metrics.get(QueryMetricsConstants.RetrievedDocumentCount);
        long indexHitCount = (long) (indexHitRatio * retrievedDocumentCount);
        double outputDocumentCount = metrics.get(QueryMetricsConstants.OutputDocumentCount);
        double outputDocumentSize = metrics.get(QueryMetricsConstants.OutputDocumentSize);
        double retrievedDocumentSize = metrics.get(QueryMetricsConstants.RetrievedDocumentSize);
        Duration totalQueryExecutionTime = QueryMetricsUtils.getDurationFromMetrics(metrics, QueryMetricsConstants.TotalQueryExecutionTimeInMs);

        List<String> activities = new ArrayList<>();
        activities.add(activityId);

        return new QueryMetrics(
                activities,
                (long) retrievedDocumentCount,
                (long) retrievedDocumentSize,
                (long) outputDocumentCount,
                (long) outputDocumentSize,
                indexHitCount,
                totalQueryExecutionTime,
                QueryPreparationTimes.createFromDelimitedString(delimitedString),
                QueryMetricsUtils.getDurationFromMetrics(metrics, QueryMetricsConstants.IndexLookupTimeInMs),
                QueryMetricsUtils.getDurationFromMetrics(metrics, QueryMetricsConstants.DocumentLoadTimeInMs),
                QueryMetricsUtils.getDurationFromMetrics(metrics, QueryMetricsConstants.VMExecutionTimeInMs),
                RuntimeExecutionTimes.createFromDelimitedString(delimitedString),
                QueryMetricsUtils.getDurationFromMetrics(metrics, QueryMetricsConstants.DocumentWriteTimeInMs),
                clientSideMetrics);
    }

    @Override
    public String toString() {
        return toTextString(0);
    }
}