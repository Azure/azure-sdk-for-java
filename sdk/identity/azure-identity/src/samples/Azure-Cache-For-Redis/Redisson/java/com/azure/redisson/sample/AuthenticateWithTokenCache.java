package com.azure.redisson.sample;

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
import org.redisson.Redisson;
import org.redisson.api.RBucket;
import org.redisson.api.RBuckets;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
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
        // Note: The Scopes parameter will change as the Azure AD Authentication support hits public preview and eventually GA's.
        TokenRequestContext trc = new TokenRequestContext().addScopes("https://*.cacheinfra.windows.net:10225/appid/.default");

        // Instantiate the Token Refresh Cache, this cache will proactively refresh the access token 2 minutes before expiry.
        TokenRefreshCache tokenRefreshCache = new TokenRefreshCache(defaultAzureCredential, trc, Duration.ofMinutes(2));;
        AccessToken accessToken = tokenRefreshCache.getAccessToken();

        // Create Redisson Client
        // Host Name, Port, Username and Azure AD Token are required here.
        // TODO: Replace <HOST_NAME> with Azure Cache for Redis Host name.
        RedissonClient redisson = createRedissonClient("redis://<HOST_NAME>:6380", "<USERNAME>", accessToken);

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
                    // Recreate the client with a fresh token non-expired token as password for authentication.
                    redisson = createRedissonClient("redis://<HOST_NAME>:6380", "<USERNAME>", tokenRefreshCache.getAccessToken());
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
