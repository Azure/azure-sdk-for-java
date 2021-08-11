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
    private boolean includeRendering;
    private boolean includeStatistics;
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
}
