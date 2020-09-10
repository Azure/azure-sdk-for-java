// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.util.concurrent.CountDownLatch;

/**
 * Sample to demonstrate retrieving a schema from Schema Registry.
 */
public class GetSchemaSample {
    /**
     * The main method to run this program.
     * @param args Ignored args.
     */
    public static void main(String[] args) throws InterruptedException {
        TokenCredential tokenCredential = new DefaultAzureCredentialBuilder().build();

        SchemaRegistryAsyncClient schemaRegistryAsyncClient = new SchemaRegistryClientBuilder()
            .endpoint("{schema-registry-endpoint")
            .credential(tokenCredential)
            .buildAsyncClient();

        CountDownLatch countDownLatch = new CountDownLatch(1);
        // Register a schema
        schemaRegistryAsyncClient
            .getSchema("{schema-id}")
            .subscribe(schemaProperties -> {
                System.out.println("Successfully retrieved the schema " + schemaProperties.getSchemaName());
                countDownLatch.countDown();
            });

        // wait for the async task to complete
        countDownLatch.await();
    }
}
