// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.models;

import com.azure.core.implementation.util.ImplUtils;
import java.time.OffsetDateTime;

public final class FileUploadInfo {
    private String eTag;
    private OffsetDateTime lastModified;
    private byte[] contentMD5;
    private Boolean isServerEncrypted;

    public FileUploadInfo(final String eTag, final OffsetDateTime lastModified, final byte[] contentMD5, final Boolean isServerEncrypted) {
        this.eTag = eTag;
        this.lastModified = lastModified;
        this.contentMD5 = ImplUtils.clone(contentMD5);
        this.isServerEncrypted = isServerEncrypted;
    }

    public String eTag() {
        return eTag;
    }

    public OffsetDateTime lastModified() {
        return lastModified;
    }

    public byte[] contentMD5() {
        return ImplUtils.clone(contentMD5);
    }

    public Boolean isServerEncrypted() {
        return isServerEncrypted;
    }
}
