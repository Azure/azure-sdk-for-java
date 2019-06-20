// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.models;

import com.azure.core.implementation.util.ImplUtils;
import java.time.OffsetDateTime;
import java.util.Map;

public final class FileProperties {
    private String eTag;
    private OffsetDateTime lastModified;
    private Map<String, String> metadata;
    private String fileType;
    private Long contentLength;
    private String contentType;
    private byte[] contentMD5;
    private String contentEncoding;
    private String cacheControl;
    private String contentDisposition;
    private OffsetDateTime copyCompletionTime;
    private String copyStatusDescription;
    private String copyId;
    private String copyProgress;
    private String copySource;
    private CopyStatusType copyStatus;
    private Boolean isServerEncrypted;

    public FileProperties(final String eTag, final OffsetDateTime lastModified, final Map<String, String> metadata,
                          final String fileType, final Long contentLength, final String contentType, final byte[] contentMD5,
                          final String contentEncoding, final String cacheControl, final String contentDisposition,
                          final OffsetDateTime copyCompletionTime, final String copyStatusDescription, final String copyId,
                          final String copyProgress, final String copySource, final CopyStatusType copyStatus, final Boolean isServerEncrypted) {
        this.eTag = eTag;
        this.lastModified = lastModified;
        this.metadata = metadata;
        this.fileType = fileType;
        this.contentLength = contentLength;
        this.contentType = contentType;
        this.contentMD5 = contentMD5;
        this.contentEncoding = contentEncoding;
        this.cacheControl = cacheControl;
        this.contentDisposition = contentDisposition;
        this.copyCompletionTime = copyCompletionTime;
        this.copyStatusDescription = copyStatusDescription;
        this.copyId = copyId;
        this.copyProgress = copyProgress;
        this.copySource = copySource;
        this.copyStatus = copyStatus;
        this.isServerEncrypted = isServerEncrypted;
    }

    public String eTag() {
        return eTag;
    }

    public FileProperties eTag(final String eTag) {
        this.eTag = eTag;
        return this;
    }

    public OffsetDateTime lastModified() {
        return lastModified;
    }

    public FileProperties lastModified(final OffsetDateTime lastModified) {
        this.lastModified = lastModified;
        return this;
    }

    public Map<String, String> metadata() {
        return metadata;
    }

    public FileProperties metadata(final Map<String, String> metadata) {
        this.metadata = metadata;
        return this;
    }

    public Long contentLength() {
        return contentLength;
    }

    public FileProperties contentLength(final Long contentLength) {
        this.contentLength = contentLength;
        return this;
    }

    public String contentType() {
        return contentType;
    }

    public FileProperties contentType(final String contentType) {
        this.contentType = contentType;
        return this;
    }

    public String fileType() {
        return fileType;
    }

    public FileProperties fileType(final String fileType) {
        this.fileType = fileType;
        return this;
    }

    public byte[] contentMD5() {
        return ImplUtils.clone(contentMD5);
    }

    public FileProperties contentMD5(final byte[] contentMD5) {
        this.contentMD5 = ImplUtils.clone(contentMD5);
        return this;
    }

    public String contentEncoding() {
        return contentEncoding;
    }

    public FileProperties contentEncoding(final String contentEncoding) {
        this.contentEncoding = contentEncoding;
        return this;
    }

    public String cacheControl() {
        return cacheControl;
    }

    public FileProperties cacheControl(final String cacheControl) {
        this.cacheControl = cacheControl;
        return this;
    }

    public String contentDisposition() {
        return contentDisposition;
    }

    public FileProperties contentDisposition(final String contentDisposition) {
        this.contentDisposition = contentDisposition;
        return this;
    }

    public OffsetDateTime copyCompletionTime() {
        return copyCompletionTime;
    }

    public FileProperties copyCompletionTime(final OffsetDateTime copyCompletionTime) {
        this.copyCompletionTime = copyCompletionTime;
        return this;
    }

    public String copyStatusDescription() {
        return copyStatusDescription;
    }

    public FileProperties copyStatusDescription(final String copyStatusDescription) {
        this.copyStatusDescription = copyStatusDescription;
        return this;
    }

    public String copyId() {
        return copyId;
    }

    public FileProperties copyId(final String copyId) {
        this.copyId = copyId;
        return this;
    }

    public String copyProgress() {
        return copyProgress;
    }

    public FileProperties copyProgress(final String copyProgress) {
        this.copyProgress = copyProgress;
        return this;
    }

    public String copySource() {
        return copySource;
    }

    public FileProperties copySource(final String copySource) {
        this.copySource = copySource;
        return this;
    }

    public CopyStatusType copyStatus() {
        return copyStatus;
    }

    public FileProperties copyStatus(final CopyStatusType copyStatus) {
        this.copyStatus = copyStatus;
        return this;
    }

    public Boolean isServerEncrypted() {
        return isServerEncrypted;
    }

    public FileProperties isServerEncrypted(final Boolean serverEncrypted) {
        isServerEncrypted = serverEncrypted;
        return this;
    }
}
