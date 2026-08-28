// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

import com.azure.core.annotation.Immutable;

/**
 * Describes a page of the physical layout of a blob.
 *
 * <p>The layout information contains the byte ranges and response metadata. The continuation properties are used when
 * the service returns a paginated layout.</p>
 */
@Immutable
public final class BlobLayout {
    private final String marker;
    private final String nextMarker;
    private final Integer maxResults;
    private final BlobLayoutInfo blobLayoutInfo;

    /**
     * Creates a blob layout.
     *
     * @param marker The continuation marker used for this request.
     * @param nextMarker The marker to use to retrieve the next page.
     * @param maxResults The maximum number of ranges returned per request.
     * @param blobLayoutInfo The blob layout ranges and response metadata.
     */
    public BlobLayout(String marker, String nextMarker, Integer maxResults, BlobLayoutInfo blobLayoutInfo) {
        this.marker = marker;
        this.nextMarker = nextMarker;
        this.maxResults = maxResults;
        this.blobLayoutInfo = blobLayoutInfo;
    }

    /**
     * Gets the continuation marker used for this request.
     *
     * @return The continuation marker.
     */
    public String getMarker() {
        return marker;
    }

    /**
     * Gets the marker to use to retrieve the next page.
     *
     * @return The next marker.
     */
    public String getNextMarker() {
        return nextMarker;
    }

    /**
     * Gets the maximum number of ranges returned per request.
     *
     * @return The maximum number of ranges.
     */
    public Integer getMaxResults() {
        return maxResults;
    }

    /**
     * Gets the blob layout ranges and response metadata associated with this layout.
     *
     * @return The blob layout ranges and response metadata, or {@code null} when unavailable.
     */
    public BlobLayoutInfo getBlobLayoutInfo() {
        return blobLayoutInfo;
    }
}
