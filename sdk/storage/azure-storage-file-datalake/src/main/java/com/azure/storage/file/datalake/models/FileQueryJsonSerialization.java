// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.models;

/**
 * Defines the input or output JSON serialization for a file query request.
 */
public class FileQueryJsonSerialization extends FileQuerySerialization {

    @Override
    public FileQueryJsonSerialization setRecordSeparator(char recordSeparator) {
        super.setRecordSeparator(recordSeparator);
        return this;
    }
}
