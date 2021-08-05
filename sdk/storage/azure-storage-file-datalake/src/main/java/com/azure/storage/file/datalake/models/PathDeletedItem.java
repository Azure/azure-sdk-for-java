// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.models;

import java.time.OffsetDateTime;

/**
 * A path that has been soft deleted.
 */
public class PathDeletedItem {
    private final String path;
    private final boolean isPrefix;
    private final String deletionId;
    private final OffsetDateTime deletedOn;
    private final Integer remainingRetentionDays;

    /**
     * Constructs a {@link PathDeletedItem}.
     *
     * @param path The name of the path
     * @param isPrefix Whether the item is a prefix
     * @param deletionId The deletion id associated with the deleted path to uniquely identify it from other items
     * deleted at this path
     * @param deletedOn When the path was deleted
     * @param remainingRetentionDays The number of days left before the soft deleted path will be permanently deleted
     */
    public PathDeletedItem(String path, boolean isPrefix, String deletionId, OffsetDateTime deletedOn,
        Integer remainingRetentionDays) {
        this.path = path;
        this.isPrefix = isPrefix;
        this.deletionId = deletionId;
        this.deletedOn = deletedOn;
        this.remainingRetentionDays = remainingRetentionDays;
    }

    /**
     * Gets the name of the path.
     *
     * @return the name of the path
     */
    public String getPath() {
        return path;
    }

    /**
     * Gets whether the item is a prefix.
     *
     * @return whether the item is a prefix
     */
    public boolean isPrefix() {
        return isPrefix;
    }

    /**
     * Gets the deletion id associated with the deleted path to uniquely identify it from other items deleted at this
     * path.
     *
     * @return the deletion id
     */
    public String getDeletionId() {
        return deletionId;
    }

    /**
     * Gets when the path was deleted.
     *
     * @return when the path was deleted
     */
    public OffsetDateTime getDeletedOn() {
        return deletedOn;
    }

    /**
     * Gets the number of days left before the soft deleted path will be permanently deleted.
     *
     * @return the number of days before permanent deletion
     */
    public Integer getRemainingRetentionDays() {
        return remainingRetentionDays;
    }
}
