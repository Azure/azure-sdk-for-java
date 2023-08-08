// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry;

import com.azure.core.credential.TokenCredential;
import com.azure.data.schemaregistry.models.SchemaFormat;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.util.concurrent.CountDownLatch;

/**
 * Sample to demonstrate registering a schema with Schema Registry.
 *
 * @see RegisterSchemaSample for the sync sample.
 */
public class RegisterSchemaSampleAsync {
    /**
     * The main method to run this program.
     *
     * @param args Ignored args.
     */
    public static void main(String[] args) throws InterruptedException {
        TokenCredential tokenCredential = new DefaultAzureCredentialBuilder().build();

        SchemaRegistryAsyncClient schemaRegistryAsyncClient = new SchemaRegistryClientBuilder()
            .fullyQualifiedNamespace("{schema-registry-endpoint}")
            .credential(tokenCredential)
            .buildAsyncClient();

        CountDownLatch countDownLatch = new CountDownLatch(1);

        // Register a schema
        // `subscribe` is a non-blocking operation. It hooks up the callbacks and then moves onto the next line of code.
        schemaRegistryAsyncClient
            .registerSchema("{group-name}", "{schema-name}", "{schema-string}", SchemaFormat.AVRO)
            .subscribe(schemaProperties -> {
                System.out.println("Successfully registered a schema with id " + schemaProperties.getId());
                System.out.println("Schema Group: " + schemaProperties.getGroupName());
                System.out.println("Schema Name: " + schemaProperties.getName());

                countDownLatch.countDown();
            });

        // wait for the async task to complete
        countDownLatch.await();
    }
}
