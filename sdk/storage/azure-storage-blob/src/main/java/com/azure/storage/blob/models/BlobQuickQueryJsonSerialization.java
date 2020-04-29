// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

/**
 * Defines the input or output JSON serialization for a blob quick query request.
 */
public class BlobQuickQueryJsonSerialization extends BlobQuickQuerySerialization {

    /**
     * Sets the record separator.
     * @param recordSeparator the record separator.
     * @return the updated BlobQuickQueryJsonSerialization object.
     */
    @Override
    public BlobQuickQueryJsonSerialization setRecordSeparator(char recordSeparator) {
        this.recordSeparator = recordSeparator;
        return this;
    }
}
