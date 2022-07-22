// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.sample;

import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.jedis.AzureJedisClientBuilder;
import redis.clients.jedis.Jedis;

public class AzureJedisWrapperSample {
    public static void main(String[] args) {

        //Construct a Token Credential from Identity library, e.g. DefaultAzureCredential / ClientSecretCredential / Client CertificateCredential / ManagedIdentityCredential etc.
        DefaultAzureCredential defaultAzureCredential = new DefaultAzureCredentialBuilder().build();

        // Create Jedis Client using the builder as follows.
        Jedis jedisClient = new AzureJedisClientBuilder()
            .cacheHostName("<HOST_NAME>") // TODO: Replace <HOST_NAME> with Azure Cache for Redis Host name.
            .port(6380) // Port is requried.
            .useSSL(true) // SSL Connection is required.
            .username("<USERNAME>") // Username is required.
            .credential(defaultAzureCredential) // A Token Credential is required to fetch Azure AD Access tokens.
            .build();

        // Set a value against your key in the Redis cache.
        jedisClient.set("Az:key", "sample");

        // Close the Jedis Client
        jedisClient.close();
    }
}
