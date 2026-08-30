// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.util;

import com.azure.storage.blob.implementation.models.BlobLayoutEndpoints;
import com.azure.storage.blob.implementation.models.BlobLayoutEndpointsEndpointItem;
import com.azure.storage.blob.implementation.models.BlobLayoutInternal;
import com.azure.storage.blob.implementation.models.BlobLayoutRanges;
import com.azure.storage.blob.implementation.models.BlobLayoutRangesRangeItem;
import com.azure.storage.blob.models.BlobLayoutRange;
import com.azure.storage.common.DataLocalityEndpoint;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ModelHelperTests {
    @Test
    public void transformBlobLayoutRangesCreatesTypedEndpoint() {
        List<BlobLayoutRange> ranges = ModelHelper.transformBlobLayoutRanges(layout("layout.example.net:443"));

        assertEquals(1, ranges.size());
        assertEquals(DataLocalityEndpoint.fromString("layout.example.net:443"), ranges.get(0).getEndpoint());
    }

    @Test
    public void transformBlobLayoutRangesIgnoresInvalidServiceEndpoint() {
        List<BlobLayoutRange> ranges = ModelHelper.transformBlobLayoutRanges(layout("http://"));

        assertTrue(ranges.isEmpty());
    }

    private static BlobLayoutInternal layout(String endpoint) {
        BlobLayoutEndpoints endpoints = new BlobLayoutEndpoints().setEndpoint(
            Collections.singletonList(new BlobLayoutEndpointsEndpointItem().setIndex(0).setValue(endpoint)));
        BlobLayoutRanges ranges = new BlobLayoutRanges().setRange(
            Collections.singletonList(new BlobLayoutRangesRangeItem().setStart(0).setEnd(99).setEndpointIndex(0)));
        return new BlobLayoutInternal().setEndpoints(endpoints).setRanges(ranges);
    }
}
