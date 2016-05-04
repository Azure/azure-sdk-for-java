/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol.implementation.api;

import java.util.List;

/**
 * Specifies details of a Job Manager task.
 */
public class JobManagerTask {
    /**
     * Gets or sets a string that uniquely identifies the Job Manager task. A
     * GUID is recommended.
     */
    private String id;

    /**
     * Gets or sets the display name of the Job Manager task.
     */
    private String displayName;

    /**
     * Gets or sets the command line of the Job Manager task.
     */
    private String commandLine;

    /**
     * Gets or sets a list of files that Batch will download to the compute
     * node before running the command line.
     */
    private List<ResourceFile> resourceFiles;

    /**
     * Gets or sets a list of environment variable settings for the Job
     * Manager task.
     */
    private List<EnvironmentSetting> environmentSettings;

    /**
     * Gets or sets constraints that apply to the Job Manager task.
     */
    private TaskConstraintsInner constraints;

    /**
     * Gets or sets whether completion of the Job Manager task signifies
     * completion of the entire job.
     */
    private Boolean killJobOnCompletion;

    /**
     * Gets or sets whether to run the Job Manager task in elevated mode. The
     * default value is false.
     */
    private Boolean runElevated;

    /**
     * Gets or sets whether the Job Manager task requires exclusive use of the
     * compute node where it runs.
     */
    private Boolean runExclusive;

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
     * @return the JobManagerTask object itself.
     */
    public JobManagerTask setId(String id) {
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
     * @return the JobManagerTask object itself.
     */
    public JobManagerTask setDisplayName(String displayName) {
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
     * @return the JobManagerTask object itself.
     */
    public JobManagerTask setCommandLine(String commandLine) {
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
     * @return the JobManagerTask object itself.
     */
    public JobManagerTask setResourceFiles(List<ResourceFile> resourceFiles) {
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
     * @return the JobManagerTask object itself.
     */
    public JobManagerTask setEnvironmentSettings(List<EnvironmentSetting> environmentSettings) {
        this.environmentSettings = environmentSettings;
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
     * @return the JobManagerTask object itself.
     */
    public JobManagerTask setConstraints(TaskConstraintsInner constraints) {
        this.constraints = constraints;
        return this;
    }

    /**
     * Get the killJobOnCompletion value.
     *
     * @return the killJobOnCompletion value
     */
    public Boolean killJobOnCompletion() {
        return this.killJobOnCompletion;
    }

    /**
     * Set the killJobOnCompletion value.
     *
     * @param killJobOnCompletion the killJobOnCompletion value to set
     * @return the JobManagerTask object itself.
     */
    public JobManagerTask setKillJobOnCompletion(Boolean killJobOnCompletion) {
        this.killJobOnCompletion = killJobOnCompletion;
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
     * @return the JobManagerTask object itself.
     */
    public JobManagerTask setRunElevated(Boolean runElevated) {
        this.runElevated = runElevated;
        return this;
    }

    /**
     * Get the runExclusive value.
     *
     * @return the runExclusive value
     */
    public Boolean runExclusive() {
        return this.runExclusive;
    }

    /**
     * Set the runExclusive value.
     *
     * @param runExclusive the runExclusive value to set
     * @return the JobManagerTask object itself.
     */
    public JobManagerTask setRunExclusive(Boolean runExclusive) {
        this.runExclusive = runExclusive;
        return this;
    }

}
