/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol.models;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * An Azure Batch job to add.
 */
public class JobAddParameter {
    /**
     * A string that uniquely identifies the job within the account. The id
     * can contain any combination of alphanumeric characters including
     * hyphens and underscores, and cannot contain more than 64 characters.
     * It is common to use a GUID for the id.
     */
    @JsonProperty(required = true)
    private String id;

    /**
     * The display name for the job.
     */
    private String displayName;

    /**
     * The priority of the job. Priority values can range from -1000 to 1000,
     * with -1000 being the lowest priority and 1000 being the highest
     * priority. The default value is 0.
     */
    private Integer priority;

    /**
     * The execution constraints for the job.
     */
    private JobConstraints constraints;

    /**
     * Details of a Job Manager task to be launched when the job is started.
     */
    private JobManagerTask jobManagerTask;

    /**
     * The Job Preparation task.
     */
    private JobPreparationTask jobPreparationTask;

    /**
     * The Job Release task.
     */
    private JobReleaseTask jobReleaseTask;

    /**
     * The list of common environment variable settings. These environment
     * variables are set for all tasks in the job (including the Job Manager,
     * Job Preparation and Job Release tasks).
     */
    private List<EnvironmentSetting> commonEnvironmentSettings;

    /**
     * The pool on which the Batch service runs the job's tasks.
     */
    @JsonProperty(required = true)
    private PoolInformation poolInfo;

    /**
     * A list of name-value pairs associated with the job as metadata.
     */
    private List<MetadataItem> metadata;

    /**
     * The flag that determines if this job will use tasks with dependencies.
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
     * @return the JobAddParameter object itself.
     */
    public JobAddParameter withId(String id) {
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
     * @return the JobAddParameter object itself.
     */
    public JobAddParameter withDisplayName(String displayName) {
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
     * @return the JobAddParameter object itself.
     */
    public JobAddParameter withPriority(Integer priority) {
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
     * @return the JobAddParameter object itself.
     */
    public JobAddParameter withConstraints(JobConstraints constraints) {
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
     * @return the JobAddParameter object itself.
     */
    public JobAddParameter withJobManagerTask(JobManagerTask jobManagerTask) {
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
     * @return the JobAddParameter object itself.
     */
    public JobAddParameter withJobPreparationTask(JobPreparationTask jobPreparationTask) {
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
     * @return the JobAddParameter object itself.
     */
    public JobAddParameter withJobReleaseTask(JobReleaseTask jobReleaseTask) {
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
     * @return the JobAddParameter object itself.
     */
    public JobAddParameter withCommonEnvironmentSettings(List<EnvironmentSetting> commonEnvironmentSettings) {
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
     * @return the JobAddParameter object itself.
     */
    public JobAddParameter withPoolInfo(PoolInformation poolInfo) {
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
     * @return the JobAddParameter object itself.
     */
    public JobAddParameter withMetadata(List<MetadataItem> metadata) {
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
     * @return the JobAddParameter object itself.
     */
    public JobAddParameter withUsesTaskDependencies(Boolean usesTaskDependencies) {
        this.usesTaskDependencies = usesTaskDependencies;
        return this;
    }

}
