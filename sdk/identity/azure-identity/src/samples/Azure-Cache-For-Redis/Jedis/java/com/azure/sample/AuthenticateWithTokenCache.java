package com.azure.sample;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
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

        // Fetch an Azure AD token to be used for authentication. This token will be used as the password.
        TokenRequestContext trc = new TokenRequestContext().addScopes("https://*.cacheinfra.windows.net:10225/appid/.default");
        TokenRefreshCache tokenRefreshCache = new TokenRefreshCache(defaultAzureCredential, trc, Duration.ofMinutes(2));;
        AccessToken accessToken = tokenRefreshCache.getAccessToken();

        // SSL connection is required for non 6379 ports.
        boolean useSsl = true;
        String cacheHostname = "YOUR_HOST_NAME.redis.cache.windows.net";

        // Create Jedis client and connect to the Azure Cache for Redis over the TLS/SSL port using the access token as password.
        Jedis jedis = createJedisClient(cacheHostname, 6380, "USERNAME", accessToken, useSsl);

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

                // Check if the client is broken, if it is then close and recreate it to create a new healthy connection.
                if (jedis.isBroken()) {
                    jedis.close();
                    jedis = createJedisClient(cacheHostname, 6380, "USERNAME", tokenRefreshCache.getAccessToken(), useSsl);
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
