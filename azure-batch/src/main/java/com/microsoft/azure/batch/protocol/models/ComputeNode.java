/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol.models;

import org.joda.time.DateTime;
import java.util.List;

/**
 * A compute node in the Batch service.
 */
public class ComputeNode {
    /**
     * Gets or sets the id of the compute node.
     */
    private String id;

    /**
     * Gets or sets the URL of the compute node.
     */
    private String url;

    /**
     * Gets or sets the current state of the compute node. Possible values
     * include: 'idle', 'rebooting', 'reimaging', 'running', 'unusable',
     * 'creating', 'starting', 'waitingforstarttask', 'starttaskfailed',
     * 'unknown', 'leavingpool', 'offline'.
     */
    private ComputeNodeState state;

    /**
     * Gets or sets whether the compute node should be available for task
     * scheduling. Possible values include: 'enabled', 'disabled'.
     */
    private SchedulingState schedulingState;

    /**
     * Gets or sets the time at which the compute node entered its current
     * state.
     */
    private DateTime stateTransitionTime;

    /**
     * Gets or sets the time at which the compute node was started.
     */
    private DateTime lastBootTime;

    /**
     * Gets or sets the time at which this compute node was allocated to the
     * pool.
     */
    private DateTime allocationTime;

    /**
     * Gets or sets the IP address that other compute nodes can use to
     * communicate with this compute node.
     */
    private String ipAddress;

    /**
     * Gets or sets an identifier which can be passed in the Add Task API to
     * request that the task be scheduled close to this compute node.
     */
    private String affinityId;

    /**
     * Gets or sets the size of the virtual machine hosting the compute node.
     */
    private String vmSize;

    /**
     * Gets or sets the total number of job tasks completed on the compute
     * node. This includes Job Preparation, Job Release and Job Manager
     * tasks, but not the pool start task.
     */
    private Integer totalTasksRun;

    /**
     * Gets or sets the list of tasks that are currently running on the
     * compute node.
     */
    private List<TaskInformation> recentTasks;

    /**
     * Gets or sets the task specified to run on the compute node as it joins
     * the pool.
     */
    private StartTask startTask;

    /**
     * Gets or sets runtime information about the execution of the start task
     * on the compute node.
     */
    private StartTaskInformation startTaskInfo;

    /**
     * Gets or sets the list of certificates installed on the compute node.
     */
    private List<CertificateReference> certificateReferences;

    /**
     * Gets or sets the list of errors that are currently being encountered by
     * the compute node.
     */
    private List<ComputeNodeError> errors;

    /**
     * Get the id value.
     *
     * @return the id value
     */
    public String getId() {
        return this.id;
    }

    /**
     * Set the id value.
     *
     * @param id the id value to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Get the url value.
     *
     * @return the url value
     */
    public String getUrl() {
        return this.url;
    }

    /**
     * Set the url value.
     *
     * @param url the url value to set
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Get the state value.
     *
     * @return the state value
     */
    public ComputeNodeState getState() {
        return this.state;
    }

    /**
     * Set the state value.
     *
     * @param state the state value to set
     */
    public void setState(ComputeNodeState state) {
        this.state = state;
    }

    /**
     * Get the schedulingState value.
     *
     * @return the schedulingState value
     */
    public SchedulingState getSchedulingState() {
        return this.schedulingState;
    }

    /**
     * Set the schedulingState value.
     *
     * @param schedulingState the schedulingState value to set
     */
    public void setSchedulingState(SchedulingState schedulingState) {
        this.schedulingState = schedulingState;
    }

    /**
     * Get the stateTransitionTime value.
     *
     * @return the stateTransitionTime value
     */
    public DateTime getStateTransitionTime() {
        return this.stateTransitionTime;
    }

    /**
     * Set the stateTransitionTime value.
     *
     * @param stateTransitionTime the stateTransitionTime value to set
     */
    public void setStateTransitionTime(DateTime stateTransitionTime) {
        this.stateTransitionTime = stateTransitionTime;
    }

    /**
     * Get the lastBootTime value.
     *
     * @return the lastBootTime value
     */
    public DateTime getLastBootTime() {
        return this.lastBootTime;
    }

    /**
     * Set the lastBootTime value.
     *
     * @param lastBootTime the lastBootTime value to set
     */
    public void setLastBootTime(DateTime lastBootTime) {
        this.lastBootTime = lastBootTime;
    }

    /**
     * Get the allocationTime value.
     *
     * @return the allocationTime value
     */
    public DateTime getAllocationTime() {
        return this.allocationTime;
    }

    /**
     * Set the allocationTime value.
     *
     * @param allocationTime the allocationTime value to set
     */
    public void setAllocationTime(DateTime allocationTime) {
        this.allocationTime = allocationTime;
    }

    /**
     * Get the ipAddress value.
     *
     * @return the ipAddress value
     */
    public String getIpAddress() {
        return this.ipAddress;
    }

    /**
     * Set the ipAddress value.
     *
     * @param ipAddress the ipAddress value to set
     */
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    /**
     * Get the affinityId value.
     *
     * @return the affinityId value
     */
    public String getAffinityId() {
        return this.affinityId;
    }

    /**
     * Set the affinityId value.
     *
     * @param affinityId the affinityId value to set
     */
    public void setAffinityId(String affinityId) {
        this.affinityId = affinityId;
    }

    /**
     * Get the vmSize value.
     *
     * @return the vmSize value
     */
    public String getVmSize() {
        return this.vmSize;
    }

    /**
     * Set the vmSize value.
     *
     * @param vmSize the vmSize value to set
     */
    public void setVmSize(String vmSize) {
        this.vmSize = vmSize;
    }

    /**
     * Get the totalTasksRun value.
     *
     * @return the totalTasksRun value
     */
    public Integer getTotalTasksRun() {
        return this.totalTasksRun;
    }

    /**
     * Set the totalTasksRun value.
     *
     * @param totalTasksRun the totalTasksRun value to set
     */
    public void setTotalTasksRun(Integer totalTasksRun) {
        this.totalTasksRun = totalTasksRun;
    }

    /**
     * Get the recentTasks value.
     *
     * @return the recentTasks value
     */
    public List<TaskInformation> getRecentTasks() {
        return this.recentTasks;
    }

    /**
     * Set the recentTasks value.
     *
     * @param recentTasks the recentTasks value to set
     */
    public void setRecentTasks(List<TaskInformation> recentTasks) {
        this.recentTasks = recentTasks;
    }

    /**
     * Get the startTask value.
     *
     * @return the startTask value
     */
    public StartTask getStartTask() {
        return this.startTask;
    }

    /**
     * Set the startTask value.
     *
     * @param startTask the startTask value to set
     */
    public void setStartTask(StartTask startTask) {
        this.startTask = startTask;
    }

    /**
     * Get the startTaskInfo value.
     *
     * @return the startTaskInfo value
     */
    public StartTaskInformation getStartTaskInfo() {
        return this.startTaskInfo;
    }

    /**
     * Set the startTaskInfo value.
     *
     * @param startTaskInfo the startTaskInfo value to set
     */
    public void setStartTaskInfo(StartTaskInformation startTaskInfo) {
        this.startTaskInfo = startTaskInfo;
    }

    /**
     * Get the certificateReferences value.
     *
     * @return the certificateReferences value
     */
    public List<CertificateReference> getCertificateReferences() {
        return this.certificateReferences;
    }

    /**
     * Set the certificateReferences value.
     *
     * @param certificateReferences the certificateReferences value to set
     */
    public void setCertificateReferences(List<CertificateReference> certificateReferences) {
        this.certificateReferences = certificateReferences;
    }

    /**
     * Get the errors value.
     *
     * @return the errors value
     */
    public List<ComputeNodeError> getErrors() {
        return this.errors;
    }

    /**
     * Set the errors value.
     *
     * @param errors the errors value to set
     */
    public void setErrors(List<ComputeNodeError> errors) {
        this.errors = errors;
    }

}
