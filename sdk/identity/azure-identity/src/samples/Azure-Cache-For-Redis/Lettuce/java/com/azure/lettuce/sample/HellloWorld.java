// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.lettuce.sample;

import com.azure.core.credential.TokenRequestContext;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.SocketOptions;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.codec.StringCodec;
import io.lettuce.core.protocol.ProtocolVersion;

import java.nio.charset.StandardCharsets;
import java.util.Base64;


public class HellloWorld {

    public static void main(String[] args) {

        //Construct a Token Credential from Identity library, e.g. DefaultAzureCredential / ClientSecretCredential / Client CertificateCredential / ManagedIdentityCredential etc.
        DefaultAzureCredential defaultAzureCredential = new DefaultAzureCredentialBuilder().build();

        // Fetch a Microsoft Entra token to be used for authentication. The Microsoft Entra token will be used as password.
        // Note: The Scopes parameter will change as the Microsoft Entra authentication support hits public preview and eventually GA's.
        String token = defaultAzureCredential
            .getToken(new TokenRequestContext()
                .addScopes("https://redis.azure.com/.default")).block().getToken();

        String username = extractUsernameFromToken(token);

        // Build Redis URI with host and authentication details.
        // TODO: Replace Host Name with Azure Cache for Redis Host Name.
        RedisURI redisURI = RedisURI.Builder.redis("<HOST_NAME>") // Host Name is Required.
            .withPort(6380) //Port is Required.
            .withSsl(true) // SSL Connection is Required.
            .withAuthentication(username, token) // Username is Required.
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
