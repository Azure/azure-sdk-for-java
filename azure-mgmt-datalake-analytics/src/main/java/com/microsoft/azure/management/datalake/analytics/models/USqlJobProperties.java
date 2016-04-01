/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.datalake.analytics.models;

import java.util.List;
import org.joda.time.Period;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * The USqlJobProperties model.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonTypeName("USql")
public class USqlJobProperties extends JobProperties {
    /**
     * Gets or sets the list of resources that are required by the job.
     */
    private List<JobResource> resources;

    /**
     * Gets or sets the job specific statistics.
     */
    private JobStatistics statistics;

    /**
     * Gets or sets the job specific debug data locations.
     */
    private JobDataPath debugData;

    /**
     * Gets the U-SQL algebra file path after the job has completed.
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String algebraFilePath;

    /**
     * Gets the total time this job spent compiling. This value should not be
     * set by the user and will be ignored if it is.
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Period totalCompilationTime;

    /**
     * Gets the total time this job spent paused. This value should not be set
     * by the user and will be ignored if it is.
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Period totalPauseTime;

    /**
     * Gets the total time this job spent queued. This value should not be set
     * by the user and will be ignored if it is.
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Period totalQueuedTime;

    /**
     * Gets the total time this job spent executing. This value should not be
     * set by the user and will be ignored if it is.
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Period totalRunningTime;

    /**
     * Gets the ID used to identify the job manager coordinating job
     * execution. This value should not be set by the user and will be
     * ignored if it is.
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String rootProcessNodeId;

    /**
     * Gets the ID used to identify the yarn application executing the job.
     * This value should not be set by the user and will be ignored if it is.
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String yarnApplicationId;

    /**
     * Gets the timestamp (in ticks) for the yarn application executing the
     * job. This value should not be set by the user and will be ignored if
     * it is.
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Long yarnApplicationTimeStamp;

    /**
     * Gets or sets the compile mode for the job. Possible values include:
     * 'Semantic', 'Full', 'SingleBox'.
     */
    private CompileMode compileMode;

    /**
     * Get the resources value.
     *
     * @return the resources value
     */
    public List<JobResource> getResources() {
        return this.resources;
    }

    /**
     * Set the resources value.
     *
     * @param resources the resources value to set
     */
    public void setResources(List<JobResource> resources) {
        this.resources = resources;
    }

    /**
     * Get the statistics value.
     *
     * @return the statistics value
     */
    public JobStatistics getStatistics() {
        return this.statistics;
    }

    /**
     * Set the statistics value.
     *
     * @param statistics the statistics value to set
     */
    public void setStatistics(JobStatistics statistics) {
        this.statistics = statistics;
    }

    /**
     * Get the debugData value.
     *
     * @return the debugData value
     */
    public JobDataPath getDebugData() {
        return this.debugData;
    }

    /**
     * Set the debugData value.
     *
     * @param debugData the debugData value to set
     */
    public void setDebugData(JobDataPath debugData) {
        this.debugData = debugData;
    }

    /**
     * Get the algebraFilePath value.
     *
     * @return the algebraFilePath value
     */
    public String getAlgebraFilePath() {
        return this.algebraFilePath;
    }

    /**
     * Get the totalCompilationTime value.
     *
     * @return the totalCompilationTime value
     */
    public Period getTotalCompilationTime() {
        return this.totalCompilationTime;
    }

    /**
     * Get the totalPauseTime value.
     *
     * @return the totalPauseTime value
     */
    public Period getTotalPauseTime() {
        return this.totalPauseTime;
    }

    /**
     * Get the totalQueuedTime value.
     *
     * @return the totalQueuedTime value
     */
    public Period getTotalQueuedTime() {
        return this.totalQueuedTime;
    }

    /**
     * Get the totalRunningTime value.
     *
     * @return the totalRunningTime value
     */
    public Period getTotalRunningTime() {
        return this.totalRunningTime;
    }

    /**
     * Get the rootProcessNodeId value.
     *
     * @return the rootProcessNodeId value
     */
    public String getRootProcessNodeId() {
        return this.rootProcessNodeId;
    }

    /**
     * Get the yarnApplicationId value.
     *
     * @return the yarnApplicationId value
     */
    public String getYarnApplicationId() {
        return this.yarnApplicationId;
    }

    /**
     * Get the yarnApplicationTimeStamp value.
     *
     * @return the yarnApplicationTimeStamp value
     */
    public Long getYarnApplicationTimeStamp() {
        return this.yarnApplicationTimeStamp;
    }

    /**
     * Get the compileMode value.
     *
     * @return the compileMode value
     */
    public CompileMode getCompileMode() {
        return this.compileMode;
    }

    /**
     * Set the compileMode value.
     *
     * @param compileMode the compileMode value to set
     */
    public void setCompileMode(CompileMode compileMode) {
        this.compileMode = compileMode;
    }

}
