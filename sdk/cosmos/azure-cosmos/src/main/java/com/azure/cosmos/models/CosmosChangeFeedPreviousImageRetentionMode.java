// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.util.Beta;

/**
 * Specifies which operations retain previous item images for the all versions and deletes change feed.
 * These settings contribute to the container's effective retention policy. Other features or account-level
 * configuration can also enable previous image capture.
 * <p>
 * Use the predefined constants on this class to select a mode. This is not a Java enum, allowing additional
 * modes to be introduced without requiring exhaustive enum switches in application code.
 *
 * @see CosmosContainerProperties#setChangeFeedPreviousImageRetentionMode(CosmosChangeFeedPreviousImageRetentionMode)
 */
@Beta(value = Beta.SinceVersion.V4_84_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
public final class CosmosChangeFeedPreviousImageRetentionMode {
    /**
     * Does not request previous image capture for the all versions and deletes change feed.
     * This does not disable capture enabled by other features or account-level configuration.
     */
    public static final CosmosChangeFeedPreviousImageRetentionMode DISABLED
        = new CosmosChangeFeedPreviousImageRetentionMode("DISABLED", 0);

    /**
     * Retains previous images for replace and patch operations, and for upsert operations that update an existing item.
     * Creating an item, including through upsert, has no previous image.
     */
    public static final CosmosChangeFeedPreviousImageRetentionMode ENABLED_FOR_REPLACE_OPERATIONS
        = new CosmosChangeFeedPreviousImageRetentionMode("ENABLED_FOR_REPLACE_OPERATIONS", 1);

    /**
     * Retains previous images for delete operations.
     */
    public static final CosmosChangeFeedPreviousImageRetentionMode ENABLED_FOR_DELETE_OPERATIONS
        = new CosmosChangeFeedPreviousImageRetentionMode("ENABLED_FOR_DELETE_OPERATIONS", 2);

    /**
     * Retains previous images for replace, patch, updating upsert, and delete operations.
     * Creating an item has no previous image.
     */
    public static final CosmosChangeFeedPreviousImageRetentionMode ENABLED_FOR_ALL_OPERATIONS
        = new CosmosChangeFeedPreviousImageRetentionMode("ENABLED_FOR_ALL_OPERATIONS", 3);

    private final String name;
    private final int value;

    private CosmosChangeFeedPreviousImageRetentionMode(String name, int value) {
        this.name = name;
        this.value = value;
    }

    /**
     * Gets the name of this retention mode.
     *
     * @return the retention mode name.
     */
    @Override
    public String toString() {
        return this.name;
    }

    int getValue() {
        return this.value;
    }

    static CosmosChangeFeedPreviousImageRetentionMode fromValue(int value) {
        switch (value) {
            case 0:
                return DISABLED;
            case 1:
                return ENABLED_FOR_REPLACE_OPERATIONS;
            case 2:
                return ENABLED_FOR_DELETE_OPERATIONS;
            case 3:
                return ENABLED_FOR_ALL_OPERATIONS;
            default:
                throw new IllegalStateException("Unsupported change feed previous image retention mode: " + value);
        }
    }
}
