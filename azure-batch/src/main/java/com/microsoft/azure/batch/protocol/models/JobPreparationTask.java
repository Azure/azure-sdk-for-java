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
     * A string that uniquely identifies the job preparation task within the
     * job. The id can contain any combination of alphanumeric characters
     * including hyphens and underscores and cannot contain more than 64
     * characters.
     */
    private String id;

    /**
     * The command line of the Job Preparation task.
     */
    private String commandLine;

    /**
     * A list of files that the Batch service will download to the compute
     * node before running the command line.
     */
    private List<ResourceFile> resourceFiles;

    /**
     * A list of environment variable settings for the Job Preparation task.
     */
    private List<EnvironmentSetting> environmentSettings;

    /**
     * Constraints that apply to the Job Preparation task.
     */
    private TaskConstraints constraints;

    /**
     * Whether the Batch service should wait for the Job Preparation task to
     * complete successfully before scheduling any other tasks of the job on
     * the compute node.
     */
    private Boolean waitForSuccess;

    /**
     * Whether to run the Job Preparation task in elevated mode. The default
     * value is false.
     */
    private Boolean runElevated;

    /**
     * Whether the Batch service should rerun the Job Preparation task after a
     * compute node reboots. Note that the Job Preparation task should still
     * be written to be idempotent because it can be rerun if the compute
     * node is rebooted while Job Preparation task is still running. The
     * default value is true.
     */
    private Boolean rerunOnNodeRebootAfterSuccess;

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
     * @return the JobPreparationTask object itself.
     */
    public JobPreparationTask withId(String id) {
        this.id = id;
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
     * @return the JobPreparationTask object itself.
     */
    public JobPreparationTask withCommandLine(String commandLine) {
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
     * @return the JobPreparationTask object itself.
     */
    public JobPreparationTask withResourceFiles(List<ResourceFile> resourceFiles) {
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
     * @return the JobPreparationTask object itself.
     */
    public JobPreparationTask withEnvironmentSettings(List<EnvironmentSetting> environmentSettings) {
        this.environmentSettings = environmentSettings;
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
     * @return the JobPreparationTask object itself.
     */
    public JobPreparationTask withConstraints(TaskConstraints constraints) {
        this.constraints = constraints;
        return this;
    }

    /**
     * Get the waitForSuccess value.
     *
     * @return the waitForSuccess value
     */
    public Boolean waitForSuccess() {
        return this.waitForSuccess;
    }

    /**
     * Set the waitForSuccess value.
     *
     * @param waitForSuccess the waitForSuccess value to set
     * @return the JobPreparationTask object itself.
     */
    public JobPreparationTask withWaitForSuccess(Boolean waitForSuccess) {
        this.waitForSuccess = waitForSuccess;
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
     * @return the JobPreparationTask object itself.
     */
    public JobPreparationTask withRunElevated(Boolean runElevated) {
        this.runElevated = runElevated;
        return this;
    }

    /**
     * Get the rerunOnNodeRebootAfterSuccess value.
     *
     * @return the rerunOnNodeRebootAfterSuccess value
     */
    public Boolean rerunOnNodeRebootAfterSuccess() {
        return this.rerunOnNodeRebootAfterSuccess;
    }

    /**
     * Set the rerunOnNodeRebootAfterSuccess value.
     *
     * @param rerunOnNodeRebootAfterSuccess the rerunOnNodeRebootAfterSuccess value to set
     * @return the JobPreparationTask object itself.
     */
    public JobPreparationTask withRerunOnNodeRebootAfterSuccess(Boolean rerunOnNodeRebootAfterSuccess) {
        this.rerunOnNodeRebootAfterSuccess = rerunOnNodeRebootAfterSuccess;
        return this;
    }

}
