// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.lettuce.sample;

import com.azure.core.credential.TokenRequestContext;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.SocketOptions;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.codec.StringCodec;
import io.lettuce.core.protocol.ProtocolVersion;


public class HellloWorld {

    public static void main(String[] args) {

        //Construct a Token Credential from Identity library, e.g. DefaultAzureCredential / ClientSecretCredential / Client CertificateCredential / ManagedIdentityCredential etc.
        DefaultAzureCredential defaultAzureCredential = new DefaultAzureCredentialBuilder().build();

        // Fetch a Microsoft Entra token to be used for authentication. The Microsoft Entra token will be used as password.
        // Note: The Scopes parameter will change as the Microsoft Entra authentication support hits public preview and eventually GA's.
        String token = defaultAzureCredential
            .getToken(new TokenRequestContext()
                .addScopes("acca5fbb-b7e4-4009-81f1-37e38fd66d78/.default")).block().getToken();

        // Build Redis URI with host and authentication details.
        // TODO: Replace Host Name with Azure Cache for Redis Host Name.
        RedisURI redisURI = RedisURI.Builder.redis("<HOST_NAME>") // Host Name is Required.
            .withPort(6380) //Port is Required.
            .withSsl(true) // SSL Connection is Required.
            .withAuthentication("<USERNAME>", token) // Username is Required.
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
}
