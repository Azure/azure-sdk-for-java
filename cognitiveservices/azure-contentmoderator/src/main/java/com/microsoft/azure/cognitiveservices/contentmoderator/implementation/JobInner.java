/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.contentmoderator.implementation;

import java.util.List;
import com.microsoft.azure.cognitiveservices.contentmoderator.KeyValuePair;
import com.microsoft.azure.cognitiveservices.contentmoderator.JobExecutionReportDetails;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The Job object.
 */
public class JobInner {
    /**
     * The job id.
     */
    @JsonProperty(value = "Id")
    private String id;

    /**
     * The team name associated with the job.
     */
    @JsonProperty(value = "TeamName")
    private String teamName;

    /**
     * The status string (&lt;Pending, Failed, Completed&gt;).
     */
    @JsonProperty(value = "Status")
    private String status;

    /**
     * The Id of the workflow.
     */
    @JsonProperty(value = "WorkflowId")
    private String workflowId;

    /**
     * Type of the content.
     */
    @JsonProperty(value = "Type")
    private String type;

    /**
     * The callback endpoint.
     */
    @JsonProperty(value = "CallBackEndpoint")
    private String callBackEndpoint;

    /**
     * Review Id if one is created.
     */
    @JsonProperty(value = "ReviewId")
    private String reviewId;

    /**
     * Array of KeyValue pairs.
     */
    @JsonProperty(value = "ResultMetaData")
    private List<KeyValuePair> resultMetaData;

    /**
     * Job execution report- Array of KeyValue pairs object.
     */
    @JsonProperty(value = "JobExecutionReport")
    private List<JobExecutionReportDetails> jobExecutionReport;

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
     * @return the JobInner object itself.
     */
    public JobInner withId(String id) {
        this.id = id;
        return this;
    }

    /**
     * Get the teamName value.
     *
     * @return the teamName value
     */
    public String teamName() {
        return this.teamName;
    }

    /**
     * Set the teamName value.
     *
     * @param teamName the teamName value to set
     * @return the JobInner object itself.
     */
    public JobInner withTeamName(String teamName) {
        this.teamName = teamName;
        return this;
    }

    /**
     * Get the status value.
     *
     * @return the status value
     */
    public String status() {
        return this.status;
    }

    /**
     * Set the status value.
     *
     * @param status the status value to set
     * @return the JobInner object itself.
     */
    public JobInner withStatus(String status) {
        this.status = status;
        return this;
    }

    /**
     * Get the workflowId value.
     *
     * @return the workflowId value
     */
    public String workflowId() {
        return this.workflowId;
    }

    /**
     * Set the workflowId value.
     *
     * @param workflowId the workflowId value to set
     * @return the JobInner object itself.
     */
    public JobInner withWorkflowId(String workflowId) {
        this.workflowId = workflowId;
        return this;
    }

    /**
     * Get the type value.
     *
     * @return the type value
     */
    public String type() {
        return this.type;
    }

    /**
     * Set the type value.
     *
     * @param type the type value to set
     * @return the JobInner object itself.
     */
    public JobInner withType(String type) {
        this.type = type;
        return this;
    }

    /**
     * Get the callBackEndpoint value.
     *
     * @return the callBackEndpoint value
     */
    public String callBackEndpoint() {
        return this.callBackEndpoint;
    }

    /**
     * Set the callBackEndpoint value.
     *
     * @param callBackEndpoint the callBackEndpoint value to set
     * @return the JobInner object itself.
     */
    public JobInner withCallBackEndpoint(String callBackEndpoint) {
        this.callBackEndpoint = callBackEndpoint;
        return this;
    }

    /**
     * Get the reviewId value.
     *
     * @return the reviewId value
     */
    public String reviewId() {
        return this.reviewId;
    }

    /**
     * Set the reviewId value.
     *
     * @param reviewId the reviewId value to set
     * @return the JobInner object itself.
     */
    public JobInner withReviewId(String reviewId) {
        this.reviewId = reviewId;
        return this;
    }

    /**
     * Get the resultMetaData value.
     *
     * @return the resultMetaData value
     */
    public List<KeyValuePair> resultMetaData() {
        return this.resultMetaData;
    }

    /**
     * Set the resultMetaData value.
     *
     * @param resultMetaData the resultMetaData value to set
     * @return the JobInner object itself.
     */
    public JobInner withResultMetaData(List<KeyValuePair> resultMetaData) {
        this.resultMetaData = resultMetaData;
        return this;
    }

    /**
     * Get the jobExecutionReport value.
     *
     * @return the jobExecutionReport value
     */
    public List<JobExecutionReportDetails> jobExecutionReport() {
        return this.jobExecutionReport;
    }

    /**
     * Set the jobExecutionReport value.
     *
     * @param jobExecutionReport the jobExecutionReport value to set
     * @return the JobInner object itself.
     */
    public JobInner withJobExecutionReport(List<JobExecutionReportDetails> jobExecutionReport) {
        this.jobExecutionReport = jobExecutionReport;
        return this;
    }

}
