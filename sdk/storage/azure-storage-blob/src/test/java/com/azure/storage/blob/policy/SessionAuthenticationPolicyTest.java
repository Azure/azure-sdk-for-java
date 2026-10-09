// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.policy;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpPipelineNextSyncPolicy;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.util.Context;
import com.azure.storage.blob.BlobTestBase;
import com.azure.storage.blob.implementation.util.ModelHelper;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.implementation.util.SessionCredential;
import com.azure.storage.blob.models.SessionOptions;
import com.azure.storage.blob.models.SessionOptions.SessionMode;
import com.azure.storage.blob.models.TestSessionProvider;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.policy.StorageBearerTokenChallengeAuthorizationPolicy;
import com.azure.storage.common.policy.RequestRetryOptions;
import com.azure.storage.common.policy.RequestRetryPolicy;
import com.azure.storage.common.policy.RetryPolicyType;
import com.azure.storage.common.test.shared.http.WireTapHttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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

public class SessionAuthenticationPolicyTest {

    private static final String FIRST_TOKEN = "first-session-token";

    private TestSessionProvider sessionProvider;
    private StorageBearerTokenChallengeAuthorizationPolicy bearerPolicy;
    private SessionAuthenticationPolicy policy;

    @BeforeEach
    public void beforeEach() {
        sessionProvider = mock(TestSessionProvider.class);
        bearerPolicy = mock(StorageBearerTokenChallengeAuthorizationPolicy.class);
        when(sessionProvider.isRequestEligible(any())).thenReturn(true);

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

    @ParameterizedTest
    @EnumSource(SessionMode.class)
    public void policyRespectsSessionModeAsync(SessionMode mode) {
        SessionOptions options = new SessionOptions().setSessionMode(mode);
        policy = new SessionAuthenticationPolicy(bearerPolicy, sessionProvider, options);
        boolean sessionsEnabled = ModelHelper.resolveSessionMode(mode) == SessionMode.ENABLED;
        options.setSessionMode(sessionsEnabled ? SessionMode.DISABLED : SessionMode.ENABLED);
        if (sessionsEnabled) {
            when(sessionProvider.getSessionAsync(any())).thenReturn(Mono.just(credentialWithToken()));
        }

        StepVerifier.create(buildPipeline(successTransport()).send(blobGetRequest()))
            .assertNext(response -> assertEquals(200, response.getStatusCode()))
            .verifyComplete();

        verify(sessionProvider, times(sessionsEnabled ? 1 : 0)).isRequestEligible(any());
        verify(sessionProvider, times(sessionsEnabled ? 1 : 0)).getSessionAsync(any());
        verify(bearerPolicy, times(sessionsEnabled ? 0 : 1)).process(any(), any());
    }

    @ParameterizedTest
    @EnumSource(SessionMode.class)
    public void policyRespectsSessionModeSync(SessionMode mode) {
        SessionOptions options = new SessionOptions().setSessionMode(mode);
        policy = new SessionAuthenticationPolicy(bearerPolicy, sessionProvider, options);
        boolean sessionsEnabled = ModelHelper.resolveSessionMode(mode) == SessionMode.ENABLED;
        options.setSessionMode(sessionsEnabled ? SessionMode.DISABLED : SessionMode.ENABLED);
        if (sessionsEnabled) {
            when(sessionProvider.getSession(any())).thenReturn(credentialWithToken());
        }
        HttpPipelineNextSyncPolicy next = mock(HttpPipelineNextSyncPolicy.class);
        when(next.processSync()).thenReturn(new MockHttpResponse(null, 200));
        when(next.clone()).thenReturn(next);

        try (HttpResponse response = policy.processSync(createContext(), next)) {
            assertEquals(200, response.getStatusCode());
        }

        verify(sessionProvider, times(sessionsEnabled ? 1 : 0)).isRequestEligible(any());
        verify(sessionProvider, times(sessionsEnabled ? 1 : 0)).getSession(any());
        verify(bearerPolicy, times(sessionsEnabled ? 0 : 1)).processSync(any(), any());
    }

    @ParameterizedTest
    @CsvSource({ "original, original", ", testaccount", "'', testaccount" })
    public void policyCapturesAccountNameAsync(String accountName, String expectedAccountName) {
        SessionOptions options = new SessionOptions().setSessionMode(SessionMode.ENABLED).setAccountName(accountName);
        policy = new SessionAuthenticationPolicy(bearerPolicy, sessionProvider, options);
        options.setAccountName("changed");
        when(sessionProvider.getSessionAsync(any())).thenReturn(Mono.just(credentialWithToken()));

        StepVerifier.create(buildPipeline(successTransport()).send(blobGetRequest())).assertNext(response -> {
            assertEquals(200, response.getStatusCode());
            response.close();
        }).verifyComplete();

        verify(sessionProvider)
            .getSessionAsync(argThat(context -> expectedAccountName.equals(context.getAccountName())));
        verify(bearerPolicy, never()).process(any(), any());
    }

    @ParameterizedTest
    @CsvSource({ "original, original", ", testaccount", "'', testaccount" })
    public void policyCapturesAccountNameSync(String accountName, String expectedAccountName) {
        SessionOptions options = new SessionOptions().setSessionMode(SessionMode.ENABLED).setAccountName(accountName);
        policy = new SessionAuthenticationPolicy(bearerPolicy, sessionProvider, options);
        options.setAccountName("changed");
        when(sessionProvider.getSession(any())).thenReturn(credentialWithToken());
        HttpPipelineNextSyncPolicy next = mock(HttpPipelineNextSyncPolicy.class);
        when(next.processSync()).thenReturn(new MockHttpResponse(null, 200));
        when(next.clone()).thenReturn(next);

        try (HttpResponse response = policy.processSync(createContext(), next)) {
            assertEquals(200, response.getStatusCode());
        }

        verify(sessionProvider).getSession(argThat(context -> expectedAccountName.equals(context.getAccountName())));
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
            "Authorization should use the Session scheme with the cached session token, but was: " + actual);
        String actualSignature = actual.substring(actual.indexOf(':') + 1);
        assertTrue(actualSignature.matches("[A-Za-z0-9+/]+={0,2}"),
            "Signature must be base64-encoded, but was: " + actualSignature);
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
        verify(sessionProvider).getSession(argThat(context -> "testaccount".equals(context.getAccountName())
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
        verify(sessionProvider).getSessionAsync(argThat(context -> "testaccount".equals(context.getAccountName())
            && "mycontainer".equals(context.getContainerName())));
        verify(sessionProvider, never()).getSession(any());
        verify(bearerPolicy, never()).process(any(), any());
    }

    @Test
    public void asyncPolicyGetsContainerNameFromCustomEndpoint() {
        when(sessionProvider.getSessionAsync(any())).thenReturn(Mono.just(credentialWithToken()));
        HttpRequest request = new HttpRequest(HttpMethod.GET, "https://custom.endpoint.example/mycontainer/myblob");

        StepVerifier.create(buildPipeline(successTransport()).send(request)).assertNext(response -> {
            assertEquals(200, response.getStatusCode());
            response.close();
        }).verifyComplete();

        verify(sessionProvider).getSessionAsync(argThat(context -> "mycontainer".equals(context.getContainerName())));
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

    // Helpers

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
        SessionOptions options = new SessionOptions().setSessionMode(SessionMode.ENABLED);
        return new SessionAuthenticationPolicy(bearerPolicy, sessionProvider, options, clock);
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
        Map<String, Object> data = new ConcurrentHashMap<>();

        when(context.getHttpRequest()).thenReturn(request);
        when(context.getData(anyString()))
            .thenAnswer(invocation -> Optional.ofNullable(data.get(invocation.getArgument(0))));
        doAnswer(invocation -> {
            data.put(invocation.getArgument(0), invocation.getArgument(1));
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
