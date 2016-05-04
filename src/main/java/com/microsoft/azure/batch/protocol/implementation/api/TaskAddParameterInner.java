/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol.implementation.api;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * An Azure Batch task to add.
 */
public class TaskAddParameterInner {
    /**
     * Gets or sets a string that uniquely identifies the task within the job.
     * The id can contain any combination of alphanumeric characters
     * including hyphens and underscores, and cannot contain more than 64
     * characters.
     */
    @JsonProperty(required = true)
    private String id;

    /**
     * Gets or sets a display name for the task.
     */
    private String displayName;

    /**
     * Gets or sets the command line of the task. For multi-instance tasks,
     * the command line is executed on the primary subtask after all the
     * subtasks have finished executing the coordianation command line.
     */
    @JsonProperty(required = true)
    private String commandLine;

    /**
     * Gets or sets a list of files that Batch will download to the compute
     * node before running the command line. For multi-instance tasks, the
     * resource files will only be downloaded to the compute node on which
     * the primary subtask is executed.
     */
    private List<ResourceFile> resourceFiles;

    /**
     * Gets or sets a list of environment variable settings for the task.
     */
    private List<EnvironmentSetting> environmentSettings;

    /**
     * Gets or sets a locality hint that can be used by the Batch service to
     * select a compute node on which to start the new task.
     */
    private AffinityInformation affinityInfo;

    /**
     * Gets or sets the execution constraints that apply to this task.
     */
    private TaskConstraintsInner constraints;

    /**
     * Gets or sets whether to run the task in elevated mode.
     */
    private Boolean runElevated;

    /**
     * Gets or sets information about how to run the multi-instance task.
     */
    private MultiInstanceSettings multiInstanceSettings;

    /**
     * Gets or sets any dependencies this task has.
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
     * @return the TaskAddParameterInner object itself.
     */
    public TaskAddParameterInner setId(String id) {
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
     * @return the TaskAddParameterInner object itself.
     */
    public TaskAddParameterInner setDisplayName(String displayName) {
        this.displayName = displayName;
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
     * @return the TaskAddParameterInner object itself.
     */
    public TaskAddParameterInner setCommandLine(String commandLine) {
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
     * @return the TaskAddParameterInner object itself.
     */
    public TaskAddParameterInner setResourceFiles(List<ResourceFile> resourceFiles) {
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
     * @return the TaskAddParameterInner object itself.
     */
    public TaskAddParameterInner setEnvironmentSettings(List<EnvironmentSetting> environmentSettings) {
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
     * @return the TaskAddParameterInner object itself.
     */
    public TaskAddParameterInner setAffinityInfo(AffinityInformation affinityInfo) {
        this.affinityInfo = affinityInfo;
        return this;
    }

    /**
     * Get the constraints value.
     *
     * @return the constraints value
     */
    public TaskConstraintsInner constraints() {
        return this.constraints;
    }

    /**
     * Set the constraints value.
     *
     * @param constraints the constraints value to set
     * @return the TaskAddParameterInner object itself.
     */
    public TaskAddParameterInner setConstraints(TaskConstraintsInner constraints) {
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
     * @return the TaskAddParameterInner object itself.
     */
    public TaskAddParameterInner setRunElevated(Boolean runElevated) {
        this.runElevated = runElevated;
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
     * @return the TaskAddParameterInner object itself.
     */
    public TaskAddParameterInner setMultiInstanceSettings(MultiInstanceSettings multiInstanceSettings) {
        this.multiInstanceSettings = multiInstanceSettings;
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
     * @return the TaskAddParameterInner object itself.
     */
    public TaskAddParameterInner setDependsOn(TaskDependencies dependsOn) {
        this.dependsOn = dependsOn;
        return this;
    }

}
