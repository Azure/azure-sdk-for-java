// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.models;

import io.netty.buffer.ByteBuf;
import java.time.OffsetDateTime;
import java.util.Map;
import reactor.core.publisher.Flux;

/**
 * Contains download information about a File in the storage File service.
 */
public final class FileDownloadInfo {
    private final String eTag;
    private final OffsetDateTime lastModified;
    private final Map<String, String> metadata;
    private final Long contentLength;
    private final String contentType;
    private final String contentRange;
    private final Flux<ByteBuf> body;

    /**
     * Creates an instance of download information about a specific File.
     *
     * @param eTag Entity tag that corresponds to the directory.
     * @param lastModified Last time the directory was modified.
     * @param metadata A set of name-value pairs associated with this file as user-defined metadata.
     * @param contentLength The number of bytes present in the response body.
     * @param contentType The content type specified for the file. The default content type is application/octet-stream.
     * @param contentRange Indicates the range of bytes returned if the client requested a subset of the file by setting the Range request header.
     * @param body The download request body.
     */
    public FileDownloadInfo(final String eTag, final OffsetDateTime lastModified, final Map<String, String> metadata, final Long contentLength, final String contentType, final String contentRange, final Flux<ByteBuf> body) {
        this.eTag = eTag;
        this.lastModified = lastModified;
        this.metadata = metadata;
        this.contentLength = contentLength;
        this.contentType = contentType;
        this.contentRange = contentRange;
        this.body = body;
    }

    /**
     * @return Entity tag that corresponds to the directory.
     */
    public String eTag() {
        return eTag;
    }

    /**
     * @return Last time the directory was modified.
     */
    public OffsetDateTime lastModified() {
        return lastModified;
    }

    /**
     * @return A set of name-value pairs associated with this file as user-defined metadata.
     */
    public Map<String, String> metadata() {
        return metadata;
    }

    /**
     * @return The number of bytes present in the response body.
     */
    public Long contentLength() {
        return contentLength;
    }

    /**
     * @return The content type specified for the file. The default content type is application/octet-stream.
     */
    public String contentType() {
        return contentType;
    }

    /**
     * @return The range of bytes returned if the client requested a subset of the file by setting the Range request header.
     */
    public String contentRange() {
        return contentRange;
    }

    /**
     * @return The download request body.
     */
    public Flux<ByteBuf> body() {
        return body;
    }
}
