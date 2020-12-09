// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.common;

import com.azure.communication.common.implementation.JwtTokenMocker;
import com.azure.core.credential.AccessToken;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CommunicationTokenCredentialTests {
    private final JwtTokenMocker tokenMocker = new JwtTokenMocker();

    @Test
    public void constructWithValidTokenWithoutFresher() throws InterruptedException, ExecutionException, IOException {
        String tokenStr = tokenMocker.generateRawToken("resourceId", "userIdentity", 3 * 60);
        CommunicationTokenCredential tokenCredential = new CommunicationTokenCredential(tokenStr);
        AccessToken token = tokenCredential.getToken().get();
        assertFalse(token.isExpired(),
                "Statically cached AccessToken should not expire when expiry is set to 3 minutes later");
        assertEquals(tokenStr, token.getToken());
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
    public void constructWithExpiredTokenWithoutRefresher() throws InterruptedException, ExecutionException, IOException {
        String tokenStr = tokenMocker.generateRawToken("resourceId", "userIdentity", -3 * 60);
        CommunicationTokenCredential tokenCredential = new CommunicationTokenCredential(tokenStr);
        AccessToken token = tokenCredential.getToken().get();
        assertTrue(token.isExpired(),
                "Statically cached AccessToken should expire when expiry is set to 3 minutes before");
        tokenCredential.close();
    }
    
    class MockImmediateRefresher implements TokenRefresher {
        private int numCalls = 0;
        private Runnable onCallReturn;

        public int numCalls() throws InterruptedException {
            return numCalls;
        }

        public void setOnCallReturn(Runnable onCallReturn) {
            this.onCallReturn = onCallReturn;
        }

        public void resetCallCount() {
            numCalls = 0;
        }

        @Override
        public Future<String> getFetchTokenFuture() {
            numCalls++;
            if (this.onCallReturn != null) {
                this.onCallReturn.run();
            }
            return new MockSyncFuture();
        }
    }

    class MockSyncFuture implements Future<String> {

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return false;
        }

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public boolean isDone() {
            return true;
        }

        @Override
        public String get() throws InterruptedException, ExecutionException {
            return tokenMocker.generateRawToken("Mock", "user", 10 * 60 + 1);
        }

        @Override
        public String get(long timeout, TimeUnit unit)
                throws InterruptedException, ExecutionException, TimeoutException {
            return null;
        }
    }

    private final MockImmediateRefresher immediateFresher = new MockImmediateRefresher();

    @Test
    public void fresherShouldNotBeCalledBeforeExpiringTime() throws InterruptedException, ExecutionException, IOException {
        String tokenStr = tokenMocker.generateRawToken("resourceId", "userIdentity", 15 * 60);
        immediateFresher.resetCallCount();
        CommunicationTokenCredential tokenCredential = new CommunicationTokenCredential(immediateFresher, tokenStr, true);
        AccessToken token = tokenCredential.getToken().get();
        assertFalse(token.isExpired(),
                "Refreshable AccessToken should not expire when expiry is set to 5 minutes later");
        assertEquals(tokenStr, token.getToken());
        assertEquals(0, immediateFresher.numCalls());
        tokenCredential.close();
    }

    @Test
    public void fresherShouldBeCalledAfterExpiringTime() throws InterruptedException, ExecutionException, IOException {
        String tokenStr = tokenMocker.generateRawToken("resourceId", "userIdentity", 601);
        immediateFresher.resetCallCount();
        CountDownLatch countDownLatch = new CountDownLatch(1);
        immediateFresher.setOnCallReturn(countDownLatch::countDown);
        CommunicationTokenCredential tokenCredential = new CommunicationTokenCredential(immediateFresher, tokenStr, true);
        
        countDownLatch.await();
        assertEquals(1, immediateFresher.numCalls());
        AccessToken token = tokenCredential.getToken().get();
        assertFalse(token.isExpired(), "Refreshable AccessToken should not expire after refresh");
        tokenCredential.close();
    }

    @Test
    public void refresherShouldBeCalledImmediatelyWithExpiredToken() throws InterruptedException, ExecutionException,
            IOException {
        String tokenStr = tokenMocker.generateRawToken("resourceId", "userIdentity", -5 * 60);
        immediateFresher.resetCallCount();
        CountDownLatch countDownLatch = new CountDownLatch(1);
        immediateFresher.setOnCallReturn(countDownLatch::countDown);
        CommunicationTokenCredential tokenCredential = new CommunicationTokenCredential(immediateFresher, tokenStr, true);
        
        countDownLatch.await();
        assertEquals(1, immediateFresher.numCalls());
        AccessToken token = tokenCredential.getToken().get();
        assertFalse(token.isExpired(), "Refreshable AccessToken should not expire after refresh");
        tokenCredential.close();
    }

    @Test
    public void refresherShouldBeCalledAgainAfterFirstRefreshCall() throws InterruptedException, ExecutionException, IOException {
        String tokenStr = tokenMocker.generateRawToken("resourceId", "userIdentity", 601);
        immediateFresher.resetCallCount();
        CountDownLatch firstCountDownLatch = new CountDownLatch(1);
        immediateFresher.setOnCallReturn(firstCountDownLatch::countDown);
        CommunicationTokenCredential tokenCredential = new CommunicationTokenCredential(immediateFresher, tokenStr, true);
        
        firstCountDownLatch.await();
        assertEquals(1, immediateFresher.numCalls());
        AccessToken token = tokenCredential.getToken().get();
        assertFalse(token.isExpired(), "Refreshable AccessToken should not expire after refresh");
        
        CountDownLatch secondCountDownLatch = new CountDownLatch(1);
        immediateFresher.setOnCallReturn(secondCountDownLatch::countDown);

        secondCountDownLatch.await();
        assertEquals(2, immediateFresher.numCalls());
        token = tokenCredential.getToken().get();
        assertFalse(token.isExpired(), "Refreshable AccessToken should not expire after second refresh");
        tokenCredential.close();
    }
    
    @Test
    public void shouldNotCallRefresherWhenTokenStillValid() throws InterruptedException, ExecutionException, IOException {
        String tokenStr = tokenMocker.generateRawToken("resourceId", "userIdentity", 15 * 60);
        immediateFresher.resetCallCount();
        CommunicationTokenCredential tokenCredential = new CommunicationTokenCredential(immediateFresher, tokenStr, false);
        AccessToken token = tokenCredential.getToken().get();
        assertFalse(token.isExpired());
        assertEquals(tokenStr, token.getToken());
        assertEquals(0, immediateFresher.numCalls());

        for (int i = 0; i < 3; i++) {
            tokenCredential.getToken();
            assertEquals(0, immediateFresher.numCalls());
        }
        tokenCredential.close();
    }

    @Test
    public void expiredTokenShouldBeRefreshedOnDemandWithoutProactiveFetch() throws InterruptedException, ExecutionException, IOException {
        String tokenStr = tokenMocker.generateRawToken("resourceId", "userIdentity", -5 * 60);
        immediateFresher.resetCallCount();
        CommunicationTokenCredential tokenCredential = new CommunicationTokenCredential(immediateFresher, tokenStr, false);
        assertEquals(0, immediateFresher.numCalls());
        AccessToken token = tokenCredential.getToken().get();
        assertFalse(token.isExpired(),
                "Expired AccessToken should have been refreshed on demand");
        assertEquals(1, immediateFresher.numCalls());

        for (int i = 0; i < 3; i++) {
            tokenCredential.getToken();
            assertEquals(1, immediateFresher.numCalls());
        }

        tokenCredential.close();
    }

    @Test
    public void shouldCallbackOnDemandWithoutRefresher() throws InterruptedException, ExecutionException, IOException {
        immediateFresher.resetCallCount();
        CommunicationTokenCredential tokenCredential = new CommunicationTokenCredential(immediateFresher);
        AccessToken accessToken = tokenCredential.getToken().get();
        assertEquals(1, immediateFresher.numCalls());
        assertFalse(accessToken.isExpired(), "On demand fetching case, should be still valid");

        tokenCredential.close();
    }

    @Test
    public void shouldStopRefreshTimerWhenClosed() throws InterruptedException, ExecutionException, IOException {
        String tokenStr = tokenMocker.generateRawToken("resourceId", "userIdentity", 12 * 60);
        CommunicationTokenCredential tokenCredential = new CommunicationTokenCredential(immediateFresher, tokenStr, true);
        assertTrue(tokenCredential.hasProactiveFetcher());
        tokenCredential.close();
        assertFalse(tokenCredential.hasProactiveFetcher());
    }

    class ExceptionRefresher implements TokenRefresher {
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
        public Future<String> getFetchTokenFuture() {
            numCalls++;
            if (this.onCallReturn != null) {
                this.onCallReturn.run();
            }
            throw new RuntimeException("Lost internet connection");
        }
    }

    private final ExceptionRefresher exceptionRefresher = new ExceptionRefresher();

    @Test
    public void shouldNotModifyTokenWhenRefresherThrows() throws InterruptedException, ExecutionException, IOException {
        String tokenStr = tokenMocker.generateRawToken("resourceId", "userIdentity", 601);
        CommunicationTokenCredential tokenCredential = new CommunicationTokenCredential(exceptionRefresher, tokenStr, true);
        CountDownLatch countDownLatch = new CountDownLatch(1);
        exceptionRefresher.setOnCallReturn(countDownLatch::countDown);

        countDownLatch.await();
        assertEquals(1, exceptionRefresher.numCalls(), "Refresher was called once");
        AccessToken token = tokenCredential.getToken().get();
        assertFalse(token.isExpired(), "When failed to refresh, token remains valid since it is not expired yet");
        tokenCredential.close();
    }
    
    @Test
    public void doNotSwallowExceptionWithoutProactiveFetching() throws InterruptedException, ExecutionException, IOException {
        String tokenStr = tokenMocker.generateRawToken("resourceId", "userIdentity", -5 * 60);
        exceptionRefresher.resetCallCount();
        CommunicationTokenCredential tokenCredential = new CommunicationTokenCredential(exceptionRefresher, tokenStr, false);
        assertThrows(Exception.class, () -> {
            tokenCredential.getToken();
        }, "Should not swallow exception when client throws");
        assertEquals(1, exceptionRefresher.numCalls());
        tokenCredential.close();
    }

    class MockLongRunningRefresher implements TokenRefresher {
        private int numCalls = 0;
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

        @Override
        public Future<String> getFetchTokenFuture() {
            numCalls++;
            return new LongRunningFuture(onCallReturn);
        }
    }

    class LongRunningFuture implements Future<String> {
        private Runnable onCallReturn;

        LongRunningFuture(Runnable onCallReturn) {
            this.onCallReturn = onCallReturn;
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return false;
        }

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public boolean isDone() {
            return false;
        }

        public void setOnCallReturn(Runnable onCallReturn) {
            this.onCallReturn = onCallReturn;
        }

        @Override
        public String get() throws InterruptedException, ExecutionException {
            if (this.onCallReturn != null) {
                this.onCallReturn.run();
            }
            return tokenMocker.generateRawToken("Mock", "user", 601);
        }

        @Override
        public String get(long timeout, TimeUnit unit)
                throws InterruptedException, ExecutionException, TimeoutException {
            return null;
        }
    }

    final MockLongRunningRefresher longRunningRefresher = new MockLongRunningRefresher();

    @Test
    public void shouldCallRefresherOnlyOnceWhileRefreshingIsInProgress()
        throws InterruptedException, ExecutionException, IOException {
        longRunningRefresher.resetCallCount();
        CountDownLatch countDownLatch = new CountDownLatch(1);
        longRunningRefresher.setOnCallReturn(countDownLatch::countDown);
        String tokenStr = tokenMocker.generateRawToken("resourceId", "userIdentity", -5 * 60);

        CommunicationTokenCredential tokenCredential = new CommunicationTokenCredential(longRunningRefresher, tokenStr, true);

        countDownLatch.await();
        assertEquals(1, longRunningRefresher.numCalls());
        for (int i = 0; i < 3; i++) {
            tokenCredential.getToken();
            assertEquals(1, longRunningRefresher.numCalls(), "No additional call is made by getToken");
        }
        tokenCredential.close();
    }

    @Test
    public void withoutInitialTokenShouldCallFresherOnlyOnceWhileRefreshingIsInProgress()
        throws InterruptedException, ExecutionException, IOException {
        longRunningRefresher.resetCallCount();
        CommunicationTokenCredential tokenCredential = new CommunicationTokenCredential(longRunningRefresher);
        tokenCredential.getToken();
        assertEquals(1, longRunningRefresher.numCalls());
        for (int i = 0; i < 3; i++) {
            tokenCredential.getToken();
            assertEquals(1, longRunningRefresher.numCalls(), "No additional calls made by getToken call");
        }
        tokenCredential.close();
    }

    class MockExceptionFutureRefresher implements TokenRefresher {
        private int numCalls = 0;

        public int numCalls() throws InterruptedException {
            return numCalls;
        }

        public void resetCallCount() {
            numCalls = 0;
        }

        @Override
        public Future<String> getFetchTokenFuture() {
            numCalls++;
            return new MockExceptionFuture();
        }
    }

    class MockExceptionFuture implements Future<String> {

        private boolean isDone = false;
        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return false;
        }

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public boolean isDone() {
            return isDone;
        }

        @Override
        public String get() throws InterruptedException, ExecutionException {
            isDone = true;
            throw new InterruptedException();
        }

        @Override
        public String get(long timeout, TimeUnit unit)
                throws InterruptedException, ExecutionException, TimeoutException {
            return null;
        }
    }

    private final MockExceptionFutureRefresher exceptionFutureRefresher = new MockExceptionFutureRefresher();

    @Test
    public void shouldRefreshAgainAfterClientFutureThrows() throws InterruptedException, ExecutionException,
            IOException {
        exceptionFutureRefresher.resetCallCount();
        CommunicationTokenCredential tokenCredential = new CommunicationTokenCredential(exceptionFutureRefresher);
        assertEquals(0, exceptionFutureRefresher.numCalls());
        Future<AccessToken> accessToken = tokenCredential.getToken();
        assertEquals(1, exceptionFutureRefresher.numCalls());

        for (int i = 0; i < 3; i++) {
            tokenCredential.getToken();
            assertEquals(1, exceptionFutureRefresher.numCalls());
        }

        assertThrows(InterruptedException.class, () -> {
            accessToken.get();
        });

        tokenCredential.getToken();
        assertEquals(2, exceptionFutureRefresher.numCalls());

        tokenCredential.close();
    }

    @Test
    public void shouldThrowWhenGetTokenCalledOnClosedObject() throws IOException {
        CommunicationTokenCredential tokenCredential = new CommunicationTokenCredential(exceptionFutureRefresher);
        tokenCredential.close();
        assertThrows(RuntimeException.class, () -> {
            tokenCredential.getToken();
        });
    }
}
