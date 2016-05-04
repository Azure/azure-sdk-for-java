/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol.implementation.api;


/**
 * The status of the Job Preparation and Job Release tasks on a particular
 * compute node.
 */
public class JobPreparationAndReleaseTaskExecutionInformationInner {
    /**
     * Gets or sets the id of the pool containing the compute node to which
     * this entry refers.
     */
    private String poolId;

    /**
     * Gets or sets the id of the compute node to which this entry refers.
     */
    private String nodeId;

    /**
     * Gets or sets the URL of the compute node to which this entry refers.
     */
    private String nodeUrl;

    /**
     * Gets or sets information about the execution status of the Job
     * Preparation task on this compute node.
     */
    private JobPreparationTaskExecutionInformation jobPreparationTaskExecutionInfo;

    /**
     * Gets or sets information about the execution status of the Job Release
     * task on this compute node. This property is set only if the Job
     * Release task has run on the node.
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
     * @return the JobPreparationAndReleaseTaskExecutionInformationInner object itself.
     */
    public JobPreparationAndReleaseTaskExecutionInformationInner setPoolId(String poolId) {
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
     * @return the JobPreparationAndReleaseTaskExecutionInformationInner object itself.
     */
    public JobPreparationAndReleaseTaskExecutionInformationInner setNodeId(String nodeId) {
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
     * @return the JobPreparationAndReleaseTaskExecutionInformationInner object itself.
     */
    public JobPreparationAndReleaseTaskExecutionInformationInner setNodeUrl(String nodeUrl) {
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
     * @return the JobPreparationAndReleaseTaskExecutionInformationInner object itself.
     */
    public JobPreparationAndReleaseTaskExecutionInformationInner setJobPreparationTaskExecutionInfo(JobPreparationTaskExecutionInformation jobPreparationTaskExecutionInfo) {
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
     * @return the JobPreparationAndReleaseTaskExecutionInformationInner object itself.
     */
    public JobPreparationAndReleaseTaskExecutionInformationInner setJobReleaseTaskExecutionInfo(JobReleaseTaskExecutionInformation jobReleaseTaskExecutionInfo) {
        this.jobReleaseTaskExecutionInfo = jobReleaseTaskExecutionInfo;
        return this;
    }

}
