// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.jedis;

import com.azure.core.credential.TokenRequestContext;
import com.azure.core.util.logging.ClientLogger;
import com.azure.jedis.implementation.authentication.AccessTokenCache;
import com.azure.jedis.implementation.authentication.AccessTokenResult;
import com.azure.jedis.implementation.authentication.AuthenticationInfo;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Authenticator manages authentication for Jedis client connection.
 */
class Authenticator {
    private final ClientLogger clientLogger = new ClientLogger(Authenticator.class);
    private AccessTokenCache tokenCache;
    private TokenRequestContext tokenRequestContext;
    private volatile boolean authenticated;
    ReentrantLock lock;

    Authenticator(AccessTokenCache tokenCache) {
        this.tokenCache = tokenCache;
        this.tokenRequestContext = new TokenRequestContext()
                .addScopes("https://redis.azure.com/.default");
        lock = new ReentrantLock();
    }

    public void authenticateIfRequired(AzureJedisClient client) {
        AuthenticationInfo authenticationInfo;
        if (client.isBroken()) {
            lock.lock();
            if (client.isBroken()) {
                client.resetClientIfBroken();
                authenticationInfo = getAuthInfo(true);
                client.authenticate(extractUsernameFromToken(authenticationInfo.getAuthToken()),
                    authenticationInfo.getAuthToken());
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
                        client.authenticate(extractUsernameFromToken(authenticationInfo.getAuthToken()),
                            authenticationInfo.getAuthToken());
                    }
                } else {
                    client.authenticate(extractUsernameFromToken(authenticationInfo.getAuthToken()),
                        authenticationInfo.getAuthToken());
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
        } else {
            return new AuthenticationInfo(null, false);
        }
    }

    private String extractUsernameFromToken(String token) {
        String[] parts = token.split("\\.");
        String base64 = parts[1];

        switch (base64.length() % 4) {
            case 2:
                base64 += "==";
                break;
            case 3:
                base64 += "=";
                break;
        }

        byte[] jsonBytes = Base64.getDecoder().decode(base64);
        String json = new String(jsonBytes, StandardCharsets.UTF_8);
        JsonObject jwt = JsonParser.parseString(json).getAsJsonObject();

        return jwt.get("oid").getAsString();
    }
}
