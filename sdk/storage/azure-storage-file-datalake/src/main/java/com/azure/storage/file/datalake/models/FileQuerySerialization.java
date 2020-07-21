// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.models;

/**
 * Defines the input and output serialization for a file query request.
 * either {@link FileQueryJsonSerialization} or {@link FileQueryDelimitedSerialization}
 */
public abstract class FileQuerySerialization {

    private char recordSeparator;

    /**
     * Gets the record separator.
     *
     * @return the record separator.
     */
    public char getRecordSeparator() {
        return recordSeparator;
    }

    /**
     * Sets the record separator.
     * @param recordSeparator the record separator.
     * @return the updated FileQuerySerialization object.
     */
    public FileQuerySerialization setRecordSeparator(char recordSeparator) {
        this.recordSeparator = recordSeparator;
        return this;
    }
}
