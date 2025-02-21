// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.models;

import java.util.List;

/**
 * AccessControlChanges contains batch and cumulative counts of operations that change Access Control Lists recursively.
 * Additionally it exposes path entries that failed to update while these operations progress.
 */
public class AccessControlChanges {
    private List<AccessControlChangeFailure> batchFailures;
    private AccessControlChangeCounters batchCounters;
    private AccessControlChangeCounters aggregateCounters;
    private String continuationToken;

    /**
     * Returns a list of path entries that failed to update Access Control List within a single batch.
     *
     * @return A list of path entries that failed to update Access Control List within a single batch.
     */
    public List<AccessControlChangeFailure> getBatchFailures() {
        return batchFailures;
    }

    /**
     * Sets a list of path entries that failed to update Access Control List within a single batch.
     *
     * @param batchFailures A list of path entries that failed to update Access Control List within a single batch.
     * @return The updated object
     */
    public AccessControlChanges setBatchFailures(List<AccessControlChangeFailure> batchFailures) {
        this.batchFailures = batchFailures;
        return this;
    }

    /**
     * Returns an {@link AccessControlChangeCounters} that contains counts of paths changed within a single batch.
     *
     * @return {@link AccessControlChangeCounters}
     */
    public AccessControlChangeCounters getBatchCounters() {
        return batchCounters;
    }

    /**
     * Sets an {@link AccessControlChangeCounters} that contains counts of paths changed within a single batch.
     *
     * @param batchCounters {@link AccessControlChangeCounters}
     * @return The updated object.
     */
    public AccessControlChanges setBatchCounters(AccessControlChangeCounters batchCounters) {
        this.batchCounters = batchCounters;
        return this;
    }

    /**
     * Returns an {@link AccessControlChangeCounters} that contains counts of paths changed from start of the operation.
     *
     * @return {@link AccessControlChangeCounters}
     */
    public AccessControlChangeCounters getAggregateCounters() {
        return aggregateCounters;
    }

    /**
     * Sets an {@link AccessControlChangeCounters} that contains counts of paths changed from start of the operation.
     *
     * @param aggregateCounters {@link AccessControlChangeCounters}
     * @return The updated object.
     */
    public AccessControlChanges setAggregateCounters(AccessControlChangeCounters aggregateCounters) {
        this.aggregateCounters = aggregateCounters;
        return this;
    }

    /**
     * Returns the continuation token.
     * <p>
     * Value is present when operation is split into multiple batches and can be used to resume progress.
     *
     * @return The continuation token
     */
    public String getContinuationToken() {
        return continuationToken;
    }

    /**
     * Sets the continuation token.
     *
     * @param continuationToken The continuation token.
     * @return The updated object.
     */
    public AccessControlChanges setContinuationToken(String continuationToken) {
        this.continuationToken = continuationToken;
        return this;
    }
}
