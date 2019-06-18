// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.models;

import java.time.OffsetDateTime;
import java.util.Map;

public final class FileDownloadInfo {
    private String eTag;
    private OffsetDateTime lastModified;
    private Map<String, String> metadata;
    private Long contentLength;
    private String contentType;
    private String contentRange;

    public FileDownloadInfo(final String eTag, final OffsetDateTime lastModified, final Map<String, String> metadata, final Long contentLength, final String contentType, final String contentRange) {
        this.eTag = eTag;
        this.lastModified = lastModified;
        this.metadata = metadata;
        this.contentLength = contentLength;
        this.contentType = contentType;
        this.contentRange = contentRange;
    }

    public String eTag() {
        return eTag;
    }

    public String eTag(final String eTag) {
        this.eTag = eTag;
        return this.eTag;
    }

    public OffsetDateTime lastModified() {
        return lastModified;
    }

    public OffsetDateTime lastModified(final OffsetDateTime lastModified) {
        this.lastModified = lastModified;
        return this.lastModified;
    }

    public Map<String, String> metadata() {
        return metadata;
    }

    public Map<String, String> metadata(final Map<String, String> metadata) {
        this.metadata = metadata;
        return this.metadata;
    }

    public Long contentLength() {
        return contentLength;
    }

    public Long contentLength(final Long contentLength) {
        this.contentLength = contentLength;
        return this.contentLength;
    }

    public String contentType() {
        return contentType;
    }

    public String contentType(final String contentType) {
        this.contentType = contentType;
        return this.contentType;
    }

    public String contentRange() {
        return contentRange;
    }

    public String contentRange(final String contentRange) {
        this.contentRange = contentRange;
        return this.contentRange;
    }
}
