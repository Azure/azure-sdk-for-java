// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.common;

import com.azure.communication.common.implementation.JwtTokenMocker;
import com.azure.communication.common.implementation.TokenParser;
import com.azure.core.credential.AccessToken;
import com.azure.core.util.logging.ClientLogger;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

public class CommunicationTokenCredentialTests {
    private final JwtTokenMocker tokenMocker = new JwtTokenMocker();
    private static final int DEFAULT_EXPIRING_OFFSET_SECONDS = 601;

    @Test
    public void constructWithValidTokenWithoutFresher() throws InterruptedException, ExecutionException, IOException {
        String tokenStr = tokenMocker.generateRawToken("resourceId", "userIdentity", 3 * 60);
        CommunicationTokenCredential tokenCredential = new CommunicationTokenCredential(tokenStr);
        StepVerifier.create(tokenCredential.getToken()).assertNext(token -> {
            assertFalse(token.isExpired(),
                    "Statically cached AccessToken should not expire when expiry is set to 3 minutes later");
            assertEquals(tokenStr, token.getToken());
        }).verifyComplete();
        tokenCredential.close();
    }

    @Test
    public void constructWithInvalidTokenStringShouldThrow() {
        String tokenStr = "IAmNotAToken";
        assertThrows(Exception.class, () -> {
            new CommunicationTokenCredential(tokenStr);
        }, "Should throw on invalid token");
    }

    @Test
    public void constructWithExpiredTokenWithoutRefresher()
            throws InterruptedException, ExecutionException, IOException {
        String tokenStr = tokenMocker.generateRawToken("resourceId", "userIdentity", -3 * 60);
        CommunicationTokenCredential tokenCredential = new CommunicationTokenCredential(tokenStr);
        StepVerifier.create(tokenCredential.getToken()).assertNext(token -> {
            assertTrue(token.isExpired(),
                    "Statically cached AccessToken should expire when expiry is set to 3 minutes before");
        }).verifyComplete();
        tokenCredential.close();
    }

    @Test
    public void constructCommunicationTokenRefreshOptionsWithoutRefresherShouldThrowException() {
        assertThrows(Exception.class, () -> {
            new CommunicationTokenRefreshOptions(null);
        }, "'tokenRefresher' cannot be null.");
        assertThrows(Exception.class, () -> {
            new CommunicationTokenRefreshOptions(null, false);
        }, "'tokenRefresher' cannot be null.");
        assertThrows(Exception.class, () -> {
            new CommunicationTokenRefreshOptions(null, true, "token");
        }, "'tokenRefresher' cannot be null.");
    }

    class MockImmediateRefresher implements Supplier<String> {
        private int numCalls = 0;
        private String refreshedToken;
        private Runnable onCallReturn;

        public int numCalls() {
            return numCalls;
        }

        public void setOnCallReturn(Runnable onCallReturn) {
            this.onCallReturn = onCallReturn;
        }

        public void resetCallCount() {
            numCalls = 0;
        }

        public void setRefreshedToken(String refreshedToken) {
            this.refreshedToken = refreshedToken;
        }

        @Override
        public String get() {
            numCalls++;
            if (this.onCallReturn != null) {
                this.onCallReturn.run();
            }
            return refreshedToken != null
                    ? refreshedToken
                    : tokenMocker.generateRawToken("Mock", "user", DEFAULT_EXPIRING_OFFSET_SECONDS);
        }
    }

    private final MockImmediateRefresher immediateFresher = new MockImmediateRefresher();

    @Test
    public void fresherShouldNotBeCalledBeforeExpiringTime()
            throws InterruptedException, ExecutionException, IOException {
        String tokenStr = tokenMocker.generateRawToken("resourceId", "userIdentity", 15 * 60);
        immediateFresher.resetCallCount();
        CommunicationTokenRefreshOptions tokenRefreshOptions = new CommunicationTokenRefreshOptions(immediateFresher)
                .setRefreshProactively(true)
                .setInitialToken(tokenStr);
        CommunicationTokenCredential tokenCredential = new CommunicationTokenCredential(tokenRefreshOptions);
        StepVerifier.create(tokenCredential.getToken()).assertNext(token -> {
            assertFalse(token.isExpired(),
                    "Refreshable AccessToken should not expire when expiry is set to 5 minutes later");
            assertEquals(tokenStr, token.getToken());
            assertEquals(0, immediateFresher.numCalls());
        })
                .verifyComplete();
        tokenCredential.close();
    }

    @Test
    public void fresherShouldBeCalledAfterExpiringTime() throws InterruptedException, ExecutionException, IOException {
        String tokenStr = tokenMocker.generateRawToken("resourceId", "userIdentity", DEFAULT_EXPIRING_OFFSET_SECONDS);
        immediateFresher.resetCallCount();
        CountDownLatch countDownLatch = new CountDownLatch(1);
        immediateFresher.setOnCallReturn(countDownLatch::countDown);
        CommunicationTokenRefreshOptions tokenRefreshOptions = new CommunicationTokenRefreshOptions(immediateFresher)
                .setRefreshProactively(true)
                .setInitialToken(tokenStr);
        CommunicationTokenCredential tokenCredential = new CommunicationTokenCredential(tokenRefreshOptions);
        countDownLatch.await();
        assertEquals(1, immediateFresher.numCalls());
        StepVerifier.create(tokenCredential.getToken())
                .assertNext(token -> {
                    assertFalse(token.isExpired(), "Refreshable AccessToken should not expire after refresh");
                })
                .verifyComplete();
        tokenCredential.close();
    }

    @Test
    public void refresherShouldBeCalledImmediatelyWithExpiredToken()
            throws InterruptedException, ExecutionException, IOException {
        String tokenStr = tokenMocker.generateRawToken("resourceId", "userIdentity", -5 * 60);
        immediateFresher.resetCallCount();
        CountDownLatch countDownLatch = new CountDownLatch(1);
        immediateFresher.setOnCallReturn(countDownLatch::countDown);
        CommunicationTokenRefreshOptions tokenRefreshOptions = new CommunicationTokenRefreshOptions(immediateFresher)
                .setRefreshProactively(true)
                .setInitialToken(tokenStr);
        CommunicationTokenCredential tokenCredential = new CommunicationTokenCredential(tokenRefreshOptions);

        countDownLatch.await();
        assertEquals(1, immediateFresher.numCalls());
        StepVerifier.create(tokenCredential.getToken())
                .assertNext(token ->
                        assertFalse(token.isExpired(), "Refreshable AccessToken should not expire after refresh"))
                .verifyComplete();
        tokenCredential.close();
    }

    @Test
    public void fractionalBackoffAppliedWhenTokenExpiring()
            throws InterruptedException, ExecutionException, IOException {
        int validForSecs = 8;
        double expectedTotalCallsTillLastSecond = Math.floor(Math.log(validForSecs));
        String tokenStr = tokenMocker.generateRawToken("resourceId", "userIdentity", validForSecs);
        AccessToken accessToken = (new TokenParser()).parseJWTToken(tokenStr);
        immediateFresher.resetCallCount();
        immediateFresher.setRefreshedToken(tokenStr);
        CommunicationTokenRefreshOptions tokenRefreshOptions = new CommunicationTokenRefreshOptions(immediateFresher)
                .setRefreshProactively(true)
                .setInitialToken(tokenStr);
        CommunicationTokenCredential tokenCredential = new CommunicationTokenCredential(tokenRefreshOptions);
        long tokenTtlSecs = 0;
        do {
            tokenTtlSecs = accessToken.getExpiresAt().toInstant().getEpochSecond() - Instant.now().getEpochSecond();
        } while (tokenTtlSecs > 1);
        tokenCredential.close();
        assertEquals(expectedTotalCallsTillLastSecond, immediateFresher.numCalls());
    }

    @Test
    public void shouldThrowExceptionOnExpiredTokenReturn()
            throws InterruptedException, ExecutionException, IOException {
        String expiredToken = tokenMocker.generateRawToken("resourceId", "userIdentity", -5 * 60);
        immediateFresher.resetCallCount();
        immediateFresher.setRefreshedToken(expiredToken);
        CountDownLatch countDownLatch = new CountDownLatch(1);
        immediateFresher.setOnCallReturn(countDownLatch::countDown);
        CommunicationTokenRefreshOptions tokenRefreshOptions = new CommunicationTokenRefreshOptions(immediateFresher)
                .setRefreshProactively(true)
                .setInitialToken(expiredToken);
        CommunicationTokenCredential tokenCredential = new CommunicationTokenCredential(tokenRefreshOptions);

        countDownLatch.await();
        assertEquals(1, immediateFresher.numCalls());
        StepVerifier.create(tokenCredential.getToken())
                .verifyError(IllegalArgumentException.class);
        tokenCredential.close();
    }

    @Test
    public void refresherShouldBeCalledAgainAfterFirstRefreshCall()
            throws InterruptedException, ExecutionException, IOException {
        String tokenStr = tokenMocker.generateRawToken("resourceId", "userIdentity", DEFAULT_EXPIRING_OFFSET_SECONDS);
        immediateFresher.resetCallCount();
        String newTokenStr = tokenMocker.generateRawToken("resourceId", "userIdentity", DEFAULT_EXPIRING_OFFSET_SECONDS + 2);
        immediateFresher.setRefreshedToken(newTokenStr);
        CountDownLatch firstCountDownLatch = new CountDownLatch(1);
        immediateFresher.setOnCallReturn(firstCountDownLatch::countDown);
        CommunicationTokenRefreshOptions tokenRefreshOptions = new CommunicationTokenRefreshOptions(immediateFresher)
                .setRefreshProactively(true)
                .setInitialToken(tokenStr);
        CommunicationTokenCredential tokenCredential = new CommunicationTokenCredential(tokenRefreshOptions);

        firstCountDownLatch.await();
        assertEquals(1, immediateFresher.numCalls());
        StepVerifier.create(tokenCredential.getToken())
                .assertNext(token -> {
                    assertFalse(token.isExpired(), "Refreshable AccessToken should not expire after refresh");
                })
                .verifyComplete();
        CountDownLatch secondCountDownLatch = new CountDownLatch(1);
        immediateFresher.setOnCallReturn(secondCountDownLatch::countDown);

        secondCountDownLatch.await();
        assertEquals(2, immediateFresher.numCalls());
        StepVerifier.create(tokenCredential.getToken())
                .assertNext(token -> {
                    assertFalse(token.isExpired(), "Refreshable AccessToken should not expire after refresh");
                })
                .verifyComplete();
        tokenCredential.close();
    }

    @Test
    public void shouldNotCallRefresherWhenTokenStillValid()
            throws InterruptedException, ExecutionException, IOException {
        String tokenStr = tokenMocker.generateRawToken("resourceId", "userIdentity", 15 * 60);
        immediateFresher.resetCallCount();
        CommunicationTokenRefreshOptions tokenRefreshOptions = new CommunicationTokenRefreshOptions(immediateFresher)
                .setRefreshProactively(false)
                .setInitialToken(tokenStr);
        CommunicationTokenCredential tokenCredential = new CommunicationTokenCredential(tokenRefreshOptions);

        StepVerifier.create(tokenCredential.getToken())
                .assertNext(token -> {
                    assertFalse(token.isExpired());
                    assertEquals(tokenStr, token.getToken());
                    assertEquals(0, immediateFresher.numCalls());
                })
                .verifyComplete();

        for (int i = 0; i < 3; i++) {
            tokenCredential.getToken();
            assertEquals(0, immediateFresher.numCalls());
        }
        tokenCredential.close();
    }

    @Test
    public void expiredTokenShouldBeRefreshedOnDemandWithoutProactiveFetch()
            throws InterruptedException, ExecutionException, IOException {
        String tokenStr = tokenMocker.generateRawToken("resourceId", "userIdentity", -5 * 60);
        immediateFresher.resetCallCount();
        CommunicationTokenRefreshOptions tokenRefreshOptions = new CommunicationTokenRefreshOptions(immediateFresher)
                .setRefreshProactively(false)
                .setInitialToken(tokenStr);
        CommunicationTokenCredential tokenCredential = new CommunicationTokenCredential(tokenRefreshOptions);
        assertEquals(0, immediateFresher.numCalls());
        StepVerifier.create(tokenCredential.getToken())
                .assertNext(token -> {
                    assertFalse(token.isExpired(), "Expired AccessToken should have been refreshed on demand");
                    assertEquals(1, immediateFresher.numCalls());
                })
                .verifyComplete();

        for (int i = 0; i < 3; i++) {
            tokenCredential.getToken();
            assertEquals(1, immediateFresher.numCalls());
        }

        tokenCredential.close();
    }

    @Test
    public void refresherShouldBeCalledImmediatelyWithoutInitialToken() throws InterruptedException, ExecutionException, IOException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        immediateFresher.setOnCallReturn(countDownLatch::countDown);
        immediateFresher.resetCallCount();
        String tokenStr = tokenMocker.generateRawToken("resourceId", "userIdentity", 15 * 60);
        immediateFresher.setRefreshedToken(tokenStr);
        CommunicationTokenRefreshOptions tokenRefreshOptions = new CommunicationTokenRefreshOptions(immediateFresher)
                .setRefreshProactively(true);
        CommunicationTokenCredential tokenCredential = new CommunicationTokenCredential(tokenRefreshOptions);
        countDownLatch.await();
        assertEquals(1, immediateFresher.numCalls());
        StepVerifier.create(tokenCredential.getToken())
                .assertNext(token -> {
                    assertFalse(token.isExpired(), "Immediate fetching case, should be still valid");
                })
                .verifyComplete();
        tokenCredential.close();
    }

    @Test
    public void shouldStopRefreshTimerWhenClosed() throws InterruptedException, ExecutionException, IOException {
        String tokenStr = tokenMocker.generateRawToken("resourceId", "userIdentity", 12 * 60);
        CommunicationTokenRefreshOptions tokenRefreshOptions = new CommunicationTokenRefreshOptions(immediateFresher)
                .setRefreshProactively(true)
                .setInitialToken(tokenStr);
        CommunicationTokenCredential tokenCredential = new CommunicationTokenCredential(tokenRefreshOptions);
        assertTrue(tokenCredential.hasProactiveFetcher());
        tokenCredential.close();
        assertFalse(tokenCredential.hasProactiveFetcher());
    }

    class ExceptionRefresher implements Supplier<String> {
        private int numCalls;
        private Runnable onCallReturn;

        public void setOnCallReturn(Runnable onCallReturn) {
            this.onCallReturn = onCallReturn;
        }

        public int numCalls() throws InterruptedException {
            return numCalls;
        }

        public void resetCallCount() {
            numCalls = 0;
        }

        @Override
        public String get() {
            numCalls++;
            if (this.onCallReturn != null) {
                this.onCallReturn.run();
            }
            final ClientLogger logger = new ClientLogger(CommunicationTokenCredential.class);
            throw new RuntimeException("Lost Internet Connection");
        }
    }

    private final ExceptionRefresher exceptionRefresher = new ExceptionRefresher();

    @Test
    public void shouldNotModifyTokenWhenRefresherThrows() throws InterruptedException, ExecutionException, IOException {
        String tokenStr = tokenMocker.generateRawToken("resourceId", "userIdentity", DEFAULT_EXPIRING_OFFSET_SECONDS);
        CommunicationTokenRefreshOptions tokenRefreshOptions = new CommunicationTokenRefreshOptions(exceptionRefresher)
                .setRefreshProactively(true)
                .setInitialToken(tokenStr);
        CommunicationTokenCredential tokenCredential = new CommunicationTokenCredential(tokenRefreshOptions);
        CountDownLatch countDownLatch = new CountDownLatch(1);
        exceptionRefresher.setOnCallReturn(countDownLatch::countDown);

        countDownLatch.await();
        assertEquals(1, exceptionRefresher.numCalls(), "Refresher was called once");
        StepVerifier.create(tokenCredential.getToken())
                .assertNext(token -> {
                    assertFalse(token.isExpired(), "When failed to refresh, token remains valid since it is not expired yet");
                })
                .verifyComplete();
        tokenCredential.close();
    }

    @Test
    public void doNotSwallowExceptionWithoutProactiveFetching()
            throws InterruptedException, ExecutionException, IOException {
        String tokenStr = tokenMocker.generateRawToken("resourceId", "userIdentity", -5 * 60);
        exceptionRefresher.resetCallCount();
        CommunicationTokenRefreshOptions tokenRefreshOptions = new CommunicationTokenRefreshOptions(exceptionRefresher)
                .setRefreshProactively(false)
                .setInitialToken(tokenStr);
        CommunicationTokenCredential tokenCredential = new CommunicationTokenCredential(tokenRefreshOptions);
        StepVerifier.create(tokenCredential.getToken())
                .verifyError(RuntimeException.class);
        assertEquals(1, exceptionRefresher.numCalls());
        tokenCredential.close();
    }

    @Test
    public void shouldThrowWhenGetTokenCalledOnClosedObject() throws IOException, InterruptedException,
            ExecutionException {
        CommunicationTokenRefreshOptions tokenRefreshOptions = new CommunicationTokenRefreshOptions(exceptionRefresher)
                .setRefreshProactively(true);
        CommunicationTokenCredential tokenCredential = new CommunicationTokenCredential(tokenRefreshOptions);
        tokenCredential.close();

        StepVerifier.create(tokenCredential.getToken())
                .verifyError(RuntimeException.class);

    }
}
