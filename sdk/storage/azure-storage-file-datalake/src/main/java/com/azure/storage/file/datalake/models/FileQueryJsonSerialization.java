// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.models;

/**
 * Defines the input or output JSON serialization for a file query request.
 */
public class FileQueryJsonSerialization extends FileQuerySerialization {

    /**
     * Sets the record separator.
     * @param recordSeparator the record separator.
     * @return the updated FileQueryJsonSerialization object.
     */
    @Override
    public FileQueryJsonSerialization setRecordSeparator(char recordSeparator) {
        this.recordSeparator = recordSeparator;
        return this;
    }
}
