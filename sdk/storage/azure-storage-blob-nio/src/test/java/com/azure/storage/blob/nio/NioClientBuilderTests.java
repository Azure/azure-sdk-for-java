// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.nio;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.util.CoreUtils;
import com.azure.storage.blob.implementation.util.BlobUserAgentModificationPolicy;
import com.azure.storage.common.StorageSharedKeyCredential;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NioClientBuilderTests {
    private static final Map<String, String> PROPERTIES = CoreUtils.getProperties("azure-storage-blob-nio.properties");
    private static final String CLIENT_NAME = PROPERTIES.getOrDefault("name", "UnknownName");
    private static final String CLIENT_VERSION = PROPERTIES.getOrDefault("version", "UnknownVersion");

    static HttpRequest request(String url) {
        return new HttpRequest(HttpMethod.HEAD, url);
    }

    @Test
    public void azureFileSystemServiceClient() throws IOException {
        Map<String, Object> config = new HashMap<>();
        config.put(AzureFileSystem.AZURE_STORAGE_FILE_STORES, "containerName");
        config.put(AzureFileSystem.AZURE_STORAGE_HTTP_CLIENT,
            new UAStringTestClient("azsdk-java-azure-storage-blob/\\d+\\.\\d+\\.\\d+[-beta\\.\\d+]* azsdk-java-"
                + CLIENT_NAME + "/" + CLIENT_VERSION + " " + "(.)*"));
        config.put(AzureFileSystem.AZURE_STORAGE_SHARED_KEY_CREDENTIAL,
            new StorageSharedKeyCredential("accountName", "accountKey"));

        AzureFileSystem fileSystem
            = new AzureFileSystem(new AzureFileSystemProvider(), "https://accountName.blob.core.windows.net", config);
        HttpPipeline pipeline = fileSystem.getBlobServiceClient().getHttpPipeline();

        verifyPipelineAndResponse(pipeline, fileSystem.getBlobServiceClient().getAccountUrl());
    }

    @Test
    public void azureFileStoreContainerClient() throws IOException {
        Map<String, Object> config = new HashMap<>();
        config.put(AzureFileSystem.AZURE_STORAGE_FILE_STORES, "containerName");
        config.put(AzureFileSystem.AZURE_STORAGE_HTTP_CLIENT,
            new UAStringTestClient("azsdk-java-azure-storage-blob/\\d+\\.\\d+\\.\\d+[-beta\\.\\d+]* azsdk-java-"
                + CLIENT_NAME + "/" + CLIENT_VERSION + " " + "(.)*"));
        config.put(AzureFileSystem.AZURE_STORAGE_SHARED_KEY_CREDENTIAL,
            new StorageSharedKeyCredential("accountName", "accountKey"));
        AzureFileSystem fileSystem
            = new AzureFileSystem(new AzureFileSystemProvider(), "https://accountName.blob.core.windows.net", config);
        AzureFileStore fileStore = (AzureFileStore) fileSystem.getFileStore("containerName");
        HttpPipeline pipeline = fileStore.getContainerClient().getHttpPipeline();

        verifyPipelineAndResponse(pipeline, fileStore.getContainerClient().getBlobContainerUrl());
    }

    @Test
    public void azResourceBlobClient() throws IOException {
        Map<String, Object> config = new HashMap<>();
        config.put(AzureFileSystem.AZURE_STORAGE_FILE_STORES, "containerName");
        config.put(AzureFileSystem.AZURE_STORAGE_HTTP_CLIENT,
            new UAStringTestClient("azsdk-java-azure-storage-blob/\\d+\\.\\d+\\.\\d+[-beta\\.\\d+]* azsdk-java-"
                + CLIENT_NAME + "/" + CLIENT_VERSION + " " + "(.)*"));
        config.put(AzureFileSystem.AZURE_STORAGE_SHARED_KEY_CREDENTIAL,
            new StorageSharedKeyCredential("accountName", "accountKey"));
        AzureFileSystem fileSystem
            = new AzureFileSystem(new AzureFileSystemProvider(), "https://accountName.blob.core.windows.net", config);
        AzurePath path = (AzurePath) fileSystem.getPath("blobName");
        AzureResource resource = new AzureResource(path);
        HttpPipeline pipeline = resource.getBlobClient().getHttpPipeline();

        verifyPipelineAndResponse(pipeline, resource.getBlobClient().getBlobUrl());
    }

    private static void verifyPipelineAndResponse(HttpPipeline pipeline, String url) {
        boolean foundPolicy = false;
        for (int i = 0; i < pipeline.getPolicyCount(); i++) {
            foundPolicy |= (pipeline.getPolicy(i) instanceof BlobUserAgentModificationPolicy);
        }

        assertTrue(foundPolicy);
        StepVerifier.create(pipeline.send(request(url)))
            .assertNext(response -> assertEquals(200, response.getStatusCode()))
            .verifyComplete();
    }

    private static final class UAStringTestClient implements HttpClient {
        private final Pattern pattern;

        UAStringTestClient(String regex) {
            this.pattern = Pattern.compile(regex);
        }

        @Override
        public Mono<HttpResponse> send(HttpRequest request) {
            if (CoreUtils.isNullOrEmpty(request.getHeaders().getValue(HttpHeaderName.USER_AGENT))) {
                throw new RuntimeException("Failed to set 'User-Agent' header.");
            }
            Matcher matcher = pattern.matcher(request.getHeaders().getValue(HttpHeaderName.USER_AGENT));
            assertTrue(matcher.matches());
            return Mono.just(new MockHttpResponse(request, 200));
        }
    }
}
