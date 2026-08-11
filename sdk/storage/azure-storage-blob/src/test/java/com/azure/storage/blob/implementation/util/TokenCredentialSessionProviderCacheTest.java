// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.util;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.util.DateTimeRfc1123;
import com.azure.storage.blob.BlobServiceVersion;
import com.azure.storage.blob.models.SessionCredential;
import com.azure.storage.blob.models.SessionRequestContext;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.test.StepVerifier;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Deterministic, network-free tests for {@link TokenCredentialSessionProvider}'s time-based, per-container caching
 * behavior.
 * <p>
 * These tests drive {@link TokenCredentialSessionProvider} with an injectable {@link Clock} and a fake HTTP transport
 * ({@link ControllableHttpClient}) so the expiry, proactive-refresh, and per-container independence logic
 * can be exercised without sleeping or hitting the service. Unlike {@code SessionProviderSeamTest} (which
 * verifies the container name is placed correctly on the wire), these tests focus on cache timing: which
 * token is returned when, and how many CreateSession calls are made. Account-level acquisition cooldown is
 * covered separately by {@code SessionTokenCredentialPolicyTest}.
 */
public class TokenCredentialSessionProviderCacheTest {

    private static final String ACCOUNT_NAME = "myaccount";
    private static final String CONTAINER_A = "container-a";
    private static final String CONTAINER_B = "container-b";
    private static final String FIRST_TOKEN = "first-session-token";
    private static final String SECOND_TOKEN = "second-session-token";

    // A session's usable lifetime in these tests (the service issues ~5 minute sessions).
    private static final Duration SESSION_LIFETIME = Duration.ofMinutes(5);

    /**
     * A request returns a good (valid) token. The clock then advances past the token's expiration. The next
     * request must detect that the cached token is expired purely due to the passage of time and request a
     * brand-new session rather than reuse or send the expired one.
     */
    @Test
    public void expiredByTimeOnSecondRequestCreatesNewSession() {
        MutableClock clock = new MutableClock(Instant.parse("2026-06-19T00:00:00Z"));
        ControllableHttpClient httpClient = new ControllableHttpClient();
        httpClient.enqueue(CONTAINER_A, FIRST_TOKEN, now(clock).plus(SESSION_LIFETIME));
        httpClient.enqueue(CONTAINER_A, SECOND_TOKEN, now(clock).plus(SESSION_LIFETIME.multipliedBy(2)));
        TokenCredentialSessionProvider provider = createProvider(httpClient, clock);

        // First request: cold cache mints a good token and uses it.
        SessionCredential firstRequest = provider.getSession(contextFor(CONTAINER_A));
        assertEquals(FIRST_TOKEN, firstRequest.getSessionToken());
        assertEquals(1, httpClient.getCallCount(CONTAINER_A));

        // Time advances past the first token's expiration with no traffic in between.
        clock.advance(SESSION_LIFETIME.plusSeconds(1));

        // Second request: the cached token is expired by time, so a new session is created instead of reused.
        SessionCredential secondRequest = provider.getSession(contextFor(CONTAINER_A));
        assertEquals(SECOND_TOKEN, secondRequest.getSessionToken());
        assertEquals(2, httpClient.getCallCount(CONTAINER_A));
    }

    /**
     * When the service has NOT sent a {@code session_expiring} hint, the cache must still refresh
     * automatically once its own jittered timer elapses (while the current token is still usable), serving
     * the current token until the refreshed one is ready.
     */
    @Test
    public void automaticBackgroundRefreshFiresWithoutServiceHint() {
        MutableClock clock = new MutableClock(Instant.parse("2026-06-19T00:00:00Z"));
        ControllableHttpClient httpClient = new ControllableHttpClient();
        httpClient.enqueue(CONTAINER_A, FIRST_TOKEN, now(clock).plus(SESSION_LIFETIME));
        httpClient.enqueue(CONTAINER_A, SECOND_TOKEN, now(clock).plus(SESSION_LIFETIME.multipliedBy(2)));
        TokenCredentialSessionProvider provider = createProvider(httpClient, clock);

        // First request: cold cache mints the initial token.
        assertEquals(FIRST_TOKEN, provider.getSession(contextFor(CONTAINER_A)).getSessionToken());
        assertEquals(1, httpClient.getCallCount(CONTAINER_A));

        // Advance to a point guaranteed to be past the jittered refresh time (80-100% of lifetime minus the
        // 5s safety buffer => at most lifetime-5s) but still before hard expiry, so the token remains usable.
        clock.advance(SESSION_LIFETIME.minusSeconds(2));

        // Second request: token still usable, refresh timer elapsed, no service hint => automatic background
        // refresh. The current token is served while the refresh happens.
        assertEquals(FIRST_TOKEN, provider.getSession(contextFor(CONTAINER_A)).getSessionToken());
        assertEquals(2, httpClient.getCallCount(CONTAINER_A));

        // Third request: the background refresh has swapped in the new token, which is now served. The
        // refresh runs on a background subscription, so poll briefly rather than asserting immediately.
        assertEquals(SECOND_TOKEN, waitForToken(() -> provider.getSession(contextFor(CONTAINER_A))).getSessionToken());
        // Still only one inline creation and one background refresh overall (no over-eager churn).
        assertEquals(2, httpClient.getCallCount(CONTAINER_A));
    }

    /**
     * Two different containers must refresh completely independently: advancing the clock past one
     * container's jittered refresh point must trigger a background refresh for that container only, leaving
     * the other container's still-fresh session untouched.
     */
    @Test
    public void independentContainersRefreshIndependently() {
        MutableClock clock = new MutableClock(Instant.parse("2026-06-19T00:00:00Z"));
        ControllableHttpClient httpClient = new ControllableHttpClient();
        httpClient.enqueue(CONTAINER_A, FIRST_TOKEN, now(clock).plus(SESSION_LIFETIME));
        httpClient.enqueue(CONTAINER_A, "refreshed-a", now(clock).plus(SESSION_LIFETIME.multipliedBy(2)));
        httpClient.enqueue(CONTAINER_B, SECOND_TOKEN, now(clock).plus(SESSION_LIFETIME));
        TokenCredentialSessionProvider provider = createProvider(httpClient, clock);

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
        waitForCallCount(httpClient);
        assertEquals(2, httpClient.getCallCount(CONTAINER_A));

        // Container B has not been touched since the clock advanced, so it must not have refreshed - proving
        // the two containers' caches operate independently rather than sharing one refresh timer.
        assertEquals(1, httpClient.getCallCount(CONTAINER_B));
    }

    /**
     * Guards against over-eager refreshing: while the token is comfortably before its jittered refresh point
     * and no service hint has arrived, repeated requests must reuse the same cached token and never trigger a
     * refresh.
     */
    @Test
    public void noRefreshBeforeJitterWindowWithoutServiceHint() {
        MutableClock clock = new MutableClock(Instant.parse("2026-06-19T00:00:00Z"));
        ControllableHttpClient httpClient = new ControllableHttpClient();
        httpClient.enqueue(CONTAINER_A, FIRST_TOKEN, now(clock).plus(SESSION_LIFETIME));
        TokenCredentialSessionProvider provider = createProvider(httpClient, clock);

        // First request mints the token.
        assertEquals(FIRST_TOKEN, provider.getSession(contextFor(CONTAINER_A)).getSessionToken());

        // Advance only slightly - well before the earliest jittered refresh point (80% of lifetime).
        clock.advance(Duration.ofSeconds(30));

        // Several more requests reuse the same token; no refresh is triggered.
        for (int i = 0; i < 3; i++) {
            assertEquals(FIRST_TOKEN, provider.getSession(contextFor(CONTAINER_A)).getSessionToken());
        }

        assertEquals(1, httpClient.getCallCount(CONTAINER_A));
    }

    /**
     * The async path on a cold cache must mint a value through the async CreateSession call and emit exactly
     * one element before completing.
     */
    @Test
    public void coldCacheCreatesValueAsync() {
        MutableClock clock = new MutableClock(Instant.parse("2026-06-19T00:00:00Z"));
        ControllableHttpClient httpClient = new ControllableHttpClient();
        httpClient.enqueue(CONTAINER_A, FIRST_TOKEN, now(clock).plus(SESSION_LIFETIME));
        TokenCredentialSessionProvider provider = createProvider(httpClient, clock);

        StepVerifier.create(provider.getSessionAsync(contextFor(CONTAINER_A)))
            .assertNext(credential -> assertEquals(FIRST_TOKEN, credential.getSessionToken()))
            .verifyComplete();

        assertEquals(1, httpClient.getCallCount(CONTAINER_A));
    }

    /**
     * Once the async path has cached a usable value, later async requests made before the jittered refresh
     * window must replay that cached value rather than creating a second one.
     */
    @Test
    public void cachedValueIsReusedOnLaterAsyncRequests() {
        MutableClock clock = new MutableClock(Instant.parse("2026-06-19T00:00:00Z"));
        ControllableHttpClient httpClient = new ControllableHttpClient();
        httpClient.enqueue(CONTAINER_A, FIRST_TOKEN, now(clock).plus(SESSION_LIFETIME));
        TokenCredentialSessionProvider provider = createProvider(httpClient, clock);

        StepVerifier.create(provider.getSessionAsync(contextFor(CONTAINER_A)))
            .assertNext(credential -> assertEquals(FIRST_TOKEN, credential.getSessionToken()))
            .verifyComplete();

        // Advance well short of the earliest jittered refresh point (80% of lifetime).
        clock.advance(Duration.ofSeconds(30));

        StepVerifier.create(provider.getSessionAsync(contextFor(CONTAINER_A)))
            .assertNext(credential -> assertEquals(FIRST_TOKEN, credential.getSessionToken()))
            .verifyComplete();

        assertEquals(1, httpClient.getCallCount(CONTAINER_A));
    }

    /**
     * A failed creation must surface to the caller as an error signal rather than an empty completion, and it
     * must not poison the cache: the in-flight creation is cleared so a later request can retry successfully.
     */
    @Test
    public void creationFailurePropagatesAndAllowsRetryAsync() {
        MutableClock clock = new MutableClock(Instant.parse("2026-06-19T00:00:00Z"));
        ControllableHttpClient httpClient = new ControllableHttpClient();
        httpClient.enqueueFailure();
        httpClient.enqueue(CONTAINER_A, FIRST_TOKEN, now(clock).plus(SESSION_LIFETIME));
        TokenCredentialSessionProvider provider = createProvider(httpClient, clock);

        StepVerifier.create(provider.getSessionAsync(contextFor(CONTAINER_A))).verifyError();

        // The failure left no cached value behind, so the retry mints a fresh one.
        StepVerifier.create(provider.getSessionAsync(contextFor(CONTAINER_A)))
            .assertNext(credential -> assertEquals(FIRST_TOKEN, credential.getSessionToken()))
            .verifyComplete();

        assertEquals(2, httpClient.getCallCount(CONTAINER_A));
    }

    /**
     * Container names must be matched case-insensitively: a container looked up with different casing must
     * reuse the same cache entry rather than minting a duplicate session.
     */
    @Test
    public void containerNameLookupIsCaseInsensitive() {
        MutableClock clock = new MutableClock(Instant.parse("2026-06-19T00:00:00Z"));
        ControllableHttpClient httpClient = new ControllableHttpClient();
        httpClient.enqueue(CONTAINER_A, FIRST_TOKEN, now(clock).plus(SESSION_LIFETIME));
        TokenCredentialSessionProvider provider = createProvider(httpClient, clock);

        assertEquals(FIRST_TOKEN, provider.getSession(contextFor(CONTAINER_A)).getSessionToken());
        assertEquals(FIRST_TOKEN,
            provider.getSession(contextFor(CONTAINER_A.toUpperCase(Locale.ROOT))).getSessionToken());

        assertEquals(1, httpClient.getCallCount(CONTAINER_A));
    }

    @Test
    public void refreshAndInvalidationAreOwnedByProvider() {
        MutableClock clock = new MutableClock(Instant.parse("2026-06-19T00:00:00Z"));
        ControllableHttpClient httpClient = new ControllableHttpClient();
        httpClient.enqueue(CONTAINER_A, FIRST_TOKEN, now(clock).plus(SESSION_LIFETIME));
        httpClient.enqueue(CONTAINER_A, SECOND_TOKEN, now(clock).plus(SESSION_LIFETIME));
        httpClient.enqueue(CONTAINER_A, "third-session-token", now(clock).plus(SESSION_LIFETIME));
        TokenCredentialSessionProvider provider = createProvider(httpClient, clock);
        SessionRequestContext context = contextFor(CONTAINER_A);

        SessionCredential first = provider.getSession(context);
        provider.refreshSession(context);
        SessionCredential second = waitForToken(() -> provider.getSession(context));

        assertFalse(provider.invalidateSession(context, first));
        assertTrue(provider.invalidateSession(context, second));
        assertFalse(provider.invalidateSession(context, second));
        assertEquals("third-session-token", provider.getSession(context).getSessionToken());
    }

    /**
     * Concurrent async callers arriving while a creation is still in flight must join that single in-flight
     * creation instead of each triggering their own, and all of them must observe the same value.
     */
    @Test
    public void concurrentAsyncRequestsShareASingleInFlightCreation() {
        MutableClock clock = new MutableClock(Instant.parse("2026-06-19T00:00:00Z"));
        ControllableHttpClient httpClient = new ControllableHttpClient();
        // A pending response models a CreateSession call that is still outstanding.
        Sinks.One<HttpResponse> pendingResponse = httpClient.preparePendingResponse(CONTAINER_A);
        TokenCredentialSessionProvider provider = createProvider(httpClient, clock);

        Mono<SessionCredential> first = provider.getSessionAsync(contextFor(CONTAINER_A));
        Mono<SessionCredential> second = provider.getSessionAsync(contextFor(CONTAINER_A));

        AtomicReference<SessionCredential> firstResult = new AtomicReference<>();
        AtomicReference<SessionCredential> secondResult = new AtomicReference<>();
        CountDownLatch firstLatch = new CountDownLatch(1);
        CountDownLatch secondLatch = new CountDownLatch(1);
        first.subscribe(cred -> {
            firstResult.set(cred);
            firstLatch.countDown();
        });
        second.subscribe(cred -> {
            secondResult.set(cred);
            secondLatch.countDown();
        });

        // Only one CreateSession call was made even though two callers subscribed.
        assertEquals(1, httpClient.getCallCount(CONTAINER_A));

        pendingResponse.tryEmitValue(httpClient.buildResponseFor(now(clock).plus(SESSION_LIFETIME)));

        awaitLatch(firstLatch);
        awaitLatch(secondLatch);

        assertEquals(FIRST_TOKEN, firstResult.get().getSessionToken());
        assertEquals(FIRST_TOKEN, secondResult.get().getSessionToken());
        assertEquals(1, httpClient.getCallCount(CONTAINER_A));
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
     * off a background refresh subscription) until it observes {@code expectedToken} or a timeout elapses.
     * Background refreshes complete on a separate subscription from the caller that triggered them, so
     * asserting on the very next call without allowing for that latency would be flaky.
     */
    private static SessionCredential waitForToken(Supplier<SessionCredential> supplier) {
        long deadline = System.nanoTime() + Duration.ofSeconds(5).toNanos();
        SessionCredential last;
        do {
            last = supplier.get();
            if (TokenCredentialSessionProviderCacheTest.SECOND_TOKEN.equals(last.getSessionToken())) {
                return last;
            }
            sleepBriefly();
        } while (System.nanoTime() < deadline);
        return last;
    }

    private static void waitForCallCount(ControllableHttpClient httpClient) {
        long deadline = System.nanoTime() + Duration.ofSeconds(5).toNanos();
        while (httpClient.getCallCount(TokenCredentialSessionProviderCacheTest.CONTAINER_A) < 2
            && System.nanoTime() < deadline) {
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

    private static TokenCredentialSessionProvider createProvider(HttpClient httpClient, Clock clock) {
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(httpClient).build();
        return new TokenCredentialSessionProvider(pipeline, "https://" + ACCOUNT_NAME + ".blob.core.windows.net",
            BlobServiceVersion.getLatest(), ACCOUNT_NAME, clock);
    }

    private static SessionRequestContext contextFor(String containerName) {
        return new SessionRequestContext().setContainerName(containerName).setAccountName(ACCOUNT_NAME);
    }

    private static OffsetDateTime now(Clock clock) {
        return OffsetDateTime.now(clock);
    }

    /**
     * A fake transport that parses the container name out of the CreateSession request's path and returns
     * pre-configured (token, expiration) pairs in FIFO order for that container, so cache timing behavior can
     * be tested deterministically without a real service. Expirations are supplied by the test relative to
     * the {@link MutableClock} under test, so the cache's expiry/refresh math lines up with the injected
     * clock rather than the real one.
     */
    private static final class ControllableHttpClient implements HttpClient {
        private final Map<String, Deque<CredentialConfig>> queuedByContainer = new ConcurrentHashMap<>();
        private final Map<String, Integer> callCountByContainer = new ConcurrentHashMap<>();
        private final Map<String, Sinks.One<HttpResponse>> pendingByContainer = new ConcurrentHashMap<>();
        private final Map<String, HttpRequest> lastRequestByContainer = new ConcurrentHashMap<>();

        void enqueue(String container, String token, OffsetDateTime expiresAt) {
            queuedByContainer.computeIfAbsent(container, k -> new ArrayDeque<>())
                .add(new CredentialConfig(token, expiresAt, false));
        }

        void enqueueFailure() {
            queuedByContainer
                .computeIfAbsent(TokenCredentialSessionProviderCacheTest.CONTAINER_A, k -> new ArrayDeque<>())
                .add(new CredentialConfig(null, null, true));
        }

        /**
         * Registers a pending (not-yet-completed) response for the given container: the next request for
         * that container will receive this response only once the returned sink is completed, modeling a
         * CreateSession call that is still in flight.
         */
        Sinks.One<HttpResponse> preparePendingResponse(String container) {
            Sinks.One<HttpResponse> sink = Sinks.one();
            pendingByContainer.put(container, sink);
            return sink;
        }

        /**
         * Builds a CreateSession success response for the most recent pending request captured for the
         * given container, for use with {@link #preparePendingResponse(String)}.
         */
        HttpResponse buildResponseFor(OffsetDateTime expiresAt) {
            HttpRequest request = lastRequestByContainer.get(TokenCredentialSessionProviderCacheTest.CONTAINER_A);
            return buildResponse(request, TokenCredentialSessionProviderCacheTest.FIRST_TOKEN, expiresAt);
        }

        int getCallCount(String container) {
            return callCountByContainer.getOrDefault(container, 0);
        }

        @Override
        public Mono<HttpResponse> send(HttpRequest request) {
            String path = request.getUrl().getPath();
            String container = path.startsWith("/") ? path.substring(1) : path;

            callCountByContainer.merge(container, 1, Integer::sum);

            Sinks.One<HttpResponse> pending = pendingByContainer.remove(container);
            if (pending != null) {
                lastRequestByContainer.put(container, request);
                return pending.asMono();
            }

            Deque<CredentialConfig> queue = queuedByContainer.get(container);
            CredentialConfig config = queue == null || queue.isEmpty() ? null : queue.poll();
            if (config == null) {
                return Mono.error(new IllegalStateException("No queued CreateSession response for " + container));
            }
            if (config.failure) {
                return Mono.error(new IllegalStateException("CreateSession failed."));
            }

            return Mono.just(buildResponse(request, config.token, config.expiresAt));
        }

        private static HttpResponse buildResponse(HttpRequest request, String token, OffsetDateTime expiresAt) {
            String expiration = new DateTimeRfc1123(expiresAt).toString();
            String body = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" + "<CreateSessionResult>"
                + "<Id>test-session-id</Id>" + "<Expiration>" + expiration + "</Expiration>"
                + "<AuthenticationType>HMAC</AuthenticationType>" + "<Credentials>" + "<SessionToken>" + token
                + "</SessionToken>" + "<SessionKey>dGVzdFNlc3Npb25LZXkxMjM0NTY3ODkwMTIzNDU2Nzg5MA==</SessionKey>"
                + "</Credentials>" + "</CreateSessionResult>";

            return new MockHttpResponse(request, 201, body.getBytes(StandardCharsets.UTF_8)).addHeader("Content-Type",
                "application/xml");
        }

        private static final class CredentialConfig {
            private final String token;
            private final OffsetDateTime expiresAt;
            private final boolean failure;

            private CredentialConfig(String token, OffsetDateTime expiresAt, boolean failure) {
                this.token = token;
                this.expiresAt = expiresAt;
                this.failure = failure;
            }
        }
    }

    /**
     * A {@link Clock} whose instant can be advanced, allowing deterministic control of the cache's notion
     * of "now" without sleeping.
     */
    private static final class MutableClock extends Clock {
        private final ZoneId zone;
        private Instant instant;

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
            return instant;
        }

        void advance(Duration duration) {
            instant = instant.plus(duration);
        }
    }
}
