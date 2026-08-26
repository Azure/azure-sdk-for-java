// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.util;

import com.azure.storage.blob.models.BlobLayoutRange;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.implementation.util.AutoRefreshingCache;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;

/**
 * The value cached by the data locality layout cache used by {@link ChunkedDownloadUtils} to route range downloads
 * to the optimal endpoint.
 * <p>
 * {@link #getRanges()} has three meaningful states:
 * <ul>
 * <li>Non-null, non-empty &mdash; the ranges returned by {@code getLayout}; locality-aware routing applies.</li>
 * <li>Non-null, empty &mdash; the service returned no layout (for example, the blob is too small to have one);
 * cached for the full TTL so the download avoids re-requesting a layout that will never exist.</li>
 * <li>{@code null} &mdash; {@code getLayout} failed (for example, the service returned an error), or the value has
 * expired. Failed layouts are cached for the full TTL so the rest of the download avoids repeatedly retrying a
 * known-bad layout endpoint.</li>
 * </ul>
 * In all cases other than the first, callers fall back to the blob's original endpoint.
 * <p>
 * RESERVED FOR INTERNAL USE.
 */
public final class BlobLayoutCacheValue implements AutoRefreshingCache.ExpiringValue {
    private final List<BlobLayoutRange> ranges;
    private final OffsetDateTime expiresOn;

    /**
     * Creates a new {@link BlobLayoutCacheValue} with the service layout lifetime.
     *
     * @param ranges The layout ranges, or an empty list if the service returned no layout, or {@code null} if
     * {@code getLayout} failed.
     */
    public BlobLayoutCacheValue(List<BlobLayoutRange> ranges) {
        this(ranges, OffsetDateTime.now().plus(Constants.DEFAULT_CACHE_LIFETIME));
    }

    /**
     * Creates a new {@link BlobLayoutCacheValue}.
     *
     * @param ranges The layout ranges, or an empty list if the service returned no layout, or {@code null} if
     * {@code getLayout} failed.
     * @param expiresOn The time at which this cached value should no longer be considered valid.
     */
    public BlobLayoutCacheValue(List<BlobLayoutRange> ranges, OffsetDateTime expiresOn) {
        this.ranges = ranges == null ? null : Collections.unmodifiableList(ranges);
        this.expiresOn = expiresOn;
    }

    /**
     * Gets the layout ranges.
     *
     * @return The layout ranges, or an empty list if the service returned no layout, or {@code null} if
     * {@code getLayout} failed or the value has expired.
     */
    public List<BlobLayoutRange> getRanges() {
        return OffsetDateTime.now().isAfter(expiresOn) ? null : ranges;
    }

    /**
     * Gets the time at which this cached value should no longer be considered valid.
     *
     * @return The expiration time.
     */
    public OffsetDateTime getExpiresOn() {
        return expiresOn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OffsetDateTime getExpiration() {
        return expiresOn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OffsetDateTime getRefreshOn() {
        /*
         * Let AutoRefreshingCache choose its default jittered refresh time. For the five-minute layout TTL this
         * distributes refreshes between 90 and 30 seconds before expiration, which prevents synchronized getLayout
         * refresh stampedes while preserving a 30-second safety margin before the service-side layout expires.
         */
        return null;
    }
}
