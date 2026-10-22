// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.util;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.implementation.models.BlobLayoutEndpoints;
import com.azure.storage.blob.implementation.models.BlobLayoutEndpointsEndpointItem;
import com.azure.storage.blob.implementation.models.BlobLayoutInternal;
import com.azure.storage.blob.implementation.models.BlobLayoutRanges;
import com.azure.storage.blob.implementation.models.BlobLayoutRangesRangeItem;
import com.azure.storage.blob.models.BlobLayoutRange;
import com.azure.storage.common.LayoutAwareRouting;
import com.azure.storage.common.policy.DataLocalityPolicy;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

public class ModelHelperTests {
    @Test
    public void resolveLayoutAwareRoutingUsesDefaultAndPreservesExplicitModes() {
        assertEquals(LayoutAwareRouting.DISABLED, ModelHelper.resolveLayoutAwareRouting(null));
        assertEquals(LayoutAwareRouting.DISABLED, ModelHelper.resolveLayoutAwareRouting(LayoutAwareRouting.AUTO));
        assertEquals(LayoutAwareRouting.DISABLED, ModelHelper.resolveLayoutAwareRouting(LayoutAwareRouting.DISABLED));
        assertEquals(LayoutAwareRouting.ENABLED, ModelHelper.resolveLayoutAwareRouting(LayoutAwareRouting.ENABLED));
    }

    @Test
    public void layoutRoutingEnabledWarnsWhenPipelineDoesNotSupportDataLocality() {
        ClientLogger logger = mock(ClientLogger.class);

        assertFalse(ModelHelper.isLayoutRoutingEnabled(LayoutAwareRouting.ENABLED, pipeline(false), logger));

        verify(logger).warning(ModelHelper.UNSUPPORTED_DATA_LOCALITY_WARNING);
    }

    @Test
    public void layoutRoutingEnabledDoesNotWarnForImplicitOrDisabledModes() {
        ClientLogger logger = mock(ClientLogger.class);
        HttpPipeline pipeline = pipeline(false);

        assertFalse(ModelHelper.isLayoutRoutingEnabled(null, pipeline, logger));
        assertFalse(ModelHelper.isLayoutRoutingEnabled(LayoutAwareRouting.AUTO, pipeline, logger));
        assertFalse(ModelHelper.isLayoutRoutingEnabled(LayoutAwareRouting.DISABLED, pipeline, logger));

        verifyNoInteractions(logger);
    }

    @Test
    public void layoutRoutingEnabledDoesNotWarnWhenPipelineSupportsDataLocality() {
        ClientLogger logger = mock(ClientLogger.class);

        assertTrue(ModelHelper.isLayoutRoutingEnabled(LayoutAwareRouting.ENABLED, pipeline(true), logger));

        verifyNoInteractions(logger);
    }

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

    private static HttpPipeline pipeline(boolean supportsDataLocality) {
        HttpPipelineBuilder builder
            = new HttpPipelineBuilder().httpClient(request -> Mono.just(new MockHttpResponse(request, 200)));
        if (supportsDataLocality) {
            builder.policies(new DataLocalityPolicy());
        }
        return builder.build();
    }
}
