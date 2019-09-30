// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.models;

import java.time.OffsetDateTime;
import java.util.Map;

public class PathProperties {

    private final String eTag;
    private final OffsetDateTime lastModified;
    private final Map<String, String> metadata;
    private final String resourceType;
    private final String acceptRanges;
    private final String cacheControl;
    private final String contentDisposition;
    private final String contentEncoding;
    private final String contentLanguage;
    private final String contentLength;
    private final String contentRange;

    public PathProperties(final String eTag, final OffsetDateTime lastModified, final Map<String, String> metadata,
        final String resourceType, final String acceptRanges, final String cacheControl,
        final String contentDisposition, final String contentEncoding, final String contentLanguage,
        final String contentLength, final String contentRange) {
        this.eTag = eTag;
        this.lastModified = lastModified;
        this.metadata = metadata;
        this.resourceType = resourceType;
        this.acceptRanges = acceptRanges;
        this.cacheControl = cacheControl;
        this.contentDisposition = contentDisposition;
        this.contentEncoding = contentEncoding;
        this.contentLanguage = contentLanguage;
        this.contentLength = contentLength;
        this.contentRange = contentRange;
    }

    public String getETag() {
        return eTag;
    }

    public OffsetDateTime getLastModified() {
        return lastModified;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public String getResourceType() {
        return resourceType;
    }

    public String getAcceptRanges() {
        return acceptRanges;
    }

    public String getCacheControl() {
        return cacheControl;
    }

    public String getContentDisposition() {
        return contentDisposition;
    }

    public String getContentEncoding() {
        return contentEncoding;
    }

    public String getContentLanguage() {
        return contentLanguage;
    }

    public String getContentLength() {
        return contentLength;
    }

    public String getContentRange() {
        return contentRange;
    }
}
