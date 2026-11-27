// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.directconnectivity.rntbd;

import com.azure.cosmos.implementation.Document;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.RxDocumentServiceResponse;
import com.azure.cosmos.implementation.TestUtils;
import com.azure.cosmos.implementation.directconnectivity.StoreResponse;
import com.azure.cosmos.models.FeedResponse;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class ChangeFeedResponseMappingTest {
    @DataProvider(name = "responses")
    public Object[][] responses() {
        return new Object[][] {
            { 200, false }, { 200, true }, { 304, false }
        };
    }

    @Test(groups = "unit", dataProvider = "responses")
    public void realRntbdResponsePreservesEtagAndDistinctQueryContinuation(int status, boolean hasItems) {
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpConstants.HttpHeaders.E_TAG, "\"physical-progress\"");
        headers.put(HttpConstants.HttpHeaders.CONTINUATION, "ordinary-query-progress");
        RxDocumentServiceResponse response = decodeResponse(status, headers, hasItems);
        assertThat(response.getResponseHeaders()).containsEntry(HttpConstants.HttpHeaders.E_TAG, "\"physical-progress\"")
            .containsEntry(HttpConstants.HttpHeaders.CONTINUATION, "ordinary-query-progress");
        FeedResponse<Document> changeFeed = changeFeed(response);
        assertThat(changeFeed.getContinuationToken()).isEqualTo("\"physical-progress\"");
        assertThat(changeFeed.getResults()).hasSize(hasItems ? 1 : 0);
        FeedResponse<Document> ordinary = ImplementationBridgeHelpers.FeedResponseHelper.getFeedResponseAccessor()
            .createFeedResponse(changeFeed.getResults(), response.getResponseHeaders(), null);
        assertThat(ordinary.getContinuationToken()).isEqualTo("ordinary-query-progress");
    }

    @Test(groups = "unit", dataProvider = "responses")
    public void queryContinuationIsNotSilentlyConvertedIntoPhysicalChangeFeedProgress(int status, boolean hasItems) {
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpConstants.HttpHeaders.CONTINUATION, "ordinary-query-progress");
        RxDocumentServiceResponse response = decodeResponse(status, headers, hasItems);
        assertThat(response.getResponseHeaders()).doesNotContainKey(HttpConstants.HttpHeaders.E_TAG);
        assertThat(changeFeed(response).getContinuationToken()).isNull();
    }

    public static FeedResponse<Document> mappedChangeFeed(int status, String etag, String queryContinuation,
        boolean hasItems) {
        Map<String, String> headers = new HashMap<>();
        if (etag != null) {
            headers.put(HttpConstants.HttpHeaders.E_TAG, etag);
        }
        if (queryContinuation != null) {
            headers.put(HttpConstants.HttpHeaders.CONTINUATION, queryContinuation);
        }
        return changeFeed(decodeResponse(status, headers, hasItems));
    }

    private static FeedResponse<Document> changeFeed(RxDocumentServiceResponse response) {
        return ImplementationBridgeHelpers.FeedResponseHelper.getFeedResponseAccessor()
            .createChangeFeedResponse(response, null, Document.class);
    }

    private static RxDocumentServiceResponse decodeResponse(int status, Map<String, String> headers, boolean hasItems) {
        ByteBuf body = status == 304 ? Unpooled.EMPTY_BUFFER : Unpooled.copiedBuffer(
            hasItems ? "{\"Documents\":[{\"id\":\"synthetic\"}]}" : "{\"Documents\":[]}", StandardCharsets.UTF_8);
        RntbdResponse source = new RntbdResponse(UUID.randomUUID(), status, headers, body);
        source.retain();
        ByteBuf encoded = Unpooled.buffer();
        RntbdResponse decoded = null;
        try {
            source.encode(encoded);
            decoded = RntbdResponse.decode(encoded);
            assertThat(decoded).isNotNull();
            decoded.retain();
            StoreResponse store = decoded.toStoreResponse("test-server", "https://localhost/replica");
            RxDocumentServiceResponse response = new RxDocumentServiceResponse(TestUtils.mockDiagnosticsClientContext(), store);
            if (status != 304) {
                response.getQueryResponse(null, Document.class);
            }
            return response;
        } finally {
            if (decoded != null) {
                decoded.release();
            }
            source.release();
            encoded.release();
            if (body != Unpooled.EMPTY_BUFFER) {
                body.release();
            }
        }
    }
}
