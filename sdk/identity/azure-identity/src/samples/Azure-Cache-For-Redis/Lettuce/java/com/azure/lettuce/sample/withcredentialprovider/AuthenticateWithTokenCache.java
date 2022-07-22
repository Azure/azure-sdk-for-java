package com.azure.lettuce.sample.withcredentialprovider;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import io.lettuce.core.*;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisStringCommands;
import io.lettuce.core.codec.StringCodec;
import io.lettuce.core.protocol.ProtocolVersion;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class AuthenticateWithTokenCache {

    public static void main(String[] args) {
        //Construct a Token Credential from Identity library, e.g. DefaultAzureCredential / ClientSecretCredential / Client CertificateCredential / ManagedIdentityCredential etc.
        DefaultAzureCredential defaultAzureCredential = new DefaultAzureCredentialBuilder().build();

        // Host Name, Port, Username and Azure AD Token are required here.
        // TODO: Replace <HOST_NAME> with Azure Cache for Redis Host name.
        RedisClient client = createLettuceRedisClient("<HOST_NAME>", 6380, "<USERNAME>", defaultAzureCredential);
        StatefulRedisConnection<String, String> connection = client.connect(StringCodec.UTF8);

        int maxTries = 3;
        int i = 0;
        while (i < maxTries) {
            // Create the connection, in this case we're using a sync connection, but you can create async / reactive connections as needed.
            RedisStringCommands sync = connection.sync();
            try {
                sync.set("Az:testKey", "testVal");
                System.out.println(sync.get("Az:testKey").toString());
            } catch (RedisException e) {
                // Handle the Exception as required in your application.
                e.printStackTrace();

                // For Exceptions containing Invalid Username Password / Permissions not granted error messages, look at troubleshooting section at the end of document.

                if (!connection.isOpen()) {
                    // Recreate the connection
                    connection = client.connect(StringCodec.UTF8);
                    sync = connection.sync();
                }
            } catch (Exception e) {
                // Handle the Exception as required in your application.
                e.printStackTrace();
            }
            i++;
        }
    }

    // Helper Code
    private static RedisClient createLettuceRedisClient(String hostName, int port, String username, TokenCredential tokenCredential) {

        // Build Redis URI with host and authentication details.
        RedisURI redisURI = RedisURI.Builder.redis(hostName)
            .withPort(port)
            .withSsl(true) // Targeting SSL Based 6380 port.
            .withAuthentication(RedisCredentialsProvider.from(() -> new HandleReauthentication.AzureRedisCredentials("USERNAME", tokenCredential)))
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
            .addScopes("https://*.cacheinfra.windows.net:10225/appid/.default");
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
            this.refreshCache = new TokenRefreshCache(tokenCredential, tokenRequestContext, Duration.ofMinutes(2));
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
    }

    /**
     * The Token Cache to store and proactively refresh the Access Token.
     */
    public static class TokenRefreshCache {
        private final TokenCredential tokenCredential;
        private final TokenRequestContext tokenRequestContext;
        private final Timer timer;
        private volatile AccessToken accessToken;
        private final Duration refreshOffset;

        /**
         * Creates an instance of TokenRefreshCache
         * @param tokenCredential the token credential to be used for authentication.
         * @param tokenRequestContext the token request context to be used for authentication.
         * @param refreshOffset the refresh offset to use to proactively fetch a new access token before expiry time.
         */
        public TokenRefreshCache(TokenCredential tokenCredential, TokenRequestContext tokenRequestContext, Duration refreshOffset) {
            this.tokenCredential = tokenCredential;
            this.tokenRequestContext = tokenRequestContext;
            this.timer = new Timer();
            this.refreshOffset = refreshOffset;
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
                .minusSeconds(refreshOffset.getSeconds()))
                .toEpochSecond() - OffsetDateTime.now().toEpochSecond()) * 1000;
        }
    }
}
