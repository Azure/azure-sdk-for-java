// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query;

import java.time.Duration;

public class QueryLogsRequestOptions {
    private Duration serverTimeout;
    private boolean includeRendering;
    private boolean includeStatistics;

    public Duration getServerTimeout() {
        return serverTimeout;
    }

    public QueryLogsRequestOptions setServerTimeout(Duration serverTimeout) {
        this.serverTimeout = serverTimeout;
        return this;
    }

    public boolean isIncludeRendering() {
        return includeRendering;
    }

    public QueryLogsRequestOptions setIncludeRendering(boolean includeRendering) {
        this.includeRendering = includeRendering;
        return this;
    }

    public boolean isIncludeStatistics() {
        return includeStatistics;
    }

    public QueryLogsRequestOptions setIncludeStatistics(boolean includeStatistics) {
        this.includeStatistics = includeStatistics;
        return this;
    }
}
