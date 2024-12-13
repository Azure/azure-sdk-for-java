// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.compute.batch.models;

/**
 * Optional parameters for terminating a Batch Job, marking it as completed.
 */
public class TerminateBatchJobOptions extends BatchTerminateOptions {
    private Boolean force;

    /**
     * Creates an instance of {@link TerminateBatchJobOptions}.
     */
    public TerminateBatchJobOptions() {
    }

    /**
     * Gets the value indicating whether to force the termination of the Batch job.
     *
     * <p>If true, the server will terminate the job even if the corresponding nodes have not fully processed
     * the termination.
     *
     * @return The value indicating whether the job termination is forced.
     */
    public Boolean getForce() {
        return force;
    }

    /**
     * Sets the value indicating whether to force the termination of the Batch job.
     *
     * <p>If true, the server will terminate the job even if the corresponding nodes have not fully processed
     * the termination.
     *
     * @param force The value indicating whether to force the termination.
     * @return The {@link TerminateBatchJobOptions} object itself, allowing for method chaining.
     */
    public TerminateBatchJobOptions setForce(Boolean force) {
        this.force = force;
        return this;
    }
}
