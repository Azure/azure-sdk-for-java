// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.jedis.sample;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisException;

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

        // SSL connection is required.
        boolean useSsl = true;
        // TODO: Replace <HOST_NAME> with Azure Cache for Redis Host name.
        String cacheHostname = "<HOST_NAME>";
        String username = extractUsernameFromToken(accessToken.getToken());

        // Create Jedis client and connect to the Azure Cache for Redis over the TLS/SSL port using the access token as password.
        // Note: Cache Host Name, Port, Username, Microsoft Entra access token, and SSL connections are required below.
        Jedis jedis = createJedisClient(cacheHostname, 6380, username, accessToken, useSsl);

        int maxTries = 3;
        int i = 0;

        while (i < maxTries) {
            try {
                // Set a value against your key in the Redis cache.
                jedis.set("Az:key", "testValue");
                System.out.println(jedis.get("Az:key"));
                break;
            } catch (JedisException e) {
                // TODO: Handle The Exception as required in your application.
                e.printStackTrace();

                // For Exceptions containing Invalid Username Password / Permissions not granted error messages, look at troubleshooting section at the end of document.

                // Check if the client is broken, if it is then close and recreate it to create a new healthy connection.
                if (jedis.isBroken()) {
                    jedis.close();
                    accessToken = getAccessToken(defaultAzureCredential, trc);
                    jedis = createJedisClient(cacheHostname, 6380, username, accessToken, useSsl);
                }
            }
            i++;
        }

        // Close the Jedis Client
        jedis.close();
    }

    // Helper Code
    private static Jedis createJedisClient(String cacheHostname, int port, String username, AccessToken accessToken, boolean useSsl) {
        return new Jedis(cacheHostname, port, DefaultJedisClientConfig.builder()
            .password(accessToken.getToken())
            .user(username)
            .ssl(useSsl)
            .build());
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
