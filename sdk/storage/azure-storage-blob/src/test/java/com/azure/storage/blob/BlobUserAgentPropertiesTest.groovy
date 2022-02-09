// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob

import com.azure.core.http.*
import com.azure.core.test.http.MockHttpResponse
import com.azure.core.util.CoreUtils
import com.azure.storage.blob.implementation.util.BlobUserAgentModificationPolicy
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import spock.lang.Specification
import spock.lang.Unroll

class BlobUserAgentPropertiesTest extends Specification {

    def "User agent properties not null"() {
        given:
        Map<String, String> properties = CoreUtils.getProperties("azure-storage-blob.properties")
        expect:
        properties.get("name") == "azure-storage-blob"
        properties.get("version").matches("(\\d)+.(\\d)+.(\\d)+([-a-zA-Z0-9.])*")
    }

    @Unroll
    def "User agent modification policy test"() {
        setup:
        def uaPolicy = new BlobUserAgentModificationPolicy(name, version)
        def client = new UAStringTestClient(UAafter)
        def pipeline = new HttpPipelineBuilder().httpClient(client).policies(uaPolicy).build()

        expect:
        StepVerifier.create(pipeline.send(new HttpRequest(HttpMethod.GET, "https://account.blob.core.windows.net/").setHeader("User-Agent", UAbefore)))
            .assertNext({ it.getStatusCode() == 200 })
            .verifyComplete()

        where:
        UAbefore                                                                        | name                              | version          || UAafter
        "azsdk-java-azure-storage-blob/12.11.0-beta.2 (11.0.6; Windows 10; 10.0)"       | "azure-storage-blob-batch"        | "12.8.0-beta.2"  || "azsdk-java-azure-storage-blob/12.11.0-beta.2 azsdk-java-azure-storage-blob-batch/12.8.0-beta.2 (11.0.6; Windows 10; 10.0)" // Tests both beta
        "azsdk-java-azure-storage-blob/12.11.0 (11.0.6; Windows 10; 10.0)"              | "azure-storage-blob-batch"        | "12.8.0-beta.2"  || "azsdk-java-azure-storage-blob/12.11.0 azsdk-java-azure-storage-blob-batch/12.8.0-beta.2 (11.0.6; Windows 10; 10.0)" // Tests blob GA and batch beta
        "azsdk-java-azure-storage-blob/12.11.0-beta.2 (11.0.6; Windows 10; 10.0)"       | "azure-storage-blob-batch"        | "12.8.0"         || "azsdk-java-azure-storage-blob/12.11.0-beta.2 azsdk-java-azure-storage-blob-batch/12.8.0 (11.0.6; Windows 10; 10.0)" // Tests blob beta and batch GA
        "azsdk-java-azure-storage-blob/12.11.0 (11.0.6; Windows 10; 10.0)"              | "azure-storage-blob-batch"        | "12.8.0"         || "azsdk-java-azure-storage-blob/12.11.0 azsdk-java-azure-storage-blob-batch/12.8.0 (11.0.6; Windows 10; 10.0)" // Tests both GA, user agent with appended OS and JVM info
        "azsdk-java-azure-storage-blob/12.11.0 (11.0.6; Windows 10; 10.0)"              | "azure-storage-blob-changefeed"   | "12.0.0"         || "azsdk-java-azure-storage-blob/12.11.0 azsdk-java-azure-storage-blob-changefeed/12.0.0 (11.0.6; Windows 10; 10.0)" // Tests for changefeed
        "azsdk-java-azure-storage-blob/12.11.0 (11.0.6; Windows 10; 10.0)"              | "azure-storage-blob-nio"          | "12.0.0"         || "azsdk-java-azure-storage-blob/12.11.0 azsdk-java-azure-storage-blob-nio/12.0.0 (11.0.6; Windows 10; 10.0)" // Tests for nio
        "azsdk-java-azure-storage-blob/12.11.0 (11.0.6; Windows 10; 10.0)"              | "azure-storage-file-datalake"     | "12.4.0"         || "azsdk-java-azure-storage-blob/12.11.0 azsdk-java-azure-storage-file-datalake/12.4.0 (11.0.6; Windows 10; 10.0)" // Tests for datalake
        "azsdk-java-azure-storage-blob/12.11.0 (11.0.6; Windows 10; 10.0)"              | "azure-storage-blob-cryptography" | "12.11.0"        || "azsdk-java-azure-storage-blob/12.11.0 azsdk-java-azure-storage-blob-cryptography/12.11.0 (11.0.6; Windows 10; 10.0)" // Tests for cryptography
        "prependappid azsdk-java-azure-storage-blob/12.11.0"                            | "azure-storage-blob-batch"        | "12.8.0"         || "prependappid azsdk-java-azure-storage-blob/12.11.0 azsdk-java-azure-storage-blob-batch/12.8.0" // User agent with prepended custom id
        "prependappid azsdk-java-azure-storage-blob/12.11.0 (11.0.6; Windows 10; 10.0)" | "azure-storage-blob-batch"        | "12.8.0"         || "prependappid azsdk-java-azure-storage-blob/12.11.0 azsdk-java-azure-storage-blob-batch/12.8.0 (11.0.6; Windows 10; 10.0)" // User agent with prepended custom id and appended OS JVM info
        "azsdk-java-azure-storage-blob/12.11.0"                                         | "azure-storage-blob-batch"        | "12.8.0"         || "azsdk-java-azure-storage-blob/12.11.0 azsdk-java-azure-storage-blob-batch/12.8.0" // User agent
        "azsdk-java-azure-storage-file-share/12.11.0 (11.0.6; Windows 10; 10.0)"        | "azure-storage-blob-cryptography" | "12.11.0"        || "azsdk-java-azure-storage-file-share/12.11.0 (11.0.6; Windows 10; 10.0)" // Tests for a header that should not be modified
        "custom UA header"                                                              | "azure-storage-blob-cryptography" | "12.11.0"        || "custom UA header" // Tests for a custom header that should not be modified
        "customUAheader"                                                                | "azure-storage-blob-cryptography" | "12.11.0"        || "customUAheader" // Tests for a custom header that should not be modified
    }

    private static final class UAStringTestClient implements HttpClient {

        private final String expectedUA;

        UAStringTestClient(String expectedUA) {
            this.expectedUA = expectedUA;
        }

        @Override
        Mono<HttpResponse> send(HttpRequest request) {
            if (CoreUtils.isNullOrEmpty(request.getHeaders().getValue("User-Agent"))) {
                throw new RuntimeException("Failed to set 'User-Agent' header.")
            }
            assert request.getHeaders().getValue("User-Agent").equals(expectedUA)
            return Mono.just(new MockHttpResponse(request, 200))
        }
    }
}
