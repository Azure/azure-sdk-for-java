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

    public String fileType() {
        return fileType;
    }

    public String fileType(final String fileType) {
        this.fileType = fileType;
        return this.fileType;
    }

    public byte[] contentMD5() {
        return ImplUtils.clone(contentMD5);
    }

    public byte[] contentMD5(final byte[] contentMD5) {
        this.contentMD5 = ImplUtils.clone(contentMD5);
        return this.contentMD5;
    }

    public String contentEncoding() {
        return contentEncoding;
    }

    public String contentEncoding(final String contentEncoding) {
        this.contentEncoding = contentEncoding;
        return this.contentEncoding;
    }

    public String cacheControl() {
        return cacheControl;
    }

    public String cacheControl(final String cacheControl) {
        this.cacheControl = cacheControl;
        return this.cacheControl;
    }

    public String contentDisposition() {
        return contentDisposition;
    }

    public String contentDisposition(final String contentDisposition) {
        this.contentDisposition = contentDisposition;
        return this.contentDisposition;
    }

    public OffsetDateTime copyCompletionTime() {
        return copyCompletionTime;
    }

    public OffsetDateTime copyCompletionTime(final OffsetDateTime copyCompletionTime) {
        this.copyCompletionTime = copyCompletionTime;
        return this.copyCompletionTime;
    }

    public String copyStatusDescription() {
        return copyStatusDescription;
    }

    public String copyStatusDescription(final String copyStatusDescription) {
        this.copyStatusDescription = copyStatusDescription;
        return this.copyStatusDescription;
    }

    public String copyId() {
        return copyId;
    }

    public String copyId(final String copyId) {
        this.copyId = copyId;
        return this.copyId;
    }

    public String copyProgress() {
        return copyProgress;
    }

    public String copyProgress(final String copyProgress) {
        this.copyProgress = copyProgress;
        return this.copyProgress;
    }

    public String copySource() {
        return copySource;
    }

    public String copySource(final String copySource) {
        this.copySource = copySource;
        return this.copySource;
    }

    public CopyStatusType copyStatus() {
        return copyStatus;
    }

    public CopyStatusType copyStatus(final CopyStatusType copyStatus) {
        this.copyStatus = copyStatus;
        return this.copyStatus;
    }

    public Boolean isServerEncrypted() {
        return isServerEncrypted;
    }

    public Boolean isServerEncrypted(final Boolean serverEncrypted) {
        isServerEncrypted = serverEncrypted;
        return this.isServerEncrypted;
    }
}
