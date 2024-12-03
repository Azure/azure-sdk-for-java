// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share;

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
import com.azure.core.http.policy.RetryOptions;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.DateTimeRfc1123;
import com.azure.core.util.Header;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.policy.RequestRetryOptions;
import com.azure.storage.common.policy.RetryPolicyType;
import com.azure.storage.file.share.implementation.util.BuilderHelper;
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
import static org.junit.jupiter.api.Assertions.assertThrows;

public class BuilderHelperTests {

    private static final StorageSharedKeyCredential CREDENTIALS
        = new StorageSharedKeyCredential("accountName", "accountKey");
    private static final String ENDPOINT = "https://account.file.core.windows.net/";
    private static final RequestRetryOptions REQUEST_RETRY_OPTIONS
        = new RequestRetryOptions(RetryPolicyType.FIXED, 2, 2, 1000L, 4000L, null);
    private static final RetryOptions CORE_RETRY_OPTIONS
        = new RetryOptions(new FixedDelayOptions(1, Duration.ofSeconds(2)));

    private static HttpRequest request(String url) throws MalformedURLException {
        return new HttpRequest(HttpMethod.HEAD, new URL(url), new HttpHeaders().set(HttpHeaderName.CONTENT_LENGTH, "0"),
            Flux.empty());
    }

    /**
     * Tests that a new date will be applied to every retry when using the default pipeline builder.
     */
    @Test
    public void freshDateAppliedOnRetry() throws MalformedURLException {
        HttpClient httpClient = new FreshDateTestClient();
        HttpLogOptions httpLogOptions = BuilderHelper.getDefaultHttpLogOptions();
        ClientOptions clientOptions = new ClientOptions();

        StepVerifier
            .create(BuilderHelper
                .buildPipeline(CREDENTIALS, null, null, null, null, REQUEST_RETRY_OPTIONS, null, httpLogOptions,
                    clientOptions, httpClient, new ArrayList<>(), new ArrayList<>(), Configuration.NONE, null,
                    new ClientLogger("foo"))
                .send(request(ENDPOINT)))
            .assertNext(response -> assertEquals(200, response.getStatusCode()))
            .verifyComplete();
    }

    /**
     * Tests that a new date will be applied to every retry when using the service client builder's default pipeline.
     */
    @Test
    public void serviceClientFreshDateOnRetry() throws MalformedURLException {
        ShareServiceClient serviceClient = new ShareServiceClientBuilder().endpoint(ENDPOINT)
            .credential(CREDENTIALS)
            .httpClient(new FreshDateTestClient())
            .retryOptions(REQUEST_RETRY_OPTIONS)
            .buildClient();

        serviceClient.getHttpPipeline()
            .send(request(serviceClient.getFileServiceUrl()))
            .subscribe(response -> assertEquals(200, response.getStatusCode()));
    }

    /**
     * Tests that a new date will be applied to every retry when using the share client builder's default pipeline.
     */
    @Test
    public void shareClientFreshDateOnRetry() throws MalformedURLException {
        ShareClient shareClient = new ShareClientBuilder().endpoint(ENDPOINT)
            .shareName("share")
            .credential(CREDENTIALS)
            .httpClient(new FreshDateTestClient())
            .retryOptions(REQUEST_RETRY_OPTIONS)
            .buildClient();

        shareClient.getHttpPipeline()
            .send(request(shareClient.getShareUrl()))
            .subscribe(response -> assertEquals(200, response.getStatusCode()));
    }

    /**
     * Tests that a new date will be applied to every retry when using the file client builder's default pipeline.
     */
    @Test
    void fileClientFreshDateOnRetry() throws MalformedURLException {
        ShareFileClientBuilder fileClientBuilder = new ShareFileClientBuilder().endpoint(ENDPOINT)
            .shareName("fileSystem")
            .resourcePath("path")
            .credential(CREDENTIALS)
            .retryOptions(REQUEST_RETRY_OPTIONS)
            .httpClient(new FreshDateTestClient());

        ShareDirectoryClient directoryClient = fileClientBuilder.buildDirectoryClient();
        directoryClient.getHttpPipeline()
            .send(request(directoryClient.getDirectoryUrl()))
            .subscribe(response -> assertEquals(200, response.getStatusCode()));

        ShareFileClient fileClient = fileClientBuilder.buildFileClient();
        fileClient.getHttpPipeline()
            .send(request(fileClient.getFileUrl()))
            .subscribe(response -> assertEquals(200, response.getStatusCode()));
    }

    /**
     * Tests that a user application id will be honored in the UA string when using the default pipeline builder.
     */
    @ParameterizedTest
    @MethodSource("customApplicationIdInUAStringSupplier")
    public void customApplicationIdInUAString(String logOptionsUA, String clientOptionsUA, String expectedUA)
        throws MalformedURLException {
        HttpClient httpClient = new ApplicationIdUAStringTestClient(expectedUA);
        HttpLogOptions httpLogOptions = new HttpLogOptions().setApplicationId(logOptionsUA);
        ClientOptions clientOptions = new ClientOptions().setApplicationId(clientOptionsUA);

        StepVerifier
            .create(BuilderHelper
                .buildPipeline(CREDENTIALS, null, null, null, null, new RequestRetryOptions(), null, httpLogOptions,
                    clientOptions, httpClient, new ArrayList<>(), new ArrayList<>(), Configuration.NONE, null,
                    new ClientLogger("foo"))
                .send(request(ENDPOINT)))
            .assertNext(response -> assertEquals(200, response.getStatusCode()))
            .verifyComplete();
    }

    /**
     * Tests that a user application id will be honored in the UA string when using the service client builder's default
     * pipeline.
     */
    @ParameterizedTest
    @MethodSource("customApplicationIdInUAStringSupplier")
    void serviceClientCustomApplicationIdInUAString(String logOptionsUA, String clientOptionsUA, String expectedUA)
        throws MalformedURLException {
        ShareServiceClient serviceClient = new ShareServiceClientBuilder().endpoint(ENDPOINT)
            .credential(CREDENTIALS)
            .httpLogOptions(new HttpLogOptions().setApplicationId(logOptionsUA))
            .clientOptions(new ClientOptions().setApplicationId(clientOptionsUA))
            .httpClient(new ApplicationIdUAStringTestClient(expectedUA))
            .buildClient();

        StepVerifier.create(serviceClient.getHttpPipeline().send(request(serviceClient.getFileServiceUrl())))
            .assertNext(response -> assertEquals(200, response.getStatusCode()))
            .verifyComplete();
    }

    /**
     * Tests that a user application id will be honored in the UA string when using the share client builder's default
     * pipeline.
     */
    @ParameterizedTest
    @MethodSource("customApplicationIdInUAStringSupplier")
    void shareClientCustomApplicationIdInUAString(String logOptionsUA, String clientOptionsUA, String expectedUA)
        throws MalformedURLException {
        ShareClient shareClient = new ShareClientBuilder().endpoint(ENDPOINT)
            .shareName("share")
            .credential(CREDENTIALS)
            .httpLogOptions(new HttpLogOptions().setApplicationId(logOptionsUA))
            .clientOptions(new ClientOptions().setApplicationId(clientOptionsUA))
            .httpClient(new ApplicationIdUAStringTestClient(expectedUA))
            .buildClient();

        StepVerifier.create(shareClient.getHttpPipeline().send(request(shareClient.getShareUrl())))
            .assertNext(response -> assertEquals(200, response.getStatusCode()))
            .verifyComplete();
    }

    /**
     * Tests that a user application id will be honored in the UA string when using the file client builder's default
     * pipeline.
     */
    @ParameterizedTest
    @MethodSource("customApplicationIdInUAStringSupplier")
    void fileClientCustomApplicationIdInUAString(String logOptionsUA, String clientOptionsUA, String expectedUA)
        throws MalformedURLException {
        ShareFileClientBuilder fileClientBuilder = new ShareFileClientBuilder().endpoint(ENDPOINT)
            .shareName("fileSystem")
            .resourcePath("path")
            .credential(CREDENTIALS)
            .httpLogOptions(new HttpLogOptions().setApplicationId(logOptionsUA))
            .clientOptions(new ClientOptions().setApplicationId(clientOptionsUA))
            .httpClient(new ApplicationIdUAStringTestClient(expectedUA));

        ShareDirectoryClient directoryClient = fileClientBuilder.buildDirectoryClient();
        StepVerifier.create(directoryClient.getHttpPipeline().send(request(directoryClient.getDirectoryUrl())))
            .assertNext(response -> assertEquals(200, response.getStatusCode()))
            .verifyComplete();

        ShareFileClient fileClient = fileClientBuilder.buildFileClient();
        StepVerifier.create(fileClient.getHttpPipeline().send(request(fileClient.getFileUrl())))
            .assertNext(response -> assertEquals(200, response.getStatusCode()))
            .verifyComplete();
    }

    private static Stream<Arguments> customApplicationIdInUAStringSupplier() {
        return Stream.of(Arguments.of("log-options-id", null, "log-options-id"),
            Arguments.of(null, "client-options-id", "client-options-id"),
            Arguments.of("log-options-id", "client-options-id", "client-options-id"));
    }

    /**
     * Tests that a custom headers will be honored when using the default pipeline builder.
     */
    @Test
    void customHeadersClientOptions() throws MalformedURLException {
        List<Header> headers = new ArrayList<>();
        headers.add(new Header("custom", "header"));
        headers.add(new Header("Authorization", "notthis"));
        headers.add(new Header("User-Agent", "overwritten"));

        HttpPipeline pipeline = BuilderHelper.buildPipeline(CREDENTIALS, null, null, null, null,
            new RequestRetryOptions(), null, BuilderHelper.getDefaultHttpLogOptions(),
            new ClientOptions().setHeaders(headers), new ClientOptionsHeadersTestClient(headers), new ArrayList<>(),
            new ArrayList<>(), Configuration.NONE, null, new ClientLogger("foo"));

        StepVerifier.create(pipeline.send(request(ENDPOINT)))
            .assertNext(response -> assertEquals(200, response.getStatusCode()))
            .verifyComplete();
    }

    /**
     * Tests that custom headers will be honored when using the service client builder's default pipeline.
     */
    @Test
    void serviceClientCustomHeadersClientOptions() throws MalformedURLException {
        List<Header> headers = new ArrayList<>();
        headers.add(new Header("custom", "header"));
        headers.add(new Header("Authorization", "notthis"));
        headers.add(new Header("User-Agent", "overwritten"));

        ShareServiceClient serviceClient = new ShareServiceClientBuilder().endpoint(ENDPOINT)
            .credential(CREDENTIALS)
            .clientOptions(new ClientOptions().setHeaders(headers))
            .httpClient(new ClientOptionsHeadersTestClient(headers))
            .buildClient();

        StepVerifier.create(serviceClient.getHttpPipeline().send(request(serviceClient.getFileServiceUrl())))
            .assertNext(response -> assertEquals(200, response.getStatusCode()))
            .verifyComplete();
    }

    /**
     * Tests that custom headers will be honored when using the share client builder's default pipeline.
     */
    @Test
    void shareClientCustomHeadersClientOptions() throws MalformedURLException {
        List<Header> headers = new ArrayList<>();
        headers.add(new Header("custom", "header"));
        headers.add(new Header("Authorization", "notthis"));
        headers.add(new Header("User-Agent", "overwritten"));

        ShareClient shareClient = new ShareClientBuilder().endpoint(ENDPOINT)
            .shareName("share")
            .credential(CREDENTIALS)
            .clientOptions(new ClientOptions().setHeaders(headers))
            .httpClient(new ClientOptionsHeadersTestClient(headers))
            .buildClient();

        StepVerifier.create(shareClient.getHttpPipeline().send(request(shareClient.getShareUrl())))
            .assertNext(response -> assertEquals(200, response.getStatusCode()))
            .verifyComplete();
    }

    /**
     * Tests that custom headers will be honored when using the blob client builder's default pipeline.
     */
    @Test
    void blobClientCustomHeadersClientOptions() throws MalformedURLException {
        List<Header> headers = new ArrayList<>();
        headers.add(new Header("custom", "header"));
        headers.add(new Header("Authorization", "notthis"));
        headers.add(new Header("User-Agent", "overwritten"));

        ShareFileClientBuilder fileClientBuilder = new ShareFileClientBuilder().endpoint(ENDPOINT)
            .shareName("share")
            .resourcePath("blob")
            .credential(CREDENTIALS)
            .clientOptions(new ClientOptions().setHeaders(headers))
            .httpClient(new ClientOptionsHeadersTestClient(headers));

        ShareDirectoryClient directoryClient = fileClientBuilder.buildDirectoryClient();

        StepVerifier.create(directoryClient.getHttpPipeline().send(request(directoryClient.getDirectoryUrl())))
            .assertNext(response -> assertEquals(200, response.getStatusCode()))
            .verifyComplete();

        ShareFileClient fileClient = fileClientBuilder.buildFileClient();

        StepVerifier.create(fileClient.getHttpPipeline().send(request(fileClient.getFileUrl())))
            .assertNext(response -> assertEquals(200, response.getStatusCode()))
            .verifyComplete();
    }

    @Test
    void doesNotThrowOnAmbiguousCREDENTIALSWithoutAzureSasCredential() {
        assertDoesNotThrow(() -> new ShareClientBuilder().endpoint(ENDPOINT)
            .shareName("foo")
            .credential(new StorageSharedKeyCredential("foo", "bar"))
            .sasToken("foo")
            .buildClient());

        assertDoesNotThrow(() -> new ShareFileClientBuilder().endpoint(ENDPOINT)
            .shareName("foo")
            .resourcePath("foo")
            .credential(new StorageSharedKeyCredential("foo", "bar"))
            .sasToken("foo")
            .buildDirectoryClient());

        assertDoesNotThrow(() -> new ShareServiceClientBuilder().endpoint(ENDPOINT)
            .credential(new StorageSharedKeyCredential("foo", "bar"))
            .sasToken("foo")
            .buildClient());
    }

    @Test
    void throwsOnAmbiguousCREDENTIALSWithAzureSasCredential() {
        assertThrows(IllegalStateException.class,
            () -> new ShareClientBuilder().endpoint(ENDPOINT)
                .shareName("foo")
                .credential(new StorageSharedKeyCredential("foo", "bar"))
                .credential(new AzureSasCredential("foo"))
                .buildClient());

        assertThrows(IllegalStateException.class,
            () -> new ShareClientBuilder().endpoint(ENDPOINT)
                .shareName("foo")
                .sasToken("foo")
                .credential(new AzureSasCredential("foo"))
                .buildClient());

        assertThrows(IllegalStateException.class,
            () -> new ShareClientBuilder().endpoint(ENDPOINT + "?sig=foo")
                .shareName("foo")
                .credential(new AzureSasCredential("foo"))
                .buildClient());

        assertThrows(IllegalStateException.class,
            () -> new ShareFileClientBuilder().endpoint(ENDPOINT)
                .shareName("foo")
                .resourcePath("foo")
                .credential(new StorageSharedKeyCredential("foo", "bar"))
                .credential(new AzureSasCredential("foo"))
                .buildDirectoryClient());

        assertThrows(IllegalStateException.class,
            () -> new ShareFileClientBuilder().endpoint(ENDPOINT)
                .shareName("foo")
                .resourcePath("foo")
                .sasToken("foo")
                .credential(new AzureSasCredential("foo"))
                .buildDirectoryClient());

        assertThrows(IllegalStateException.class,
            () -> new ShareFileClientBuilder().endpoint(ENDPOINT + "?sig=foo")
                .shareName("foo")
                .resourcePath("foo")
                .credential(new AzureSasCredential("foo"))
                .buildDirectoryClient());

        assertThrows(IllegalStateException.class,
            () -> new ShareServiceClientBuilder().endpoint(ENDPOINT)
                .credential(new StorageSharedKeyCredential("foo", "bar"))
                .credential(new AzureSasCredential("foo"))
                .buildClient());

        assertThrows(IllegalStateException.class,
            () -> new ShareServiceClientBuilder().endpoint(ENDPOINT)
                .sasToken("foo")
                .credential(new AzureSasCredential("foo"))
                .buildClient());

        assertThrows(IllegalStateException.class,
            () -> new ShareServiceClientBuilder().endpoint(ENDPOINT + "?sig=foo")
                .credential(new AzureSasCredential("foo"))
                .buildClient());
    }

    @Test
    void onlyOneRetryOptionsCanBeApplied() {
        assertThrows(IllegalStateException.class,
            () -> new ShareServiceClientBuilder().endpoint(ENDPOINT)
                .credential(CREDENTIALS)
                .retryOptions(REQUEST_RETRY_OPTIONS)
                .retryOptions(CORE_RETRY_OPTIONS)
                .buildClient());

        assertThrows(IllegalStateException.class,
            () -> new ShareClientBuilder().endpoint(ENDPOINT)
                .credential(CREDENTIALS)
                .shareName("foo")
                .retryOptions(REQUEST_RETRY_OPTIONS)
                .retryOptions(CORE_RETRY_OPTIONS)
                .buildClient());

        assertThrows(IllegalStateException.class,
            () -> new ShareFileClientBuilder().endpoint(ENDPOINT)
                .credential(CREDENTIALS)
                .shareName("foo")
                .resourcePath("foo")
                .retryOptions(REQUEST_RETRY_OPTIONS)
                .retryOptions(CORE_RETRY_OPTIONS)
                .buildFileClient());

        assertThrows(IllegalStateException.class,
            () -> new ShareFileClientBuilder().endpoint(ENDPOINT)
                .credential(CREDENTIALS)
                .shareName("foo")
                .resourcePath("foo")
                .retryOptions(REQUEST_RETRY_OPTIONS)
                .retryOptions(CORE_RETRY_OPTIONS)
                .buildDirectoryClient());
    }

    private static final class FreshDateTestClient implements HttpClient {
        private DateTimeRfc1123 firstDate;

        @Override
        public Mono<HttpResponse> send(HttpRequest request) {
            if (firstDate == null) {
                firstDate = convertToDateObject(request.getHeaders().getValue(HttpHeaderName.DATE));
                return Mono.error(new IOException("IOException!"));
            }

            assert !firstDate.equals(convertToDateObject(request.getHeaders().getValue(HttpHeaderName.DATE)));
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
            assert request.getHeaders().getValue(HttpHeaderName.USER_AGENT).startsWith(expectedUA);
            return Mono.just(new MockHttpResponse(request, 200));
        }
    }

    private static final class ClientOptionsHeadersTestClient implements HttpClient {

        private final Iterable<Header> headers;

        ClientOptionsHeadersTestClient(Iterable<Header> headers) {
            this.headers = headers;
        }

        @Override
        public Mono<HttpResponse> send(HttpRequest request) {
            headers.forEach(header -> {
                if (CoreUtils.isNullOrEmpty(header.getName())) {
                    throw new RuntimeException("Failed to set custom header name.");
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
}
