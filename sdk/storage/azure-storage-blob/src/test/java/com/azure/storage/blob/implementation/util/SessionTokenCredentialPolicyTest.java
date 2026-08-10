// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.util;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpPipelineNextSyncPolicy;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.models.SessionMode;
import com.azure.storage.blob.models.SessionOptions;
import com.azure.storage.blob.models.SessionCredential;
import com.azure.storage.blob.models.SessionProvider;
import com.azure.storage.common.policy.StorageBearerTokenChallengeAuthorizationPolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

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
    private static final String SECOND_TOKEN = "second-session-token";
    HttpHeaderName authHeaderName = HttpHeaderName.AUTHORIZATION;

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

        policy = createPolicy(SessionMode.ENABLED);
    }

    @Test
    public void sessionAcquisitionServerFailureStartsAccountCooldown() {
        HttpPipelineNextPolicy firstNext = mock(HttpPipelineNextPolicy.class);
        HttpPipelineNextPolicy secondNext = mock(HttpPipelineNextPolicy.class);
        HttpResponse firstBearerResponse = mock(HttpResponse.class);
        HttpResponse secondBearerResponse = mock(HttpResponse.class);
        BlobStorageException serverFailure
            = new BlobStorageException("CreateSession failed.", new MockHttpResponse(null, 500), null);

        when(sessionProvider.getSessionAsync(any())).thenReturn(Mono.error(serverFailure));
        when(firstNext.process()).thenReturn(Mono.just(firstBearerResponse));
        when(secondNext.process()).thenReturn(Mono.just(secondBearerResponse));

        assertEquals(firstBearerResponse, policy.process(createContext(), firstNext).block());
        assertEquals(secondBearerResponse, policy.process(createContext(), secondNext).block());

        verify(sessionProvider, times(1)).getSessionAsync(any());
        verify(firstNext, times(1)).process();
        verify(secondNext, times(1)).process();
    }

    @Test
    public void sessionAcquisitionCooldownExpiresAfterFiveMinutes() {
        MutableClock clock = new MutableClock(Instant.parse("2026-06-19T00:00:00Z"));
        policy = createPolicy(SessionMode.ENABLED, clock);
        HttpPipelineNextPolicy firstNext = mock(HttpPipelineNextPolicy.class);
        HttpPipelineNextPolicy cooldownNext = mock(HttpPipelineNextPolicy.class);
        HttpPipelineNextPolicy expiredNext = mock(HttpPipelineNextPolicy.class);
        BlobStorageException serverFailure
            = new BlobStorageException("CreateSession failed.", new MockHttpResponse(null, 500), null);

        when(sessionProvider.getSessionAsync(any())).thenReturn(Mono.error(serverFailure))
            .thenReturn(Mono.just(credentialWithToken(FIRST_TOKEN)));
        when(firstNext.process()).thenReturn(Mono.just(mock(HttpResponse.class)));
        when(cooldownNext.process()).thenReturn(Mono.just(mock(HttpResponse.class)));
        when(expiredNext.clone()).thenReturn(expiredNext);
        when(expiredNext.process()).thenReturn(Mono.just(mock(HttpResponse.class)));

        policy.process(createContext(), firstNext).block();
        policy.process(createContext(), cooldownNext).block();
        clock.advance(Duration.ofMinutes(5));
        policy.process(createContext(), expiredNext).block();

        verify(sessionProvider, times(2)).getSessionAsync(any());
    }

    @Test
    public void policySignsRequestWithSessionCredential() {
        HttpPipelineCallContext context = createContext();
        HttpPipelineNextPolicy next = mock(HttpPipelineNextPolicy.class);
        HttpResponse response = mock(HttpResponse.class);

        when(sessionProvider.getSessionAsync(any())).thenReturn(Mono.just(credentialWithToken(FIRST_TOKEN)));
        when(next.clone()).thenReturn(next);
        when(next.process()).thenReturn(Mono.just(response));
        when(response.getStatusCode()).thenReturn(200);

        try (HttpResponse actualResponse = policy.process(context, next).block()) {
            assertEquals(response, actualResponse);
            assertTrue(
                context.getHttpRequest().getHeaders().getValue("Authorization").startsWith("Session " + FIRST_TOKEN),
                "Expected request to be signed with a session credential.");
            verify(next, times(1)).process();
        }
    }

    @Test
    public void policyInvalidatesSessionAndFallsBackToBearerAsync() {
        HttpPipelineCallContext context = createContext();
        HttpPipelineNextPolicy next = mock(HttpPipelineNextPolicy.class);
        HttpPipelineNextPolicy retryNext = mock(HttpPipelineNextPolicy.class);
        HttpResponse initialResponse = mock(HttpResponse.class);
        HttpResponse retriedResponse = mock(HttpResponse.class);

        when(sessionProvider.getSessionAsync(any())).thenReturn(Mono.just(credentialWithToken(FIRST_TOKEN)));
        when(next.clone()).thenReturn(retryNext);
        when(next.process()).thenReturn(Mono.just(initialResponse));
        when(retryNext.process()).thenReturn(Mono.just(retriedResponse));
        when(initialResponse.getStatusCode()).thenReturn(401);
        when(initialResponse.getHeaderValue(HttpHeaderName.WWW_AUTHENTICATE))
            .thenReturn("Session error=session_expired");
        when(retriedResponse.getStatusCode()).thenReturn(200);

        try (HttpResponse actualResponse = policy.process(context, next).block()) {
            assertEquals(retriedResponse, actualResponse);
            assertNull(context.getHttpRequest().getHeaders().getValue("Authorization"));
            verify(initialResponse, times(1)).close();
            verify(next, times(1)).process();
            verify(retryNext, times(1)).process();
            verify(sessionProvider, times(1)).getSessionAsync(any());
            verify(sessionProvider, times(1)).invalidateSession(any(), any());
        }
    }

    @Test
    public void policyInvalidatesSessionAndFallsBackToBearerSync() {
        HttpPipelineCallContext context = createContext();
        HttpPipelineNextSyncPolicy next = mock(HttpPipelineNextSyncPolicy.class);
        HttpPipelineNextSyncPolicy retryNext = mock(HttpPipelineNextSyncPolicy.class);
        HttpResponse initialResponse = mock(HttpResponse.class);
        HttpResponse retriedResponse = mock(HttpResponse.class);

        when(sessionProvider.getSession(any())).thenReturn(credentialWithToken(FIRST_TOKEN));
        when(next.clone()).thenReturn(retryNext);
        when(next.processSync()).thenReturn(initialResponse);
        when(retryNext.processSync()).thenReturn(retriedResponse);
        when(initialResponse.getStatusCode()).thenReturn(401);
        when(initialResponse.getHeaderValue(HttpHeaderName.WWW_AUTHENTICATE))
            .thenReturn("Session error=session_expired");
        when(retriedResponse.getStatusCode()).thenReturn(200);

        try (HttpResponse actualResponse = policy.processSync(context, next)) {
            assertEquals(retriedResponse, actualResponse);
            assertNull(context.getHttpRequest().getHeaders().getValue("Authorization"));
            verify(initialResponse, times(1)).close();
            verify(next, times(1)).processSync();
            verify(retryNext, times(1)).processSync();
        }
    }

    @Test
    public void policyDoesNotRetrySessionAfter401() {
        HttpPipelineCallContext context = createContext();
        HttpPipelineNextPolicy next = mock(HttpPipelineNextPolicy.class);
        HttpPipelineNextPolicy retryNext = mock(HttpPipelineNextPolicy.class);
        HttpResponse initialResponse = mock(HttpResponse.class);
        HttpResponse retriedResponse = mock(HttpResponse.class);

        when(sessionProvider.getSessionAsync(any())).thenReturn(Mono.just(credentialWithToken(FIRST_TOKEN)));
        when(next.clone()).thenReturn(retryNext);
        when(next.process()).thenReturn(Mono.just(initialResponse));
        when(retryNext.process()).thenReturn(Mono.just(retriedResponse));
        when(initialResponse.getStatusCode()).thenReturn(401);
        when(initialResponse.getHeaderValue(HttpHeaderName.WWW_AUTHENTICATE))
            .thenReturn("Session error=session_expired");
        when(retriedResponse.getStatusCode()).thenReturn(200);

        try (HttpResponse actualResponse = policy.process(context, next).block()) {
            assertEquals(retriedResponse, actualResponse);
            verify(retryNext, times(1)).process();
            verify(sessionProvider, times(1)).getSessionAsync(any());
        }
    }

    @Test
    public void policyReturns403WithoutRetry() {
        HttpPipelineCallContext context = createContext();
        HttpPipelineNextPolicy next = mock(HttpPipelineNextPolicy.class);
        HttpPipelineNextPolicy retryNext = mock(HttpPipelineNextPolicy.class);
        HttpResponse forbiddenResponse = mock(HttpResponse.class);

        when(sessionProvider.getSessionAsync(any())).thenReturn(Mono.just(credentialWithToken(FIRST_TOKEN)));
        when(next.clone()).thenReturn(retryNext);
        when(next.process()).thenReturn(Mono.just(forbiddenResponse));
        when(forbiddenResponse.getStatusCode()).thenReturn(403);

        try (HttpResponse actualResponse = policy.process(context, next).block()) {
            assertEquals(forbiddenResponse, actualResponse);
            verify(next, times(1)).process();
            verify(retryNext, times(0)).process();
            verify(forbiddenResponse, times(0)).close();
            verify(sessionProvider, times(1)).getSessionAsync(any());
        }
    }

    @Test
    public void policyFallsBackToBearerOnAny401() {
        HttpPipelineCallContext context = createContext();
        HttpPipelineNextPolicy next = mock(HttpPipelineNextPolicy.class);
        HttpPipelineNextPolicy retryNext = mock(HttpPipelineNextPolicy.class);
        HttpResponse unauthorizedResponse = mock(HttpResponse.class);
        HttpResponse retriedResponse = mock(HttpResponse.class);

        when(sessionProvider.getSessionAsync(any())).thenReturn(Mono.just(credentialWithToken(FIRST_TOKEN)));
        when(next.clone()).thenReturn(retryNext);
        when(next.process()).thenReturn(Mono.just(unauthorizedResponse));
        when(retryNext.process()).thenReturn(Mono.just(retriedResponse));
        when(unauthorizedResponse.getStatusCode()).thenReturn(401);
        when(retriedResponse.getStatusCode()).thenReturn(200);

        try (HttpResponse actualResponse = policy.process(context, next).block()) {
            assertEquals(retriedResponse, actualResponse);
            assertNull(context.getHttpRequest().getHeaders().getValue("Authorization"));
            verify(unauthorizedResponse, times(1)).close();
            verify(next, times(1)).process();
            verify(retryNext, times(1)).process();
            verify(sessionProvider, times(1)).getSessionAsync(any());
        }
    }

    @Test
    public void policyReturnsDataRequest503WithoutBearerFallbackAsync() {
        HttpPipelineCallContext context = createContext();
        HttpPipelineNextPolicy next = mock(HttpPipelineNextPolicy.class);
        HttpPipelineNextPolicy retryNext = mock(HttpPipelineNextPolicy.class);
        HttpResponse unavailableResponse = mock(HttpResponse.class);

        when(sessionProvider.getSessionAsync(any())).thenReturn(Mono.just(credentialWithToken(FIRST_TOKEN)));
        when(next.clone()).thenReturn(retryNext);
        when(next.process()).thenReturn(Mono.just(unavailableResponse));
        when(unavailableResponse.getStatusCode()).thenReturn(503);

        try (HttpResponse actualResponse = policy.process(context, next).block()) {
            assertEquals(unavailableResponse, actualResponse);
            verify(unavailableResponse, times(0)).close();
            verify(bearerPolicy, times(0)).process(any(), any());
            verify(retryNext, times(0)).process();
        }
    }

    @Test
    public void policyFallsToBearerOn400Async() {
        HttpPipelineCallContext context = createContext();
        HttpPipelineNextPolicy next = mock(HttpPipelineNextPolicy.class);
        HttpPipelineNextPolicy retryNext = mock(HttpPipelineNextPolicy.class);
        HttpResponse badRequestResponse = mock(HttpResponse.class);
        HttpResponse bearerResponse = mock(HttpResponse.class);

        when(sessionProvider.getSessionAsync(any())).thenReturn(Mono.just(credentialWithToken(FIRST_TOKEN)));
        when(next.clone()).thenReturn(retryNext);
        when(next.process()).thenReturn(Mono.just(badRequestResponse));
        when(retryNext.process()).thenReturn(Mono.just(bearerResponse));
        when(badRequestResponse.getStatusCode()).thenReturn(400);
        when(bearerResponse.getStatusCode()).thenReturn(200);

        try (HttpResponse actualResponse = policy.process(context, next).block()) {
            assertEquals(bearerResponse, actualResponse);
            verify(badRequestResponse, times(1)).close();
            verify(bearerPolicy, times(1)).process(any(), any());
            String authHeader = context.getHttpRequest().getHeaders().getValue("Authorization");
            assertTrue(authHeader == null || !authHeader.startsWith("Session"),
                "Session auth should have been stripped but was: " + authHeader);
        }
    }

    @Test
    public void policyReturnsDataRequest503WithoutBearerFallbackSync() {
        HttpPipelineCallContext context = createContext();
        HttpPipelineNextSyncPolicy next = mock(HttpPipelineNextSyncPolicy.class);
        HttpPipelineNextSyncPolicy retryNext = mock(HttpPipelineNextSyncPolicy.class);
        HttpResponse unavailableResponse = mock(HttpResponse.class);

        when(sessionProvider.getSession(any())).thenReturn(credentialWithToken(FIRST_TOKEN));
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

        when(sessionProvider.getSession(any())).thenReturn(credentialWithToken(FIRST_TOKEN));
        when(next.clone()).thenReturn(retryNext);
        when(next.processSync()).thenReturn(badRequestResponse);
        when(retryNext.processSync()).thenReturn(bearerResponse);
        when(badRequestResponse.getStatusCode()).thenReturn(400);
        when(bearerResponse.getStatusCode()).thenReturn(200);

        try (HttpResponse actualResponse = policy.processSync(context, next)) {
            assertEquals(bearerResponse, actualResponse);
            verify(badRequestResponse, times(1)).close();
            verify(bearerPolicy, times(1)).processSync(any(), any());
            String authHeader = context.getHttpRequest().getHeaders().getValue("Authorization");
            assertTrue(authHeader == null || !authHeader.startsWith("Session"),
                "Session auth should have been stripped but was: " + authHeader);
        }
    }

    @Test
    public void policyReturns503ServerBusyWithoutBearerFallback() {
        HttpPipelineCallContext context = createContext();
        HttpPipelineNextPolicy next = mock(HttpPipelineNextPolicy.class);
        HttpPipelineNextPolicy retryNext = mock(HttpPipelineNextPolicy.class);
        HttpResponse busyResponse = mock(HttpResponse.class);

        when(sessionProvider.getSessionAsync(any())).thenReturn(Mono.just(credentialWithToken(FIRST_TOKEN)));
        when(next.clone()).thenReturn(retryNext);
        when(next.process()).thenReturn(Mono.just(busyResponse));
        when(busyResponse.getStatusCode()).thenReturn(503);
        when(busyResponse.getHeaderValue(HttpHeaderName.fromString("x-ms-error-code"))).thenReturn("ServerBusy");

        try (HttpResponse actualResponse = policy.process(context, next).block()) {
            // ServerBusy 503 is not session-specific — return as-is for retry policy to handle
            assertEquals(busyResponse, actualResponse);
            verify(retryNext, times(0)).process();
            verify(busyResponse, times(0)).close();
        }
    }

    @Test
    public void disabledModeAlwaysPassesThrough() {
        SessionTokenCredentialPolicy nonePolicy = createPolicy(SessionMode.DISABLED);
        HttpPipelineCallContext context = createContext();
        HttpPipelineNextPolicy next = mock(HttpPipelineNextPolicy.class);
        HttpResponse response = mock(HttpResponse.class);

        when(next.process()).thenReturn(Mono.just(response));
        when(response.getStatusCode()).thenReturn(200);

        try (HttpResponse actualResponse = nonePolicy.process(context, next).block()) {
            assertEquals(response, actualResponse);
            // Verify bearer policy was invoked (session delegates to bearer in DISABLED mode)
            verify(bearerPolicy, times(1)).process(any(), any());
            verify(sessionProvider, times(0)).getSessionAsync(any());
        }
    }

    @Test
    public void disabledModeSyncAlwaysPassesThrough() {
        SessionTokenCredentialPolicy nonePolicy = createPolicy(SessionMode.DISABLED);
        HttpPipelineCallContext context = createContext();
        HttpPipelineNextSyncPolicy next = mock(HttpPipelineNextSyncPolicy.class);
        HttpResponse response = mock(HttpResponse.class);

        when(next.processSync()).thenReturn(response);
        when(response.getStatusCode()).thenReturn(200);

        try (HttpResponse actualResponse = nonePolicy.processSync(context, next)) {
            assertEquals(response, actualResponse);
            // Verify bearer policy was invoked (session delegates to bearer in DISABLED mode)
            verify(bearerPolicy, times(1)).processSync(any(), any());
            verify(sessionProvider, times(0)).getSession(any());
        }
    }

    @Test
    public void enabledModeSignsFirstRequest() {
        // The default `policy` in setUp is ENABLED — verify it signs the very first request
        HttpPipelineCallContext context = createContext();
        HttpPipelineNextPolicy next = mock(HttpPipelineNextPolicy.class);
        HttpResponse response = mock(HttpResponse.class);

        when(sessionProvider.getSessionAsync(any())).thenReturn(Mono.just(credentialWithToken(FIRST_TOKEN)));
        when(next.clone()).thenReturn(next);
        when(next.process()).thenReturn(Mono.just(response));
        when(response.getStatusCode()).thenReturn(200);

        policy.process(context, next).block().close();

        assertTrue(context.getHttpRequest().getHeaders().getValue(authHeaderName).startsWith("Session "));
        verify(sessionProvider, times(1)).getSessionAsync(any());
    }

    @Test
    public void sessionExpiringHintForcesBackgroundRefreshEvenWhenTimerNotDue() {
        HttpPipelineCallContext context = createContext();
        HttpPipelineNextPolicy next = mock(HttpPipelineNextPolicy.class);
        HttpResponse response = mock(HttpResponse.class);

        // Fresh session far from expiry, so the client's own jittered refresh timer is NOT due.
        when(sessionProvider.getSessionAsync(any())).thenReturn(Mono.just(credentialWithToken(FIRST_TOKEN)));
        when(next.clone()).thenReturn(next);
        when(next.process()).thenReturn(Mono.just(response));
        when(response.getStatusCode()).thenReturn(200);
        // The service signals (via x-ms-auth-info: session_expiring) that this session is about to stop
        // being honored — for example, just before its network-context binding rotates.
        when(response.getHeaderValue(HttpHeaderName.fromString("x-ms-auth-info"))).thenReturn("session_expiring");

        policy.process(context, next).block().close();

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
        HttpPipelineCallContext context = createContext();
        HttpPipelineNextPolicy next = mock(HttpPipelineNextPolicy.class);
        HttpResponse response = mock(HttpResponse.class);

        when(sessionProvider.getSessionAsync(any())).thenReturn(Mono.just(credentialWithToken(FIRST_TOKEN)));
        when(next.clone()).thenReturn(next);
        when(next.process()).thenReturn(Mono.just(response));
        when(response.getStatusCode()).thenReturn(200);
        // No x-ms-auth-info hint on the response.
        when(response.getHeaderValue(HttpHeaderName.fromString("x-ms-auth-info"))).thenReturn(null);

        policy.process(context, next).block().close();

        // Without the hint and with a fresh session, only the initial session is created and no refresh
        // hint is forwarded to the provider.
        verify(sessionProvider, times(1)).getSessionAsync(any());
        verify(sessionProvider, never()).refreshSession(any());
    }

    private SessionTokenCredentialPolicy createPolicy(SessionMode mode) {
        return createPolicy(mode, Clock.systemUTC());
    }

    private SessionTokenCredentialPolicy createPolicy(SessionMode mode, Clock clock) {
        SessionOptions options = new SessionOptions().setSessionMode(mode).setContainerName("mycontainer");
        return new SessionTokenCredentialPolicy(bearerPolicy, sessionProvider, options, clock);
    }

    private static SessionCredential credentialWithToken(String token) {
        return credentialWithToken(token, OffsetDateTime.now().plusHours(1));
    }

    private static SessionCredential credentialWithToken(String token, OffsetDateTime expiration) {
        return new SessionCredential(token, SessionTestHelper.TEST_SESSION_KEY, expiration,
            SessionTestHelper.TEST_ACCOUNT_NAME);
    }

    private static HttpPipelineCallContext createContext() {
        return createContextForUrl("https://myaccount.blob.core.windows.net/mycontainer/myblob");
    }

    private static HttpPipelineCallContext createContextForUrl(String url) {
        return createContextForRequest(new HttpRequest(HttpMethod.GET, url));
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

    @Test
    public void getBlobRequestUsesSessionAuth() {
        HttpPipelineCallContext context
            = createContextForUrl("https://myaccount.blob.core.windows.net/mycontainer/myblob");
        HttpPipelineNextPolicy next = mock(HttpPipelineNextPolicy.class);
        HttpResponse response = mock(HttpResponse.class);

        when(sessionProvider.getSessionAsync(any())).thenReturn(Mono.just(credentialWithToken(FIRST_TOKEN)));
        when(next.clone()).thenReturn(next);
        when(next.process()).thenReturn(Mono.just(response));
        when(response.getStatusCode()).thenReturn(200);

        policy.process(context, next).block().close();

        assertTrue(context.getHttpRequest().getHeaders().getValue(authHeaderName).startsWith("Session "),
            "GetBlob request should be signed with session auth");
    }

    @Test
    public void getBlobRequestProducesWellFormedSessionAuthHeader() {
        SessionCredential cred = credentialWithToken(FIRST_TOKEN);
        HttpRequest request
            = new HttpRequest(HttpMethod.GET, "https://myaccount.blob.core.windows.net/mycontainer/myblob");
        request.getHeaders()
            .set(HttpHeaderName.fromString("x-ms-version"), "2025-01-05")
            .set(HttpHeaderName.fromString("x-ms-client-request-id"), "11111111-2222-3333-4444-555555555555")
            .set(HttpHeaderName.RANGE, "bytes=0-1023");

        HttpPipelineCallContext context = createContextForRequest(request);
        HttpPipelineNextPolicy next = mock(HttpPipelineNextPolicy.class);
        HttpResponse response = mock(HttpResponse.class);

        when(sessionProvider.getSessionAsync(any())).thenReturn(Mono.just(cred));
        when(next.clone()).thenReturn(next);
        when(next.process()).thenReturn(Mono.just(response));
        when(response.getStatusCode()).thenReturn(200);

        policy.process(context, next).block().close();

        // The policy adapts Shared Key signing to the Session authorization scheme.
        String actual = request.getHeaders().getValue(authHeaderName);
        assertNotNull(actual, "Authorization header should be set by the policy");
        assertTrue(actual.startsWith("Session " + FIRST_TOKEN + ":"),
            "Authorization should use the Session scheme with the cached session token, but was: " + actual);
        String actualSignature = actual.substring(actual.indexOf(':') + 1);
        assertTrue(actualSignature.matches("[A-Za-z0-9+/]+={0,2}"),
            "Signature must be base64-encoded, but was: " + actualSignature);
    }

    /**
     * Regression guard: the Session protocol must normalize {@code Content-Length: "0"} to
     * {@code ""} in the string-to-sign, matching the server's canonicalization (which is the
     * same as documented Shared Key canonicalization). Signing with {@code Content-Length: 0}
     * must therefore produce the same HMAC as signing without a Content-Length header at all.
     * <p>
     * Originally we expected the opposite (signing the literal "0") based on a misread of the
     * service behavior; that caused 401 InvalidAuthenticationInfo errors on real blob GETs
     * because azure-core's {@code RestProxyBase} unconditionally puts {@code Content-Length: 0}
     * on body-less GETs while the server canonicalizes that to "" before computing its HMAC.
     */
    @Test
    public void contentLengthZeroProducesSameSignatureAsMissingContentLength() {
        String pinnedDate = "Wed, 22 Apr 2026 20:00:00 GMT";

        HttpRequest withCl0
            = new HttpRequest(HttpMethod.GET, "https://myaccount.blob.core.windows.net/mycontainer/myblob");
        withCl0.getHeaders()
            .set(HttpHeaderName.fromString("x-ms-version"), "2025-01-05")
            .set(HttpHeaderName.fromString("x-ms-client-request-id"), "11111111-2222-3333-4444-555555555555")
            .set(HttpHeaderName.RANGE, "bytes=0-1023")
            .set(HttpHeaderName.CONTENT_LENGTH, "0")
            .set(HttpHeaderName.fromString("x-ms-date"), pinnedDate);
        signRequestWithPolicy(withCl0);
        String sigWithCl0 = extractSignature(withCl0.getHeaders().getValue(authHeaderName));

        HttpRequest withoutCl
            = new HttpRequest(HttpMethod.GET, "https://myaccount.blob.core.windows.net/mycontainer/myblob");
        withoutCl.getHeaders()
            .set(HttpHeaderName.fromString("x-ms-version"), "2025-01-05")
            .set(HttpHeaderName.fromString("x-ms-client-request-id"), "11111111-2222-3333-4444-555555555555")
            .set(HttpHeaderName.RANGE, "bytes=0-1023")
            .set(HttpHeaderName.fromString("x-ms-date"), pinnedDate);
        signRequestWithPolicy(withoutCl);
        String sigWithoutCl = extractSignature(withoutCl.getHeaders().getValue(authHeaderName));

        assertEquals(sigWithoutCl, sigWithCl0,
            "Signing with Content-Length: 0 must produce the same signature as omitting it entirely.");
    }

    private static String extractSignature(String authHeader) {
        assertNotNull(authHeader, "Authorization header should be set");
        return authHeader.substring(authHeader.indexOf(':') + 1);
    }

    private void signRequestWithPolicy(HttpRequest request) {
        HttpPipelineNextPolicy next = mock(HttpPipelineNextPolicy.class);
        HttpResponse response = mock(HttpResponse.class);
        when(sessionProvider.getSessionAsync(any())).thenReturn(Mono.just(credentialWithToken(FIRST_TOKEN)));
        when(next.clone()).thenReturn(next);
        when(next.process()).thenReturn(Mono.just(response));
        when(response.getStatusCode()).thenReturn(200);
        policy.process(createContextForRequest(request), next).block().close();
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
