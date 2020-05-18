// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

/**
 * Defines the input and output serialization for a blob quick query request.
 * either {@link BlobQueryJsonSerialization} or {@link BlobQueryDelimitedSerialization}
 */
public abstract class BlobQuerySerialization {

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
     * @return the updated BlobQuerySerialization object.
     */
    public BlobQuerySerialization setRecordSeparator(char recordSeparator) {
        this.recordSeparator = recordSeparator;
        return this;
    }
}
