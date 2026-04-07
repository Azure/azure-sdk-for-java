// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SessionProviderTest {

    private static final String FIRST_TOKEN = "first-session-token";
    private static final String SECOND_TOKEN = "second-session-token-should-not-appear";

    private BlobSessionClient sessionClient;
    private SessionProvider provider;

    @BeforeEach
    public void beforeEach() {
        sessionClient = mock(BlobSessionClient.class);
        provider = new SessionProvider(sessionClient);
    }

    private static StorageSessionCredential credentialWithToken(String token) {
        return new StorageSessionCredential(token, SessionTestHelper.TEST_SESSION_KEY,
            OffsetDateTime.now().plusHours(1), SessionTestHelper.TEST_ACCOUNT_NAME);
    }

    // ---- Async tests ----

    @Test
    public void providerCreatesSessionOnFirstCall() {
        when(sessionClient.createSessionAsync()).thenReturn(Mono.just(credentialWithToken(FIRST_TOKEN)));

        StorageSessionCredential credential = provider.getOrCreateSessionAsync().block();

        assertNotNull(credential);
        assertEquals(FIRST_TOKEN, credential.getSessionToken());
        assertEquals(SessionTestHelper.TEST_SESSION_KEY, credential.getSessionKey());
        verify(sessionClient, times(1)).createSessionAsync();
    }

    @Test
    public void providerReturnsCachedSessionOnSubsequentCalls() {
        // First call returns FIRST_TOKEN, any leaked second call would return SECOND_TOKEN
        when(sessionClient.createSessionAsync()).thenReturn(Mono.just(credentialWithToken(FIRST_TOKEN)))
            .thenReturn(Mono.just(credentialWithToken(SECOND_TOKEN)));

        // Fire 5 parallel async calls — all should share one CreateSession
        List<StorageSessionCredential> results
            = Flux.range(0, 5).flatMap(i -> provider.getOrCreateSessionAsync()).collectList().block();

        assertNotNull(results);
        assertEquals(5, results.size());
        // All results must have FIRST_TOKEN — if any has SECOND_TOKEN, dedup is broken
        results.forEach(cred -> assertEquals(FIRST_TOKEN, cred.getSessionToken()));
        verify(sessionClient, times(1)).createSessionAsync();
    }

    // ---- Sync tests ----

    @Test
    public void providerCreatesSessionSyncOnFirstCall() {
        when(sessionClient.createSessionSync()).thenReturn(credentialWithToken(FIRST_TOKEN));

        StorageSessionCredential credential = provider.getOrCreateSessionSync();

        assertNotNull(credential);
        assertEquals(FIRST_TOKEN, credential.getSessionToken());
        verify(sessionClient, times(1)).createSessionSync();
    }

    @Test
    public void concurrentSyncCallsOnlyCreateOneSession() throws Exception {
        // First call returns FIRST_TOKEN (with delay), second would return SECOND_TOKEN
        when(sessionClient.createSessionSync()).thenAnswer(invocation -> {
            Thread.sleep(100);
            return credentialWithToken(FIRST_TOKEN);
        }).thenReturn(credentialWithToken(SECOND_TOKEN));

        int threadCount = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        try {
            List<Callable<StorageSessionCredential>> tasks = IntStream.range(0, threadCount)
                .mapToObj(i -> (Callable<StorageSessionCredential>) provider::getOrCreateSessionSync)
                .collect(Collectors.toList());

            List<Future<StorageSessionCredential>> futures = executor.invokeAll(tasks);

            for (Future<StorageSessionCredential> future : futures) {
                StorageSessionCredential credential = future.get();
                assertNotNull(credential);
                assertEquals(FIRST_TOKEN, credential.getSessionToken());
            }

            verify(sessionClient, times(1)).createSessionSync();
        } finally {
            executor.shutdownNow();
        }
    }
}
