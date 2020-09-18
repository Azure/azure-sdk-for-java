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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CommunicationUserCredentialTests {
    private final JwtTokenMocker tokenMocker = new JwtTokenMocker();

    @Test
    public void constructWithValidTokenWithoutFresher() throws InterruptedException, ExecutionException, IOException {
        String tokenStr = tokenMocker.generateRawToken("resourceId", "userIdentity", 3 * 60);
        CommunicationUserCredential userCredential = new CommunicationUserCredential(tokenStr);
        AccessToken token = userCredential.getToken().get();
        assertFalse(token.isExpired(),
                "Statically cached AccessToken should not expire when expiry is set to 3 minutes later");
        assertEquals(tokenStr, token.getToken());
        userCredential.close();
    }

    @Test
    public void constructWithInvalidTokenStringShouldThrow() {
        String tokenStr = "IAmNotAToken";
        assertThrows(Exception.class, () -> {
            new CommunicationUserCredential(tokenStr);
        }, "Should throw on invalid token");
    }

    @Test
    public void constructWithExpiredTokenWithoutRefresher() throws InterruptedException, ExecutionException, IOException {
        String tokenStr = tokenMocker.generateRawToken("resourceId", "userIdentity", -3 * 60);
        CommunicationUserCredential userCredential = new CommunicationUserCredential(tokenStr);
        AccessToken token = userCredential.getToken().get();
        assertTrue(token.isExpired(),
                "Statically cached AccessToken should expire when expiry is set to 3 minutes before");
        userCredential.close();
    }
    
    class MockImmediateRefresher implements TokenRefresher {
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
            return tokenMocker.generateRawToken("Mock", "user", 12 * 60 + 5);
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
        CommunicationUserCredential userCredential = new CommunicationUserCredential(immediateFresher, tokenStr, true);
        AccessToken token = userCredential.getToken().get();
        assertFalse(token.isExpired(),
                "Refreshable AccessToken should not expire when expiry is set to 5 minutes later");
        assertEquals(tokenStr, token.getToken());
        assertEquals(0, immediateFresher.numCalls());
        userCredential.close();
    }

    // @Test
    public void fresherShouldBeCalledAfterExpiringTime() throws InterruptedException, ExecutionException, IOException {
        String tokenStr = tokenMocker.generateRawToken("resourceId", "userIdentity", 12 * 60 + 2);
        immediateFresher.resetCallCount();
        CommunicationUserCredential userCredential = new CommunicationUserCredential(immediateFresher, tokenStr, true);
        Thread.sleep(2 * 1000 + 50);
        assertEquals(1, immediateFresher.numCalls());
        AccessToken token = userCredential.getToken().get();
        assertFalse(token.isExpired(), "Refreshable AccessToken should not expire after refresh");
        userCredential.close();
    }

    // @Test
    public void refresherShouldBeCalledImmediatelyWithExpiredToken() throws InterruptedException, ExecutionException,
            IOException {
        String tokenStr = tokenMocker.generateRawToken("resourceId", "userIdentity", -5 * 60);
        immediateFresher.resetCallCount();
        CommunicationUserCredential userCredential = new CommunicationUserCredential(immediateFresher, tokenStr, true);
        Thread.sleep(100);
        assertEquals(1, immediateFresher.numCalls());
        AccessToken token = userCredential.getToken().get();
        assertFalse(token.isExpired(), "Refreshable AccessToken should not expire after refresh");
        userCredential.close();
    }

    // @Test
    public void refresherShouldBeCalledAgainAfterFirstRefreshCall() throws InterruptedException, ExecutionException, IOException {
        String tokenStr = tokenMocker.generateRawToken("resourceId", "userIdentity", 12 * 60 + 2);
        immediateFresher.resetCallCount();
        CommunicationUserCredential userCredential = new CommunicationUserCredential(immediateFresher, tokenStr, true);
        Thread.sleep(2 * 1000 + 100);
        assertEquals(1, immediateFresher.numCalls());
        AccessToken token = userCredential.getToken().get();
        assertFalse(token.isExpired(), "Refreshable AccessToken should not expire after refresh");
        Thread.sleep(5 * 1000 + 100);
        assertEquals(2, immediateFresher.numCalls());
        token = userCredential.getToken().get();
        assertFalse(token.isExpired(), "Refreshable AccessToken should not expire after second refresh");
        userCredential.close();
    }
    
    @Test
    public void shouldNotCallRefresherWhenTokenStillValid() throws InterruptedException, ExecutionException, IOException {
        String tokenStr = tokenMocker.generateRawToken("resourceId", "userIdentity", 15 * 60);
        immediateFresher.resetCallCount();
        CommunicationUserCredential userCredential = new CommunicationUserCredential(immediateFresher, tokenStr, false);
        AccessToken token = userCredential.getToken().get();
        assertFalse(token.isExpired());
        assertEquals(tokenStr, token.getToken());
        assertEquals(0, immediateFresher.numCalls());

        for (int i = 0; i < 3; i++) {
            Thread.sleep(50);
            userCredential.getToken();
            assertEquals(0, immediateFresher.numCalls());
        }
        userCredential.close();
    }

    @Test
    public void expiredTokenShouldBeRefreshedOnDemandWithoutProactiveFetch() throws InterruptedException, ExecutionException, IOException {
        String tokenStr = tokenMocker.generateRawToken("resourceId", "userIdentity", -5 * 60);
        immediateFresher.resetCallCount();
        CommunicationUserCredential userCredential = new CommunicationUserCredential(immediateFresher, tokenStr, false);
        Thread.sleep(100);
        assertEquals(0, immediateFresher.numCalls());
        AccessToken token = userCredential.getToken().get();
        assertFalse(token.isExpired(),
                "Expired AccessToken should have been refreshed on demand");
        assertEquals(1, immediateFresher.numCalls());

        for (int i = 0; i < 3; i++) {
            userCredential.getToken();
            assertEquals(1, immediateFresher.numCalls());
        }

        userCredential.close();
    }

    @Test
    public void shouldCallbackOnDemandWithoutRefresher() throws InterruptedException, ExecutionException, IOException {
        immediateFresher.resetCallCount();
        CommunicationUserCredential userCredential = new CommunicationUserCredential(immediateFresher);
        AccessToken accessToken = userCredential.getToken().get();
        assertEquals(1, immediateFresher.numCalls());
        assertFalse(accessToken.isExpired(), "On demand fetching case, should be still valid");

        userCredential.close();
    }

    @Test
    public void shouldStopRefreshTimerWhenClosed() throws InterruptedException, ExecutionException, IOException {
        String tokenStr = tokenMocker.generateRawToken("resourceId", "userIdentity", 12 * 60 + 5);
        CommunicationUserCredential userCredential = new CommunicationUserCredential(immediateFresher, tokenStr, true);
        assertTrue(userCredential.hasProactiveFetcher());
        userCredential.close();
        assertFalse(userCredential.hasProactiveFetcher());
    }

    class ExceptionRefresher implements TokenRefresher {
        private int numCalls;

        public int numCalls() throws InterruptedException {
            return numCalls;
        }

        public void resetCallCount() {
            numCalls = 0;
        }

        @Override
        public Future<String> getFetchTokenFuture() {
            numCalls++;
            throw new RuntimeException("Lost internet connection");
        }
    }

    private final ExceptionRefresher exceptionRefresher = new ExceptionRefresher();

    // @Test
    public void shouldNotModifyTokenWhenRefresherThrows() throws InterruptedException, ExecutionException, IOException {
        String tokenStr = tokenMocker.generateRawToken("resourceId", "userIdentity", 12 * 60 + 2);
        CommunicationUserCredential userCredential = new CommunicationUserCredential(exceptionRefresher, tokenStr, true);
        Thread.sleep(2 * 1000 + 100);
        assertEquals(1, exceptionRefresher.numCalls(), "Refresher was called once");
        AccessToken token = userCredential.getToken().get();
        assertFalse(token.isExpired(), "When failed to refresh, token remains valid since it is not expired yet");
        userCredential.close();
    }
    
    @Test
    public void doNotSwallowExceptionWithoutProactiveFetching() throws InterruptedException, ExecutionException, IOException {
        String tokenStr = tokenMocker.generateRawToken("resourceId", "userIdentity", -5 * 60);
        exceptionRefresher.resetCallCount();
        CommunicationUserCredential userCredential = new CommunicationUserCredential(exceptionRefresher, tokenStr, false);
        assertThrows(Exception.class, () -> {
            userCredential.getToken();
        }, "Should not swallow exception when client throws");
        assertEquals(1, exceptionRefresher.numCalls());
        userCredential.close();
    }

    class MockLongRunningRefresher implements TokenRefresher {
        private int numCalls = 0;

        public int numCalls() {
            return numCalls;
        }

        public void resetCallCount() {
            numCalls = 0;
        }

        @Override
        public Future<String> getFetchTokenFuture() {
            numCalls++;
            return new LongRunningFuture();
        }
    }

    class LongRunningFuture implements Future<String> {
        private List<String> synchedList = Collections.synchronizedList(new ArrayList<String>());

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

        @Override
        public String get() throws InterruptedException, ExecutionException {
            synchronized (synchedList) {
                synchedList.wait();
                return tokenMocker.generateRawToken("Mock", "user", 6 * 60);
            }
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
        String tokenStr = tokenMocker.generateRawToken("resourceId", "userIdentity", -5 * 60);
        CommunicationUserCredential userCredential = new CommunicationUserCredential(longRunningRefresher, tokenStr, true);
        // Wait a bit to ensure callback is invoked
        Thread.sleep(300);
        assertEquals(1, longRunningRefresher.numCalls());
        for (int i = 0; i < 3; i++) {
            Thread.sleep(200);
            userCredential.getToken();
            assertEquals(1, longRunningRefresher.numCalls(), "No additional call is made by getToken");
        }
        userCredential.close();
    }

    @Test
    public void withoutInitialTokenShouldCallFresherOnlyOnceWhileRefreshingIsInProgress()
        throws InterruptedException, ExecutionException, IOException {
        longRunningRefresher.resetCallCount();
        CommunicationUserCredential userCredential = new CommunicationUserCredential(longRunningRefresher);
        userCredential.getToken();
        assertEquals(1, longRunningRefresher.numCalls());
        for (int i = 0; i < 3; i++) {
            Thread.sleep(50);
            userCredential.getToken();
            assertEquals(1, longRunningRefresher.numCalls(), "No additional calls made by getToken call");
        }
        userCredential.close();
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
        CommunicationUserCredential userCredential = new CommunicationUserCredential(exceptionFutureRefresher);
        assertEquals(0, exceptionFutureRefresher.numCalls());
        Future<AccessToken> accessToken = userCredential.getToken();
        assertEquals(1, exceptionFutureRefresher.numCalls());

        for (int i = 0; i < 3; i++) {
            Thread.sleep(50);
            userCredential.getToken();
            assertEquals(1, exceptionFutureRefresher.numCalls());
        }

        assertThrows(InterruptedException.class, () -> {
            accessToken.get();
        });

        userCredential.getToken();
        assertEquals(2, exceptionFutureRefresher.numCalls());

        userCredential.close();
    }

    @Test
    public void shouldThrowWhenGetTokenCalledOnClosedObject() throws IOException {
        CommunicationUserCredential userCredential = new CommunicationUserCredential(exceptionFutureRefresher);
        userCredential.close();
        assertThrows(RuntimeException.class, () -> {
            userCredential.getToken();
        });
    }
}
