// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.util;

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
import com.azure.storage.blob.BlobTestBase;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.models.SessionCredential;
import com.azure.storage.blob.models.SessionOptions;
import com.azure.storage.blob.models.SessionProvider;
import com.azure.storage.common.policy.StorageBearerTokenChallengeAuthorizationPolicy;
import com.azure.storage.common.test.shared.http.WireTapHttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
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
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

    private SessionProvider sessionProvider;
    private StorageBearerTokenChallengeAuthorizationPolicy bearerPolicy;
    private SessionAuthenticationPolicy policy;

    @BeforeEach
    public void beforeEach() {
        sessionProvider = mock(SessionProvider.class);
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
    @CsvSource({ "400, 1", "401, 2", "403, 1", "404, 2", "429, 2", "500, 1", "503, 1", "599, 1", "600, 2" })
    public void sessionAcquisitionFailureCooldownAsync(int statusCode, int expectedAcquisitions) {
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

        verify(sessionProvider, times(expectedAcquisitions)).getSessionAsync(any());
        verify(bearerPolicy, times(2)).process(any(), any());
    }

    @ParameterizedTest
    @CsvSource({ "400, 1", "401, 2", "403, 1", "404, 2", "429, 2", "500, 1", "503, 1", "599, 1", "600, 2" })
    public void sessionAcquisitionFailureCooldownSync(int statusCode, int expectedAcquisitions) {
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

        verify(sessionProvider, times(expectedAcquisitions)).getSession(any());
        verify(bearerPolicy, times(2)).processSync(any(), any());
    }

    @Test
    public void sessionAcquisitionCooldownExpiresAfterFiveMinutes() {
        MutableClock clock = new MutableClock(Instant.parse("2026-06-19T00:00:00Z"));
        policy = createPolicy(clock);
        BlobStorageException serverFailure
            = new BlobStorageException("CreateSession failed.", new MockHttpResponse(null, 500), null);

        when(sessionProvider.getSessionAsync(any())).thenReturn(Mono.error(serverFailure))       // first call: acquisition fails
            .thenReturn(Mono.just(credentialWithToken())); // third call: cooldown expired

        HttpClient transport = successTransport();
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

    @ParameterizedTest
    @CsvSource({
        "400, testaccount, othercontainer",
        "403, testaccount, othercontainer",
        "503, testaccount, othercontainer",
        "400, otheraccount, mycontainer",
        "403, otheraccount, mycontainer",
        "503, otheraccount, mycontainer" })
    public void acquisitionCooldownIsIsolatedToContainerAsync(int statusCode, String otherAccount,
        String otherContainer) {
        MutableClock clock = new MutableClock(Instant.parse("2026-06-19T00:00:00Z"));
        policy = createPolicy(clock);
        BlobStorageException failure
            = new BlobStorageException("CreateSession failed.", new MockHttpResponse(null, statusCode), null);
        when(sessionProvider.getSessionAsync(any())).thenReturn(Mono.error(failure))
            .thenReturn(Mono.just(credentialWithToken()));
        HttpPipeline pipeline = buildPipeline(successTransport());

        StepVerifier.create(pipeline.send(blobGetRequest()))
            .assertNext(r -> assertEquals(200, r.getStatusCode()))
            .verifyComplete();
        StepVerifier.create(pipeline.send(blobGetRequest("TESTACCOUNT", "MYCONTAINER")))
            .assertNext(r -> assertEquals(200, r.getStatusCode()))
            .verifyComplete();
        verify(sessionProvider, times(1)).getSessionAsync(any());

        HttpRequest otherRequest = blobGetRequest(otherAccount, otherContainer);
        StepVerifier.create(pipeline.send(otherRequest))
            .assertNext(r -> assertEquals(200, r.getStatusCode()))
            .verifyComplete();
        assertTrue(isSessionAuthenticated(otherRequest));
        verify(sessionProvider, times(1))
            .getSessionAsync(argThat(context -> otherAccount.equals(context.getAccountName())
                && otherContainer.equals(context.getContainerName())));

        StepVerifier.create(pipeline.send(blobGetRequest()))
            .assertNext(r -> assertEquals(200, r.getStatusCode()))
            .verifyComplete();
        verify(sessionProvider, times(2)).getSessionAsync(any());

        clock.advance(Duration.ofMinutes(5));
        HttpRequest resumedRequest = blobGetRequest();
        StepVerifier.create(pipeline.send(resumedRequest))
            .assertNext(r -> assertEquals(200, r.getStatusCode()))
            .verifyComplete();
        assertTrue(isSessionAuthenticated(resumedRequest));
        verify(sessionProvider, times(3)).getSessionAsync(any());
    }

    @ParameterizedTest
    @CsvSource({
        "400, testaccount, othercontainer",
        "403, testaccount, othercontainer",
        "503, testaccount, othercontainer",
        "400, otheraccount, mycontainer",
        "403, otheraccount, mycontainer",
        "503, otheraccount, mycontainer" })
    public void acquisitionCooldownIsIsolatedToContainerSync(int statusCode, String otherAccount,
        String otherContainer) {
        MutableClock clock = new MutableClock(Instant.parse("2026-06-19T00:00:00Z"));
        policy = createPolicy(clock);
        BlobStorageException failure
            = new BlobStorageException("CreateSession failed.", new MockHttpResponse(null, statusCode), null);
        when(sessionProvider.getSession(any())).thenThrow(failure).thenReturn(credentialWithToken());

        sendSessionResponseSync(blobGetRequest(), 200);
        sendSessionResponseSync(blobGetRequest("TESTACCOUNT", "MYCONTAINER"), 200);
        verify(sessionProvider, times(1)).getSession(any());

        HttpRequest otherRequest = blobGetRequest(otherAccount, otherContainer);
        sendSessionResponseSync(otherRequest, 200);
        assertTrue(isSessionAuthenticated(otherRequest));
        verify(sessionProvider, times(1)).getSession(argThat(context -> otherAccount.equals(context.getAccountName())
            && otherContainer.equals(context.getContainerName())));

        sendSessionResponseSync(blobGetRequest(), 200);
        verify(sessionProvider, times(2)).getSession(any());

        clock.advance(Duration.ofMinutes(5));
        HttpRequest resumedRequest = blobGetRequest();
        sendSessionResponseSync(resumedRequest, 200);
        assertTrue(isSessionAuthenticated(resumedRequest));
        verify(sessionProvider, times(3)).getSession(any());
    }

    @ParameterizedTest
    @CsvSource({ "testaccount, othercontainer", "otheraccount, mycontainer" })
    public void rejectionCountsAndCooldownsAreIsolatedToContainerAsync(String otherAccount, String otherContainer) {
        MutableClock clock = new MutableClock(Instant.parse("2026-06-19T00:00:00Z"));
        policy = createPolicy(clock);
        when(sessionProvider.getSessionAsync(any())).thenReturn(Mono.just(credentialWithToken()));
        HttpPipeline pipeline = buildPipeline(sessionRejectionTransportWithAcceptedSecondRequest());
        boolean[] useOtherContainer = { false, true, false, true, false, true, false, true, true };
        int[] expectedAcquisitions = { 1, 2, 3, 4, 5, 6, 6, 7, 7 };

        // B's success must not reset A's count, and their rejections must not combine.
        // Once A enters cooldown, B still has one rejection left before its own cooldown.
        for (int i = 0; i < useOtherContainer.length; i++) {
            HttpRequest request
                = useOtherContainer[i] ? blobGetRequest(otherAccount, otherContainer) : blobGetRequest();
            StepVerifier.create(pipeline.send(request))
                .assertNext(r -> assertEquals(200, r.getStatusCode()))
                .verifyComplete();
            verify(sessionProvider, times(expectedAcquisitions[i])).getSessionAsync(any());
        }

        clock.advance(Duration.ofMinutes(5));
        StepVerifier.create(pipeline.send(blobGetRequest()))
            .assertNext(r -> assertEquals(200, r.getStatusCode()))
            .verifyComplete();
        StepVerifier.create(pipeline.send(blobGetRequest(otherAccount, otherContainer)))
            .assertNext(r -> assertEquals(200, r.getStatusCode()))
            .verifyComplete();
        verify(sessionProvider, times(9)).getSessionAsync(any());
    }

    @ParameterizedTest
    @CsvSource({ "testaccount, othercontainer", "otheraccount, mycontainer" })
    public void rejectionCountsAndCooldownsAreIsolatedToContainerSync(String otherAccount, String otherContainer) {
        MutableClock clock = new MutableClock(Instant.parse("2026-06-19T00:00:00Z"));
        policy = createPolicy(clock);
        when(sessionProvider.getSession(any())).thenReturn(credentialWithToken());
        boolean[] useOtherContainer = { false, true, false, true, false, true, false, true, true };
        int[] expectedAcquisitions = { 1, 2, 3, 4, 5, 6, 6, 7, 7 };

        for (int i = 0; i < useOtherContainer.length; i++) {
            HttpRequest request
                = useOtherContainer[i] ? blobGetRequest(otherAccount, otherContainer) : blobGetRequest();
            sendSessionResponseSync(request, i == 1 ? 200 : 401);
            verify(sessionProvider, times(expectedAcquisitions[i])).getSession(any());
        }

        clock.advance(Duration.ofMinutes(5));
        sendSessionResponseSync(blobGetRequest(), 401);
        sendSessionResponseSync(blobGetRequest(otherAccount, otherContainer), 401);
        verify(sessionProvider, times(9)).getSession(any());
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
        WireTapHttpClient transport = bearerFallbackTransport(401);

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
    public void repeatedSessionRejectionStartsContainerCooldown() {
        when(sessionProvider.getSessionAsync(any())).thenReturn(Mono.just(credentialWithToken()));

        WireTapHttpClient transport = bearerFallbackTransport(401);
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

        WireTapHttpClient transport = sessionRejectionTransportWithAcceptedSecondRequest();
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

        WireTapHttpClient transport = bearerFallbackTransport(401);
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

    @ParameterizedTest
    @ValueSource(ints = { 404, 409, 429, 499, 600 })
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
    @ValueSource(ints = { 400, 401, 403, 500, 503, 599 })
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

    @ParameterizedTest
    @ValueSource(ints = { 400, 403, 500, 503, 599 })
    public void policyFallsToBearerWithoutInvalidationAsync(int statusCode) {
        HttpRequest request = blobGetRequest();
        WireTapHttpClient transport = bearerFallbackTransport(statusCode);
        when(sessionProvider.getSessionAsync(any())).thenReturn(Mono.just(credentialWithToken()));

        HttpPipeline pipeline = buildPipeline(transport);
        for (int i = 0; i < 4; i++) {
            request = blobGetRequest();
            StepVerifier.create(pipeline.send(request))
                .assertNext(r -> assertEquals(200, r.getStatusCode()))
                .verifyComplete();
        }

        assertEquals(8, transport.getRequestCount());
        verify(bearerPolicy, times(4)).process(any(), any());
        verify(sessionProvider, times(4)).getSessionAsync(any());
        verify(sessionProvider, never()).invalidateSession(any(), any());
        String authHeader = request.getHeaders().getValue(HttpHeaderName.AUTHORIZATION);
        assertTrue(authHeader == null || !authHeader.startsWith("Session"),
            "Session auth should have been stripped but was: " + authHeader);
    }

    @Test
    public void sessionExpiringHintForcesBackgroundRefreshEvenWhenTimerNotDue() {
        HttpRequest request = blobGetRequest();
        HttpHeaders responseHeaders
            = new HttpHeaders().set(HttpHeaderName.fromString("x-ms-auth-info"), "session_expiring");
        HttpClient transport = responseTransport(200, responseHeaders);
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
        HttpClient transport = successTransport();
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

    @ParameterizedTest
    @ValueSource(ints = { 404, 409, 429, 499, 600 })
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

    @ParameterizedTest
    @CsvSource({
        "400, 200",
        "403, 200",
        "500, 200",
        "503, 200",
        "599, 200",
        "400, 400",
        "403, 403",
        "500, 500",
        "503, 503",
        "599, 599" })
    public void policyFallsToBearerWithoutInvalidationSync(int statusCode, int bearerStatusCode) {
        HttpPipelineCallContext context = createContext();
        HttpPipelineNextSyncPolicy next = mock(HttpPipelineNextSyncPolicy.class);
        HttpPipelineNextSyncPolicy retryNext = mock(HttpPipelineNextSyncPolicy.class);
        HttpResponse badRequestResponse = mock(HttpResponse.class);
        HttpResponse bearerResponse = mock(HttpResponse.class);

        when(sessionProvider.getSession(any())).thenReturn(credentialWithToken());
        when(next.clone()).thenReturn(retryNext);
        when(next.processSync()).thenReturn(badRequestResponse);
        when(retryNext.processSync()).thenReturn(bearerResponse);
        when(badRequestResponse.getStatusCode()).thenReturn(statusCode);
        when(bearerResponse.getStatusCode()).thenReturn(bearerStatusCode);

        try (HttpResponse actualResponse = policy.processSync(context, next)) {
            assertEquals(bearerResponse, actualResponse);
            assertEquals(bearerStatusCode, actualResponse.getStatusCode());
            verify(badRequestResponse, times(1)).close();
            verify(bearerPolicy, times(1)).processSync(any(), any());
            verify(next, times(1)).processSync();
            verify(retryNext, times(1)).processSync();
            verify(sessionProvider, never()).invalidateSession(any(), any());
            String authHeader = context.getHttpRequest().getHeaders().getValue(HttpHeaderName.AUTHORIZATION);
            assertTrue(authHeader == null || !authHeader.startsWith("Session"),
                "Session auth should have been stripped but was: " + authHeader);
        }
    }

    @Test
    public void repeatedSessionRejectionStartsContainerCooldownSync() {
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

    // Helpers

    private void sendSessionResponseSync(HttpRequest request, int sessionStatusCode) {
        HttpPipelineNextSyncPolicy next = mock(HttpPipelineNextSyncPolicy.class);
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
        SessionOptions options = new SessionOptions();
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
