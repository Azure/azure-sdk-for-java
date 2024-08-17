// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.lettuce.sample.withcredentialprovider;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.util.CoreUtils;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisCredentialsProvider;
import io.lettuce.core.RedisException;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.codec.StringCodec;
import io.lettuce.core.protocol.ProtocolVersion;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.SocketOptions;
import io.lettuce.core.RedisCredentials;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ThreadLocalRandom;

public class AuthenticateWithTokenCache {

    public static void main(String[] args) {

        //Construct a Token Credential from Identity library, e.g. DefaultAzureCredential / ClientSecretCredential / Client CertificateCredential / ManagedIdentityCredential etc.
        DefaultAzureCredential defaultAzureCredential = new DefaultAzureCredentialBuilder().build();

        // Host Name, Port, and Microsoft Entra token are required here.
        // TODO: Replace <HOST_NAME> with Azure Cache for Redis Host name.
        String hostName = "<HOST_NAME>";

        AzureRedisCredentials credentials = new AzureRedisCredentials(defaultAzureCredential);
        RedisClient client = createLettuceRedisClient(hostName, 6380, RedisCredentialsProvider.from(() -> credentials));
        StatefulRedisConnection<String, String> connection = client.connect(StringCodec.UTF8);

        // Create the connection, in this case we're using a sync connection, but you can create async / reactive connections as needed.
        RedisCommands<String, String> sync = connection.sync();

        credentials.getTokenCache()
            .setLettuceInstanceToAuthenticate(sync);

        int maxTries = 3;
        int i = 0;
        while (i < maxTries) {

            try {
                sync.set("Az:testKey", "testVal");
                System.out.println(sync.get("Az:testKey"));
                break;
            } catch (RedisException e) {
                // Handle the Exception as required in your application.
                e.printStackTrace();

                // For Exceptions containing Invalid Username Password / Permissions not granted error messages, look at troubleshooting section at the end of document.

                if (!connection.isOpen()) {
                    // Recreate the connection
                    connection = client.connect(StringCodec.UTF8);
                    sync = connection.sync();

                    credentials.getTokenCache()
                        .setLettuceInstanceToAuthenticate(sync);
                }
            } catch (Exception e) {
                // Handle the Exception as required in your application.
                e.printStackTrace();
            }
            i++;
        }
    }

    // Helper Code
    private static RedisClient createLettuceRedisClient(String hostName, int port, RedisCredentialsProvider credentialsProvider) {

        // Build Redis URI with host and authentication details.
        RedisURI redisURI = RedisURI.Builder.redis(hostName)
            .withPort(port)
            .withSsl(true) // Targeting SSL Based 6380 port.
            .withAuthentication(credentialsProvider)
            .withClientName("LettuceClient")
            .build();

        // Create Lettuce Redis Client
        RedisClient client = RedisClient.create(redisURI);

        // Configure the client options.
        client.setOptions(ClientOptions.builder()
            .socketOptions(SocketOptions.builder()
                .keepAlive(true) // Keep the connection alive to work with Azure Redis Cache
                .build())
            .protocolVersion(ProtocolVersion.RESP2) // Use RESP2 Protocol to ensure AUTH command is used for handshake.
            .build());

        return client;
    }

    /**
     * Redis Credential Implementation for Azure Redis for Cache
     */
    public static class AzureRedisCredentials implements RedisCredentials {
        private TokenRequestContext tokenRequestContext = new TokenRequestContext()
            .addScopes("https://redis.azure.com/.default");
        private TokenCredential tokenCredential;
        private TokenRefreshCache refreshCache;
        private final String username;

        /**
         * Create instance of Azure Redis Credentials
         * @param username the username to be used for authentication.
         * @param tokenCredential the token credential to be used to fetch requests.
         */
        public AzureRedisCredentials(String username, TokenCredential tokenCredential) {
            Objects.requireNonNull(username, "Username is required");
            Objects.requireNonNull(tokenCredential, "Token Credential is required");
            this.username = username;
            this.tokenCredential = tokenCredential;
            this.refreshCache = new TokenRefreshCache(tokenCredential, tokenRequestContext);
        }

        /**
         * Create instance of Azure Redis Credentials
         * @param tokenCredential the token credential to be used to fetch requests.
         */
        public AzureRedisCredentials(TokenCredential tokenCredential) {
            Objects.requireNonNull(tokenCredential, "Token Credential is required");
            this.tokenCredential = tokenCredential;
            this.username = extractUsernameFromToken(tokenCredential.getToken(tokenRequestContext).block().getToken());
            this.refreshCache = new TokenRefreshCache(tokenCredential, tokenRequestContext);
        }

        @Override
        public String getUsername() {
            return username;
        }

        @Override
        public boolean hasUsername() {
            return username != null;
        }

        @Override
        public char[] getPassword() {
            return refreshCache.getAccessToken()
                .getToken().toCharArray();
        }

        @Override
        public boolean hasPassword() {
            return tokenCredential != null;
        }

        public TokenRefreshCache getTokenCache() {
            return this.refreshCache;
        }
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
        private RedisCommands<String, String> lettuceInstanceToAuthenticate;
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
                username = extractUsernameFromToken(accessToken.getToken());
                System.out.println("Refreshed Token with Expiry: " + accessToken.getExpiresAt().toEpochSecond());

                if (lettuceInstanceToAuthenticate != null && !CoreUtils.isNullOrEmpty(username)) {
                    lettuceInstanceToAuthenticate.auth(username, accessToken.getToken());
                    System.out.println("Refreshed Lettuce Connection with fresh access token, token expires at : "
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
         * Sets the Lettuce instance to proactively authenticate before token expiry.
         * @param lettuceInstanceToAuthenticate the instance to authenticate
         * @return the updated instance
         */
        public TokenRefreshCache setLettuceInstanceToAuthenticate(RedisCommands<String, String> lettuceInstanceToAuthenticate) {
            this.lettuceInstanceToAuthenticate = lettuceInstanceToAuthenticate;
            return this;
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
