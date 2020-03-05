// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.quickquery.models;

/**
 * Defines the input and output serialization for a blob quick query request.
 * @param <T> either {@link BlobQuickQueryJsonSerialization} or {@link BlobQuickQueryDelimitedSerialization}
 */
public class BlobQuickQuerySerialization <T extends BlobQuickQuerySerialization<T>> {

    private char recordSeparator;

    /**
     * Gets the record separator.
     * @return the record separator.
     */
    public char getRecordSeparator() {
        return recordSeparator;
    }

    /**
     * Sets the record separator.
     * @param recordSeparator the record separator.
     * @return the updated BlobQuickQuerySerialization object.
     */
    public T setRecordSeparator(char recordSeparator) {
        this.recordSeparator = recordSeparator;
        return (T) this;
    }

}
