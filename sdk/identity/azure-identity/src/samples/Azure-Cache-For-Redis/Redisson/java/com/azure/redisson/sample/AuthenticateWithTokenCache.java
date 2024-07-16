// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.redisson.sample;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.redisson.Redisson;
import org.redisson.api.RBucket;
import org.redisson.api.RBuckets;
import org.redisson.api.RedissonClient;
import org.redisson.client.RedisException;
import org.redisson.config.Config;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ThreadLocalRandom;

public class AuthenticateWithTokenCache {

    public static void main(String[] args) {

        //Construct a Token Credential from Identity library, e.g. DefaultAzureCredential / ClientSecretCredential / Client CertificateCredential / ManagedIdentityCredential etc.
        DefaultAzureCredential defaultAzureCredential = new DefaultAzureCredentialBuilder().build();

        // Fetch a Microsoft Entra token to be used for authentication. This token will be used as the password.
        TokenRequestContext trc = new TokenRequestContext().addScopes("https://redis.azure.com/.default");

        // Instantiate the Token Refresh Cache, this cache will proactively refresh the access token 2 - 5 minutes before expiry.
        TokenRefreshCache tokenRefreshCache = new TokenRefreshCache(defaultAzureCredential, trc);
        AccessToken accessToken = tokenRefreshCache.getAccessToken();
        String username = extractUsernameFromToken(accessToken.getToken());

        // Create Redisson Client
        // Host Name, Port, and Microsoft Entra token are required here.
        // TODO: Replace <HOST_NAME> with Azure Cache for Redis Host name.
        RedissonClient redisson = createRedissonClient("rediss://<HOST_NAME>:6380", username, accessToken);

        int maxTries = 3;
        int i = 0;

        while (i < maxTries) {
            try {
                // perform operations
                RBuckets rBuckets = redisson.getBuckets();
                RBucket<String> bucket = redisson.getBucket("Az:key");
                bucket.set("This is object value");

                String objectValue = bucket.get().toString();
                System.out.println("stored object value: " + objectValue);
                break;
            } catch (RedisException exception) {
                // TODO: Handle Exception as Required.
                exception.printStackTrace();

                // For Exceptions containing Invalid Username Password / Permissions not granted error messages, look at troubleshooting section at the end of document.

                if (redisson.isShutdown()) {
                    AccessToken token = tokenRefreshCache.getAccessToken();
                    // Recreate the client with a fresh token non-expired token as password for authentication.
                    redisson = createRedissonClient("rediss://<HOST_NAME>:6380", username, token);
                }
            } catch (Exception e) {
                // Handle Exception as required
                e.printStackTrace();
            }
            i++;
        }
        redisson.shutdown();
    }


    // Helper Code
    private static RedissonClient createRedissonClient(String address, String username, AccessToken accessToken) {

        Config config = new Config();
        config.useSingleServer()
            .setAddress(address)
            .setKeepAlive(true)
            .setUsername(username)
            .setPassword(accessToken.getToken())
            .setClientName("Reddison-Client");

        return Redisson.create(config);
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

        /**
         * Creates an instance of TokenRefreshCache
         * @param tokenCredential the token credential to be used for authentication.
         * @param tokenRequestContext the token request context to be used for authentication.
         */
        public TokenRefreshCache(TokenCredential tokenCredential, TokenRequestContext tokenRequestContext) {
            this.tokenCredential = tokenCredential;
            this.tokenRequestContext = tokenRequestContext;
            this.timer = new Timer();
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
                timer.schedule(new TokenRefreshTask(), getTokenRefreshDelay());
            }
        }

        private long getTokenRefreshDelay() {
            return ((accessToken.getExpiresAt()
                .minusSeconds(ThreadLocalRandom.current().nextLong(baseRefreshOffset.getSeconds(), maxRefreshOffset.getSeconds()))
                .toEpochSecond() - OffsetDateTime.now().toEpochSecond()) * 1000);
        }
    }

    private static String extractUsernameFromToken(String token) {
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
