// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.util;

import com.azure.core.http.HttpHeaderName;
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
import com.azure.storage.blob.BlobTestBase;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.models.SessionCredential;
import com.azure.storage.blob.models.SessionOptions;
import com.azure.storage.blob.models.SessionProvider;
import com.azure.storage.common.policy.StorageBearerTokenChallengeAuthorizationPolicy;
import com.azure.storage.common.test.shared.http.ScriptedHttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SessionTokenCredentialPolicyTest {

    private static final String FIRST_TOKEN = "first-session-token";

    private SessionProvider sessionProvider;
    private StorageBearerTokenChallengeAuthorizationPolicy bearerPolicy;
    private SessionTokenCredentialPolicy policy;

    @BeforeEach
    public void beforeEach() {
        sessionProvider = mock(SessionProvider.class);
        bearerPolicy = mock(StorageBearerTokenChallengeAuthorizationPolicy.class);

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
    public void sessionAcquisitionServerFailureStartsAccountCooldown() {
        BlobStorageException serverFailure
            = new BlobStorageException("CreateSession failed.", new MockHttpResponse(null, 500), null);
        when(sessionProvider.getSessionAsync(any())).thenReturn(Mono.error(serverFailure));

        ScriptedHttpClient transport = new ScriptedHttpClient().enqueueResponse(200)  // first request: acquisition fails, bearer fallback
            .enqueueResponse(200); // second request: cooldown active, bearer fallback
        HttpPipeline pipeline = buildPipeline(transport);

        StepVerifier.create(pipeline.send(blobGetRequest()))
            .assertNext(r -> assertEquals(200, r.getStatusCode()))
            .verifyComplete();
        StepVerifier.create(pipeline.send(blobGetRequest()))
            .assertNext(r -> assertEquals(200, r.getStatusCode()))
            .verifyComplete();

        // Session acquisition is attempted only once; the cooldown suppresses the second attempt.
        verify(sessionProvider, times(1)).getSessionAsync(any());
    }

    @Test
    public void sessionAcquisitionCooldownExpiresAfterFiveMinutes() {
        MutableClock clock = new MutableClock(Instant.parse("2026-06-19T00:00:00Z"));
        policy = createPolicy(clock);
        BlobStorageException serverFailure
            = new BlobStorageException("CreateSession failed.", new MockHttpResponse(null, 500), null);

        when(sessionProvider.getSessionAsync(any())).thenReturn(Mono.error(serverFailure))       // first call: acquisition fails
            .thenReturn(Mono.just(credentialWithToken())); // third call: cooldown expired

        ScriptedHttpClient transport
            = new ScriptedHttpClient().enqueueResponse(200).enqueueResponse(200).enqueueResponse(200);
        HttpPipeline pipeline = buildPipeline(transport);

        StepVerifier.create(pipeline.send(blobGetRequest()))
            .assertNext(r -> assertEquals(200, r.getStatusCode()))
            .verifyComplete();
        StepVerifier.create(pipeline.send(blobGetRequest()))
            .assertNext(r -> assertEquals(200, r.getStatusCode()))
            .verifyComplete();

        clock.advance(Duration.ofMinutes(5));

        StepVerifier.create(pipeline.send(blobGetRequest()))
            .assertNext(r -> assertEquals(200, r.getStatusCode()))
            .verifyComplete();

        verify(sessionProvider, times(2)).getSessionAsync(any());
    }

    @Test
    public void policySignsRequestWithSessionCredential() {
        HttpRequest request = blobGetRequest();
        ScriptedHttpClient transport = new ScriptedHttpClient().enqueueResponse(200);
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
        ScriptedHttpClient transport = new ScriptedHttpClient().enqueueResponse(401)  // session auth returns 401
            .enqueueResponse(200); // bearer retry succeeds

        when(sessionProvider.getSessionAsync(any())).thenReturn(Mono.just(credentialWithToken()));

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
     * Invalidating a rejected session means the next request creates a brand new one. Where sessions cannot work at
     * all, that would repeat forever, so consecutive rejections must eventually suppress session authentication.
     */
    @Test
    public void repeatedSessionRejectionStartsAccountCooldown() {
        when(sessionProvider.getSessionAsync(any())).thenReturn(Mono.just(credentialWithToken()));

        ScriptedHttpClient transport = new ScriptedHttpClient().enqueueResponse(401)
            .enqueueResponse(200)
            .enqueueResponse(401)
            .enqueueResponse(200)
            .enqueueResponse(401)
            .enqueueResponse(200)
            .enqueueResponse(200); // fourth request: cooldown active, bearer only
        HttpPipeline pipeline = buildPipeline(transport);

        for (int i = 0; i < 4; i++) {
            StepVerifier.create(pipeline.send(blobGetRequest()))
                .assertNext(r -> assertEquals(200, r.getStatusCode()))
                .verifyComplete();
        }

        // Three rejections trip the cooldown, so the fourth request never acquires a session.
        verify(sessionProvider, times(3)).getSessionAsync(any());
        assertEquals(7, transport.getRequestCount());
    }

    @Test
    public void acceptedSessionResetsRejectionCount() {
        when(sessionProvider.getSessionAsync(any())).thenReturn(Mono.just(credentialWithToken()));

        ScriptedHttpClient transport = new ScriptedHttpClient().enqueueResponse(401)
            .enqueueResponse(200)
            .enqueueResponse(200) // session accepted, clearing the count
            .enqueueResponse(401)
            .enqueueResponse(200)
            .enqueueResponse(401)
            .enqueueResponse(200)
            .enqueueResponse(200); // fifth request still attempts a session
        HttpPipeline pipeline = buildPipeline(transport);

        for (int i = 0; i < 5; i++) {
            StepVerifier.create(pipeline.send(blobGetRequest()))
                .assertNext(r -> assertEquals(200, r.getStatusCode()))
                .verifyComplete();
        }

        // Four rejections total, but the accepted session reset the run, so the threshold is never reached.
        verify(sessionProvider, times(5)).getSessionAsync(any());
    }

    @Test
    public void sessionRejectionCooldownExpiresAfterFiveMinutes() {
        MutableClock clock = new MutableClock(Instant.parse("2026-06-19T00:00:00Z"));
        policy = createPolicy(clock);
        when(sessionProvider.getSessionAsync(any())).thenReturn(Mono.just(credentialWithToken()));

        ScriptedHttpClient transport = new ScriptedHttpClient().enqueueResponse(401)
            .enqueueResponse(200)
            .enqueueResponse(401)
            .enqueueResponse(200)
            .enqueueResponse(401)
            .enqueueResponse(200)
            .enqueueResponse(200)  // fourth request: cooldown active
            .enqueueResponse(200); // fifth request: cooldown expired, session attempted again
        HttpPipeline pipeline = buildPipeline(transport);

        for (int i = 0; i < 4; i++) {
            StepVerifier.create(pipeline.send(blobGetRequest()))
                .assertNext(r -> assertEquals(200, r.getStatusCode()))
                .verifyComplete();
        }
        verify(sessionProvider, times(3)).getSessionAsync(any());

        clock.advance(Duration.ofMinutes(5));

        StepVerifier.create(pipeline.send(blobGetRequest()))
            .assertNext(r -> assertEquals(200, r.getStatusCode()))
            .verifyComplete();

        verify(sessionProvider, times(4)).getSessionAsync(any());
    }

    @Test
    public void policyReturns403WithoutRetry() {
        HttpRequest request = blobGetRequest();
        ScriptedHttpClient transport = new ScriptedHttpClient().enqueueResponse(403);
        when(sessionProvider.getSessionAsync(any())).thenReturn(Mono.just(credentialWithToken()));

        StepVerifier.create(buildPipeline(transport).send(request))
            .assertNext(r -> assertEquals(403, r.getStatusCode()))
            .verifyComplete();

        assertEquals(1, transport.getRequestCount());
        verify(bearerPolicy, times(0)).process(any(), any());
    }

    @Test
    public void policyReturnsDataRequest503WithoutBearerFallbackAsync() {
        HttpRequest request = blobGetRequest();
        ScriptedHttpClient transport = new ScriptedHttpClient().enqueueResponse(503);
        when(sessionProvider.getSessionAsync(any())).thenReturn(Mono.just(credentialWithToken()));

        StepVerifier.create(buildPipeline(transport).send(request))
            .assertNext(r -> assertEquals(503, r.getStatusCode()))
            .verifyComplete();

        // 503 is not a bearer-fallback trigger; the response is returned as-is.
        assertEquals(1, transport.getRequestCount());
        verify(bearerPolicy, times(0)).process(any(), any());
    }

    @Test
    public void policyFallsToBearerOn400Async() {
        HttpRequest request = blobGetRequest();
        ScriptedHttpClient transport = new ScriptedHttpClient().enqueueResponse(400).enqueueResponse(200);
        when(sessionProvider.getSessionAsync(any())).thenReturn(Mono.just(credentialWithToken()));

        StepVerifier.create(buildPipeline(transport).send(request))
            .assertNext(r -> assertEquals(200, r.getStatusCode()))
            .verifyComplete();

        assertEquals(2, transport.getRequestCount());
        verify(bearerPolicy, times(1)).process(any(), any());
        String authHeader = request.getHeaders().getValue(HttpHeaderName.AUTHORIZATION);
        assertTrue(authHeader == null || !authHeader.startsWith("Session"),
            "Session auth should have been stripped but was: " + authHeader);
    }

    @Test
    public void sessionExpiringHintForcesBackgroundRefreshEvenWhenTimerNotDue() {
        HttpRequest request = blobGetRequest();
        HttpHeaders responseHeaders
            = new HttpHeaders().set(HttpHeaderName.fromString("x-ms-auth-info"), "session_expiring");
        ScriptedHttpClient transport = new ScriptedHttpClient().enqueueResponse(200, responseHeaders);
        when(sessionProvider.getSessionAsync(any())).thenReturn(Mono.just(credentialWithToken()));

        StepVerifier.create(buildPipeline(transport).send(request))
            .assertNext(r -> assertEquals(200, r.getStatusCode()))
            .verifyComplete();

        // The service hint must trigger a proactive background refresh call, even though the client's
        // own refresh timer had not yet elapsed. Dropping the hint here is what previously let the session
        // be used past the rotation boundary, surfacing as a 401 "session_token_invalid" (network context
        // mismatch). The refresh itself is delegated to the provider via refreshSession, distinct from the
        // single getSessionAsync call used to obtain the credential for this request.
        verify(sessionProvider, times(1)).getSessionAsync(any());
        verify(sessionProvider, times(1)).refreshSession(any());
    }

    @Test
    public void noSessionExpiringHintDoesNotForceBackgroundRefresh() {
        HttpRequest request = blobGetRequest();
        ScriptedHttpClient transport = new ScriptedHttpClient().enqueueResponse(200);
        when(sessionProvider.getSessionAsync(any())).thenReturn(Mono.just(credentialWithToken()));

        StepVerifier.create(buildPipeline(transport).send(request))
            .assertNext(r -> assertEquals(200, r.getStatusCode()))
            .verifyComplete();

        // Without the hint and with a fresh session, only the initial get is made and no refresh occurs.
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

        ScriptedHttpClient transport = new ScriptedHttpClient().enqueueResponse(200);
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

    // Sync tests use a minimal mock next-policy because the real pipeline doesn't expose sync invocation.

    @Test
    public void policyInvalidatesSessionAndFallsBackToBearerSync() {
        HttpPipelineCallContext context = createContext();
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

        try (HttpResponse actualResponse = policy.processSync(context, next)) {
            assertEquals(retriedResponse, actualResponse);
            assertNull(context.getHttpRequest().getHeaders().getValue(HttpHeaderName.AUTHORIZATION));
            verify(initialResponse, times(1)).close();
            verify(next, times(1)).processSync();
            verify(retryNext, times(1)).processSync();
            verify(sessionProvider, times(1)).invalidateSession(any(), any());
        }
    }

    @Test
    public void policyReturnsDataRequest503WithoutBearerFallbackSync() {
        HttpPipelineCallContext context = createContext();
        HttpPipelineNextSyncPolicy next = mock(HttpPipelineNextSyncPolicy.class);
        HttpPipelineNextSyncPolicy retryNext = mock(HttpPipelineNextSyncPolicy.class);
        HttpResponse unavailableResponse = mock(HttpResponse.class);

        when(sessionProvider.getSession(any())).thenReturn(credentialWithToken());
        when(next.clone()).thenReturn(retryNext);
        when(next.processSync()).thenReturn(unavailableResponse);
        when(unavailableResponse.getStatusCode()).thenReturn(503);

        try (HttpResponse actualResponse = policy.processSync(context, next)) {
            assertEquals(unavailableResponse, actualResponse);
            verify(unavailableResponse, times(0)).close();
            verify(bearerPolicy, times(0)).processSync(any(), any());
            verify(retryNext, times(0)).processSync();
        }
    }

    @Test
    public void policyFallsToBearerOn400Sync() {
        HttpPipelineCallContext context = createContext();
        HttpPipelineNextSyncPolicy next = mock(HttpPipelineNextSyncPolicy.class);
        HttpPipelineNextSyncPolicy retryNext = mock(HttpPipelineNextSyncPolicy.class);
        HttpResponse badRequestResponse = mock(HttpResponse.class);
        HttpResponse bearerResponse = mock(HttpResponse.class);

        when(sessionProvider.getSession(any())).thenReturn(credentialWithToken());
        when(next.clone()).thenReturn(retryNext);
        when(next.processSync()).thenReturn(badRequestResponse);
        when(retryNext.processSync()).thenReturn(bearerResponse);
        when(badRequestResponse.getStatusCode()).thenReturn(400);
        when(bearerResponse.getStatusCode()).thenReturn(200);

        try (HttpResponse actualResponse = policy.processSync(context, next)) {
            assertEquals(bearerResponse, actualResponse);
            verify(badRequestResponse, times(1)).close();
            verify(bearerPolicy, times(1)).processSync(any(), any());
            String authHeader = context.getHttpRequest().getHeaders().getValue(HttpHeaderName.AUTHORIZATION);
            assertTrue(authHeader == null || !authHeader.startsWith("Session"),
                "Session auth should have been stripped but was: " + authHeader);
        }
    }

    @Test
    public void repeatedSessionRejectionStartsAccountCooldownSync() {
        when(sessionProvider.getSession(any())).thenReturn(credentialWithToken());

        for (int i = 0; i < 3; i++) {
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

        verify(sessionProvider, times(3)).getSession(any());

        // The cooldown is now active, so this request goes straight to bearer without acquiring a session.
        HttpPipelineCallContext context = createContext();
        HttpPipelineNextSyncPolicy next = mock(HttpPipelineNextSyncPolicy.class);
        HttpResponse bearerResponse = mock(HttpResponse.class);
        when(next.processSync()).thenReturn(bearerResponse);
        when(bearerResponse.getStatusCode()).thenReturn(200);

        try (HttpResponse actualResponse = policy.processSync(context, next)) {
            assertEquals(bearerResponse, actualResponse);
            verify(sessionProvider, times(3)).getSession(any());
            verify(next, times(0)).clone();
        }
    }

    // Helpers

    private HttpPipeline buildPipeline(ScriptedHttpClient transport) {
        return new HttpPipelineBuilder().httpClient(transport).policies(policy).build();
    }

    private static HttpRequest blobGetRequest() {
        return new HttpRequest(HttpMethod.GET, "https://testaccount.blob.core.windows.net/mycontainer/myblob");
    }

    private SessionTokenCredentialPolicy createPolicy() {
        return createPolicy(Clock.systemUTC());
    }

    private SessionTokenCredentialPolicy createPolicy(Clock clock) {
        SessionOptions options = new SessionOptions().setContainerName("mycontainer");
        return new SessionTokenCredentialPolicy(bearerPolicy, sessionProvider, options, clock);
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
