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
        this.contentMD5 = contentMD5;
        this.isServerEncrypted = isServerEncrypted;
    }

    public String eTag() {
        return eTag;
    }

    public FileUploadInfo eTag(final String eTag) {
        this.eTag = eTag;
        return this;
    }

    public OffsetDateTime lastModified() {
        return lastModified;
    }

    public FileUploadInfo lastModified(final OffsetDateTime lastModified) {
        this.lastModified = lastModified;
        return this;
    }

    public byte[] contentMD5() {
        return contentMD5;
    }

    public FileUploadInfo contentMD5(final byte[] contentMD5) {
        this.contentMD5 = ImplUtils.clone(contentMD5);
        return this;
    }

    public Boolean isServerEncrypted() {
        return isServerEncrypted;
    }

    public FileUploadInfo isServerEncrypted(final Boolean isServerEncrypted) {
        this.isServerEncrypted = isServerEncrypted;
        return this;
    }
}
