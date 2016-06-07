/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol.models;

import org.joda.time.DateTime;
import java.util.List;

/**
 * An Azure Batch task.
 */
public class CloudTask {
    /**
     * A string that uniquely identifies the task within the job. The id can
     * contain any combination of alphanumeric characters including hyphens
     * and underscores, and cannot contain more than 64 characters.
     */
    private String id;

    /**
     * A display name for the task.
     */
    private String displayName;

    /**
     * The URL of the task.
     */
    private String url;

    /**
     * The ETag of the task.
     */
    private String eTag;

    /**
     * The last modified time of the task.
     */
    private DateTime lastModified;

    /**
     * The creation time of the task.
     */
    private DateTime creationTime;

    /**
     * The current state of the task. Possible values include: 'active',
     * 'preparing', 'running', 'completed'.
     */
    private TaskState state;

    /**
     * The time at which the task entered its current state.
     */
    private DateTime stateTransitionTime;

    /**
     * The previous state of the task. This property is not set if the task is
     * in its initial Active state. Possible values include: 'active',
     * 'preparing', 'running', 'completed'.
     */
    private TaskState previousState;

    /**
     * The time at which the task entered its previous state. This property is
     * not set if the task is in its initial Active state.
     */
    private DateTime previousStateTransitionTime;

    /**
     * The command line of the task. For multi-instance tasks, the command
     * line is executed on the primary subtask after all the subtasks have
     * finished executing the coordianation command line.
     */
    private String commandLine;

    /**
     * A list of files that the Batch service will download to the compute
     * node before running the command line. For multi-instance tasks, the
     * resource files will only be downloaded to the compute node on which
     * the primary subtask is executed.
     */
    private List<ResourceFile> resourceFiles;

    /**
     * A list of environment variable settings for the task.
     */
    private List<EnvironmentSetting> environmentSettings;

    /**
     * A locality hint that can be used by the Batch service to select a
     * compute node on which to start the new task.
     */
    private AffinityInformation affinityInfo;

    /**
     * The execution constraints that apply to this task.
     */
    private TaskConstraints constraints;

    /**
     * Whether to run the task in elevated mode.
     */
    private Boolean runElevated;

    /**
     * Information about the execution of the task.
     */
    private TaskExecutionInformation executionInfo;

    /**
     * Information about the compute node on which the task ran.
     */
    private ComputeNodeInformation nodeInfo;

    /**
     * Information about how to run the multi-instance task.
     */
    private MultiInstanceSettings multiInstanceSettings;

    /**
     * Resource usage statistics for the task.
     */
    private TaskStatistics stats;

    /**
     * Any dependencies this task has.
     */
    private TaskDependencies dependsOn;

    /**
     * Get the id value.
     *
     * @return the id value
     */
    public String id() {
        return this.id;
    }

    /**
     * Set the id value.
     *
     * @param id the id value to set
     * @return the CloudTask object itself.
     */
    public CloudTask withId(String id) {
        this.id = id;
        return this;
    }

    /**
     * Get the displayName value.
     *
     * @return the displayName value
     */
    public String displayName() {
        return this.displayName;
    }

    /**
     * Set the displayName value.
     *
     * @param displayName the displayName value to set
     * @return the CloudTask object itself.
     */
    public CloudTask withDisplayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    /**
     * Get the url value.
     *
     * @return the url value
     */
    public String url() {
        return this.url;
    }

    /**
     * Set the url value.
     *
     * @param url the url value to set
     * @return the CloudTask object itself.
     */
    public CloudTask withUrl(String url) {
        this.url = url;
        return this;
    }

    /**
     * Get the eTag value.
     *
     * @return the eTag value
     */
    public String eTag() {
        return this.eTag;
    }

    /**
     * Set the eTag value.
     *
     * @param eTag the eTag value to set
     * @return the CloudTask object itself.
     */
    public CloudTask withETag(String eTag) {
        this.eTag = eTag;
        return this;
    }

    /**
     * Get the lastModified value.
     *
     * @return the lastModified value
     */
    public DateTime lastModified() {
        return this.lastModified;
    }

    /**
     * Set the lastModified value.
     *
     * @param lastModified the lastModified value to set
     * @return the CloudTask object itself.
     */
    public CloudTask withLastModified(DateTime lastModified) {
        this.lastModified = lastModified;
        return this;
    }

    /**
     * Get the creationTime value.
     *
     * @return the creationTime value
     */
    public DateTime creationTime() {
        return this.creationTime;
    }

    /**
     * Set the creationTime value.
     *
     * @param creationTime the creationTime value to set
     * @return the CloudTask object itself.
     */
    public CloudTask withCreationTime(DateTime creationTime) {
        this.creationTime = creationTime;
        return this;
    }

    /**
     * Get the state value.
     *
     * @return the state value
     */
    public TaskState state() {
        return this.state;
    }

    /**
     * Set the state value.
     *
     * @param state the state value to set
     * @return the CloudTask object itself.
     */
    public CloudTask withState(TaskState state) {
        this.state = state;
        return this;
    }

    /**
     * Get the stateTransitionTime value.
     *
     * @return the stateTransitionTime value
     */
    public DateTime stateTransitionTime() {
        return this.stateTransitionTime;
    }

    /**
     * Set the stateTransitionTime value.
     *
     * @param stateTransitionTime the stateTransitionTime value to set
     * @return the CloudTask object itself.
     */
    public CloudTask withStateTransitionTime(DateTime stateTransitionTime) {
        this.stateTransitionTime = stateTransitionTime;
        return this;
    }

    /**
     * Get the previousState value.
     *
     * @return the previousState value
     */
    public TaskState previousState() {
        return this.previousState;
    }

    /**
     * Set the previousState value.
     *
     * @param previousState the previousState value to set
     * @return the CloudTask object itself.
     */
    public CloudTask withPreviousState(TaskState previousState) {
        this.previousState = previousState;
        return this;
    }

    /**
     * Get the previousStateTransitionTime value.
     *
     * @return the previousStateTransitionTime value
     */
    public DateTime previousStateTransitionTime() {
        return this.previousStateTransitionTime;
    }

    /**
     * Set the previousStateTransitionTime value.
     *
     * @param previousStateTransitionTime the previousStateTransitionTime value to set
     * @return the CloudTask object itself.
     */
    public CloudTask withPreviousStateTransitionTime(DateTime previousStateTransitionTime) {
        this.previousStateTransitionTime = previousStateTransitionTime;
        return this;
    }

    /**
     * Get the commandLine value.
     *
     * @return the commandLine value
     */
    public String commandLine() {
        return this.commandLine;
    }

    /**
     * Set the commandLine value.
     *
     * @param commandLine the commandLine value to set
     * @return the CloudTask object itself.
     */
    public CloudTask withCommandLine(String commandLine) {
        this.commandLine = commandLine;
        return this;
    }

    /**
     * Get the resourceFiles value.
     *
     * @return the resourceFiles value
     */
    public List<ResourceFile> resourceFiles() {
        return this.resourceFiles;
    }

    /**
     * Set the resourceFiles value.
     *
     * @param resourceFiles the resourceFiles value to set
     * @return the CloudTask object itself.
     */
    public CloudTask withResourceFiles(List<ResourceFile> resourceFiles) {
        this.resourceFiles = resourceFiles;
        return this;
    }

    /**
     * Get the environmentSettings value.
     *
     * @return the environmentSettings value
     */
    public List<EnvironmentSetting> environmentSettings() {
        return this.environmentSettings;
    }

    /**
     * Set the environmentSettings value.
     *
     * @param environmentSettings the environmentSettings value to set
     * @return the CloudTask object itself.
     */
    public CloudTask withEnvironmentSettings(List<EnvironmentSetting> environmentSettings) {
        this.environmentSettings = environmentSettings;
        return this;
    }

    /**
     * Get the affinityInfo value.
     *
     * @return the affinityInfo value
     */
    public AffinityInformation affinityInfo() {
        return this.affinityInfo;
    }

    /**
     * Set the affinityInfo value.
     *
     * @param affinityInfo the affinityInfo value to set
     * @return the CloudTask object itself.
     */
    public CloudTask withAffinityInfo(AffinityInformation affinityInfo) {
        this.affinityInfo = affinityInfo;
        return this;
    }

    /**
     * Get the constraints value.
     *
     * @return the constraints value
     */
    public TaskConstraints constraints() {
        return this.constraints;
    }

    /**
     * Set the constraints value.
     *
     * @param constraints the constraints value to set
     * @return the CloudTask object itself.
     */
    public CloudTask withConstraints(TaskConstraints constraints) {
        this.constraints = constraints;
        return this;
    }

    /**
     * Get the runElevated value.
     *
     * @return the runElevated value
     */
    public Boolean runElevated() {
        return this.runElevated;
    }

    /**
     * Set the runElevated value.
     *
     * @param runElevated the runElevated value to set
     * @return the CloudTask object itself.
     */
    public CloudTask withRunElevated(Boolean runElevated) {
        this.runElevated = runElevated;
        return this;
    }

    /**
     * Get the executionInfo value.
     *
     * @return the executionInfo value
     */
    public TaskExecutionInformation executionInfo() {
        return this.executionInfo;
    }

    /**
     * Set the executionInfo value.
     *
     * @param executionInfo the executionInfo value to set
     * @return the CloudTask object itself.
     */
    public CloudTask withExecutionInfo(TaskExecutionInformation executionInfo) {
        this.executionInfo = executionInfo;
        return this;
    }

    /**
     * Get the nodeInfo value.
     *
     * @return the nodeInfo value
     */
    public ComputeNodeInformation nodeInfo() {
        return this.nodeInfo;
    }

    /**
     * Set the nodeInfo value.
     *
     * @param nodeInfo the nodeInfo value to set
     * @return the CloudTask object itself.
     */
    public CloudTask withNodeInfo(ComputeNodeInformation nodeInfo) {
        this.nodeInfo = nodeInfo;
        return this;
    }

    /**
     * Get the multiInstanceSettings value.
     *
     * @return the multiInstanceSettings value
     */
    public MultiInstanceSettings multiInstanceSettings() {
        return this.multiInstanceSettings;
    }

    /**
     * Set the multiInstanceSettings value.
     *
     * @param multiInstanceSettings the multiInstanceSettings value to set
     * @return the CloudTask object itself.
     */
    public CloudTask withMultiInstanceSettings(MultiInstanceSettings multiInstanceSettings) {
        this.multiInstanceSettings = multiInstanceSettings;
        return this;
    }

    /**
     * Get the stats value.
     *
     * @return the stats value
     */
    public TaskStatistics stats() {
        return this.stats;
    }

    /**
     * Set the stats value.
     *
     * @param stats the stats value to set
     * @return the CloudTask object itself.
     */
    public CloudTask withStats(TaskStatistics stats) {
        this.stats = stats;
        return this;
    }

    /**
     * Get the dependsOn value.
     *
     * @return the dependsOn value
     */
    public TaskDependencies dependsOn() {
        return this.dependsOn;
    }

    /**
     * Set the dependsOn value.
     *
     * @param dependsOn the dependsOn value to set
     * @return the CloudTask object itself.
     */
    public CloudTask withDependsOn(TaskDependencies dependsOn) {
        this.dependsOn = dependsOn;
        return this;
    }

}
