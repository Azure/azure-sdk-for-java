// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.jedis;

import com.azure.core.credential.TokenRequestContext;
import com.azure.core.util.logging.ClientLogger;
import com.azure.jedis.implementation.authentication.AccessTokenCache;
import com.azure.jedis.implementation.authentication.AccessTokenResult;
import com.azure.jedis.implementation.authentication.AuthenticationInfo;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Authenticator manages authentication for Jedis client connection.
 */
class Authenticator {
    private final ClientLogger clientLogger = new ClientLogger(Authenticator.class);
    private String username;
    private AccessTokenCache tokenCache;
    private TokenRequestContext tokenRequestContext;
    private String password;
    private volatile boolean authenticated;
    ReentrantLock lock;

    public Authenticator(String username, AccessTokenCache tokenCache) {
        this.username = username;
        this.tokenCache = tokenCache;
        this.tokenRequestContext = new TokenRequestContext()
                .addScopes("https://*.cacheinfra.windows.net:10225/appid/.default");
        lock = new ReentrantLock();
    }

    public Authenticator(String username, String password) {
        this.password = password;
        this.username = username;
        lock = new ReentrantLock();
    }

    public void authenticateIfRequired(AzureJedisClient client) {
        AuthenticationInfo authenticationInfo;
        if (client.isBroken()) {
            lock.lock();
            if (client.isBroken()) {
                client.resetClientIfBroken();
                authenticationInfo = getAuthInfo(true);
                client.authenticate(username, authenticationInfo.getAuthToken());
            }
            lock.unlock();
            return;
        } else {
            authenticationInfo = getAuthInfo(false);
            if (authenticationInfo.isShouldAuthenticate()) {
                lock.lock();
                if (authenticated) {
                    if (authenticationInfo.isRefreshedToken()) {
                        client.resetClient();
                        client.authenticate(username, authenticationInfo.getAuthToken());
                    }
                } else {
                    client.authenticate(username, authenticationInfo.getAuthToken());
                    authenticated = true;
                }
                lock.unlock();
            }
        }
    }

    private AuthenticationInfo getAuthInfo(boolean forceAuth) {
        if (tokenCache != null && tokenRequestContext != null) {
            AccessTokenResult result = tokenCache
                    .getToken(tokenRequestContext, forceAuth).block();
            return new AuthenticationInfo(result.getAccessToken().getToken(),
                    result.isRefreshedToken() || !authenticated, result.isRefreshedToken());
        } else if (password != null) {
            return new AuthenticationInfo(password, !authenticated);
        } else {
            return new AuthenticationInfo(null, false);
        }
    }
}
