// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.util;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpResponse;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.core.util.Context;
import com.azure.core.util.DateTimeRfc1123;
import com.azure.core.util.tracing.Tracer;
import com.azure.storage.blob.BlobServiceVersion;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.implementation.accesshelpers.SessionContextAccessor;
import com.azure.storage.blob.SessionContext;
import com.azure.storage.common.policy.RequestRetryOptions;
import com.azure.storage.common.policy.RetryPolicyType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.Isolated;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.reactivestreams.Subscription;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.test.StepVerifier;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Deterministic, network-free tests for {@link ContainerSessionProvider}'s time-based, per-container caching
 * behavior.
 * <p>
 * These tests drive {@link ContainerSessionProvider} with an injectable {@link Clock} and a fake HTTP transport
 * ({@link CreateSessionTransport}) so the expiry, proactive-refresh, and per-container independence logic
 * can be exercised without waiting for real credential expiration or hitting the service.
 * {@link ContainerSessionApiTests} covers session acquisition through the service client; these tests
 * focus on cache timing, returned credentials, and CreateSession call counts. Per-container acquisition
 * cooldown is shared across clients using the same options. Tests inject a clock-controlled provider into options;
 * {@link SessionTests} covers provider creation and sharing through the public builders.
 */
@Isolated("Virtual-time tests temporarily replace Reactor schedulers.")
@Execution(ExecutionMode.SAME_THREAD)
public class ContainerSessionProviderCacheTests {

    private static final String ACCOUNT_NAME = "testaccount";
    private static final String CONTAINER_A = "container-a";
    private static final String CONTAINER_B = "container-b";
    private static final String FIRST_TOKEN = "first-session-token";
    private static final String SECOND_TOKEN = "second-session-token";
    private static final HttpHeaderName CLIENT_HEADER = HttpHeaderName.fromString("x-test-client");
    private static final HttpHeaderName ACQUISITION_HEADER = HttpHeaderName.fromString("x-test-acquisition");
    private static final HttpHeaderName TRACING_DISABLED_HEADER = HttpHeaderName.fromString("x-test-tracing-disabled");
    private static final String CLIENT_CONTEXT_KEY = "test-session-client";

    // A session's usable lifetime in these tests.
    private static final Duration SESSION_LIFETIME = Duration.ofMinutes(5);
    private static final Instant TEST_START = Instant.parse("2026-06-19T00:00:00Z");

    private MutableClock clock;
    private CreateSessionTransport httpClient;
    private ContainerSessionProvider provider;
    private SessionAcquirer acquirer;
    private SessionContext sessionContext;

    @BeforeEach
    public void setup() {
        clock = new MutableClock(TEST_START);
        httpClient = new CreateSessionTransport();

        configureProvider();
    }

    @Test
    public void policiesUsingSameProviderShareSessionsAndInvalidationSync() {
        configureTestProvider();
        enqueueSessionResponse(CONTAINER_A, FIRST_TOKEN, now().plusMinutes(20));
        enqueueSessionResponse(CONTAINER_B, "container-b-token", now().plusMinutes(20));
        enqueueSessionResponse(CONTAINER_A, SECOND_TOKEN, now().plusMinutes(20));
        HttpPipeline first = independentClientPipeline();
        HttpPipeline second = independentClientPipeline();

        assertEquals(0, httpClient.getRequestCount(CONTAINER_A));
        assertSyncToken(first, CONTAINER_A, FIRST_TOKEN);
        assertSyncToken(second, CONTAINER_A, FIRST_TOKEN);
        assertSyncToken(second, CONTAINER_B, "container-b-token");
        assertEquals(1, httpClient.getRequestCount(CONTAINER_A));
        assertEquals(1, httpClient.getRequestCount(CONTAINER_B));
        httpClient.rejectNextSession = true;
        assertSyncToken(first, CONTAINER_A, null);
        assertSyncToken(independentClientPipeline(), CONTAINER_A, SECOND_TOKEN);
        assertSyncToken(second, CONTAINER_A, SECOND_TOKEN);
        assertSyncToken(first, CONTAINER_B, "container-b-token");
        assertEquals(2, httpClient.getRequestCount(CONTAINER_A));
        assertEquals(1, httpClient.getRequestCount(CONTAINER_B));
    }

    @Test
    public void policiesUsingSameProviderShareSessionsAndInvalidationAsync() {
        configureTestProvider();
        enqueueSessionResponse(CONTAINER_A, FIRST_TOKEN, now().plusMinutes(20));
        enqueueSessionResponse(CONTAINER_B, "container-b-token", now().plusMinutes(20));
        enqueueSessionResponse(CONTAINER_A, SECOND_TOKEN, now().plusMinutes(20));
        HttpPipeline first = independentAsyncClientPipeline();
        HttpPipeline second = independentAsyncClientPipeline();

        assertEquals(0, httpClient.getRequestCount(CONTAINER_A));
        assertAsyncToken(first, CONTAINER_A, FIRST_TOKEN);
        assertAsyncToken(second, CONTAINER_A, FIRST_TOKEN);
        assertAsyncToken(second, CONTAINER_B, "container-b-token");
        assertEquals(1, httpClient.getRequestCount(CONTAINER_A));
        httpClient.rejectNextSession = true;
        assertAsyncToken(first, CONTAINER_A, null);
        assertAsyncToken(independentAsyncClientPipeline(), CONTAINER_A, SECOND_TOKEN);
        assertAsyncToken(second, CONTAINER_A, SECOND_TOKEN);
        assertAsyncToken(first, CONTAINER_B, "container-b-token");
        assertEquals(2, httpClient.getRequestCount(CONTAINER_A));
        assertEquals(1, httpClient.getRequestCount(CONTAINER_B));
    }

    @Test
    public void providerSharesSessionsBetweenSyncAndAsyncClients() {
        configureTestProvider();
        enqueueSessionResponse(CONTAINER_A, FIRST_TOKEN, now().plusMinutes(20));
        enqueueSessionResponse(CONTAINER_A, SECOND_TOKEN, now().plusMinutes(20));
        HttpPipeline sync = independentClientPipeline();
        HttpPipeline async = independentAsyncClientPipeline();

        assertSyncToken(sync, CONTAINER_A, FIRST_TOKEN);
        assertAsyncToken(async, CONTAINER_A, FIRST_TOKEN);
        assertEquals(1, httpClient.getRequestCount(CONTAINER_A));
        httpClient.rejectNextSession = true;
        assertAsyncToken(async, CONTAINER_A, null);
        assertSyncToken(sync, CONTAINER_A, SECOND_TOKEN);
        assertAsyncToken(async, CONTAINER_A, SECOND_TOKEN);
        assertEquals(2, httpClient.getRequestCount(CONTAINER_A));
    }

    /**
     * Each client acquires sessions through its own pipeline, and a forced refresh goes through the pipeline of the
     * client that used the session most recently.
     */
    @Test
    public void acquisitionAndRefreshUseTheCallingClientsPipeline() {
        configureTestProvider();
        enqueueSessionResponse(CONTAINER_A, FIRST_TOKEN, now().plusMinutes(20));
        enqueueSessionResponse(CONTAINER_A, SECOND_TOKEN, now().plusMinutes(20));
        HttpPipeline first = namedClientPipeline("first");
        HttpPipeline second = namedClientPipeline("second");

        assertSyncToken(first, CONTAINER_A, FIRST_TOKEN);
        assertAsyncToken(second, CONTAINER_A, FIRST_TOKEN);
        assertEquals(Collections.singletonList("first"), httpClient.getAcquiringClients());

        provider.refreshSession(contextFor(CONTAINER_A));
        waitForRequestCount(CONTAINER_A, 2);

        assertEquals(Arrays.asList("first", "second"), httpClient.getAcquiringClients());
    }

    @Test
    public void syncAcquisitionPreservesOriginatingContext() {
        enqueueContextAwareSessionResponse();
        HttpPipeline pipeline = contextAwareClientBuilder().buildClient().getHttpPipeline();
        Context context = new Context(CLIENT_CONTEXT_KEY, "sync").addData(Tracer.DISABLE_TRACING_KEY, true);
        HttpRequest request = dataRequest(CONTAINER_A);

        try (HttpResponse response = pipeline.sendSync(request, context)) {
            assertEquals(200, response.getStatusCode());
            assertEquals("sync", response.getRequest().getHeaders().getValue(CLIENT_HEADER));
            assertEquals("true", response.getRequest().getHeaders().getValue(TRACING_DISABLED_HEADER));
            assertNull(response.getRequest().getHeaders().getValue(ACQUISITION_HEADER));
        }

        assertEquals(Collections.singletonList("sync"), httpClient.getAcquiringClients());
        assertFalse(context.getData(SessionAcquirer.SESSION_ACQUISITION_CONTEXT_KEY).isPresent());
    }

    @Test
    public void asyncAcquisitionPreservesOriginatingContext() {
        enqueueContextAwareSessionResponse();
        HttpPipeline pipeline = contextAwareClientBuilder().buildAsyncClient().getHttpPipeline();
        Context context = new Context(CLIENT_CONTEXT_KEY, "async").addData(Tracer.DISABLE_TRACING_KEY, true);
        HttpRequest request = dataRequest(CONTAINER_A);

        StepVerifier.create(pipeline.send(request, context)).assertNext(response -> {
            assertEquals(200, response.getStatusCode());
            assertEquals("async", response.getRequest().getHeaders().getValue(CLIENT_HEADER));
            assertEquals("true", response.getRequest().getHeaders().getValue(TRACING_DISABLED_HEADER));
            assertNull(response.getRequest().getHeaders().getValue(ACQUISITION_HEADER));
            response.close();
        }).verifyComplete();

        assertEquals(Collections.singletonList("async"), httpClient.getAcquiringClients());
        assertFalse(context.getData(SessionAcquirer.SESSION_ACQUISITION_CONTEXT_KEY).isPresent());
    }

    @ParameterizedTest
    @CsvSource({ "403,", "500,", "503,", "599,", "400,FeatureNotEnabled", "400,featurenotenabled" })
    public void policiesUsingSameProviderShareCooldownAndExpirationSync(int status, String errorCode) {
        enqueueCooldown(status, errorCode);
        HttpPipeline first = independentClientPipeline();
        assertSyncToken(first, CONTAINER_A, null);
        assertSyncToken(independentClientPipeline(), CONTAINER_A, null);
        assertSyncToken(first, CONTAINER_B, "container-b-token");
        clock.advance(Duration.ofMinutes(5).minusSeconds(1));
        provider.refreshSession(contextFor(CONTAINER_A));
        assertSyncToken(independentClientPipeline(), CONTAINER_A, null);
        assertEquals(1, httpClient.getRequestCount(CONTAINER_A));
        clock.advance(Duration.ofSeconds(1));
        assertSyncToken(first, CONTAINER_A, SECOND_TOKEN);
        assertSyncToken(independentClientPipeline(), CONTAINER_A, SECOND_TOKEN);
        assertEquals(2, httpClient.getRequestCount(CONTAINER_A));
    }

    @ParameterizedTest
    @CsvSource({ "403,", "500,", "503,", "599,", "400,FeatureNotEnabled", "400,featurenotenabled" })
    public void policiesUsingSameProviderShareCooldownAndExpirationAsync(int status, String errorCode) {
        enqueueCooldown(status, errorCode);
        HttpPipeline first = independentAsyncClientPipeline();
        assertAsyncToken(first, CONTAINER_A, null);
        assertAsyncToken(independentAsyncClientPipeline(), CONTAINER_A, null);
        assertAsyncToken(first, CONTAINER_B, "container-b-token");
        clock.advance(Duration.ofMinutes(5).minusSeconds(1));
        provider.refreshSession(contextFor(CONTAINER_A));
        assertAsyncToken(independentAsyncClientPipeline(), CONTAINER_A, null);
        assertEquals(1, httpClient.getRequestCount(CONTAINER_A));
        clock.advance(Duration.ofSeconds(1));
        assertAsyncToken(first, CONTAINER_A, SECOND_TOKEN);
        assertAsyncToken(independentAsyncClientPipeline(), CONTAINER_A, SECOND_TOKEN);
        assertEquals(2, httpClient.getRequestCount(CONTAINER_A));
    }

    @ParameterizedTest
    @CsvSource({ "400,", "401,", "404,", "429,", "600,", "400,OtherError" })
    public void otherAcquisitionFailuresDoNotStartCooldownSync(int status, String errorCode) {
        enqueueCooldown(status, errorCode);
        assertSyncToken(independentClientPipeline(), CONTAINER_A, null);
        assertSyncToken(independentClientPipeline(), CONTAINER_A, SECOND_TOKEN);
        assertEquals(2, httpClient.getRequestCount(CONTAINER_A));
    }

    @ParameterizedTest
    @CsvSource({ "400,", "401,", "404,", "429,", "600,", "400,OtherError" })
    public void otherAcquisitionFailuresDoNotStartCooldownAsync(int status, String errorCode) {
        enqueueCooldown(status, errorCode);
        assertAsyncToken(independentAsyncClientPipeline(), CONTAINER_A, null);
        assertAsyncToken(independentAsyncClientPipeline(), CONTAINER_A, SECOND_TOKEN);
        assertEquals(2, httpClient.getRequestCount(CONTAINER_A));
    }

    @Test
    public void suppliedProviderHonorsAccountOverrideAndSeparatesSigningAccountsSync() {
        enqueueSessionResponse(CONTAINER_A, FIRST_TOKEN, now().plusMinutes(20));
        enqueueSessionResponse(CONTAINER_A, SECOND_TOKEN, now().plusMinutes(20));
        HttpPipeline original = overrideAccountBuilder().buildClient().getHttpPipeline();
        assertSyncToken(original, CONTAINER_A, FIRST_TOKEN);
        sessionContext.setAccountName(null);
        assertEquals("override-account",
            provider.getSession(contextFor(CONTAINER_A, "override-account")).getAccountName());
        assertSyncToken(independentClientPipeline(), CONTAINER_A, SECOND_TOKEN);
        assertEquals(ACCOUNT_NAME, provider.getSession(contextFor(CONTAINER_A)).getAccountName());
        assertSyncToken(original, CONTAINER_A, FIRST_TOKEN);
        assertEquals(2, httpClient.getRequestCount(CONTAINER_A));
    }

    @Test
    public void suppliedProviderHonorsAccountOverrideAndSeparatesSigningAccountsAsync() {
        enqueueSessionResponse(CONTAINER_A, FIRST_TOKEN, now().plusMinutes(20));
        enqueueSessionResponse(CONTAINER_A, SECOND_TOKEN, now().plusMinutes(20));
        HttpPipeline original = overrideAccountBuilder().buildAsyncClient().getHttpPipeline();
        assertAsyncToken(original, CONTAINER_A, FIRST_TOKEN);
        sessionContext.setAccountName(null);
        StepVerifier.create(provider.getSessionAsync(contextFor(CONTAINER_A, "override-account")))
            .assertNext(credential -> assertEquals("override-account", credential.getAccountName()))
            .verifyComplete();
        assertAsyncToken(independentAsyncClientPipeline(), CONTAINER_A, SECOND_TOKEN);
        StepVerifier.create(provider.getSessionAsync(contextFor(CONTAINER_A)))
            .assertNext(credential -> assertEquals(ACCOUNT_NAME, credential.getAccountName()))
            .verifyComplete();
        assertAsyncToken(original, CONTAINER_A, FIRST_TOKEN);
        assertEquals(2, httpClient.getRequestCount(CONTAINER_A));
    }

    @ParameterizedTest
    @CsvSource({
        "https://testaccount.blob.core.windows.net, , testaccount",
        "https://testaccount.blob.core.windows.net, '', testaccount",
        "https://testaccount.blob.core.windows.net, override-account, override-account",
        "https://testaccount.dfs.core.windows.net, , testaccount",
        "https://custom.endpoint.example, custom-account, custom-account" })
    public void builderResolvesAndCapturesSigningAccountSync(String endpoint, String accountName,
        String expectedAccountName) {
        HttpPipeline pipeline = accountResolutionBuilder(endpoint, accountName).buildClient().getHttpPipeline();
        sessionContext.setAccountName("changed");
        enqueueSessionResponse(CONTAINER_A, FIRST_TOKEN, now().plusMinutes(20));

        HttpRequest request = new HttpRequest(HttpMethod.GET, endpoint + "/" + CONTAINER_A + "/blob");
        try (HttpResponse response = pipeline.sendSync(request, Context.NONE)) {
            assertEquals(200, response.getStatusCode());
            assertAuthorization(response.getRequest(), FIRST_TOKEN);
        }
        SessionCredential credential = provider.getSession(contextFor(CONTAINER_A, expectedAccountName));
        assertEquals(expectedAccountName, credential.getAccountName());
        assertEquals(FIRST_TOKEN, credential.getSessionToken());
        assertEquals(1, httpClient.getRequestCount(CONTAINER_A));
    }

    @ParameterizedTest
    @CsvSource({
        "https://testaccount.blob.core.windows.net, , testaccount",
        "https://testaccount.blob.core.windows.net, '', testaccount",
        "https://testaccount.blob.core.windows.net, override-account, override-account",
        "https://testaccount.dfs.core.windows.net, , testaccount",
        "https://custom.endpoint.example, custom-account, custom-account" })
    public void builderResolvesAndCapturesSigningAccountAsync(String endpoint, String accountName,
        String expectedAccountName) {
        HttpPipeline pipeline = accountResolutionBuilder(endpoint, accountName).buildAsyncClient().getHttpPipeline();
        sessionContext.setAccountName("changed");
        enqueueSessionResponse(CONTAINER_A, FIRST_TOKEN, now().plusMinutes(20));

        HttpRequest request = new HttpRequest(HttpMethod.GET, endpoint + "/" + CONTAINER_A + "/blob");
        StepVerifier.create(pipeline.send(request)).assertNext(response -> {
            assertEquals(200, response.getStatusCode());
            assertAuthorization(response.getRequest(), FIRST_TOKEN);
            response.close();
        }).verifyComplete();
        StepVerifier.create(provider.getSessionAsync(contextFor(CONTAINER_A, expectedAccountName)))
            .assertNext(credential -> {
                assertEquals(expectedAccountName, credential.getAccountName());
                assertEquals(FIRST_TOKEN, credential.getSessionToken());
            })
            .verifyComplete();
        assertEquals(1, httpClient.getRequestCount(CONTAINER_A));
    }

    /**
     * A request returns a good (valid) token. The clock then advances past the token's expiration. The next
     * request must detect that the cached token is expired purely due to the passage of time and request a
     * brand-new session rather than reuse or send the expired one.
     */
    @Test
    public void expiredByTimeOnSecondRequestCreatesNewSession() {
        enqueueSessionResponse(CONTAINER_A, FIRST_TOKEN, now().plus(SESSION_LIFETIME));
        enqueueSessionResponse(CONTAINER_A, SECOND_TOKEN, now().plus(SESSION_LIFETIME.multipliedBy(2)));

        // First request: cold cache mints a good token and uses it.
        SessionCredential firstRequest = provider.getSession(contextFor(CONTAINER_A));
        assertEquals(FIRST_TOKEN, firstRequest.getSessionToken());
        assertEquals(1, httpClient.getRequestCount(CONTAINER_A));

        // Time advances past the first token's expiration with no traffic in between.
        clock.advance(SESSION_LIFETIME.plusSeconds(1));

        // Second request: the cached token is expired by time, so a new session is created instead of reused.
        SessionCredential secondRequest = provider.getSession(contextFor(CONTAINER_A));
        assertEquals(SECOND_TOKEN, secondRequest.getSessionToken());
        assertEquals(2, httpClient.getRequestCount(CONTAINER_A));
    }

    @Test
    public void validIdleCacheSurvivesEvictionSweep() {
        enqueueSessionResponse(CONTAINER_A, FIRST_TOKEN, now().plusMinutes(20));
        enqueueSessionResponse(CONTAINER_B, "container-b-token", now().plusMinutes(20));

        assertEquals(FIRST_TOKEN, provider.getSession(contextFor(CONTAINER_A)).getSessionToken());
        clock.advance(Duration.ofMinutes(1));

        assertEquals("container-b-token", provider.getSession(contextFor(CONTAINER_B)).getSessionToken());
        assertEquals(FIRST_TOKEN, provider.getSession(contextFor(CONTAINER_A)).getSessionToken());
        assertEquals(1, httpClient.getRequestCount(CONTAINER_A));
    }

    @Test
    public void evictionSweepRemovesExpiredEntries() {
        enqueueSessionResponse(CONTAINER_A, FIRST_TOKEN, now().plusMinutes(1));
        enqueueSessionResponse(CONTAINER_B, SECOND_TOKEN, now().plusMinutes(1));
        assertEquals(FIRST_TOKEN, provider.getSession(contextFor(CONTAINER_A)).getSessionToken());
        assertEquals(SECOND_TOKEN, provider.getSession(contextFor(CONTAINER_B)).getSessionToken());
        assertEquals(2, sessionCaches().size());
        clock.advance(Duration.ofMinutes(1));

        enqueueSessionResponse("trigger", "trigger-token", now().plusMinutes(20));
        assertEquals("trigger-token", provider.getSession(contextFor("trigger")).getSessionToken());

        assertEquals(1, sessionCaches().size());
        assertTrue(sessionCaches().containsKey(ACCOUNT_NAME + "/trigger"));
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void evictionSweepRemovesFailedInitialAcquisition(boolean transportError) {
        if (transportError) {
            enqueueFailure(CONTAINER_A);
        } else {
            httpClient.enqueueResponse(CONTAINER_A, request -> Mono.just(new MockHttpResponse(request, 404)));
        }
        StepVerifier.create(provider.getSessionAsync(contextFor(CONTAINER_A))).verifyError();
        assertEquals(1, sessionCaches().size());
        assertTrue(sessionCaches().containsKey(ACCOUNT_NAME + "/" + CONTAINER_A));

        sweepWithAnotherContainer();

        assertEquals(1, sessionCaches().size());
        assertFalse(sessionCaches().containsKey(ACCOUNT_NAME + "/" + CONTAINER_A));
        assertEquals(1, httpClient.getRequestCount(CONTAINER_A));
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void pendingAcquisitionSurvivesSweepAfterCallerCancellation(boolean retry) {
        if (retry) {
            enqueueFailure(CONTAINER_A);
            StepVerifier.create(provider.getSessionAsync(contextFor(CONTAINER_A))).verifyError();
        }
        Sinks.One<HttpResponse> pending = Sinks.one();
        AtomicReference<HttpRequest> request = new AtomicReference<>();
        AtomicInteger cancellations = new AtomicInteger();
        httpClient.enqueueResponse(CONTAINER_A, value -> {
            request.set(value);
            return pending.asMono().doOnCancel(cancellations::incrementAndGet);
        });

        StepVerifier.create(provider.getSessionAsync(contextFor(CONTAINER_A)))
            .then(() -> assertEquals(retry ? 2 : 1, httpClient.getRequestCount(CONTAINER_A)))
            .thenCancel()
            .verify();
        Object entry = sessionCaches().get(ACCOUNT_NAME + "/" + CONTAINER_A);
        try {
            // Even a long acquisition must not be mistaken for an idle failed entry.
            clock.advance(Duration.ofHours(1));
            sweepWithAnotherContainer();
            assertSame(entry, sessionCaches().get(ACCOUNT_NAME + "/" + CONTAINER_A));
            assertEquals(0, cancellations.get());
            StepVerifier.create(provider.getSessionAsync(contextFor(CONTAINER_A))).then(() -> {
                assertEquals(retry ? 2 : 1, httpClient.getRequestCount(CONTAINER_A));
                assertEquals(Sinks.EmitResult.OK,
                    pending.tryEmitValue(buildResponse(request.get(), FIRST_TOKEN, now().plusMinutes(20))));
            }).assertNext(value -> assertEquals(FIRST_TOKEN, value.getSessionToken())).verifyComplete();
        } finally {
            pending.tryEmitError(new IllegalStateException("Test finished."));
        }
    }

    @Test
    public void cancelledCallersFailedAcquisitionIsEvicted() {
        Sinks.One<HttpResponse> pending = Sinks.one();
        httpClient.enqueueResponse(CONTAINER_A, request -> pending.asMono());
        StepVerifier.create(provider.getSessionAsync(contextFor(CONTAINER_A))).thenCancel().verify();
        assertEquals(1, httpClient.getRequestCount(CONTAINER_A));

        assertEquals(Sinks.EmitResult.OK, pending.tryEmitError(new IllegalStateException("CreateSession failed.")));
        sweepWithAnotherContainer();

        assertFalse(sessionCaches().containsKey(ACCOUNT_NAME + "/" + CONTAINER_A));
        assertEquals(1, sessionCaches().size());
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void pendingRefreshSurvivesSweepAfterCredentialExpiration(boolean forced) {
        enqueueSessionResponse(CONTAINER_A, FIRST_TOKEN, now().plus(SESSION_LIFETIME));
        assertEquals(FIRST_TOKEN, provider.getSession(contextFor(CONTAINER_A)).getSessionToken());
        Object entry = sessionCaches().get(ACCOUNT_NAME + "/" + CONTAINER_A);
        Sinks.One<HttpResponse> pending = Sinks.one();
        AtomicReference<HttpRequest> request = new AtomicReference<>();
        httpClient.enqueueResponse(CONTAINER_A, value -> {
            request.set(value);
            return pending.asMono();
        });
        if (forced) {
            provider.refreshSession(contextFor(CONTAINER_A));
        } else {
            clock.advance(SESSION_LIFETIME.minusSeconds(2));
            assertEquals(FIRST_TOKEN, provider.getSession(contextFor(CONTAINER_A)).getSessionToken());
        }
        try {
            assertEquals(2, httpClient.getRequestCount(CONTAINER_A));
            clock.advance(SESSION_LIFETIME);
            sweepWithAnotherContainer();
            assertSame(entry, sessionCaches().get(ACCOUNT_NAME + "/" + CONTAINER_A));
            StepVerifier.create(provider.getSessionAsync(contextFor(CONTAINER_A))).then(() -> {
                assertEquals(2, httpClient.getRequestCount(CONTAINER_A));
                assertEquals(Sinks.EmitResult.OK,
                    pending.tryEmitValue(buildResponse(request.get(), SECOND_TOKEN, now().plusMinutes(20))));
            }).assertNext(value -> assertEquals(SECOND_TOKEN, value.getSessionToken())).verifyComplete();
        } finally {
            pending.tryEmitError(new IllegalStateException("Test finished."));
        }
    }

    @Test
    public void backgroundTimeoutReleasesEntryForEviction() {
        AtomicInteger cancellations = new AtomicInteger();
        StepVerifier.withVirtualTime(() -> {
            // Construct the entry only after virtual time installs the timeout scheduler.
            enqueueSessionResponse(CONTAINER_A, FIRST_TOKEN, now().plus(SESSION_LIFETIME));
            assertEquals(FIRST_TOKEN, provider.getSession(contextFor(CONTAINER_A)).getSessionToken());
            httpClient.enqueueResponse(CONTAINER_A,
                request -> Mono.<HttpResponse>never().doOnCancel(cancellations::incrementAndGet));
            provider.refreshSession(contextFor(CONTAINER_A));
            clock.advance(SESSION_LIFETIME);
            sweepWithAnotherContainer();
            assertTrue(sessionCaches().containsKey(ACCOUNT_NAME + "/" + CONTAINER_A));
            return Mono.never();
        }).expectSubscription().thenAwait(Duration.ofSeconds(30)).then(() -> {
            assertEquals(1, cancellations.get());
            sweepWithAnotherContainer();
            assertFalse(sessionCaches().containsKey(ACCOUNT_NAME + "/" + CONTAINER_A));
            assertEquals(1, sessionCaches().size());
        }).thenCancel().verify();
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void lookupReservationSurvivesSweepBeforeAcquisitionSubscription(boolean cancel) throws Exception {
        enqueueFailure(CONTAINER_A);
        StepVerifier.create(provider.getSessionAsync(contextFor(CONTAINER_A))).verifyError();
        Object entry = sessionCaches().get(ACCOUNT_NAME + "/" + CONTAINER_A);
        Sinks.One<HttpResponse> pending = Sinks.one();
        AtomicReference<HttpRequest> request = new AtomicReference<>();
        httpClient.enqueueResponse(CONTAINER_A, value -> {
            request.set(value);
            return pending.asMono();
        });
        CountDownLatch lookupReserved = new CountDownLatch(1);
        CountDownLatch resume = new CountDownLatch(1);
        AtomicInteger reads = new AtomicInteger();
        // The first clock read is the map lookup's sweep; the second is the shared cache's value lookup.
        // Pause before the supplier is subscribed, when tracking only supplier activity would miss the caller.
        clock.onRead = () -> {
            if (reads.incrementAndGet() == 2) {
                lookupReserved.countDown();
                awaitLatch(resume);
            }
        };
        ExecutorService executor = Executors.newSingleThreadExecutor();
        AtomicReference<Subscription> subscription = new AtomicReference<>();
        Future<CompletableFuture<SessionCredential>> first = executor.submit(
            () -> provider.getSessionAsync(contextFor(CONTAINER_A)).doOnSubscribe(subscription::set).toFuture());
        try {
            awaitLatch(lookupReserved);
            if (cancel) {
                subscription.get().cancel();
            }
            sweepWithAnotherContainer();
            assertSame(entry, sessionCaches().get(ACCOUNT_NAME + "/" + CONTAINER_A));
            CompletableFuture<SessionCredential> second = provider.getSessionAsync(contextFor(CONTAINER_A)).toFuture();
            resume.countDown();
            CompletableFuture<SessionCredential> firstResult = first.get(5, TimeUnit.SECONDS);
            assertFalse(firstResult.isDone());
            assertFalse(second.isDone());
            assertEquals(2, httpClient.getRequestCount(CONTAINER_A));
            assertEquals(Sinks.EmitResult.OK,
                pending.tryEmitValue(buildResponse(request.get(), FIRST_TOKEN, now().plusMinutes(20))));
            SessionCredential credential = second.get(5, TimeUnit.SECONDS);
            assertEquals(FIRST_TOKEN, credential.getSessionToken());
            if (cancel) {
                // Cancelling the raw Subscription above deliberately does not complete the observing future.
                assertFalse(firstResult.isDone());
                firstResult.cancel(false);
            } else {
                assertSame(firstResult.get(5, TimeUnit.SECONDS), credential);
            }
        } finally {
            clock.onRead = null;
            resume.countDown();
            pending.tryEmitError(new IllegalStateException("Test finished."));
            executor.shutdownNow();
        }
    }

    @Test
    public void activeCooldownSurvivesEvictionSweep() {
        httpClient.enqueueResponse(CONTAINER_A, request -> {
            MockHttpResponse response = new MockHttpResponse(request, 403);
            return Mono.just(response);
        });
        enqueueSessionResponse(CONTAINER_A, SECOND_TOKEN, now().plusMinutes(20));

        assertEquals(null, provider.getSession(contextFor(CONTAINER_A)));
        clock.advance(Duration.ofMinutes(1));
        enqueueSessionResponse(CONTAINER_B, "container-b-token", now().plusMinutes(20));
        assertEquals("container-b-token", provider.getSession(contextFor(CONTAINER_B)).getSessionToken());
        assertEquals(null, provider.getSession(contextFor(CONTAINER_A)));
        assertEquals(1, httpClient.getRequestCount(CONTAINER_A));
    }

    @Test
    public void concurrentAccessAtSweepBoundaryRetainsValidCache() throws Exception {
        enqueueSessionResponse(CONTAINER_A, FIRST_TOKEN, now().plusMinutes(20));
        SessionRequestContext context = contextFor(CONTAINER_A);
        assertEquals(FIRST_TOKEN, provider.getSession(context).getSessionToken());
        clock.advance(Duration.ofMinutes(1));

        int callerCount = 16;
        ExecutorService executor = Executors.newFixedThreadPool(callerCount);
        CountDownLatch ready = new CountDownLatch(callerCount);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<SessionCredential>> results = new ArrayList<>();
        try {
            for (int i = 0; i < callerCount; i++) {
                results.add(executor.submit(() -> {
                    ready.countDown();
                    start.await();
                    return provider.getSession(context);
                }));
            }
            awaitLatch(ready);
            start.countDown();
            for (Future<SessionCredential> result : results) {
                assertEquals(FIRST_TOKEN, result.get(5, TimeUnit.SECONDS).getSessionToken());
            }
        } finally {
            start.countDown();
            executor.shutdownNow();
        }
        assertEquals(1, httpClient.getRequestCount(CONTAINER_A));
    }

    @Test
    public void absentExpirationUsesProviderClock() {
        enqueueSessionResponse(CONTAINER_A, FIRST_TOKEN, null);

        SessionCredential credential = provider.getSession(contextFor(CONTAINER_A));

        assertEquals(now().plus(SESSION_LIFETIME), credential.getExpiresAt());
    }

    /**
     * A stale rejection must not evict a credential that a background refresh has already replaced.
     */
    @Test
    public void invalidateSessionIgnoresACredentialAlreadyReplacedByRefresh() {
        enqueueSessionResponse(CONTAINER_A, FIRST_TOKEN, now().plus(SESSION_LIFETIME));
        enqueueSessionResponse(CONTAINER_A, SECOND_TOKEN, now().plus(SESSION_LIFETIME.multipliedBy(2)));
        SessionRequestContext context = contextFor(CONTAINER_A);

        SessionCredential firstCredential = provider.getSession(context);

        // Replace the cached credential before reporting a rejection of the original one.
        clock.advance(SESSION_LIFETIME.minusSeconds(2));
        assertEquals(FIRST_TOKEN, provider.getSession(context).getSessionToken());
        assertEquals(2, httpClient.getRequestCount(CONTAINER_A));
        assertEquals(SECOND_TOKEN, waitForToken(() -> provider.getSession(context)).getSessionToken());
        assertEquals(2, httpClient.getRequestCount(CONTAINER_A));

        assertFalse(provider.invalidateSession(context, firstCredential));
        assertEquals(SECOND_TOKEN, provider.getSession(context).getSessionToken());
        assertEquals(2, httpClient.getRequestCount(CONTAINER_A));
    }

    /**
     * Two different containers must refresh completely independently: advancing the clock past one
     * container's jittered refresh point and accessing it must trigger a background refresh for that container
     * only, leaving the other container's cached session untouched until that container is accessed.
     */
    @Test
    public void independentContainersRefreshIndependently() {
        enqueueSessionResponse(CONTAINER_A, FIRST_TOKEN, now().plus(SESSION_LIFETIME));
        enqueueSessionResponse(CONTAINER_A, "refreshed-a", now().plus(SESSION_LIFETIME.multipliedBy(2)));
        enqueueSessionResponse(CONTAINER_B, SECOND_TOKEN, now().plus(SESSION_LIFETIME));

        // Mint an initial session for each container.
        assertEquals(FIRST_TOKEN, provider.getSession(contextFor(CONTAINER_A)).getSessionToken());
        assertEquals(SECOND_TOKEN, provider.getSession(contextFor(CONTAINER_B)).getSessionToken());

        // Advance past container A's jittered refresh window (both containers were minted at the same time,
        // so this is also past B's refresh window by clock time - but B must only refresh once *it* is
        // accessed, not merely because time passed).
        clock.advance(SESSION_LIFETIME.minusSeconds(2));

        // Touching container A triggers its background refresh. The refresh runs on a background
        // subscription, so poll briefly rather than asserting the call count immediately.
        assertEquals(FIRST_TOKEN, provider.getSession(contextFor(CONTAINER_A)).getSessionToken());
        waitForRequestCount(CONTAINER_A, 2);
        assertEquals(2, httpClient.getRequestCount(CONTAINER_A));

        // Container B has not been touched since the clock advanced, so it must not have refreshed - proving
        // the two containers' caches refresh independently on access.
        assertEquals(1, httpClient.getRequestCount(CONTAINER_B));
    }

    /**
     * Guards against over-eager refreshing: while the token is comfortably before its jittered refresh point
     * and no service hint has arrived, repeated requests must reuse the same cached token and never trigger a
     * refresh.
     */
    @Test
    public void noRefreshBeforeJitterWindowWithoutServiceHint() {
        enqueueSessionResponse(CONTAINER_A, FIRST_TOKEN, now().plus(SESSION_LIFETIME));

        // First request mints the token.
        assertEquals(FIRST_TOKEN, provider.getSession(contextFor(CONTAINER_A)).getSessionToken());

        // Advance only slightly - well before 80% of (lifetime minus the safety buffer).
        clock.advance(Duration.ofSeconds(30));

        // Several more requests reuse the same token; no refresh is triggered.
        for (int i = 0; i < 3; i++) {
            assertEquals(FIRST_TOKEN, provider.getSession(contextFor(CONTAINER_A)).getSessionToken());
        }

        assertEquals(1, httpClient.getRequestCount(CONTAINER_A));
    }

    /**
     * Once the async path has cached a usable value, later async requests made before the jittered refresh
     * window must replay that cached value rather than creating a second one.
     */
    @Test
    public void cachedValueIsReusedOnLaterAsyncRequests() {
        enqueueSessionResponse(CONTAINER_A, FIRST_TOKEN, now().plus(SESSION_LIFETIME));

        StepVerifier.create(provider.getSessionAsync(contextFor(CONTAINER_A)))
            .assertNext(credential -> assertEquals(FIRST_TOKEN, credential.getSessionToken()))
            .verifyComplete();

        // Advance well short of 80% of (lifetime minus the safety buffer).
        clock.advance(Duration.ofSeconds(30));

        StepVerifier.create(provider.getSessionAsync(contextFor(CONTAINER_A)))
            .assertNext(credential -> assertEquals(FIRST_TOKEN, credential.getSessionToken()))
            .verifyComplete();

        assertEquals(1, httpClient.getRequestCount(CONTAINER_A));
    }

    /**
     * A failed creation must surface to the caller as an error signal rather than an empty completion, and it
     * must not poison the cache: the in-flight creation is cleared so a later request can retry successfully.
     */
    @Test
    public void creationFailurePropagatesAndAllowsRetryAsync() {
        enqueueFailure(CONTAINER_A);
        enqueueSessionResponse(CONTAINER_A, FIRST_TOKEN, now().plus(SESSION_LIFETIME));

        StepVerifier.create(provider.getSessionAsync(contextFor(CONTAINER_A))).verifyError();

        // The failure left no cached value behind, so the retry mints a fresh one.
        StepVerifier.create(provider.getSessionAsync(contextFor(CONTAINER_A)))
            .assertNext(credential -> assertEquals(FIRST_TOKEN, credential.getSessionToken()))
            .verifyComplete();

        assertEquals(2, httpClient.getRequestCount(CONTAINER_A));
    }

    /**
     * Container names must be matched case-insensitively: a container looked up with different casing must
     * reuse the same cache entry rather than minting a duplicate session.
     */
    @Test
    public void containerNameLookupIsCaseInsensitive() {
        enqueueSessionResponse(CONTAINER_A, FIRST_TOKEN, now().plus(SESSION_LIFETIME));

        assertEquals(FIRST_TOKEN, provider.getSession(contextFor(CONTAINER_A)).getSessionToken());
        assertEquals(FIRST_TOKEN,
            provider.getSession(contextFor(CONTAINER_A.toUpperCase(Locale.ROOT))).getSessionToken());

        assertEquals(1, httpClient.getRequestCount(CONTAINER_A));
    }

    @Test
    public void refreshAndInvalidationAreOwnedByProvider() {
        enqueueSessionResponse(CONTAINER_A, FIRST_TOKEN, now().plus(SESSION_LIFETIME));
        enqueueSessionResponse(CONTAINER_A, SECOND_TOKEN, now().plus(SESSION_LIFETIME));
        enqueueSessionResponse(CONTAINER_A, "third-session-token", now().plus(SESSION_LIFETIME));
        SessionRequestContext context = contextFor(CONTAINER_A);

        SessionCredential first = provider.getSession(context);
        provider.refreshSession(context);
        SessionCredential second = waitForToken(() -> provider.getSession(context));

        assertFalse(provider.invalidateSession(context, first));
        assertTrue(provider.invalidateSession(context, second));
        assertFalse(provider.invalidateSession(context, second));
        assertEquals("third-session-token", provider.getSession(context).getSessionToken());
        assertEquals(3, httpClient.getRequestCount(CONTAINER_A));
    }

    @Test
    public void syncAndAsyncRequestsShareInFlightCreation() throws Exception {
        Sinks.One<HttpResponse> pendingResponse = Sinks.one();
        AtomicReference<HttpRequest> pendingRequest = new AtomicReference<>();
        CountDownLatch requestStarted = new CountDownLatch(1);
        httpClient.enqueueResponse(CONTAINER_A, request -> {
            pendingRequest.set(request);
            requestStarted.countDown();
            return pendingResponse.asMono();
        });
        SessionRequestContext context = contextFor(CONTAINER_A);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<SessionCredential> syncResult = executor.submit(() -> provider.getSession(context));
        try {
            awaitLatch(requestStarted);
            CompletableFuture<SessionCredential> asyncResult = provider.getSessionAsync(context).toFuture();
            assertFalse(syncResult.isDone());
            assertFalse(asyncResult.isDone());
            assertEquals(1, httpClient.getRequestCount(CONTAINER_A));

            assertEquals(Sinks.EmitResult.OK, pendingResponse
                .tryEmitValue(buildResponse(pendingRequest.get(), FIRST_TOKEN, now().plus(SESSION_LIFETIME))));

            SessionCredential credential = syncResult.get(5, TimeUnit.SECONDS);
            assertEquals(FIRST_TOKEN, credential.getSessionToken());
            assertSame(credential, asyncResult.get(5, TimeUnit.SECONDS));
            assertSame(credential, provider.getSession(context));
            StepVerifier.create(provider.getSessionAsync(context))
                .assertNext(value -> assertSame(credential, value))
                .verifyComplete();
            assertEquals(1, httpClient.getRequestCount(CONTAINER_A));
        } finally {
            pendingResponse.tryEmitError(new IllegalStateException("Test finished."));
            syncResult.cancel(true);
            executor.shutdownNow();
        }
    }

    private void configureProvider() {
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(httpClient).build();
        provider = new ContainerSessionProvider(clock);
        acquirer = new SessionAcquirer("https://" + ACCOUNT_NAME + ".blob.core.windows.net",
            BlobServiceVersion.getLatest(), ACCOUNT_NAME, null).bind(pipeline);
        initializeSessionContext();
    }

    private void configureTestProvider() {
        clock = new MutableClock(Instant.now());
        configureProvider();
    }

    private void enqueueContextAwareSessionResponse() {
        httpClient.enqueueResponse(CONTAINER_A, request -> {
            assertEquals("true", request.getHeaders().getValue(ACQUISITION_HEADER));
            assertEquals("true", request.getHeaders().getValue(TRACING_DISABLED_HEADER));
            return Mono.just(buildResponse(request, FIRST_TOKEN, now().plusMinutes(20)));
        });
    }

    private BlobServiceClientBuilder contextAwareClientBuilder() {
        return independentClientBuilder().addPolicy((context, next) -> {
            context.getData(CLIENT_CONTEXT_KEY)
                .ifPresent(value -> context.getHttpRequest().setHeader(CLIENT_HEADER, value.toString()));
            context.getData(Tracer.DISABLE_TRACING_KEY)
                .filter(Boolean.TRUE::equals)
                .ifPresent(ignored -> context.getHttpRequest().setHeader(TRACING_DISABLED_HEADER, "true"));
            if (SessionAcquirer.isAcquisitionRequest(context)) {
                context.getHttpRequest().setHeader(ACQUISITION_HEADER, "true");
            }
            return next.process();
        });
    }

    private HttpPipeline namedClientPipeline(String name) {
        return independentClientBuilder().addPolicy((context, next) -> {
            context.getHttpRequest().setHeader(CLIENT_HEADER, name);
            return next.process();
        }).buildClient().getHttpPipeline();
    }

    private void enqueueCooldown(int status, String errorCode) {
        httpClient.enqueueResponse(CONTAINER_A, request -> {
            MockHttpResponse response = new MockHttpResponse(request, status);
            if (errorCode != null) {
                response.addHeader("x-ms-error-code", errorCode);
            }
            return Mono.just(response);
        });
        enqueueSessionResponse(CONTAINER_A, SECOND_TOKEN, now().plusMinutes(20));
        enqueueSessionResponse(CONTAINER_B, "container-b-token", now().plusMinutes(20));
    }

    private BlobServiceClientBuilder accountResolutionBuilder(String endpoint, String accountName) {
        sessionContext
            = new SessionContext().setSessionMode(SessionContext.SessionMode.ENABLED).setAccountName(accountName);
        SessionContextAccessor.getOrCreateSessionProvider(sessionContext, SessionProvider.class, () -> provider);
        return independentClientBuilder().endpoint(endpoint);
    }

    private BlobServiceClientBuilder overrideAccountBuilder() {
        sessionContext.setAccountName("override-account");
        return independentClientBuilder();
    }

    private BlobServiceClientBuilder independentClientBuilder() {
        // Retries are disabled so each CreateSession response is consumed by exactly one acquisition attempt.
        return new BlobServiceClientBuilder().endpoint("https://" + ACCOUNT_NAME + ".blob.core.windows.net")
            .credential(new MockTokenCredential())
            .httpClient(httpClient)
            .retryOptions(new RequestRetryOptions(RetryPolicyType.FIXED, 1, 30, 1L, 1L, null))
            .sessionContext(sessionContext);
    }

    private void initializeSessionContext() {
        sessionContext
            = new SessionContext("https://" + ACCOUNT_NAME + ".blob.core.windows.net", new MockTokenCredential())
                .setSessionMode(SessionContext.SessionMode.ENABLED);
        SessionContextAccessor.getOrCreateSessionProvider(sessionContext, SessionProvider.class, () -> provider);
    }

    private HttpPipeline independentClientPipeline() {
        return independentClientBuilder().buildClient().getHttpPipeline();
    }

    private HttpPipeline independentAsyncClientPipeline() {
        return independentClientBuilder().buildAsyncClient().getHttpPipeline();
    }

    private void assertSyncToken(HttpPipeline pipeline, String container, String token) {
        HttpRequest request = dataRequest(container);
        try (HttpResponse response = pipeline.sendSync(request, Context.NONE)) {
            assertEquals(200, response.getStatusCode());
            assertAuthorization(response.getRequest(), token);
        }
    }

    private void assertAsyncToken(HttpPipeline pipeline, String container, String token) {
        HttpRequest request = dataRequest(container);
        StepVerifier.create(pipeline.send(request)).assertNext(response -> {
            assertEquals(200, response.getStatusCode());
            assertAuthorization(response.getRequest(), token);
            response.close();
        }).verifyComplete();
    }

    private static HttpRequest dataRequest(String container) {
        return new HttpRequest(HttpMethod.GET,
            "https://" + ACCOUNT_NAME + ".blob.core.windows.net/" + container + "/blob");
    }

    private static void assertAuthorization(HttpRequest request, String token) {
        String authorization = request.getHeaders().getValue(HttpHeaderName.AUTHORIZATION);
        assertTrue(authorization.startsWith(token == null ? "Bearer " : "Session " + token + ":"));
    }

    private Map<?, ?> sessionCaches() {
        try {
            Field field = ContainerSessionProvider.class.getDeclaredField("containerSessionCaches");
            field.setAccessible(true);
            return (Map<?, ?>) field.get(provider);
        } catch (ReflectiveOperationException ex) {
            throw new AssertionError(ex);
        }
    }

    private void sweepWithAnotherContainer() {
        clock.advance(Duration.ofMinutes(1));
        enqueueSessionResponse(CONTAINER_B, "container-b-token", now().plusMinutes(20));
        assertEquals("container-b-token", provider.getSession(contextFor(CONTAINER_B)).getSessionToken());
    }

    private static void awaitLatch(CountDownLatch latch) {
        try {
            assertTrue(latch.await(5, TimeUnit.SECONDS), "Timed out waiting for async result.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }

    /**
     * Repeatedly invokes {@code supplier} (which triggers a synchronous cache lookup that may itself kick
     * off a background refresh subscription) until it observes {@link #SECOND_TOKEN} or a timeout elapses.
     * Background refreshes complete on a separate subscription from the caller that triggered them, so
     * asserting on the very next call without allowing for that latency would be flaky.
     */
    private static SessionCredential waitForToken(Supplier<SessionCredential> supplier) {
        long deadline = System.nanoTime() + Duration.ofSeconds(5).toNanos();
        SessionCredential last;
        do {
            last = supplier.get();
            if (SECOND_TOKEN.equals(last.getSessionToken())) {
                return last;
            }
            sleepBriefly();
        } while (System.nanoTime() < deadline);
        return last;
    }

    private void waitForRequestCount(String container, int expectedCount) {
        long deadline = System.nanoTime() + Duration.ofSeconds(5).toNanos();
        while (httpClient.getRequestCount(container) < expectedCount && System.nanoTime() < deadline) {
            sleepBriefly();
        }
    }

    private static void sleepBriefly() {
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }

    private SessionRequestContext contextFor(String containerName) {
        return new SessionRequestContext(containerName, acquirer, Context.NONE);
    }

    private SessionRequestContext contextFor(String containerName, String accountName) {
        SessionAcquirer accountAcquirer
            = new SessionAcquirer("https://" + ACCOUNT_NAME + ".blob.core.windows.net", BlobServiceVersion.getLatest(),
                accountName, null).bind(new HttpPipelineBuilder().httpClient(httpClient).build());
        return new SessionRequestContext(containerName, accountAcquirer, Context.NONE);
    }

    private OffsetDateTime now() {
        return OffsetDateTime.now(clock);
    }

    private void enqueueSessionResponse(String container, String token, OffsetDateTime expiresAt) {
        httpClient.enqueueResponse(container, request -> Mono.just(buildResponse(request, token, expiresAt)));
    }

    private void enqueueFailure(String container) {
        httpClient.enqueueResponse(container,
            request -> Mono.error(new IllegalStateException("CreateSession failed.")));
    }

    private static String containerFromPath(HttpRequest request) {
        String path = request.getUrl().getPath();
        return path.startsWith("/") ? path.substring(1) : path;
    }

    private static HttpResponse buildResponse(HttpRequest request, String token, OffsetDateTime expiresAt) {
        String expiration = expiresAt == null ? "" : "<Expiration>" + new DateTimeRfc1123(expiresAt) + "</Expiration>";
        String body = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" + "<CreateSessionResult>"
            + "<Id>test-session-id</Id>" + expiration + "<AuthenticationType>HMAC</AuthenticationType>"
            + "<Credentials>" + "<SessionToken>" + token + "</SessionToken>"
            + "<SessionKey>dGVzdFNlc3Npb25LZXkxMjM0NTY3ODkwMTIzNDU2Nzg5MA==</SessionKey>" + "</Credentials>"
            + "</CreateSessionResult>";

        return new MockHttpResponse(request, 201, body.getBytes(StandardCharsets.UTF_8)).addHeader("Content-Type",
            "application/xml");
    }

    /**
     * Serves a per-container FIFO queue of CreateSession responses, keyed off the container name in the request
     * path, and counts the requests each container received.
     */
    private static final class CreateSessionTransport implements HttpClient {
        private boolean rejectNextSession;
        private final ConcurrentHashMap<String, ConcurrentLinkedQueue<Function<HttpRequest, Mono<HttpResponse>>>> responsesByContainer
            = new ConcurrentHashMap<>();
        private final ConcurrentHashMap<String, AtomicInteger> requestCountsByContainer = new ConcurrentHashMap<>();
        private final List<String> acquiringClients = Collections.synchronizedList(new ArrayList<>());

        void enqueueResponse(String container, Function<HttpRequest, Mono<HttpResponse>> response) {
            responsesByContainer.computeIfAbsent(container, ignored -> new ConcurrentLinkedQueue<>()).add(response);
        }

        int getRequestCount(String container) {
            AtomicInteger count = requestCountsByContainer.get(container);
            return count == null ? 0 : count.get();
        }

        List<String> getAcquiringClients() {
            synchronized (acquiringClients) {
                return new ArrayList<>(acquiringClients);
            }
        }

        @Override
        public Mono<HttpResponse> send(HttpRequest request) {
            if (request.getHttpMethod() == HttpMethod.GET) {
                boolean rejected = rejectNextSession
                    && request.getHeaders().getValue(HttpHeaderName.AUTHORIZATION).startsWith("Session ");
                rejectNextSession = false;
                return Mono.just(new MockHttpResponse(request, rejected ? 401 : 200));
            }
            String clientName = request.getHeaders().getValue(CLIENT_HEADER);
            if (clientName != null) {
                acquiringClients.add(clientName);
            }
            String container = containerFromPath(request);
            requestCountsByContainer.computeIfAbsent(container, ignored -> new AtomicInteger()).incrementAndGet();

            ConcurrentLinkedQueue<Function<HttpRequest, Mono<HttpResponse>>> responses
                = responsesByContainer.get(container);
            Function<HttpRequest, Mono<HttpResponse>> response = responses == null ? null : responses.poll();
            return response == null
                ? Mono.error(new IllegalStateException("No CreateSession response for " + container))
                : response.apply(request);
        }
    }

    /**
     * A {@link Clock} whose instant can be advanced, allowing deterministic control of the cache's notion
     * of "now" without sleeping.
     */
    private static final class MutableClock extends Clock {
        private final ZoneId zone;
        private volatile Instant instant;
        private volatile Runnable onRead;

        MutableClock(Instant instant) {
            this(instant, ZoneOffset.UTC);
        }

        private MutableClock(Instant instant, ZoneId zone) {
            this.instant = instant;
            this.zone = zone;
        }

        @Override
        public ZoneId getZone() {
            return zone;
        }

        @Override
        public Clock withZone(ZoneId newZone) {
            return new MutableClock(instant, newZone);
        }

        @Override
        public Instant instant() {
            Runnable callback = onRead;
            if (callback != null) {
                callback.run();
            }
            return instant;
        }

        void advance(Duration duration) {
            instant = instant.plus(duration);
        }
    }
}
