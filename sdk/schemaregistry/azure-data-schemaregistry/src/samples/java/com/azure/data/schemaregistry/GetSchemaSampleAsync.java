// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.util.concurrent.CountDownLatch;

/**
 * Sample to demonstrate retrieving a schema from Schema Registry using the async client.
 */
public class GetSchemaSampleAsync {
    /**
     * The main method to run this program.
     * @param args Ignored args.
     */
    public static void main(String[] args) throws InterruptedException {
        TokenCredential tokenCredential = new DefaultAzureCredentialBuilder().build();

        SchemaRegistryAsyncClient schemaRegistryAsyncClient = new SchemaRegistryClientBuilder()
            .fullyQualifiedNamespace("{schema-registry-endpoint")
            .credential(tokenCredential)
            .buildAsyncClient();

        CountDownLatch countDownLatch = new CountDownLatch(1);

        // Get a schema using its id. The schema id is generated when it is registered via the client or Azure Portal.
        // `subscribe` is a non-blocking operation. It hooks up the callbacks and then moves onto the next line of code.
        schemaRegistryAsyncClient
            .getSchema("{schema-id}")
            .subscribe(schema -> {
                System.out.println("Successfully retrieved schema.");
                System.out.printf("Id: %s%nContents: %s%n", schema.getProperties().getId(), schema.getDefinition());

                countDownLatch.countDown();
            });

        // wait for the async task to complete
        countDownLatch.await();
    }
}
