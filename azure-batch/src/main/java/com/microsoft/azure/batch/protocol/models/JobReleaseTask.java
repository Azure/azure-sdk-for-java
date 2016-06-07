/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol.models;

import java.util.List;
import org.joda.time.Period;

/**
 * A Job Release task to run on job completion on any compute node where the
 * job has run.
 */
public class JobReleaseTask {
    /**
     * A string that uniquely identifies the Job Release task within the job.
     * The id can contain any combination of alphanumeric characters
     * including hyphens and underscores and cannot contain more than 64
     * characters.
     */
    private String id;

    /**
     * The command line of the Job Release task.
     */
    private String commandLine;

    /**
     * A list of files that the Batch service will download to the compute
     * node before running the command line.
     */
    private List<ResourceFile> resourceFiles;

    /**
     * A list of environment variable settings for the Job Release task.
     */
    private List<EnvironmentSetting> environmentSettings;

    /**
     * The maximum elapsed time that the Job Release task may run on a given
     * compute node, measured from the time the task starts. If the task does
     * not complete within the time limit, the Batch service terminates it.
     * The default value is 15 minutes.
     */
    private Period maxWallClockTime;

    /**
     * The minimum time to retain the working directory for the Job Release
     * task on the compute node. After this time, the Batch service may
     * delete the working directory and all its contents. The default is
     * infinite.
     */
    private Period retentionTime;

    /**
     * Whether to run the Job Release task in elevated mode. The default value
     * is false.
     */
    private Boolean runElevated;

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
     * @return the JobReleaseTask object itself.
     */
    public JobReleaseTask withId(String id) {
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
     * @return the JobReleaseTask object itself.
     */
    public JobReleaseTask withCommandLine(String commandLine) {
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
     * @return the JobReleaseTask object itself.
     */
    public JobReleaseTask withResourceFiles(List<ResourceFile> resourceFiles) {
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
     * @return the JobReleaseTask object itself.
     */
    public JobReleaseTask withEnvironmentSettings(List<EnvironmentSetting> environmentSettings) {
        this.environmentSettings = environmentSettings;
        return this;
    }

    /**
     * Get the maxWallClockTime value.
     *
     * @return the maxWallClockTime value
     */
    public Period maxWallClockTime() {
        return this.maxWallClockTime;
    }

    /**
     * Set the maxWallClockTime value.
     *
     * @param maxWallClockTime the maxWallClockTime value to set
     * @return the JobReleaseTask object itself.
     */
    public JobReleaseTask withMaxWallClockTime(Period maxWallClockTime) {
        this.maxWallClockTime = maxWallClockTime;
        return this;
    }

    /**
     * Get the retentionTime value.
     *
     * @return the retentionTime value
     */
    public Period retentionTime() {
        return this.retentionTime;
    }

    /**
     * Set the retentionTime value.
     *
     * @param retentionTime the retentionTime value to set
     * @return the JobReleaseTask object itself.
     */
    public JobReleaseTask withRetentionTime(Period retentionTime) {
        this.retentionTime = retentionTime;
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
     * @return the JobReleaseTask object itself.
     */
    public JobReleaseTask withRunElevated(Boolean runElevated) {
        this.runElevated = runElevated;
        return this;
    }

}
