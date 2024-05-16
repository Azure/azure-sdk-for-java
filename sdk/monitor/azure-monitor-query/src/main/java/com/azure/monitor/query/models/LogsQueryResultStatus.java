// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query.models;

/**
 * Enum to indicate the status of a logs query result.
 */
public enum LogsQueryResultStatus {
    /**
     * Successful logs query result.
     */
    SUCCESS,

    /**
     * Unsuccessful logs query result.
     */
    FAILURE,

    /**
     * Partially successful logs query result.
     */
    PARTIAL_FAILURE;
}
