// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.models;

/**
 * Defines the input and output serialization for a file query request.
 * either {@link FileQueryJsonSerialization} or {@link FileQueryDelimitedSerialization}
 */
public abstract class FileQuerySerialization {

    protected char recordSeparator;

    /**
     * Gets the record separator.
     *
     * @return the record separator.
     */
    public char getRecordSeparator() {
        return recordSeparator;
    }

    abstract FileQuerySerialization setRecordSeparator(char recordSeparator);
}
