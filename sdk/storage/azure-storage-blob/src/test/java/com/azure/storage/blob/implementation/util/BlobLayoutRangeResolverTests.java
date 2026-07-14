// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.util;

import com.azure.core.http.HttpRange;
import com.azure.storage.blob.models.BlobLayoutRange;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class BlobLayoutRangeResolverTests {

    private static BlobLayoutRange range(long start, long end, String endpoint) {
        return new BlobLayoutRange(new HttpRange(start, end - start + 1), endpoint);
    }

    @Test
    public void nullLayoutReturnsNull() {
        assertNull(BlobLayoutRangeResolver.resolveEndpoint(0, null));
    }

    @Test
    public void emptyLayoutReturnsNull() {
        assertNull(BlobLayoutRangeResolver.resolveEndpoint(0, Collections.emptyList()));
    }

    @Test
    public void singleRangeCoversWholeBlob() {
        List<BlobLayoutRange> ranges = Collections.singletonList(range(0, 999, "https://host-a:443"));

        assertEquals("https://host-a:443", BlobLayoutRangeResolver.resolveEndpoint(0, ranges));
        assertEquals("https://host-a:443", BlobLayoutRangeResolver.resolveEndpoint(500, ranges));
        assertEquals("https://host-a:443", BlobLayoutRangeResolver.resolveEndpoint(999, ranges));
    }

    @Test
    public void multipleRangesResolveToCorrectEndpoint() {
        List<BlobLayoutRange> ranges = Arrays.asList(range(0, 999, "https://host-a:443"),
            range(1000, 1999, "https://host-b:443"), range(2000, 2999, "https://host-c:443"));

        assertEquals("https://host-a:443", BlobLayoutRangeResolver.resolveEndpoint(0, ranges));
        assertEquals("https://host-a:443", BlobLayoutRangeResolver.resolveEndpoint(999, ranges));
        assertEquals("https://host-b:443", BlobLayoutRangeResolver.resolveEndpoint(1000, ranges));
        assertEquals("https://host-b:443", BlobLayoutRangeResolver.resolveEndpoint(1999, ranges));
        assertEquals("https://host-c:443", BlobLayoutRangeResolver.resolveEndpoint(2000, ranges));
        assertEquals("https://host-c:443", BlobLayoutRangeResolver.resolveEndpoint(2999, ranges));
    }

    @Test
    public void unalignedRangeBoundariesResolveCorrectly() {
        List<BlobLayoutRange> ranges = Arrays.asList(range(20, 45, "https://host-a:443"),
            range(46, 82, "https://host-b:443"), range(83, 99, "https://host-c:443"));

        assertEquals("https://host-a:443", BlobLayoutRangeResolver.resolveEndpoint(20, ranges));
        assertEquals("https://host-a:443", BlobLayoutRangeResolver.resolveEndpoint(45, ranges));
        assertEquals("https://host-b:443", BlobLayoutRangeResolver.resolveEndpoint(46, ranges));
        assertEquals("https://host-b:443", BlobLayoutRangeResolver.resolveEndpoint(82, ranges));
        assertEquals("https://host-c:443", BlobLayoutRangeResolver.resolveEndpoint(83, ranges));
        assertEquals("https://host-c:443", BlobLayoutRangeResolver.resolveEndpoint(99, ranges));
    }

    @Test
    public void manyRangesResolveViaBinarySearch() {
        List<BlobLayoutRange> ranges = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            long start = i * 100L;
            long end = start + 99;
            ranges.add(range(start, end, "https://host-" + i + ":443"));
        }

        assertEquals("https://host-0:443", BlobLayoutRangeResolver.resolveEndpoint(0, ranges));
        assertEquals("https://host-500:443", BlobLayoutRangeResolver.resolveEndpoint(50042, ranges));
        assertEquals("https://host-999:443", BlobLayoutRangeResolver.resolveEndpoint(99999, ranges));
    }

    @Test
    public void chunkStartPastLastRangeReturnsNull() {
        List<BlobLayoutRange> ranges = Collections.singletonList(range(0, 999, "https://host-a:443"));

        assertNull(BlobLayoutRangeResolver.resolveEndpoint(1000, ranges));
    }
}
