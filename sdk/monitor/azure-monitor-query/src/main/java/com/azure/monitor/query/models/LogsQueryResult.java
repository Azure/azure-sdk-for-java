// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query.models;

import com.azure.core.annotation.Immutable;

import java.util.List;

/**
 * The result of a logs query.
 */
@Immutable
public final class LogsQueryResult {
    private final List<LogsTable> logsTables;

    /**
     * Creates an instance {@link LogsQueryResult} with a list of {@link LogsTable}.
     * @param logsTables The list of {@link LogsTable} returned as query result.
     */
    public LogsQueryResult(List<LogsTable> logsTables) {
        this.logsTables = logsTables;
    }

    /**
     * The list of {@link LogsTable} returned as query result.
     * @return The list of {@link LogsTable} returned as query result.
     */
    public List<LogsTable> getLogsTables() {
        return logsTables;
    }
}
