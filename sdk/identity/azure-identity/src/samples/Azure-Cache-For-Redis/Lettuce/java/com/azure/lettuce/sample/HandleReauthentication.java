// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.lettuce.sample;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisException;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.codec.StringCodec;
import io.lettuce.core.protocol.ProtocolVersion;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.SocketOptions;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class HandleReauthentication {

    public static void main(String[] args) {
        //Construct a Token Credential from Identity library, e.g. DefaultAzureCredential / ClientSecretCredential / Client CertificateCredential / ManagedIdentityCredential etc.
        DefaultAzureCredential defaultAzureCredential = new DefaultAzureCredentialBuilder().build();

        // Fetch a Microsoft Entra token to be used for authentication. This token will be used as the password.
        // Note: The Scopes parameter will change as the Microsoft Entra authentication support hits public preview and eventually GA's.
        TokenRequestContext trc = new TokenRequestContext().addScopes("https://redis.azure.com/.default");
        AccessToken accessToken = getAccessToken(defaultAzureCredential, trc);

        // Host Name, Port, and Microsoft Entra token are required here.
        // TODO: Replace <HOST_NAME> with Azure Cache for Redis Host name.
        String hostName = "<HOST_NAME>";
        RedisClient client = createLettuceRedisClient(hostName, 6380, accessToken);
        StatefulRedisConnection<String, String> connection = client.connect(StringCodec.UTF8);

        int maxTries = 3;
        int i = 0;

        while (i < maxTries) {
            // Create the connection, in this case we're using a sync connection, but you can create async / reactive connections as needed.
            RedisCommands<String, String> sync = connection.sync();
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
                    client = createLettuceRedisClient(hostName, 6380, getAccessToken(defaultAzureCredential, trc));
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
    private static RedisClient createLettuceRedisClient(String hostName, int port, AccessToken accessToken) {

        // Build Redis URI with host and authentication details.
        RedisURI redisURI = RedisURI.Builder.redis(hostName)
            .withPort(port)
            .withSsl(true) // Targeting SSL based port
            .withAuthentication(extractUsernameFromToken(accessToken.getToken()), accessToken.getToken())
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
