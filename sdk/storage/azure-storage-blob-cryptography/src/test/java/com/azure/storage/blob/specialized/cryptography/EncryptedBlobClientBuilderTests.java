// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized.cryptography;

import com.azure.core.credential.AzureSasCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.FixedDelayOptions;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.RetryOptions;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.Header;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobClientBuilder;
import com.azure.storage.blob.implementation.util.BlobUserAgentModificationPolicy;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.Utility;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.policy.RequestRetryOptions;
import com.azure.storage.common.policy.RetryPolicyType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EncryptedBlobClientBuilderTests {
    private static final StorageSharedKeyCredential CREDENTIALS =
        new StorageSharedKeyCredential("accountName", "accountKey");
    private static final String ENDPOINT = "https://account.blob.core.windows.net/";
    private static final RequestRetryOptions REQUEST_RETRY_OPTIONS = new RequestRetryOptions(RetryPolicyType.FIXED, 2,
        2, 1000L, 4000L, null);
    private static final RetryOptions CORE_RETRY_OPTIONS = new RetryOptions(new FixedDelayOptions(1,
        Duration.ofSeconds(1)));
    private static final Map<String, String> PROPERTIES =
        CoreUtils.getProperties("azure-storage-blob-cryptography.properties");
    private static final String CLIENT_NAME = PROPERTIES.getOrDefault("name", "UnknownName");
    private static final String CLIENT_VERSION = PROPERTIES.getOrDefault("version", "UnknownVersion");
    private static final List<Header> CLIENT_OPTIONS_HEADERS;
    private static final Map<HttpHeaderName, String> HEADERS_MAP;

    static {
        CLIENT_OPTIONS_HEADERS = new ArrayList<>();
        CLIENT_OPTIONS_HEADERS.add(new Header("custom", "header"));
        CLIENT_OPTIONS_HEADERS.add(new Header("Authorization", "notthis"));
        CLIENT_OPTIONS_HEADERS.add(new Header("User-Agent", "overwritten"));

        HEADERS_MAP = new LinkedHashMap<>();
        HEADERS_MAP.put(HttpHeaderName.fromString("custom"), "header");
        HEADERS_MAP.put(HttpHeaderName.AUTHORIZATION, "notthis");
        HEADERS_MAP.put(HttpHeaderName.USER_AGENT, "overwritten");
    }

    private byte[] randomData;

    @BeforeEach
    public void setup() {
        randomData = new byte[256];
        new SecureRandom().nextBytes(randomData);
    }

    static HttpRequest request(String url) {
        return new HttpRequest(HttpMethod.HEAD, url);
    }

    /**
     * Tests that a new date will be applied to every retry when using the encrypted blob client builder's default
     * pipeline.
     */
    @Test
    public void encryptedBlobClientFreshDateOnRetry() {
        EncryptedBlobClient encryptedBlobClient = new EncryptedBlobClientBuilder()
            .endpoint(ENDPOINT)
            .containerName("container")
            .blobName("blob")
            .credential(CREDENTIALS)
            .key(new FakeKey("keyId", randomData), "keyWrapAlgorithm")
            .httpClient(new FreshDateTestClient())
            .retryOptions(REQUEST_RETRY_OPTIONS)
            .buildEncryptedBlobClient();

        StepVerifier.create(encryptedBlobClient.getHttpPipeline().send(request(encryptedBlobClient.getBlobUrl())))
            .assertNext(response -> assertEquals(200, response.getStatusCode()))
            .verifyComplete();
    }

    /**
     * Tests that a user application id will be honored in the UA string when using the encrypted blob client builder's default
     * pipeline.
     */
    @ParameterizedTest
    @CsvSource(value = {"log-options-id,,log-options-id", ",client-options-id,client-options-id",
        "log-options-id,client-options-id,client-options-id" /* Client options preferred over log options */})
    public void encryptedBlobClientCustomApplicationIdInUAString(String logOptionsUA, String clientOptionsUA,
        String expectedUA) {
        EncryptedBlobClient encryptedBlobClient = new EncryptedBlobClientBuilder()
            .endpoint(ENDPOINT)
            .containerName("container")
            .blobName("blob")
            .credential(CREDENTIALS)
            .key(new FakeKey("keyId", randomData), "keyWrapAlgorithm")
            .httpClient(new ApplicationIdUAStringTestClient(expectedUA))
            .httpLogOptions(new HttpLogOptions().setApplicationId(logOptionsUA))
            .clientOptions(new ClientOptions().setApplicationId(clientOptionsUA))
            .buildEncryptedBlobClient();

        StepVerifier.create(encryptedBlobClient.getHttpPipeline().send(request(encryptedBlobClient.getBlobUrl())))
            .assertNext(response -> assertEquals(200, response.getStatusCode()))
            .verifyComplete();
    }

    /**
     * Tests that custom headers will be honored when using the encrypted blob client builder's default
     * pipeline.
     */
    @Test
    public void encryptedBlobClientCustomHeadersClientOptions() {
        EncryptedBlobClient encryptedBlobClient = new EncryptedBlobClientBuilder()
            .endpoint(ENDPOINT)
            .containerName("container")
            .blobName("blob")
            .credential(CREDENTIALS)
            .key(new FakeKey("keyId", randomData), "keyWrapAlgorithm")
            .httpClient(new ClientOptionsHeadersTestClient(HEADERS_MAP))
            .clientOptions(new ClientOptions().setHeaders(CLIENT_OPTIONS_HEADERS))
            .buildEncryptedBlobClient();

        StepVerifier.create(encryptedBlobClient.getHttpPipeline().send(request(encryptedBlobClient.getBlobUrl())))
            .assertNext(response -> assertEquals(200, response.getStatusCode()))
            .verifyComplete();
    }

    @Test
    public void doesNotThrowOnAmbiguousCredentialsWithoutAzureSasCredential() {
        assertDoesNotThrow(() -> new EncryptedBlobClientBuilder()
            .endpoint(ENDPOINT)
            .containerName("container")
            .blobName("foo")
            .key(new FakeKey("keyId", randomData), "keyWrapAlgorithm")
            .credential(new StorageSharedKeyCredential("foo", "bar"))
            .credential(new MockTokenCredential())
            .sasToken("foo")
            .buildEncryptedBlobClient());
    }

    @Test
    public void throwsOnAmbiguousCredentialsWithAzureSasCredential() {
        assertThrows(IllegalStateException.class, () -> new EncryptedBlobClientBuilder()
            .endpoint(ENDPOINT)
            .blobName("foo")
            .containerName("container")
            .key(new FakeKey("keyId", randomData), "keyWrapAlgorithm")
            .credential(new StorageSharedKeyCredential("foo", "bar"))
            .credential(new AzureSasCredential("foo"))
            .buildEncryptedBlobClient());

        assertThrows(IllegalStateException.class, () -> new EncryptedBlobClientBuilder()
            .endpoint(ENDPOINT)
            .blobName("foo")
            .containerName("container")
            .key(new FakeKey("keyId", randomData), "keyWrapAlgorithm")
            .credential(new MockTokenCredential())
            .credential(new AzureSasCredential("foo"))
            .buildEncryptedBlobClient());

        assertThrows(IllegalStateException.class, () -> new EncryptedBlobClientBuilder()
            .endpoint(ENDPOINT)
            .blobName("foo")
            .containerName("container")
            .key(new FakeKey("keyId", randomData), "keyWrapAlgorithm")
            .sasToken("foo")
            .credential(new AzureSasCredential("foo"))
            .buildEncryptedBlobClient());

        assertThrows(IllegalStateException.class, () -> new EncryptedBlobClientBuilder()
            .endpoint(ENDPOINT + "?sig=foo")
            .blobName("foo")
            .containerName("container")
            .key(new FakeKey("keyId", randomData), "keyWrapAlgorithm")
            .credential(new AzureSasCredential("foo"))
            .buildEncryptedBlobClient());
    }

    @Test
    public void onlyOneRetryOptionsCanBeApplied() {
        assertThrows(IllegalStateException.class, () -> new EncryptedBlobClientBuilder()
            .endpoint(ENDPOINT)
            .blobName("foo")
            .containerName("foo")
            .key(new FakeKey("keyId", randomData), "keyWrapAlgorithm")
            .retryOptions(REQUEST_RETRY_OPTIONS)
            .retryOptions(CORE_RETRY_OPTIONS)
            .buildEncryptedBlobClient());
    }

    @Test
    public void constructFromBlobClientBlobUserAgentModificationPolicy() {
        BlobClient blobClient = new BlobClientBuilder()
            .endpoint(ENDPOINT)
            .credential(CREDENTIALS)
            .blobName("foo")
            .containerName("container")
            .httpClient(new UAStringTestClient("azsdk-java-azure-storage-blob/\\d+\\.\\d+\\.\\d+[-beta\\.\\d+]* azsdk-java-" + CLIENT_NAME + "/" + CLIENT_VERSION + " " + "(.)*"))
            .buildClient();

        EncryptedBlobClient cryptoClient = new EncryptedBlobClientBuilder()
            .blobClient(blobClient)
            .key(new FakeKey("keyId", randomData), "keyWrapAlgorithm")
            .buildEncryptedBlobClient();

        sendAndValidateUserAgentHeader(cryptoClient.getHttpPipeline(), cryptoClient.getBlobUrl());
    }

    @Test
    public void constructFromNoClientBlobUserAgentModificationPolicy() {
        EncryptedBlobClient cryptoClient = new EncryptedBlobClientBuilder()
            .endpoint(ENDPOINT)
            .blobName("foo")
            .containerName("container")
            .key(new FakeKey("keyId", randomData), "keyWrapAlgorithm")
            .credential(new AzureSasCredential("foo"))
            .httpClient(new UAStringTestClient("azstorage-clientsideencryption/1.0 azsdk-java-azure-storage-blob/\\d+\\.\\d+\\.\\d+[-beta\\.\\d+]* azsdk-java-" + CLIENT_NAME + "/" + CLIENT_VERSION + " " + "(.)*"))
            .buildEncryptedBlobClient();

        sendAndValidateUserAgentHeader(cryptoClient.getHttpPipeline(), cryptoClient.getBlobUrl());
    }

    private static Stream<Arguments> getNonEncodedBlobNameSupplier() {
        return Stream.of(
            Arguments.of("test%test"),
            Arguments.of("ab2a7d5f-b973-4222-83ba-d0581817a819 %Россия 한국 中国!?/file"),
            Arguments.of("%E6%96%91%E9%BB%9E"),
            Arguments.of("斑點"));
    }

    @ParameterizedTest
    @MethodSource("getNonEncodedBlobNameSupplier")
    public void getNonEncodedBlobName(String originalBlobName) {
        EncryptedBlobClient encryptedBlobClient = new EncryptedBlobClientBuilder()
            .endpoint(ENDPOINT)
            .containerName("container")
            .blobName(originalBlobName)
            .credential(CREDENTIALS)
            .key(new FakeKey("keyId", randomData), "keyWrapAlgorithm")
            .buildEncryptedBlobClient();

        assertEquals(encryptedBlobClient.getBlobName(), originalBlobName);

        // see if the blob name will be properly encoded in the url
        String encodedName = Utility.urlEncode(originalBlobName);
        assertTrue(encryptedBlobClient.getBlobUrl().contains(encodedName));
    }

    @ParameterizedTest
    @ValueSource(longs = { 0, -1, 15, 4L * Constants.GB })
    public void illegalRegionLength(long regionLength) {
        assertThrows(IllegalArgumentException.class, () -> new BlobClientSideEncryptionOptions()
                    .setAuthenticatedRegionDataLengthInBytes(regionLength));
    }

    @ParameterizedTest
    @ValueSource(longs = { 16, 4 * Constants.KB, 4 * Constants.MB, Constants.GB })
    public void encryptedRegionLength(long regionLength) {
        EncryptedBlobClient encryptedBlobClient = new EncryptedBlobClientBuilder(EncryptionVersion.V2_1)
            .blobName("foo")
            .containerName("container")
            .key(new FakeKey("keyId", randomData), "keyWrapAlgorithm")
            .clientSideEncryptionOptions(new BlobClientSideEncryptionOptions()
                    .setAuthenticatedRegionDataLengthInBytes(regionLength))
                .buildEncryptedBlobClient();
        assertEquals(regionLength, encryptedBlobClient.getClientSideEncryptionOptions().getAuthenticatedRegionDataLengthInBytes());
    }

    @Test
    public void encryptedRegionLengthDefault() {
        EncryptedBlobClient encryptedBlobClient = new EncryptedBlobClientBuilder(EncryptionVersion.V2)
            .blobName("foo")
            .containerName("container")
            .key(new FakeKey("keyId", randomData), "keyWrapAlgorithm")
            .buildEncryptedBlobClient();
        assertEquals(4 * Constants.MB, encryptedBlobClient.getClientSideEncryptionOptions().getAuthenticatedRegionDataLengthInBytes());
    }

    @ParameterizedTest
    @MethodSource("encryptedRegionLengthWithIllegalVersionSupplier")
    public void encryptedRegionLengthWithIllegalVersion(EncryptionVersion version) {
        assertThrows(IllegalArgumentException.class, ()->  new EncryptedBlobClientBuilder(version)
            .blobName("foo")
            .containerName("container")
            .key(new FakeKey("keyId", randomData), "keyWrapAlgorithm")
            .clientSideEncryptionOptions(new BlobClientSideEncryptionOptions()
                .setAuthenticatedRegionDataLengthInBytes(Constants.KB))
            .buildEncryptedBlobClient());
    }

    private static Stream<Arguments> encryptedRegionLengthWithIllegalVersionSupplier() {
        return Stream.of(
            Arguments.of(EncryptionVersion.V1),
            Arguments.of(EncryptionVersion.V2)
        );
    }


    private static void sendAndValidateUserAgentHeader(HttpPipeline pipeline, String url) {
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

    private static final class FreshDateTestClient implements HttpClient {
        private String firstDate;

        @Override
        public Mono<HttpResponse> send(HttpRequest request) {
            if (firstDate == null) {
                firstDate = request.getHeaders().getValue(HttpHeaderName.DATE);
                return Mono.error(new IOException("IOException!"));
            }

            assertNotEquals(firstDate, request.getHeaders().getValue(HttpHeaderName.DATE));
            return Mono.just(new MockHttpResponse(request, 200));
        }
    }

    private static final class ApplicationIdUAStringTestClient implements HttpClient {
        private final String expectedUA;

        ApplicationIdUAStringTestClient(String expectedUA) {
            this.expectedUA = expectedUA;
        }

        @Override
        public Mono<HttpResponse> send(HttpRequest request) {
            assertTrue(request.getHeaders().getValue(HttpHeaderName.USER_AGENT).startsWith(expectedUA));

            return Mono.just(new MockHttpResponse(request, 200));
        }
    }

    private static final class ClientOptionsHeadersTestClient implements HttpClient {
        private final Map<HttpHeaderName, String> headers;

        ClientOptionsHeadersTestClient(Map<HttpHeaderName, String> headers) {
            this.headers = headers;
        }

        @Override
        public Mono<HttpResponse> send(HttpRequest request) {
            headers.forEach((name, value) -> {
                if (CoreUtils.isNullOrEmpty(request.getHeaders().getValue(name))) {
                    throw new RuntimeException("Failed to set custom header " + name);
                }

                if (name == HttpHeaderName.AUTHORIZATION) {
                    if (Objects.equals(value, request.getHeaders().getValue(HttpHeaderName.AUTHORIZATION))) {
                        throw new RuntimeException("Custom header " + name + " did not match expectation.");
                    }
                } else {
                    if (!Objects.equals(value, request.getHeaders().getValue(name))) {
                        throw new RuntimeException("Custom header " + name + " did not match expectation.");
                    }
                }
            });

            return Mono.just(new MockHttpResponse(request, 200));
        }
    }
}
