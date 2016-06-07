/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol.models;


/**
 * The status of the Job Preparation and Job Release tasks on a compute node.
 */
public class JobPreparationAndReleaseTaskExecutionInformation {
    /**
     * The id of the pool containing the compute node to which this entry
     * refers.
     */
    private String poolId;

    /**
     * The id of the compute node to which this entry refers.
     */
    private String nodeId;

    /**
     * The URL of the compute node to which this entry refers.
     */
    private String nodeUrl;

    /**
     * Information about the execution status of the Job Preparation task on
     * this compute node.
     */
    private JobPreparationTaskExecutionInformation jobPreparationTaskExecutionInfo;

    /**
     * Information about the execution status of the Job Release task on this
     * compute node. This property is set only if the Job Release task has
     * run on the node.
     */
    private JobReleaseTaskExecutionInformation jobReleaseTaskExecutionInfo;

    /**
     * Get the poolId value.
     *
     * @return the poolId value
     */
    public String poolId() {
        return this.poolId;
    }

    /**
     * Set the poolId value.
     *
     * @param poolId the poolId value to set
     * @return the JobPreparationAndReleaseTaskExecutionInformation object itself.
     */
    public JobPreparationAndReleaseTaskExecutionInformation withPoolId(String poolId) {
        this.poolId = poolId;
        return this;
    }

    /**
     * Get the nodeId value.
     *
     * @return the nodeId value
     */
    public String nodeId() {
        return this.nodeId;
    }

    /**
     * Set the nodeId value.
     *
     * @param nodeId the nodeId value to set
     * @return the JobPreparationAndReleaseTaskExecutionInformation object itself.
     */
    public JobPreparationAndReleaseTaskExecutionInformation withNodeId(String nodeId) {
        this.nodeId = nodeId;
        return this;
    }

    /**
     * Get the nodeUrl value.
     *
     * @return the nodeUrl value
     */
    public String nodeUrl() {
        return this.nodeUrl;
    }

    /**
     * Set the nodeUrl value.
     *
     * @param nodeUrl the nodeUrl value to set
     * @return the JobPreparationAndReleaseTaskExecutionInformation object itself.
     */
    public JobPreparationAndReleaseTaskExecutionInformation withNodeUrl(String nodeUrl) {
        this.nodeUrl = nodeUrl;
        return this;
    }

    /**
     * Get the jobPreparationTaskExecutionInfo value.
     *
     * @return the jobPreparationTaskExecutionInfo value
     */
    public JobPreparationTaskExecutionInformation jobPreparationTaskExecutionInfo() {
        return this.jobPreparationTaskExecutionInfo;
    }

    /**
     * Set the jobPreparationTaskExecutionInfo value.
     *
     * @param jobPreparationTaskExecutionInfo the jobPreparationTaskExecutionInfo value to set
     * @return the JobPreparationAndReleaseTaskExecutionInformation object itself.
     */
    public JobPreparationAndReleaseTaskExecutionInformation withJobPreparationTaskExecutionInfo(JobPreparationTaskExecutionInformation jobPreparationTaskExecutionInfo) {
        this.jobPreparationTaskExecutionInfo = jobPreparationTaskExecutionInfo;
        return this;
    }

    /**
     * Get the jobReleaseTaskExecutionInfo value.
     *
     * @return the jobReleaseTaskExecutionInfo value
     */
    public JobReleaseTaskExecutionInformation jobReleaseTaskExecutionInfo() {
        return this.jobReleaseTaskExecutionInfo;
    }

    /**
     * Set the jobReleaseTaskExecutionInfo value.
     *
     * @param jobReleaseTaskExecutionInfo the jobReleaseTaskExecutionInfo value to set
     * @return the JobPreparationAndReleaseTaskExecutionInformation object itself.
     */
    public JobPreparationAndReleaseTaskExecutionInformation withJobReleaseTaskExecutionInfo(JobReleaseTaskExecutionInformation jobReleaseTaskExecutionInfo) {
        this.jobReleaseTaskExecutionInfo = jobReleaseTaskExecutionInfo;
        return this;
    }

}
