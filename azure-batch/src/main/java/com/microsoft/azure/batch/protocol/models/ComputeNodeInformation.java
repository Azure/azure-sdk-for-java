/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol.models;


/**
 * Information about the compute node on which a task ran.
 */
public class ComputeNodeInformation {
    /**
     * An identifier for the compute node on which the task ran, which can be
     * passed when adding a task to request that the task be scheduled close
     * to this compute node.
     */
    private String affinityId;

    /**
     * The URL of the node on which the task ran.
     */
    private String nodeUrl;

    /**
     * The id of the pool on which the task ran.
     */
    private String poolId;

    /**
     * The id of the node on which the task ran.
     */
    private String nodeId;

    /**
     * The root directory of the task on the compute node.
     */
    private String taskRootDirectory;

    /**
     * The URL to the root directory of the task on the compute node.
     */
    private String taskRootDirectoryUrl;

    /**
     * Get the affinityId value.
     *
     * @return the affinityId value
     */
    public String affinityId() {
        return this.affinityId;
    }

    /**
     * Set the affinityId value.
     *
     * @param affinityId the affinityId value to set
     * @return the ComputeNodeInformation object itself.
     */
    public ComputeNodeInformation withAffinityId(String affinityId) {
        this.affinityId = affinityId;
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
     * @return the ComputeNodeInformation object itself.
     */
    public ComputeNodeInformation withNodeUrl(String nodeUrl) {
        this.nodeUrl = nodeUrl;
        return this;
    }

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
     * @return the ComputeNodeInformation object itself.
     */
    public ComputeNodeInformation withPoolId(String poolId) {
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
     * @return the ComputeNodeInformation object itself.
     */
    public ComputeNodeInformation withNodeId(String nodeId) {
        this.nodeId = nodeId;
        return this;
    }

    /**
     * Get the taskRootDirectory value.
     *
     * @return the taskRootDirectory value
     */
    public String taskRootDirectory() {
        return this.taskRootDirectory;
    }

    /**
     * Set the taskRootDirectory value.
     *
     * @param taskRootDirectory the taskRootDirectory value to set
     * @return the ComputeNodeInformation object itself.
     */
    public ComputeNodeInformation withTaskRootDirectory(String taskRootDirectory) {
        this.taskRootDirectory = taskRootDirectory;
        return this;
    }

    /**
     * Get the taskRootDirectoryUrl value.
     *
     * @return the taskRootDirectoryUrl value
     */
    public String taskRootDirectoryUrl() {
        return this.taskRootDirectoryUrl;
    }

    /**
     * Set the taskRootDirectoryUrl value.
     *
     * @param taskRootDirectoryUrl the taskRootDirectoryUrl value to set
     * @return the ComputeNodeInformation object itself.
     */
    public ComputeNodeInformation withTaskRootDirectoryUrl(String taskRootDirectoryUrl) {
        this.taskRootDirectoryUrl = taskRootDirectoryUrl;
        return this;
    }

}
