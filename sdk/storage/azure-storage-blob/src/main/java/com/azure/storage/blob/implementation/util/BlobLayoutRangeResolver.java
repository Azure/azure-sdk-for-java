// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.util;

import com.azure.core.http.HttpRange;
import com.azure.storage.blob.models.BlobLayoutRange;

import java.util.List;

/**
 * Resolves the optimal endpoint for a download chunk range from a blob's layout.
 * <p>
 * RESERVED FOR INTERNAL USE.
 */
public final class BlobLayoutRangeResolver {
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

        int low = 0;
        int high = layoutRanges.size() - 1;
        int overlapIndex = layoutRanges.size();
        while (low <= high) {
            int mid = low + (high - low) / 2;
            if (rangeEnd(layoutRanges.get(mid).getRange()) >= chunkRangeStart) {
                overlapIndex = mid;
                high = mid - 1;
            } else {
                low = mid + 1;
            }
        }

        // Should theoretically never happen since the layout returned by the service always covers the whole blob.
        return overlapIndex == layoutRanges.size() ? null : layoutRanges.get(overlapIndex).getEndpoint();
    }

    private static long rangeEnd(HttpRange range) {
        Long length = range.getLength();
        return length == null ? Long.MAX_VALUE : range.getOffset() + length - 1;
    }
}
