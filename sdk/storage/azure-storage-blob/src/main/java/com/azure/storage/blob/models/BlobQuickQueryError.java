// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

/**
 * Defines a blob quick query error that can be returned on parsing a blob quick query request.
 */
public class BlobQuickQueryError {

    private boolean fatal;
    private String name;
    private String description;
    private long position;

    /**
     * Creates a new BlobQuickQueryError object.
     * @param fatal Whether or not the error is fatal.
     * @param name The name of the error.
     * @param description A description of the error.
     * @param position The blob offset at which the error occurred.
     */
    public BlobQuickQueryError(boolean fatal, String name, String description, long position) {
        this.fatal = fatal;
        this.name = name;
        this.description = description;
        this.position = position;
    }

    /**
     * Whether or not the error is fatal. If true, this error prevents further query processing. More result data may
     * be returned, but there is no guarantee that all of the original data will be processed. If false, this error
     * does not prevent further query processing.
     * @return Whether or not the error is fatal.
     */
    public boolean isFatal() {
        return fatal;
    }

    /**
     * @return The name of the error.
     */
    public String getName() {
        return name;
    }

    /**
     *
     * @return A description of the error.
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return The blob offset at which the error occurred.
     */
    public long getPosition() {
        return position;
    }

    @Override
    public String toString() {
        return String.format("BlobQuickQueryError. fatality = %b, name = %s, description = %s, position (in source "
            + "blob) = %d.", this.fatal, this.name, this.description, this.position);
    }

}
