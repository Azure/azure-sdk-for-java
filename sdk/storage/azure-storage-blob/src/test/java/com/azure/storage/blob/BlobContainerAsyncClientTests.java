// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.models.ListBlobsOptions;
import com.azure.storage.blob.models.StorageResponseSerializationFormat;
import com.azure.storage.common.implementation.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BlobContainerAsyncClientTests {
    private AtomicReference<String> acceptHeader;
    private BlobContainerAsyncClient client;

    @BeforeEach
    public void setup() {
        acceptHeader = new AtomicReference<>();
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(request -> {
            acceptHeader.set(request.getHeaders().getValue(HttpHeaderName.ACCEPT));
            // Fail after capturing the request so the test doesn't need a format-specific response body.
            return Mono.just(new MockHttpResponse(request, 500));
        }).build();
        client = new BlobContainerClientBuilder().endpoint("https://account.blob.core.windows.net/container")
            .credential(new MockTokenCredential())
            .pipeline(pipeline)
            .buildAsyncClient();
    }

    @ParameterizedTest
    @MethodSource("serializationFormatSupplier")
    public void listBlobsUsesResolvedSerializationFormat(ListBlobsOptions options, String expectedAcceptHeader) {
        sendRequest(() -> client.listBlobs(options).byPage(1).blockFirst());
        assertEquals(expectedAcceptHeader, acceptHeader.get());
    }

    @ParameterizedTest
    @MethodSource("serializationFormatSupplier")
    public void listBlobsByHierarchyUsesResolvedSerializationFormat(ListBlobsOptions options,
        String expectedAcceptHeader) {
        sendRequest(() -> client.listBlobsByHierarchy("/", options).byPage(1).blockFirst());
        assertEquals(expectedAcceptHeader, acceptHeader.get());
    }

    private static void sendRequest(Supplier<?> request) {
        try {
            request.get();
        } catch (BlobStorageException exception) {
            if (exception.getStatusCode() != 500) {
                throw exception;
            }
        }
    }

    private static Stream<Arguments> serializationFormatSupplier() {
        String arrowAcceptHeader
            = Constants.ContentTypeConstants.APPLICATION_VND_APACHE_ARROW_STREAM + ",application/xml";
        return Stream.of(Arguments.of(null, arrowAcceptHeader), Arguments.of(new ListBlobsOptions(), arrowAcceptHeader),
            Arguments.of(
                new ListBlobsOptions().setStorageResponseSerializationFormat(StorageResponseSerializationFormat.AUTO),
                arrowAcceptHeader),
            Arguments.of(
                new ListBlobsOptions().setStorageResponseSerializationFormat(StorageResponseSerializationFormat.ARROW),
                arrowAcceptHeader),
            Arguments.of(
                new ListBlobsOptions().setStorageResponseSerializationFormat(StorageResponseSerializationFormat.XML),
                "application/xml"));
    }
}
