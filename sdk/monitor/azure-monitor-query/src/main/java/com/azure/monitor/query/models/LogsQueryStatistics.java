// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query.models;

import com.azure.core.annotation.Immutable;

/**
 * The statistics information related to query execution.
 */
@Immutable
public final class LogsQueryStatistics {
    private final Object rawStatistics;

    /**
     * Creates an instance with the statistics information related to query execution.
     * @param rawStatistics the statistics information related to query execution.
     */
    public LogsQueryStatistics(Object rawStatistics) {
        this.rawStatistics = rawStatistics;
    }

    /**
     * Returns the raw statistics information related to query execution as JSON object.
     * @return the raw statistics information related to query execution as JSON object.
     */
    public Object getRawStatistics() {
        return rawStatistics;
    }
}
