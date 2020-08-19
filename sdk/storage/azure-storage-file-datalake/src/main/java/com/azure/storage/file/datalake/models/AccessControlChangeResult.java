// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.models;

/**
 * AccessControlChangeResult contains result of operations that change Access Control Lists recursively.
 */
public class AccessControlChangeResult {
    private AccessControlChangeCounters counters;
    private String continuationToken;

    /**
     * Returns the {@link AccessControlChangeCounters}.
     *
     * @return {@link AccessControlChangeCounters}
     */
    public AccessControlChangeCounters getCounters() {
        return counters;
    }

    /**
     * Sets the {@link AccessControlChangeCounters}.
     *
     * @param counters {@link AccessControlChangeCounters}
     * @return The updated object.
     */
    public AccessControlChangeResult setCounters(AccessControlChangeCounters counters) {
        this.counters = counters;
        return this;
    }

    /**
     * Optional continuation token. Value is present when operation is split into multiple batches and can be used to
     * resume progress.
     *
     * @return The continuation token to pass on the next request.
     */
    public String getContinuationToken() {
        return continuationToken;
    }

    /**
     * Sets the continuation token.
     *
     * @param continuationToken Optional continuation token. Value is present when operation is split into multiple
     *                          batches and can be used to resume progress.
     * @return The updated object.
     */
    public AccessControlChangeResult setContinuationToken(String continuationToken) {
        this.continuationToken = continuationToken;
        return this;
    }
}
