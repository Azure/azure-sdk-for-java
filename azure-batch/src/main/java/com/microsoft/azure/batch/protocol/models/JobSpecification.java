/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol.models;

import java.util.List;

/**
 * Specifies details of the jobs to be created on a schedule.
 */
public class JobSpecification {
    /**
     * Gets or sets the priority of jobs created under this schedule. Priority
     * values can range from -1000 to 1000, with -1000 being the lowest
     * priority and 1000 being the highest priority. The default value is 0.
     */
    private Integer priority;

    /**
     * Gets or sets the display name for jobs created under this schedule. It
     * need not be unique and can contain any Unicode characters up to a
     * maximum length of 1024.
     */
    private String displayName;

    /**
     * Gets or sets the flag that determines if this job will use tasks with
     * dependencies.
     */
    private Boolean usesTaskDependencies;

    /**
     * Gets or sets the execution constraints for jobs created under this
     * schedule.
     */
    private JobConstraints constraints;

    /**
     * Gets or sets the details of a Job Manager task to be launched when a
     * job is started under this schedule.
     */
    private JobManagerTask jobManagerTask;

    /**
     * Gets or sets the Job Preparation task for jobs created under this
     * schedule.
     */
    private JobPreparationTask jobPreparationTask;

    /**
     * Gets or sets the Job Release task for jobs created under this schedule.
     */
    private JobReleaseTask jobReleaseTask;

    /**
     * Gets or sets a list of common environment variable settings.  These
     * environment variables are set for all tasks in jobs created under this
     * schedule (including the Job Manager, Job Preparation and Job Release
     * tasks).
     */
    private List<EnvironmentSetting> commonEnvironmentSettings;

    /**
     * Gets or sets the pool on which the Batch service runs the tasks of jobs
     * created under this schedule.
     */
    private PoolInformation poolInfo;

    /**
     * Gets or sets a list of name-value pairs associated with each job
     * created under this schedule as metadata.
     */
    private List<MetadataItem> metadata;

    /**
     * Get the priority value.
     *
     * @return the priority value
     */
    public Integer getPriority() {
        return this.priority;
    }

    /**
     * Set the priority value.
     *
     * @param priority the priority value to set
     */
    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    /**
     * Get the displayName value.
     *
     * @return the displayName value
     */
    public String getDisplayName() {
        return this.displayName;
    }

    /**
     * Set the displayName value.
     *
     * @param displayName the displayName value to set
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Get the usesTaskDependencies value.
     *
     * @return the usesTaskDependencies value
     */
    public Boolean getUsesTaskDependencies() {
        return this.usesTaskDependencies;
    }

    /**
     * Set the usesTaskDependencies value.
     *
     * @param usesTaskDependencies the usesTaskDependencies value to set
     */
    public void setUsesTaskDependencies(Boolean usesTaskDependencies) {
        this.usesTaskDependencies = usesTaskDependencies;
    }

    /**
     * Get the constraints value.
     *
     * @return the constraints value
     */
    public JobConstraints getConstraints() {
        return this.constraints;
    }

    /**
     * Set the constraints value.
     *
     * @param constraints the constraints value to set
     */
    public void setConstraints(JobConstraints constraints) {
        this.constraints = constraints;
    }

    /**
     * Get the jobManagerTask value.
     *
     * @return the jobManagerTask value
     */
    public JobManagerTask getJobManagerTask() {
        return this.jobManagerTask;
    }

    /**
     * Set the jobManagerTask value.
     *
     * @param jobManagerTask the jobManagerTask value to set
     */
    public void setJobManagerTask(JobManagerTask jobManagerTask) {
        this.jobManagerTask = jobManagerTask;
    }

    /**
     * Get the jobPreparationTask value.
     *
     * @return the jobPreparationTask value
     */
    public JobPreparationTask getJobPreparationTask() {
        return this.jobPreparationTask;
    }

    /**
     * Set the jobPreparationTask value.
     *
     * @param jobPreparationTask the jobPreparationTask value to set
     */
    public void setJobPreparationTask(JobPreparationTask jobPreparationTask) {
        this.jobPreparationTask = jobPreparationTask;
    }

    /**
     * Get the jobReleaseTask value.
     *
     * @return the jobReleaseTask value
     */
    public JobReleaseTask getJobReleaseTask() {
        return this.jobReleaseTask;
    }

    /**
     * Set the jobReleaseTask value.
     *
     * @param jobReleaseTask the jobReleaseTask value to set
     */
    public void setJobReleaseTask(JobReleaseTask jobReleaseTask) {
        this.jobReleaseTask = jobReleaseTask;
    }

    /**
     * Get the commonEnvironmentSettings value.
     *
     * @return the commonEnvironmentSettings value
     */
    public List<EnvironmentSetting> getCommonEnvironmentSettings() {
        return this.commonEnvironmentSettings;
    }

    /**
     * Set the commonEnvironmentSettings value.
     *
     * @param commonEnvironmentSettings the commonEnvironmentSettings value to set
     */
    public void setCommonEnvironmentSettings(List<EnvironmentSetting> commonEnvironmentSettings) {
        this.commonEnvironmentSettings = commonEnvironmentSettings;
    }

    /**
     * Get the poolInfo value.
     *
     * @return the poolInfo value
     */
    public PoolInformation getPoolInfo() {
        return this.poolInfo;
    }

    /**
     * Set the poolInfo value.
     *
     * @param poolInfo the poolInfo value to set
     */
    public void setPoolInfo(PoolInformation poolInfo) {
        this.poolInfo = poolInfo;
    }

    /**
     * Get the metadata value.
     *
     * @return the metadata value
     */
    public List<MetadataItem> getMetadata() {
        return this.metadata;
    }

    /**
     * Set the metadata value.
     *
     * @param metadata the metadata value to set
     */
    public void setMetadata(List<MetadataItem> metadata) {
        this.metadata = metadata;
    }

}
