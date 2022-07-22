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


public class HelloWorld {

    public static void main(String[] args) {
        //Construct a Token Credential from Identity library, e.g. DefaultAzureCredential / ClientSecretCredential / Client CertificateCredential / ManagedIdentityCredential etc.
        DefaultAzureCredential defaultAzureCredential = new DefaultAzureCredentialBuilder().build();

        // Build Redis URI with host and authentication details.
        // TODO: Replace Host Name with Azure Cache for Redis Host Name.
        RedisURI redisURI = RedisURI.Builder.redis("<HOST_NAME>") // Host Name is Required
            .withPort(6380) // Port is Required
            .withSsl(true) // SSL Connections are required.
            .withAuthentication(RedisCredentialsProvider.from(() -> new AzureRedisCredentials("USERNAME", defaultAzureCredential))) // Username and Token Credential are required.
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

        StatefulRedisConnection<String, String> connection = client.connect(StringCodec.UTF8);

        // Create the connection, in this case we're using a sync connection, but you can create async / reactive connections as needed.
        RedisStringCommands sync = connection.sync();
        sync.set("Az:testKey", "testVal");
        System.out.println(sync.get("Az:testKey").toString());
    }

    // Implementation of Redis Credentials used above.
    /**
     * Redis Credential Implementation for Azure Redis for Cache
     */
    public static class AzureRedisCredentials implements RedisCredentials {
        // Note: The Scopes value will change as the Azure AD Authentication support hits public preview and eventually GA's.
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
