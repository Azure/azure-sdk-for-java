// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry;

import com.azure.core.credential.TokenCredential;
import com.azure.data.schemaregistry.models.SchemaFormat;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.util.concurrent.CountDownLatch;

/**
 * Sample to demonstrate retrieving the schema id of a schema from Schema Registry.
 */
public class GetSchemaIdSample {

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
        // Register a schema
        schemaRegistryAsyncClient
            .getSchemaProperties("{group-name}", "{schema-name}", "{schema-string}", SchemaFormat.AVRO)
            .subscribe(schemaId -> {
                System.out.println("Successfully retrieved the schema id: " + schemaId);
                countDownLatch.countDown();
            });

        // wait for the async task to complete
        countDownLatch.await();

    }
}
