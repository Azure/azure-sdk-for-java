// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.common;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.azure.core.util.logging.ClientLogger;
import com.azure.core.credential.AccessToken;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import com.azure.communication.common.implementation.TokenParser;

/**
 * Provide user credential for Communication service user
 */
public final class CommunicationUserCredential implements AutoCloseable {
    private static final int DEFAULT_EXPIRING_OFFSET_MINUTES = 10;

    private final ClientLogger logger = new ClientLogger(CommunicationUserCredential.class);

    private AccessToken accessToken;
    private Future<AccessToken> tokenFuture;
    private final TokenParser tokenParser = new TokenParser();
    private TokenRefresher refresher;
    private FetchingTask fetchingTask;
    private boolean isClosed = false;

    private static class TokenImmediate implements Future<AccessToken> {
        private final AccessToken accessToken;

        TokenImmediate(AccessToken accessToken) {
            this.accessToken = accessToken;
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
            return true;
        }

        @Override
        public AccessToken get() throws InterruptedException, ExecutionException {
            return this.accessToken;
        }

        @Override
        public AccessToken get(long timeout, TimeUnit unit)
                throws InterruptedException, ExecutionException, TimeoutException {
            return this.accessToken;
        }
    }

    /**
     * Create with serialized JWT token
     * 
     * @param initialToken serialized JWT token
     */
    public CommunicationUserCredential(String initialToken) {
        Objects.requireNonNull(initialToken, "'initialToken' cannot be null.");
        setToken(initialToken);
        tokenFuture = new TokenImmediate(accessToken);
    }

    /**
     * Create with a tokenRefresher
     * 
     * @param tokenRefresher implementation to supply fresh token when reqested
     */
    public CommunicationUserCredential(TokenRefresher tokenRefresher) {
        Objects.requireNonNull(tokenRefresher, "'tokenRefresher' cannot be null.");
        refresher = tokenRefresher;
    }

    /**
     * Create with serialized JWT token and a token supplier to auto-refresh the
     * token before it expires. Callback function tokenRefresher will be called ahead
     * of the token expiry by the number of minutes specified by
     * CallbackOffsetMinutes defaulted to two minutes. To modify this default, call
     * setCallbackOffsetMinutes after construction
     * 
     * @param tokenRefresher implementation to supply fresh token when reqested
     * @param initialToken serialized JWT token
     * @param refreshProactively when set to true, turn on proactive fetching to
     *                           call tokenRefresher before token expiry by minutes
     *                           set with setCallbackOffsetMinutes or default value
     *                           of two minutes
     */
    public CommunicationUserCredential(TokenRefresher tokenRefresher, String initialToken, boolean refreshProactively) {
        this(tokenRefresher);
        Objects.requireNonNull(initialToken, "'initialToken' cannot be null.");
        setToken(initialToken);
        tokenFuture = new TokenImmediate(accessToken);
        if (refreshProactively) {
            OffsetDateTime nextFetchTime = accessToken.getExpiresAt().minusMinutes(DEFAULT_EXPIRING_OFFSET_MINUTES);
            fetchingTask = new FetchingTask(this, nextFetchTime);
        }
    }

    /**
     * Get Azure core access token from credential
     * 
     * @return Asynchronous call to fetch actual token
     * @throws ExecutionException when supplier throws this exception
     * @throws InterruptedException when supplier throws this exception
     */
    public Future<AccessToken> getToken() throws InterruptedException, ExecutionException {
        if (isClosed) {
            throw logger.logExceptionAsError(
                new RuntimeException("getToken called on closed CommunicationUserCredential object"));
        }
        if ((accessToken == null || accessToken.isExpired()) // no valid token to return
            && refresher != null // can refresh
            && (tokenFuture == null || tokenFuture.isDone())) { // no fetching in progress, proactive or on-demand 
            fetchFreshToken();
        }

        return tokenFuture;
    }

    @Override
    public void close() throws IOException {
        isClosed = true;
        if (fetchingTask != null) {
            fetchingTask.stopTimer();
            fetchingTask = null;
        }
        refresher = null;
    }

    // For test verification usage only
    boolean hasProactiveFetcher() {
        return fetchingTask != null;
    }

    private void setToken(String freshToken) {        
        accessToken = tokenParser.parseJWTToken(freshToken);
        
        if (fetchingTask != null) {
            OffsetDateTime nextFetchTime = accessToken.getExpiresAt().minusMinutes(DEFAULT_EXPIRING_OFFSET_MINUTES);
            fetchingTask.setNextFetchTime(nextFetchTime);
        }
    }
        
    private Future<String> fetchFreshToken() {
        Future<String> fetchFuture = refresher.getFetchTokenFuture();
        if (fetchFuture == null) {
            throw logger.logExceptionAsError(
                new RuntimeException("TokenRefresher returned null when getFetchTokenFuture is called"));
        }
        tokenFuture = new TokenFuture(fetchFuture);
        return fetchFuture;
    }

    private class TokenFuture implements Future<AccessToken> {
        private final Future<String> clientTokenFuture;

        TokenFuture(Future<String> tokenStringFuture) {
            this.clientTokenFuture = tokenStringFuture;
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return clientTokenFuture.cancel(mayInterruptIfRunning);
        }

        @Override
        public boolean isCancelled() {
            return clientTokenFuture.isCancelled();
        }

        @Override
        public boolean isDone() {
            return clientTokenFuture.isDone();
        }

        @Override
        public AccessToken get() throws InterruptedException, ExecutionException {
            String freshToken = clientTokenFuture.get();
            setToken(freshToken);            
            return accessToken;
        }

        @Override
        public AccessToken get(long timeout, TimeUnit unit)
                throws InterruptedException, ExecutionException, TimeoutException {
            String freshToken = clientTokenFuture.get(timeout, unit);
            setToken(freshToken);
            return accessToken;
        }
    }

    private static class FetchingTask {
        private final CommunicationUserCredential host;
        private Timer expiringTimer;
        private OffsetDateTime nextFetchTime;

        FetchingTask(CommunicationUserCredential tokenHost,
            OffsetDateTime nextFetchAt) {
            host = tokenHost;
            nextFetchTime = nextFetchAt;
            startTimer();
        }

        private synchronized void setNextFetchTime(OffsetDateTime newFetchTime) {
            nextFetchTime = newFetchTime;
            stopTimer();
            startTimer();
        }

        private synchronized void startTimer() {
            expiringTimer = new Timer();
            Date expiring = Date.from(nextFetchTime.toInstant());
            expiringTimer.schedule(new TokenExpiringTask(this), expiring);
        }
    
        private synchronized void stopTimer() {
            if (expiringTimer == null) {
                return;
            }

            expiringTimer.cancel();
            expiringTimer.purge();
            expiringTimer = null;
        }

        private Future<String> fetchFreshToken() {
            return host.fetchFreshToken();
        }

        private void setToken(String freshTokenString) {
            host.setToken(freshTokenString);
        }

        private class TokenExpiringTask extends TimerTask {
            private final ClientLogger logger = new ClientLogger(TokenExpiringTask.class);
            private final FetchingTask tokenCache;
    
            TokenExpiringTask(FetchingTask host) {
                tokenCache = host;
            }
    
            @Override
            public void run() {
                try {
                    Future<String> tokenStringFuture = tokenCache.fetchFreshToken();
                    tokenCache.setToken(tokenStringFuture.get());
                } catch (Exception exception) {
                    logger.logExceptionAsError(new RuntimeException(exception));
                }
                
            }
        }    
    }
}
