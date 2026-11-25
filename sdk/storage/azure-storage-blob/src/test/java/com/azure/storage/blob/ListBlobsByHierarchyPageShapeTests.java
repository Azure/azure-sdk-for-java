// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.ListBlobsOptions;
import com.azure.storage.blob.models.StorageResponseSerializationFormat;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Offline tests for hierarchical listing pages that contain no blobs, which the generated model returns without a
 * blob list.
 */
public class ListBlobsByHierarchyPageShapeTests {
    private static final String ENDPOINT = "https://account.blob.core.windows.net";
    private static final String PREFIXES_ONLY = "<Blobs><BlobPrefix><Name>dir1/</Name></BlobPrefix>"
        + "<BlobPrefix><Name>dir2/</Name></BlobPrefix></Blobs>";
    private static final String EMPTY = "<Blobs />";

    @ParameterizedTest
    @MethodSource("pageShapeSupplier")
    public void listBlobsByHierarchyWithoutBlobs(String blobsElement, int expectedPrefixes,
        StorageResponseSerializationFormat format) {
        ListBlobsOptions options = new ListBlobsOptions().setStorageResponseSerializationFormat(format);

        List<BlobItem> items = builder(blobsElement).buildClient()
            .listBlobsByHierarchy("/", options, null)
            .stream()
            .collect(Collectors.toList());

        assertPrefixes(items, expectedPrefixes);
    }

    @ParameterizedTest
    @MethodSource("pageShapeSupplier")
    public void listBlobsByHierarchyWithoutBlobsAsync(String blobsElement, int expectedPrefixes,
        StorageResponseSerializationFormat format) {
        ListBlobsOptions options = new ListBlobsOptions().setStorageResponseSerializationFormat(format);

        List<BlobItem> items = builder(blobsElement).buildAsyncClient()
            .listBlobsByHierarchy("/", options)
            .collectList()
            .block();

        assertPrefixes(items, expectedPrefixes);
    }

    // The service answers an Arrow request with XML when it can't produce Arrow, so both formats parse this XML.
    private static Stream<Arguments> pageShapeSupplier() {
        return Stream.of(Arguments.of(PREFIXES_ONLY, 2, StorageResponseSerializationFormat.XML),
            Arguments.of(PREFIXES_ONLY, 2, StorageResponseSerializationFormat.ARROW),
            Arguments.of(EMPTY, 0, StorageResponseSerializationFormat.XML),
            Arguments.of(EMPTY, 0, StorageResponseSerializationFormat.ARROW));
    }

    private static void assertPrefixes(List<BlobItem> items, int expectedPrefixes) {
        assertEquals(expectedPrefixes, items.size());
        items.forEach(item -> assertTrue(item.isPrefix()));
    }

    private static BlobContainerClientBuilder builder(String blobsElement) {
        return new BlobContainerClientBuilder().endpoint(ENDPOINT)
            .containerName("container")
            .pipeline(new HttpPipelineBuilder().httpClient(new ListingHttpClient(blobsElement)).build());
    }

    /*
     * Answers every request with a single hierarchical listing page.
     */
    private static final class ListingHttpClient implements HttpClient {
        private final byte[] body;

        ListingHttpClient(String blobsElement) {
            this.body = ("<?xml version=\"1.0\" encoding=\"utf-8\"?><EnumerationResults ServiceEndpoint=\"" + ENDPOINT
                + "/\" ContainerName=\"container\"><Delimiter>/</Delimiter>" + blobsElement
                + "<NextMarker /></EnumerationResults>").getBytes(StandardCharsets.UTF_8);
        }

        @Override
        public Mono<HttpResponse> send(HttpRequest request) {
            return Mono.just(new MockHttpResponse(request, 200,
                new HttpHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/xml"), body));
        }
    }
}
