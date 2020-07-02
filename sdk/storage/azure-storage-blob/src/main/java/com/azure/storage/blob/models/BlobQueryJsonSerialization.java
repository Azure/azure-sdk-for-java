// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

/**
 * Defines the input or output JSON serialization for a blob quick query request.
 */
public class BlobQueryJsonSerialization extends BlobQuerySerialization {

    @Override
    public BlobQueryJsonSerialization setRecordSeparator(char recordSeparator) {
        super.setRecordSeparator(recordSeparator);
        return this;
    }
}
