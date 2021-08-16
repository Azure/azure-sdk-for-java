// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.nio

import com.azure.core.http.*
import com.azure.core.test.http.MockHttpResponse
import com.azure.core.util.CoreUtils
import com.azure.storage.blob.implementation.util.BlobUserAgentModificationPolicy
import com.azure.storage.common.StorageSharedKeyCredential
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import spock.lang.Specification

import java.util.regex.Matcher
import java.util.regex.Pattern

class NioClientBuilderTest extends Specification {

    private static final Map<String, String> PROPERTIES =
        CoreUtils.getProperties("azure-storage-blob-nio.properties");
    private static final String SDK_NAME = "name";
    private static final String SDK_VERSION = "version";
    private static String clientName = PROPERTIES.getOrDefault(SDK_NAME, "UnknownName");
    private static String clientVersion = PROPERTIES.getOrDefault(SDK_VERSION, "UnknownVersion");

    static HttpRequest request(String url) {
        return new HttpRequest(HttpMethod.HEAD, new URL(url), new HttpHeaders().put("Content-Length", "0"),
            Flux.empty())
    }

    def "AzureFileSystem ServiceClient"() {
        setup:
        def config = [:]
        config[AzureFileSystem.AZURE_STORAGE_FILE_STORES] = String.join(",", ["containerName"])
        config[AzureFileSystem.AZURE_STORAGE_HTTP_CLIENT] = new UAStringTestClient("azsdk-java-azure-storage-blob/\\d+\\.\\d+\\.\\d+[-beta\\.\\d+]* azsdk-java-" + clientName + "/" + clientVersion + " " + "(.)*")
        config[AzureFileSystem.AZURE_STORAGE_SHARED_KEY_CREDENTIAL] = new StorageSharedKeyCredential("accountName", "accountKey")

        when:
        def fileSystem = new AzureFileSystem(new AzureFileSystemProvider(), "https://accountName.blob.core.windows.net", config)
        def pipeline = fileSystem.blobServiceClient.getHttpPipeline()
        def foundPolicy = false
        for (int i = 0; i < pipeline.getPolicyCount(); i++)
            foundPolicy |= (pipeline.getPolicy(i) instanceof BlobUserAgentModificationPolicy)


        then:
        foundPolicy
        StepVerifier.create(pipeline.send(request(fileSystem.blobServiceClient.getAccountUrl())))
            .assertNext({ it.getStatusCode() == 200 })
            .verifyComplete()
    }

    def "AzureFileStore ContainerClient"() {
        setup:
        def config = [:]
        config[AzureFileSystem.AZURE_STORAGE_FILE_STORES] = String.join(",", ["containerName"])
        config[AzureFileSystem.AZURE_STORAGE_HTTP_CLIENT] = new UAStringTestClient("azsdk-java-azure-storage-blob/\\d+\\.\\d+\\.\\d+[-beta\\.\\d+]* azsdk-java-" + clientName + "/" + clientVersion + " " + "(.)*")
        config[AzureFileSystem.AZURE_STORAGE_SHARED_KEY_CREDENTIAL] = new StorageSharedKeyCredential("accountName", "accountKey")
        def fileSystem = new AzureFileSystem(new AzureFileSystemProvider(), "https://accountName.blob.core.windows.net", config)

        when:
        AzureFileStore fileStore = fileSystem.getFileStore("containerName")
        def pipeline = fileStore.containerClient.getHttpPipeline()
        def foundPolicy = false
        for (int i = 0; i < pipeline.getPolicyCount(); i++)
            foundPolicy |= (pipeline.getPolicy(i) instanceof BlobUserAgentModificationPolicy)


        then:
        foundPolicy
        StepVerifier.create(pipeline.send(request(fileStore.containerClient.getBlobContainerUrl())))
            .assertNext({ it.getStatusCode() == 200 })
            .verifyComplete()
    }

    def "AzureResource BlobClient"() {
        setup:
        def config = [:]
        config[AzureFileSystem.AZURE_STORAGE_FILE_STORES] = String.join(",", ["containerName"])
        config[AzureFileSystem.AZURE_STORAGE_HTTP_CLIENT] = new UAStringTestClient("azsdk-java-azure-storage-blob/\\d+\\.\\d+\\.\\d+[-beta\\.\\d+]* azsdk-java-" + clientName + "/" + clientVersion + " " + "(.)*")
        config[AzureFileSystem.AZURE_STORAGE_SHARED_KEY_CREDENTIAL] = new StorageSharedKeyCredential("accountName", "accountKey")
        def fileSystem = new AzureFileSystem(new AzureFileSystemProvider(), "https://accountName.blob.core.windows.net", config)
        AzurePath path = fileSystem.getPath("blobName")

        when:
        AzureResource resource = new AzureResource(path)
        def pipeline = resource.blobClient.getHttpPipeline()
        def foundPolicy = false
        for (int i = 0; i < pipeline.getPolicyCount(); i++)
            foundPolicy |= (pipeline.getPolicy(i) instanceof BlobUserAgentModificationPolicy)

        then:
        foundPolicy
        StepVerifier.create(pipeline.send(request(resource.blobClient.getBlobUrl())))
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
