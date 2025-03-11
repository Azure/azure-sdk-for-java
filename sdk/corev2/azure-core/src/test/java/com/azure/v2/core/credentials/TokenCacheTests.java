// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.core.credentials;

import com.azure.v2.core.implementation.AccessTokenCache;
import io.clientcore.core.credentials.oauth.AccessToken;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;

import static java.lang.Thread.sleep;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TokenCacheTests {

    @Test
    public void testEachSyncThreadRefreshesToken() {
        AtomicLong refreshes = new AtomicLong(0);

        TokenCredential dummyCred = request -> {
            refreshes.incrementAndGet();
            // Token acquisition time grows in 1 sec, 2 sec... To make sure only one token acquisition is run
            return incrementalRemoteGetTokenAsync(new AtomicInteger(1));
        };

        AccessTokenCache cache = new AccessTokenCache(dummyCred);

        IntStream.range(0, 5).parallel().flatMap(integer -> {
            cache.getToken(new TokenRequestContext().addScopes("test" + integer + "/.default"), true);
            return IntStream.of(integer);
        }).forEach(ignored -> {
        });

        // Ensure that refresh attempts are made.
        assertEquals(5, refreshes.get());
    }

    @Test
    public void testOnlyOneSyncThreadRefreshesToken() {
        AtomicLong refreshes = new AtomicLong(0);

        TokenCredential dummyCred = request -> {
            refreshes.incrementAndGet();
            return incrementalRemoteGetTokenAsync(new AtomicInteger(1));
        };

        // Token acquisition time grows in 1 sec, 2 sec... To make sure only one token acquisition is run
        AccessTokenCache cache = new AccessTokenCache(dummyCred);

        IntStream.range(1, 10).parallel().flatMap(integer -> {
            cache.getToken(new TokenRequestContext(), false);
            return IntStream.of(integer);
        }).forEach(ignored -> {
        });

        // Ensure that only one refresh attempt is made.
        assertEquals(1, refreshes.get());
    }

    @Test
    public void testRefreshOnFlow() throws InterruptedException {
        AtomicLong refreshes = new AtomicLong(0);

        TokenCredential dummyCred = request -> {
            refreshes.incrementAndGet();
            return new Token("testToken", 200000, 33000);
        };

        AccessTokenCache cache = new AccessTokenCache(dummyCred);

        cache.getToken(new TokenRequestContext(), false);

        sleep(40000);

        cache.getToken(new TokenRequestContext(), false);

        // Ensure refresh is made after refreshOn duration.
        assertEquals(2, refreshes.get());
    }

    // First token takes latency seconds, and adds 1 sec every subsequent call
    private AccessToken incrementalRemoteGetTokenAsync(AtomicInteger latency) {
        try {
            Thread.sleep(latency.getAndIncrement());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return new Token(Integer.toString(ThreadLocalRandom.current().nextInt(100)));
    }

    public static class Token extends AccessToken {
        Token(String token) {
            this(token, 5000);
        }

        Token(String token, long validityInMillis) {
            super(token, OffsetDateTime.now().plus(Duration.ofMillis(validityInMillis)),
                OffsetDateTime.now().plusSeconds(5));
        }

        Token(String token, long validityInMillis, long refreshInMillis) {
            super(token, OffsetDateTime.now().plus(Duration.ofMillis(validityInMillis)),
                OffsetDateTime.now().plus(Duration.ofMillis(refreshInMillis)));
        }
    }
}
