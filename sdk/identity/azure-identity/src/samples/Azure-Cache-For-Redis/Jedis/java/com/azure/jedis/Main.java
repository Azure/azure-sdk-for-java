// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.jedis;

import com.azure.identity.ClientCertificateCredential;
import com.azure.identity.ClientCertificateCredentialBuilder;
import redis.clients.jedis.Jedis;

public class Main {
    public static void main(String[] args) {

        // Construct a Token Credential from Identity library, e.g. ClientSecretCredential / Client CertificateCredential / ManagedIdentityCredential etc.
        ClientCertificateCredential clientCertificateCredential = new ClientCertificateCredentialBuilder()
            .clientId("<clientId>")
            .pfxCertificate("<Cert-File-Path>", "<Cert-Password-if-Applicable>")
            .tenantId("<tenantId>")
            .build();

        // Create Jedis Client using the builder as follows.
        Jedis jedisClient = new AzureJedisClientBuilder()
            .cacheHostName("<cache host name>")
            .port(6380)
            .useSSL(true)
            .username("<username>")
            .credential(clientCertificateCredential)
            .build();

        // Set a value against your key in the Redis cache.
        jedisClient.set("Az:key", "sample");

        // Close the Jedis Client
        jedisClient.close();
    }
}
