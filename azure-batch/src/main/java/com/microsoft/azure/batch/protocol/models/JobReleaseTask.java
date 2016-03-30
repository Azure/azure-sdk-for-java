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
     * Gets or sets a string that uniquely identifies the Job Release task
     * within the job. The id can contain any combination of alphanumeric
     * characters including hyphens and underscores and cannot contain more
     * than 64 characters.
     */
    private String id;

    /**
     * Gets or sets the command line of the Job Release task.
     */
    private String commandLine;

    /**
     * Gets or sets a list of files that Batch will download to the compute
     * node before running the command line.
     */
    private List<ResourceFile> resourceFiles;

    /**
     * Gets or sets a list of environment variable settings for the Job
     * Release task.
     */
    private List<EnvironmentSetting> environmentSettings;

    /**
     * Gets or sets the maximum elapsed time that the Job Release task may run
     * on a given compute node, measured from the time the task starts. If
     * the task does not complete within the time limit, the Batch service
     * terminates it. The default value is 15 minutes.
     */
    private Period maxWallClockTime;

    /**
     * Gets or sets the minimum time to retain the working directory for the
     * Job Release task on the compute node.  After this time, the Batch
     * service may delete the working directory and all its contents. The
     * default is infinite.
     */
    private Period retentionTime;

    /**
     * Gets or sets whether to run the Job Release task in elevated mode. The
     * default value is false.
     */
    private Boolean runElevated;

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
     * Get the maxWallClockTime value.
     *
     * @return the maxWallClockTime value
     */
    public Period getMaxWallClockTime() {
        return this.maxWallClockTime;
    }

    /**
     * Set the maxWallClockTime value.
     *
     * @param maxWallClockTime the maxWallClockTime value to set
     */
    public void setMaxWallClockTime(Period maxWallClockTime) {
        this.maxWallClockTime = maxWallClockTime;
    }

    /**
     * Get the retentionTime value.
     *
     * @return the retentionTime value
     */
    public Period getRetentionTime() {
        return this.retentionTime;
    }

    /**
     * Set the retentionTime value.
     *
     * @param retentionTime the retentionTime value to set
     */
    public void setRetentionTime(Period retentionTime) {
        this.retentionTime = retentionTime;
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

}
