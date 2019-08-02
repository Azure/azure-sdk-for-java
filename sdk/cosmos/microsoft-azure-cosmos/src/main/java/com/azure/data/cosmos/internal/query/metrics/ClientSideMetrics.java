// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.internal.query.metrics;

import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * Client side QueryMetrics
 */
public class ClientSideMetrics {

    public static final ClientSideMetrics ZERO = new ClientSideMetrics(
            0, /* retries*/
            0, /* requestCharge */
            new ArrayList<>(), /* fetchExecutionRanges */
            new ArrayList<>()); /* partitionSchedulingTimeSpans */

    private final long retries;
    private final double requestCharge;
    private final List<FetchExecutionRange> fetchExecutionRanges;
    private final List<ImmutablePair<String, SchedulingTimeSpan>> partitionSchedulingTimeSpans;

    /**
     * Constructor
     *
     * @param retries             The number of retries required to execute the query.
     * @param requestCharge       The request charge incurred from executing the query.
     * @param executionRanges     The fetch execution ranges from executing the query.
     * @param schedulingTimeSpans The partition scheduling timespans from the query.
     */
    public ClientSideMetrics(int retries, double requestCharge, List<FetchExecutionRange> executionRanges,
                             List<ImmutablePair<String, SchedulingTimeSpan>> schedulingTimeSpans) {
        if (executionRanges == null || executionRanges.contains(null)) {
            throw new NullPointerException("executionRanges");
        }
        if (schedulingTimeSpans == null || schedulingTimeSpans.contains(null)) {
            throw new NullPointerException("schedulingTimeSpans");
        }
        if (retries < 0) {
            throw new IllegalArgumentException("retries must not be negative");
        }
        if (requestCharge < 0) {
            throw new IllegalArgumentException("requestCharge must not be negative");
        }

        this.retries = retries;
        this.requestCharge = requestCharge;
        this.fetchExecutionRanges = executionRanges;
        this.partitionSchedulingTimeSpans = schedulingTimeSpans;
    }

    /**
     * Gets number of retries in the Azure Cosmos database service
     *
     * @return the retries
     */
    public long getRetries() {
        return retries;
    }

    /**
     * Gets the request charge for this continuation of the query.
     *
     * @return the requestCharge
     */
    public double getRequestCharge() {
        return requestCharge;
    }

    /**
     * create ClientSideMetrics from collection
     *
     * @param clientSideMetricsCollection
     * @return
     */
    public static ClientSideMetrics createFromCollection(Collection<ClientSideMetrics> clientSideMetricsCollection) {
        if (clientSideMetricsCollection == null) {
            throw new NullPointerException("clientSideMetricsCollection");
        }

        int retries = 0;
        double requestCharge = 0;
        List<FetchExecutionRange> fetchExecutionRanges = new ArrayList<>();
        List<ImmutablePair<String, SchedulingTimeSpan>> partitionSchedulingTimeSpans = new ArrayList<>();

        for (ClientSideMetrics clientSideQueryMetrics : clientSideMetricsCollection) {
            retries += clientSideQueryMetrics.retries;
            requestCharge += clientSideQueryMetrics.requestCharge;
            fetchExecutionRanges.addAll(clientSideQueryMetrics.fetchExecutionRanges);
            partitionSchedulingTimeSpans.addAll(clientSideQueryMetrics.partitionSchedulingTimeSpans);
        }

        return new ClientSideMetrics(retries, requestCharge, fetchExecutionRanges, partitionSchedulingTimeSpans);
    }

    static double getOrDefault(HashMap<String, Double> metrics, String key) {
        Double doubleReference = metrics.get(key);
        return doubleReference == null ? 0 : doubleReference;
    }

    /**
     * Gets the Fetch Execution Ranges for this continuation of the query.
     *
     * @return the Fetch Execution Ranges for this continuation of the query
     */
    public List<FetchExecutionRange> getFetchExecutionRanges() {
        return fetchExecutionRanges;
    }

    /**
     * Gets the Partition Scheduling TimeSpans for this query.
     *
     * @return the List of Partition Scheduling TimeSpans for this query
     */
    public List<ImmutablePair<String, SchedulingTimeSpan>> getPartitionSchedulingTimeSpans() {
        return partitionSchedulingTimeSpans;
    }
}
