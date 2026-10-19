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
import com.azure.storage.blob.implementation.accesshelpers.SessionContextAccessor;
import com.azure.storage.blob.SessionContext;
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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SessionContextTests {
    private static final String ENDPOINT = "https://account.blob.core.windows.net";
    private static final HttpHeaderName CONSUMER_HEADER = HttpHeaderName.fromString("x-test-consumer");

    @Test
    public void defaultOptionsUseAutomaticModeAndPipelineConfiguration() {
        SessionContext options = new SessionContext();

        assertEquals(SessionContext.SessionMode.AUTO, options.getSessionMode());
        assertNull(SessionContextAccessor.getEndpoint(options));
        assertNull(SessionContextAccessor.getCredential(options));
    }

    @Test
    public void constructorStoresSessionAcquisitionConfiguration() {
        MockTokenCredential credential = new MockTokenCredential();
        SessionContext options = new SessionContext(ENDPOINT, credential);

        assertEquals(SessionContext.SessionMode.AUTO, options.getSessionMode());
        assertEquals(ENDPOINT, SessionContextAccessor.getEndpoint(options));
        assertSame(credential, SessionContextAccessor.getCredential(options));
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    public void sharedOptionsReuseProviderAcrossIndependentClients(boolean customOptions) {
        SessionContext options
            = (customOptions ? new SessionContext(ENDPOINT, new MockTokenCredential()) : new SessionContext())
                .setSessionMode(SessionContext.SessionMode.ENABLED);
        BlobServiceClient first = serviceClientBuilder().sessionContext(options).buildClient();
        SessionProvider provider = existingProvider(options);
        BlobServiceClient second = serviceClientBuilder().sessionContext(options).buildClient();
        serviceClientBuilder().sessionContext(options).buildAsyncClient();

        assertSame(first.getHttpPipeline(), first.getBlobContainerClient("container").getHttpPipeline());
        assertSame(first.getHttpPipeline(),
            first.getBlobContainerClient("container").getBlobClient("blob").getHttpPipeline());
        assertNotSame(first.getHttpPipeline(), second.getHttpPipeline());
        assertSame(provider, existingProvider(options));

        SessionContext separate
            = (customOptions ? new SessionContext(ENDPOINT, new MockTokenCredential()) : new SessionContext())
                .setSessionMode(SessionContext.SessionMode.ENABLED);
        serviceClientBuilder().sessionContext(separate).buildClient();
        assertNotSame(provider, existingProvider(separate));
    }

    @Test
    public void concurrentBuildersInitializeOnlyOneProvider() throws Exception {
        SessionContext options = new SessionContext().setSessionMode(SessionContext.SessionMode.ENABLED);
        ExecutorService executor = Executors.newFixedThreadPool(4);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<SessionProvider>> results = new ArrayList<>();
        try {
            for (int i = 0; i < 4; i++) {
                results.add(executor.submit(() -> {
                    assertTrue(start.await(10, TimeUnit.SECONDS));
                    serviceClientBuilder().sessionContext(options).buildClient();
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

    private static SessionProvider existingProvider(SessionContext options) {
        return SessionContextAccessor.getOrCreateSessionProvider(options, SessionProvider.class, () -> {
            throw new AssertionError("The builder must initialize the provider.");
        });
    }

    @Test
    public void constructorRequiresEndpointAndCredential() {
        assertThrows(NullPointerException.class, () -> new SessionContext(null, new MockTokenCredential()));
        assertThrows(NullPointerException.class, () -> new SessionContext(ENDPOINT, null));
    }

    @Test
    public void publicProviderTypesAreRemoved() {
        assertThrows(ClassNotFoundException.class,
            () -> Class.forName("com.azure.storage.blob.models.SessionProvider"));
        assertThrows(ClassNotFoundException.class,
            () -> Class.forName("com.azure.storage.blob.models.ContainerSessionProvider"));
    }

    @ParameterizedTest
    @CsvSource({
        "true,true,true",
        "true,true,false",
        "true,false,true",
        "true,false,false",
        "false,true,true",
        "false,true,false",
        "false,false,true",
        "false,false,false" })
    public void sharedProviderAcquiresThroughCallingClientsPipeline(boolean customOptions, boolean asyncFirst,
        boolean sameTransport) {
        ConcurrentHashMap<String, AtomicInteger> acquisitions = new ConcurrentHashMap<>();
        ConcurrentHashMap<String, AtomicInteger> downloads = new ConcurrentHashMap<>();
        AtomicReference<String> currentToken = new AtomicReference<>();
        AtomicBoolean rejectNextSession = new AtomicBoolean();
        AtomicInteger loggedAcquisitions = new AtomicInteger();
        TokenCredential acquisitionCredential
            = context -> Mono.just(new AccessToken("acquisition-token", OffsetDateTime.now().plusHours(1)));
        SessionContext options
            = (customOptions ? new SessionContext(ENDPOINT, acquisitionCredential) : new SessionContext())
                .setSessionMode(SessionContext.SessionMode.ENABLED);
        HttpClient firstTransport = consumerTransport(sameTransport ? null : "first", customOptions, acquisitions,
            downloads, currentToken, rejectNextSession);
        HttpClient secondTransport = sameTransport
            ? firstTransport
            : consumerTransport("second", customOptions, acquisitions, downloads, currentToken, rejectNextSession);
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
        assertNull(acquisitions.get("second"), "The second client must reuse the shared session.");
        assertEquals(2, downloads.get("first").get(), "Derived clients should reuse the first session.");
        assertEquals(2, downloads.get("second").get(), "Independent clients should reuse the same session.");

        // The service rejects the shared session; the next session is acquired through the requesting client.
        rejectNextSession.set(true);
        downloadFromDerivedClients(second, !asyncFirst);
        assertEquals(1, acquisitions.get("first").get());
        assertEquals(1, acquisitions.get("second").get(), "Reacquisition must use the requesting client's pipeline.");
        assertEquals("second", currentToken.get());
        assertEquals(0, loggedAcquisitions.get(), "Custom HTTP loggers must not receive session credentials.");
    }

    @Test
    public void sessionModeIsCapturedAtBuildAndAcquisitionUsesRequestingClient() {
        AtomicInteger acquisitions = new AtomicInteger();
        SessionContext options = new SessionContext();
        BlobServiceClient disabledClient = serviceClientBuilder().httpClient(request -> {
            assertEquals(HttpMethod.GET, request.getHttpMethod(),
                "A client built with sessions disabled must not acquire sessions.");
            assertTrue(request.getHeaders().getValue(HttpHeaderName.AUTHORIZATION).startsWith("Bearer "));
            return downloadResponse(request);
        }).sessionContext(options).buildClient();
        options.setSessionMode(SessionContext.SessionMode.ENABLED);
        downloadFromDerivedClients(disabledClient);

        BlobServiceClientBuilder enabled = serviceClientBuilder().httpClient(request -> {
            if (request.getHttpMethod() == HttpMethod.POST) {
                acquisitions.incrementAndGet();
                return sessionResponse(request, "enabled");
            }
            assertTrue(request.getHeaders().getValue(HttpHeaderName.AUTHORIZATION).startsWith("Session enabled:"));
            return downloadResponse(request);
        }).sessionContext(options);

        downloadFromDerivedClients(enabled, true);
        assertEquals(1, acquisitions.get());
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    public void acquisitionFallsBackToBearerAfterClientRetriesAreExhausted(boolean async) {
        AtomicInteger attempts = new AtomicInteger();
        AtomicInteger bearerDownloads = new AtomicInteger();
        HttpClient transport = request -> {
            if (request.getHttpMethod() == HttpMethod.POST) {
                attempts.incrementAndGet();
                return Mono.just(new MockHttpResponse(request, 503));
            }
            assertTrue(request.getHeaders().getValue(HttpHeaderName.AUTHORIZATION).startsWith("Bearer "));
            bearerDownloads.incrementAndGet();
            return downloadResponse(request);
        };
        BlobServiceClientBuilder builder = serviceClientBuilder().httpClient(transport)
            .sessionContext(new SessionContext().setSessionMode(SessionContext.SessionMode.ENABLED))
            .retryOptions(new RequestRetryOptions(RetryPolicyType.FIXED, 3, 10, 1L, 1L, null));

        downloadFromDerivedClients(builder, async);

        assertEquals(3, attempts.get(), "CreateSession must use the client's retry policy before falling back.");
        assertEquals(2, bearerDownloads.get(), "The container must stay in cooldown after the fallback.");
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
            .sessionContext(new SessionContext(ENDPOINT, new MockTokenCredential())
                .setSessionMode(SessionContext.SessionMode.ENABLED))
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
        SessionContext options = new SessionContext("http://account.blob.core.windows.net", new MockTokenCredential())
            .setSessionMode(SessionContext.SessionMode.ENABLED);
        assertThrows(IllegalArgumentException.class,
            () -> serviceClientBuilder().sessionContext(options).buildClient());
    }

    private static BlobServiceClientBuilder consumerBuilder(SessionContext options, HttpClient transport, String name,
        HttpLogOptions logOptions) {
        return serviceClientBuilder().httpClient(transport)
            .sessionContext(options)
            .serviceVersion(BlobServiceVersion.V2026_10_06)
            .httpLogOptions(logOptions)
            .addPolicy(new HttpLoggingPolicy(logOptions))
            .addPolicy(new AddHeadersPolicy(new HttpHeaders().set(CONSUMER_HEADER, name)));
    }

    private static HttpClient consumerTransport(String transportName, boolean customOptions,
        ConcurrentHashMap<String, AtomicInteger> acquisitions, ConcurrentHashMap<String, AtomicInteger> downloads,
        AtomicReference<String> currentToken, AtomicBoolean rejectNextSession) {
        return request -> {
            String name = request.getHeaders().getValue(CONSUMER_HEADER);
            assertTrue("first".equals(name) || "second".equals(name));
            if (transportName != null) {
                assertEquals(transportName, name, "CreateSession must use the consuming transport.");
            }
            assertEquals("account.blob.core.windows.net", request.getUrl().getHost());
            assertEquals(BlobServiceVersion.V2026_10_06.getVersion(),
                request.getHeaders().getValue(HttpHeaderName.fromString("x-ms-version")));
            String authorization = request.getHeaders().getValue(HttpHeaderName.AUTHORIZATION);
            if (request.getHttpMethod() == HttpMethod.POST) {
                assertEquals("/container", request.getUrl().getPath());
                assertEquals("restype=container&comp=session", request.getUrl().getQuery());
                assertEquals("Bearer " + (customOptions ? "acquisition-token" : "consumer-token"), authorization);
                acquisitions.computeIfAbsent(name, ignored -> new AtomicInteger()).incrementAndGet();
                currentToken.set(name);
                return sessionResponse(request, name);
            }
            assertEquals(HttpMethod.GET, request.getHttpMethod());
            if (authorization.startsWith("Bearer ")) {
                // Bearer retry after the service rejected a session-signed request.
                assertEquals("Bearer consumer-token", authorization);
                return downloadResponse(request);
            }
            assertTrue(authorization.startsWith("Session " + currentToken.get() + ":"));
            if (rejectNextSession.compareAndSet(true, false)) {
                return Mono.just(new MockHttpResponse(request, 401));
            }
            downloads.computeIfAbsent(name, ignored -> new AtomicInteger()).incrementAndGet();
            return downloadResponse(request);
        };
    }

    private static Mono<HttpResponse> sessionResponse(HttpRequest request, String token) {
        String body = "<CreateSessionResult><Id>test-session</Id><Expiration>"
            + new DateTimeRfc1123(OffsetDateTime.now().plusHours(1))
            + "</Expiration><AuthenticationType>HMAC</AuthenticationType><Credentials>" + "<SessionToken>" + token
            + "</SessionToken>" + "<SessionKey>dGVzdFNlc3Npb25LZXkxMjM0NTY3ODkwMTIzNDU2Nzg5MA==</SessionKey>"
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
        return new BlobServiceClientBuilder().endpoint(ENDPOINT)
            .credential(credential)
            .httpClient(new NoOpHttpClient());
    }

    private static void downloadFromDerivedClients(BlobServiceClientBuilder builder, boolean async) {
        if (async) {
            BlobServiceAsyncClient service = builder.buildAsyncClient();
            for (int i = 0; i < 2; i++) {
                StepVerifier.create(
                    service.getBlobContainerAsyncClient("container").getBlobAsyncClient("blob" + i).downloadContent())
                    .assertNext(content -> assertEquals("data", content.toString()))
                    .verifyComplete();
            }
        } else {
            downloadFromDerivedClients(builder.buildClient());
        }
    }

    private static void downloadFromDerivedClients(BlobServiceClient service) {
        for (int i = 0; i < 2; i++) {
            assertEquals("data",
                service.getBlobContainerClient("container").getBlobClient("blob" + i).downloadContent().toString());
        }
    }
}
