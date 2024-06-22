// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.redisson.sample;

import com.azure.core.credential.TokenRequestContext;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.redisson.Redisson;
import org.redisson.api.RBucket;
import org.redisson.api.RBuckets;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

import java.nio.charset.StandardCharsets;
import java.util.Base64;


public class HelloWorld {

    public static void main(String[] args) {
        //Construct a Token Credential from Identity library, e.g. DefaultAzureCredential / ClientSecretCredential / Client CertificateCredential / ManagedIdentityCredential etc.
        DefaultAzureCredential defaultAzureCredential = new DefaultAzureCredentialBuilder().build();

        // Fetch a Microsoft Entra token to be used for authentication.
        String token = defaultAzureCredential
            .getToken(new TokenRequestContext()
                .addScopes("https://redis.azure.com/.default")).block().getToken();

        String username = extractUsernameFromToken(token);

        // Create Client Configuration
        Config config = new Config();
        config.useSingleServer()
            .setAddress("rediss://<HOST_NAME>:6380") // TODO: Replace Host Name with Azure Cache for Redis Host Name.
            .setKeepAlive(true) // Keep the connection alive.
            .setUsername(username) // Username is Required
            .setPassword(token) // Microsoft Entra access token as password is required.
            .setClientName("Reddison-Client");

        RedissonClient redisson = Redisson.create(config);

        // perform operations
        RBuckets rBuckets =  redisson.getBuckets();
        RBucket<String> bucket = redisson.getBucket("Az:key");
        bucket.set("This is object value");

        String objectValue = bucket.get();
        System.out.println("stored object value: " + objectValue);

        redisson.shutdown();
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
