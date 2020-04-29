// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

/**
 * Defines the input and output serialization for a blob quick query request.
 * either {@link BlobQuickQueryJsonSerialization} or {@link BlobQuickQueryDelimitedSerialization}
 */
public abstract class BlobQuickQuerySerialization {

    protected char recordSeparator;

    /**
     * Gets the record separator.
     *
     * @return the record separator.
     */
    public char getRecordSeparator() {
        return recordSeparator;
    }

    abstract BlobQuickQuerySerialization setRecordSeparator(char recordSeparator);
}
