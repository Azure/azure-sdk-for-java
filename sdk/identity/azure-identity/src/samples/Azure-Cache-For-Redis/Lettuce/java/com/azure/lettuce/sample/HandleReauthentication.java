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

public class HandleReauthentication {

    public static void main(String[] args) {
        //Construct a Token Credential from Identity library, e.g. DefaultAzureCredential / ClientSecretCredential / Client CertificateCredential / ManagedIdentityCredential etc.
        DefaultAzureCredential defaultAzureCredential = new DefaultAzureCredentialBuilder().build();

        // Fetch an AAD token to be used for authentication. This token will be used as the password.
        // Note: The Scopes parameter will change as the Azure AD Authentication support hits public preview and eventually GA's.
        TokenRequestContext trc = new TokenRequestContext().addScopes("https://*.cacheinfra.windows.net:10225/appid/.default");
        AccessToken accessToken = getAccessToken(defaultAzureCredential, trc);

        // Host Name, Port, Username and Azure AD Token are required here.
        // TODO: Replace <HOST_NAME> with Azure Cache for Redis Host name.
        RedisClient client = createLettuceRedisClient("<HOST_NAME>", 6380, "<USERNAME>", accessToken);
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
                // TODO: Handle the Exception as required in your application.
                e.printStackTrace();

                // For Exceptions containing Invalid Username Password / Permissions not granted error messages, look at troubleshooting section at the end of document.

                if (!connection.isOpen()) {
                    // Recreate the client with a fresh token non-expired token as password for authentication.
                    client = createLettuceRedisClient("<HOST_NAME>", 6380, "USERNAME", getAccessToken(defaultAzureCredential, trc));
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

    private static AccessToken getAccessToken(TokenCredential tokenCredential, TokenRequestContext trc) {
        return tokenCredential.getToken(trc).block();
    }
}
