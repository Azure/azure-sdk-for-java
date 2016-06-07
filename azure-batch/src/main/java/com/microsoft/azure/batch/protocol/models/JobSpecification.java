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
     * The priority of jobs created under this schedule. Priority values can
     * range from -1000 to 1000, with -1000 being the lowest priority and
     * 1000 being the highest priority. The default value is 0.
     */
    private Integer priority;

    /**
     * The display name for jobs created under this schedule. It need not be
     * unique and can contain any Unicode characters up to a maximum length
     * of 1024.
     */
    private String displayName;

    /**
     * The flag that determines if this job will use tasks with dependencies.
     */
    private Boolean usesTaskDependencies;

    /**
     * The execution constraints for jobs created under this schedule.
     */
    private JobConstraints constraints;

    /**
     * The details of a Job Manager task to be launched when a job is started
     * under this schedule.
     */
    private JobManagerTask jobManagerTask;

    /**
     * The Job Preparation task for jobs created under this schedule.
     */
    private JobPreparationTask jobPreparationTask;

    /**
     * The Job Release task for jobs created under this schedule.
     */
    private JobReleaseTask jobReleaseTask;

    /**
     * A list of common environment variable settings. These environment
     * variables are set for all tasks in jobs created under this schedule
     * (including the Job Manager, Job Preparation and Job Release tasks).
     */
    private List<EnvironmentSetting> commonEnvironmentSettings;

    /**
     * The pool on which the Batch service runs the tasks of jobs created
     * under this schedule.
     */
    private PoolInformation poolInfo;

    /**
     * A list of name-value pairs associated with each job created under this
     * schedule as metadata.
     */
    private List<MetadataItem> metadata;

    /**
     * Get the priority value.
     *
     * @return the priority value
     */
    public Integer priority() {
        return this.priority;
    }

    /**
     * Set the priority value.
     *
     * @param priority the priority value to set
     * @return the JobSpecification object itself.
     */
    public JobSpecification withPriority(Integer priority) {
        this.priority = priority;
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
     * @return the JobSpecification object itself.
     */
    public JobSpecification withDisplayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    /**
     * Get the usesTaskDependencies value.
     *
     * @return the usesTaskDependencies value
     */
    public Boolean usesTaskDependencies() {
        return this.usesTaskDependencies;
    }

    /**
     * Set the usesTaskDependencies value.
     *
     * @param usesTaskDependencies the usesTaskDependencies value to set
     * @return the JobSpecification object itself.
     */
    public JobSpecification withUsesTaskDependencies(Boolean usesTaskDependencies) {
        this.usesTaskDependencies = usesTaskDependencies;
        return this;
    }

    /**
     * Get the constraints value.
     *
     * @return the constraints value
     */
    public JobConstraints constraints() {
        return this.constraints;
    }

    /**
     * Set the constraints value.
     *
     * @param constraints the constraints value to set
     * @return the JobSpecification object itself.
     */
    public JobSpecification withConstraints(JobConstraints constraints) {
        this.constraints = constraints;
        return this;
    }

    /**
     * Get the jobManagerTask value.
     *
     * @return the jobManagerTask value
     */
    public JobManagerTask jobManagerTask() {
        return this.jobManagerTask;
    }

    /**
     * Set the jobManagerTask value.
     *
     * @param jobManagerTask the jobManagerTask value to set
     * @return the JobSpecification object itself.
     */
    public JobSpecification withJobManagerTask(JobManagerTask jobManagerTask) {
        this.jobManagerTask = jobManagerTask;
        return this;
    }

    /**
     * Get the jobPreparationTask value.
     *
     * @return the jobPreparationTask value
     */
    public JobPreparationTask jobPreparationTask() {
        return this.jobPreparationTask;
    }

    /**
     * Set the jobPreparationTask value.
     *
     * @param jobPreparationTask the jobPreparationTask value to set
     * @return the JobSpecification object itself.
     */
    public JobSpecification withJobPreparationTask(JobPreparationTask jobPreparationTask) {
        this.jobPreparationTask = jobPreparationTask;
        return this;
    }

    /**
     * Get the jobReleaseTask value.
     *
     * @return the jobReleaseTask value
     */
    public JobReleaseTask jobReleaseTask() {
        return this.jobReleaseTask;
    }

    /**
     * Set the jobReleaseTask value.
     *
     * @param jobReleaseTask the jobReleaseTask value to set
     * @return the JobSpecification object itself.
     */
    public JobSpecification withJobReleaseTask(JobReleaseTask jobReleaseTask) {
        this.jobReleaseTask = jobReleaseTask;
        return this;
    }

    /**
     * Get the commonEnvironmentSettings value.
     *
     * @return the commonEnvironmentSettings value
     */
    public List<EnvironmentSetting> commonEnvironmentSettings() {
        return this.commonEnvironmentSettings;
    }

    /**
     * Set the commonEnvironmentSettings value.
     *
     * @param commonEnvironmentSettings the commonEnvironmentSettings value to set
     * @return the JobSpecification object itself.
     */
    public JobSpecification withCommonEnvironmentSettings(List<EnvironmentSetting> commonEnvironmentSettings) {
        this.commonEnvironmentSettings = commonEnvironmentSettings;
        return this;
    }

    /**
     * Get the poolInfo value.
     *
     * @return the poolInfo value
     */
    public PoolInformation poolInfo() {
        return this.poolInfo;
    }

    /**
     * Set the poolInfo value.
     *
     * @param poolInfo the poolInfo value to set
     * @return the JobSpecification object itself.
     */
    public JobSpecification withPoolInfo(PoolInformation poolInfo) {
        this.poolInfo = poolInfo;
        return this;
    }

    /**
     * Get the metadata value.
     *
     * @return the metadata value
     */
    public List<MetadataItem> metadata() {
        return this.metadata;
    }

    /**
     * Set the metadata value.
     *
     * @param metadata the metadata value to set
     * @return the JobSpecification object itself.
     */
    public JobSpecification withMetadata(List<MetadataItem> metadata) {
        this.metadata = metadata;
        return this;
    }

}
