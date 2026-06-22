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
import com.azure.storage.blob.models.SessionMode;
import com.azure.storage.blob.models.SessionOptions;
import com.azure.storage.common.policy.StorageBearerTokenChallengeAuthorizationPolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SessionTokenCredentialPolicyTest {

    private static final String FIRST_TOKEN = "first-session-token";
    private static final String SECOND_TOKEN = "second-session-token";
    HttpHeaderName authHeaderName = HttpHeaderName.AUTHORIZATION;

    private BlobSessionClient sessionClient;
    private StorageBearerTokenChallengeAuthorizationPolicy bearerPolicy;
    private SessionTokenCredentialPolicy policy;

    @BeforeEach
    public void beforeEach() {
        sessionClient = mock(BlobSessionClient.class);
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

        policy = createPolicy(SessionMode.SINGLE_SPECIFIED_CONTAINER);
    }

    @Test
    public void policyCreatesSessionOnFirstAsyncAccess() {
        when(sessionClient.createSessionAsync()).thenReturn(Mono.just(credentialWithToken(FIRST_TOKEN)));

        StorageSessionCredential credential = policy.getValidSessionAsync().block();

        assertNotNull(credential);
        assertEquals(FIRST_TOKEN, credential.getSessionToken());
        verify(sessionClient, times(1)).createSessionAsync();
    }

    @Test
    public void policyReturnsCachedSessionOnConcurrentAsyncAccess() {
        when(sessionClient.createSessionAsync()).thenReturn(Mono.just(credentialWithToken(FIRST_TOKEN)))
            .thenReturn(Mono.just(credentialWithToken(SECOND_TOKEN)));

        List<StorageSessionCredential> results
            = Flux.range(0, 5).flatMap(ignored -> policy.getValidSessionAsync()).collectList().block();

        assertNotNull(results);
        assertEquals(5, results.size());
        results.forEach(credential -> assertEquals(FIRST_TOKEN, credential.getSessionToken()));
        verify(sessionClient, times(1)).createSessionAsync();
    }

    @Test
    public void policyRefreshesNearExpiryWithoutBlockingSyncRequests() {
        StorageSessionCredential nearExpiry = credentialWithToken(FIRST_TOKEN, OffsetDateTime.now().plusSeconds(2));
        StorageSessionCredential refreshed = credentialWithToken(SECOND_TOKEN);

        when(sessionClient.createSessionSync()).thenReturn(nearExpiry);
        // This is a Reactor quirk where Mono.just() emits synchronously on subscribe, so the refresh happens
        // immediately when the cache determines the credential is near expiry
        when(sessionClient.createSessionAsync()).thenReturn(Mono.just(refreshed));

        // Cold call to getValidSessionSync triggers session creation via createSessionSync
        StorageSessionCredential initial = policy.getValidSessionSync();
        // Trigger refresh,  which uses sessionClient.createSessionAsync() to get the refreshed session
        StorageSessionCredential duringRefresh = policy.getValidSessionSync();
        StorageSessionCredential afterRefresh = policy.getValidSessionSync();

        assertEquals(FIRST_TOKEN, initial.getSessionToken());
        assertEquals(FIRST_TOKEN, duringRefresh.getSessionToken());
        assertEquals(SECOND_TOKEN, afterRefresh.getSessionToken());
        verify(sessionClient, times(1)).createSessionSync();
        verify(sessionClient, times(1)).createSessionAsync();
    }

    @Test
    public void concurrentSyncAccessOnlyCreatesOneSession() throws Exception {
        when(sessionClient.createSessionSync()).thenAnswer(invocation -> {
            Thread.sleep(100);
            return credentialWithToken(FIRST_TOKEN);
        }).thenReturn(credentialWithToken(SECOND_TOKEN));

        int threadCount = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        try {
            List<Callable<StorageSessionCredential>> tasks = IntStream.range(0, threadCount)
                .mapToObj(i -> (Callable<StorageSessionCredential>) policy::getValidSessionSync)
                .collect(Collectors.toList());

            List<Future<StorageSessionCredential>> futures = executor.invokeAll(tasks);
            for (Future<StorageSessionCredential> future : futures) {
                assertEquals(FIRST_TOKEN, future.get().getSessionToken());
            }

            verify(sessionClient, times(1)).createSessionSync();
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    public void policySignsRequestWithSessionCredential() {
        HttpPipelineCallContext context = createContext();
        HttpPipelineNextPolicy next = mock(HttpPipelineNextPolicy.class);
        HttpResponse response = mock(HttpResponse.class);

        when(sessionClient.createSessionAsync()).thenReturn(Mono.just(credentialWithToken(FIRST_TOKEN)));
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
    public void policyInvalidatesSessionAndRetriesOnceAsync() {
        HttpPipelineCallContext context = createContext();
        HttpPipelineNextPolicy next = mock(HttpPipelineNextPolicy.class);
        HttpPipelineNextPolicy retryNext = mock(HttpPipelineNextPolicy.class);
        HttpResponse initialResponse = mock(HttpResponse.class);
        HttpResponse retriedResponse = mock(HttpResponse.class);

        when(sessionClient.createSessionAsync()).thenReturn(Mono.just(credentialWithToken(FIRST_TOKEN)))
            .thenReturn(Mono.just(credentialWithToken(SECOND_TOKEN)));
        when(next.clone()).thenReturn(retryNext);
        when(next.process()).thenReturn(Mono.just(initialResponse));
        when(retryNext.process()).thenReturn(Mono.just(retriedResponse));
        when(initialResponse.getStatusCode()).thenReturn(401);
        when(initialResponse.getHeaderValue(HttpHeaderName.WWW_AUTHENTICATE))
            .thenReturn("Session error=session_expired");
        when(retriedResponse.getStatusCode()).thenReturn(200);

        try (HttpResponse actualResponse = policy.process(context, next).block()) {
            assertEquals(retriedResponse, actualResponse);
            assertTrue(
                context.getHttpRequest().getHeaders().getValue("Authorization").startsWith("Session " + SECOND_TOKEN));
            verify(initialResponse, times(1)).close();
            verify(next, times(1)).process();
            verify(retryNext, times(1)).process();
            verify(sessionClient, times(2)).createSessionAsync();
        }
    }

    @Test
    public void policyInvalidatesSessionAndRetriesOnceSync() {
        HttpPipelineCallContext context = createContext();
        HttpPipelineNextSyncPolicy next = mock(HttpPipelineNextSyncPolicy.class);
        HttpPipelineNextSyncPolicy retryNext = mock(HttpPipelineNextSyncPolicy.class);
        HttpResponse initialResponse = mock(HttpResponse.class);
        HttpResponse retriedResponse = mock(HttpResponse.class);

        when(sessionClient.createSessionSync()).thenReturn(credentialWithToken(FIRST_TOKEN))
            .thenReturn(credentialWithToken(SECOND_TOKEN));
        when(next.clone()).thenReturn(retryNext);
        when(next.processSync()).thenReturn(initialResponse);
        when(retryNext.processSync()).thenReturn(retriedResponse);
        when(initialResponse.getStatusCode()).thenReturn(401);
        when(initialResponse.getHeaderValue(HttpHeaderName.WWW_AUTHENTICATE))
            .thenReturn("Session error=session_expired");
        when(retriedResponse.getStatusCode()).thenReturn(200);

        try (HttpResponse actualResponse = policy.processSync(context, next)) {
            assertEquals(retriedResponse, actualResponse);
            assertTrue(
                context.getHttpRequest().getHeaders().getValue("Authorization").startsWith("Session " + SECOND_TOKEN));
            verify(initialResponse, times(1)).close();
            verify(next, times(1)).processSync();
            verify(retryNext, times(1)).processSync();
        }
    }

    @Test
    public void policyOnlyRetriesOncePerRequest() {
        HttpPipelineCallContext context = createContext();
        HttpPipelineNextPolicy next = mock(HttpPipelineNextPolicy.class);
        HttpPipelineNextPolicy retryNext = mock(HttpPipelineNextPolicy.class);
        HttpResponse initialResponse = mock(HttpResponse.class);
        HttpResponse retriedResponse = mock(HttpResponse.class);

        when(sessionClient.createSessionAsync()).thenReturn(Mono.just(credentialWithToken(FIRST_TOKEN)))
            .thenReturn(Mono.just(credentialWithToken(SECOND_TOKEN)));
        when(next.clone()).thenReturn(retryNext);
        when(next.process()).thenReturn(Mono.just(initialResponse));
        when(retryNext.process()).thenReturn(Mono.just(retriedResponse));
        when(initialResponse.getStatusCode()).thenReturn(401);
        when(initialResponse.getHeaderValue(HttpHeaderName.WWW_AUTHENTICATE))
            .thenReturn("Session error=session_expired");
        when(retriedResponse.getStatusCode()).thenReturn(401);
        when(retriedResponse.getHeaderValue(HttpHeaderName.WWW_AUTHENTICATE))
            .thenReturn("Session error=session_expired");

        try (HttpResponse actualResponse = policy.process(context, next).block()) {
            assertEquals(retriedResponse, actualResponse);
            verify(retryNext, times(1)).process();
            verify(sessionClient, times(2)).createSessionAsync();
        }
    }

    @Test
    public void policyReturns403WithoutRetry() {
        HttpPipelineCallContext context = createContext();
        HttpPipelineNextPolicy next = mock(HttpPipelineNextPolicy.class);
        HttpPipelineNextPolicy retryNext = mock(HttpPipelineNextPolicy.class);
        HttpResponse forbiddenResponse = mock(HttpResponse.class);

        when(sessionClient.createSessionAsync()).thenReturn(Mono.just(credentialWithToken(FIRST_TOKEN)));
        when(next.clone()).thenReturn(retryNext);
        when(next.process()).thenReturn(Mono.just(forbiddenResponse));
        when(forbiddenResponse.getStatusCode()).thenReturn(403);

        try (HttpResponse actualResponse = policy.process(context, next).block()) {
            assertEquals(forbiddenResponse, actualResponse);
            verify(next, times(1)).process();
            verify(retryNext, times(0)).process();
            verify(forbiddenResponse, times(0)).close();
            verify(sessionClient, times(1)).createSessionAsync();
        }
    }

    @Test
    public void policyRetriesAny401WithNewSession() {
        HttpPipelineCallContext context = createContext();
        HttpPipelineNextPolicy next = mock(HttpPipelineNextPolicy.class);
        HttpPipelineNextPolicy retryNext = mock(HttpPipelineNextPolicy.class);
        HttpResponse unauthorizedResponse = mock(HttpResponse.class);
        HttpResponse retriedResponse = mock(HttpResponse.class);

        when(sessionClient.createSessionAsync()).thenReturn(Mono.just(credentialWithToken(FIRST_TOKEN)))
            .thenReturn(Mono.just(credentialWithToken(SECOND_TOKEN)));
        when(next.clone()).thenReturn(retryNext);
        when(next.process()).thenReturn(Mono.just(unauthorizedResponse));
        when(retryNext.process()).thenReturn(Mono.just(retriedResponse));
        when(unauthorizedResponse.getStatusCode()).thenReturn(401);
        when(retriedResponse.getStatusCode()).thenReturn(200);

        try (HttpResponse actualResponse = policy.process(context, next).block()) {
            assertEquals(retriedResponse, actualResponse);
            assertTrue(
                context.getHttpRequest().getHeaders().getValue("Authorization").startsWith("Session " + SECOND_TOKEN));
            verify(unauthorizedResponse, times(1)).close();
            verify(next, times(1)).process();
            verify(retryNext, times(1)).process();
            verify(sessionClient, times(2)).createSessionAsync();
        }
    }

    @Test
    public void policyFallsToBearerOn503SessionUnavailableAsync() {
        HttpPipelineCallContext context = createContext();
        HttpPipelineNextPolicy next = mock(HttpPipelineNextPolicy.class);
        HttpPipelineNextPolicy retryNext = mock(HttpPipelineNextPolicy.class);
        HttpResponse unavailableResponse = mock(HttpResponse.class);
        HttpResponse bearerResponse = mock(HttpResponse.class);

        when(sessionClient.createSessionAsync()).thenReturn(Mono.just(credentialWithToken(FIRST_TOKEN)));
        when(next.clone()).thenReturn(retryNext);
        when(next.process()).thenReturn(Mono.just(unavailableResponse));
        when(retryNext.process()).thenReturn(Mono.just(bearerResponse));
        when(unavailableResponse.getStatusCode()).thenReturn(503);
        when(unavailableResponse.getHeaderValue(HttpHeaderName.fromString("x-ms-error-code")))
            .thenReturn("SessionOperationsTemporarilyUnavailable");
        when(bearerResponse.getStatusCode()).thenReturn(200);

        try (HttpResponse actualResponse = policy.process(context, next).block()) {
            assertEquals(bearerResponse, actualResponse);
            verify(unavailableResponse, times(1)).close();
            // Verify that the bearer policy was invoked for fallback
            verify(bearerPolicy, times(1)).process(any(), any());
            // Authorization header should have been stripped so bearer policy can add its own
            String authHeader = context.getHttpRequest().getHeaders().getValue("Authorization");
            assertTrue(authHeader == null || !authHeader.startsWith("Session"),
                "Session auth should have been stripped but was: " + authHeader);
        }
    }

    @Test
    public void policyFallsToBearerOn400Async() {
        HttpPipelineCallContext context = createContext();
        HttpPipelineNextPolicy next = mock(HttpPipelineNextPolicy.class);
        HttpPipelineNextPolicy retryNext = mock(HttpPipelineNextPolicy.class);
        HttpResponse badRequestResponse = mock(HttpResponse.class);
        HttpResponse bearerResponse = mock(HttpResponse.class);

        when(sessionClient.createSessionAsync()).thenReturn(Mono.just(credentialWithToken(FIRST_TOKEN)));
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
    public void policyFallsToBearerOn503SessionUnavailableSync() {
        HttpPipelineCallContext context = createContext();
        HttpPipelineNextSyncPolicy next = mock(HttpPipelineNextSyncPolicy.class);
        HttpPipelineNextSyncPolicy retryNext = mock(HttpPipelineNextSyncPolicy.class);
        HttpResponse unavailableResponse = mock(HttpResponse.class);
        HttpResponse bearerResponse = mock(HttpResponse.class);

        when(sessionClient.createSessionSync()).thenReturn(credentialWithToken(FIRST_TOKEN));
        when(next.clone()).thenReturn(retryNext);
        when(next.processSync()).thenReturn(unavailableResponse);
        when(retryNext.processSync()).thenReturn(bearerResponse);
        when(unavailableResponse.getStatusCode()).thenReturn(503);
        when(unavailableResponse.getHeaderValue(HttpHeaderName.fromString("x-ms-error-code")))
            .thenReturn("SessionOperationsTemporarilyUnavailable");
        when(bearerResponse.getStatusCode()).thenReturn(200);

        try (HttpResponse actualResponse = policy.processSync(context, next)) {
            assertEquals(bearerResponse, actualResponse);
            verify(unavailableResponse, times(1)).close();
            // Verify that the bearer policy was invoked for fallback
            verify(bearerPolicy, times(1)).processSync(any(), any());
            String authHeader = context.getHttpRequest().getHeaders().getValue("Authorization");
            assertTrue(authHeader == null || !authHeader.startsWith("Session"),
                "Session auth should have been stripped but was: " + authHeader);
        }
    }

    @Test
    public void policyFallsToBearerOn400Sync() {
        HttpPipelineCallContext context = createContext();
        HttpPipelineNextSyncPolicy next = mock(HttpPipelineNextSyncPolicy.class);
        HttpPipelineNextSyncPolicy retryNext = mock(HttpPipelineNextSyncPolicy.class);
        HttpResponse badRequestResponse = mock(HttpResponse.class);
        HttpResponse bearerResponse = mock(HttpResponse.class);

        when(sessionClient.createSessionSync()).thenReturn(credentialWithToken(FIRST_TOKEN));
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

        when(sessionClient.createSessionAsync()).thenReturn(Mono.just(credentialWithToken(FIRST_TOKEN)));
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
    public void noneModeAlwaysPassesThrough() {
        SessionTokenCredentialPolicy nonePolicy = createPolicy(SessionMode.NONE);
        HttpPipelineCallContext context = createContext();
        HttpPipelineNextPolicy next = mock(HttpPipelineNextPolicy.class);
        HttpResponse response = mock(HttpResponse.class);

        when(next.process()).thenReturn(Mono.just(response));
        when(response.getStatusCode()).thenReturn(200);

        try (HttpResponse actualResponse = nonePolicy.process(context, next).block()) {
            assertEquals(response, actualResponse);
            // Verify bearer policy was invoked (session delegates to bearer in NONE mode)
            verify(bearerPolicy, times(1)).process(any(), any());
            verify(sessionClient, times(0)).createSessionAsync();
        }
    }

    @Test
    public void noneModeSyncAlwaysPassesThrough() {
        SessionTokenCredentialPolicy nonePolicy = createPolicy(SessionMode.NONE);
        HttpPipelineCallContext context = createContext();
        HttpPipelineNextSyncPolicy next = mock(HttpPipelineNextSyncPolicy.class);
        HttpResponse response = mock(HttpResponse.class);

        when(next.processSync()).thenReturn(response);
        when(response.getStatusCode()).thenReturn(200);

        try (HttpResponse actualResponse = nonePolicy.processSync(context, next)) {
            assertEquals(response, actualResponse);
            // Verify bearer policy was invoked (session delegates to bearer in NONE mode)
            verify(bearerPolicy, times(1)).processSync(any(), any());
            verify(sessionClient, times(0)).createSessionSync();
        }
    }

    @Test
    public void alwaysModeSignsFirstRequest() {
        // The default `policy` in setUp is ALWAYS — verify it signs the very first request
        HttpPipelineCallContext context = createContext();
        HttpPipelineNextPolicy next = mock(HttpPipelineNextPolicy.class);
        HttpResponse response = mock(HttpResponse.class);

        when(sessionClient.createSessionAsync()).thenReturn(Mono.just(credentialWithToken(FIRST_TOKEN)));
        when(next.clone()).thenReturn(next);
        when(next.process()).thenReturn(Mono.just(response));
        when(response.getStatusCode()).thenReturn(200);

        policy.process(context, next).block().close();

        assertTrue(context.getHttpRequest().getHeaders().getValue(authHeaderName).startsWith("Session "));
        verify(sessionClient, times(1)).createSessionAsync();
    }

    @Test
    public void autoModeResolvesToNoneAndAlwaysDelegatesToBearer() {
        SessionTokenCredentialPolicy autoPolicy = createPolicy(SessionMode.AUTO);
        HttpResponse response = mock(HttpResponse.class);

        when(response.getStatusCode()).thenReturn(200);

        // AUTO resolves to NONE, so all requests should delegate to bearer
        HttpPipelineCallContext context1 = createContext();
        HttpPipelineNextPolicy next1 = mock(HttpPipelineNextPolicy.class);
        when(next1.process()).thenReturn(Mono.just(response));

        try (HttpResponse actual1 = autoPolicy.process(context1, next1).block()) {
            assertEquals(response, actual1);
            verify(bearerPolicy, times(1)).process(any(), any());
            verify(sessionClient, times(0)).createSessionAsync();
        }

        // Second GetBlob also delegates to bearer (AUTO == NONE, no session ever)
        HttpPipelineCallContext context2 = createContext();
        HttpPipelineNextPolicy next2 = mock(HttpPipelineNextPolicy.class);
        when(next2.process()).thenReturn(Mono.just(response));

        try (HttpResponse actual2 = autoPolicy.process(context2, next2).block()) {
            assertEquals(response, actual2);
            verify(bearerPolicy, times(2)).process(any(), any());
            verify(sessionClient, times(0)).createSessionAsync();
        }
    }

    @Test
    public void autoModeSyncResolvesToNoneAndAlwaysDelegatesToBearer() {
        SessionTokenCredentialPolicy autoPolicy = createPolicy(SessionMode.AUTO);
        HttpResponse response = mock(HttpResponse.class);

        when(response.getStatusCode()).thenReturn(200);

        // AUTO resolves to NONE, so all requests should delegate to bearer
        HttpPipelineCallContext context1 = createContext();
        HttpPipelineNextSyncPolicy next1 = mock(HttpPipelineNextSyncPolicy.class);
        when(next1.processSync()).thenReturn(response);

        try (HttpResponse actual1 = autoPolicy.processSync(context1, next1)) {
            assertEquals(response, actual1);
            verify(bearerPolicy, times(1)).processSync(any(), any());
            verify(sessionClient, times(0)).createSessionSync();
        }

        HttpPipelineCallContext context2 = createContext();
        HttpPipelineNextSyncPolicy next2 = mock(HttpPipelineNextSyncPolicy.class);
        when(next2.processSync()).thenReturn(response);

        try (HttpResponse actual2 = autoPolicy.processSync(context2, next2)) {
            assertEquals(response, actual2);
            verify(bearerPolicy, times(2)).processSync(any(), any());
            verify(sessionClient, times(0)).createSessionSync();
        }
    }

    @Test
    public void sessionExpiringHintForcesBackgroundRefreshEvenWhenTimerNotDue() {
        HttpPipelineCallContext context = createContext();
        HttpPipelineNextPolicy next = mock(HttpPipelineNextPolicy.class);
        HttpResponse response = mock(HttpResponse.class);

        // Fresh session far from expiry, so the client's own jittered refresh timer is NOT due.
        when(sessionClient.createSessionAsync()).thenReturn(Mono.just(credentialWithToken(FIRST_TOKEN)))
            .thenReturn(Mono.just(credentialWithToken(SECOND_TOKEN)));
        when(next.clone()).thenReturn(next);
        when(next.process()).thenReturn(Mono.just(response));
        when(response.getStatusCode()).thenReturn(200);
        // The service signals (via x-ms-auth-info: session_expiring) that this session is about to stop
        // being honored — for example, just before its network-context binding rotates.
        when(response.getHeaderValue(HttpHeaderName.fromString("x-ms-auth-info"))).thenReturn("session_expiring");

        policy.process(context, next).block().close();

        // The service hint must trigger a proactive background refresh (a second createSession), even
        // though the client's own refresh timer had not yet elapsed. Dropping the hint here is what
        // previously let the session be used past the rotation boundary, surfacing as a 401
        // "session_token_invalid" (network context mismatch).
        verify(sessionClient, times(2)).createSessionAsync();
    }

    @Test
    public void noSessionExpiringHintDoesNotForceBackgroundRefresh() {
        HttpPipelineCallContext context = createContext();
        HttpPipelineNextPolicy next = mock(HttpPipelineNextPolicy.class);
        HttpResponse response = mock(HttpResponse.class);

        when(sessionClient.createSessionAsync()).thenReturn(Mono.just(credentialWithToken(FIRST_TOKEN)))
            .thenReturn(Mono.just(credentialWithToken(SECOND_TOKEN)));
        when(next.clone()).thenReturn(next);
        when(next.process()).thenReturn(Mono.just(response));
        when(response.getStatusCode()).thenReturn(200);
        // No x-ms-auth-info hint on the response.
        when(response.getHeaderValue(HttpHeaderName.fromString("x-ms-auth-info"))).thenReturn(null);

        policy.process(context, next).block().close();

        // Without the hint and with a fresh session, only the initial session is created.
        verify(sessionClient, times(1)).createSessionAsync();
    }

    private SessionTokenCredentialPolicy createPolicy(SessionMode mode) {
        SessionOptions options = new SessionOptions().setSessionMode(mode).setContainerName("mycontainer");
        return new SessionTokenCredentialPolicy(bearerPolicy, new StorageSessionCredentialCache(sessionClient),
            options);
    }

    private static StorageSessionCredential credentialWithToken(String token) {
        return credentialWithToken(token, OffsetDateTime.now().plusHours(1));
    }

    private static StorageSessionCredential credentialWithToken(String token, OffsetDateTime expiration) {
        return new StorageSessionCredential(token, SessionTestHelper.TEST_SESSION_KEY, expiration,
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

        when(sessionClient.createSessionAsync()).thenReturn(Mono.just(credentialWithToken(FIRST_TOKEN)));
        when(next.clone()).thenReturn(next);
        when(next.process()).thenReturn(Mono.just(response));
        when(response.getStatusCode()).thenReturn(200);

        policy.process(context, next).block().close();

        assertTrue(context.getHttpRequest().getHeaders().getValue(authHeaderName).startsWith("Session "),
            "GetBlob request should be signed with session auth");
    }

    @Test
    public void getBlobRequestProducesWellFormedSessionAuthHeader() {
        StorageSessionCredential cred = credentialWithToken(FIRST_TOKEN);
        HttpRequest request
            = new HttpRequest(HttpMethod.GET, "https://myaccount.blob.core.windows.net/mycontainer/myblob");
        request.getHeaders()
            .set(HttpHeaderName.fromString("x-ms-version"), "2025-01-05")
            .set(HttpHeaderName.fromString("x-ms-client-request-id"), "11111111-2222-3333-4444-555555555555")
            .set(HttpHeaderName.RANGE, "bytes=0-1023");

        HttpPipelineCallContext context = createContextForRequest(request);
        HttpPipelineNextPolicy next = mock(HttpPipelineNextPolicy.class);
        HttpResponse response = mock(HttpResponse.class);

        when(sessionClient.createSessionAsync()).thenReturn(Mono.just(cred));
        when(next.clone()).thenReturn(next);
        when(next.process()).thenReturn(Mono.just(response));
        when(response.getStatusCode()).thenReturn(200);

        policy.process(context, next).block().close();

        // The policy must delegate signing to StorageSessionCredential, producing a Session-scheme
        // Authorization header of the form `Session <token>:<base64-signature>`. End-to-end signature
        // correctness against the live service is covered by ContainerApiTests.downloadBlobOverSessionAuth.
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
        credentialWithToken(FIRST_TOKEN).signRequest(withCl0);
        String sigWithCl0 = extractSignature(withCl0.getHeaders().getValue(authHeaderName));

        HttpRequest withoutCl
            = new HttpRequest(HttpMethod.GET, "https://myaccount.blob.core.windows.net/mycontainer/myblob");
        withoutCl.getHeaders()
            .set(HttpHeaderName.fromString("x-ms-version"), "2025-01-05")
            .set(HttpHeaderName.fromString("x-ms-client-request-id"), "11111111-2222-3333-4444-555555555555")
            .set(HttpHeaderName.RANGE, "bytes=0-1023")
            .set(HttpHeaderName.fromString("x-ms-date"), pinnedDate);
        credentialWithToken(FIRST_TOKEN).signRequest(withoutCl);
        String sigWithoutCl = extractSignature(withoutCl.getHeaders().getValue(authHeaderName));

        assertEquals(sigWithCl0, sigWithoutCl,
            "Session signature must normalize Content-Length: 0 to empty: signing with "
                + "Content-Length: 0 must match signing without Content-Length");
    }

    private static String extractSignature(String authHeader) {
        return authHeader.substring(authHeader.indexOf(':') + 1);
    }

    @Test
    public void putBlobRequestSkipsSessionAuth() {
        HttpPipelineCallContext context = createContextForRequest(
            new HttpRequest(HttpMethod.PUT, "https://myaccount.blob.core.windows.net/mycontainer/myblob"));
        HttpPipelineNextPolicy next = mock(HttpPipelineNextPolicy.class);
        HttpResponse response = mock(HttpResponse.class);

        when(next.process()).thenReturn(Mono.just(response));

        policy.process(context, next).block().close();

        // Non-GetBlob requests delegate to bearer policy instead of session auth
        verify(bearerPolicy, times(1)).process(any(), any());
        verify(sessionClient, times(0)).createSessionAsync();
    }

    @Test
    public void listBlobsRequestSkipsSessionAuth() {
        HttpPipelineCallContext context
            = createContextForUrl("https://myaccount.blob.core.windows.net/mycontainer?restype=container&comp=list");
        HttpPipelineNextPolicy next = mock(HttpPipelineNextPolicy.class);
        HttpResponse response = mock(HttpResponse.class);

        when(next.process()).thenReturn(Mono.just(response));

        policy.process(context, next).block().close();

        // ListBlobs requests delegate to bearer policy instead of session auth
        verify(bearerPolicy, times(1)).process(any(), any());
        verify(sessionClient, times(0)).createSessionAsync();
    }

    @Test
    public void getBlobPropertiesRequestSkipsSessionAuth() {
        HttpPipelineCallContext context
            = createContextForUrl("https://myaccount.blob.core.windows.net/mycontainer/myblob?comp=metadata");
        HttpPipelineNextPolicy next = mock(HttpPipelineNextPolicy.class);
        HttpResponse response = mock(HttpResponse.class);

        when(next.process()).thenReturn(Mono.just(response));

        policy.process(context, next).block().close();

        // GetBlobProperties (comp=metadata) delegates to bearer policy instead of session auth
        verify(bearerPolicy, times(1)).process(any(), any());
        verify(sessionClient, times(0)).createSessionAsync();
    }

    @Test
    public void getBlobWithSnapshotUsesSessionAuth() {
        HttpPipelineCallContext context = createContextForUrl(
            "https://myaccount.blob.core.windows.net/mycontainer/myblob?snapshot=2021-01-01T00:00:00Z");
        HttpPipelineNextPolicy next = mock(HttpPipelineNextPolicy.class);
        HttpResponse response = mock(HttpResponse.class);

        when(sessionClient.createSessionAsync()).thenReturn(Mono.just(credentialWithToken(FIRST_TOKEN)));
        when(next.clone()).thenReturn(next);
        when(next.process()).thenReturn(Mono.just(response));
        when(response.getStatusCode()).thenReturn(200);

        policy.process(context, next).block().close();

        assertTrue(context.getHttpRequest().getHeaders().getValue(authHeaderName).startsWith("Session "),
            "GetBlob with snapshot should still use session auth");
    }

    @Test
    public void containerLevelGetRequestSkipsSessionAuth() {
        HttpPipelineCallContext context
            = createContextForUrl("https://myaccount.blob.core.windows.net/mycontainer?restype=container");
        HttpPipelineNextPolicy next = mock(HttpPipelineNextPolicy.class);
        HttpResponse response = mock(HttpResponse.class);

        when(next.process()).thenReturn(Mono.just(response));

        policy.process(context, next).block().close();

        // Container-level GET (restype=container) delegates to bearer policy instead of session auth
        verify(bearerPolicy, times(1)).process(any(), any());
        verify(sessionClient, times(0)).createSessionAsync();
    }

    @Test
    public void autoModeAlwaysDelegatesToBearerEvenForGetBlobRequests() {
        SessionTokenCredentialPolicy autoPolicy = createPolicy(SessionMode.AUTO);
        HttpResponse response = mock(HttpResponse.class);
        when(response.getStatusCode()).thenReturn(200);

        // PUT request — delegates to bearer (AUTO == NONE)
        HttpPipelineCallContext putContext = createContextForRequest(
            new HttpRequest(HttpMethod.PUT, "https://myaccount.blob.core.windows.net/mycontainer/myblob"));
        HttpPipelineNextPolicy putNext = mock(HttpPipelineNextPolicy.class);
        when(putNext.process()).thenReturn(Mono.just(response));
        autoPolicy.process(putContext, putNext).block().close();

        // GET blob — also delegates to bearer (AUTO == NONE)
        HttpPipelineCallContext getContext
            = createContextForUrl("https://myaccount.blob.core.windows.net/mycontainer/myblob");
        HttpPipelineNextPolicy getNext = mock(HttpPipelineNextPolicy.class);
        when(getNext.process()).thenReturn(Mono.just(response));
        Objects.requireNonNull(autoPolicy.process(getContext, getNext).block()).close();

        verify(bearerPolicy, times(2)).process(any(), any());
        verify(sessionClient, times(0)).createSessionAsync();
    }

    @Test
    public void singleSpecifiedContainerModeNonGetBlobSkipsSession() {
        HttpPipelineCallContext context = createContextForRequest(
            new HttpRequest(HttpMethod.DELETE, "https://myaccount.blob.core.windows.net/mycontainer/myblob"));
        HttpPipelineNextPolicy next = mock(HttpPipelineNextPolicy.class);
        HttpResponse response = mock(HttpResponse.class);

        when(next.process()).thenReturn(Mono.just(response));

        Objects.requireNonNull(policy.process(context, next).block()).close();

        // SINGLE_SPECIFIED_CONTAINER mode non-GetBlob requests delegate to bearer instead of session auth
        verify(bearerPolicy, times(1)).process(any(), any());
        verify(sessionClient, times(0)).createSessionAsync();
    }

    @Test
    public void ipStyleEndpointGetBlobUsesSessionAuth() {
        HttpPipelineCallContext context
            = createContextForUrl("https://127.0.0.1:10000/devstoreaccount1/mycontainer/myblob");
        HttpPipelineNextPolicy next = mock(HttpPipelineNextPolicy.class);
        HttpResponse response = mock(HttpResponse.class);

        when(sessionClient.createSessionAsync()).thenReturn(Mono.just(credentialWithToken(FIRST_TOKEN)));
        when(next.clone()).thenReturn(next);
        when(next.process()).thenReturn(Mono.just(response));
        when(response.getStatusCode()).thenReturn(200);

        Objects.requireNonNull(policy.process(context, next).block()).close();

        assertTrue(context.getHttpRequest().getHeaders().getValue(authHeaderName).startsWith("Session "),
            "GetBlob on IP-style endpoint should use session auth");
    }

    // endregion
}
