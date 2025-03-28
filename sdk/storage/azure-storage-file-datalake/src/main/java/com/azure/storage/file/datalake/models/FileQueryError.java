// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.models;

/**
 * Defines a file query error that can be returned on parsing a file query request.
 */
public class FileQueryError {
    private final boolean fatal;
    private final String name;
    private final String description;
    private final long position;

    /**
     * Creates a new FileQueryError object.
     * @param fatal Whether the error is fatal.
     * @param name The name of the error.
     * @param description A description of the error.
     * @param position The file offset at which the error occurred.
     */
    public FileQueryError(boolean fatal, String name, String description, long position) {
        this.fatal = fatal;
        this.name = name;
        this.description = description;
        this.position = position;
    }

    /**
     * Whether the error is fatal. If true, this error prevents further query processing. More result data may be
     * returned, but there is no guarantee that all the original data will be processed. If false, this error does not
     * prevent further query processing.
     *
     * @return Whether the error is fatal.
     */
    public boolean isFatal() {
        return fatal;
    }

    /**
     * Gets the name of the error.
     *
     * @return The name of the error.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets a description of the error.
     *
     * @return A description of the error.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets the file offset at which the error occurred.
     *
     * @return The file offset at which the error occurred.
     */
    public long getPosition() {
        return position;
    }

    @Override
    public String toString() {
        return String.format("QueryError. fatality = %b, name = %s, description = %s, position (in source) = %d.",
            this.fatal, this.name, this.description, this.position);
    }

}
