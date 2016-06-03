/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol.models;

import java.util.List;

/**
 * Specifies details of a Job Manager task.
 */
public class JobManagerTask {
    /**
     * A string that uniquely identifies the Job Manager task. A GUID is
     * recommended.
     */
    private String id;

    /**
     * The display name of the Job Manager task.
     */
    private String displayName;

    /**
     * The command line of the Job Manager task.
     */
    private String commandLine;

    /**
     * A list of files that the Batch service will download to the compute
     * node before running the command line.
     */
    private List<ResourceFile> resourceFiles;

    /**
     * A list of environment variable settings for the Job Manager task.
     */
    private List<EnvironmentSetting> environmentSettings;

    /**
     * Constraints that apply to the Job Manager task.
     */
    private TaskConstraints constraints;

    /**
     * Whether completion of the Job Manager task signifies completion of the
     * entire job.
     */
    private Boolean killJobOnCompletion;

    /**
     * Whether to run the Job Manager task in elevated mode. The default value
     * is false.
     */
    private Boolean runElevated;

    /**
     * Whether the Job Manager task requires exclusive use of the compute node
     * where it runs. If true, no other tasks will run on the same compute
     * node for as long as the Job Manager is running. If false, other tasks
     * can run simultaneously with the Job Manager on a compute node. (The
     * Job Manager task counts normally against the node's concurrent task
     * limit, so this is only relevant if the node allows multiple concurrent
     * tasks.).
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
    public JobManagerTask withId(String id) {
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
    public JobManagerTask withDisplayName(String displayName) {
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
    public JobManagerTask withCommandLine(String commandLine) {
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
    public JobManagerTask withResourceFiles(List<ResourceFile> resourceFiles) {
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
    public JobManagerTask withEnvironmentSettings(List<EnvironmentSetting> environmentSettings) {
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
     * @return the JobManagerTask object itself.
     */
    public JobManagerTask withConstraints(TaskConstraints constraints) {
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
    public JobManagerTask withKillJobOnCompletion(Boolean killJobOnCompletion) {
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
    public JobManagerTask withRunElevated(Boolean runElevated) {
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
    public JobManagerTask withRunExclusive(Boolean runExclusive) {
        this.runExclusive = runExclusive;
        return this;
    }

}
