// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.credential.AzureSasCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.FixedDelayOptions;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryOptions;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.test.http.NoOpHttpClient;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.DateTimeRfc1123;
import com.azure.core.util.Header;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.implementation.util.BuilderHelper;
import com.azure.storage.blob.models.SessionCredential;
import com.azure.storage.blob.models.SessionMode;
import com.azure.storage.blob.models.SessionOptions;
import com.azure.storage.blob.models.SessionProvider;
import com.azure.storage.blob.models.SessionRequestContext;
import com.azure.storage.blob.specialized.AppendBlobClient;
import com.azure.storage.blob.specialized.BlockBlobClient;
import com.azure.storage.blob.specialized.PageBlobClient;
import com.azure.storage.blob.specialized.SpecializedBlobClientBuilder;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.policy.RequestRetryOptions;
import com.azure.storage.common.policy.RetryPolicyType;
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
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
    private static final String CREATE_SESSION_RESPONSE_BODY = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
        + "<CreateSessionResult><Id>session-id</Id><Expiration>" + OffsetDateTime.now().plusHours(1)
        + "</Expiration><AuthenticationType>HMAC</AuthenticationType><Credentials>"
        + "<SessionToken>session-token</SessionToken>"
        + "<SessionKey>dGVzdFNlc3Npb25LZXkxMjM0NTY3ODkwMTIzNDU2Nzg5MA==</SessionKey>"
        + "</Credentials></CreateSessionResult>";

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

    @ParameterizedTest(name = "{0}")
    @MethodSource("pipelinesWithoutSessionsSupplier")
    public void pipelinesWithoutSessionsDoNotContainSessionPolicy(String scenario,
        Supplier<HttpPipeline> pipelineSupplier, boolean expectsBearerPolicy) {
        HttpPipeline pipeline = pipelineSupplier.get();

        assertFalse(hasPolicyOfType(pipeline, "SessionTokenCredentialPolicy"),
            scenario + " should not contain SessionTokenCredentialPolicy");
        assertEquals(expectsBearerPolicy, hasPolicyOfType(pipeline, "StorageBearerTokenChallengeAuthorizationPolicy"),
            scenario + " bearer policy expectation mismatch");
    }

    @Test
    public void serviceBuilderUsesBuiltInSessionProviderByDefault() {
        BlobServiceClient client = new BlobServiceClientBuilder().endpoint(ENDPOINT)
            .credential(new MockTokenCredential())
            .httpClient(new NoOpHttpClient())
            .buildClient();

        assertTrue(hasPolicyOfType(client.getHttpPipeline(), "SessionTokenCredentialPolicy"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("standaloneSessionPipelineSupplier")
    public void standaloneBlobBuildersUseBuiltInSessionProviderByDefault(String scenario,
        Supplier<HttpPipeline> pipelineSupplier) {
        assertTrue(hasPolicyOfType(pipelineSupplier.get(), "SessionTokenCredentialPolicy"),
            scenario + " should contain SessionTokenCredentialPolicy");
    }

    /**
     * Session credentials are bound to the network context of the CreateSession call, so the session creation
     * pipeline must run the same post-authentication policies as the data pipeline.
     */
    @Test
    public void perRetryPoliciesAreAppliedToCreateSessionRequests() {
        List<HttpMethod> observedMethods = Collections.synchronizedList(new ArrayList<>());
        HttpPipelinePolicy perRetryPolicy = (context, next) -> {
            observedMethods.add(context.getHttpRequest().getHttpMethod());
            return next.process();
        };

        HttpClient sessionClient = request -> {
            if (request.getHttpMethod() == HttpMethod.POST) {
                HttpHeaders headers = new HttpHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/xml");
                return Mono.just(new MockHttpResponse(request, 201, headers,
                    CREATE_SESSION_RESPONSE_BODY.getBytes(StandardCharsets.UTF_8)));
            }

            return Mono.just(new MockHttpResponse(request, 200));
        };

        HttpPipeline pipeline = BuilderHelper.buildPipeline(null, new MockTokenCredential(), null, null, ENDPOINT,
            REQUEST_RETRY_OPTIONS, null, BuilderHelper.getDefaultHttpLogOptions(), new ClientOptions(), sessionClient,
            new ArrayList<>(), Collections.singletonList(perRetryPolicy), null, null,
            new ClientLogger(BuilderHelperTests.class), new SessionOptions(), BlobServiceVersion.getLatest());

        StepVerifier.create(pipeline.send(new HttpRequest(HttpMethod.GET, ENDPOINT + "container/blob")))
            .assertNext(response -> assertEquals(200, response.getStatusCode()))
            .verifyComplete();

        assertTrue(observedMethods.contains(HttpMethod.POST),
            "Per-retry policies must run for CreateSession requests, saw " + observedMethods);
        assertTrue(observedMethods.contains(HttpMethod.GET),
            "Per-retry policies must run for data requests, saw " + observedMethods);
    }

    @Test
    public void customSessionProviderIsWiredIntoPipelineWithResolvedRequestContext() {
        AtomicReference<SessionRequestContext> capturedContext = new AtomicReference<>();
        SessionProvider provider = createCapturingSessionProvider(capturedContext);

        HttpPipeline pipeline = buildPipelineWithSessionProvider(provider);
        HttpRequest request = new HttpRequest(HttpMethod.GET, ENDPOINT + "container/blob");

        StepVerifier.create(pipeline.send(request)).expectNextCount(1).verifyComplete();

        assertNotNull(capturedContext.get(), "Custom session provider should have been invoked by the pipeline");
        assertEquals("container", capturedContext.get().getContainerName());
        assertEquals("account", capturedContext.get().getAccountName());
    }

    private SessionProvider createCapturingSessionProvider(AtomicReference<SessionRequestContext> capture) {
        SessionCredential credential = new SessionCredential("session-token",
            "dGVzdFNlc3Npb25LZXkxMjM0NTY3ODkwMTIzNDU2Nzg5MA==", OffsetDateTime.now().plusMinutes(5), "account");

        return new SessionProvider() {
            @Override
            public Mono<SessionCredential> getSessionAsync(SessionRequestContext context) {
                capture.set(context);
                return Mono.just(credential);
            }

            @Override
            public SessionCredential getSession(SessionRequestContext context) {
                capture.set(context);
                return credential;
            }

            @Override
            public boolean invalidateSession(SessionRequestContext context, SessionCredential rejectedCredential) {
                return false;
            }

            @Override
            public void refreshSession(SessionRequestContext context) {
            }
        };
    }

    private HttpPipeline buildPipelineWithSessionProvider(SessionProvider provider) {
        SessionOptions options = new SessionOptions().setSessionProvider(provider);
        HttpClient mockClient = request -> {
            MockHttpResponse response = new MockHttpResponse(request, 200);
            return Mono.just(response);
        };

        return BuilderHelper.buildPipeline(null, new MockTokenCredential(), null, null, ENDPOINT, REQUEST_RETRY_OPTIONS,
            null, BuilderHelper.getDefaultHttpLogOptions(), new ClientOptions(), mockClient, new ArrayList<>(),
            new ArrayList<>(), null, null, new ClientLogger(BuilderHelperTests.class), options, null);
    }

    private static Stream<Arguments> pipelinesWithoutSessionsSupplier() {
        return Stream.of(
            Arguments.of("null session options", (Supplier<HttpPipeline>) () -> buildTokenPipeline(null), true),
            Arguments.of("sessions disabled",
                (Supplier<HttpPipeline>) () -> buildTokenPipeline(
                    new SessionOptions().setSessionMode(SessionMode.DISABLED)),
                true),
            Arguments.of("shared key credential", (Supplier<HttpPipeline>) BuilderHelperTests::buildSharedKeyPipeline,
                false));
    }

    private static Stream<Arguments> standaloneSessionPipelineSupplier() {
        return Stream.of(
            Arguments.of("standalone BlobClientBuilder",
                (Supplier<HttpPipeline>) () -> new BlobClientBuilder().endpoint(ENDPOINT)
                    .containerName("mycontainer")
                    .blobName("myblob")
                    .credential(new MockTokenCredential())
                    .httpClient(new NoOpHttpClient())
                    .buildClient()
                    .getHttpPipeline()),
            Arguments.of("standalone BlobContainerClientBuilder",
                (Supplier<HttpPipeline>) () -> new BlobContainerClientBuilder().endpoint(ENDPOINT)
                    .containerName("mycontainer")
                    .credential(new MockTokenCredential())
                    .httpClient(new NoOpHttpClient())
                    .buildClient()
                    .getHttpPipeline()),
            Arguments.of("standalone SpecializedBlobClientBuilder",
                (Supplier<HttpPipeline>) () -> new SpecializedBlobClientBuilder().endpoint(ENDPOINT)
                    .containerName("mycontainer")
                    .blobName("myblob")
                    .credential(new MockTokenCredential())
                    .httpClient(new NoOpHttpClient())
                    .buildBlockBlobClient()
                    .getHttpPipeline()));
    }

    /**
     * Helper to build a pipeline with bearer token auth and the given session options.
     */
    private static HttpPipeline buildTokenPipeline(SessionOptions sessionOptions) {
        return BuilderHelper.buildPipeline(null, new MockTokenCredential(), null, null, ENDPOINT,
            new RequestRetryOptions(), null, BuilderHelper.getDefaultHttpLogOptions(), new ClientOptions(),
            new NoOpHttpClient(), new ArrayList<>(), new ArrayList<>(), null, null,
            new ClientLogger(BuilderHelperTests.class), sessionOptions, BlobServiceVersion.getLatest());
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

}
