// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.util;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.util.logging.ClientLogger;
import com.azure.xml.XmlReader;
import com.azure.storage.blob.implementation.models.BlobLayoutInternal;
import com.azure.storage.common.LayoutAwareRouting;
import com.azure.storage.common.policy.DataLocalityPolicy;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import javax.xml.stream.XMLStreamException;
import java.net.URI;
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
    public void transformBlobLayoutRangesConvertsEndpointValuesToUris() throws XMLStreamException {
        // The layout models are response-only, so build one the way the service sends it.
        String xml = "<BlobLayout><Ranges>" + "<Range Start=\"0\" End=\"99\" EndpointIndex=\"0\"/>"
            + "<Range Start=\"100\" End=\"199\" EndpointIndex=\"1\"/>"
            + "<Range Start=\"200\" End=\"299\" EndpointIndex=\"2\"/></Ranges><Endpoints>"
            + "<Endpoint Index=\"0\" Value=\"host-a:443\"/>" + "<Endpoint Index=\"1\" Value=\"https://host-b:443\"/>"
            + "<Endpoint Index=\"2\" Value=\"invalid endpoint\"/></Endpoints></BlobLayout>";
        BlobLayoutInternal layout;
        try (XmlReader reader = XmlReader.fromString(xml)) {
            layout = BlobLayoutInternal.fromXml(reader);
        }

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
