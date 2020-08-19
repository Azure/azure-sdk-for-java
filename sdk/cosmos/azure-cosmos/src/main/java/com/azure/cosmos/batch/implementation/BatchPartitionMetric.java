// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.batch.implementation;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;

public class BatchPartitionMetric {

    private long numberOfItemsOperatedOn;
    private long timeTakenInMilliseconds;
    private long numberOfThrottles;

    public BatchPartitionMetric() {
        this.numberOfItemsOperatedOn = 0;
        this.timeTakenInMilliseconds = 0;
        this.numberOfThrottles = 0;
    }

    /**
     * Initializes a new instance of the OperationMetrics class (instance constructor).
     *
     * @param numberOfItemsOperatedOn Number of documents operated on.
     * @param timeTakenInMilliseconds Amount of time taken to insert the documents.
     * @param numberOfThrottles The number of throttles encountered to insert the documents.
     */
    public BatchPartitionMetric(long numberOfItemsOperatedOn, long timeTakenInMilliseconds, long numberOfThrottles) {
        checkArgument(numberOfItemsOperatedOn >= 0, "numberOfItemsOperatedOn must be non negative");
        checkArgument(timeTakenInMilliseconds >= 0, "timeTakenInMilliseconds must be non negative");
        checkArgument(numberOfThrottles >= 0, "numberOfThrottles must be non negative");

        this.numberOfItemsOperatedOn = numberOfItemsOperatedOn;
        this.timeTakenInMilliseconds = timeTakenInMilliseconds;
        this.numberOfThrottles = numberOfThrottles;
    }

    public void add(long numberOfItemsOperatedOn, long timeTakenInMilliseconds, long numberOfThrottles) {
        checkArgument(numberOfItemsOperatedOn >= 0, "numberOfItemsOperatedOn must be non negative");
        checkArgument(timeTakenInMilliseconds >= 0, "timeTakenInMilliseconds must be non negative");
        checkArgument(numberOfThrottles >= 0, "numberOfThrottles must be non negative");

        this.numberOfItemsOperatedOn += numberOfItemsOperatedOn;
        this.timeTakenInMilliseconds += timeTakenInMilliseconds;
        this.numberOfThrottles += numberOfThrottles;
    }

    /**
     * Gets the number of documents operated on.
     *
     * @return total numberOfItemsOperatedOn.
     */
    public long getNumberOfItemsOperatedOn() {
        return numberOfItemsOperatedOn;
    }

    /**
     * Gets the time taken to operate on the documents.
     *
     * @return total timeTakenInMilliseconds.
     */
    public long getTimeTakenInMilliseconds() {
        return timeTakenInMilliseconds;
    }

    /**
     * Gets the number of throttles incurred while operating on the documents.
     *
     * @return total numberOfThrottles.
     */
    public long getNumberOfThrottles() {
        return numberOfThrottles;
    }

    @Override
    public String toString() {
        return "BatchPartitionMetric{" +
            "numberOfItemsOperatedOn=" + numberOfItemsOperatedOn +
            ", timeTakenInMilliseconds=" + timeTakenInMilliseconds +
            ", numberOfThrottles=" + numberOfThrottles +
            '}';
    }
}
