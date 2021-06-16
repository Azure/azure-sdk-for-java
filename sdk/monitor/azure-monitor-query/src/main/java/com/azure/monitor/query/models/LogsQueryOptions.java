// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query.models;

import com.azure.core.annotation.Fluent;

import java.time.Duration;
import java.util.List;

/**
 * The options type to configure the logs query
 */
@Fluent
public final class LogsQueryOptions {
    private final QueryTimeSpan timeSpan;
    private final String workspaceId;
    private final String query;

    private boolean includeRendering;
    private boolean includeStatistics;
    private Duration serverTimeout;
    private List<String> workspaceNames;
    private List<String> workspaceIds;
    private List<String> azureResourceIds;
    private List<String> qualifiedWorkspaceNames;

    /**
     * Creates an instance of {@link LogsQueryOptions} with required params.
     * @param workspaceId The workspaceId  on which the query is executed.
     * @param query The Kusto query.
     * @param timeSpan The time period for which the logs should be queried.
     */
    public LogsQueryOptions(String workspaceId, String query, QueryTimeSpan timeSpan) {
        this.workspaceId = workspaceId;
        this.query = query;
        this.timeSpan = timeSpan;
    }

    /**
     * Returns the server timeout for this query.
     * @return The server timeout duration.
     */
    public Duration getServerTimeout() {
        return serverTimeout;
    }

    /**
     * Sets the server timeout for this query.
     * @param serverTimeout The server timeout duration.
     * @return The updated options instance.
     */
    public LogsQueryOptions setServerTimeout(Duration serverTimeout) {
        this.serverTimeout = serverTimeout;
        return this;
    }

    /**
     * Returns the flag that indicates if the query should return rendering details.
     * @return The flag that indicates if the query should return rendering details.
     */
    public boolean isIncludeRendering() {
        return includeRendering;
    }

    /**
     * Sets the flag that indicates if the query should return rendering details.
     * @param includeRendering The flag that indicates if the query should return rendering details.
     * @return The updated options instance.
     */
    public LogsQueryOptions setIncludeRendering(boolean includeRendering) {
        this.includeRendering = includeRendering;
        return this;
    }

    /**
     * Returns the flag that indicates if the query should include statistics.
     * @return the flag that indicates if the query should include statistics.
     */
    public boolean isIncludeStatistics() {
        return includeStatistics;
    }

    /**
     * Sets the flag that indicates if the query should include statistics.
     * @param includeStatistics the flag that indicates if the query should include statistics.
     * @return The updated options instance.
     */
    public LogsQueryOptions setIncludeStatistics(boolean includeStatistics) {
        this.includeStatistics = includeStatistics;
        return this;
    }

    /**
     * Returns the list of workspace names on which this query is executed.
     * @return The list of workspace names on which this query is executed.
     */
    public List<String> getWorkspaceNames() {
        return workspaceNames;
    }

    /**
     * Sets the list of workspace names on which this query is executed.
     * @param workspaceNames The list of workspace names on which this query is executed.
     * @return The updated options instance.
     */
    public LogsQueryOptions setWorkspaceNames(List<String> workspaceNames) {
        this.workspaceNames = workspaceNames;
        return this;
    }

    /**
     * Returns the list of workspace ids on which this query is executed.
     * @return The list of workspace ids on which this query is executed.
     */
    public List<String> getWorkspaceIds() {
        return workspaceIds;
    }

    /**
     * Sets the list of workspace ids on which this query is executed.
     * @param workspaceIds the list of workspace ids on which this query is executed.
     * @return The updated options instance.
     */
    public LogsQueryOptions setWorkspaceIds(List<String> workspaceIds) {
        this.workspaceIds = workspaceIds;
        return this;
    }

    /**
     * Returns the list of Azure resource ids on which this query is executed.
     * @return the list of Azure resource ids on which this query is executed.
     */
    public List<String> getAzureResourceIds() {
        return azureResourceIds;
    }

    /**
     * Sets the list of Azure resource ids on which this query is executed.
     * @param azureResourceIds the list of Azure resource ids on which this query is executed.
     * @return The updated options instance.
     */
    public LogsQueryOptions setAzureResourceIds(List<String> azureResourceIds) {
        this.azureResourceIds = azureResourceIds;
        return this;
    }

    /**
     * Returns the list of qualified workspace names on which this query is executed.
     * @return the list of qualified workspace names on which this query is executed.
     */
    public List<String> getQualifiedWorkspaceNames() {
        return qualifiedWorkspaceNames;
    }

    /**
     * Sets the list of qualified workspace names on which this query is executed.
     * @param qualifiedWorkspaceNames the list of qualified workspace names on which this query is executed.
     * @return The updated options instance.
     */
    public LogsQueryOptions setQualifiedWorkspaceNames(List<String> qualifiedWorkspaceNames) {
        this.qualifiedWorkspaceNames = qualifiedWorkspaceNames;
        return this;
    }

    /**
     * Returns the workspace id on which this query is executed.
     * @return the workspace id on which this query is executed.
     */
    public String getWorkspaceId() {
        return workspaceId;
    }

    /**
     * Returns the Kusto query.
     * @return the Kusto query.
     */
    public String getQuery() {
        return query;
    }

    /**
     * Returns the timespan for this query.
     * @return the timespan for this query.
     */
    public QueryTimeSpan getTimeSpan() {
        return timeSpan;
    }
}
