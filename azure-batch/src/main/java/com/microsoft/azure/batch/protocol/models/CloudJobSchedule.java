/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol.models;

import org.joda.time.DateTime;
import java.util.List;

/**
 * A job schedule that allows recurring jobs by specifying when to run jobs
 * and a specification used to create each job.
 */
public class CloudJobSchedule {
    /**
     * A string that uniquely identifies the schedule within the account. A
     * GUID is recommended.
     */
    private String id;

    /**
     * The display name for the schedule.
     */
    private String displayName;

    /**
     * The URL of the job schedule.
     */
    private String url;

    /**
     * The ETag of the job schedule.
     */
    private String eTag;

    /**
     * The last modified time of the job schedule.
     */
    private DateTime lastModified;

    /**
     * The creation time of the job schedule.
     */
    private DateTime creationTime;

    /**
     * The current state of the job schedule. Possible values include:
     * 'active', 'completed', 'disabled', 'terminating', 'deleting'.
     */
    private JobScheduleState state;

    /**
     * The time at which the job schedule entered the current state.
     */
    private DateTime stateTransitionTime;

    /**
     * The previous state of the job schedule. Possible values include:
     * 'active', 'completed', 'disabled', 'terminating', 'deleting'.
     */
    private JobScheduleState previousState;

    /**
     * The time at which the job schedule entered its previous state.
     */
    private DateTime previousStateTransitionTime;

    /**
     * The schedule according to which jobs will be created.
     */
    private Schedule schedule;

    /**
     * The details of the jobs to be created on this schedule.
     */
    private JobSpecification jobSpecification;

    /**
     * Information about jobs that have been and will be run under this
     * schedule.
     */
    private JobScheduleExecutionInformation executionInfo;

    /**
     * A list of name-value pairs associated with the schedule as metadata.
     */
    private List<MetadataItem> metadata;

    /**
     * The lifetime resource usage statistics for the job schedule.
     */
    private JobScheduleStatistics stats;

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
     * @return the CloudJobSchedule object itself.
     */
    public CloudJobSchedule withId(String id) {
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
     * @return the CloudJobSchedule object itself.
     */
    public CloudJobSchedule withDisplayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    /**
     * Get the url value.
     *
     * @return the url value
     */
    public String url() {
        return this.url;
    }

    /**
     * Set the url value.
     *
     * @param url the url value to set
     * @return the CloudJobSchedule object itself.
     */
    public CloudJobSchedule withUrl(String url) {
        this.url = url;
        return this;
    }

    /**
     * Get the eTag value.
     *
     * @return the eTag value
     */
    public String eTag() {
        return this.eTag;
    }

    /**
     * Set the eTag value.
     *
     * @param eTag the eTag value to set
     * @return the CloudJobSchedule object itself.
     */
    public CloudJobSchedule withETag(String eTag) {
        this.eTag = eTag;
        return this;
    }

    /**
     * Get the lastModified value.
     *
     * @return the lastModified value
     */
    public DateTime lastModified() {
        return this.lastModified;
    }

    /**
     * Set the lastModified value.
     *
     * @param lastModified the lastModified value to set
     * @return the CloudJobSchedule object itself.
     */
    public CloudJobSchedule withLastModified(DateTime lastModified) {
        this.lastModified = lastModified;
        return this;
    }

    /**
     * Get the creationTime value.
     *
     * @return the creationTime value
     */
    public DateTime creationTime() {
        return this.creationTime;
    }

    /**
     * Set the creationTime value.
     *
     * @param creationTime the creationTime value to set
     * @return the CloudJobSchedule object itself.
     */
    public CloudJobSchedule withCreationTime(DateTime creationTime) {
        this.creationTime = creationTime;
        return this;
    }

    /**
     * Get the state value.
     *
     * @return the state value
     */
    public JobScheduleState state() {
        return this.state;
    }

    /**
     * Set the state value.
     *
     * @param state the state value to set
     * @return the CloudJobSchedule object itself.
     */
    public CloudJobSchedule withState(JobScheduleState state) {
        this.state = state;
        return this;
    }

    /**
     * Get the stateTransitionTime value.
     *
     * @return the stateTransitionTime value
     */
    public DateTime stateTransitionTime() {
        return this.stateTransitionTime;
    }

    /**
     * Set the stateTransitionTime value.
     *
     * @param stateTransitionTime the stateTransitionTime value to set
     * @return the CloudJobSchedule object itself.
     */
    public CloudJobSchedule withStateTransitionTime(DateTime stateTransitionTime) {
        this.stateTransitionTime = stateTransitionTime;
        return this;
    }

    /**
     * Get the previousState value.
     *
     * @return the previousState value
     */
    public JobScheduleState previousState() {
        return this.previousState;
    }

    /**
     * Set the previousState value.
     *
     * @param previousState the previousState value to set
     * @return the CloudJobSchedule object itself.
     */
    public CloudJobSchedule withPreviousState(JobScheduleState previousState) {
        this.previousState = previousState;
        return this;
    }

    /**
     * Get the previousStateTransitionTime value.
     *
     * @return the previousStateTransitionTime value
     */
    public DateTime previousStateTransitionTime() {
        return this.previousStateTransitionTime;
    }

    /**
     * Set the previousStateTransitionTime value.
     *
     * @param previousStateTransitionTime the previousStateTransitionTime value to set
     * @return the CloudJobSchedule object itself.
     */
    public CloudJobSchedule withPreviousStateTransitionTime(DateTime previousStateTransitionTime) {
        this.previousStateTransitionTime = previousStateTransitionTime;
        return this;
    }

    /**
     * Get the schedule value.
     *
     * @return the schedule value
     */
    public Schedule schedule() {
        return this.schedule;
    }

    /**
     * Set the schedule value.
     *
     * @param schedule the schedule value to set
     * @return the CloudJobSchedule object itself.
     */
    public CloudJobSchedule withSchedule(Schedule schedule) {
        this.schedule = schedule;
        return this;
    }

    /**
     * Get the jobSpecification value.
     *
     * @return the jobSpecification value
     */
    public JobSpecification jobSpecification() {
        return this.jobSpecification;
    }

    /**
     * Set the jobSpecification value.
     *
     * @param jobSpecification the jobSpecification value to set
     * @return the CloudJobSchedule object itself.
     */
    public CloudJobSchedule withJobSpecification(JobSpecification jobSpecification) {
        this.jobSpecification = jobSpecification;
        return this;
    }

    /**
     * Get the executionInfo value.
     *
     * @return the executionInfo value
     */
    public JobScheduleExecutionInformation executionInfo() {
        return this.executionInfo;
    }

    /**
     * Set the executionInfo value.
     *
     * @param executionInfo the executionInfo value to set
     * @return the CloudJobSchedule object itself.
     */
    public CloudJobSchedule withExecutionInfo(JobScheduleExecutionInformation executionInfo) {
        this.executionInfo = executionInfo;
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
     * @return the CloudJobSchedule object itself.
     */
    public CloudJobSchedule withMetadata(List<MetadataItem> metadata) {
        this.metadata = metadata;
        return this;
    }

    /**
     * Get the stats value.
     *
     * @return the stats value
     */
    public JobScheduleStatistics stats() {
        return this.stats;
    }

    /**
     * Set the stats value.
     *
     * @param stats the stats value to set
     * @return the CloudJobSchedule object itself.
     */
    public CloudJobSchedule withStats(JobScheduleStatistics stats) {
        this.stats = stats;
        return this;
    }

}
