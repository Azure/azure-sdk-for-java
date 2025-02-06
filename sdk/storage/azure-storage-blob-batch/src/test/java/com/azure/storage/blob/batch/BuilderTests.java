// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.batch;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.util.CoreUtils;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.implementation.util.BlobUserAgentModificationPolicy;
import com.azure.storage.common.StorageSharedKeyCredential;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BuilderTests {
    private static final StorageSharedKeyCredential CREDENTIALS
        = new StorageSharedKeyCredential("accountName", "accountKey");
    private static final String ENDPOINT = "https://account.blob.core.windows.net/";
    private static final Map<String, String> PROPERTIES
        = CoreUtils.getProperties("azure-storage-blob-batch.properties");
    private static final String CLIENT_NAME = PROPERTIES.getOrDefault("name", "UnknownName");
    private static final String CLIENT_VERSION = PROPERTIES.getOrDefault("version", "UnknownVersion");

    static HttpRequest request(String url) {
        return new HttpRequest(HttpMethod.HEAD, url);
    }

    @Test
    public void constructFromServiceClientBlobUserAgentModificationPolicy() {
        BlobServiceClient serviceClient = new BlobServiceClientBuilder().endpoint(ENDPOINT)
            .credential(CREDENTIALS)
            // This is supposed to be matching the following azsdk-java-azure-storage-blob/<version> azsdk-java-azure-storage-blob-batch/<version> <this part is the OS/runtime information>
            .httpClient(
                new UAStringTestClient("azsdk-java-azure-storage-blob/\\d+\\.\\d+\\.\\d+[-beta\\.\\d+]* azsdk-java-"
                    + CLIENT_NAME + "/" + CLIENT_VERSION + " " + "(.)*"))
            .buildClient();

        BlobBatchClient batchClient = new BlobBatchClientBuilder(serviceClient).buildClient();
        HttpPipeline pipeline = batchClient.getClient().getClient().getHttpPipeline();

        assertPipelineAndRequest(pipeline, request(serviceClient.getAccountUrl()));
    }

    @Test
    public void constructFromContainerClientBlobUserAgentModificationPolicy() {
        BlobContainerClient containerClient = new BlobContainerClientBuilder().endpoint(ENDPOINT)
            .containerName("containerName")
            .credential(CREDENTIALS)
            .httpClient(
                new UAStringTestClient("azsdk-java-azure-storage-blob/\\d+\\.\\d+\\.\\d+[-beta\\.\\d+]* azsdk-java-"
                    + CLIENT_NAME + "/" + CLIENT_VERSION + " " + "(.)*"))
            .buildClient();

        BlobBatchClient batchClient = new BlobBatchClientBuilder(containerClient).buildClient();
        HttpPipeline pipeline = batchClient.getClient().getClient().getHttpPipeline();

        assertPipelineAndRequest(pipeline, request(containerClient.getBlobContainerUrl()));
    }

    private static void assertPipelineAndRequest(HttpPipeline pipeline, HttpRequest request) {
        boolean foundPolicy = false;
        for (int i = 0; i < pipeline.getPolicyCount(); i++) {
            foundPolicy |= (pipeline.getPolicy(i) instanceof BlobUserAgentModificationPolicy);
        }

        assertTrue(foundPolicy);
        StepVerifier.create(pipeline.send(request))
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
            if (CoreUtils.isNullOrEmpty(request.getHeaders().getValue("User-Agent"))) {
                throw new RuntimeException("Failed to set 'User-Agent' header.");
            }
            Matcher matcher = pattern.matcher(request.getHeaders().getValue("User-Agent"));
            assertTrue(matcher.matches());
            return Mono.just(new MockHttpResponse(request, 200));
        }
    }

}
