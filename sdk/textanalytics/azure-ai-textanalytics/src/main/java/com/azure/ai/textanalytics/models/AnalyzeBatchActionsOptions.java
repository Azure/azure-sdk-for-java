// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Fluent;

/**
 * The {@link AnalyzeBatchActionsOptions} model.
 */
@Fluent
public final class AnalyzeBatchActionsOptions {
    private boolean includeStatistics;

    /**
     * Get the value of {@code includeStatistics}.
     *
     * @return The value of {@code includeStatistics}.
     */
    public boolean isIncludeStatistics() {
        return includeStatistics;
    }

    /**
     * Set the value of {@code includeStatistics}. If set to true, indicates that the service
     * should return document and document batch statistics with the results of the operation.
     *
     * @param includeStatistics If a boolean value was specified in the request this field will contain
     * information about the document payload.
     *
     * @return the {@link AnalyzeBatchActionsOptions} object itself.
     */
    public AnalyzeBatchActionsOptions setIncludeStatistics(boolean includeStatistics) {
        this.includeStatistics = includeStatistics;
        return this;
    }
}
