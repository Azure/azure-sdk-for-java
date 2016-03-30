/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol.models;

import java.util.List;

/**
 * A task defined on a pool and run by compute nodes when they join the pool.
 */
public class StartTask {
    /**
     * Gets or sets the command line of the start task.
     */
    private String commandLine;

    /**
     * Gets or sets a list of files that Batch will download to the compute
     * node before running the command line.
     */
    private List<ResourceFile> resourceFiles;

    /**
     * Gets or sets a list of environment variable settings for the start task.
     */
    private List<EnvironmentSetting> environmentSettings;

    /**
     * Gets or sets whether to run the start task in elevated mode. The
     * default value is false.
     */
    private Boolean runElevated;

    /**
     * Gets or sets the maximum number of times the task may be retried.
     */
    private Integer maxTaskRetryCount;

    /**
     * Gets or sets whether the Batch Service should wait for the start task
     * to complete successfully (that is, to exit with exit code 0) before
     * scheduling any tasks on the compute node.
     */
    private Boolean waitForSuccess;

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
     * Get the maxTaskRetryCount value.
     *
     * @return the maxTaskRetryCount value
     */
    public Integer getMaxTaskRetryCount() {
        return this.maxTaskRetryCount;
    }

    /**
     * Set the maxTaskRetryCount value.
     *
     * @param maxTaskRetryCount the maxTaskRetryCount value to set
     */
    public void setMaxTaskRetryCount(Integer maxTaskRetryCount) {
        this.maxTaskRetryCount = maxTaskRetryCount;
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

}
