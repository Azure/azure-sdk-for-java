// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.models;

/**
 * AccessControlChangeCounters contains counts of operations that change Access Control Lists recursively.
 */
public class AccessControlChangeCounters {
    private long changedDirectoriesCount;
    private long changedFilesCount;
    private long failedChangesCount;

    /**
     *  The number of directories where Access Control List has been updated successfully.
     *
     * @return The number of directories where Access Control List has been updated successfully.
     */
    public long getChangedDirectoriesCount() {
        return this.changedDirectoriesCount;
    }

    /**
     * Sets the number of directories where Access Control List has been updated successfully.
     * @param changedDirectoriesCount The number of directories where Access Control List has been updated
     * successfully.
     * @return The updated object.
     */
    public AccessControlChangeCounters setChangedDirectoriesCount(long changedDirectoriesCount) {
        this.changedDirectoriesCount = changedDirectoriesCount;
        return this;
    }

    /**
     * Returns the number of files where Access Control List has been updated successfully.
     *
     * @return The number of files where Access Control List has been updated successfully.
     */
    public long getChangedFilesCount() {
        return this.changedFilesCount;
    }

    /**
     * Sets number of files where Access Control List has been updated successfully.
     *
     * @param changedFilesCount The number of files where Access Control List has been updated successfully.
     * @return The updated object
     */
    public AccessControlChangeCounters setChangedFilesCount(long changedFilesCount) {
        this.changedFilesCount = changedFilesCount;
        return this;
    }

    /**
     * Returns the number of paths where Access Control List update has failed.
     *
     * @return The number of paths where Access Control List update has failed.
     */
    public long getFailedChangesCount() {
        return failedChangesCount;
    }

    /**
     * Sets the number of paths where Access Control List update has failed.
     * @param failedChangesCount The number of paths where Access Control List update has failed.
     * @return The updated object.
     */
    public AccessControlChangeCounters setFailedChangesCount(long failedChangesCount) {
        this.failedChangesCount = failedChangesCount;
        return this;
    }
}
