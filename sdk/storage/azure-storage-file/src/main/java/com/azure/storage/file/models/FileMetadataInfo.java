// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.models;

public class FileMetadataInfo {
    private final String eTag;
    private final Boolean isServerEncrypted;

    public FileMetadataInfo(final String eTag, final Boolean isServerEncrypted) {
        this.eTag = eTag;
        this.isServerEncrypted = isServerEncrypted;
    }

    public String eTag() {
        return eTag;
    }

    public Boolean isServerEncrypted() {
        return isServerEncrypted;
    }
}
