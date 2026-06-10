// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

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
import com.azure.core.test.http.NoOpHttpClient;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.core.test.utils.TestConfigurationSource;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Configuration;
import com.azure.core.util.ConfigurationBuilder;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.DateTimeRfc1123;
import com.azure.core.util.Header;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.implementation.util.BuilderHelper;
import com.azure.storage.blob.models.SessionOptions;
import com.azure.storage.blob.models.SessionMode;
import com.azure.storage.blob.specialized.AppendBlobClient;
import com.azure.storage.blob.specialized.BlockBlobClient;
import com.azure.storage.blob.specialized.PageBlobClient;
import com.azure.storage.blob.specialized.SpecializedBlobClientBuilder;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.policy.RequestRetryOptions;
import com.azure.storage.common.policy.RetryPolicyType;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BuilderHelperTests {
    private static final StorageSharedKeyCredential CREDENTIALS
        = new StorageSharedKeyCredential("accountName", "accountKey");
    private static final String ENDPOINT = "https://account.blob.core.windows.net/";
    private static final RequestRetryOptions REQUEST_RETRY_OPTIONS
        = new RequestRetryOptions(RetryPolicyType.FIXED, 2, 2, 1000L, 4000L, null);
    private static final RetryOptions CORE_RETRY_OPTIONS
        = new RetryOptions(new FixedDelayOptions(1, Duration.ofSeconds(2)));

    private static HttpRequest request(String url) {
        return new HttpRequest(HttpMethod.HEAD, url).setBody(Flux.empty())
            .setHeader(HttpHeaderName.CONTENT_LENGTH, "0");
    }

    /**
     * Tests that a new date will be applied to every retry when using the default pipeline builder.
     */
    @Test
    public void freshDateAppliedOnRetry() {
        HttpPipeline pipeline = BuilderHelper.buildPipeline(CREDENTIALS, null, null, null, ENDPOINT,
            REQUEST_RETRY_OPTIONS, null, BuilderHelper.getDefaultHttpLogOptions(), new ClientOptions(),
            new FreshDateTestClient(), new ArrayList<>(), new ArrayList<>(), null, null,
            new ClientLogger(BuilderHelperTests.class), null, null);

        StepVerifier.create(pipeline.send(request(ENDPOINT)))
            .assertNext(it -> assertEquals(200, it.getStatusCode()))
            .verifyComplete();
    }

    /**
     * Tests that a new date will be applied to every retry when using the serviceClientBuilder's default pipeline.
     */
    @Test
    public void serviceClientFreshDateOnRetry() {
        BlobServiceClient serviceClient = new BlobServiceClientBuilder().endpoint(ENDPOINT)
            .credential(CREDENTIALS)
            .httpClient(new FreshDateTestClient())
            .retryOptions(REQUEST_RETRY_OPTIONS)
            .buildClient();

        StepVerifier.create(serviceClient.getHttpPipeline().send(request(serviceClient.getAccountUrl())))
            .assertNext(it -> assertEquals(200, it.getStatusCode()))
            .verifyComplete();
    }

    /**
     * Tests that a new date will be applied to every retry when using the serviceClientBuilder's default pipeline.
     */
    @Test
    public void containerClientFreshDateOnRetry() {
        BlobContainerClient containerClient = new BlobContainerClientBuilder().endpoint(ENDPOINT)
            .containerName("container")
            .credential(CREDENTIALS)
            .httpClient(new FreshDateTestClient())
            .retryOptions(REQUEST_RETRY_OPTIONS)
            .buildClient();

        StepVerifier.create(containerClient.getHttpPipeline().send(request(containerClient.getBlobContainerUrl())))
            .assertNext(it -> assertEquals(200, it.getStatusCode()))
            .verifyComplete();
    }

    /**
     * Tests that a new date will be applied to every retry when using the blobClientBuilder's default pipeline.
     */
    @Test
    public void blobClientFreshDateOnRetry() {
        BlobClient blobClient = new BlobClientBuilder().endpoint(ENDPOINT)
            .containerName("container")
            .blobName("blob")
            .credential(CREDENTIALS)
            .httpClient(new FreshDateTestClient())
            .retryOptions(REQUEST_RETRY_OPTIONS)
            .buildClient();

        StepVerifier.create(blobClient.getHttpPipeline().send(request(blobClient.getBlobUrl())))
            .assertNext(it -> assertEquals(200, it.getStatusCode()))
            .verifyComplete();
    }

    /**
     * Tests that a new date will be applied to every retry when using the specializedBlobClientBuilder's default
     * pipeline.
     */
    @Test
    public void specializedBlobClientFreshDateOnRetry() {
        SpecializedBlobClientBuilder specializedBlobClientBuilder
            = new SpecializedBlobClientBuilder().endpoint(ENDPOINT)
                .containerName("container")
                .blobName("blob")
                .credential(CREDENTIALS)
                .retryOptions(REQUEST_RETRY_OPTIONS)
                .httpClient(new FreshDateTestClient());

        AppendBlobClient appendBlobClient = specializedBlobClientBuilder.buildAppendBlobClient();

        StepVerifier.create(appendBlobClient.getHttpPipeline().send(request(appendBlobClient.getBlobUrl())))
            .assertNext(it -> assertEquals(200, it.getStatusCode()))
            .verifyComplete();

        BlockBlobClient blockBlobClient = specializedBlobClientBuilder.buildBlockBlobClient();

        StepVerifier.create(blockBlobClient.getHttpPipeline().send(request(blockBlobClient.getBlobUrl())))
            .assertNext(it -> assertEquals(200, it.getStatusCode()))
            .verifyComplete();

        PageBlobClient pageBlobClient = specializedBlobClientBuilder.buildPageBlobClient();

        StepVerifier.create(pageBlobClient.getHttpPipeline().send(request(pageBlobClient.getBlobUrl())))
            .assertNext(it -> assertEquals(200, it.getStatusCode()))
            .verifyComplete();
    }

    /**
     * Tests that a user application id will be honored in the UA string when using the default pipeline builder.
     */
    @SuppressWarnings("deprecation")
    @ParameterizedTest
    @MethodSource("customApplicationIdInUAStringSupplier")
    public void customApplicationIdInUAString(String logOptionsUA, String clientOptionsUA, String expectedUA) {
        HttpPipeline pipeline = BuilderHelper.buildPipeline(CREDENTIALS, null, null, null, ENDPOINT,
            new RequestRetryOptions(), null, new HttpLogOptions().setApplicationId(logOptionsUA),
            new ClientOptions().setApplicationId(clientOptionsUA), new ApplicationIdUAStringTestClient(expectedUA),
            new ArrayList<>(), new ArrayList<>(), null, null, new ClientLogger(BuilderHelperTests.class), null, null);

        StepVerifier.create(pipeline.send(request(ENDPOINT)))
            .assertNext(it -> assertEquals(200, it.getStatusCode()))
            .verifyComplete();
    }

    private static Stream<Arguments> customApplicationIdInUAStringSupplier() {
        return Stream.of(Arguments.of("log-options-id", null, "log-options-id"),
            Arguments.of(null, "client-options-id", "client-options-id"),
            // Client options preferred over log options
            Arguments.of("log-options-id", "client-options-id", "client-options-id"));
    }

    /**
     * Tests that a user application id will be honored in the UA string when using the serviceClientBuilder's
     * default pipeline.
     */
    @SuppressWarnings("deprecation")
    @ParameterizedTest
    @MethodSource("customApplicationIdInUAStringSupplier")
    public void serviceClientCustomApplicationIdInUAString(String logOptionsUA, String clientOptionsUA,
        String expectedUA) {
        BlobServiceClient serviceClient = new BlobServiceClientBuilder().endpoint(ENDPOINT)
            .credential(CREDENTIALS)
            .httpLogOptions(new HttpLogOptions().setApplicationId(logOptionsUA))
            .clientOptions(new ClientOptions().setApplicationId(clientOptionsUA))
            .httpClient(new ApplicationIdUAStringTestClient(expectedUA))
            .buildClient();

        StepVerifier.create(serviceClient.getHttpPipeline().send(request(serviceClient.getAccountUrl())))
            .assertNext(it -> assertEquals(200, it.getStatusCode()))
            .verifyComplete();
    }

    /**
     * Tests that a user application id will be honored in the UA string when using the serviceClientBuilder
     * default pipeline.
     */
    @SuppressWarnings("deprecation")
    @ParameterizedTest
    @MethodSource("customApplicationIdInUAStringSupplier")
    public void containerClientcustomApplicationIdInUAString(String logOptionsUA, String clientOptionsUA,
        String expectedUA) {
        BlobContainerClient containerClient = new BlobContainerClientBuilder().endpoint(ENDPOINT)
            .containerName("container")
            .credential(CREDENTIALS)
            .httpLogOptions(new HttpLogOptions().setApplicationId(logOptionsUA))
            .clientOptions(new ClientOptions().setApplicationId(clientOptionsUA))
            .httpClient(new ApplicationIdUAStringTestClient(expectedUA))
            .buildClient();

        StepVerifier.create(containerClient.getHttpPipeline().send(request(containerClient.getBlobContainerUrl())))
            .assertNext(it -> assertEquals(200, it.getStatusCode()))
            .verifyComplete();
    }

    /**
     * Tests that a user application id will be honored in the UA string when using the blobClientBuilder default pipeline.
     */
    @SuppressWarnings("deprecation")
    @ParameterizedTest
    @MethodSource("customApplicationIdInUAStringSupplier")
    public void blobClientcustomApplicationIdInUAString(String logOptionsUA, String clientOptionsUA,
        String expectedUA) {
        BlobClient blobClient = new BlobClientBuilder().endpoint(ENDPOINT)
            .containerName("container")
            .blobName("blob")
            .credential(CREDENTIALS)
            .httpLogOptions(new HttpLogOptions().setApplicationId(logOptionsUA))
            .clientOptions(new ClientOptions().setApplicationId(clientOptionsUA))
            .httpClient(new ApplicationIdUAStringTestClient(expectedUA))
            .buildClient();

        StepVerifier.create(blobClient.getHttpPipeline().send(request(blobClient.getBlobUrl())))
            .assertNext(it -> assertEquals(200, it.getStatusCode()))
            .verifyComplete();
    }

    /**
     * Tests that a user application id will be honored in the UA string when using the specializedBlobClientBuilder
     * default pipeline.
     */
    @SuppressWarnings("deprecation")
    @ParameterizedTest
    @MethodSource("customApplicationIdInUAStringSupplier")
    public void specializedBlobClientCustomApplicationIdInUAString(String logOptionsUA, String clientOptionsUA,
        String expectedUA) {
        SpecializedBlobClientBuilder specializedBlobClientBuilder
            = new SpecializedBlobClientBuilder().endpoint(ENDPOINT)
                .containerName("container")
                .blobName("blob")
                .credential(CREDENTIALS)
                .httpLogOptions(new HttpLogOptions().setApplicationId(logOptionsUA))
                .clientOptions(new ClientOptions().setApplicationId(clientOptionsUA))
                .httpClient(new ApplicationIdUAStringTestClient(expectedUA));

        AppendBlobClient appendBlobClient = specializedBlobClientBuilder.buildAppendBlobClient();

        StepVerifier.create(appendBlobClient.getHttpPipeline().send(request(appendBlobClient.getBlobUrl())))
            .assertNext(it -> assertEquals(200, it.getStatusCode()))
            .verifyComplete();

        BlockBlobClient blockBlobClient = specializedBlobClientBuilder.buildBlockBlobClient();

        StepVerifier.create(blockBlobClient.getHttpPipeline().send(request(blockBlobClient.getBlobUrl())))
            .assertNext(it -> assertEquals(200, it.getStatusCode()))
            .verifyComplete();

        PageBlobClient pageBlobClient = specializedBlobClientBuilder.buildPageBlobClient();

        StepVerifier.create(pageBlobClient.getHttpPipeline().send(request(pageBlobClient.getBlobUrl())))
            .assertNext(it -> assertEquals(200, it.getStatusCode()))
            .verifyComplete();
    }

    /**
     * Tests that a custom headers will be honored when using the default pipeline builder.
     */
    @Test
    public void customHeadersClientOptions() {
        List<Header> headers = new ArrayList<>();
        headers.add(new Header("custom", "header"));
        headers.add(new Header("Authorization", "notthis"));
        headers.add(new Header("User-Agent", "overwritten"));

        HttpPipeline pipeline = BuilderHelper.buildPipeline(CREDENTIALS, null, null, null, ENDPOINT,
            new RequestRetryOptions(), null, BuilderHelper.getDefaultHttpLogOptions(),
            new ClientOptions().setHeaders(headers), new ClientOptionsHeadersTestClient(headers), new ArrayList<>(),
            new ArrayList<>(), null, null, new ClientLogger(BuilderHelperTests.class), null, null);

        StepVerifier.create(pipeline.send(request(ENDPOINT)))
            .assertNext(it -> assertEquals(200, it.getStatusCode()))
            .verifyComplete();
    }

    /**
     * Tests that custom headers will be honored when using the serviceClienBuilder's default pipeline.
     */
    @Test
    public void serviceClientcustomHeadersClientoptions() {
        List<Header> headers = new ArrayList<>();
        headers.add(new Header("custom", "header"));
        headers.add(new Header("Authorization", "notthis"));
        headers.add(new Header("User-Agent", "overwritten"));

        BlobServiceClient serviceClient = new BlobServiceClientBuilder().endpoint(ENDPOINT)
            .credential(CREDENTIALS)
            .clientOptions(new ClientOptions().setHeaders(headers))
            .httpClient(new ClientOptionsHeadersTestClient(headers))
            .buildClient();

        StepVerifier.create(serviceClient.getHttpPipeline().send(request(serviceClient.getAccountUrl())))
            .assertNext(it -> assertEquals(200, it.getStatusCode()))
            .verifyComplete();
    }

    /**
     * Tests that custom headers will be honored when using the serviceClientBuilder's default pipeline.
     */
    @Test
    public void containerClientCustomHeadersClientOptions() {
        List<Header> headers = new ArrayList<>();
        headers.add(new Header("custom", "header"));
        headers.add(new Header("Authorization", "notthis"));
        headers.add(new Header("User-Agent", "overwritten"));

        BlobContainerClient containerClient = new BlobContainerClientBuilder().endpoint(ENDPOINT)
            .containerName("container")
            .credential(CREDENTIALS)
            .clientOptions(new ClientOptions().setHeaders(headers))
            .httpClient(new ClientOptionsHeadersTestClient(headers))
            .buildClient();

        StepVerifier.create(containerClient.getHttpPipeline().send(request(containerClient.getBlobContainerUrl())))
            .assertNext(it -> assertEquals(200, it.getStatusCode()))
            .verifyComplete();
    }

    /**
     * Tests that custom headers will be honored when using the blobClientBuilder's default pipeline.
     */
    @Test
    public void blobClientCustomHeadersClientoptions() {
        List<Header> headers = new ArrayList<>();
        headers.add(new Header("custom", "header"));
        headers.add(new Header("Authorization", "notthis"));
        headers.add(new Header("User-Agent", "overwritten"));

        BlobClient blobClient = new BlobClientBuilder().endpoint(ENDPOINT)
            .containerName("container")
            .blobName("blob")
            .credential(CREDENTIALS)
            .clientOptions(new ClientOptions().setHeaders(headers))
            .httpClient(new ClientOptionsHeadersTestClient(headers))
            .buildClient();

        StepVerifier.create(blobClient.getHttpPipeline().send(request(blobClient.getBlobUrl())))
            .assertNext(it -> assertEquals(200, it.getStatusCode()))
            .verifyComplete();
    }

    /**
     * Tests that custom headers will be honored when using the specializedBlobClientBuilder's default
     * pipeline.
     */
    @Test
    public void specializedBlobClientCustomHeadersClientOptions() {
        List<Header> headers = new ArrayList<>();
        headers.add(new Header("custom", "header"));
        headers.add(new Header("Authorization", "notthis"));
        headers.add(new Header("User-Agent", "overwritten"));

        SpecializedBlobClientBuilder specializedBlobClientBuilder
            = new SpecializedBlobClientBuilder().endpoint(ENDPOINT)
                .containerName("container")
                .blobName("blob")
                .credential(CREDENTIALS)
                .clientOptions(new ClientOptions().setHeaders(headers))
                .httpClient(new ClientOptionsHeadersTestClient(headers));

        AppendBlobClient appendBlobClient = specializedBlobClientBuilder.buildAppendBlobClient();

        StepVerifier.create(appendBlobClient.getHttpPipeline().send(request(appendBlobClient.getBlobUrl())))
            .assertNext(it -> assertEquals(200, it.getStatusCode()))
            .verifyComplete();

        BlockBlobClient blockBlobClient = specializedBlobClientBuilder.buildBlockBlobClient();

        StepVerifier.create(blockBlobClient.getHttpPipeline().send(request(blockBlobClient.getBlobUrl())))
            .assertNext(it -> assertEquals(200, it.getStatusCode()))
            .verifyComplete();

        PageBlobClient pageBlobClient = specializedBlobClientBuilder.buildPageBlobClient();

        StepVerifier.create(pageBlobClient.getHttpPipeline().send(request(pageBlobClient.getBlobUrl())))
            .assertNext(it -> assertEquals(200, it.getStatusCode()))
            .verifyComplete();
    }

    @Test
    public void doesNotThrowOnAmbiguousCredentialsWithoutAzureSasCredential() {
        assertDoesNotThrow(() -> new BlobClientBuilder().endpoint(ENDPOINT)
            .blobName("foo")
            .credential(new StorageSharedKeyCredential("foo", "bar"))
            .credential(new MockTokenCredential())
            .sasToken("foo")
            .httpClient(new NoOpHttpClient())
            .buildClient());

        assertDoesNotThrow(() -> new SpecializedBlobClientBuilder().endpoint(ENDPOINT)
            .blobName("foo")
            .credential(new StorageSharedKeyCredential("foo", "bar"))
            .credential(new MockTokenCredential())
            .sasToken("foo")
            .httpClient(new NoOpHttpClient())
            .buildBlockBlobClient());

        assertDoesNotThrow(() -> new BlobContainerClientBuilder().endpoint(ENDPOINT)
            .credential(new StorageSharedKeyCredential("foo", "bar"))
            .credential(new MockTokenCredential())
            .sasToken("foo")
            .httpClient(new NoOpHttpClient())
            .buildClient());

        assertDoesNotThrow(() -> new BlobServiceClientBuilder().endpoint(ENDPOINT)
            .credential(new StorageSharedKeyCredential("foo", "bar"))
            .credential(new MockTokenCredential())
            .sasToken("foo")
            .httpClient(new NoOpHttpClient())
            .buildClient());
    }

    @Test
    public void throwsOnAmbiguousCredentialsWithAzureSasCredential() {
        assertThrows(IllegalStateException.class,
            () -> new BlobClientBuilder().endpoint(ENDPOINT)
                .blobName("foo")
                .credential(new StorageSharedKeyCredential("foo", "bar"))
                .credential(new AzureSasCredential("foo"))
                .buildClient());

        assertThrows(IllegalStateException.class,
            () -> new BlobClientBuilder().endpoint(ENDPOINT)
                .blobName("foo")
                .sasToken("foo")
                .credential(new AzureSasCredential("foo"))
                .buildClient());

        assertThrows(IllegalStateException.class,
            () -> new BlobClientBuilder().endpoint(ENDPOINT + "?sig=foo")
                .blobName("foo")
                .credential(new AzureSasCredential("foo"))
                .buildClient());

        assertThrows(IllegalStateException.class,
            () -> new SpecializedBlobClientBuilder().endpoint(ENDPOINT)
                .blobName("foo")
                .credential(new StorageSharedKeyCredential("foo", "bar"))
                .credential(new AzureSasCredential("foo"))
                .buildBlockBlobClient());

        assertThrows(IllegalStateException.class,
            () -> new SpecializedBlobClientBuilder().endpoint(ENDPOINT)
                .blobName("foo")
                .sasToken("foo")
                .credential(new AzureSasCredential("foo"))
                .buildBlockBlobClient());

        assertThrows(IllegalStateException.class,
            () -> new SpecializedBlobClientBuilder().endpoint(ENDPOINT + "?sig=foo")
                .blobName("foo")
                .credential(new AzureSasCredential("foo"))
                .buildBlockBlobClient());

        assertThrows(IllegalStateException.class,
            () -> new BlobContainerClientBuilder().endpoint(ENDPOINT)
                .credential(new StorageSharedKeyCredential("foo", "bar"))
                .credential(new AzureSasCredential("foo"))
                .buildClient());

        assertThrows(IllegalStateException.class,
            () -> new BlobContainerClientBuilder().endpoint(ENDPOINT)
                .sasToken("foo")
                .credential(new AzureSasCredential("foo"))
                .buildClient());

        assertThrows(IllegalStateException.class,
            () -> new BlobContainerClientBuilder().endpoint(ENDPOINT + "?sig=foo")
                .credential(new AzureSasCredential("foo"))
                .buildClient());

        assertThrows(IllegalStateException.class,
            () -> new BlobServiceClientBuilder().endpoint(ENDPOINT)
                .credential(new StorageSharedKeyCredential("foo", "bar"))
                .credential(new AzureSasCredential("foo"))
                .buildClient());

        assertThrows(IllegalStateException.class,
            () -> new BlobServiceClientBuilder().endpoint(ENDPOINT)
                .sasToken("foo")
                .credential(new AzureSasCredential("foo"))
                .buildClient());

        assertThrows(IllegalStateException.class,
            () -> new BlobServiceClientBuilder().endpoint(ENDPOINT + "?sig=foo")
                .credential(new AzureSasCredential("foo"))
                .buildClient());
    }

    @ParameterizedTest
    @MethodSource("blobAccountNameSupplier")
    void secondaryIpv6Dualstack(String urlString, String expectedAccountName) throws MalformedURLException {
        BlobUrlParts blobUrlParts = BlobUrlParts.parse(new URL(urlString));

        assertEquals("https", blobUrlParts.getScheme());
        assertEquals(expectedAccountName, blobUrlParts.getAccountName());
        assertEquals("", blobUrlParts.getBlobContainerName());
        assertNull(blobUrlParts.getSnapshot());
        assertEquals("", blobUrlParts.getCommonSasQueryParameters().encode());
        assertNull(blobUrlParts.getVersionId());

        // Verify the endpoint can be used to reconstruct the original URL
        String newUri = blobUrlParts.toUrl().toString();
        assertEquals(urlString, newUri);
    }

    private static Stream<Arguments> blobAccountNameSupplier() {
        return Stream.of(Arguments.of("https://myaccount.blob.core.windows.net/", "myaccount"),
            Arguments.of("https://myaccount-secondary.blob.core.windows.net/", "myaccount"),
            Arguments.of("https://myaccount-dualstack.blob.core.windows.net/", "myaccount"),
            Arguments.of("https://myaccount-ipv6.blob.core.windows.net/", "myaccount"),
            Arguments.of("https://myaccount-secondary-dualstack.blob.core.windows.net/", "myaccount"),
            Arguments.of("https://myaccount-secondary-ipv6.blob.core.windows.net/", "myaccount"));
    }

    @ParameterizedTest
    @MethodSource("blobManagedDiskAccountNameSupplier")
    void ipv6InternalAccounts(String urlString, String expectedAccountName) throws MalformedURLException {
        BlobUrlParts blobUrlParts = BlobUrlParts.parse(new URL(urlString));

        assertEquals("https", blobUrlParts.getScheme());
        assertEquals(expectedAccountName, blobUrlParts.getAccountName());
        assertEquals("", blobUrlParts.getBlobContainerName());
        assertNull(blobUrlParts.getSnapshot());
        assertEquals("", blobUrlParts.getCommonSasQueryParameters().encode());
        assertNull(blobUrlParts.getVersionId());

        // Verify the endpoint can be used to reconstruct the original URL
        String newUri = blobUrlParts.toUrl().toString();
        assertEquals(urlString, newUri);
    }

    private static Stream<Arguments> blobManagedDiskAccountNameSupplier() {
        return Stream.of(Arguments.of("https://md-d3rqxhqbxbwq.blob.core.windows.net/", "md-d3rqxhqbxbwq"),
            Arguments.of("https://md-ssd-bndub02px100c21.blob.core.windows.net/", "md-ssd-bndub02px100c21"));
    }

    @Test
    public void onlyOneRetryOptionsCanBeApplied() {
        assertThrows(IllegalStateException.class,
            () -> new BlobServiceClientBuilder().endpoint(ENDPOINT)
                .credential(CREDENTIALS)
                .retryOptions(REQUEST_RETRY_OPTIONS)
                .retryOptions(CORE_RETRY_OPTIONS)
                .buildClient());

        assertThrows(IllegalStateException.class,
            () -> new BlobContainerClientBuilder().endpoint(ENDPOINT)
                .credential(CREDENTIALS)
                .containerName("foo")
                .retryOptions(REQUEST_RETRY_OPTIONS)
                .retryOptions(CORE_RETRY_OPTIONS)
                .buildClient());

        assertThrows(IllegalStateException.class,
            () -> new BlobClientBuilder().endpoint(ENDPOINT)
                .credential(CREDENTIALS)
                .containerName("foo")
                .blobName("foo")
                .retryOptions(REQUEST_RETRY_OPTIONS)
                .retryOptions(CORE_RETRY_OPTIONS)
                .buildClient());

        assertThrows(IllegalStateException.class,
            () -> new SpecializedBlobClientBuilder().endpoint(ENDPOINT)
                .credential(CREDENTIALS)
                .containerName("foo")
                .blobName("foo")
                .retryOptions(REQUEST_RETRY_OPTIONS)
                .retryOptions(CORE_RETRY_OPTIONS)
                .buildBlockBlobClient());
    }

    private static final class FreshDateTestClient implements HttpClient {
        private DateTimeRfc1123 firstDate;

        @Override
        public Mono<HttpResponse> send(HttpRequest request) {
            if (firstDate == null) {
                firstDate = convertToDateObject(request.getHeaders().getValue(HttpHeaderName.DATE));
                return Mono.error(new IOException("IOException!"));
            }

            assertNotEquals(firstDate, convertToDateObject(request.getHeaders().getValue(HttpHeaderName.DATE)));
            return Mono.just(new MockHttpResponse(request, 200));
        }

        private static DateTimeRfc1123 convertToDateObject(String dateHeader) {
            if (CoreUtils.isNullOrEmpty(dateHeader)) {
                throw new RuntimeException("Failed to set 'Date' header.");
            }

            return new DateTimeRfc1123(dateHeader);
        }
    }

    private static final class ApplicationIdUAStringTestClient implements HttpClient {

        private final String expectedUA;

        ApplicationIdUAStringTestClient(String expectedUA) {
            this.expectedUA = expectedUA;
        }

        @Override
        public Mono<HttpResponse> send(HttpRequest request) {
            if (CoreUtils.isNullOrEmpty(request.getHeaders().getValue(HttpHeaderName.USER_AGENT))) {
                throw new RuntimeException("Failed to set 'User-Agent' header.");
            }
            assertTrue(request.getHeaders().getValue(HttpHeaderName.USER_AGENT).startsWith(expectedUA));
            return Mono.just(new MockHttpResponse(request, 200));
        }
    }

    private static final class ClientOptionsHeadersTestClient implements HttpClient {

        private final Iterable<Header> headers;

        ClientOptionsHeadersTestClient(Iterable<Header> headers) {
            this.headers = headers;
        }

        @SuppressWarnings("deprecation")
        @Override
        public Mono<HttpResponse> send(HttpRequest request) {
            headers.forEach(header -> {
                if (CoreUtils.isNullOrEmpty(request.getHeaders().getValue(header.getName()))) {
                    throw new RuntimeException("Failed to set custom header " + header.getName());
                }
                // This is meant to not match.
                if (Objects.equals(header.getName(), "Authorization")) {
                    if (Objects.equals(request.getHeaders().getValue(header.getName()), header.getValue())) {
                        throw new RuntimeException("Custom header " + header.getName() + " did not match expectation.");
                    }
                } else {
                    if (!Objects.equals(request.getHeaders().getValue(header.getName()), header.getValue())) {
                        throw new RuntimeException("Custom header " + header.getName() + " did not match expectation.");
                    }
                }
            });
            return Mono.just(new MockHttpResponse(request, 200));
        }
    }

    // region buildPipeline session tests

    @Test
    public void buildPipelineWithTokenCredentialAlwaysHasSessionPolicy() {
        HttpPipeline pipeline = buildBearerPipeline();

        assertTrue(hasPolicyOfType(pipeline, "SessionTokenCredentialPolicy"),
            "Pipeline with tokenCredential should always contain SessionTokenCredentialPolicy");
    }

    @Test
    public void buildPipelineWithSharedKeyDoesNotHaveSessionPolicy() {
        HttpPipeline pipeline = buildSharedKeyPipeline();

        assertFalse(hasPolicyOfType(pipeline, "SessionTokenCredentialPolicy"),
            "Pipeline with shared key should not contain SessionTokenCredentialPolicy");
    }

    /**
     * Helper to build a pipeline with bearer token auth.
     */
    private static HttpPipeline buildBearerPipeline() {
        return BuilderHelper.buildPipeline(null, new MockTokenCredential(), null, null, ENDPOINT,
            new RequestRetryOptions(), null, BuilderHelper.getDefaultHttpLogOptions(), new ClientOptions(),
            new NoOpHttpClient(), new ArrayList<>(), new ArrayList<>(), null, null,
            new ClientLogger(BuilderHelperTests.class), null, BlobServiceVersion.getLatest());
    }

    /**
     * Helper to build a pipeline without bearer token auth (shared key only).
     */
    private static HttpPipeline buildSharedKeyPipeline() {
        return BuilderHelper.buildPipeline(CREDENTIALS, null, null, null, ENDPOINT, new RequestRetryOptions(), null,
            BuilderHelper.getDefaultHttpLogOptions(), new ClientOptions(), new NoOpHttpClient(), new ArrayList<>(),
            new ArrayList<>(), null, null, new ClientLogger(BuilderHelperTests.class), null, null);
    }

    /**
     * Checks whether the pipeline contains a policy whose simple class name matches the given name.
     */
    private static boolean hasPolicyOfType(HttpPipeline pipeline, String simpleClassName) {
        for (int i = 0; i < pipeline.getPolicyCount(); i++) {
            if (pipeline.getPolicy(i).getClass().getSimpleName().equals(simpleClassName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the index of the first policy whose simple class name matches, or -1 if not found.
     */
    private static int indexOfPolicy(HttpPipeline pipeline, String simpleClassName) {
        for (int i = 0; i < pipeline.getPolicyCount(); i++) {
            if (pipeline.getPolicy(i).getClass().getSimpleName().equals(simpleClassName)) {
                return i;
            }
        }
        return -1;
    }

    // endregion

    // region BlobClientBuilder sessionOptions tests

    @Test
    public void blobBuilderWithSingleSpecifiedContainerSessionBuilds() {
        SessionOptions options = new SessionOptions().setSessionMode(SessionMode.SINGLE_SPECIFIED_CONTAINER);

        assertDoesNotThrow(() -> new BlobClientBuilder().endpoint(ENDPOINT)
            .containerName("mycontainer")
            .blobName("myblob")
            .credential(new MockTokenCredential())
            .httpClient(new NoOpHttpClient())
            .sessionOptions(options)
            .buildClient());
    }

    @Test
    public void blobBuilderWithSingleSpecifiedContainerSessionAndNoContainerNameThrows() {
        SessionOptions options = new SessionOptions().setSessionMode(SessionMode.SINGLE_SPECIFIED_CONTAINER);

        assertThrows(IllegalArgumentException.class,
            () -> new BlobClientBuilder().endpoint(ENDPOINT)
                .blobName("myblob")
                .credential(new MockTokenCredential())
                .httpClient(new NoOpHttpClient())
                .sessionOptions(options)
                .buildClient());
    }

    @Test
    public void blobBuilderWithoutSessionOptionsBuilds() {
        assertDoesNotThrow(() -> new BlobClientBuilder().endpoint(ENDPOINT)
            .containerName("mycontainer")
            .blobName("myblob")
            .credential(new MockTokenCredential())
            .httpClient(new NoOpHttpClient())
            .buildClient());
    }

    // endregion

    // region BlobContainerClientBuilder sessionOptions tests

    @Test
    public void containerBuilderWithSessionOptionsAlwaysAndContainerNameSucceeds() {
        SessionOptions options = new SessionOptions().setSessionMode(SessionMode.SINGLE_SPECIFIED_CONTAINER);

        assertDoesNotThrow(() -> new BlobContainerClientBuilder().endpoint(ENDPOINT)
            .containerName("mycontainer")
            .credential(new MockTokenCredential())
            .httpClient(new NoOpHttpClient())
            .sessionOptions(options)
            .buildClient());
    }

    @Test
    public void containerBuilderWithSessionOptionsAlwaysAndNoContainerNameThrows() {
        SessionOptions options = new SessionOptions().setSessionMode(SessionMode.SINGLE_SPECIFIED_CONTAINER);

        assertThrows(IllegalArgumentException.class,
            () -> new BlobContainerClientBuilder().endpoint(ENDPOINT)
                .credential(new MockTokenCredential())
                .httpClient(new NoOpHttpClient())
                .sessionOptions(options)
                .buildClient());
    }

    @Test
    public void containerBuilderWithSessionOptionsNoneAndNoContainerNameSucceeds() {
        SessionOptions options = new SessionOptions().setSessionMode(SessionMode.NONE);

        assertDoesNotThrow(() -> new BlobContainerClientBuilder().endpoint(ENDPOINT)
            .credential(new MockTokenCredential())
            .httpClient(new NoOpHttpClient())
            .sessionOptions(options)
            .buildClient());
    }

    @Test
    public void containerBuilderWithNoSessionOptionsSucceeds() {
        assertDoesNotThrow(() -> new BlobContainerClientBuilder().endpoint(ENDPOINT)
            .containerName("mycontainer")
            .credential(new MockTokenCredential())
            .httpClient(new NoOpHttpClient())
            .buildClient());
    }

    // endregion

    // region environment variable session activation tests

    private static Configuration envConfiguration(String mode, String container) {
        TestConfigurationSource envSource = new TestConfigurationSource();
        if (mode != null) {
            envSource.put(BuilderHelper.PROPERTY_AZURE_STORAGE_SESSION_MODE, mode);
        }
        if (container != null) {
            envSource.put(BuilderHelper.PROPERTY_AZURE_STORAGE_SESSION_CONTAINER_NAME, container);
        }
        return new ConfigurationBuilder(new TestConfigurationSource(), new TestConfigurationSource(), envSource)
            .build();
    }

    @Test
    public void containerBuilderActivatesSessionFromEnvWhenNothingExplicit() {
        Configuration config = envConfiguration("SINGLE_SPECIFIED_CONTAINER", "envcontainer");

        assertDoesNotThrow(() -> new BlobContainerClientBuilder().endpoint(ENDPOINT)
            .credential(new MockTokenCredential())
            .httpClient(new NoOpHttpClient())
            .configuration(config)
            .buildClient());
    }

    @Test
    public void containerBuilderEnvModeWithoutContainerNameStillThrows() {
        Configuration config = envConfiguration("SINGLE_SPECIFIED_CONTAINER", null);

        assertThrows(IllegalArgumentException.class,
            () -> new BlobContainerClientBuilder().endpoint(ENDPOINT)
                .credential(new MockTokenCredential())
                .httpClient(new NoOpHttpClient())
                .configuration(config)
                .buildClient());
    }

    @Test
    public void containerBuilderEnvModeIsCaseInsensitive() {
        Configuration config = envConfiguration("single_specified_container", "envcontainer");

        assertDoesNotThrow(() -> new BlobContainerClientBuilder().endpoint(ENDPOINT)
            .credential(new MockTokenCredential())
            .httpClient(new NoOpHttpClient())
            .configuration(config)
            .buildClient());
    }

    @Test
    public void containerBuilderInvalidEnvModeThrows() {
        Configuration config = envConfiguration("NOT_A_REAL_MODE", "envcontainer");

        assertThrows(IllegalArgumentException.class,
            () -> new BlobContainerClientBuilder().endpoint(ENDPOINT)
                .credential(new MockTokenCredential())
                .httpClient(new NoOpHttpClient())
                .configuration(config)
                .buildClient());
    }

    @Test
    public void containerBuilderExplicitSessionModeOverridesEnv() {
        Configuration config = envConfiguration("SINGLE_SPECIFIED_CONTAINER", "envcontainer");
        SessionOptions explicitNone = new SessionOptions().setSessionMode(SessionMode.NONE);

        // Explicit NONE must not be upgraded by env vars and no container is required.
        assertDoesNotThrow(() -> new BlobContainerClientBuilder().endpoint(ENDPOINT)
            .credential(new MockTokenCredential())
            .httpClient(new NoOpHttpClient())
            .configuration(config)
            .sessionOptions(explicitNone)
            .buildClient());
    }

    @Test
    public void containerBuilderExplicitContainerNameWinsOverEnv() {
        Configuration config = envConfiguration("SINGLE_SPECIFIED_CONTAINER", "envcontainer");
        SessionOptions options = new SessionOptions().setContainerName("explicitcontainer");

        assertDoesNotThrow(() -> new BlobContainerClientBuilder().endpoint(ENDPOINT)
            .containerName("explicitcontainer")
            .credential(new MockTokenCredential())
            .httpClient(new NoOpHttpClient())
            .configuration(config)
            .sessionOptions(options)
            .buildClient());

        assertEquals("explicitcontainer", options.getContainerName());
    }

    @Test
    public void blobBuilderActivatesSessionFromEnvWhenNothingExplicit() {
        Configuration config = envConfiguration("SINGLE_SPECIFIED_CONTAINER", "envcontainer");

        assertDoesNotThrow(() -> new BlobClientBuilder().endpoint(ENDPOINT)
            .blobName("myblob")
            .credential(new MockTokenCredential())
            .httpClient(new NoOpHttpClient())
            .configuration(config)
            .buildClient());
    }

    @Test
    public void serviceBuilderActivatesSessionFromEnvWhenNothingExplicit() {
        Configuration config = envConfiguration("SINGLE_SPECIFIED_CONTAINER", "envcontainer");
        SessionOptions options = new SessionOptions();

        assertDoesNotThrow(() -> new BlobServiceClientBuilder().endpoint(ENDPOINT)
            .credential(new MockTokenCredential())
            .httpClient(new NoOpHttpClient())
            .configuration(config)
            .sessionOptions(options)
            .buildClient());

        // Env vars must have flowed through to the SessionOptions instance.
        assertEquals(SessionMode.SINGLE_SPECIFIED_CONTAINER, options.getSessionMode());
        assertEquals("envcontainer", options.getContainerName());
    }

    @Test
    public void applyEnvironmentSessionDefaultsLeavesExplicitValuesIntact() {
        Configuration config = envConfiguration("SINGLE_SPECIFIED_CONTAINER", "envcontainer");
        SessionOptions options = new SessionOptions().setSessionMode(SessionMode.NONE).setContainerName("explicit");

        BuilderHelper.applyEnvironmentSessionDefaults(options, config, new ClientLogger(BuilderHelperTests.class));

        assertEquals(SessionMode.NONE, options.getSessionMode());
        assertEquals("explicit", options.getContainerName());
    }

    @Test
    public void applyEnvironmentSessionDefaultsAppliesOnlyContainerNameWhenModeExplicit() {
        Configuration config = envConfiguration(null, "envcontainer");
        SessionOptions options = new SessionOptions().setSessionMode(SessionMode.SINGLE_SPECIFIED_CONTAINER);

        BuilderHelper.applyEnvironmentSessionDefaults(options, config, new ClientLogger(BuilderHelperTests.class));

        assertEquals(SessionMode.SINGLE_SPECIFIED_CONTAINER, options.getSessionMode());
        assertEquals("envcontainer", options.getContainerName());
    }

    // endregion

    // region environment-variable end-to-end test
    //
    // This single test verifies that a customer can activate the session feature with NO code
    // change at all -- just by exporting environment variables before starting the JVM:
    //
    //     set AZURE_STORAGE_SESSION_MODE=SINGLE_SPECIFIED_CONTAINER
    //     set AZURE_STORAGE_SESSION_CONTAINER_NAME=mycontainer
    //
    // It is @Disabled by default because:
    //   1. CI doesn't (and shouldn't) set these process-level env vars.
    //   2. EnvironmentConfiguration in azure-core caches reads from the global Configuration
    //      for the lifetime of the JVM, so it cannot be reliably reset between tests inside
    //      the same Surefire fork.
    //
    // To run it manually after setting the env vars above:
    //
    //     mvn -pl sdk/storage/azure-storage-blob test ^
    //       "-Dtest=BuilderHelperTests#environmentVariablesActivateSession"
    //
    // The 10 injection-based tests above already cover all of the helper's branching logic
    // by injecting a Configuration directly; this test exists only to prove the real
    // System.getenv lookup path also works.

    @Test
    @Disabled("Run manually after exporting AZURE_STORAGE_SESSION_MODE=SINGLE_SPECIFIED_CONTAINER and "
        + "AZURE_STORAGE_SESSION_CONTAINER_NAME=<name>. See the comment above for details.")
    public void environmentVariablesActivateSession() {
        SessionOptions options = new SessionOptions();
        assertDoesNotThrow(() -> new BlobContainerClientBuilder().endpoint(ENDPOINT)
            .credential(new MockTokenCredential())
            .httpClient(new NoOpHttpClient())
            .sessionOptions(options)
            .buildClient());

        String expectedContainer = System.getenv(BuilderHelper.PROPERTY_AZURE_STORAGE_SESSION_CONTAINER_NAME);
        assertEquals(SessionMode.SINGLE_SPECIFIED_CONTAINER, options.getSessionMode(),
            "Expected env var AZURE_STORAGE_SESSION_MODE=SINGLE_SPECIFIED_CONTAINER to populate sessionMode");
        assertEquals(expectedContainer, options.getContainerName(),
            "Expected env var AZURE_STORAGE_SESSION_CONTAINER_NAME to populate containerName");
    }

    // endregion
}
