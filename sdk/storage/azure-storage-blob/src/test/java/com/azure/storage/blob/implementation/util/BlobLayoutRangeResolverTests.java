// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.util;

import com.azure.core.http.HttpRange;
import com.azure.storage.blob.models.BlobLayoutRange;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class BlobLayoutRangeResolverTests {

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

    @ParameterizedTest
    @MethodSource("unusableEndpointCases")
    public void unusableResolvedEndpointReturnsNull(String endpoint) {
        List<BlobLayoutRange> ranges = Collections.singletonList(range(0, 999, endpoint));

        assertNull(BlobLayoutRangeResolver.resolveEndpoint(0, ranges));
    }

    @ParameterizedTest
    @MethodSource("validEndpointCases")
    public void validResolvedEndpointReturnsUnchanged(String endpoint) {
        List<BlobLayoutRange> ranges = Collections.singletonList(range(0, 999, endpoint));

        assertEquals(endpoint, BlobLayoutRangeResolver.resolveEndpoint(0, ranges));
    }

    @ParameterizedTest
    @MethodSource("validOffsetCases")
    public void resolvesCorrectEndpointForValidOffsets(long offset, List<BlobLayoutRange> ranges,
        String expectedEndpoint) {
        assertEquals(expectedEndpoint, BlobLayoutRangeResolver.resolveEndpoint(offset, ranges));
    }

    @Test
    public void chunkStartPastLastRangeReturnsNull() {
        List<BlobLayoutRange> ranges = Collections.singletonList(range(0, 999, "https://host-a:443"));

        assertNull(BlobLayoutRangeResolver.resolveEndpoint(1000, ranges));
    }

    @Test
    public void chunkStartInGapBetweenRangesReturnsNull() {
        List<BlobLayoutRange> ranges
            = Arrays.asList(range(0, 999, "https://host-a:443"), range(2000, 2999, "https://host-b:443"));

        assertNull(BlobLayoutRangeResolver.resolveEndpoint(1500, ranges));
    }

    @Test
    public void unboundedFinalRangeResolvesForAnyOffsetAtOrPastItsStart() {
        List<BlobLayoutRange> ranges = Arrays.asList(range(0, 999, "https://host-a:443"),
            new BlobLayoutRange(new HttpRange(1000), "https://host-b:443"));

        assertEquals("https://host-a:443", BlobLayoutRangeResolver.resolveEndpoint(999, ranges));
        assertEquals("https://host-b:443", BlobLayoutRangeResolver.resolveEndpoint(1000, ranges));
        assertEquals("https://host-b:443", BlobLayoutRangeResolver.resolveEndpoint(Long.MAX_VALUE, ranges));
    }

    @Test
    public void rangeEndingAtMaxValueBoundaryResolves() {
        List<BlobLayoutRange> ranges = Collections
            .singletonList(new BlobLayoutRange(new HttpRange(Long.MAX_VALUE - 10, 10L), "https://host-a:443"));

        assertEquals("https://host-a:443", BlobLayoutRangeResolver.resolveEndpoint(Long.MAX_VALUE - 10, ranges));
        assertEquals("https://host-a:443", BlobLayoutRangeResolver.resolveEndpoint(Long.MAX_VALUE - 1, ranges));
        assertNull(BlobLayoutRangeResolver.resolveEndpoint(Long.MAX_VALUE, ranges));
    }

    @Test
    public void rangeWhoseEndOverflowsLongStillResolves() {
        // offset + length exceeds Long.MAX_VALUE, so computing the inclusive end directly would wrap negative.
        List<BlobLayoutRange> ranges = Collections
            .singletonList(new BlobLayoutRange(new HttpRange(Long.MAX_VALUE - 10, 100L), "https://host-a:443"));

        assertEquals("https://host-a:443", BlobLayoutRangeResolver.resolveEndpoint(Long.MAX_VALUE - 10, ranges));
        assertEquals("https://host-a:443", BlobLayoutRangeResolver.resolveEndpoint(Long.MAX_VALUE, ranges));
        assertNull(BlobLayoutRangeResolver.resolveEndpoint(Long.MAX_VALUE - 11, ranges));
    }

    @Test
    public void offsetBeforeFirstRangeReturnsNull() {
        List<BlobLayoutRange> ranges = Collections.singletonList(range(100, 199, "https://host-a:443"));

        assertNull(BlobLayoutRangeResolver.resolveEndpoint(0, ranges));
        assertNull(BlobLayoutRangeResolver.resolveEndpoint(99, ranges));
    }

    private static Stream<Arguments> validOffsetCases() {
        return Stream.of(validCase(0L, threeEndpointRanges(), "https://host-a:443"),
            validCase(999L, threeEndpointRanges(), "https://host-a:443"),
            validCase(1000L, threeEndpointRanges(), "https://host-b:443"),
            validCase(1999L, threeEndpointRanges(), "https://host-b:443"),
            validCase(2000L, threeEndpointRanges(), "https://host-c:443"),
            validCase(2999L, threeEndpointRanges(), "https://host-c:443"),
            validCase(20L, unalignedRanges(), "https://host-a:443"),
            validCase(45L, unalignedRanges(), "https://host-a:443"),
            validCase(46L, unalignedRanges(), "https://host-b:443"),
            validCase(82L, unalignedRanges(), "https://host-b:443"),
            validCase(83L, unalignedRanges(), "https://host-c:443"),
            validCase(99L, unalignedRanges(), "https://host-c:443"),
            validCase(0L, createManyRanges(), "https://host-0:443"),
            validCase(50042L, createManyRanges(), "https://host-500:443"),
            validCase(99999L, createManyRanges(), "https://host-999:443"));
    }

    private static Arguments validCase(long offset, List<BlobLayoutRange> ranges, String expectedEndpoint) {
        return Arguments.of(offset, ranges, expectedEndpoint);
    }

    private static Stream<Arguments> unusableEndpointCases() {
        return Stream.of(Arguments.of((Object) null), Arguments.of(""), Arguments.of("   "), Arguments.of(":443"));
    }

    private static Stream<Arguments> validEndpointCases() {
        return Stream.of(Arguments.of("blob.stamp.store.core.windows.net:443"),
            Arguments.of("blob.stamp.store.core.windows.net"),
            Arguments.of("https://blob.stamp.store.core.windows.net:443"));
    }

    private static List<BlobLayoutRange> threeEndpointRanges() {
        return layout(range(0, 999, "https://host-a:443"), range(1000, 1999, "https://host-b:443"),
            range(2000, 2999, "https://host-c:443"));
    }

    private static List<BlobLayoutRange> unalignedRanges() {
        return layout(range(20, 45, "https://host-a:443"), range(46, 82, "https://host-b:443"),
            range(83, 99, "https://host-c:443"));
    }

    private static List<BlobLayoutRange> createManyRanges() {
        List<BlobLayoutRange> ranges = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            long start = i * 100L;
            long end = start + 99;
            ranges.add(range(start, end, "https://host-" + i + ":443"));
        }
        return ranges;
    }

    private static List<BlobLayoutRange> layout(BlobLayoutRange... ranges) {
        return Arrays.asList(ranges);
    }

    private static BlobLayoutRange range(long start, long end, String endpoint) {
        return new BlobLayoutRange(new HttpRange(start, end - start + 1), endpoint);
    }

}
