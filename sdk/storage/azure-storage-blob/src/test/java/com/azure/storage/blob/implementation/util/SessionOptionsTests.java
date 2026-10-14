// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.util;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpPipelinePosition;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.AddHeadersPolicy;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.test.http.NoOpHttpClient;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.core.util.DateTimeRfc1123;
import com.azure.storage.blob.BlobServiceAsyncClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.BlobServiceVersion;
import com.azure.storage.blob.implementation.accesshelpers.SessionOptionsAccessor;
import com.azure.storage.blob.options.SessionOptions;
import com.azure.storage.common.policy.RequestRetryOptions;
import com.azure.storage.common.policy.RetryPolicyType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SessionOptionsTests {
    private static final String ENDPOINT = "https://account.blob.core.windows.net";
    private static final HttpHeaderName CONSUMER_HEADER = HttpHeaderName.fromString("x-test-consumer");

    @Test
    public void defaultOptionsUseAutomaticModeAndPipelineConfiguration() {
        SessionOptions options = new SessionOptions();

        assertEquals(SessionOptions.SessionMode.AUTO, options.getSessionMode());
        assertNull(SessionOptionsAccessor.getEndpoint(options));
        assertNull(SessionOptionsAccessor.getCredential(options));
    }

    @Test
    public void constructorStoresSessionAcquisitionConfiguration() {
        MockTokenCredential credential = new MockTokenCredential();
        SessionOptions options = new SessionOptions(ENDPOINT, credential);

        assertEquals(SessionOptions.SessionMode.AUTO, options.getSessionMode());
        assertEquals(ENDPOINT, SessionOptionsAccessor.getEndpoint(options));
        assertSame(credential, SessionOptionsAccessor.getCredential(options));
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    public void sharedOptionsReuseProviderAcrossIndependentClients(boolean customOptions) {
        SessionOptions options = (customOptions ? new SessionOptions(ENDPOINT, new MockTokenCredential()) : new SessionOptions())
            .setSessionMode(SessionOptions.SessionMode.ENABLED);
        BlobServiceClient first = serviceClientBuilder().sessionOptions(options).buildClient();
        SessionProvider provider = existingProvider(options);
        BlobServiceClient second = serviceClientBuilder().sessionOptions(options).buildClient();
        serviceClientBuilder().sessionOptions(options).buildAsyncClient();

        assertSame(first.getHttpPipeline(), first.getBlobContainerClient("container").getHttpPipeline());
        assertSame(first.getHttpPipeline(),
            first.getBlobContainerClient("container").getBlobClient("blob").getHttpPipeline());
        assertNotSame(first.getHttpPipeline(), second.getHttpPipeline());
        assertSame(provider, existingProvider(options));

        SessionOptions separate = (customOptions ? new SessionOptions(ENDPOINT, new MockTokenCredential()) : new SessionOptions())
            .setSessionMode(SessionOptions.SessionMode.ENABLED);
        serviceClientBuilder().sessionOptions(separate).buildClient();
        assertNotSame(provider, existingProvider(separate));
    }

    @Test
    public void concurrentBuildersInitializeOnlyOneProvider() throws Exception {
        SessionOptions options = new SessionOptions().setSessionMode(SessionOptions.SessionMode.ENABLED);
        ExecutorService executor = Executors.newFixedThreadPool(4);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<SessionProvider>> results = new ArrayList<>();
        try {
            for (int i = 0; i < 4; i++) {
                results.add(executor.submit(() -> {
                    assertTrue(start.await(10, TimeUnit.SECONDS));
                    serviceClientBuilder().sessionOptions(options).buildClient();
                    return existingProvider(options);
                }));
            }
            start.countDown();
            SessionProvider provider = results.get(0).get(30, TimeUnit.SECONDS);
            for (Future<SessionProvider> result : results) {
                assertSame(provider, result.get(30, TimeUnit.SECONDS));
            }
        } finally {
            start.countDown();
            executor.shutdownNow();
        }
    }

    private static SessionProvider existingProvider(SessionOptions options) {
        return SessionOptionsAccessor.getOrCreateSessionClient(options, SessionProvider.class, () -> {
            throw new AssertionError("The builder must initialize the provider.");
        });
    }

    @Test
    public void constructorRequiresEndpointAndCredential() {
        assertThrows(NullPointerException.class, () -> new SessionOptions(null, new MockTokenCredential()));
        assertThrows(NullPointerException.class, () -> new SessionOptions(ENDPOINT, null));
    }

    @Test
    public void publicProviderTypesAreRemoved() {
        assertThrows(ClassNotFoundException.class,
            () -> Class.forName("com.azure.storage.blob.models.SessionProvider"));
        assertThrows(ClassNotFoundException.class,
            () -> Class.forName("com.azure.storage.blob.models.ContainerSessionProvider"));
    }

    @ParameterizedTest
    @CsvSource({ "true,true,true", "true,true,false", "true,false,true", "true,false,false",
        "false,true,true", "false,true,false", "false,false,true", "false,false,false" })
    public void sharedProviderRetainsFirstBuildersTransportAndPolicies(boolean customOptions, boolean asyncFirst,
        boolean sameTransport) {
        ConcurrentHashMap<String, AtomicInteger> acquisitions = new ConcurrentHashMap<>();
        ConcurrentHashMap<String, AtomicInteger> downloads = new ConcurrentHashMap<>();
        AtomicInteger loggedAcquisitions = new AtomicInteger();
        TokenCredential acquisitionCredential
            = context -> Mono.just(new AccessToken("acquisition-token", OffsetDateTime.now().plusHours(1)));
        SessionOptions options = (customOptions ? new SessionOptions(ENDPOINT, acquisitionCredential) : new SessionOptions())
            .setSessionMode(SessionOptions.SessionMode.ENABLED);
        HttpClient firstTransport = consumerTransport(sameTransport ? null : "first", customOptions,
            acquisitions, downloads);
        HttpClient secondTransport
            = sameTransport ? firstTransport : consumerTransport("second", customOptions, acquisitions, downloads);
        HttpLogOptions logOptions = new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS)
            .setResponseLogger((logger, context) -> {
                if (context.getHttpResponse().getRequest().getHttpMethod() == HttpMethod.POST) {
                    loggedAcquisitions.incrementAndGet();
                }
                return Mono.just(context.getHttpResponse());
            });
        BlobServiceClientBuilder first = consumerBuilder(options, firstTransport, "first", logOptions);
        BlobServiceClientBuilder second = consumerBuilder(options, secondTransport, "second", logOptions);

        // Build the sync or async client first, then reverse the order on the other parameterized invocation.
        downloadFromDerivedClients(first, asyncFirst);
        downloadFromDerivedClients(second, !asyncFirst);

        assertEquals(1, acquisitions.get("first").get());
        assertNull(acquisitions.get("second"), "The second client must reuse the options-owned provider.");
        assertEquals(2, downloads.get("first").get(), "Derived clients should reuse the first session.");
        assertEquals(2, downloads.get("second").get(), "Independent clients should reuse the same session.");

        SessionProvider provider = existingProvider(options);
        SessionRequestContext context = new SessionRequestContext().setAccountName("account").setContainerName("container");
        assertTrue(provider.invalidateSession(context, provider.getSession(context)));
        downloadFromDerivedClients(second, !asyncFirst);
        assertEquals(2, acquisitions.get("first").get(), "Reacquisition must retain the original pipeline.");
        assertNull(acquisitions.get("second"));
        assertEquals(0, loggedAcquisitions.get(), "Custom HTTP loggers must not receive session credentials.");
    }

    @Test
    public void providerConfigurationIsSelectedAtBuildNotFirstRequest() {
        AtomicInteger acquisitions = new AtomicInteger();
        SessionOptions options = new SessionOptions();
        serviceClientBuilder().httpClient(request -> {
            throw new AssertionError("A client built with sessions disabled must not own the provider.");
        }).sessionOptions(options).buildClient();
        options.setSessionMode(SessionOptions.SessionMode.ENABLED);
        serviceClientBuilder().httpClient(request -> {
            acquisitions.incrementAndGet();
            assertEquals(HttpMethod.POST, request.getHttpMethod());
            return sessionResponse(request, "built-first");
        }).sessionOptions(options).buildClient();
        BlobServiceClientBuilder later = serviceClientBuilder().httpClient(request -> {
            assertEquals(HttpMethod.GET, request.getHttpMethod());
            assertTrue(request.getHeaders().getValue(HttpHeaderName.AUTHORIZATION).startsWith("Session built-first:"));
            return downloadResponse(request);
        }).sessionOptions(options);

        downloadFromDerivedClients(later, true);
        assertEquals(1, acquisitions.get());
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    public void customAcquisitionInheritsRetrySettings(boolean async) {
        AtomicInteger attempts = new AtomicInteger();
        AtomicInteger perCallInvocations = new AtomicInteger();
        AtomicInteger perRetryInvocations = new AtomicInteger();
        HttpClient transport = request -> {
            if (request.getHttpMethod() == HttpMethod.POST) {
                if (attempts.incrementAndGet() == 1) {
                    return Mono.just(new MockHttpResponse(request, 503));
                }
                return sessionResponse(request, "retried");
            }
            assertTrue(request.getHeaders().getValue(HttpHeaderName.AUTHORIZATION).startsWith("Session retried:"));
            return downloadResponse(request);
        };
        BlobServiceClientBuilder builder = serviceClientBuilder().httpClient(transport)
            .sessionOptions(new SessionOptions(ENDPOINT, new MockTokenCredential())
                .setSessionMode(SessionOptions.SessionMode.ENABLED))
            .retryOptions(new RequestRetryOptions(RetryPolicyType.FIXED, 2, 10, 1L, 1L, null))
            .addPolicy((context, next) -> {
                if (context.getHttpRequest().getHttpMethod() == HttpMethod.POST) {
                    perRetryInvocations.incrementAndGet();
                }
                return next.process();
            })
            .addPolicy(new HttpPipelinePolicy() {
                @Override
                public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
                    if (context.getHttpRequest().getHttpMethod() == HttpMethod.POST) {
                        perCallInvocations.incrementAndGet();
                    }
                    return next.process();
                }

                @Override
                public HttpPipelinePosition getPipelinePosition() {
                    return HttpPipelinePosition.PER_CALL;
                }
            });

        downloadFromDerivedClients(builder, async);

        assertEquals(2, attempts.get());
        assertEquals(1, perCallInvocations.get(), "The per-call policy must remain outside the retry policy.");
        assertEquals(2, perRetryInvocations.get(), "The per-retry policy must run on both acquisition attempts.");
    }

    @Test
    public void customAcquisitionRequiresHttps() {
        SessionOptions options = new SessionOptions("http://account.blob.core.windows.net", new MockTokenCredential())
            .setSessionMode(SessionOptions.SessionMode.ENABLED);
        assertThrows(IllegalArgumentException.class,
            () -> serviceClientBuilder().sessionOptions(options).buildClient());
    }

    private static BlobServiceClientBuilder consumerBuilder(SessionOptions options, HttpClient transport,
        String name, HttpLogOptions logOptions) {
        return serviceClientBuilder().httpClient(transport).sessionOptions(options)
            .serviceVersion(BlobServiceVersion.V2026_10_06)
            .httpLogOptions(logOptions)
            .addPolicy(new HttpLoggingPolicy(logOptions))
            .addPolicy(new AddHeadersPolicy(new HttpHeaders().set(CONSUMER_HEADER, name)));
    }

    private static HttpClient consumerTransport(String transportName, boolean customOptions,
        ConcurrentHashMap<String, AtomicInteger> acquisitions, ConcurrentHashMap<String, AtomicInteger> downloads) {
        return request -> {
            String name = request.getHeaders().getValue(CONSUMER_HEADER);
            assertTrue("first".equals(name) || "second".equals(name));
            if (transportName != null) {
                assertEquals(transportName, name, "CreateSession must use the consuming transport.");
            }
            assertEquals("account.blob.core.windows.net", request.getUrl().getHost());
            assertEquals(BlobServiceVersion.V2026_10_06.getVersion(),
                request.getHeaders().getValue(HttpHeaderName.fromString("x-ms-version")));
            if (request.getHttpMethod() == HttpMethod.POST) {
                assertEquals("/container", request.getUrl().getPath());
                assertEquals("restype=container&comp=session", request.getUrl().getQuery());
                assertEquals("Bearer " + (customOptions ? "acquisition-token" : "consumer-token"),
                    request.getHeaders().getValue(HttpHeaderName.AUTHORIZATION));
                acquisitions.computeIfAbsent(name, ignored -> new AtomicInteger()).incrementAndGet();
                return sessionResponse(request, name);
            }
            assertEquals(HttpMethod.GET, request.getHttpMethod());
            assertTrue(request.getHeaders().getValue(HttpHeaderName.AUTHORIZATION).startsWith("Session first:"));
            downloads.computeIfAbsent(name, ignored -> new AtomicInteger()).incrementAndGet();
            return downloadResponse(request);
        };
    }

    private static Mono<HttpResponse> sessionResponse(HttpRequest request, String token) {
        String body = "<CreateSessionResult><Id>test-session</Id><Expiration>"
            + new DateTimeRfc1123(OffsetDateTime.now().plusHours(1))
            + "</Expiration><AuthenticationType>HMAC</AuthenticationType><Credentials>"
            + "<SessionToken>" + token + "</SessionToken>"
            + "<SessionKey>dGVzdFNlc3Npb25LZXkxMjM0NTY3ODkwMTIzNDU2Nzg5MA==</SessionKey>"
            + "</Credentials></CreateSessionResult>";
        return Mono.just(new MockHttpResponse(request, 201, body.getBytes(StandardCharsets.UTF_8))
            .addHeader(HttpHeaderName.CONTENT_TYPE.toString(), "application/xml"));
    }

    private static Mono<HttpResponse> downloadResponse(HttpRequest request) {
        return Mono.just(new MockHttpResponse(request, 200, "data".getBytes(StandardCharsets.UTF_8))
            .addHeader(HttpHeaderName.CONTENT_LENGTH.toString(), "4")
            .addHeader(HttpHeaderName.CONTENT_TYPE.toString(), "application/octet-stream")
            .addHeader(HttpHeaderName.ETAG.toString(), "\"etag\"")
            .addHeader(HttpHeaderName.LAST_MODIFIED.toString(), new DateTimeRfc1123(OffsetDateTime.now()).toString()));
    }

    private static BlobServiceClientBuilder serviceClientBuilder() {
        TokenCredential credential
            = context -> Mono.just(new AccessToken("consumer-token", OffsetDateTime.now().plusHours(1)));
        return new BlobServiceClientBuilder().endpoint(ENDPOINT).credential(credential).httpClient(new NoOpHttpClient());
    }

    private static void downloadFromDerivedClients(BlobServiceClientBuilder builder, boolean async) {
        if (async) {
            BlobServiceAsyncClient service = builder.buildAsyncClient();
            for (int i = 0; i < 2; i++) {
                StepVerifier.create(service.getBlobContainerAsyncClient("container").getBlobAsyncClient("blob" + i)
                    .downloadContent()).assertNext(content -> assertEquals("data", content.toString())).verifyComplete();
            }
        } else {
            BlobServiceClient service = builder.buildClient();
            for (int i = 0; i < 2; i++) {
                assertEquals("data",
                    service.getBlobContainerClient("container").getBlobClient("blob" + i).downloadContent().toString());
            }
        }
    }
}
