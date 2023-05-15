// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.batch

import com.azure.core.http.*
import com.azure.core.test.http.MockHttpResponse
import com.azure.core.util.CoreUtils
import com.azure.storage.blob.BlobContainerClientBuilder
import com.azure.storage.blob.BlobServiceClientBuilder
import com.azure.storage.blob.implementation.util.BlobUserAgentModificationPolicy
import com.azure.storage.common.StorageSharedKeyCredential
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import spock.lang.Specification

import java.util.regex.Matcher
import java.util.regex.Pattern

class BuilderTest extends Specification {

    static def credentials = new StorageSharedKeyCredential("accountName", "accountKey")
    static def endpoint = "https://account.blob.core.windows.net/"
    private static final Map<String, String> PROPERTIES =
        CoreUtils.getProperties("azure-storage-blob-batch.properties");
    private static final String SDK_NAME = "name";
    private static final String SDK_VERSION = "version";
    private static String clientName = PROPERTIES.getOrDefault(SDK_NAME, "UnknownName");
    private static String clientVersion = PROPERTIES.getOrDefault(SDK_VERSION, "UnknownVersion");

    static HttpRequest request(String url) {
        return new HttpRequest(HttpMethod.HEAD, new URL(url), new HttpHeaders().put("Content-Length", "0"),
            Flux.empty())
    }

    def "Construct from service client BlobUserAgentModificationPolicy"() {
        setup:
        def serviceClient = new BlobServiceClientBuilder()
            .endpoint(endpoint)
            .credential(credentials)
                                                // This is supposed to be matching the following azsdk-java-azure-storage-blob/<version> azsdk-java-azure-storage-blob-batch/<version> <this part is the OS/runtime information>
            .httpClient(new UAStringTestClient("azsdk-java-azure-storage-blob/\\d+\\.\\d+\\.\\d+[-beta\\.\\d+]* azsdk-java-" + clientName + "/" + clientVersion + " " + "(.)*"))
            .buildClient()

        when:
        def batchClient = new BlobBatchClientBuilder(serviceClient).buildClient()
        def pipeline = batchClient.client.client.getHttpPipeline()
        def foundPolicy = false
        for (int i = 0; i < pipeline.getPolicyCount(); i++)
            foundPolicy |= (pipeline.getPolicy(i) instanceof BlobUserAgentModificationPolicy)


        then:
        foundPolicy
        StepVerifier.create(pipeline.send(request(serviceClient.getAccountUrl())))
            .assertNext({ it.getStatusCode() == 200 })
            .verifyComplete()
    }

    def "Construct from container client BlobUserAgentModificationPolicy"() {
        setup:
        def containerClient = new BlobContainerClientBuilder()
            .endpoint(endpoint)
            .containerName("containerName")
            .credential(credentials)
            .httpClient(new UAStringTestClient("azsdk-java-azure-storage-blob/\\d+\\.\\d+\\.\\d+[-beta\\.\\d+]* azsdk-java-" + clientName + "/" + clientVersion + " " + "(.)*"))
            .buildClient()

        when:
        def batchClient = new BlobBatchClientBuilder(containerClient).buildClient()
        def pipeline = batchClient.client.client.getHttpPipeline()
        def foundPolicy = false
        for (int i = 0; i < pipeline.getPolicyCount(); i++)
            foundPolicy |= (pipeline.getPolicy(i) instanceof BlobUserAgentModificationPolicy)


        then:
        foundPolicy
        StepVerifier.create(pipeline.send(request(containerClient.getBlobContainerUrl())))
            .assertNext({ it.getStatusCode() == 200 })
            .verifyComplete()
    }

    private static final class UAStringTestClient implements HttpClient {

        private final Pattern pattern

        UAStringTestClient(String regex) {
            this.pattern = Pattern.compile(regex);
        }

        @Override
        Mono<HttpResponse> send(HttpRequest request) {
            if (CoreUtils.isNullOrEmpty(request.getHeaders().getValue("User-Agent"))) {
                throw new RuntimeException("Failed to set 'User-Agent' header.")
            }
            Matcher matcher = pattern.matcher(request.getHeaders().getValue("User-Agent"));
            assert matcher.matches()
            return Mono.just(new MockHttpResponse(request, 200))
        }
    }

}
