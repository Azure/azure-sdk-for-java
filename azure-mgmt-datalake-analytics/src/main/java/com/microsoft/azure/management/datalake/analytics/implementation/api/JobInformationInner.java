/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.datalake.analytics.implementation.api;

import java.util.UUID;
import java.util.List;
import org.joda.time.DateTime;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The common Data Lake Analytics job information properties.
 */
public class JobInformationInner {
    /**
     * Gets or sets the job's unique identifier (a GUID).
     */
    private UUID jobId;

    /**
     * Gets or sets the friendly name of the job.
     */
    @JsonProperty(required = true)
    private String name;

    /**
     * Gets or sets the job type of the current job (Hive or USql). Possible
     * values include: 'USql', 'Hive'.
     */
    @JsonProperty(required = true)
    private JobType type;

    /**
     * Gets or sets the user or account that submitted the job.
     */
    private String submitter;

    /**
     * Gets the error message details for the job, if the job failed.
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private List<JobErrorDetails> errorMessage;

    /**
     * Gets or sets the degree of parallelism used for this job. This must be
     * greater than 0.
     */
    private Integer degreeOfParallelism;

    /**
     * Gets or sets the priority value for the current job. Lower numbers have
     * a higher priority. By default, a job has a priority of 1000. This must
     * be greater than 0.
     */
    private Integer priority;

    /**
     * Gets the time the job was submitted to the service.
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private DateTime submitTime;

    /**
     * Gets the start time of the job.
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private DateTime startTime;

    /**
     * Gets the completion time of the job.
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private DateTime endTime;

    /**
     * Gets the job state. When the job is in the Ended state, refer to Result
     * and ErrorMessage for details. Possible values include: 'Accepted',
     * 'Compiling', 'Ended', 'New', 'Queued', 'Running', 'Scheduling',
     * 'Starting', 'Paused', 'WaitingForCapacity'.
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private JobState state;

    /**
     * Gets the result of job execution or the current result of the running
     * job. Possible values include: 'None', 'Succeeded', 'Cancelled',
     * 'Failed'.
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private JobResult result;

    /**
     * Gets or sets the log folder path to use in the following format:
     * adl://&lt;accountName&gt;.azuredatalakestore.net/system/jobservice/jobs/Usql/2016/03/13/17/18/5fe51957-93bc-4de0-8ddc-c5a4753b068b/logs/.
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String logFolder;

    /**
     * Gets or sets the list of log file name patterns to find in the
     * logFolder. '*' is the only matching character allowed. Example format:
     * jobExecution*.log or *mylog*.txt.
     */
    private List<String> logFilePatterns;

    /**
     * Gets the job state audit records, indicating when various operations
     * have been performed on this job.
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private List<JobStateAuditRecord> stateAuditRecords;

    /**
     * Gets or sets the job specific properties.
     */
    @JsonProperty(required = true)
    private JobProperties properties;

    /**
     * Get the jobId value.
     *
     * @return the jobId value
     */
    public UUID jobId() {
        return this.jobId;
    }

    /**
     * Set the jobId value.
     *
     * @param jobId the jobId value to set
     * @return the JobInformationInner object itself.
     */
    public JobInformationInner withJobId(UUID jobId) {
        this.jobId = jobId;
        return this;
    }

    /**
     * Get the name value.
     *
     * @return the name value
     */
    public String name() {
        return this.name;
    }

    /**
     * Set the name value.
     *
     * @param name the name value to set
     * @return the JobInformationInner object itself.
     */
    public JobInformationInner withName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Get the type value.
     *
     * @return the type value
     */
    public JobType type() {
        return this.type;
    }

    /**
     * Set the type value.
     *
     * @param type the type value to set
     * @return the JobInformationInner object itself.
     */
    public JobInformationInner withType(JobType type) {
        this.type = type;
        return this;
    }

    /**
     * Get the submitter value.
     *
     * @return the submitter value
     */
    public String submitter() {
        return this.submitter;
    }

    /**
     * Set the submitter value.
     *
     * @param submitter the submitter value to set
     * @return the JobInformationInner object itself.
     */
    public JobInformationInner withSubmitter(String submitter) {
        this.submitter = submitter;
        return this;
    }

    /**
     * Get the errorMessage value.
     *
     * @return the errorMessage value
     */
    public List<JobErrorDetails> errorMessage() {
        return this.errorMessage;
    }

    /**
     * Get the degreeOfParallelism value.
     *
     * @return the degreeOfParallelism value
     */
    public Integer degreeOfParallelism() {
        return this.degreeOfParallelism;
    }

    /**
     * Set the degreeOfParallelism value.
     *
     * @param degreeOfParallelism the degreeOfParallelism value to set
     * @return the JobInformationInner object itself.
     */
    public JobInformationInner withDegreeOfParallelism(Integer degreeOfParallelism) {
        this.degreeOfParallelism = degreeOfParallelism;
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
     * @return the JobInformationInner object itself.
     */
    public JobInformationInner withPriority(Integer priority) {
        this.priority = priority;
        return this;
    }

    /**
     * Get the submitTime value.
     *
     * @return the submitTime value
     */
    public DateTime submitTime() {
        return this.submitTime;
    }

    /**
     * Get the startTime value.
     *
     * @return the startTime value
     */
    public DateTime startTime() {
        return this.startTime;
    }

    /**
     * Get the endTime value.
     *
     * @return the endTime value
     */
    public DateTime endTime() {
        return this.endTime;
    }

    /**
     * Get the state value.
     *
     * @return the state value
     */
    public JobState state() {
        return this.state;
    }

    /**
     * Get the result value.
     *
     * @return the result value
     */
    public JobResult result() {
        return this.result;
    }

    /**
     * Get the logFolder value.
     *
     * @return the logFolder value
     */
    public String logFolder() {
        return this.logFolder;
    }

    /**
     * Get the logFilePatterns value.
     *
     * @return the logFilePatterns value
     */
    public List<String> logFilePatterns() {
        return this.logFilePatterns;
    }

    /**
     * Set the logFilePatterns value.
     *
     * @param logFilePatterns the logFilePatterns value to set
     * @return the JobInformationInner object itself.
     */
    public JobInformationInner withLogFilePatterns(List<String> logFilePatterns) {
        this.logFilePatterns = logFilePatterns;
        return this;
    }

    /**
     * Get the stateAuditRecords value.
     *
     * @return the stateAuditRecords value
     */
    public List<JobStateAuditRecord> stateAuditRecords() {
        return this.stateAuditRecords;
    }

    /**
     * Get the properties value.
     *
     * @return the properties value
     */
    public JobProperties properties() {
        return this.properties;
    }

    /**
     * Set the properties value.
     *
     * @param properties the properties value to set
     * @return the JobInformationInner object itself.
     */
    public JobInformationInner withProperties(JobProperties properties) {
        this.properties = properties;
        return this;
    }

}
