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

public class HandleReauthentication {

    public static void main(String[] args) {

        //Construct a Token Credential from Identity library, e.g. DefaultAzureCredential / ClientSecretCredential / Client CertificateCredential / ManagedIdentityCredential etc.
        DefaultAzureCredential defaultAzureCredential = new DefaultAzureCredentialBuilder().build();

        // Fetch an Azure AD token to be used for authentication. This token will be used as the password.
        // Note: The Scopes parameter will change as the Azure AD Authentication support hits public preview and eventually GA's.
        TokenRequestContext trc = new TokenRequestContext().addScopes("https://*.cacheinfra.windows.net:10225/appid/.default");
        AccessToken accessToken = getAccessToken(defaultAzureCredential, trc);

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

                String objectValue = bucket.get();
                System.out.println("stored object value: " + objectValue);
                break;
            } catch (RedisException exception) {
                // TODO: Handle Exception as Required.
                exception.printStackTrace();

                // For Exceptions containing Invalid Username Password / Permissions not granted error messages, look at troubleshooting section at the end of document.

                if (redisson.isShutdown()) {
                    // Recreate the client with a fresh token non-expired token as password for authentication.
                    redisson = createRedissonClient("redis://<HOST_NAME>:6380", "<USERNAME>", getAccessToken(defaultAzureCredential, trc));
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

    private static AccessToken getAccessToken(TokenCredential tokenCredential, TokenRequestContext trc) {
        return tokenCredential.getToken(trc).block();
    }
}
