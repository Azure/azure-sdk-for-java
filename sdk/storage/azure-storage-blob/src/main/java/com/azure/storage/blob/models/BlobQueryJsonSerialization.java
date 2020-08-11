// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

/**
 * Defines the input or output JSON serialization for a blob quick query request.
 */
public class BlobQueryJsonSerialization implements BlobQuerySerialization {

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
     * @return the updated BlobQueryJsonSerialization object.
     */
    public BlobQueryJsonSerialization setRecordSeparator(char recordSeparator) {
        this.recordSeparator = recordSeparator;
        return this;
    }
}
