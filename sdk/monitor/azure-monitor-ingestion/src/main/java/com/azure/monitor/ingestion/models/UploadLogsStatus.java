// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.ingestion.models;

/**
 * Enum to indicate the status of a logs upload operation.
 */
public enum UploadLogsStatus {
    /**
     * Indicates that all the logs were successfully uploaded to Azure Monitor.
     */
    SUCCESS,
    /**
     * Indicates that some logs failed to upload to Azure Monitor.
     */
    PARTIAL_FAILURE,
    /**
     * Indicates that all the logs failed to upload to Azure Monitor.
     */
    FAILURE;
}
