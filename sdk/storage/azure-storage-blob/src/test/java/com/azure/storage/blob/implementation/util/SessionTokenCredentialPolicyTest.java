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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
    private SessionTokenCredentialPolicy policy;

    @BeforeEach
    public void beforeEach() {
        sessionClient = mock(BlobSessionClient.class);
        policy = createPolicy(SessionMode.ALWAYS);
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
        when(sessionClient.createSessionAsync()).thenReturn(Mono.just(refreshed));

        StorageSessionCredential initial = policy.getValidSessionSync();
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

        HttpResponse actualResponse = policy.process(context, next).block();

        assertEquals(response, actualResponse);
        assertTrue(context.getHttpRequest().getHeaders().getValue("Authorization").startsWith("Session " + FIRST_TOKEN),
            "Expected request to be signed with a session credential.");
        verify(next, times(1)).process();
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

        HttpResponse actualResponse = policy.process(context, next).block();

        assertEquals(retriedResponse, actualResponse);
        assertTrue(
            context.getHttpRequest().getHeaders().getValue("Authorization").startsWith("Session " + SECOND_TOKEN));
        verify(initialResponse, times(1)).close();
        verify(next, times(1)).process();
        verify(retryNext, times(1)).process();
        verify(sessionClient, times(2)).createSessionAsync();
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

        HttpResponse actualResponse = policy.processSync(context, next);

        assertEquals(retriedResponse, actualResponse);
        assertTrue(
            context.getHttpRequest().getHeaders().getValue("Authorization").startsWith("Session " + SECOND_TOKEN));
        verify(initialResponse, times(1)).close();
        verify(next, times(1)).processSync();
        verify(retryNext, times(1)).processSync();
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

        HttpResponse actualResponse = policy.process(context, next).block();

        assertEquals(retriedResponse, actualResponse);
        verify(retryNext, times(1)).process();
        verify(sessionClient, times(2)).createSessionAsync();
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

        HttpResponse actualResponse = policy.process(context, next).block();

        assertEquals(forbiddenResponse, actualResponse);
        verify(next, times(1)).process();
        verify(retryNext, times(0)).process();
        verify(forbiddenResponse, times(0)).close();
        verify(sessionClient, times(1)).createSessionAsync();
    }

    @Test
    public void policyReturnsSessionTokenInvalidWithoutRetryButInvalidatesSession() {
        HttpPipelineCallContext context = createContext();
        HttpPipelineNextPolicy next = mock(HttpPipelineNextPolicy.class);
        HttpPipelineNextPolicy retryNext = mock(HttpPipelineNextPolicy.class);
        HttpResponse invalidResponse = mock(HttpResponse.class);

        when(sessionClient.createSessionAsync()).thenReturn(Mono.just(credentialWithToken(FIRST_TOKEN)))
            .thenReturn(Mono.just(credentialWithToken(SECOND_TOKEN)));
        when(next.clone()).thenReturn(retryNext);
        when(next.process()).thenReturn(Mono.just(invalidResponse));
        when(invalidResponse.getStatusCode()).thenReturn(401);
        when(invalidResponse.getHeaderValue(HttpHeaderName.WWW_AUTHENTICATE))
            .thenReturn("Session error=session_token_invalid");

        HttpResponse actualResponse = policy.process(context, next).block();

        // Returns the 401 as-is — no retry
        assertEquals(invalidResponse, actualResponse);
        verify(next, times(1)).process();
        verify(retryNext, times(0)).process();
        verify(invalidResponse, times(0)).close();

        // But the session was invalidated so the next request gets a fresh session
        StorageSessionCredential nextSession = policy.getValidSessionAsync().block();
        assertEquals(SECOND_TOKEN, nextSession.getSessionToken());
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

        HttpResponse actualResponse = policy.process(context, next).block();

        assertEquals(bearerResponse, actualResponse);
        verify(unavailableResponse, times(1)).close();
        verify(retryNext, times(1)).process();
        // Authorization header should have been stripped so bearer policy can add its own
        String authHeader = context.getHttpRequest().getHeaders().getValue("Authorization");
        assertTrue(authHeader == null || !authHeader.startsWith("Session"),
            "Session auth should have been stripped but was: " + authHeader);
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

        HttpResponse actualResponse = policy.processSync(context, next);

        assertEquals(bearerResponse, actualResponse);
        verify(unavailableResponse, times(1)).close();
        verify(retryNext, times(1)).processSync();
        String authHeader = context.getHttpRequest().getHeaders().getValue("Authorization");
        assertTrue(authHeader == null || !authHeader.startsWith("Session"),
            "Session auth should have been stripped but was: " + authHeader);
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

        HttpResponse actualResponse = policy.process(context, next).block();

        // ServerBusy 503 is not session-specific — return as-is for retry policy to handle
        assertEquals(busyResponse, actualResponse);
        verify(retryNext, times(0)).process();
        verify(busyResponse, times(0)).close();
    }

    @Test
    public void noneModeAlwaysPassesThrough() {
        SessionTokenCredentialPolicy nonePolicy = createPolicy(SessionMode.NONE);
        HttpPipelineCallContext context = createContext();
        HttpPipelineNextPolicy next = mock(HttpPipelineNextPolicy.class);
        HttpResponse response = mock(HttpResponse.class);

        when(next.process()).thenReturn(Mono.just(response));
        when(response.getStatusCode()).thenReturn(200);

        HttpResponse actualResponse = nonePolicy.process(context, next).block();

        assertEquals(response, actualResponse);
        verify(next, times(1)).process();
        verify(sessionClient, times(0)).createSessionAsync();
        assertNull(context.getHttpRequest().getHeaders().getValue(authHeaderName));
    }

    @Test
    public void noneModeSyncAlwaysPassesThrough() {
        SessionTokenCredentialPolicy nonePolicy = createPolicy(SessionMode.NONE);
        HttpPipelineCallContext context = createContext();
        HttpPipelineNextSyncPolicy next = mock(HttpPipelineNextSyncPolicy.class);
        HttpResponse response = mock(HttpResponse.class);

        when(next.processSync()).thenReturn(response);
        when(response.getStatusCode()).thenReturn(200);

        HttpResponse actualResponse = nonePolicy.processSync(context, next);

        assertEquals(response, actualResponse);
        verify(next, times(1)).processSync();
        verify(sessionClient, times(0)).createSessionSync();
        assertNull(context.getHttpRequest().getHeaders().getValue(authHeaderName));
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

        policy.process(context, next).block();

        assertTrue(context.getHttpRequest().getHeaders().getValue(authHeaderName).startsWith("Session "));
        verify(sessionClient, times(1)).createSessionAsync();
    }

    @Test
    public void autoModePassesThroughFirstRequestThenSignsSecond() {
        SessionTokenCredentialPolicy autoPolicy = createPolicy(SessionMode.AUTO);
        HttpResponse firstResponse = mock(HttpResponse.class);
        HttpResponse secondResponse = mock(HttpResponse.class);

        when(sessionClient.createSessionAsync()).thenReturn(Mono.just(credentialWithToken(FIRST_TOKEN)));
        when(firstResponse.getStatusCode()).thenReturn(200);
        when(secondResponse.getStatusCode()).thenReturn(200);

        // First request — should pass through without session
        HttpPipelineCallContext context1 = createContext();
        HttpPipelineNextPolicy next1 = mock(HttpPipelineNextPolicy.class);
        when(next1.process()).thenReturn(Mono.just(firstResponse));

        HttpResponse actual1 = autoPolicy.process(context1, next1).block();

        assertEquals(firstResponse, actual1);
        verify(sessionClient, times(0)).createSessionAsync();
        assertNull(context1.getHttpRequest().getHeaders().getValue(authHeaderName));

        // Second request — should use session
        HttpPipelineCallContext context2 = createContext();
        HttpPipelineNextPolicy next2 = mock(HttpPipelineNextPolicy.class);
        when(next2.clone()).thenReturn(next2);
        when(next2.process()).thenReturn(Mono.just(secondResponse));

        HttpResponse actual2 = autoPolicy.process(context2, next2).block();

        assertEquals(secondResponse, actual2);
        verify(sessionClient, times(1)).createSessionAsync();
        assertTrue(context2.getHttpRequest().getHeaders().getValue(authHeaderName).startsWith("Session "));
    }

    @Test
    public void autoModeSyncPassesThroughFirstRequestThenSignsSecond() {
        SessionTokenCredentialPolicy autoPolicy = createPolicy(SessionMode.AUTO);
        HttpResponse firstResponse = mock(HttpResponse.class);
        HttpResponse secondResponse = mock(HttpResponse.class);

        when(sessionClient.createSessionSync()).thenReturn(credentialWithToken(FIRST_TOKEN));
        when(firstResponse.getStatusCode()).thenReturn(200);
        when(secondResponse.getStatusCode()).thenReturn(200);

        // First request — pass through
        HttpPipelineCallContext context1 = createContext();
        HttpPipelineNextSyncPolicy next1 = mock(HttpPipelineNextSyncPolicy.class);
        when(next1.processSync()).thenReturn(firstResponse);

        HttpResponse actual1 = autoPolicy.processSync(context1, next1);

        assertEquals(firstResponse, actual1);
        verify(sessionClient, times(0)).createSessionSync();
        assertNull(context1.getHttpRequest().getHeaders().getValue(authHeaderName));

        // Second request — session signed
        HttpPipelineCallContext context2 = createContext();
        HttpPipelineNextSyncPolicy next2 = mock(HttpPipelineNextSyncPolicy.class);
        when(next2.clone()).thenReturn(next2);
        when(next2.processSync()).thenReturn(secondResponse);

        HttpResponse actual2 = autoPolicy.processSync(context2, next2);

        assertEquals(secondResponse, actual2);
        verify(sessionClient, times(1)).createSessionSync();
        assertTrue(context2.getHttpRequest().getHeaders().getValue(authHeaderName).startsWith("Session "));
    }

    private SessionTokenCredentialPolicy createPolicy(SessionMode mode) {
        return new SessionTokenCredentialPolicy(new StorageSessionCredentialCache(sessionClient), mode);
    }

    private static StorageSessionCredential credentialWithToken(String token) {
        return credentialWithToken(token, OffsetDateTime.now().plusHours(1));
    }

    private static StorageSessionCredential credentialWithToken(String token, OffsetDateTime expiration) {
        return new StorageSessionCredential(token, SessionTestHelper.TEST_SESSION_KEY, expiration,
            SessionTestHelper.TEST_ACCOUNT_NAME);
    }

    private static HttpPipelineCallContext createContext() {
        HttpPipelineCallContext context = mock(HttpPipelineCallContext.class);
        HttpRequest request
            = new HttpRequest(HttpMethod.GET, "https://myaccount.blob.core.windows.net/mycontainer/myblob");
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
}
