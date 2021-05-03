// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query.models;

import com.azure.core.annotation.Immutable;

import java.util.List;

/**
 *
 */
@Immutable
public final class LogsQueryResult {
    private final List<LogsTable> logsTables;

    /**
     * @param logsTables
     */
    public LogsQueryResult(List<LogsTable> logsTables) {
        this.logsTables = logsTables;
    }

    /**
     * @return
     */
    public List<LogsTable> getLogsTables() {
        return logsTables;
    }
}
