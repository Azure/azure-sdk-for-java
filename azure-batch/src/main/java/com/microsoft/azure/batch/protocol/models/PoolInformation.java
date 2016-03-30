/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol.models;


/**
 * Specifies how a job should be assigned to a pool.
 */
public class PoolInformation {
    /**
     * Gets or sets the id of an existing pool. All the tasks of the job will
     * run on the specified pool. You must specify either PoolId or
     * AutoPoolSpecification, but not both.
     */
    private String poolId;

    /**
     * Gets or sets characteristics for a temporary 'auto pool.' The Batch
     * service will create this auto pool and run all the tasks of the job on
     * it, and will delete the pool once the job has completed. You must
     * specify either PoolId or AutoPoolSpecification, but not both.
     */
    private AutoPoolSpecification autoPoolSpecification;

    /**
     * Get the poolId value.
     *
     * @return the poolId value
     */
    public String getPoolId() {
        return this.poolId;
    }

    /**
     * Set the poolId value.
     *
     * @param poolId the poolId value to set
     */
    public void setPoolId(String poolId) {
        this.poolId = poolId;
    }

    /**
     * Get the autoPoolSpecification value.
     *
     * @return the autoPoolSpecification value
     */
    public AutoPoolSpecification getAutoPoolSpecification() {
        return this.autoPoolSpecification;
    }

    /**
     * Set the autoPoolSpecification value.
     *
     * @param autoPoolSpecification the autoPoolSpecification value to set
     */
    public void setAutoPoolSpecification(AutoPoolSpecification autoPoolSpecification) {
        this.autoPoolSpecification = autoPoolSpecification;
    }

}
