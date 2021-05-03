// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query.models;

import com.azure.core.annotation.Fluent;

import java.time.Duration;
import java.util.List;

/**
 *
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
     * @param workspaceId
     * @param query
     * @param timeSpan
     */
    public LogsQueryOptions(String workspaceId, String query, QueryTimeSpan timeSpan) {
        this.workspaceId = workspaceId;
        this.query = query;
        this.timeSpan = timeSpan;
    }

    /**
     * @return
     */
    public Duration getServerTimeout() {
        return serverTimeout;
    }

    /**
     * @param serverTimeout
     *
     * @return
     */
    public LogsQueryOptions setServerTimeout(Duration serverTimeout) {
        this.serverTimeout = serverTimeout;
        return this;
    }

    /**
     * @return
     */
    public boolean isIncludeRendering() {
        return includeRendering;
    }

    /**
     * @param includeRendering
     *
     * @return
     */
    public LogsQueryOptions setIncludeRendering(boolean includeRendering) {
        this.includeRendering = includeRendering;
        return this;
    }

    /**
     * @return
     */
    public boolean isIncludeStatistics() {
        return includeStatistics;
    }

    /**
     * @param includeStatistics
     *
     * @return
     */
    public LogsQueryOptions setIncludeStatistics(boolean includeStatistics) {
        this.includeStatistics = includeStatistics;
        return this;
    }

    /**
     * @return
     */
    public List<String> getWorkspaceNames() {
        return workspaceNames;
    }

    /**
     * @param workspaceNames
     * @return
     */
    public LogsQueryOptions setWorkspaceNames(List<String> workspaceNames) {
        this.workspaceNames = workspaceNames;
        return this;
    }

    /**
     * @return
     */
    public List<String> getWorkspaceIds() {
        return workspaceIds;
    }

    /**
     * @param workspaceIds
     * @return
     */
    public LogsQueryOptions setWorkspaceIds(List<String> workspaceIds) {
        this.workspaceIds = workspaceIds;
        return this;
    }

    /**
     * @return
     */
    public List<String> getAzureResourceIds() {
        return azureResourceIds;
    }

    /**
     * @param azureResourceIds
     * @return
     */
    public LogsQueryOptions setAzureResourceIds(List<String> azureResourceIds) {
        this.azureResourceIds = azureResourceIds;
        return this;
    }

    /**
     * @return
     */
    public List<String> getQualifiedWorkspaceNames() {
        return qualifiedWorkspaceNames;
    }

    /**
     * @param qualifiedWorkspaceNames
     * @return
     */
    public LogsQueryOptions setQualifiedWorkspaceNames(List<String> qualifiedWorkspaceNames) {
        this.qualifiedWorkspaceNames = qualifiedWorkspaceNames;
        return this;
    }

    /**
     * @return
     */
    public String getWorkspaceId() {
        return workspaceId;
    }

    /**
     * @return
     */
    public String getQuery() {
        return query;
    }

    /**
     * @return
     */
    public QueryTimeSpan getTimeSpan() {
        return timeSpan;
    }
}
