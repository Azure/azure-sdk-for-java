// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.models;

import com.azure.core.implementation.util.ImplUtils;
import java.time.OffsetDateTime;

public final class FileCopyInfo {
    private String eTag;
    private OffsetDateTime lastModified;
    private String copyId;
    private CopyStatusType copyStatus;

    public FileCopyInfo(final String eTag, final OffsetDateTime lastModified, final String copyId, final CopyStatusType copyStatus) {
        this.eTag = eTag;
        this.lastModified = lastModified;
        this.copyId = copyId;
        this.copyStatus = copyStatus;
    }

    public String eTag() {
        return eTag;
    }

    public FileCopyInfo eTag(final String eTag) {
        this.eTag = eTag;
        return this;
    }

    public OffsetDateTime lastModified() {
        return lastModified;
    }

    public FileCopyInfo lastModified(final OffsetDateTime lastModified) {
        this.lastModified = lastModified;
        return this;
    }

    public String copyId() {
        return copyId;
    }

    public FileCopyInfo copyId(final String copyId) {
        this.copyId = copyId;
        return this;
    }

    public CopyStatusType copyStatus() {
        return copyStatus;
    }

    public FileCopyInfo copyStatus(final CopyStatusType copyStatus) {
        this.copyStatus = copyStatus;
        return this;
    }
}
