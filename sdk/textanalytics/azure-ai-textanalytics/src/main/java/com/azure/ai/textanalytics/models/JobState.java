// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

/**
 * Operation job's states
 */
public enum JobState {
    /** Enum value notstarted. */
    NOT_STARTED,

    /** Enum value running. */
    RUNNING,

    /** Enum value succeeded. */
    SUCCEEDED,

    /** Enum value failed. */
    FAILED,

    /** Enum value cancelled. */
    CANCELLED,

    /** Enum value cancelling. */
    CANCELLING,

    /** Enum value partiallycompleted. */
    PARTIALLY_COMPLETED;
}
