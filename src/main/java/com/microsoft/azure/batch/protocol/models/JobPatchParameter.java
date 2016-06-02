/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol.models;

import java.util.List;

/**
 * Parameters for a CloudJobOperations.Patch request.
 */
public class JobPatchParameter {
    /**
     * The priority of the job. Priority values can range from -1000 to 1000,
     * with -1000 being the lowest priority and 1000 being the highest
     * priority. If omitted, the priority of the job is left unchanged.
     */
    private Integer priority;

    /**
     * The execution constraints for the job. If omitted, the existing
     * execution constraints are left unchanged.
     */
    private JobConstraints constraints;

    /**
     * The pool on which the Batch service runs the job's tasks. If omitted,
     * the job continues to run on its current pool.
     */
    private PoolInformation poolInfo;

    /**
     * A list of name-value pairs associated with the job as metadata. If
     * omitted, the existing job metadata is left unchanged.
     */
    private List<MetadataItem> metadata;

    /**
     * Get the priority value.
     *
     * @return the priority value
     */
    public Integer priority() {
        return this.priority;
    }

    /**
     * Set the priority value.
     *
     * @param priority the priority value to set
     * @return the JobPatchParameter object itself.
     */
    public JobPatchParameter withPriority(Integer priority) {
        this.priority = priority;
        return this;
    }

    /**
     * Get the constraints value.
     *
     * @return the constraints value
     */
    public JobConstraints constraints() {
        return this.constraints;
    }

    /**
     * Set the constraints value.
     *
     * @param constraints the constraints value to set
     * @return the JobPatchParameter object itself.
     */
    public JobPatchParameter withConstraints(JobConstraints constraints) {
        this.constraints = constraints;
        return this;
    }

    /**
     * Get the poolInfo value.
     *
     * @return the poolInfo value
     */
    public PoolInformation poolInfo() {
        return this.poolInfo;
    }

    /**
     * Set the poolInfo value.
     *
     * @param poolInfo the poolInfo value to set
     * @return the JobPatchParameter object itself.
     */
    public JobPatchParameter withPoolInfo(PoolInformation poolInfo) {
        this.poolInfo = poolInfo;
        return this;
    }

    /**
     * Get the metadata value.
     *
     * @return the metadata value
     */
    public List<MetadataItem> metadata() {
        return this.metadata;
    }

    /**
     * Set the metadata value.
     *
     * @param metadata the metadata value to set
     * @return the JobPatchParameter object itself.
     */
    public JobPatchParameter withMetadata(List<MetadataItem> metadata) {
        this.metadata = metadata;
        return this;
    }

}
