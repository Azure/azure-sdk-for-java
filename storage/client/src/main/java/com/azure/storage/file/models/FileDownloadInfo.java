// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.models;

import io.netty.buffer.ByteBuf;
import java.time.OffsetDateTime;
import java.util.Map;
import reactor.core.publisher.Flux;

public final class FileDownloadInfo {
    private String eTag;
    private OffsetDateTime lastModified;
    private Map<String, String> metadata;
    private Long contentLength;
    private String contentType;
    private String contentRange;
    private Flux<ByteBuf> body;

    public FileDownloadInfo(final String eTag, final OffsetDateTime lastModified, final Map<String, String> metadata, final Long contentLength, final String contentType, final String contentRange, final Flux<ByteBuf> body) {
        this.eTag = eTag;
        this.lastModified = lastModified;
        this.metadata = metadata;
        this.contentLength = contentLength;
        this.contentType = contentType;
        this.contentRange = contentRange;
        this.body = body;
    }

    public String eTag() {
        return eTag;
    }

    public FileDownloadInfo eTag(final String eTag) {
        this.eTag = eTag;
        return this;
    }

    public OffsetDateTime lastModified() {
        return lastModified;
    }

    public FileDownloadInfo lastModified(final OffsetDateTime lastModified) {
        this.lastModified = lastModified;
        return this;
    }

    public Map<String, String> metadata() {
        return metadata;
    }

    public FileDownloadInfo metadata(final Map<String, String> metadata) {
        this.metadata = metadata;
        return this;
    }

    public Long contentLength() {
        return contentLength;
    }

    public FileDownloadInfo contentLength(final Long contentLength) {
        this.contentLength = contentLength;
        return this;
    }

    public String contentType() {
        return contentType;
    }

    public FileDownloadInfo contentType(final String contentType) {
        this.contentType = contentType;
        return this;
    }

    public String contentRange() {
        return contentRange;
    }

    public FileDownloadInfo contentRange(final String contentRange) {
        this.contentRange = contentRange;
        return this;
    }

    public Flux<ByteBuf> body() {
        return body;
    }

    public FileDownloadInfo body(final Flux<ByteBuf> body) {
        this.body = body;
        return this;
    }
}
