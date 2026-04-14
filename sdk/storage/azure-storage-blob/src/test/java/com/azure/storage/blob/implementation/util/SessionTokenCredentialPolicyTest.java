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

    private BlobSessionClient sessionClient;
    private SessionTokenCredentialPolicy policy;

    @BeforeEach
    public void beforeEach() {
        sessionClient = mock(BlobSessionClient.class);
        policy = new SessionTokenCredentialPolicy(sessionClient);
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
        HttpPipelineCallContext context = createContext("https://myaccount.blob.core.windows.net/mycontainer/myblob");
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
        HttpPipelineCallContext context = createContext("https://myaccount.blob.core.windows.net/mycontainer/myblob");
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
        HttpPipelineCallContext context = createContext("https://myaccount.blob.core.windows.net/mycontainer/myblob");
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
            .thenReturn("Session error=session_token_invalid");
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
        HttpPipelineCallContext context = createContext("https://myaccount.blob.core.windows.net/mycontainer/myblob");
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

    private static StorageSessionCredential credentialWithToken(String token) {
        return credentialWithToken(token, OffsetDateTime.now().plusHours(1));
    }

    private static StorageSessionCredential credentialWithToken(String token, OffsetDateTime expiration) {
        return new StorageSessionCredential(token, SessionTestHelper.TEST_SESSION_KEY, expiration,
            SessionTestHelper.TEST_ACCOUNT_NAME);
    }

    private static HttpPipelineCallContext createContext(String url) {
        HttpPipelineCallContext context = mock(HttpPipelineCallContext.class);
        HttpRequest request = new HttpRequest(HttpMethod.GET, url);
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
