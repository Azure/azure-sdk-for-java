// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.util;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpPipelineNextSyncPolicy;
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
import com.azure.core.util.Context;
import com.azure.core.util.DateTimeRfc1123;
import com.azure.storage.blob.BlobTestBase;
import com.azure.storage.blob.BlobServiceAsyncClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.BlobServiceVersion;
import com.azure.storage.blob.BlobUrlParts;
import com.azure.storage.blob.SessionContext;
import com.azure.storage.blob.implementation.accesshelpers.SessionContextAccessor;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.SessionContext.SessionMode;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.policy.StorageBearerTokenChallengeAuthorizationPolicy;
import com.azure.storage.common.policy.RequestRetryOptions;
import com.azure.storage.common.policy.RequestRetryPolicy;
import com.azure.storage.common.policy.RetryPolicyType;
import com.azure.storage.common.test.shared.http.WireTapHttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SessionTests {

    private static final String FIRST_TOKEN = "first-session-token";
    private static final String ENDPOINT = "https://account.blob.core.windows.net";
    private static final HttpHeaderName CONSUMER_HEADER = HttpHeaderName.fromString("x-test-consumer");

    private SessionProvider sessionProvider;
    private StorageBearerTokenChallengeAuthorizationPolicy bearerPolicy;
    private SessionAuthenticationPolicy policy;

    @BeforeEach
    public void beforeEach(TestInfo testInfo) {
        if (testInfo.getTestClass().orElse(null) != SessionTests.class) {
            return;
        }
        sessionProvider = mock(SessionProvider.class);
        bearerPolicy = mock(StorageBearerTokenChallengeAuthorizationPolicy.class);
        when(sessionProvider.getEligibleRequestParts(any()))
            .thenReturn(BlobUrlParts.parse("https://testaccount.blob.core.windows.net/mycontainer/myblob"));

        // Default mock behavior: bearer policy delegates to next policy in the pipeline.
        when(bearerPolicy.process(any(), any())).thenAnswer(invocation -> {
            HttpPipelineNextPolicy nextPolicy = invocation.getArgument(1);
            return nextPolicy.process();
        });
        when(bearerPolicy.processSync(any(), any())).thenAnswer(invocation -> {
            HttpPipelineNextSyncPolicy nextPolicy = invocation.getArgument(1);
            return nextPolicy.processSync();
        });

        policy = createPolicy();
    }

    @Test
    public void sessionPolicyIsPackagePrivate() {
        assertEquals(0, SessionAuthenticationPolicy.class.getModifiers()
            & (Modifier.PUBLIC | Modifier.PROTECTED | Modifier.PRIVATE));
        assertEquals(0, SessionAuthenticationPolicy.class.getConstructors().length);
    }

    @Test
    public void builderHelperRecognizesSessionPolicy() {
        assertTrue(BuilderHelper.isSessionAuthenticationPolicy(policy));
        assertFalse(BuilderHelper.isSessionAuthenticationPolicy(bearerPolicy));
    }

    @ParameterizedTest
    @EnumSource(value = SessionMode.class, names = { "DISABLED", "ENABLED" })
    public void policyRespectsSessionModeAsync(SessionMode mode) {
        policy = new SessionAuthenticationPolicy(bearerPolicy, sessionProvider, testAcquirer(), mode);
        boolean sessionsEnabled = mode == SessionMode.ENABLED;
        if (sessionsEnabled) {
            when(sessionProvider.getSessionAsync(any())).thenReturn(Mono.just(credentialWithToken()));
        }

        StepVerifier.create(buildPipeline(successTransport()).send(blobGetRequest()))
            .assertNext(response -> assertEquals(200, response.getStatusCode()))
            .verifyComplete();

        verify(sessionProvider, times(sessionsEnabled ? 1 : 0)).getEligibleRequestParts(any());
        verify(sessionProvider, times(sessionsEnabled ? 1 : 0)).getSessionAsync(any());
        verify(bearerPolicy, times(sessionsEnabled ? 0 : 1)).process(any(), any());
    }

    @ParameterizedTest
    @EnumSource(value = SessionMode.class, names = { "DISABLED", "ENABLED" })
    public void policyRespectsSessionModeSync(SessionMode mode) {
        policy = new SessionAuthenticationPolicy(bearerPolicy, sessionProvider, testAcquirer(), mode);
        boolean sessionsEnabled = mode == SessionMode.ENABLED;
        if (sessionsEnabled) {
            when(sessionProvider.getSession(any())).thenReturn(credentialWithToken());
        }
        HttpPipelineNextSyncPolicy next = mock(HttpPipelineNextSyncPolicy.class);
        when(next.processSync()).thenReturn(new MockHttpResponse(null, 200));
        when(next.clone()).thenReturn(next);

        try (HttpResponse response = policy.processSync(createContext(), next)) {
            assertEquals(200, response.getStatusCode());
        }

        verify(sessionProvider, times(sessionsEnabled ? 1 : 0)).getEligibleRequestParts(any());
        verify(sessionProvider, times(sessionsEnabled ? 1 : 0)).getSession(any());
        verify(bearerPolicy, times(sessionsEnabled ? 0 : 1)).processSync(any(), any());
    }

    @Test
    public void policyPassesAcquirerWithoutResolvingAccountNameAsync() {
        SessionAcquirer acquirer = testAcquirer();
        policy = new SessionAuthenticationPolicy(bearerPolicy, sessionProvider, acquirer, SessionMode.ENABLED);
        when(sessionProvider.getSessionAsync(any())).thenReturn(Mono.just(credentialWithToken()));

        StepVerifier.create(buildPipeline(successTransport()).send(blobGetRequest())).assertNext(response -> {
            assertEquals(200, response.getStatusCode());
            response.close();
        }).verifyComplete();

        verify(sessionProvider).getSessionAsync(argThat(context -> context.getAcquirer() == acquirer));
        verify(bearerPolicy, never()).process(any(), any());
    }

    @Test
    public void policyPassesAcquirerWithoutResolvingAccountNameSync() {
        SessionAcquirer acquirer = testAcquirer();
        policy = new SessionAuthenticationPolicy(bearerPolicy, sessionProvider, acquirer, SessionMode.ENABLED);
        when(sessionProvider.getSession(any())).thenReturn(credentialWithToken());
        HttpPipelineNextSyncPolicy next = mock(HttpPipelineNextSyncPolicy.class);
        when(next.processSync()).thenReturn(new MockHttpResponse(null, 200));
        when(next.clone()).thenReturn(next);

        try (HttpResponse response = policy.processSync(createContext(), next)) {
            assertEquals(200, response.getStatusCode());
        }

        verify(sessionProvider).getSession(argThat(context -> context.getAcquirer() == acquirer));
        verify(bearerPolicy, never()).processSync(any(), any());
    }

    @ParameterizedTest
    @ValueSource(ints = { 400, 401, 403, 404, 429, 500, 503, 599, 600 })
    public void uncachedAcquisitionFailureFallsBackForCurrentRequestAsync(int statusCode) {
        BlobStorageException serverFailure
            = new BlobStorageException("CreateSession failed.", new MockHttpResponse(null, statusCode), null);
        when(sessionProvider.getSessionAsync(any())).thenReturn(Mono.error(serverFailure));

        HttpClient transport = successTransport();
        HttpPipeline pipeline = buildPipeline(transport);

        StepVerifier.create(pipeline.send(blobGetRequest()))
            .assertNext(r -> assertEquals(200, r.getStatusCode()))
            .verifyComplete();
        StepVerifier.create(pipeline.send(blobGetRequest()))
            .assertNext(r -> assertEquals(200, r.getStatusCode()))
            .verifyComplete();

        verify(sessionProvider, times(2)).getSessionAsync(any());
        verify(bearerPolicy, times(2)).process(any(), any());
    }

    @ParameterizedTest
    @ValueSource(ints = { 400, 401, 403, 404, 429, 500, 503, 599, 600 })
    public void uncachedAcquisitionFailureFallsBackForCurrentRequestSync(int statusCode) {
        BlobStorageException failure
            = new BlobStorageException("CreateSession failed.", new MockHttpResponse(null, statusCode), null);
        when(sessionProvider.getSession(any())).thenThrow(failure);
        HttpPipelineNextSyncPolicy next = mock(HttpPipelineNextSyncPolicy.class);
        when(next.processSync()).thenAnswer(invocation -> new MockHttpResponse(null, 200));

        for (int i = 0; i < 2; i++) {
            try (HttpResponse response = policy.processSync(createContext(), next)) {
                assertEquals(200, response.getStatusCode());
            }
        }

        verify(sessionProvider, times(2)).getSession(any());
        verify(bearerPolicy, times(2)).processSync(any(), any());
    }

    @Test
    public void policyDoesNotCacheFeatureNotEnabledFailures() {
        HttpHeaders headers
            = new HttpHeaders().set(Constants.HeaderConstants.ERROR_CODE_HEADER_NAME, "FeatureNotEnabled");
        BlobStorageException failure
            = new BlobStorageException("CreateSession failed.", new MockHttpResponse(null, 400, headers), null);
        when(sessionProvider.getSessionAsync(any())).thenReturn(Mono.error(failure));

        HttpPipeline pipeline = buildPipeline(successTransport());
        for (int i = 0; i < 2; i++) {
            StepVerifier.create(pipeline.send(blobGetRequest()))
                .assertNext(r -> assertEquals(200, r.getStatusCode()))
                .verifyComplete();
        }

        verify(sessionProvider, times(2)).getSessionAsync(any());
        verify(bearerPolicy, times(2)).process(any(), any());
    }

    @Test
    public void providerFallbackIsRespectedAsync() {
        when(sessionProvider.getSessionAsync(any())).thenReturn(Mono.empty())
            .thenReturn(Mono.empty())
            .thenReturn(Mono.just(credentialWithToken()));
        HttpPipeline pipeline = buildPipeline(successTransport());

        StepVerifier.create(pipeline.send(blobGetRequest("testaccount", "container-a")))
            .assertNext(response -> assertEquals(200, response.getStatusCode()))
            .verifyComplete();
        StepVerifier.create(pipeline.send(blobGetRequest("testaccount", "container-a")))
            .assertNext(response -> assertEquals(200, response.getStatusCode()))
            .verifyComplete();

        HttpRequest otherContainerRequest = blobGetRequest("testaccount", "container-b");
        StepVerifier.create(pipeline.send(otherContainerRequest))
            .assertNext(response -> assertEquals(200, response.getStatusCode()))
            .verifyComplete();

        assertTrue(isSessionAuthenticated(otherContainerRequest));
        verify(sessionProvider, times(3)).getSessionAsync(any());
        verify(bearerPolicy, times(2)).process(any(), any());
    }

    @Test
    public void providerFallbackIsRespectedSync() {
        when(sessionProvider.getSession(any())).thenReturn(null, null, credentialWithToken());
        HttpPipelineNextSyncPolicy next = mock(HttpPipelineNextSyncPolicy.class);
        when(next.processSync()).thenAnswer(invocation -> new MockHttpResponse(null, 200));

        sendSessionResponseSync(blobGetRequest("testaccount", "container-a"), 200, next);
        sendSessionResponseSync(blobGetRequest("testaccount", "container-a"), 200, next);
        HttpRequest otherContainerRequest = blobGetRequest("testaccount", "container-b");
        sendSessionResponseSync(otherContainerRequest, 200, next);

        assertTrue(isSessionAuthenticated(otherContainerRequest));
        verify(sessionProvider, times(3)).getSession(any());
        verify(bearerPolicy, times(2)).processSync(any(), any());
    }

    @Test
    public void policySignsRequestWithSessionCredential() {
        HttpRequest request = blobGetRequest();
        HttpClient transport = successTransport();
        when(sessionProvider.getSessionAsync(any())).thenReturn(Mono.just(credentialWithToken()));

        StepVerifier.create(buildPipeline(transport).send(request))
            .assertNext(r -> assertEquals(200, r.getStatusCode()))
            .verifyComplete();

        assertTrue(request.getHeaders().getValue(HttpHeaderName.AUTHORIZATION).startsWith("Session " + FIRST_TOKEN),
            "Expected request to be signed with a session credential.");
    }

    /**
     * Verifies that a 401 from the service invalidates the cached session and retries the request
     * using bearer authentication. No WWW-Authenticate header is required to trigger this fallback;
     * any 401 from a session-authenticated request unconditionally falls back to bearer.
     */
    @Test
    public void policyInvalidatesSessionAndFallsBackToBearerAsync() {
        HttpRequest request = blobGetRequest();
        String date = "Thu, 24 Sep 2026 00:00:00 GMT";
        request.setHeader(HttpHeaderName.DATE, date);
        WireTapHttpClient transport = bearerFallbackTransport(401);

        when(sessionProvider.getSessionAsync(any())).thenReturn(Mono.just(credentialWithToken()));
        doAnswer(invocation -> {
            assertNull(request.getHeaders().getValue(HttpHeaderName.AUTHORIZATION));
            assertNull(request.getHeaders().getValue(HttpHeaderName.fromString("x-ms-date")));
            assertEquals(date, request.getHeaders().getValue(HttpHeaderName.DATE));
            HttpPipelineNextPolicy next = invocation.getArgument(1);
            return next.process();
        }).when(bearerPolicy).process(any(), any());

        StepVerifier.create(buildPipeline(transport).send(request))
            .assertNext(r -> assertEquals(200, r.getStatusCode()))
            .verifyComplete();

        // Session auth was stripped before the bearer retry.
        assertNull(request.getHeaders().getValue(HttpHeaderName.AUTHORIZATION));
        // Transport received two dispatches: one for session auth, one for bearer retry.
        assertEquals(2, transport.getRequestCount());
        verify(sessionProvider, times(1)).getSessionAsync(any());
        verify(sessionProvider, times(1)).invalidateSession(any(), any());
        verify(bearerPolicy, times(1)).process(any(), any());
    }

    /**
     * Each rejected session falls back to bearer for that request. Repeated rejections do not suppress
     * session acquisition on subsequent requests.
     */
    @Test
    public void repeatedSessionRejectionsDoNotStartCooldownAsync() {
        when(sessionProvider.getSessionAsync(any())).thenReturn(Mono.just(credentialWithToken()));

        WireTapHttpClient transport = bearerFallbackTransport(401);
        HttpPipeline pipeline = buildPipeline(transport);

        for (int i = 0; i < 4; i++) {
            StepVerifier.create(pipeline.send(blobGetRequest()))
                .assertNext(r -> assertEquals(200, r.getStatusCode()))
                .verifyComplete();
        }

        verify(sessionProvider, times(4)).getSessionAsync(any());
        assertEquals(8, transport.getRequestCount());
    }

    @Test
    public void sessionAcquisitionContinuesAcrossAcceptedAndRejectedResponses() {
        when(sessionProvider.getSessionAsync(any())).thenReturn(Mono.just(credentialWithToken()));

        WireTapHttpClient transport = sessionRejectionTransportWithAcceptedSecondRequest();
        HttpPipeline pipeline = buildPipeline(transport);

        for (int i = 0; i < 5; i++) {
            StepVerifier.create(pipeline.send(blobGetRequest()))
                .assertNext(r -> assertEquals(200, r.getStatusCode()))
                .verifyComplete();
        }

        // Every request attempts session authentication, regardless of the preceding response.
        verify(sessionProvider, times(5)).getSessionAsync(any());
    }

    @Test
    public void sessionRejectionsDoNotSuppressAcquisitionAfterTimeAdvances() {
        MutableClock clock = new MutableClock(Instant.parse("2026-06-19T00:00:00Z"));
        policy = createPolicy(clock);
        when(sessionProvider.getSessionAsync(any())).thenReturn(Mono.just(credentialWithToken()));

        WireTapHttpClient transport = bearerFallbackTransport(401);
        HttpPipeline pipeline = buildPipeline(transport);

        for (int i = 0; i < 4; i++) {
            StepVerifier.create(pipeline.send(blobGetRequest()))
                .assertNext(r -> assertEquals(200, r.getStatusCode()))
                .verifyComplete();
        }
        verify(sessionProvider, times(4)).getSessionAsync(any());

        clock.advance(Duration.ofMinutes(5));

        StepVerifier.create(pipeline.send(blobGetRequest()))
            .assertNext(r -> assertEquals(200, r.getStatusCode()))
            .verifyComplete();

        verify(sessionProvider, times(5)).getSessionAsync(any());
    }

    @ParameterizedTest
    @ValueSource(ints = { 400, 403, 404, 409, 429, 499, 500, 503, 599, 600 })
    public void policyReturnsNonFallbackStatusWithoutRetryAsync(int statusCode) {
        HttpRequest request = blobGetRequest();
        WireTapHttpClient transport = new WireTapHttpClient(statusCodeTransport(statusCode));
        when(sessionProvider.getSessionAsync(any())).thenReturn(Mono.just(credentialWithToken()));

        StepVerifier.create(buildPipeline(transport).send(request))
            .assertNext(r -> assertEquals(statusCode, r.getStatusCode()))
            .verifyComplete();

        assertEquals(1, transport.getRequestCount());
        verify(bearerPolicy, times(0)).process(any(), any());
    }

    @ParameterizedTest
    @ValueSource(ints = { 401 })
    public void policyReturnsBearerFailureWithoutFurtherFallbackAsync(int statusCode) {
        HttpRequest request = blobGetRequest();
        WireTapHttpClient transport = new WireTapHttpClient(statusCodeTransport(statusCode));
        when(sessionProvider.getSessionAsync(any())).thenReturn(Mono.just(credentialWithToken()));

        StepVerifier.create(buildPipeline(transport).send(request))
            .assertNext(r -> assertEquals(statusCode, r.getStatusCode()))
            .verifyComplete();

        assertEquals(2, transport.getRequestCount());
        assertNull(request.getHeaders().getValue(HttpHeaderName.AUTHORIZATION));
        verify(bearerPolicy, times(1)).process(any(), any());
    }

    @Test
    public void sessionExpiringHintRequestsProviderRefresh() {
        HttpRequest request = blobGetRequest();
        HttpHeaders responseHeaders
            = new HttpHeaders().set(HttpHeaderName.fromString("x-ms-auth-info"), "session_expiring");
        HttpClient transport = responseTransport(200, responseHeaders);
        when(sessionProvider.getSessionAsync(any())).thenReturn(Mono.just(credentialWithToken()));

        StepVerifier.create(buildPipeline(transport).send(request))
            .assertNext(r -> assertEquals(200, r.getStatusCode()))
            .verifyComplete();

        // The policy delegates the hint to refreshSession; the provider owns refresh timing and backoff.
        verify(sessionProvider, times(1)).getSessionAsync(any());
        verify(sessionProvider, times(1)).refreshSession(any());
    }

    @Test
    public void noSessionExpiringHintDoesNotForceBackgroundRefresh() {
        HttpRequest request = blobGetRequest();
        HttpClient transport = successTransport();
        when(sessionProvider.getSessionAsync(any())).thenReturn(Mono.just(credentialWithToken()));

        StepVerifier.create(buildPipeline(transport).send(request))
            .assertNext(r -> assertEquals(200, r.getStatusCode()))
            .verifyComplete();

        // Without the hint, the policy does not explicitly request a refresh from the provider.
        verify(sessionProvider, times(1)).getSessionAsync(any());
        verify(sessionProvider, never()).refreshSession(any());
    }

    @Test
    public void getBlobRequestProducesWellFormedSessionAuthHeader() {
        SessionCredential cred = credentialWithToken();
        HttpRequest request
            = new HttpRequest(HttpMethod.GET, "https://testaccount.blob.core.windows.net/mycontainer/myblob");
        request.getHeaders()
            .set(HttpHeaderName.fromString("x-ms-version"), "2025-01-05")
            .set(HttpHeaderName.fromString("x-ms-client-request-id"), "11111111-2222-3333-4444-555555555555")
            .set(HttpHeaderName.RANGE, "bytes=0-1023");

        HttpClient transport = successTransport();
        when(sessionProvider.getSessionAsync(any())).thenReturn(Mono.just(cred));

        StepVerifier.create(buildPipeline(transport).send(request))
            .assertNext(r -> assertEquals(200, r.getStatusCode()))
            .verifyComplete();

        // The policy adapts Shared Key signing to the Session authorization scheme.
        String actual = request.getHeaders().getValue(HttpHeaderName.AUTHORIZATION);
        assertNotNull(actual, "Authorization header should be set by the policy");
        assertTrue(actual.startsWith("Session " + FIRST_TOKEN + ":"),
            "Authorization should use the Session scheme with the cached session token.");
        String actualSignature = actual.substring(actual.indexOf(':') + 1);
        assertTrue(actualSignature.matches("[A-Za-z0-9+/]+={0,2}"), "Signature must be base64-encoded.");
    }

    @Test
    public void policyRefreshesExistingSigningDateAsync() {
        HttpRequest request = blobGetRequest();
        HttpHeaderName dateHeader = HttpHeaderName.fromString("x-ms-date");
        String staleDate = "Thu, 24 Sep 2026 00:00:00 GMT";
        request.setHeader(dateHeader, staleDate);
        when(sessionProvider.getSessionAsync(any())).thenReturn(Mono.just(credentialWithToken()));

        StepVerifier.create(buildPipeline(successTransport()).send(request)).assertNext(response -> {
            assertEquals(200, response.getStatusCode());
            response.close();
        }).verifyComplete();

        assertNotNull(request.getHeaders().getValue(dateHeader));
        assertNotEquals(staleDate, request.getHeaders().getValue(dateHeader));
        assertTrue(isSessionAuthenticated(request));
    }

    // Sync tests invoke the policy directly with a mock next-policy.

    @Test
    public void policyRefreshesExistingSigningDateSync() {
        HttpRequest request = blobGetRequest();
        HttpHeaderName dateHeader = HttpHeaderName.fromString("x-ms-date");
        String staleDate = "Thu, 24 Sep 2026 00:00:00 GMT";
        request.setHeader(dateHeader, staleDate);
        when(sessionProvider.getSession(any())).thenReturn(credentialWithToken());

        sendSessionResponseSync(request, 200);

        assertNotNull(request.getHeaders().getValue(dateHeader));
        assertNotEquals(staleDate, request.getHeaders().getValue(dateHeader));
        assertTrue(isSessionAuthenticated(request));
    }

    @Test
    public void policyInvalidatesSessionAndFallsBackToBearerSync() {
        HttpPipelineCallContext context = createContext();
        String date = "Thu, 24 Sep 2026 00:00:00 GMT";
        context.getHttpRequest().setHeader(HttpHeaderName.DATE, date);
        HttpPipelineNextSyncPolicy next = mock(HttpPipelineNextSyncPolicy.class);
        HttpPipelineNextSyncPolicy retryNext = mock(HttpPipelineNextSyncPolicy.class);
        HttpResponse initialResponse = mock(HttpResponse.class);
        HttpResponse retriedResponse = mock(HttpResponse.class);

        when(sessionProvider.getSession(any())).thenReturn(credentialWithToken());
        when(next.clone()).thenReturn(retryNext);
        when(next.processSync()).thenReturn(initialResponse);
        when(retryNext.processSync()).thenReturn(retriedResponse);
        when(initialResponse.getStatusCode()).thenReturn(401);
        when(retriedResponse.getStatusCode()).thenReturn(200);
        doAnswer(invocation -> {
            assertNull(context.getHttpRequest().getHeaders().getValue(HttpHeaderName.AUTHORIZATION));
            assertNull(context.getHttpRequest().getHeaders().getValue(HttpHeaderName.fromString("x-ms-date")));
            assertEquals(date, context.getHttpRequest().getHeaders().getValue(HttpHeaderName.DATE));
            HttpPipelineNextSyncPolicy bearerNext = invocation.getArgument(1);
            return bearerNext.processSync();
        }).when(bearerPolicy).processSync(any(), any());

        try (HttpResponse actualResponse = policy.processSync(context, next)) {
            assertEquals(retriedResponse, actualResponse);
            assertNull(context.getHttpRequest().getHeaders().getValue(HttpHeaderName.AUTHORIZATION));
            verify(initialResponse, times(1)).close();
            verify(next, times(1)).processSync();
            verify(retryNext, times(1)).processSync();
            verify(sessionProvider, times(1)).invalidateSession(any(), any());
        }
    }

    @ParameterizedTest
    @ValueSource(ints = { 400, 403, 404, 409, 429, 499, 500, 503, 599, 600 })
    public void policyReturnsNonFallbackStatusWithoutRetrySync(int statusCode) {
        HttpPipelineCallContext context = createContext();
        HttpPipelineNextSyncPolicy next = mock(HttpPipelineNextSyncPolicy.class);
        HttpPipelineNextSyncPolicy retryNext = mock(HttpPipelineNextSyncPolicy.class);
        HttpResponse unavailableResponse = mock(HttpResponse.class);

        when(sessionProvider.getSession(any())).thenReturn(credentialWithToken());
        when(next.clone()).thenReturn(retryNext);
        when(next.processSync()).thenReturn(unavailableResponse);
        when(unavailableResponse.getStatusCode()).thenReturn(statusCode);

        try (HttpResponse actualResponse = policy.processSync(context, next)) {
            assertEquals(unavailableResponse, actualResponse);
            verify(unavailableResponse, times(0)).close();
            verify(bearerPolicy, times(0)).processSync(any(), any());
            verify(retryNext, times(0)).processSync();
        }
    }

    @Test
    public void repeatedSessionRejectionsDoNotStartCooldownSync() {
        when(sessionProvider.getSession(any())).thenReturn(credentialWithToken());

        for (int i = 0; i < 4; i++) {
            HttpPipelineCallContext context = createContext();
            HttpPipelineNextSyncPolicy next = mock(HttpPipelineNextSyncPolicy.class);
            HttpPipelineNextSyncPolicy retryNext = mock(HttpPipelineNextSyncPolicy.class);
            HttpResponse rejectedResponse = mock(HttpResponse.class);
            HttpResponse bearerResponse = mock(HttpResponse.class);

            when(next.clone()).thenReturn(retryNext);
            when(next.processSync()).thenReturn(rejectedResponse);
            when(retryNext.processSync()).thenReturn(bearerResponse);
            when(rejectedResponse.getStatusCode()).thenReturn(401);
            when(bearerResponse.getStatusCode()).thenReturn(200);

            policy.processSync(context, next).close();
        }

        verify(sessionProvider, times(4)).getSession(any());
    }

    @Test
    public void syncPolicyUsesGetSessionOverride() {
        when(sessionProvider.getSession(any())).thenReturn(credentialWithToken());
        HttpRequest request = blobGetRequest();

        sendSessionResponseSync(request, 200);

        assertTrue(isSessionAuthenticated(request));
        verify(sessionProvider)
            .getSession(argThat(context -> "testaccount".equals(context.getAcquirer().getAccountName())
                && "mycontainer".equals(context.getContainerName())));
        verify(sessionProvider, never()).getSessionAsync(any());
        verify(bearerPolicy, never()).processSync(any(), any());
    }

    @Test
    public void syncPolicyUsesDefaultGetSession() {
        when(sessionProvider.getSession(any())).thenCallRealMethod();
        when(sessionProvider.getSessionAsync(any())).thenReturn(Mono.just(credentialWithToken()));
        HttpRequest request = blobGetRequest();

        sendSessionResponseSync(request, 200);

        assertTrue(isSessionAuthenticated(request));
        verify(sessionProvider).getSession(any());
        verify(sessionProvider).getSessionAsync(any());
        verify(bearerPolicy, never()).processSync(any(), any());
    }

    @Test
    public void syncPolicyFallsBackWhenDefaultGetSessionFails() {
        when(sessionProvider.getSession(any())).thenCallRealMethod();
        when(sessionProvider.getSessionAsync(any()))
            .thenReturn(Mono.error(new IllegalStateException("Session acquisition failed.")));

        sendSessionResponseSync(blobGetRequest(), 200);

        verify(sessionProvider).getSession(any());
        verify(sessionProvider).getSessionAsync(any());
        verify(bearerPolicy).processSync(any(), any());
    }

    @Test
    public void asyncPolicyUsesGetSessionAsync() {
        when(sessionProvider.getSessionAsync(any())).thenReturn(Mono.just(credentialWithToken()));
        HttpRequest request = blobGetRequest();

        StepVerifier.create(buildPipeline(successTransport()).send(request)).assertNext(response -> {
            assertEquals(200, response.getStatusCode());
            response.close();
        }).verifyComplete();

        assertTrue(isSessionAuthenticated(request));
        verify(sessionProvider)
            .getSessionAsync(argThat(context -> "testaccount".equals(context.getAcquirer().getAccountName())
                && "mycontainer".equals(context.getContainerName())));
        verify(sessionProvider, never()).getSession(any());
        verify(bearerPolicy, never()).process(any(), any());
    }

    @Test
    public void asyncPolicyGetsContainerNameFromCustomEndpoint() {
        policy = new SessionAuthenticationPolicy(bearerPolicy, sessionProvider,
            testAcquirer("https://custom.endpoint.example"), SessionMode.ENABLED);
        when(sessionProvider.getSessionAsync(any())).thenReturn(Mono.just(credentialWithToken()));
        HttpRequest request = new HttpRequest(HttpMethod.GET, "https://custom.endpoint.example/mycontainer/myblob");

        StepVerifier.create(buildPipeline(successTransport()).send(request)).assertNext(response -> {
            assertEquals(200, response.getStatusCode());
            response.close();
        }).verifyComplete();

        verify(sessionProvider).getSessionAsync(argThat(context -> "mycontainer".equals(context.getContainerName())));
    }

    @Test
    public void requestsForAnotherEndpointUseBearer() {
        HttpRequest request = blobGetRequest("otheraccount", "mycontainer");

        StepVerifier.create(buildPipeline(successTransport()).send(request)).assertNext(response -> {
            assertEquals(200, response.getStatusCode());
            response.close();
        }).verifyComplete();

        assertFalse(isSessionAuthenticated(request));
        verify(sessionProvider, never()).getSessionAsync(any());
        verify(bearerPolicy).process(any(), any());
    }

    @Test
    public void dfsAcquisitionEndpointMatchesOnlyTheCorrespondingBlobAccount() {
        SessionAcquirer acquirer = testAcquirer("https://testaccount.dfs.core.windows.net/mycontainer?comp=session");

        assertTrue(acquirer.isRequestForSessionEndpoint(blobGetRequest()));
        assertFalse(acquirer.isRequestForSessionEndpoint(blobGetRequest("otheraccount", "mycontainer")));
    }

    @Test
    public void sessionRequestContextCarriesPolicyAcquirer() {
        SessionAcquirer acquirer = testAcquirer();
        policy = new SessionAuthenticationPolicy(bearerPolicy, sessionProvider, acquirer, SessionMode.ENABLED);
        when(sessionProvider.getSessionAsync(any())).thenReturn(Mono.just(credentialWithToken()));

        StepVerifier.create(buildPipeline(successTransport()).send(blobGetRequest())).assertNext(response -> {
            assertEquals(200, response.getStatusCode());
            response.close();
        }).verifyComplete();

        verify(sessionProvider).getSessionAsync(argThat(context -> context.getAcquirer() == acquirer));
    }

    @Test
    public void sessionRequestContextCarriesOriginatingContext() {
        Context originatingContext = new Context("test-key", "test-value");
        when(sessionProvider.getSessionAsync(any())).thenReturn(Mono.just(credentialWithToken()));

        StepVerifier.create(buildPipeline(successTransport()).send(blobGetRequest(), originatingContext))
            .assertNext(response -> {
                assertEquals(200, response.getStatusCode());
                response.close();
            })
            .verifyComplete();

        verify(sessionProvider).getSessionAsync(argThat(context -> context.getContext() == originatingContext));
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    public void acquisitionRequestsUseAcquisitionCredentialOverride(boolean sync) {
        StorageBearerTokenChallengeAuthorizationPolicy acquisitionBearerPolicy
            = mock(StorageBearerTokenChallengeAuthorizationPolicy.class);
        when(acquisitionBearerPolicy.process(any(), any())).thenAnswer(invocation -> {
            HttpPipelineNextPolicy nextPolicy = invocation.getArgument(1);
            return nextPolicy.process();
        });
        when(acquisitionBearerPolicy.processSync(any(), any())).thenAnswer(invocation -> {
            HttpPipelineNextSyncPolicy nextPolicy = invocation.getArgument(1);
            return nextPolicy.processSync();
        });
        policy = new SessionAuthenticationPolicy(bearerPolicy, sessionProvider,
            new SessionAcquirer("https://testaccount.blob.core.windows.net", null, "testaccount",
                acquisitionBearerPolicy),
            SessionMode.ENABLED);
        HttpPipeline pipeline = buildPipeline(successTransport());
        Context acquisition = new Context(SessionAcquirer.SESSION_ACQUISITION_CONTEXT_KEY, true);

        for (Context context : new Context[] { acquisition, Context.NONE }) {
            HttpRequest request = new HttpRequest(HttpMethod.POST,
                "https://testaccount.blob.core.windows.net/mycontainer?restype=container&comp=session");
            when(sessionProvider.getEligibleRequestParts(any())).thenReturn(null);
            if (sync) {
                try (HttpResponse response = pipeline.sendSync(request, context)) {
                    assertEquals(200, response.getStatusCode());
                }
            } else {
                StepVerifier.create(pipeline.send(request, context)).assertNext(response -> {
                    assertEquals(200, response.getStatusCode());
                    response.close();
                }).verifyComplete();
            }
        }

        if (sync) {
            verify(acquisitionBearerPolicy, times(1)).processSync(any(), any());
            verify(bearerPolicy, times(1)).processSync(any(), any());
        } else {
            verify(acquisitionBearerPolicy, times(1)).process(any(), any());
            verify(bearerPolicy, times(1)).process(any(), any());
        }
        verify(sessionProvider, never()).getSessionAsync(any());
        verify(sessionProvider, never()).getSession(any());
    }

    @ParameterizedTest
    @CsvSource({ "true,200", "false,200", "true,401", "false,401" })
    public void sessionRejectionFallsBackOnEachTransportAttempt(boolean sync, int finalStatusCode) {
        SessionCredential credential = credentialWithToken();
        when(sessionProvider.getSession(any())).thenReturn(credential);
        when(sessionProvider.getSessionAsync(any())).thenReturn(Mono.just(credential));
        AtomicInteger sends = new AtomicInteger();
        HttpClient transport = request -> {
            int attempt = sends.incrementAndGet();
            assertEquals(attempt % 2 == 1, isSessionAuthenticated(request));
            int status = attempt % 2 == 1 ? 401 : (attempt == 2 ? 500 : finalStatusCode);
            return Mono.just(new MockHttpResponse(request, status));
        };
        RequestRetryOptions retryOptions = new RequestRetryOptions(RetryPolicyType.FIXED, 2, 30, 1L, 1L, null);
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(transport)
            .policies(new RequestRetryPolicy(retryOptions), policy)
            .build();

        if (sync) {
            try (HttpResponse response = pipeline.sendSync(blobGetRequest(), Context.NONE)) {
                assertEquals(finalStatusCode, response.getStatusCode());
            }
            verify(sessionProvider, times(2)).getSession(any());
            verify(bearerPolicy, times(2)).processSync(any(), any());
        } else {
            StepVerifier.create(pipeline.send(blobGetRequest())).assertNext(response -> {
                assertEquals(finalStatusCode, response.getStatusCode());
                response.close();
            }).verifyComplete();
            verify(sessionProvider, times(2)).getSessionAsync(any());
            verify(bearerPolicy, times(2)).process(any(), any());
        }
        assertEquals(4, sends.get());
        verify(sessionProvider, times(2)).invalidateSession(any(), org.mockito.ArgumentMatchers.same(credential));
    }

    @Nested
    class ContextAndModelTests {
        @Test
        public void defaultOptionsUseAutomaticModeAndPipelineConfiguration() {
            SessionContext options = new SessionContext();

            assertEquals(SessionMode.AUTO, options.getSessionMode());
            assertNull(SessionContextAccessor.getEndpoint(options));
            assertNull(SessionContextAccessor.getCredential(options));
        }

        @Test
        public void constructorStoresSessionAcquisitionConfiguration() {
            MockTokenCredential credential = new MockTokenCredential();
            SessionContext options = new SessionContext(ENDPOINT, credential);

            assertEquals(SessionMode.AUTO, options.getSessionMode());
            assertEquals(ENDPOINT, SessionContextAccessor.getEndpoint(options));
            assertSame(credential, SessionContextAccessor.getCredential(options));
        }

        @ParameterizedTest
        @ValueSource(booleans = { true, false })
        public void sharedOptionsReuseProviderAcrossIndependentClients(boolean customOptions) {
            SessionContext options
                = (customOptions ? new SessionContext(ENDPOINT, new MockTokenCredential()) : new SessionContext())
                    .setSessionMode(SessionMode.ENABLED);
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
                    .setSessionMode(SessionMode.ENABLED);
            serviceClientBuilder().sessionContext(separate).buildClient();
            assertNotSame(provider, existingProvider(separate));
        }

        @Test
        public void concurrentBuildersInitializeOnlyOneProvider() throws Exception {
            SessionContext options = new SessionContext().setSessionMode(SessionMode.ENABLED);
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
                    .setSessionMode(SessionMode.ENABLED);
            HttpClient firstTransport = consumerTransport(sameTransport ? null : "first", customOptions, acquisitions,
                downloads, currentToken, rejectNextSession);
            HttpClient secondTransport = sameTransport
                ? firstTransport
                : consumerTransport("second", customOptions, acquisitions, downloads, currentToken, rejectNextSession);
            HttpLogOptions logOptions = new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS)
                .setRequestLogger((logger, context) -> {
                    if (context.getHttpRequest().getHttpMethod() == HttpMethod.POST) {
                        loggedAcquisitions.incrementAndGet();
                    }
                    return Mono.empty();
                })
                .setResponseLogger((logger, context) -> {
                    if (context.getHttpResponse().getRequest().getHttpMethod() == HttpMethod.POST) {
                        loggedAcquisitions.incrementAndGet();
                    }
                    return Mono.just(context.getHttpResponse());
                });
            BlobServiceClientBuilder first = consumerBuilder(options, firstTransport, "first", logOptions);
            BlobServiceClientBuilder second = consumerBuilder(options, secondTransport, "second", logOptions);

            downloadFromDerivedClients(first, asyncFirst);
            downloadFromDerivedClients(second, !asyncFirst);

            assertEquals(1, acquisitions.get("first").get());
            assertNull(acquisitions.get("second"), "The second client must reuse the shared session.");
            assertEquals(2, downloads.get("first").get(), "Derived clients should reuse the first session.");
            assertEquals(2, downloads.get("second").get(), "Independent clients should reuse the same session.");

            rejectNextSession.set(true);
            downloadFromDerivedClients(second, !asyncFirst);
            assertEquals(1, acquisitions.get("first").get());
            assertEquals(1, acquisitions.get("second").get(),
                "Reacquisition must use the requesting client's pipeline.");
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
            options.setSessionMode(SessionMode.ENABLED);
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
                .sessionContext(new SessionContext().setSessionMode(SessionMode.ENABLED))
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
                .sessionContext(
                    new SessionContext(ENDPOINT, new MockTokenCredential()).setSessionMode(SessionMode.ENABLED))
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
            SessionContext options
                = new SessionContext("http://account.blob.core.windows.net", new MockTokenCredential())
                    .setSessionMode(SessionMode.ENABLED);
            assertThrows(IllegalArgumentException.class,
                () -> serviceClientBuilder().sessionContext(options).buildClient());
        }

        @Test
        public void constructorRejectsNullExpiration() {
            assertThrows(NullPointerException.class, () -> new SessionCredential(BlobTestBase.TEST_SESSION_TOKEN,
                BlobTestBase.TEST_SESSION_KEY, null, BlobTestBase.TEST_SESSION_ACCOUNT_NAME));
        }

        @Test
        public void requestContextRequiresContainerAndAcquirer() {
            SessionAcquirer acquirer
                = new SessionAcquirer("https://testaccount.blob.core.windows.net", null, "testaccount", null);

            assertThrows(IllegalArgumentException.class, () -> new SessionRequestContext(null, acquirer, Context.NONE));
            assertThrows(IllegalArgumentException.class, () -> new SessionRequestContext("", acquirer, Context.NONE));
            assertThrows(NullPointerException.class, () -> new SessionRequestContext("container", null, Context.NONE));
            assertThrows(NullPointerException.class, () -> new SessionRequestContext("container", acquirer, null));

            Context originatingContext = new Context("test-key", "test-value");
            SessionRequestContext context = new SessionRequestContext("container", acquirer, originatingContext);
            assertEquals("container", context.getContainerName());
            assertSame(acquirer, context.getAcquirer());
            assertSame(originatingContext, context.getContext());
        }

        @Test
        public void acquirerRequiresResolvedAccountName() {
            assertThrows(IllegalArgumentException.class,
                () -> new SessionAcquirer("https://testaccount.blob.core.windows.net", null, null, null));
            assertThrows(IllegalArgumentException.class,
                () -> new SessionAcquirer("https://testaccount.blob.core.windows.net", null, "", null));
        }
    }

    // Helpers

    private static SessionProvider existingProvider(SessionContext options) {
        return SessionContextAccessor.getOrCreateSessionProvider(options, SessionProvider.class, () -> {
            throw new AssertionError("The builder must initialize the provider.");
        });
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

    private void sendSessionResponseSync(HttpRequest request, int sessionStatusCode) {
        sendSessionResponseSync(request, sessionStatusCode, mock(HttpPipelineNextSyncPolicy.class));
    }

    private void sendSessionResponseSync(HttpRequest request, int sessionStatusCode, HttpPipelineNextSyncPolicy next) {
        HttpPipelineNextSyncPolicy retryNext = mock(HttpPipelineNextSyncPolicy.class);
        when(next.clone()).thenReturn(retryNext);
        when(next.processSync()).thenAnswer(
            invocation -> new MockHttpResponse(request, isSessionAuthenticated(request) ? sessionStatusCode : 200));
        when(retryNext.processSync()).thenAnswer(invocation -> new MockHttpResponse(request, 200));

        try (HttpResponse response = policy.processSync(createContextForRequest(request), next)) {
            assertEquals(200, response.getStatusCode());
        }
    }

    private HttpPipeline buildPipeline(HttpClient transport) {
        return new HttpPipelineBuilder().httpClient(transport).policies(policy).build();
    }

    private static HttpClient successTransport() {
        return statusCodeTransport(200);
    }

    private static HttpClient statusCodeTransport(int statusCode) {
        return request -> Mono.just(new MockHttpResponse(request, statusCode));
    }

    private static HttpClient responseTransport(int statusCode, HttpHeaders headers) {
        return request -> Mono.just(new MockHttpResponse(request, statusCode, headers));
    }

    private static WireTapHttpClient bearerFallbackTransport(int sessionResponseStatusCode) {
        return new WireTapHttpClient(request -> Mono
            .just(new MockHttpResponse(request, isSessionAuthenticated(request) ? sessionResponseStatusCode : 200)));
    }

    private static WireTapHttpClient sessionRejectionTransportWithAcceptedSecondRequest() {
        AtomicInteger sessionRequestCount = new AtomicInteger();
        return new WireTapHttpClient(request -> Mono.just(new MockHttpResponse(request,
            !isSessionAuthenticated(request) || sessionRequestCount.incrementAndGet() == 2 ? 200 : 401)));
    }

    private static boolean isSessionAuthenticated(HttpRequest request) {
        String authorization = request.getHeaders().getValue(HttpHeaderName.AUTHORIZATION);
        return authorization != null && authorization.startsWith("Session ");
    }

    private static HttpRequest blobGetRequest() {
        return blobGetRequest("testaccount", "mycontainer");
    }

    private static HttpRequest blobGetRequest(String accountName, String containerName) {
        return new HttpRequest(HttpMethod.GET,
            "https://" + accountName + ".blob.core.windows.net/" + containerName + "/myblob");
    }

    private SessionAuthenticationPolicy createPolicy() {
        return createPolicy(Clock.systemUTC());
    }

    private SessionAuthenticationPolicy createPolicy(Clock clock) {
        return new SessionAuthenticationPolicy(bearerPolicy, sessionProvider, testAcquirer(), SessionMode.ENABLED,
            clock);
    }

    private static SessionAcquirer testAcquirer() {
        return testAcquirer("https://testaccount.blob.core.windows.net");
    }

    private static SessionAcquirer testAcquirer(String endpoint) {
        return new SessionAcquirer(endpoint, null, "testaccount", null);
    }

    private static SessionCredential credentialWithToken() {
        return credentialWithToken(OffsetDateTime.now().plusHours(1));
    }

    private static SessionCredential credentialWithToken(OffsetDateTime expiration) {
        return new SessionCredential(FIRST_TOKEN, BlobTestBase.TEST_SESSION_KEY, expiration,
            BlobTestBase.TEST_SESSION_ACCOUNT_NAME);
    }

    private static HttpPipelineCallContext createContext() {
        return createContextForRequest(
            new HttpRequest(HttpMethod.GET, "https://testaccount.blob.core.windows.net/mycontainer/myblob"));
    }

    private static HttpPipelineCallContext createContextForRequest(HttpRequest request) {
        HttpPipelineCallContext context = mock(HttpPipelineCallContext.class);
        AtomicReference<Context> data = new AtomicReference<>(Context.NONE);

        when(context.getHttpRequest()).thenReturn(request);
        when(context.getContext()).thenAnswer(invocation -> data.get());
        when(context.getData(anyString())).thenAnswer(invocation -> data.get().getData(invocation.getArgument(0)));
        doAnswer(invocation -> {
            data.updateAndGet(current -> current.addData(invocation.getArgument(0), invocation.getArgument(1)));
            return null;
        }).when(context).setData(anyString(), org.mockito.ArgumentMatchers.any());

        return context;
    }

    private static final class MutableClock extends Clock {
        private final ZoneId zone;
        private Instant instant;

        private MutableClock(Instant instant) {
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

        private void advance(Duration duration) {
            instant = instant.plus(duration);
        }
    }
}
