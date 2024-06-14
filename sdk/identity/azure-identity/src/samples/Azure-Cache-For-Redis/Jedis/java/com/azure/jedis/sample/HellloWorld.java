// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.jedis.sample;

import com.azure.core.credential.TokenRequestContext;
import com.azure.identity.AzureCliCredential;
import com.azure.identity.AzureCliCredentialBuilder;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.Jedis;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class HellloWorld {

    public static void main(String[] args) {
        //Construct a Token Credential from Identity library, e.g. DefaultAzureCredential / ClientSecretCredential / Client CertificateCredential / ManagedIdentityCredential etc.
        DefaultAzureCredential defaultAzureCredential = new DefaultAzureCredentialBuilder().build();
        // Fetch a Microsoft Entra token to be used for authentication. This token will be used as the password.
        String token = defaultAzureCredential
            .getTokenSync(new TokenRequestContext()
                .addScopes("https://redis.azure.com/.default")).getToken();


        // SSL connection is required.
        boolean useSsl = true;
        // TODO: Replace Host Name with Azure Cache for Redis Host Name.
        String cacheHostname = "<Host-Name>";
        String username = extractUsernameFromToken(token);

        // Create Jedis client and connect to the Azure Cache for Redis over the TLS/SSL port using the access token as password.
        // Note, Redis Cache Host Name and Port are required below
        Jedis jedis = new Jedis(cacheHostname, 6380, DefaultJedisClientConfig.builder()
            .password(token) // Microsoft Entra access token as password is required.
            .user(username) // Username is Required
            .ssl(useSsl) // SSL Connection is Required
            .build());

        // Set a value against your key in the Redis cache.
        jedis.set("Az:key", "testValue");
        System.out.println(jedis.get("Az:key"));

        // Close the Jedis Client
        jedis.close();
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
