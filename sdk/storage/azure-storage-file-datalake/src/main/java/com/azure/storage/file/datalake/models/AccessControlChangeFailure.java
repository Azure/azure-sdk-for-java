// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.models;

/**
 * Represents an entry that failed to update Access Control List.
 */
public class AccessControlChangeFailure {
    private String name;
    private boolean isDirectory;
    private String errorMessage;

    /**
     * Returns the name of an entry.
     *
     * @return The name of an entry.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of an entry.
     *
     * @param name The name of an entry.
     * @return The updated object.
     */
    public AccessControlChangeFailure setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Returns whether entry is a directory.
     *
     * @return Whether the entry is a directory.
     */
    public boolean isDirectory() {
        return isDirectory;
    }

    /**
     * Sets whether entry is a directory.
     *
     * @param directory Whether entry is a directory.
     * @return The updated object.
     */
    public AccessControlChangeFailure setDirectory(boolean directory) {
        isDirectory = directory;
        return this;
    }

    /**
     * Returns error message that is the reason why entry failed to update.
     *
     * @return The error message that is the reason why entry failed to update.
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Sets the error message that is the reason why entry failed to update.
     *
     * @param errorMessage The error message that is the reason why entry failed to update.
     * @return The updated object.
     */
    public AccessControlChangeFailure setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        return this;
    }
}
