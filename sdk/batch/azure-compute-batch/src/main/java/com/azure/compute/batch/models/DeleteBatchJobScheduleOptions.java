// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.compute.batch.models;

import com.azure.core.http.RequestConditions;

/**
 * Optional parameters for deleting a Batch job schedule.
 */
public class DeleteBatchJobScheduleOptions extends BatchBaseOptions {
    private Boolean force;
    private RequestConditions requestConditions;

    /**
     * Creates an instance of {@link DeleteBatchJobScheduleOptions}.
     */
    public DeleteBatchJobScheduleOptions() {
    }

    /**
     * Gets the value indicating whether to force the deletion of the Batch job schedule.
     *
     * <p>If true, the server will delete the job schedule even if the corresponding nodes have not fully processed
     * the deletion.
     *
     * @return The value indicating whether the job schedule deletion is forced.
     */
    public Boolean getForce() {
        return force;
    }

    /**
     * Sets the value indicating whether to force the deletion of the Batch job schedule.
     *
     * <p>If true, the server will delete the job schedule even if the corresponding nodes have not fully processed
     * the deletion.
     *
     * @param force The value indicating whether to force the deletion.
     * @return The {@link DeleteBatchJobScheduleOptions} object itself, allowing for method chaining.
     */
    public DeleteBatchJobScheduleOptions setForce(Boolean force) {
        this.force = force;
        return this;
    }

    /**
     * Gets the HTTP options for conditional requests based on modification time.
     *
     * <p>The request conditions allow you to specify conditions that must be met for the request to be processed.
     *
     * @return The HTTP options for conditional requests.
     */
    public RequestConditions getRequestConditions() {
        return requestConditions;
    }

    /**
     * Sets the HTTP options for conditional requests based on modification time.
     *
     * <p>The request conditions allow you to specify conditions that must be met for the request to be processed.
     *
     * @param requestConditions The HTTP options for conditional requests.
     * @return The {@link DeleteBatchJobScheduleOptions} object itself, allowing for method chaining.
     */
    public DeleteBatchJobScheduleOptions setRequestConditions(RequestConditions requestConditions) {
        this.requestConditions = requestConditions;
        return this;
    }
}
