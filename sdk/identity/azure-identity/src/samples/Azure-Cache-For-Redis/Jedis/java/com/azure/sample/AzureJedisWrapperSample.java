// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.sample;

import com.azure.identity.ClientCertificateCredential;
import com.azure.identity.ClientCertificateCredentialBuilder;
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
            .cacheHostName("<cache host name>")
            .port(6380)
            .useSSL(true)
            .username("<username>")
            .credential(defaultAzureCredential)
            .build();

        // Set a value against your key in the Redis cache.
        jedisClient.set("Az:key", "sample");

        // Close the Jedis Client
        jedisClient.close();
    }
}
