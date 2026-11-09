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
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Map;
import java.util.StringJoiner;
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
    public void userAgentModificationPolicyTest(UserAgentTestCase testCase) {
        BlobUserAgentModificationPolicy uaPolicy
            = new BlobUserAgentModificationPolicy(testCase.policyName, testCase.policyVersion);
        UAStringTestClient client = new UAStringTestClient(testCase.expected.build());
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(client).policies(uaPolicy).build();

        StepVerifier
            .create(pipeline.send(new HttpRequest(HttpMethod.GET, "https://account.blob.core.windows.net/")
                .setHeader(HttpHeaderName.USER_AGENT, testCase.original.build())))
            .assertNext(it -> assertEquals(it.getStatusCode(), 200))
            .verifyComplete();
    }

    private static Stream<UserAgentTestCase> userAgentModificationPolicyTestSupplier() {
        return Stream.of(
            // Private dev-feed releases.
            new UserAgentTestCase().baseSdk("azure-storage-blob", "12.11.0-private")
                .policy("azure-storage-blob-batch", "12.8.0-private")
                .expectAdditionalSdk("azure-storage-blob-batch", "12.8.0-private")
                .platform("11.0.6", "Windows 10", "10.0"),
            // Private dev-feed beta releases.
            new UserAgentTestCase().baseSdk("azure-storage-blob", "12.11.0-beta.2-private")
                .policy("azure-storage-blob-batch", "12.8.0-beta.2-private")
                .expectAdditionalSdk("azure-storage-blob-batch", "12.8.0-beta.2-private")
                .platform("11.0.6", "Windows 10", "10.0"),
            // Both blob and batch beta.
            new UserAgentTestCase().baseSdk("azure-storage-blob", "12.11.0-beta.2")
                .policy("azure-storage-blob-batch", "12.8.0-beta.2")
                .expectAdditionalSdk("azure-storage-blob-batch", "12.8.0-beta.2")
                .platform("11.0.6", "Windows 10", "10.0"),
            // Blob GA and batch beta.
            new UserAgentTestCase().baseSdk("azure-storage-blob", "12.11.0")
                .policy("azure-storage-blob-batch", "12.8.0-beta.2")
                .expectAdditionalSdk("azure-storage-blob-batch", "12.8.0-beta.2")
                .platform("11.0.6", "Windows 10", "10.0"),
            // Blob beta and batch GA.
            new UserAgentTestCase().baseSdk("azure-storage-blob", "12.11.0-beta.2")
                .policy("azure-storage-blob-batch", "12.8.0")
                .expectAdditionalSdk("azure-storage-blob-batch", "12.8.0")
                .platform("11.0.6", "Windows 10", "10.0"),
            // Both GA, preserving JVM and OS information.
            new UserAgentTestCase().baseSdk("azure-storage-blob", "12.11.0")
                .policy("azure-storage-blob-batch", "12.8.0")
                .expectAdditionalSdk("azure-storage-blob-batch", "12.8.0")
                .platform("11.0.6", "Windows 10", "10.0"),
            // Change feed clients include their SDK token.
            new UserAgentTestCase().baseSdk("azure-storage-blob", "12.11.0")
                .policy("azure-storage-blob-changefeed", "12.0.0")
                .expectAdditionalSdk("azure-storage-blob-changefeed", "12.0.0")
                .platform("11.0.6", "Windows 10", "10.0"),
            // NIO clients include their SDK token.
            new UserAgentTestCase().baseSdk("azure-storage-blob", "12.11.0")
                .policy("azure-storage-blob-nio", "12.0.0")
                .expectAdditionalSdk("azure-storage-blob-nio", "12.0.0")
                .platform("11.0.6", "Windows 10", "10.0"),
            // Data Lake clients include their SDK token.
            new UserAgentTestCase().baseSdk("azure-storage-blob", "12.11.0")
                .policy("azure-storage-file-datalake", "12.4.0")
                .expectAdditionalSdk("azure-storage-file-datalake", "12.4.0")
                .platform("11.0.6", "Windows 10", "10.0"),
            // Cryptography clients include their SDK token.
            new UserAgentTestCase().baseSdk("azure-storage-blob", "12.11.0")
                .policy("azure-storage-blob-cryptography", "12.11.0")
                .expectAdditionalSdk("azure-storage-blob-cryptography", "12.11.0")
                .platform("11.0.6", "Windows 10", "10.0"),
            // Preserve a prepended application ID without platform information.
            new UserAgentTestCase().baseSdk("azure-storage-blob", "12.11.0")
                .policy("azure-storage-blob-batch", "12.8.0")
                .expectAdditionalSdk("azure-storage-blob-batch", "12.8.0")
                .applicationId("prependappid"),
            // Preserve both the application ID and platform information.
            new UserAgentTestCase().baseSdk("azure-storage-blob", "12.11.0")
                .policy("azure-storage-blob-batch", "12.8.0")
                .expectAdditionalSdk("azure-storage-blob-batch", "12.8.0")
                .applicationId("prependappid")
                .platform("11.0.6", "Windows 10", "10.0"),
            // Bare SDK token without an application ID or platform information.
            new UserAgentTestCase().baseSdk("azure-storage-blob", "12.11.0")
                .policy("azure-storage-blob-batch", "12.8.0")
                .expectAdditionalSdk("azure-storage-blob-batch", "12.8.0"),
            // Non-blob SDK headers remain unchanged.
            new UserAgentTestCase().baseSdk("azure-storage-file-share", "12.11.0")
                .policy("azure-storage-blob-cryptography", "12.11.0")
                .platform("11.0.6", "Windows 10", "10.0"),
            // Custom headers with spaces remain unchanged.
            new UserAgentTestCase().customHeader("custom UA header")
                .policy("azure-storage-blob-cryptography", "12.11.0"),
            // Custom headers without spaces remain unchanged.
            new UserAgentTestCase().customHeader("customUAheader")
                .policy("azure-storage-blob-cryptography", "12.11.0"));
    }

    @Test
    public void userAgentBuilderFormatsComponents() {
        assertEquals("azsdk-java-azure-storage-blob/12.11.0", userAgent().sdk("azure-storage-blob", "12.11.0").build());
        assertEquals(
            "prependappid azsdk-java-azure-storage-blob/12.11.0 "
                + "azsdk-java-azure-storage-blob-batch/12.8.0-beta.2 (11.0.6; Windows 10; 10.0)",
            userAgent().applicationId("prependappid")
                .sdk("azure-storage-blob", "12.11.0")
                .sdk("azure-storage-blob-batch", "12.8.0-beta.2")
                .platform("11.0.6", "Windows 10", "10.0")
                .build());
    }

    private static UserAgentBuilder userAgent() {
        return new UserAgentBuilder();
    }

    private static final class UserAgentTestCase {
        private final UserAgentBuilder original = userAgent();
        private final UserAgentBuilder expected = userAgent();
        private String policyName;
        private String policyVersion;

        UserAgentTestCase baseSdk(String name, String version) {
            original.sdk(name, version);
            expected.sdk(name, version);
            return this;
        }

        UserAgentTestCase policy(String name, String version) {
            policyName = name;
            policyVersion = version;
            return this;
        }

        UserAgentTestCase expectAdditionalSdk(String name, String version) {
            expected.sdk(name, version);
            return this;
        }

        UserAgentTestCase applicationId(String applicationId) {
            original.applicationId(applicationId);
            expected.applicationId(applicationId);
            return this;
        }

        UserAgentTestCase platform(String javaVersion, String osName, String osVersion) {
            original.platform(javaVersion, osName, osVersion);
            expected.platform(javaVersion, osName, osVersion);
            return this;
        }

        UserAgentTestCase customHeader(String header) {
            original.sdkTokens.add(header);
            expected.sdkTokens.add(header);
            return this;
        }

        @Override
        public String toString() {
            return original.build() + " + " + policyName + "/" + policyVersion;
        }
    }

    private static final class UserAgentBuilder {
        private final StringJoiner sdkTokens = new StringJoiner(" ");
        private String applicationId;
        private String platform;

        UserAgentBuilder applicationId(String applicationId) {
            this.applicationId = applicationId;
            return this;
        }

        UserAgentBuilder sdk(String name, String version) {
            sdkTokens.add("azsdk-java-" + name + "/" + version);
            return this;
        }

        UserAgentBuilder platform(String javaVersion, String osName, String osVersion) {
            platform = "(" + javaVersion + "; " + osName + "; " + osVersion + ")";
            return this;
        }

        String build() {
            return (applicationId == null ? "" : applicationId + " ") + sdkTokens
                + (platform == null ? "" : " " + platform);
        }
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
            assertEquals(expectedUA, request.getHeaders().getValue(HttpHeaderName.USER_AGENT));
            return Mono.just(new MockHttpResponse(request, 200));
        }
    }
}
