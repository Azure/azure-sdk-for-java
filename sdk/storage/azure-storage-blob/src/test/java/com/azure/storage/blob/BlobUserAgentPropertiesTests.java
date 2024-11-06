// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.util.CoreUtils;
import com.azure.storage.blob.implementation.util.BlobUserAgentModificationPolicy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BlobUserAgentPropertiesTests {

    @Test
    public void userAgentPropertiesNotNull() {
        Map<String, String> properties = CoreUtils.getProperties("azure-storage-blob.properties");
        assertEquals(properties.get("name"), "azure-storage-blob");
        assertTrue(properties.get("version").matches("(\\d)+.(\\d)+.(\\d)+([-a-zA-Z0-9.])*"));
    }

    @ParameterizedTest
    @MethodSource("userAgentModificationPolicyTestSupplier")
    public void userAgentModificationPolicyTest(String userAgentBefore, String name, String version,
        String userAgentAfter) {
        BlobUserAgentModificationPolicy uaPolicy = new BlobUserAgentModificationPolicy(name, version);
        UAStringTestClient client = new UAStringTestClient(userAgentAfter);
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(client).policies(uaPolicy).build();

        StepVerifier
            .create(pipeline.send(new HttpRequest(HttpMethod.GET, "https://account.blob.core.windows.net/")
                .setHeader(HttpHeaderName.USER_AGENT, userAgentBefore)))
            .assertNext(it -> assertEquals(it.getStatusCode(), 200))
            .verifyComplete();
    }

    private static Stream<Arguments> userAgentModificationPolicyTestSupplier() {
        return Stream.of(Arguments.of("azsdk-java-azure-storage-blob/12.11.0-beta.2 (11.0.6; Windows 10; 10.0)",
            "azure-storage-blob-batch", "12.8.0-beta.2",
            "azsdk-java-azure-storage-blob/12.11.0-beta.2 azsdk-java-azure-storage-blob-batch/12.8.0-beta.2 (11.0.6; Windows 10; 10.0)"), // Tests both beta
            Arguments.of("azsdk-java-azure-storage-blob/12.11.0 (11.0.6; Windows 10; 10.0)", "azure-storage-blob-batch",
                "12.8.0-beta.2",
                "azsdk-java-azure-storage-blob/12.11.0 azsdk-java-azure-storage-blob-batch/12.8.0-beta.2 (11.0.6; Windows 10; 10.0)"), // Tests blob GA and batch beta
            Arguments.of("azsdk-java-azure-storage-blob/12.11.0-beta.2 (11.0.6; Windows 10; 10.0)",
                "azure-storage-blob-batch", "12.8.0",
                "azsdk-java-azure-storage-blob/12.11.0-beta.2 azsdk-java-azure-storage-blob-batch/12.8.0 (11.0.6; Windows 10; 10.0)"), // Tests blob beta and batch GA
            Arguments.of("azsdk-java-azure-storage-blob/12.11.0 (11.0.6; Windows 10; 10.0)", "azure-storage-blob-batch",
                "12.8.0",
                "azsdk-java-azure-storage-blob/12.11.0 azsdk-java-azure-storage-blob-batch/12.8.0 (11.0.6; Windows 10; 10.0)"), // Tests both GA, user agent with appended OS and JVM info
            Arguments.of("azsdk-java-azure-storage-blob/12.11.0 (11.0.6; Windows 10; 10.0)",
                "azure-storage-blob-changefeed", "12.0.0",
                "azsdk-java-azure-storage-blob/12.11.0 azsdk-java-azure-storage-blob-changefeed/12.0.0 (11.0.6; Windows 10; 10.0)"), // Tests for changefeed
            Arguments.of("azsdk-java-azure-storage-blob/12.11.0 (11.0.6; Windows 10; 10.0)", "azure-storage-blob-nio",
                "12.0.0",
                "azsdk-java-azure-storage-blob/12.11.0 azsdk-java-azure-storage-blob-nio/12.0.0 (11.0.6; Windows 10; 10.0)"), // Tests for nio
            Arguments.of("azsdk-java-azure-storage-blob/12.11.0 (11.0.6; Windows 10; 10.0)",
                "azure-storage-file-datalake", "12.4.0",
                "azsdk-java-azure-storage-blob/12.11.0 azsdk-java-azure-storage-file-datalake/12.4.0 (11.0.6; Windows 10; 10.0)"), // Tests for datalake
            Arguments.of("azsdk-java-azure-storage-blob/12.11.0 (11.0.6; Windows 10; 10.0)",
                "azure-storage-blob-cryptography", "12.11.0",
                "azsdk-java-azure-storage-blob/12.11.0 azsdk-java-azure-storage-blob-cryptography/12.11.0 (11.0.6; Windows 10; 10.0)"), // Tests for cryptography
            Arguments.of("prependappid azsdk-java-azure-storage-blob/12.11.0", "azure-storage-blob-batch", "12.8.0",
                "prependappid azsdk-java-azure-storage-blob/12.11.0 azsdk-java-azure-storage-blob-batch/12.8.0"), // User agent with prepended custom id
            Arguments.of("prependappid azsdk-java-azure-storage-blob/12.11.0 (11.0.6; Windows 10; 10.0)",
                "azure-storage-blob-batch", "12.8.0",
                "prependappid azsdk-java-azure-storage-blob/12.11.0 azsdk-java-azure-storage-blob-batch/12.8.0 (11.0.6; Windows 10; 10.0)"), // User agent with prepended custom id and appended OS JVM info
            Arguments.of("azsdk-java-azure-storage-blob/12.11.0", "azure-storage-blob-batch", "12.8.0",
                "azsdk-java-azure-storage-blob/12.11.0 azsdk-java-azure-storage-blob-batch/12.8.0"), // User agent
            Arguments.of("azsdk-java-azure-storage-file-share/12.11.0 (11.0.6; Windows 10; 10.0)",
                "azure-storage-blob-cryptography", "12.11.0",
                "azsdk-java-azure-storage-file-share/12.11.0 (11.0.6; Windows 10; 10.0)"), // Tests for a header that should not be modified
            Arguments.of("custom UA header", "azure-storage-blob-cryptography", "12.11.0", "custom UA header"), // Tests for a custom header that should not be modified
            Arguments.of("customUAheader", "azure-storage-blob-cryptography", "12.11.0", "customUAheader") // Tests for a custom header that should not be modified
        );
    }

    private static final class UAStringTestClient implements HttpClient {

        private final String expectedUA;

        UAStringTestClient(String expectedUA) {
            this.expectedUA = expectedUA;
        }

        @Override
        public Mono<HttpResponse> send(HttpRequest request) {
            if (CoreUtils.isNullOrEmpty(request.getHeaders().getValue(HttpHeaderName.USER_AGENT))) {
                throw new RuntimeException("Failed to set 'User-Agent' header.");
            }
            assert request.getHeaders().getValue(HttpHeaderName.USER_AGENT).equals(expectedUA);
            return Mono.just(new MockHttpResponse(request, 200));
        }
    }
}
