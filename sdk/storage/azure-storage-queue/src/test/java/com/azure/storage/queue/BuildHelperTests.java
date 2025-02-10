// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.queue;

import com.azure.core.credential.AzureSasCredential;
import com.azure.core.credential.TokenCredential;
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
import com.azure.core.util.Header;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.policy.RequestRetryOptions;
import com.azure.storage.common.policy.RetryPolicyType;
import com.azure.storage.queue.implementation.util.BuilderHelper;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BuildHelperTests {
    private static final ClientLogger LOGGER = new ClientLogger(BuildHelperTests.class);
    private static final StorageSharedKeyCredential CREDENTIALS
        = new StorageSharedKeyCredential("accountName", "accountKey");
    private static final String ENDPOINT = "https://account.queue.core.windows.net/";
    private static final RequestRetryOptions REQUEST_RETRY_OPTIONS
        = new RequestRetryOptions(RetryPolicyType.FIXED, 2, 2, 1000L, 4000L, null);
    private static final RetryOptions RETRY_OPTIONS = new RetryOptions(new FixedDelayOptions(1, Duration.ofSeconds(1)));
    private static final TokenCredential MOCK_CREDENTIAL = request -> null;
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

    private static void sendRequestAndValidate(HttpPipeline pipeline, String url) {
        try {
            StepVerifier
                .create(pipeline.send(new HttpRequest(HttpMethod.HEAD, new URL(url),
                    new HttpHeaders().set(HttpHeaderName.CONTENT_LENGTH, "0"), Flux.empty())))
                .assertNext(response -> assertEquals(200, response.getStatusCode()))
                .verifyComplete();
        } catch (MalformedURLException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static HttpPipeline buildPipeline(HttpLogOptions logOptions, ClientOptions clientOptions,
        HttpClient httpClient) {
        return BuilderHelper.buildPipeline(CREDENTIALS, null, null, null, ENDPOINT, REQUEST_RETRY_OPTIONS, null,
            logOptions, clientOptions, httpClient, new ArrayList<>(), new ArrayList<>(), Configuration.NONE, null,
            LOGGER);
    }

    /**
     * Tests that a new date will be applied to every retry when using the default pipeline builder.
     */
    @Test
    public void freshDateAppliedOnRetry() {
        HttpPipeline pipeline
            = buildPipeline(BuilderHelper.getDefaultHttpLogOptions(), new ClientOptions(), new FreshDateTestClient());

        sendRequestAndValidate(pipeline, ENDPOINT);
    }

    /**
     * Tests that a new date will be applied to every retry when using the service client builder's default pipeline.
     */
    @Test
    public void serviceClientFreshDateOnRetry() {
        QueueServiceClient serviceClient = new QueueServiceClientBuilder().endpoint(ENDPOINT)
            .credential(CREDENTIALS)
            .httpClient(new FreshDateTestClient())
            .retryOptions(REQUEST_RETRY_OPTIONS)
            .buildClient();

        sendRequestAndValidate(serviceClient.getHttpPipeline(), serviceClient.getQueueServiceUrl());
    }

    /**
     * Tests that a new date will be applied to every retry when using the queue client builder's default pipeline.
     */
    @Test
    public void queueClientFreshDateOnRetry() {
        QueueClient queueClient = new QueueClientBuilder().endpoint(ENDPOINT)
            .queueName("queue")
            .credential(CREDENTIALS)
            .httpClient(new FreshDateTestClient())
            .retryOptions(REQUEST_RETRY_OPTIONS)
            .buildClient();

        sendRequestAndValidate(queueClient.getHttpPipeline(), queueClient.getQueueUrl());
    }

    /**
     * Tests that a user application id will be honored in the UA string when using the default pipeline builder.
     */
    @ParameterizedTest
    @MethodSource("customApplicationIdSupplier")
    public void customApplicationIdInUserAgentString(String logOptionsUA, String clientOptionsUA, String expectedUA) {
        HttpPipeline pipeline = buildPipeline(new HttpLogOptions().setApplicationId(logOptionsUA),
            new ClientOptions().setApplicationId(clientOptionsUA), new ApplicationIdUAStringTestClient(expectedUA));

        sendRequestAndValidate(pipeline, ENDPOINT);
    }

    /**
     * Tests that a user application id will be honored in the UA string when using the service client builder's default pipeline.
     */
    @ParameterizedTest
    @MethodSource("customApplicationIdSupplier")
    public void serviceClientCustomApplicationIdInUserAgentString(String logOptionsUA, String clientOptionsUA,
        String expectedUA) {
        QueueServiceClient serviceClient = new QueueServiceClientBuilder().endpoint(ENDPOINT)
            .credential(CREDENTIALS)
            .httpLogOptions(new HttpLogOptions().setApplicationId(logOptionsUA))
            .clientOptions(new ClientOptions().setApplicationId(clientOptionsUA))
            .httpClient(new ApplicationIdUAStringTestClient(expectedUA))
            .buildClient();

        sendRequestAndValidate(serviceClient.getHttpPipeline(), serviceClient.getQueueServiceUrl());
    }

    /**
     * Tests that a user application id will be honored in the UA string when using the queue client builder's default pipeline.
     */
    @ParameterizedTest
    @MethodSource("customApplicationIdSupplier")
    public void queueClientCustomApplicationIdInUserAgentString(String logOptionsUA, String clientOptionsUA,
        String expectedUA) {
        QueueClient queueClient = new QueueClientBuilder().endpoint(ENDPOINT)
            .queueName("queue")
            .credential(CREDENTIALS)
            .httpLogOptions(new HttpLogOptions().setApplicationId(logOptionsUA))
            .clientOptions(new ClientOptions().setApplicationId(clientOptionsUA))
            .httpClient(new ApplicationIdUAStringTestClient(expectedUA))
            .buildClient();

        sendRequestAndValidate(queueClient.getHttpPipeline(), queueClient.getQueueUrl());
    }

    private static Stream<Arguments> customApplicationIdSupplier() {
        return Stream.of(Arguments.of("log-options-id", null, "log-options-id"),
            Arguments.of(null, "client-options-id", "client-options-id"),

            // Client options preferred over log options
            Arguments.of("log-options-id", "client-options-id", "client-options-id"));
    }

    /**
     * Tests that a custom headers will be honored when using the default pipeline builder.
     */
    @Test
    public void customHeadersClientOptions() {
        HttpPipeline pipeline = buildPipeline(BuilderHelper.getDefaultHttpLogOptions(),
            new ClientOptions().setHeaders(CLIENT_OPTIONS_HEADERS), new ClientOptionsHeadersTestClient(HEADERS_MAP));

        sendRequestAndValidate(pipeline, ENDPOINT);
    }

    /**
     * Tests that a user application id will be honored in the UA string when using the service client builder's default pipeline.
     */
    @Test
    public void serviceClientCustomHeadersClientOptions() {
        QueueServiceClient serviceClient = new QueueServiceClientBuilder().endpoint(ENDPOINT)
            .credential(CREDENTIALS)
            .clientOptions(new ClientOptions().setHeaders(CLIENT_OPTIONS_HEADERS))
            .httpClient(new ClientOptionsHeadersTestClient(HEADERS_MAP))
            .buildClient();

        sendRequestAndValidate(serviceClient.getHttpPipeline(), serviceClient.getQueueServiceUrl());
    }

    /**
     * Tests that a user application id will be honored in the UA string when using the queue client builder's default pipeline.
     */
    @Test
    public void queueClientCustomHeadersClientOptions() {
        QueueClient queueClient = new QueueClientBuilder().endpoint(ENDPOINT)
            .queueName("queue")
            .credential(CREDENTIALS)
            .clientOptions(new ClientOptions().setHeaders(CLIENT_OPTIONS_HEADERS))
            .httpClient(new ClientOptionsHeadersTestClient(HEADERS_MAP))
            .buildClient();

        sendRequestAndValidate(queueClient.getHttpPipeline(), queueClient.getQueueUrl());
    }

    @Test
    public void doesNotThrowOnAmbiguousCredentialsWithoutAzureSasCredential() {
        assertDoesNotThrow(() -> new QueueClientBuilder().endpoint(ENDPOINT)
            .queueName("foo")
            .credential(new StorageSharedKeyCredential("foo", "bar"))
            .credential(MOCK_CREDENTIAL)
            .sasToken("foo")
            .buildClient());

        assertDoesNotThrow(() -> new QueueServiceClientBuilder().endpoint(ENDPOINT)
            .credential(new StorageSharedKeyCredential("foo", "bar"))
            .credential(MOCK_CREDENTIAL)
            .sasToken("foo")
            .buildClient());
    }

    @Test
    public void throwsOnAmbiguousCredentialsWithAzureSasCredential() {
        assertThrows(IllegalStateException.class,
            () -> new QueueClientBuilder().endpoint(ENDPOINT)
                .queueName("foo")
                .credential(new StorageSharedKeyCredential("foo", "bar"))
                .credential(new AzureSasCredential("foo"))
                .buildClient());

        assertThrows(IllegalStateException.class,
            () -> new QueueClientBuilder().endpoint(ENDPOINT)
                .queueName("foo")
                .credential(MOCK_CREDENTIAL)
                .credential(new AzureSasCredential("foo"))
                .buildClient());

        assertThrows(IllegalStateException.class,
            () -> new QueueClientBuilder().endpoint(ENDPOINT)
                .queueName("foo")
                .sasToken("foo")
                .credential(new AzureSasCredential("foo"))
                .buildClient());

        assertThrows(IllegalStateException.class,
            () -> new QueueClientBuilder().endpoint(ENDPOINT + "?sig=foo")
                .queueName("foo")
                .credential(new AzureSasCredential("foo"))
                .buildClient());

        assertThrows(IllegalStateException.class,
            () -> new QueueServiceClientBuilder().endpoint(ENDPOINT)
                .credential(new StorageSharedKeyCredential("foo", "bar"))
                .credential(new AzureSasCredential("foo"))
                .buildClient());

        assertThrows(IllegalStateException.class,
            () -> new QueueServiceClientBuilder().endpoint(ENDPOINT)
                .credential(MOCK_CREDENTIAL)
                .credential(new AzureSasCredential("foo"))
                .buildClient());

        assertThrows(IllegalStateException.class,
            () -> new QueueServiceClientBuilder().endpoint(ENDPOINT)
                .sasToken("foo")
                .credential(new AzureSasCredential("foo"))
                .buildClient());

        assertThrows(IllegalStateException.class,
            () -> new QueueServiceClientBuilder().endpoint(ENDPOINT + "?sig=foo")
                .credential(new AzureSasCredential("foo"))
                .buildClient());
    }

    @Test
    public void onlyOneRetryOptionsCanBeApplied() {
        assertThrows(IllegalStateException.class,
            () -> new QueueServiceClientBuilder().endpoint(ENDPOINT)
                .credential(CREDENTIALS)
                .retryOptions(REQUEST_RETRY_OPTIONS)
                .retryOptions(RETRY_OPTIONS)
                .buildClient());

        assertThrows(IllegalStateException.class,
            () -> new QueueClientBuilder().endpoint(ENDPOINT)
                .credential(CREDENTIALS)
                .queueName("foo")
                .retryOptions(REQUEST_RETRY_OPTIONS)
                .retryOptions(RETRY_OPTIONS)
                .buildClient());
    }

    @Test
    public void parseProtocol() {
        BuilderHelper.QueueUrlParts parts = BuilderHelper.parseEndpoint(ENDPOINT + "?sv=2019-12-12&ss=bfqt&srt=s"
            + "&sp=rwdlacupx&se=2020-08-15T05:43:05Z&st=2020-08-14T21:43:05Z&spr=https,http&sig=sig", null);

        assertTrue(parts.getSasToken().contains("https%2Chttp"), "Expected SAS token to contain 'https%2Chttp'.");
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
