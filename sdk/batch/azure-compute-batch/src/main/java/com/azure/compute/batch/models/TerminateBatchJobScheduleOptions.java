// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.compute.batch.models;

/**
 * Optional parameters for terminating a Batch Job Schedule.
 */
public class TerminateBatchJobScheduleOptions extends BatchTerminateOptions {
    private Boolean force;

    /**
     * Creates an instance of {@link TerminateBatchJobScheduleOptions}.
     */
    public TerminateBatchJobScheduleOptions() {
    }

    /**
     * Gets the value indicating whether to force the termination of the Batch job schedule.
     *
     * <p>If true, the server will terminate the job schedule even if the corresponding nodes have not fully processed
     * the termination.
     *
     * @return The value indicating whether the job schedule termination is forced.
     */
    public Boolean getForce() {
        return force;
    }

    /**
     * Sets the value indicating whether to force the termination of the Batch job schedule.
     *
     * <p>If true, the server will terminate the job schedule even if the corresponding nodes have not fully processed
     * the termination.
     *
     * @param force The value indicating whether to force the termination.
     * @return The {@link TerminateBatchJobScheduleOptions} object itself, allowing for method chaining.
     */
    public TerminateBatchJobScheduleOptions setForce(Boolean force) {
        this.force = force;
        return this;
    }
}
