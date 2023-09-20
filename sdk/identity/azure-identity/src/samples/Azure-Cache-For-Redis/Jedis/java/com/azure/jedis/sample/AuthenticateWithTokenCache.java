// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.jedis.sample;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.util.CoreUtils;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisException;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ThreadLocalRandom;

public class AuthenticateWithTokenCache {

    public static void main(String[] args) {
        //Construct a Token Credential from Identity library, e.g. DefaultAzureCredential / ClientSecretCredential / Client CertificateCredential / ManagedIdentityCredential etc.
        DefaultAzureCredential defaultAzureCredential = new DefaultAzureCredentialBuilder().build();

        // Fetch an Azure AD token to be used for authentication. This token will be used as the password.
        // Note: The Scopes parameter will change as the Azure AD Authentication support hits public preview and eventually GA's.
        TokenRequestContext trc = new TokenRequestContext().addScopes("acca5fbb-b7e4-4009-81f1-37e38fd66d78/.default");
        TokenRefreshCache tokenRefreshCache = new TokenRefreshCache(defaultAzureCredential, trc);
        AccessToken accessToken = tokenRefreshCache.getAccessToken();

        // SSL connection is required.
        boolean useSsl = true;
        // TODO: Replace <HOST_NAME> with Azure Cache for Redis Host name.
        String cacheHostname = "<HOST_NAME>";

        // Create Jedis client and connect to the Azure Cache for Redis over the TLS/SSL port using the access token as password.
        // Note: Cache Host Name, Port, Username, Azure AD Access Token and ssl connections are required below.
        Jedis jedis = createJedisClient(cacheHostname, 6380, "<USERNAME>", accessToken, useSsl);

        // Configure the jedis instance for proactive authentication before token expires.
        tokenRefreshCache
            .setJedisInstanceToAuthenticate(jedis)
            .setUsername("<USERNAME>");

        int maxTries = 3;
        int i = 0;

        while (i < maxTries) {
            try {
                // Set a value against your key in the Redis cache.
                jedis.set("Az:key", "testValue");
                System.out.println(jedis.get("Az:key"));
                break;
            } catch (JedisException e) {
                // Handle The Exception as required in your application.
                e.printStackTrace();

                // For Exceptions containing Invalid Username Password / Permissions not granted error messages, look at troubleshooting section at the end of document.

                // Check if the client is broken, if it is then close and recreate it to create a new healthy connection.
                if (jedis.isBroken()) {
                    jedis.close();
                    jedis = createJedisClient(cacheHostname, 6380, "<USERNAME>", tokenRefreshCache.getAccessToken(), useSsl);

                    // Configure the jedis instance for proactive authentication before token expires.
                    tokenRefreshCache
                        .setJedisInstanceToAuthenticate(jedis)
                        .setUsername("<USERNAME>");
                }
            }
            i++;
        }
        // Close the Jedis Client
        jedis.close();

    }

    // Helper Code
    private static Jedis createJedisClient(String cacheHostname, int port, String username, AccessToken accessToken, boolean useSsl) {
        return new Jedis(cacheHostname, port, DefaultJedisClientConfig.builder()
            .password(accessToken.getToken())
            .user(username)
            .ssl(useSsl)
            .build());
    }

    /**
     * The token cache to store and proactively refresh the access token.
     */
    public static class TokenRefreshCache {
        private final TokenCredential tokenCredential;
        private final TokenRequestContext tokenRequestContext;
        private final Timer timer;
        private volatile AccessToken accessToken;
        private final Duration maxRefreshOffset = Duration.ofMinutes(5);
        private final Duration baseRefreshOffset = Duration.ofMinutes(2);
        private Jedis jedisInstanceToAuthenticate;
        private String username;

        /**
         * Creates an instance of TokenRefreshCache
         * @param tokenCredential the token credential to be used for authentication.
         * @param tokenRequestContext the token request context to be used for authentication.
         */
        public TokenRefreshCache(TokenCredential tokenCredential, TokenRequestContext tokenRequestContext) {
            this.tokenCredential = tokenCredential;
            this.tokenRequestContext = tokenRequestContext;
            this.timer = new Timer(true);
        }

        /**
         * Gets the cached access token.
         * @return the {@link AccessToken}
         */
        public AccessToken getAccessToken() {
            if (accessToken != null) {
                return  accessToken;
            } else {
                TokenRefreshTask tokenRefreshTask = new TokenRefreshTask();
                accessToken = tokenCredential.getToken(tokenRequestContext).block();
                timer.schedule(tokenRefreshTask, getTokenRefreshDelay());
                return accessToken;
            }
        }

        private class TokenRefreshTask extends TimerTask {
            // Add your task here
            public void run() {
                accessToken = tokenCredential.getToken(tokenRequestContext).block();
                System.out.println("Refreshed Token with Expiry: " + accessToken.getExpiresAt().toEpochSecond());

                if (jedisInstanceToAuthenticate != null && !CoreUtils.isNullOrEmpty(username)) {
                    jedisInstanceToAuthenticate.auth(username, accessToken.getToken());
                    System.out.println("Refreshed Jedis Connection with fresh access token, token expires at : "
                        + accessToken.getExpiresAt().toEpochSecond());
                }
                timer.schedule(new TokenRefreshTask(), getTokenRefreshDelay());
            }
        }

        private long getTokenRefreshDelay() {
            return ((accessToken.getExpiresAt()
                .minusSeconds(ThreadLocalRandom.current().nextLong(baseRefreshOffset.getSeconds(), maxRefreshOffset.getSeconds()))
                .toEpochSecond() - OffsetDateTime.now().toEpochSecond()) * 1000);
        }

        /**
         * Sets the Jedis to proactively authenticate before token expiry.
         * @param jedisInstanceToAuthenticate the instance to authenticate
         * @return the updated instance
         */
        public TokenRefreshCache setJedisInstanceToAuthenticate(Jedis jedisInstanceToAuthenticate) {
            this.jedisInstanceToAuthenticate = jedisInstanceToAuthenticate;
            return this;
        }

        /**
         * Sets the username to authenticate jedis instance with.
         * @param username the username to authenticate with
         * @return the updated instance
         */
        public TokenRefreshCache setUsername(String username) {
            this.username = username;
            return this;
        }
    }
}
