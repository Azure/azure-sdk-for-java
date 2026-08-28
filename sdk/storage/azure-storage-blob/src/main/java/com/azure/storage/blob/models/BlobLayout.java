// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

import com.azure.core.annotation.Immutable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Describes a page of the physical layout of a blob.
 *
 * <p>Each range identifies the byte interval and endpoint that serves it. The continuation properties are used when
 * the service returns a paginated layout.</p>
 */
@Immutable
public final class BlobLayout {
    private final List<BlobLayoutRange> ranges;
    private final String marker;
    private final String nextMarker;
    private final Integer maxResults;
    private final BlobProperties blobProperties;

    /**
     * Creates a blob layout.
     *
     * @param ranges The ranges in the layout.
     * @param marker The continuation marker used for this request.
     * @param nextMarker The marker to use to retrieve the next page.
     * @param maxResults The maximum number of ranges returned per request.
     * @param blobProperties The blob properties returned with the layout.
     */
    public BlobLayout(List<BlobLayoutRange> ranges, String marker, String nextMarker, Integer maxResults,
        BlobProperties blobProperties) {
        this.ranges = ranges == null ? Collections.emptyList() : Collections.unmodifiableList(new ArrayList<>(ranges));
        this.marker = marker;
        this.nextMarker = nextMarker;
        this.maxResults = maxResults;
        this.blobProperties = blobProperties;
    }

    /**
     * Gets the ranges in the layout.
     *
     * @return The ranges in the layout.
     */
    public List<BlobLayoutRange> getRanges() {
        return ranges;
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
     * Gets the blob properties associated with this layout.
     *
     * @return The blob properties, or {@code null} when unavailable.
     */
    public BlobProperties getBlobProperties() {
        return blobProperties;
    }
}
