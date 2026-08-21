// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.util;

import com.azure.core.http.HttpRange;
import com.azure.storage.blob.models.BlobLayoutRange;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BlobLayoutCacheValueTests {

    @Test
    public void populatedRangesAreRetained() {
        List<BlobLayoutRange> ranges
            = Collections.singletonList(new BlobLayoutRange(new HttpRange(0, 999L), "https://host-a:443"));
        OffsetDateTime expiresOn = OffsetDateTime.now().plusMinutes(5);

        BlobLayoutCacheValue value = new BlobLayoutCacheValue(ranges, expiresOn);

        assertEquals(1, value.getRanges().size());
        assertEquals("https://host-a:443", value.getRanges().get(0).getEndpoint());
        assertEquals(expiresOn, value.getExpiresOn());
    }

    @Test
    public void emptyRangesRepresentNoLayout() {
        BlobLayoutCacheValue value = new BlobLayoutCacheValue(Collections.emptyList(), OffsetDateTime.now());

        assertTrue(value.getRanges().isEmpty());
    }

    @Test
    public void nullRangesRepresentFailure() {
        BlobLayoutCacheValue value = new BlobLayoutCacheValue(null, OffsetDateTime.now());

        assertNull(value.getRanges());
    }

    @Test
    public void rangesAreUnmodifiable() {
        List<BlobLayoutRange> ranges
            = Collections.singletonList(new BlobLayoutRange(new HttpRange(0, 999L), "https://host-a:443"));
        BlobLayoutCacheValue value = new BlobLayoutCacheValue(ranges, OffsetDateTime.now());

        assertThrows(UnsupportedOperationException.class,
            () -> value.getRanges().add(new BlobLayoutRange(new HttpRange(1000, 999L), "https://host-b:443")));
    }

    @Test
    public void refreshesThirtySecondsBeforeExpiration() {
        OffsetDateTime expiresOn = OffsetDateTime.now().plusMinutes(5);

        BlobLayoutCacheValue value = new BlobLayoutCacheValue(Collections.emptyList(), expiresOn);

        assertEquals(expiresOn.minus(Duration.ofSeconds(30)), value.getRefreshOn());
    }
}
