package com.azure.lettuce.sample.withcredentialprovider;

import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import io.lettuce.core.*;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisStringCommands;
import io.lettuce.core.codec.StringCodec;
import io.lettuce.core.protocol.ProtocolVersion;

import java.util.Objects;

public class HandleReauthentication {

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
                // TODO: Handle the Exception as required in your application.
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
            .withAuthentication(RedisCredentialsProvider.from(() -> new AzureRedisCredentials("USERNAME", tokenCredential)))
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
            return tokenCredential
                .getToken(tokenRequestContext).block().getToken()
                .toCharArray();
        }

        @Override
        public boolean hasPassword() {
            return tokenCredential != null;
        }
    }
}
