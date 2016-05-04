/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol.implementation.api;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * An Azure Batch job to add.
 */
public class JobAddParameterInner {
    /**
     * Gets or sets a string that uniquely identifies the job within the
     * account. The id can contain any combination of alphanumeric characters
     * including hyphens and underscores, and cannot contain more than 64
     * characters. It is common to use a GUID for the id.
     */
    @JsonProperty(required = true)
    private String id;

    /**
     * Gets or sets the display name for the job.
     */
    private String displayName;

    /**
     * Gets or sets the priority of the job. Priority values can range from
     * -1000 to 1000, with -1000 being the lowest priority and 1000 being the
     * highest priority. The default value is 0.
     */
    private Integer priority;

    /**
     * Gets or sets the execution constraints for the job.
     */
    private JobConstraints constraints;

    /**
     * Gets or sets details of a Job Manager task to be launched when the job
     * is started.
     */
    private JobManagerTask jobManagerTask;

    /**
     * Gets or sets the Job Preparation task.
     */
    private JobPreparationTask jobPreparationTask;

    /**
     * Gets or sets the Job Release task.
     */
    private JobReleaseTask jobReleaseTask;

    /**
     * Gets or sets the list of common environment variable settings.  These
     * environment variables are set for all tasks in the job (including the
     * Job Manager, Job Preparation and Job Release tasks).
     */
    private List<EnvironmentSetting> commonEnvironmentSettings;

    /**
     * Gets or sets the pool on which the Batch service runs the job’s tasks.
     */
    @JsonProperty(required = true)
    private PoolInformation poolInfo;

    /**
     * Gets or sets a list of name-value pairs associated with the job as
     * metadata.
     */
    private List<MetadataItem> metadata;

    /**
     * Gets or sets the flag that determines if this job will use tasks with
     * dependencies.
     */
    private Boolean usesTaskDependencies;

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
     * @return the JobAddParameterInner object itself.
     */
    public JobAddParameterInner setId(String id) {
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
     * @return the JobAddParameterInner object itself.
     */
    public JobAddParameterInner setDisplayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

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
     * @return the JobAddParameterInner object itself.
     */
    public JobAddParameterInner setPriority(Integer priority) {
        this.priority = priority;
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
     * @return the JobAddParameterInner object itself.
     */
    public JobAddParameterInner setConstraints(JobConstraints constraints) {
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
     * @return the JobAddParameterInner object itself.
     */
    public JobAddParameterInner setJobManagerTask(JobManagerTask jobManagerTask) {
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
     * @return the JobAddParameterInner object itself.
     */
    public JobAddParameterInner setJobPreparationTask(JobPreparationTask jobPreparationTask) {
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
     * @return the JobAddParameterInner object itself.
     */
    public JobAddParameterInner setJobReleaseTask(JobReleaseTask jobReleaseTask) {
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
     * @return the JobAddParameterInner object itself.
     */
    public JobAddParameterInner setCommonEnvironmentSettings(List<EnvironmentSetting> commonEnvironmentSettings) {
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
     * @return the JobAddParameterInner object itself.
     */
    public JobAddParameterInner setPoolInfo(PoolInformation poolInfo) {
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
     * @return the JobAddParameterInner object itself.
     */
    public JobAddParameterInner setMetadata(List<MetadataItem> metadata) {
        this.metadata = metadata;
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
     * @return the JobAddParameterInner object itself.
     */
    public JobAddParameterInner setUsesTaskDependencies(Boolean usesTaskDependencies) {
        this.usesTaskDependencies = usesTaskDependencies;
        return this;
    }

}
