/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol.models;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Parameters for a CloudJobScheduleOperations.Update request.
 */
public class JobScheduleUpdateParameter {
    /**
     * Sets the schedule according to which jobs will be created. If you do
     * not specify this element, it is equivalent to passing the default
     * schedule: that is, a single job scheduled to run immediately.
     */
    @JsonProperty(required = true)
    private Schedule schedule;

    /**
     * Sets details of the jobs to be created on this schedule.
     */
    @JsonProperty(required = true)
    private JobSpecification jobSpecification;

    /**
     * Sets a list of name-value pairs associated with the job schedule as
     * metadata. If you do not specify this element, it takes the default
     * value of an empty list; in effect, any existing metadata is deleted.
     */
    private List<MetadataItem> metadata;

    /**
     * Get the schedule value.
     *
     * @return the schedule value
     */
    public Schedule getSchedule() {
        return this.schedule;
    }

    /**
     * Set the schedule value.
     *
     * @param schedule the schedule value to set
     */
    public void setSchedule(Schedule schedule) {
        this.schedule = schedule;
    }

    /**
     * Get the jobSpecification value.
     *
     * @return the jobSpecification value
     */
    public JobSpecification getJobSpecification() {
        return this.jobSpecification;
    }

    /**
     * Set the jobSpecification value.
     *
     * @param jobSpecification the jobSpecification value to set
     */
    public void setJobSpecification(JobSpecification jobSpecification) {
        this.jobSpecification = jobSpecification;
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
