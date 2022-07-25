package com.azure.lettuce.sample;

import com.azure.core.credential.TokenRequestContext;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.SocketOptions;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisStringCommands;
import io.lettuce.core.codec.StringCodec;
import io.lettuce.core.protocol.ProtocolVersion;


public class HellloWorld {

    public static void main(String[] args) {

        //Construct a Token Credential from Identity library, e.g. DefaultAzureCredential / ClientSecretCredential / Client CertificateCredential / ManagedIdentityCredential etc.
        DefaultAzureCredential defaultAzureCredential = new DefaultAzureCredentialBuilder().build();

        // Fetch an Azure AD token to be used for authentication. The Azure AD token will be used as password.
        // Note: The Scopes parameter will change as the Azure AD Authentication support hits public preview and eventually GA's.
        String token = defaultAzureCredential
            .getToken(new TokenRequestContext()
                .addScopes("https://*.cacheinfra.windows.net:10225/appid/.default")).block().getToken();

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
        RedisStringCommands<String, String> sync = connection.sync();
        sync.set("Az:testKey", "testVal");
        System.out.println(sync.get("Az:testKey"));
    }
}
