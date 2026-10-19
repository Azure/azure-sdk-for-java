// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.util;

import com.azure.storage.blob.implementation.models.BlobLayoutEndpoints;
import com.azure.storage.blob.implementation.models.BlobLayoutEndpointsEndpointItem;
import com.azure.storage.blob.implementation.models.BlobLayoutInternal;
import com.azure.storage.blob.implementation.models.BlobLayoutRanges;
import com.azure.storage.blob.implementation.models.BlobLayoutRangesRangeItem;
import com.azure.storage.blob.models.BlobLayoutRange;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ModelHelperTests {
    @Test
    public void transformBlobLayoutRangesConvertsEndpointValuesToUris() {
        BlobLayoutInternal layout = new BlobLayoutInternal()
            .setRanges(new BlobLayoutRanges()
                .setRange(Arrays.asList(new BlobLayoutRangesRangeItem().setStart(0).setEnd(99).setEndpointIndex(0),
                    new BlobLayoutRangesRangeItem().setStart(100).setEnd(199).setEndpointIndex(1),
                    new BlobLayoutRangesRangeItem().setStart(200).setEnd(299).setEndpointIndex(2))))
            .setEndpoints(new BlobLayoutEndpoints()
                .setEndpoint(Arrays.asList(new BlobLayoutEndpointsEndpointItem().setIndex(0).setValue("host-a:443"),
                    new BlobLayoutEndpointsEndpointItem().setIndex(1).setValue("https://host-b:443"),
                    new BlobLayoutEndpointsEndpointItem().setIndex(2).setValue("invalid endpoint"))));

        List<BlobLayoutRange> ranges = ModelHelper.transformBlobLayoutRanges(layout);

        assertEquals(3, ranges.size());
        assertEquals(URI.create("//host-a:443"), ranges.get(0).getEndpoint());
        assertEquals(URI.create("https://host-b:443"), ranges.get(1).getEndpoint());
        assertNull(ranges.get(2).getEndpoint());
    }
}
