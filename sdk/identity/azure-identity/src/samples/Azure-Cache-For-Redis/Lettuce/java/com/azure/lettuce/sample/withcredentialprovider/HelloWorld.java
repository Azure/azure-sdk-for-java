// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.lettuce.sample.withcredentialprovider;

import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisCredentialsProvider;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.codec.StringCodec;
import io.lettuce.core.protocol.ProtocolVersion;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.SocketOptions;
import io.lettuce.core.RedisCredentials;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
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
            .withAuthentication(RedisCredentialsProvider.from(() -> new AzureRedisCredentials(defaultAzureCredential))) // Username and Token Credential are required.
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
        RedisCommands<String, String> sync = connection.sync();
        sync.set("Az:testKey", "testVal");
        System.out.println(sync.get("Az:testKey"));
    }

    // Implementation of Redis Credentials used above.
    /**
     * Redis Credential Implementation for Azure Redis for Cache
     */
    public static class AzureRedisCredentials implements RedisCredentials {
        // Note: The Scopes value will change as the Microsoft Entra authentication support hits public preview and eventually GA's.
        private TokenRequestContext tokenRequestContext = new TokenRequestContext()
            .addScopes("https://redis.azure.com/.default");
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

        /**
         * Create instance of Azure Redis Credentials
         * @param tokenCredential the token credential to be used to fetch requests.
         */
        public AzureRedisCredentials(TokenCredential tokenCredential) {
            Objects.requireNonNull(tokenCredential, "Token Credential is required");
            this.tokenCredential = tokenCredential;
            this.username = extractUsernameFromToken(tokenCredential.getToken(tokenRequestContext).block().getToken());
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
