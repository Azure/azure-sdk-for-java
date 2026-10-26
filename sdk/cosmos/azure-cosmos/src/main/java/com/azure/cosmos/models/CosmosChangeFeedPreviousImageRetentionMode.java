// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.util.Beta;

/**
 * Specifies which operations retain previous item images for the all versions and deletes change feed.
 * These settings contribute to the container's effective retention policy. Other features or account-level
 * configuration can also enable previous image capture.
 *
 * @see CosmosContainerProperties#setChangeFeedPreviousImageRetentionMode(CosmosChangeFeedPreviousImageRetentionMode)
 */
@Beta(value = Beta.SinceVersion.V4_84_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
public enum CosmosChangeFeedPreviousImageRetentionMode {
    /**
     * Does not request previous image capture for the all versions and deletes change feed.
     * This does not disable capture enabled by other features or account-level configuration.
     */
    DISABLED(0),

    /**
     * Retains previous images for replace and patch operations, and for upsert operations that update an existing item.
     * Creating an item, including through upsert, has no previous image.
     */
    ENABLED_FOR_REPLACE_OPERATIONS(1),

    /**
     * Retains previous images for delete operations.
     */
    ENABLED_FOR_DELETE_OPERATIONS(2),

    /**
     * Retains previous images for replace, patch, updating upsert, and delete operations.
     * Creating an item has no previous image.
     */
    ENABLED_FOR_ALL_OPERATIONS(3);

    private final int value;

    CosmosChangeFeedPreviousImageRetentionMode(int value) {
        this.value = value;
    }

    int getValue() {
        return this.value;
    }

    static CosmosChangeFeedPreviousImageRetentionMode fromValue(int value) {
        for (CosmosChangeFeedPreviousImageRetentionMode mode : values()) {
            if (mode.value == value) {
                return mode;
            }
        }
        throw new IllegalStateException("Unsupported change feed previous image retention mode: " + value);
    }
}
