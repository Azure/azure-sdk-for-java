package com.azure.lettuce.sample;

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
import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisException;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Timer;
import java.util.TimerTask;

public class AuthenticateWithTokenCache {

    public static void main(String[] args) {
        //Construct a Token Credential from Identity library, e.g. DefaultAzureCredential / ClientSecretCredential / Client CertificateCredential / ManagedIdentityCredential etc.
        DefaultAzureCredential defaultAzureCredential = new DefaultAzureCredentialBuilder().build();

        // Fetch an AAD token to be used for authentication. This token will be used as the password.
        // Note: The Scopes parameter will change as the Azure AD Authentication support hits public preview and eventually GA's.
        TokenRequestContext trc = new TokenRequestContext().addScopes("https://*.cacheinfra.windows.net:10225/appid/.default");

        // Instantiate the Token Refresh Cache, this cache will proactively refresh the access token 2 minutes before expiry.
        TokenRefreshCache tokenRefreshCache = new TokenRefreshCache(defaultAzureCredential, trc, Duration.ofMinutes(2));;
        AccessToken accessToken = tokenRefreshCache.getAccessToken();

        // Host Name, Port, Username and Azure AD Token are required here.
        // TODO: Replace <HOST_NAME> with Azure Cache for Redis Host name.
        RedisClient client = createLettuceRedisClient("<HOST_NAME>", 6380, "USERNAME", accessToken);
        StatefulRedisConnection<String, String> connection = client.connect(StringCodec.UTF8);

        int maxTries = 3;
        int i = 0;

        while (i < maxTries) {
            // Create the connection, in this case we're using a sync connection, but you can create async / reactive connections as needed.
            RedisStringCommands<String, String> sync = connection.sync();
            try {
                sync.set("Az:testKey", "testVal");
                System.out.println(sync.get("Az:testKey"));
                break;
            } catch (RedisException e) {
                // Handle the Exception as required in your application.
                e.printStackTrace();

                // For Exceptions containing Invalid Username Password / Permissions not granted error messages, look at troubleshooting section at the end of document.

                if (!connection.isOpen()) {
                    // Recreate the client with a fresh token non-expired token as password for authentication.
                    client = createLettuceRedisClient("<HOST_NAME>", 6380, "USERNAME", tokenRefreshCache.getAccessToken());
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

    // Helper code
    private static RedisClient createLettuceRedisClient(String hostName, int port, String username, AccessToken accessToken) {

        // Build Redis URI with host and authentication details.
        RedisURI redisURI = RedisURI.Builder.redis(hostName)
            .withPort(port)
            .withSsl(true) // Targeting SSL based port
            .withAuthentication(username, accessToken.getToken())
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
     * The token cache to store and proactively refresh the access token.
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
