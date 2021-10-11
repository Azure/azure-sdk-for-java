// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query.models;

import com.azure.core.annotation.Fluent;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

/**
 * The options type to configure the logs query
 */
@Fluent
public final class LogsQueryOptions {
    private boolean includeVisualization;
    private boolean includeStatistics;
    private boolean allowPartialErrors;
    private Duration serverTimeout;
    private List<String> additionalWorkspaces;

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
     * Returns the flag that indicates if the query should return visualization details.
     * @return The flag that indicates if the query should return visualization details.
     */
    public boolean isIncludeVisualization() {
        return includeVisualization;
    }

    /**
     * Sets the flag that indicates if the query should return visualization details.
     * @param includeVisualization The flag that indicates if the query should return visualization details.
     * @return The updated options instance.
     */
    public LogsQueryOptions setIncludeVisualization(boolean includeVisualization) {
        this.includeVisualization = includeVisualization;
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
     * If a query has partial errors, the result is returned instead of throwing an exception if this is set to true.
     * The partial error information is available as part of the query result.
     *
     * @return Returns true if partial errors should not throw exception.
     */
    public boolean isAllowPartialErrors() {
        return allowPartialErrors;
    }

    /**
     * If set to {@code true}, exception is not thrown if query returns partial errors. The partial error information
     * is available as part of the query result.
     * @param allowPartialErrors set this to {@code true} to not throw exception if a query returns partial errors.
     * @return The updated options instance.
     */
    public LogsQueryOptions setAllowPartialErrors(boolean allowPartialErrors) {
        this.allowPartialErrors = allowPartialErrors;
        return this;
    }

    /**
     * Returns the list of additional workspaces on which this query is executed. The list can contain any of the
     * following workspace identifiers:
     * <ul>
     *     <li>Workspace Name - human-readable string {@code <workspaceName>} of the OMS workspace </li>
     *     <li>Qualified Name - string with format {@code <subscriptionName>/<resourceGroup>/<workspaceName>}</li>
     *     <li>Workspace ID - GUID string</li>
     *     <li>Azure Resource ID - string with format
     *     {@code /subscriptions/<subscriptionId>/resourceGroups/<resourceGroup>/providers/microsoft
     *     .operationalinsights/workspaces/<workspaceName>}</li>
     * </ul>
     * @return the list of additional workspaces on which this query is executed.
     */
    public List<String> getAdditionalWorkspaces() {
        return additionalWorkspaces;
    }

    /**
     * Sets the list of additional workspaces on which this query is executed. The list can contain any of the
     * following workspace identifiers:
     * <ul>
     *     <li>Workspace Name - human-readable string {@code <workspaceName>} of the OMS workspace </li>
     *     <li>Qualified Name - string with format {@code <subscriptionName>/<resourceGroup>/<workspaceName>}</li>
     *     <li>Workspace ID - GUID string</li>
     *     <li>Azure Resource ID - string with format
     *     {@code /subscriptions/<subscriptionId>/resourceGroups/<resourceGroup>/providers/microsoft
     *     .operationalinsights/workspaces/<workspaceName>}</li>
     * </ul>
     * @param additionalWorkspaces the list of additional workspaces on which this query is executed.
     * @return The updated options instance.
     */
    public LogsQueryOptions setAdditionalWorkspaces(List<String> additionalWorkspaces) {
        this.additionalWorkspaces = additionalWorkspaces;
        return this;
    }

    /**
     * Sets the list of additional workspaces on which this query is executed. The list can contain any of the
     * following workspace identifiers:
     * <ul>
     *     <li>Workspace Name - human-readable string {@code <workspaceName>} of the OMS workspace </li>
     *     <li>Qualified Name - string with format {@code <subscriptionName>/<resourceGroup>/<workspaceName>}</li>
     *     <li>Workspace ID - GUID string</li>
     *     <li>Azure Resource ID - string with format
     *     {@code /subscriptions/<subscriptionId>/resourceGroups/<resourceGroup>/providers/microsoft
     *     .operationalinsights/workspaces/<workspaceName>}</li>
     * </ul>
     * @param additionalWorkspaces additional workspaces on which this query is executed.
     * @return The updated options instance.
     */
    public LogsQueryOptions setAdditionalWorkspaces(String... additionalWorkspaces) {
        this.additionalWorkspaces = Arrays.asList(additionalWorkspaces);
        return this;
    }
}
