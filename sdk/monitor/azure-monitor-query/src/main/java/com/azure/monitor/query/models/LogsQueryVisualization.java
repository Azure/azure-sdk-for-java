// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query.models;

/**
 * The visualization information related to query execution.
 */
public class LogsQueryVisualization {
    private final Object rawVisualization;

    /**
     * Creates an instance with the visualization information related to query execution.
     * @param rawVisualization the visualization information related to query execution.
     */
    public LogsQueryVisualization(Object rawVisualization) {
        this.rawVisualization = rawVisualization;
    }

    /**
     * Returns the raw statistics information related to query execution as JSON object.
     * @return the raw statistics information related to query execution as JSON object.
     */
    public Object getRawVisualization() {
        return rawVisualization;
    }
}
