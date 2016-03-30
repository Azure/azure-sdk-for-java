/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol.models;

import java.util.List;

/**
 * A Job Preparation task to run before any tasks of the job on any given
 * compute node.
 */
public class JobPreparationTask {
    /**
     * Gets or sets a string that uniquely identifies the job preparation task
     * within the job. The id can contain any combination of alphanumeric
     * characters including hyphens and underscores and cannot contain more
     * than 64 characters.
     */
    private String id;

    /**
     * Gets or sets the command line of the Job Preparation task.
     */
    private String commandLine;

    /**
     * Gets or sets a list of files that Batch will download to the compute
     * node before running the command line.
     */
    private List<ResourceFile> resourceFiles;

    /**
     * Gets or sets a list of environment variable settings for the Job
     * Preparation task.
     */
    private List<EnvironmentSetting> environmentSettings;

    /**
     * Gets or sets constraints that apply to the Job Preparation task..
     */
    private TaskConstraints constraints;

    /**
     * Gets or sets whether the Batch Service should wait for the Job
     * Preparation task to complete successfully before scheduling any other
     * tasks of the job on the compute node.
     */
    private Boolean waitForSuccess;

    /**
     * Gets or sets whether to run the Job Preparation task in elevated mode.
     * The default value is false.
     */
    private Boolean runElevated;

    /**
     * Gets or sets whether the Batch service should rerun the Job Preparation
     * task after a compute node reboots. The default value is true.
     */
    private Boolean rerunOnNodeRebootAfterSuccess;

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
     * Get the commandLine value.
     *
     * @return the commandLine value
     */
    public String getCommandLine() {
        return this.commandLine;
    }

    /**
     * Set the commandLine value.
     *
     * @param commandLine the commandLine value to set
     */
    public void setCommandLine(String commandLine) {
        this.commandLine = commandLine;
    }

    /**
     * Get the resourceFiles value.
     *
     * @return the resourceFiles value
     */
    public List<ResourceFile> getResourceFiles() {
        return this.resourceFiles;
    }

    /**
     * Set the resourceFiles value.
     *
     * @param resourceFiles the resourceFiles value to set
     */
    public void setResourceFiles(List<ResourceFile> resourceFiles) {
        this.resourceFiles = resourceFiles;
    }

    /**
     * Get the environmentSettings value.
     *
     * @return the environmentSettings value
     */
    public List<EnvironmentSetting> getEnvironmentSettings() {
        return this.environmentSettings;
    }

    /**
     * Set the environmentSettings value.
     *
     * @param environmentSettings the environmentSettings value to set
     */
    public void setEnvironmentSettings(List<EnvironmentSetting> environmentSettings) {
        this.environmentSettings = environmentSettings;
    }

    /**
     * Get the constraints value.
     *
     * @return the constraints value
     */
    public TaskConstraints getConstraints() {
        return this.constraints;
    }

    /**
     * Set the constraints value.
     *
     * @param constraints the constraints value to set
     */
    public void setConstraints(TaskConstraints constraints) {
        this.constraints = constraints;
    }

    /**
     * Get the waitForSuccess value.
     *
     * @return the waitForSuccess value
     */
    public Boolean getWaitForSuccess() {
        return this.waitForSuccess;
    }

    /**
     * Set the waitForSuccess value.
     *
     * @param waitForSuccess the waitForSuccess value to set
     */
    public void setWaitForSuccess(Boolean waitForSuccess) {
        this.waitForSuccess = waitForSuccess;
    }

    /**
     * Get the runElevated value.
     *
     * @return the runElevated value
     */
    public Boolean getRunElevated() {
        return this.runElevated;
    }

    /**
     * Set the runElevated value.
     *
     * @param runElevated the runElevated value to set
     */
    public void setRunElevated(Boolean runElevated) {
        this.runElevated = runElevated;
    }

    /**
     * Get the rerunOnNodeRebootAfterSuccess value.
     *
     * @return the rerunOnNodeRebootAfterSuccess value
     */
    public Boolean getRerunOnNodeRebootAfterSuccess() {
        return this.rerunOnNodeRebootAfterSuccess;
    }

    /**
     * Set the rerunOnNodeRebootAfterSuccess value.
     *
     * @param rerunOnNodeRebootAfterSuccess the rerunOnNodeRebootAfterSuccess value to set
     */
    public void setRerunOnNodeRebootAfterSuccess(Boolean rerunOnNodeRebootAfterSuccess) {
        this.rerunOnNodeRebootAfterSuccess = rerunOnNodeRebootAfterSuccess;
    }

}
