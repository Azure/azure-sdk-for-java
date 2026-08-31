// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.util;

import com.azure.core.http.HttpRange;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.UrlBuilder;
import com.azure.storage.blob.models.BlobLayoutRange;

import java.util.List;

/**
 * Resolves the optimal endpoint for a download chunk range from a blob's layout.
 * <p>
 * RESERVED FOR INTERNAL USE.
 */
public final class BlobLayoutRangeResolver {
    private static final int NOT_FOUND = -1;

    private BlobLayoutRangeResolver() {
    }

    /**
     * Gets the endpoint of the layout range that overlaps with the start of the given download chunk range.
     * <p>
     * {@code layoutRanges} is expected to be sorted in ascending order and non-overlapping, which is how
     * {@code getLayout} returns them. A binary search is used to find the first range whose end offset is at or
     * after {@code chunkRangeStart}, which -- given the layout covers the whole blob with no gaps -- will always be
     * the range that contains {@code chunkRangeStart}.
     *
     * @param chunkRangeStart The start offset, in bytes, of the download chunk to resolve an endpoint for.
     * @param layoutRanges The layout ranges to search, sorted in ascending order by offset. May be {@code null} or
     * empty, in which case there is no layout to route by and {@code null} is returned.
     * @return The endpoint to use for the chunk starting at {@code chunkRangeStart}, or {@code null} if
     * {@code layoutRanges} is {@code null}, empty, or (unexpectedly) does not cover {@code chunkRangeStart}.
     */
    public static String resolveEndpoint(long chunkRangeStart, List<BlobLayoutRange> layoutRanges) {
        if (layoutRanges == null || layoutRanges.isEmpty()) {
            return null;
        }

        int candidateIndex = findFirstRangeEndingAtOrAfter(layoutRanges, chunkRangeStart);

        // Should theoretically never happen since the layout returned by the service always covers the whole blob.
        if (candidateIndex == NOT_FOUND) {
            return null;
        }

        BlobLayoutRange candidate = layoutRanges.get(candidateIndex);
        if (!contains(candidate, chunkRangeStart)) {
            return null;
        }

        String endpoint = candidate.getEndpoint();
        if (!isUsableDataLocalityEndpoint(endpoint)) {
            // Layout data is an optimization for SDK-managed downloads; ETag conditions still guarantee correctness,
            // so ignore an unusable service-supplied endpoint instead of failing like caller-supplied policy input.
            return null;
        }

        return endpoint;
    }

    private static boolean isUsableDataLocalityEndpoint(String endpoint) {
        if (CoreUtils.isNullOrEmpty(endpoint) || endpoint.trim().isEmpty()) {
            return false;
        }

        UrlBuilder endpointUrlBuilder;
        try {
            endpointUrlBuilder = UrlBuilder.parse(endpoint);
        } catch (RuntimeException ex) {
            return false;
        }

        return endpointUrlBuilder != null && !CoreUtils.isNullOrEmpty(endpointUrlBuilder.getHost());
    }

    /**
     * Finds the index of the first range that ends at or after {@code offset}, or {@link #NOT_FOUND} if there is none.
     */
    private static int findFirstRangeEndingAtOrAfter(List<BlobLayoutRange> layoutRanges, long offset) {
        int low = 0;
        int high = layoutRanges.size() - 1;
        int firstMatch = NOT_FOUND;

        while (low <= high) {
            int mid = low + (high - low) / 2;
            if (endsAtOrAfter(layoutRanges.get(mid).getRange(), offset)) {
                // A match, but an earlier range may also end at or after the offset, so keep searching to the left.
                firstMatch = mid;
                high = mid - 1;
            } else {
                // This range ends before the offset, so every range left of it does too. Search to the right.
                low = mid + 1;
            }
        }

        return firstMatch;
    }

    /**
     * Determines whether {@code layoutRange} contains {@code offset}.
     */
    private static boolean contains(BlobLayoutRange layoutRange, long offset) {
        HttpRange range = layoutRange.getRange();
        boolean startsAtOrBefore = range.getOffset() <= offset;

        return startsAtOrBefore && endsAtOrAfter(range, offset);
    }

    /**
     * Determines whether {@code range} ends at or after {@code offset}.
     */
    private static boolean endsAtOrAfter(HttpRange range, long offset) {
        // The range begins after the offset, so it necessarily ends after it too.
        if (range.getOffset() > offset) {
            return true;
        }

        Long length = range.getLength();
        // Equivalent to offset < rangeOffset + length, rearranged so the sum can never overflow for a range near
        // Long.MAX_VALUE.
        return length == null || (offset - range.getOffset()) < length;
    }
}
